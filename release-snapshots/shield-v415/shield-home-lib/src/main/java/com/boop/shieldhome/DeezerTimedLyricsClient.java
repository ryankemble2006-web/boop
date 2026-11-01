package com.boop.shieldhome;

import org.json.JSONObject;

/** Anonymous, bounded exact-recording lookup. No account credentials or persistent lyric store. */
final class DeezerTimedLyricsClient {
    private static final String QUERY = "query BoopTimedLyrics($trackId: String!) { "
            + "track(trackId: $trackId) { id lyrics { "
            + "synchronizedLines { line milliseconds duration } "
            + "synchronizedWordByWordLines { start end words { word } } "
            + "copyright licence } } }";
    private final DeezerLyricsClient.Transport transport;
    private String guestToken;
    private long guestUntil;
    DeezerTimedLyricsClient() { this(new DeezerLyricsHttp()); }
    DeezerTimedLyricsClient(DeezerLyricsClient.Transport transport) { this.transport = transport; }

    DeezerLyricsDocument load(String id, DeezerLyricsClient.Call call, long deadline) {
        if (!DeezerLyricsDocument.validTrackId(id) || call == null || call.cancelled()
                || DeezerLyricsClient.nowMs() >= deadline) return DeezerLyricsDocument.unknown(id);
        try {
            if (guestToken == null || DeezerLyricsClient.nowMs() >= guestUntil) {
                String response = transport.request(DeezerLyricsClient.AUTH_URL, null, null, call, deadline);
                Object value = new JSONObject(response).opt("jwt");
                if (!(value instanceof String)) return DeezerLyricsDocument.unknown(id);
                String token = (String) value;
                if (token.length() > 8192 || !token.matches("[A-Za-z0-9_-]+[.][A-Za-z0-9_-]+[.][A-Za-z0-9_-]+"))
                    return DeezerLyricsDocument.unknown(id);
                guestToken = token;
                guestUntil = DeezerLyricsClient.nowMs() + 240000L;
            }
            if (call.cancelled() || DeezerLyricsClient.nowMs() >= deadline) return DeezerLyricsDocument.unknown(id);
            String body = new JSONObject().put("operationName", "BoopTimedLyrics")
                    .put("variables", new JSONObject().put("trackId", id)).put("query", QUERY).toString();
            String response = transport.request(DeezerLyricsClient.API_URL, guestToken, body, call, deadline);
            if (call.cancelled() || DeezerLyricsClient.nowMs() >= deadline) return DeezerLyricsDocument.unknown(id);
            DeezerLyricsDocument result = DeezerLyricsDocument.parse(response, id);
            if (result.status() == DeezerLyricsDocument.Status.UNKNOWN) guestToken = null;
            return result;
        } catch (Exception unavailable) {
            guestToken = null;
            return DeezerLyricsDocument.unknown(id);
        }
    }
}
