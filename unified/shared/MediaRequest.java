package com.boop.shared;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Transport commands and artist requests; provider resolution validates artist names. */
public final class MediaRequest {
    public enum Kind { DEEZER_SEARCH, PAUSE, RESUME, NEXT, PREVIOUS }
    private static final Pattern DEEZER = Pattern.compile("^play\\s+(.+?)\\s+on\\s+deezer[.!?]*$", Pattern.CASE_INSENSITIVE);
    public final Kind kind;
    public final String query;
    public final boolean explicitProvider;
    private MediaRequest(Kind kind, String query) { this(kind,query,false); }
    private MediaRequest(Kind kind, String query, boolean explicitProvider) {
        this.kind = kind; this.query = query; this.explicitProvider = explicitProvider;
    }
    public static MediaRequest parse(String text) {
        if (text == null) return null;
        String value = text.trim().replaceAll("\\s+", " ");
        Matcher matcher = DEEZER.matcher(value);
        if (matcher.matches() && !matcher.group(1).trim().isEmpty())
            return new MediaRequest(Kind.DEEZER_SEARCH, matcher.group(1).trim(),true);
        switch(value.toLowerCase(Locale.ROOT).replaceAll("[.!?]+$", "")) {
            case "pause": case "pause music": case "pause the music": return new MediaRequest(Kind.PAUSE, "");
            case "resume": case "resume music": case "continue music": return new MediaRequest(Kind.RESUME, "");
            case "next": case "next track": case "skip track": return new MediaRequest(Kind.NEXT, "");
            case "previous": case "previous track": return new MediaRequest(Kind.PREVIOUS, "");
            default:
                if (!value.toLowerCase(Locale.ROOT).startsWith("play ")) return null;
                String query = value.substring(5).trim().replaceAll("[.!?]+$", "");
                String lower = query.toLowerCase(Locale.ROOT);
                if (lower.isEmpty() || lower.startsWith("on ") || lower.equals("music")
                        || lower.equals("the music") || lower.equals("something")) return null;
                return new MediaRequest(Kind.DEEZER_SEARCH, query);
        }
    }
}
