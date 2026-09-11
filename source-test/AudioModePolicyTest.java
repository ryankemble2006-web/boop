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
        check(AudioModePolicy.forLaunch("com.nvidia.bbciplayer") == AudioModePolicy.Mode.NORMAL_VIDEO,
                "iPlayer should use normal video audio mode");
        check(AudioModePolicy.forCast("com.google.android.apps.mediashell", 2, true) == AudioModePolicy.Mode.NATIVE_MUSIC,
                "Music Cast should enable native sample-rate matching");
        check(AudioModePolicy.forCast("com.google.android.apps.mediashell", 1, true) == AudioModePolicy.Mode.NORMAL_VIDEO,
                "Non-music Cast should use normal video audio mode");
        check(AudioModePolicy.forCast("other.package", 2, true) == AudioModePolicy.Mode.IGNORE,
                "Non-Cast packages should not change mode from session classification");
        check(AudioModePolicy.forCast("com.google.android.apps.mediashell", 2, false) == AudioModePolicy.Mode.IGNORE,
                "Paused Cast should not steal audio mode from the active foreground app");
        System.out.println("PASS: launcher and Cast audio-mode policy");
    }
}
