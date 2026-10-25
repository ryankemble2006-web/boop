package com.boop.alpha1;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class BoopRoomCommandParser {
    private static final Pattern PREFIX = Pattern.compile(
            "^(?:please\\s+)?(?:turn|switch)\\s+(?:(on|off)\\s+)?(?:the\\s+)?(.+?)(?:\\s+(on|off))?$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern AREA = Pattern.compile("^(.+?)\\s+in\\s+(?:the\\s+)?(.+)$",
            Pattern.CASE_INSENSITIVE);

    static GenericHomeCommand parse(String text, String roomId, String roomName) {
        if (text == null) return null;
        Matcher matcher = PREFIX.matcher(text.trim());
        if (!matcher.matches()) return null;
        String action = matcher.group(1) != null ? matcher.group(1) : matcher.group(3);
        if (action == null) return null;
        String target = matcher.group(2).trim();
        boolean other = false;
        Matcher area = AREA.matcher(target);
        if (area.matches()) {
            target = area.group(1).trim();
            String namedArea = normalize(area.group(2));
            other = !namedArea.equals(normalize(roomName)) && !namedArea.equals(normalize(roomId));
        }
        if (target.isEmpty()) return null;
        return new GenericHomeCommand(target, "on".equalsIgnoreCase(action) ? "turn_on" : "turn_off", other);
    }

    static boolean namesOtherRoom(String text, String roomId, String roomName) {
        if (text == null) return false;
        Matcher marker = Pattern.compile(".*\\bin\\s+(?:the\\s+)?([a-z0-9 _-]+?)(?:[.!?]|$)",
                Pattern.CASE_INSENSITIVE).matcher(text.trim());
        if (!marker.matches()) return false;
        String namedArea = normalize(marker.group(1));
        return !namedArea.equals(normalize(roomName)) && !namedArea.equals(normalize(roomId));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replace('_', ' ').trim();
    }
}
