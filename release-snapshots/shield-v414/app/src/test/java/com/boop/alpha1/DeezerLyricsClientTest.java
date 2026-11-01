package com.boop.shieldhome;

import org.junit.Test;
import static org.junit.Assert.*;

public class DeezerLyricsClientTest {
    private static String classify(String body, String id) {
        return DeezerLyricsClient.classify(body, id).name();
    }
    private static String response(String lyrics) {
        return "{\"data\":{\"track\":{\"id\":\"123\",\"lyrics\":" + lyrics + "}}}";
    }
    @Test public void explicitEmptyTimedListsMeanUnavailable() throws Exception {
        assertEquals("UNAVAILABLE", classify(response("{\"synchronizedLines\":null,\"synchronizedWordByWordLines\":null}"), "123"));
        assertEquals("UNAVAILABLE", classify(response("{\"synchronizedLines\":[],\"synchronizedWordByWordLines\":[]}"), "123"));
        assertEquals("UNAVAILABLE", classify(response("null"), "123"));
    }
    @Test public void eitherTimedFormatMeansAvailable() throws Exception {
        assertEquals("AVAILABLE", classify(response("{\"synchronizedLines\":[{\"__typename\":\"Line\"}],\"synchronizedWordByWordLines\":null}"), "123"));
        assertEquals("AVAILABLE", classify(response("{\"synchronizedLines\":null,\"synchronizedWordByWordLines\":[{\"__typename\":\"WordLine\"}]}"), "123"));
    }
    @Test public void missingOrMalformedDataIsNotNoLyrics() throws Exception {
        for (String body : new String[]{null, "", "not json", "{}", "{\"data\":null}",
                "{\"data\":{\"track\":null}}", "{\"data\":{\"track\":{\"id\":\"123\"}}}",
                response("{}"), response("[]"), response("{\"synchronizedLines\":null}"),
                response("{\"synchronizedLines\":false,\"synchronizedWordByWordLines\":null}"),
                response("{\"synchronizedLines\":[null],\"synchronizedWordByWordLines\":null}")}) {
            assertEquals("Malformed/unknown must never say no lyrics: " + body,
                    "UNKNOWN", classify(body, "123"));
        }
    }
    @Test public void apiErrorsOverrideEvenAnEmptyLyricsObject() throws Exception {
        String body = response("null");
        body = body.substring(0, body.length() - 1) + ",\"errors\":[{\"message\":\"unauthorized\"}]}";
        assertEquals("UNKNOWN", classify(body, "123"));
    }
    @Test public void differentRecordingMustNotControlThisTrack() throws Exception {
        assertEquals("UNKNOWN", classify(response("null"), "124"));
        for (String id : new String[]{null, "", "0", "-1", "123;other", " 123", "123/4"})
            assertEquals("UNKNOWN", classify(response("null"), id));
    }
    @Test public void oldPublicFlagsAreNotAuthoritative() throws Exception {
        assertEquals("UNKNOWN", classify("{\"id\":\"123\",\"LYRICS_ID\":0,\"explicit_lyrics\":false}", "123"));
    }
}
