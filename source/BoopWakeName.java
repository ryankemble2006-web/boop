package com.boop.alpha1;

final class BoopWakeName {
    static final String DEFAULT = "BOOP";
    static final int MAX_LENGTH = 48;

    private BoopWakeName() { }

    static String normalize(String raw) {
        if (raw == null) {
            return DEFAULT;
        }
        String value = raw.trim().replaceAll("\\s+", " ");
        if (value.isEmpty()) {
            return DEFAULT;
        }
        if (value.length() > MAX_LENGTH) {
            value = value.substring(0, MAX_LENGTH).trim();
        }
        return value.isEmpty() ? DEFAULT : value;
    }

    static boolean isDefault(String value) {
        return DEFAULT.equalsIgnoreCase(normalize(value));
    }
}
