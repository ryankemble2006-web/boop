package com.boop.shieldhome;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Bounded HTTPS transport. No cookies, login storage or redirecting bearer tokens. */
final class DeezerLyricsHttp implements DeezerLyricsClient.Transport {
    @Override public String request(String url, String bearer, String body,
            DeezerLyricsClient.Call call, long deadline) throws Exception {
        if (!DeezerLyricsClient.AUTH_URL.equals(url) && !DeezerLyricsClient.API_URL.equals(url))
            throw new IOException("Unexpected lyrics endpoint");
        int remaining = remaining(call, deadline);
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        call.attach(connection);
        try {
            connection.setConnectTimeout(remaining);
            connection.setReadTimeout(remaining);
            connection.setInstanceFollowRedirects(false);
            connection.setUseCaches(false);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "BOOP/lyrics-check");
            if (bearer != null) connection.setRequestProperty("Authorization", "Bearer " + bearer);
            if (body != null) {
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(bytes.length);
                try (java.io.OutputStream output = connection.getOutputStream()) { output.write(bytes); }
            }
            connection.setReadTimeout(remaining(call, deadline));
            if (connection.getResponseCode() != 200) throw new IOException("Lyrics service unavailable");
            if (connection.getContentLengthLong() > DeezerLyricsClient.MAX_RESPONSE_BYTES)
                throw new IOException("Oversized lyrics response");
            try (InputStream input = connection.getInputStream();
                    ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                while (true) {
                    connection.setReadTimeout(remaining(call, deadline));
                    int read = input.read(buffer);
                    if (read == -1) break;
                    if (output.size() + read > DeezerLyricsClient.MAX_RESPONSE_BYTES)
                        throw new IOException("Oversized lyrics response");
                    output.write(buffer, 0, read);
                }
                remaining(call, deadline);
                return new String(output.toByteArray(), StandardCharsets.UTF_8);
            }
        } finally {
            call.detach(connection);
            connection.disconnect();
        }
    }
    private static int remaining(DeezerLyricsClient.Call call, long deadline) throws IOException {
        long left = deadline - DeezerLyricsClient.nowMs();
        if (call.cancelled() || left <= 0) throw new InterruptedIOException("Lyrics check ended");
        return (int) Math.min(left, 2500L);
    }
}
