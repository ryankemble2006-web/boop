package com.boop.shared;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Transport commands and artist requests; provider resolution validates artist names. */
public final class MediaRequest {
    public enum Kind { DEEZER_SEARCH, DEEZER_FLOW, PAUSE, RESUME, NEXT, PREVIOUS }
    private static final Pattern REQUEST = Pattern.compile("^(?:please )?(?:(?:can|could|would) you )?(?:please )?"
            + "(play|put|listen to|(?:i(?:['\u2019]d| would) like to|i want to) (?:hear|listen to)) (.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DEEZER = Pattern.compile("^(.+?) on deezer$", Pattern.CASE_INSENSITIVE);
    private static final Pattern HOME_TARGET = Pattern.compile("^(?:.+ )?"
            + "(?:lights?|lamps?|fans?|heating|heaters?|radiators?|thermostats?|switch(?:es)?|sockets?|plugs?"
            + "|air conditioners?|air conditioning|air purifiers?|coffee machines?)(?: in .+)?$");
    private static final Pattern CONVERSATION_TARGET = Pattern.compile("^(?:me|you|us|him|her|them"
            + "|(?:your|my|our|his|her|their) (?:opinion|thoughts|advice)"
            + "|(?:what|why|how|whether|about) .+)$");
    public final Kind kind;
    public final String query;
    public final boolean explicitProvider;
    private MediaRequest(Kind kind, String query) { this(kind,query,false); }
    private MediaRequest(Kind kind, String query, boolean explicitProvider) {
        this.kind = kind; this.query = query; this.explicitProvider = explicitProvider;
    }
    public static MediaRequest parse(String text) {
        if (text == null) return null;
        String value = text.trim().replaceAll("\\s+", " ").replaceAll("[.!?]+$", "").trim();
        switch(value.toLowerCase(Locale.ROOT)) {
            case "music": case "play some music":
            case "play music": case "play the music": case "play flow": case "play my flow": case "play deezer flow":
                return new MediaRequest(Kind.DEEZER_FLOW, "",true);
            case "pause": case "pause music": case "pause the music": return new MediaRequest(Kind.PAUSE, "");
            case "resume": case "resume music": case "continue music": return new MediaRequest(Kind.RESUME, "");
            case "next": case "next track": case "skip track": return new MediaRequest(Kind.NEXT, "");
            case "previous": case "previous track": return new MediaRequest(Kind.PREVIOUS, "");
            default:
                Matcher spoken = REQUEST.matcher(value);
                if (!spoken.matches()) return null;
                String query = spoken.group(2).trim();
                String polite = withoutCourtesy(query);
                if (polite.equalsIgnoreCase("on deezer")) return null;
                Matcher provider = DEEZER.matcher(query);
                if (!provider.matches()) provider = DEEZER.matcher(polite);
                boolean explicit = provider.matches();
                if (explicit) query = provider.group(1).trim();
                String verb = spoken.group(1).toLowerCase(Locale.ROOT);
                if (verb.equals("put")) {
                    String put = query.toLowerCase(Locale.ROOT);
                    if (put.startsWith("on ")) query = query.substring(3).trim();
                    else if (put.endsWith(" on")) query = query.substring(0,query.length()-3).trim();
                    else if (withoutCourtesy(query).toLowerCase(Locale.ROOT).endsWith(" on")) {
                        query = withoutCourtesy(query);
                        query = query.substring(0,query.length()-3).trim();
                    }
                    else if (!explicit) return null;
                }
                String subject = withoutCourtesy(query).toLowerCase(Locale.ROOT);
                boolean namedMusic = explicit || subject.matches("^(?:the )?(?:song|track|artist|band) .+")
                        || subject.contains(" by ");
                // Catalogue title collisions cannot turn a house command or a request to
                // listen to the speaker into music. Leave those words for the existing router.
                if (!namedMusic && (verb.equals("put") && HOME_TARGET.matcher(subject).matches()
                        || (!verb.equals("play") && !verb.equals("put") && CONVERSATION_TARGET.matcher(subject).matches()))) return null;
                if (flow(query) || flow(withoutCourtesy(query))) return new MediaRequest(Kind.DEEZER_FLOW,"",true);
                String lower = query.toLowerCase(Locale.ROOT);
                if (lower.isEmpty() || (!explicit && (lower.startsWith("on ") || lower.equals("something")))) return null;
                return new MediaRequest(Kind.DEEZER_SEARCH, query,explicit);
        }
    }
    /** Catalogue callers try the literal title before treating its final words as courtesy. */
    public static String withoutCourtesy(String query) {
        return query.replaceFirst("(?i)\\s+(?:please|thank you|thanks)$","").trim();
    }
    private static boolean flow(String query) {
        String value=query.trim().toLowerCase(Locale.ROOT);
        return value.equals("music") || value.equals("some music") || value.equals("the music") || value.equals("flow") || value.equals("my flow") || value.equals("deezer flow");
    }
}
