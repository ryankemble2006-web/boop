package com.boop.shieldhome;

public final class AudioModePolicyTest {
    private static void check(boolean result, String message) {
        if (!result) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        check(AudioModePolicy.forLaunch("deezer.android.app") == AudioModePolicy.Mode.NATIVE_MUSIC,
                "Native Deezer should enable native sample-rate matching");
        check(AudioModePolicy.forLaunch("org.xbmc.kodi") == AudioModePolicy.Mode.NORMAL_VIDEO,
                "Kodi should use normal video audio mode");
        check(AudioModePolicy.forCast("com.google.android.apps.mediashell", 2, true, false, false) == AudioModePolicy.Mode.NATIVE_MUSIC,
                "Explicit music Cast should enable native sample-rate matching");
        check(AudioModePolicy.forCast("com.google.android.apps.mediashell", 0, true, true, true) == AudioModePolicy.Mode.NATIVE_MUSIC,
                "Unknown Deezer-shaped Cast with previous and next should stay native-rate");
        check(AudioModePolicy.forCast("com.google.android.apps.mediashell", 0, true, true, false) == AudioModePolicy.Mode.NORMAL_VIDEO,
                "Unknown Cast without both track-skip actions should stay video mode");
        check(AudioModePolicy.forCast("com.google.android.apps.mediashell", 1, true, true, true) == AudioModePolicy.Mode.NORMAL_VIDEO,
                "Explicit non-music Cast should stay video mode");
        check(AudioModePolicy.forCast("other.package", 2, true, true, true) == AudioModePolicy.Mode.IGNORE,
                "Non-Cast packages should not change mode");
        check(AudioModePolicy.forCast("com.google.android.apps.mediashell", 2, false, true, true) == AudioModePolicy.Mode.IGNORE,
                "Paused Cast should not steal audio mode");
        System.out.println("PASS: launcher and Cast audio-mode policy");
    }
}
