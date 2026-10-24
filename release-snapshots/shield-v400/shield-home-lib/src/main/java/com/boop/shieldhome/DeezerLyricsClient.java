package com.boop.shieldhome;

import org.json.JSONArray;
import org.json.JSONObject;

/** Exact-recording availability only. Never fetches or retains lyric text. */
final class DeezerLyricsClient {
    enum Result { AVAILABLE, UNAVAILABLE, UNKNOWN }
    static final int MAX_RESPONSE_BYTES = 262144;

    static boolean validTrackId(String id) {
        return id != null && id.matches("[1-9][0-9]{0,18}");
    }
    static Result classify(String body, String expectedId) {
        if (!validTrackId(expectedId) || body == null || body.length() > MAX_RESPONSE_BYTES)
            return Result.UNKNOWN;
        try {
            JSONObject root = new JSONObject(body);
            if (root.has("errors") && !root.isNull("errors")) {
                JSONArray errors = root.optJSONArray("errors");
                if (errors == null || errors.length() != 0) return Result.UNKNOWN;
            }
            JSONObject data = root.optJSONObject("data");
            JSONObject track = data == null ? null : data.optJSONObject("track");
            if (track == null || !expectedId.equals(track.opt("id")) || !track.has("lyrics"))
                return Result.UNKNOWN;
            if (track.isNull("lyrics")) return Result.UNAVAILABLE;
            JSONObject lyrics = track.optJSONObject("lyrics");
            if (lyrics == null) return Result.UNKNOWN;
            int lines = count(lyrics, "synchronizedLines");
            int words = count(lyrics, "synchronizedWordByWordLines");
            if (lines < 0 || words < 0) return Result.UNKNOWN;
            return lines > 0 || words > 0 ? Result.AVAILABLE : Result.UNAVAILABLE;
        } catch (Exception malformed) {
            return Result.UNKNOWN;
        }
    }
    private static int count(JSONObject lyrics, String key) {
        if (!lyrics.has(key)) return -1;
        if (lyrics.isNull(key)) return 0;
        JSONArray list = lyrics.optJSONArray(key);
        if (list == null) return -1;
        for (int i = 0; i < list.length(); i++) {
            JSONObject line = list.optJSONObject(i);
            if (line == null || !(line.opt("__typename") instanceof String)
                    || line.optString("__typename").isEmpty()) return -1;
        }
        return list.length();
    }

    static final String AUTH_URL = "https://auth.deezer.com/login/anonymous?jo=p";
    static final String API_URL = "https://pipe.deezer.com/api";
    interface Transport {
        String request(String url, String bearer, String body, Call call, long deadline) throws Exception;
    }
    static final class Call {
        private volatile boolean cancelled;
        private java.net.HttpURLConnection connection;
        boolean cancelled() { return cancelled || Thread.currentThread().isInterrupted(); }
        synchronized void attach(java.net.HttpURLConnection next) throws java.io.IOException {
            if (cancelled()) { next.disconnect(); throw new java.io.InterruptedIOException(); }
            connection = next;
        }
        synchronized void detach(java.net.HttpURLConnection current) {
            if (connection == current) connection = null;
        }
        void cancel() {
            java.net.HttpURLConnection current;
            synchronized (this) { cancelled = true; current = connection; connection = null; }
            if (current != null) {
                Thread close = new Thread(() -> {
                    try { current.disconnect(); } catch (RuntimeException alreadyClosed) { }
                }, "boop-lyrics-cancel");
                close.setDaemon(true);
                close.start();
            }
        }
    }
    private final Transport transport;
    private String guestToken;
    private long guestUntil;
    DeezerLyricsClient(Transport transport) { this.transport = transport; }
    static long nowMs() { return java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime()); }
    DeezerLyricsClient() { this(new DeezerLyricsHttp()); }
    private static final String QUERY = "query BoopLyricsAvailability($trackId: String!) { "
            + "track(trackId: $trackId) { id lyrics { synchronizedLines { __typename } "
            + "synchronizedWordByWordLines { __typename } } } }";
    Result check(String id, Call call, long deadline) {
        if (!validTrackId(id) || call == null || call.cancelled() || nowMs() >= deadline)
            return Result.UNKNOWN;
        try {
            if (guestToken == null || nowMs() >= guestUntil) {
                String auth = transport.request(AUTH_URL, null, null, call, deadline);
                Object value = new JSONObject(auth).opt("jwt");
                if (!(value instanceof String)) return Result.UNKNOWN;
                String token = (String) value;
                if (token.length() > 8192 || !token.matches("[A-Za-z0-9_-]+[.][A-Za-z0-9_-]+[.][A-Za-z0-9_-]+"))
                    return Result.UNKNOWN;
                guestToken = token;
                // Conservative in-memory reuse only. A server rejection clears it.
                guestUntil = nowMs() + 240000L;
            }
            if (call.cancelled() || nowMs() >= deadline) return Result.UNKNOWN;
            String body = new JSONObject().put("operationName", "BoopLyricsAvailability")
                    .put("variables", new JSONObject().put("trackId", id))
                    .put("query", QUERY).toString();
            String response = transport.request(API_URL, guestToken, body, call, deadline);
            if (call.cancelled() || nowMs() >= deadline) return Result.UNKNOWN;
            Result result = classify(response, id);
            if (result == Result.UNKNOWN) guestToken = null;
            return result;
        } catch (Exception unavailable) {
            guestToken = null;
            return Result.UNKNOWN;
        }
    }
}
