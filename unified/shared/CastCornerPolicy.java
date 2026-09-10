package com.boop.shared;
/** Fail closed: only the observed Deezer Cast receiver may own the corner. */
public final class CastCornerPolicy {
    public static final String RECEIVER="com.google.android.apps.mediashell";
    private CastCornerPolicy() { }
    public static String foreground(String packageName,String className) {
        return RECEIVER.equals(packageName)
                && "org.chromium.chromecast.shell.CastWebContentsActivity".equals(className)
                ? RECEIVER : "";
    }
    public static boolean allowed(String visiblePackage,String playerPackage,String castApp) {
        return RECEIVER.equals(visiblePackage) && RECEIVER.equals(playerPackage)
                && "Deezer".equalsIgnoreCase(castApp == null ? "" : castApp.trim());
    }
}
