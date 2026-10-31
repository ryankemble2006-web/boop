package com.boop.shieldhome;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONObject;

/** One request per local connection. Mutations are deliberately never retried. */
final class KodiJsonRpc implements Closeable {
    private static final AtomicInteger IDS = new AtomicInteger();
    private final String host;
    private final int port;
    private final Set<Socket> active = new HashSet<>();
    private boolean closed;

    KodiJsonRpc() { this("127.0.0.1", 9090); }
    KodiJsonRpc(String host, int port) { this.host = host; this.port = port; }

    JSONObject call(String method, JSONObject params, int timeoutMs) throws IOException {
        Socket socket = new Socket();
        synchronized (active) {
            if (closed) throw new IOException("Kodi connection closed");
            active.add(socket);
        }
        long deadline = System.nanoTime() + timeoutMs * 1_000_000L;
        try (Socket owned = socket) {
            owned.connect(new InetSocketAddress(host, port), Math.min(timeoutMs, 1200));
            owned.setSoTimeout(timeoutMs);
            int id = IDS.incrementAndGet();
            JSONObject request = new JSONObject().put("jsonrpc", "2.0").put("id", id)
                    .put("method", method).put("params", params);
            owned.getOutputStream().write(request.toString().getBytes(StandardCharsets.UTF_8));
            owned.getOutputStream().flush();
            Reader reader = new InputStreamReader(owned.getInputStream(), StandardCharsets.UTF_8) {
                @Override public int read() throws IOException {
                    if (Thread.currentThread().isInterrupted() || System.nanoTime() > deadline)
                        throw new InterruptedIOException("Kodi request timed out");
                    return super.read();
                }
            };
            while (System.nanoTime() < deadline) {
                JSONObject reply = new JSONObject(readObject(reader));
                if (reply.optInt("id", -1) != id) continue;
                if (reply.has("error")) throw new IOException("Kodi could not complete the request");
                if (!reply.has("result")) throw new IOException("Incomplete Kodi response");
                return reply;
            }
            throw new IOException("Kodi request timed out");
        } catch (org.json.JSONException malformed) {
            throw new IOException("Invalid Kodi response", malformed);
        } finally {
            synchronized (active) { active.remove(socket); }
        }
    }

    static String readObject(Reader reader) throws IOException {
        StringBuilder out = new StringBuilder();
        int depth = 0;
        boolean string = false, escaped = false;
        for (;;) {
            int next = reader.read();
            if (next == -1) throw new IOException("Kodi closed the connection");
            char c = (char) next;
            if (out.length() == 0) {
                if (Character.isWhitespace(c)) continue;
                if (c != '{') throw new IOException("Invalid Kodi frame");
            }
            out.append(c);
            if (out.length() > 4_000_000) throw new IOException("Kodi response is too large");
            if (string) {
                if (escaped) escaped = false;
                else if (c == '\\') escaped = true;
                else if (c == '"') string = false;
            } else {
                if (c == '"') string = true;
                else if (c == '{') depth++;
                else if (c == '}' && --depth == 0) return out.toString();
            }
        }
    }

    @Override public void close() {
        synchronized (active) {
            closed = true;
            for (Socket socket : active) try { socket.close(); } catch (IOException ignored) { }
            active.clear();
        }
    }
}
