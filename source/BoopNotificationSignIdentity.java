package com.boop.alpha1;

/** Exact production and local-preview identities for the four existing sign designs. */
final class BoopNotificationSignIdentity {
    private BoopNotificationSignIdentity() { }

    static int brandedStyle(String packageName) {
        if (packageName == null) return -1;
        switch (packageName) {
            case "com.whatsapp":
            case "com.whatsapp.w4b":
            case "boop.dev.whatsapp": return 0;
            case "com.google.android.gm":
            case "boop.dev.gmail": return 1;
            case "com.facebook.katana":
            case "boop.dev.facebook": return 2;
            case "com.twitter.android":
            case "boop.dev.x": return 3;
            default: return -1;
        }
    }
}
