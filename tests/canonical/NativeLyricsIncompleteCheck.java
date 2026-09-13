package com.boop.shieldhome;

/** Missing timing is unknown, never a confident catalogue absence. */
public final class NativeLyricsIncompleteCheck {
    public static void main(String[] args) {
        String incomplete = "{\"data\":{\"track\":{\"id\":\"123\",\"lyrics\":{\"synchronizedLines\":[{\"line\":\"Invented incomplete phrase\",\"milliseconds\":null,\"duration\":null}],\"synchronizedWordByWordLines\":null}}}}";
        if (DeezerLyricsDocument.parse(incomplete, "123").status() != DeezerLyricsDocument.Status.UNKNOWN)
            throw new AssertionError("Existing lyric text without usable timing must be UNKNOWN, not UNAVAILABLE");
        String words = "[{\"start\":1000,\"end\":2000,\"words\":[{\"word\":\"Invented\"}]}]";
        String fallback = incomplete.replace("\"synchronizedWordByWordLines\":null", "\"synchronizedWordByWordLines\":" + words);
        if (DeezerLyricsDocument.parse(fallback, "123").status() != DeezerLyricsDocument.Status.AVAILABLE)
            throw new AssertionError("Valid word timing must rescue incomplete line timing");
        System.out.println("PASS: 2 incomplete-timing fallback checks.");
    }
}
