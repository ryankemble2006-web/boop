package com.boop.alpha1;

final class JohnnyStatePolicy {
    static final String PACKAGE = "local.johnnycastaway.halab";
    static final String SIGNER = "785ce5d046b1947a48dd40fbbd4e83aa1e69665d5b89426af5d9abc215c05215";
    static boolean trusted(String name, String signer) {
        return PACKAGE.equals(name) && SIGNER.equals(signer);
    }
    static String lights(String[] states) {
        if (states.length == 0) return "unknown";
        boolean allOff = true;
        for (String state : states) {
            if ("on".equals(state)) return "on";
            if (!"off".equals(state)) allOff = false;
        }
        return allOff ? "off" : "unknown";
    }
    static boolean isFan(String id, String name) {
        if (id.startsWith("fan.")) return true;
        String control = (id + " " + name).toLowerCase(java.util.Locale.ROOT);
        return !control.contains("oscillat") && (id.startsWith("switch.")
                && java.util.regex.Pattern.compile("(?i)\\bfan\\b").matcher(name).find());
    }
    static String state(String raw) {
        return "on".equals(raw) || "off".equals(raw) ? raw : "unknown";
    }
}
