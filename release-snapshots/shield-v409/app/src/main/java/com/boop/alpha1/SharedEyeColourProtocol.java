package com.boop.alpha1;

import java.util.List;

/** Bounded hue payload and ownership marker for the opt-in HA helper. */
final class SharedEyeColourProtocol {
    static final String PREFIX = "BOOP_EYE_V1|";
    static final String NAME = "BOOP shared eye colour";
    static final String MARKER = "^BOOP_EYE_V1\\|(?:[0-9]|[1-9][0-9]|[12][0-9]{2}|3[0-5][0-9])$";
    private SharedEyeColourProtocol() { }
    static void requireHue(int hue) {
        if (hue < 0 || hue > 359) throw new IllegalArgumentException("Invalid eye hue");
    }
    static String encode(int hue) {
        requireHue(hue);
        return PREFIX + hue;
    }
    static Integer decode(String value) {
        if (value == null || value.length() > 15 || !value.startsWith(PREFIX)) return null;
        String number = value.substring(PREFIX.length());
        if (!number.matches("(?:0|[1-9][0-9]{0,2})")) return null;
        int hue = Integer.parseInt(number);
        return hue <= 359 ? hue : null;
    }
    static boolean isOwnedHelper(String pattern, int min, int max, String mode, boolean hasInitial) {
        return MARKER.equals(pattern) && min == 0 && max == 32
                && "text".equals(mode) && !hasInitial;
    }
    static String chooseHelper(List<String> ids) {
        if (ids == null || ids.isEmpty()) return null;
        if (ids.size() != 1) throw new IllegalArgumentException("More than one BOOP colour helper");
        String id = ids.get(0);
        if (id == null || !id.matches("[a-z0-9_]{1,128}")) {
            throw new IllegalArgumentException("Invalid colour helper identity");
        }
        return id;
    }
}
