package com.boop.shieldhome;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.json.JSONArray;
import org.json.JSONObject;

/** Keyless synced-lyrics fallback. Deezer remains the primary source. */
final class LrclibLyricsClient {
    private static final String BASE = "https://lrclib.net/api";
    private static final int MAX_RESPONSE = 524288;

    DeezerLyricsDocument load(NowPlayingSnapshot track, String cacheId,
            DeezerLyricsClient.Call call, long deadline) {
        if (track == null || track.title().isEmpty() || track.subtitle().isEmpty()
                || call == null || call.cancelled() || DeezerLyricsClient.nowMs() >= deadline)
            return DeezerLyricsDocument.unknown(cacheId);
        try {
            JSONObject exact = get(track, true, call, deadline);
            DeezerLyricsDocument parsed = parseCandidate(exact, track, cacheId);
            if (parsed.status() == DeezerLyricsDocument.Status.AVAILABLE) return parsed;

            exact = get(track, false, call, deadline);
            parsed = parseCandidate(exact, track, cacheId);
            if (parsed.status() == DeezerLyricsDocument.Status.AVAILABLE) return parsed;

            JSONArray search = search(track, call, deadline);
            if (search != null) {
                for (int i = 0; i < search.length(); i++) {
                    JSONObject candidate = search.optJSONObject(i);
                    if (!matches(candidate, track)) continue;
                    parsed = parseCandidate(candidate, track, cacheId);
                    if (parsed.status() == DeezerLyricsDocument.Status.AVAILABLE) return parsed;
                }
            }
            return DeezerLyricsDocument.unavailableFallback(cacheId);
        } catch (Exception unavailable) {
            return DeezerLyricsDocument.unknown(cacheId);
        }
    }

    private static JSONObject get(NowPlayingSnapshot track, boolean album,
            DeezerLyricsClient.Call call, long deadline) throws Exception {
        StringBuilder url = new StringBuilder(BASE).append("/get?track_name=")
                .append(enc(track.title())).append("&artist_name=").append(enc(track.subtitle()));
        if (album && !track.album().isEmpty()) url.append("&album_name=").append(enc(track.album()));
        if (track.durationMs() > 0L) url.append("&duration=").append(Math.round(track.durationMs()/1000.0));
        String body = request(url.toString(), call, deadline, true);
        return body == null ? null : new JSONObject(body);
    }

    private static JSONArray search(NowPlayingSnapshot track,
            DeezerLyricsClient.Call call, long deadline) throws Exception {
        String url = BASE + "/search?track_name=" + enc(track.title())
                + "&q=" + enc(track.title());
        String body = request(url, call, deadline, true);
        return body == null ? null : new JSONArray(body);
    }

    private static String request(String address, DeezerLyricsClient.Call call,
            long deadline, boolean notFoundIsNull) throws Exception {
        if (call.cancelled() || DeezerLyricsClient.nowMs() >= deadline) return null;
        HttpURLConnection connection=(HttpURLConnection)new URL(address).openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(1800);
        connection.setReadTimeout(1800);
        connection.setRequestProperty("User-Agent","BOOP-Shield/1");
        call.attach(connection);
        try {
            int code=connection.getResponseCode();
            if (code==404 && notFoundIsNull) return null;
            if (code!=200) throw new java.io.IOException("LRCLIB HTTP "+code);
            StringBuilder body=new StringBuilder();
            try(Reader reader=new InputStreamReader(connection.getInputStream(),"UTF-8")) {
                char[] buffer=new char[4096]; int count;
                while((count=reader.read(buffer))!=-1) {
                    if(body.length()+count>MAX_RESPONSE) throw new java.io.IOException("LRCLIB response too large");
                    body.append(buffer,0,count);
                }
            }
            return body.toString();
        } finally {
            call.detach(connection);
            connection.disconnect();
        }
    }

    private static DeezerLyricsDocument parseCandidate(JSONObject candidate,
            NowPlayingSnapshot track, String cacheId) {
        if (!matches(candidate, track)) return DeezerLyricsDocument.unknown(cacheId);
        String synced=candidate == null ? "" : candidate.optString("syncedLyrics","");
        if (synced == null || synced.trim().isEmpty())
            return DeezerLyricsDocument.unavailableFallback(cacheId);
        return DeezerLyricsDocument.fromLrc(synced, cacheId, track.durationMs());
    }

    private static boolean matches(JSONObject row, NowPlayingSnapshot track) {
        if (row == null) return false;
        if (!sameText(row.optString("trackName"), track.title())) return false;
        if (!sameText(row.optString("artistName"), track.subtitle())) return false;
        double duration=row.optDouble("duration",-1);
        return duration <= 0 || track.durationMs() <= 0
                || Math.abs(duration*1000.0-track.durationMs()) <= 3500.0;
    }

    private static boolean sameText(String one, String two) {
        String left = norm(one), right = norm(two);
        if (left.equals(right)) return true;
        if (left.startsWith("the ")) left = left.substring(4);
        if (right.startsWith("the ")) right = right.substring(4);
        return left.equals(right);
    }

    private static String norm(String value) {
        return value == null ? "" : value.toLowerCase(java.util.Locale.ROOT)
                .replaceAll("\\([^)]*\\)$","")
                .replaceAll("\\[[^]]*\\]$","")
                .replaceAll("[^\\p{L}\\p{N}]+"," ").trim().replaceAll("\\s+"," ");
    }

    private static String enc(String value) throws Exception {
        return URLEncoder.encode(value == null ? "" : value,"UTF-8");
    }
}
