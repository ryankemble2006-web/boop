package com.boop.shared;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Only explicit supported media intents; never captures arbitrary conversation. */
public final class MediaRequest {
    public enum Kind { DEEZER_SEARCH, PAUSE, RESUME, NEXT, PREVIOUS }
    private static final Pattern DEEZER = Pattern.compile("^play\\s+(.+?)\\s+on\\s+deezer[.!?]*$", Pattern.CASE_INSENSITIVE);
    public final Kind kind;
    public final String query;
    private MediaRequest(Kind kind, String query) { this.kind = kind; this.query = query; }
    public static MediaRequest parse(String text) {
        if (text == null) return null;
        String value = text.trim().replaceAll("\\s+", " ");
        Matcher matcher = DEEZER.matcher(value);
        if (matcher.matches() && !matcher.group(1).trim().isEmpty())
            return new MediaRequest(Kind.DEEZER_SEARCH, matcher.group(1).trim());
        switch(value.toLowerCase(Locale.ROOT).replaceAll("[.!?]+$", "")) {
            case "pause": case "pause music": case "pause the music": return new MediaRequest(Kind.PAUSE, "");
            case "resume": case "resume music": case "continue music": return new MediaRequest(Kind.RESUME, "");
            case "next": case "next track": case "skip track": return new MediaRequest(Kind.NEXT, "");
            case "previous": case "previous track": return new MediaRequest(Kind.PREVIOUS, "");
            default: return null;
        }
    }
}
