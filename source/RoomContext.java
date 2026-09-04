package com.boop.alpha1;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class RoomContext {
    private static final Pattern EXPLICIT_MULTI = Pattern.compile(
            "\\b(all|both|every|each)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern TRAILING_TURN_STATE = Pattern.compile(
            "^turn\\s+(.+?)\\s+(on|off)$", Pattern.CASE_INSENSITIVE);

    private final String homeArea;
    private final List<String> knownAreas;

    RoomContext(String homeArea, List<String> knownAreas) {
        this.homeArea = homeArea;
        this.knownAreas = List.copyOf(knownAreas);
    }

    String qualify(String transcript) {
        String text = transcript == null ? "" : transcript.trim();
        if (text.isEmpty()) {
            return text;
        }

        text = normalizeTrailingTurnState(text);
        String lower = text.toLowerCase(Locale.ROOT);
        if (EXPLICIT_MULTI.matcher(lower).find()) {
            return text;
        }

        for (String area : knownAreas) {
            if (lower.contains(area.toLowerCase(Locale.ROOT))) {
                return text;
            }
        }

        return text + " in the " + homeArea;
    }

    private static String normalizeTrailingTurnState(String text) {
        Matcher match = TRAILING_TURN_STATE.matcher(text);
        if (!match.matches()) {
            return text;
        }
        return "turn " + match.group(2).toLowerCase(Locale.ROOT) + " " + match.group(1);
    }
}
