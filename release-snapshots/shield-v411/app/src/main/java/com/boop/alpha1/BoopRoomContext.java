package com.boop.alpha1;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Adds the selected Shield room to natural Home Assistant conversation requests. */
final class BoopRoomContext {
    private static final Pattern EXPLICIT_MULTI = Pattern.compile(
            "\\b(all|both|every|each)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern TRAILING_TURN_STATE = Pattern.compile(
            "^turn\\s+(.+?)\\s+(on|off)$", Pattern.CASE_INSENSITIVE);

    private BoopRoomContext() { }

    static String qualify(String transcript, String roomId, String roomName) {
        String text = transcript == null ? "" : transcript.trim();
        if (text.isEmpty()) return text;

        text = normalizeTrailingTurnState(text);
        String lower = text.toLowerCase(Locale.ROOT);
        if (EXPLICIT_MULTI.matcher(lower).find()
                || lower.contains(normalize(roomName))
                || BoopRoomCommandParser.namesOtherRoom(text, roomId, roomName)) {
            return text;
        }
        return text + " in the " + roomName;
    }

    private static String normalizeTrailingTurnState(String text) {
        Matcher match = TRAILING_TURN_STATE.matcher(text);
        if (!match.matches()) return text;
        return "turn " + match.group(2).toLowerCase(Locale.ROOT) + " " + match.group(1);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT)
                .replace('_', ' ').trim();
    }
}
