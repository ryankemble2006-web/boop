package com.boop.alpha1;

/** Stable developer-preview ordering for the canonical animation catalogue. */
public final class BoopCanonicalAnimationMenu {
    private static final String[] CLIPS = {
            "idle", "blink", "double_blink", "wake", "sleep", "listening",
            "reading", "thinking", "berry_remember", "berry_curious", "berry_cheeky",
            "music", "media_pause", "track_change", "cinema", "shake_reaction",
            "curious", "skeptical", "success", "confused", "wink_left", "wink_right",
            "attention_left", "attention_right", "notification", "reset"
    };

    private BoopCanonicalAnimationMenu() { }

    public static String[] clipIds() { return CLIPS.clone(); }
    public static int signStyles() { return 4; }
    public static boolean hasFreddie() { return true; }
}
