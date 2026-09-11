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
        check(AudioModePolicy.forCast("com.google.android.apps.mediashell", 2, true, 1L) == AudioModePolicy.Mode.NATIVE_MUSIC,
                "Explicit music Cast should enable native sample-rate matching");
        check(AudioModePolicy.forCast("com.google.android.apps.mediashell", 0, true, 951L) == AudioModePolicy.Mode.NATIVE_MUSIC,
                "Unknown Deezer-shaped Cast with previous and next should stay native-rate");
        check(AudioModePolicy.forCast("com.google.android.apps.mediashell", 0, true, 1L) == AudioModePolicy.Mode.IGNORE,
                "STOP-only Cast transition should preserve the current audio mode");
        check(AudioModePolicy.forCast("com.google.android.apps.mediashell", 0, true, 7L) == AudioModePolicy.Mode.NORMAL_VIDEO,
                "Unknown Cast with real playback controls but no track skips should use video mode");
        check(AudioModePolicy.forCast("com.google.android.apps.mediashell", 1, true, 951L) == AudioModePolicy.Mode.NORMAL_VIDEO,
                "Explicit non-music Cast should stay video mode");
        check(AudioModePolicy.arbitrate(AudioModePolicy.Mode.NORMAL_VIDEO, AudioModePolicy.Mode.NATIVE_MUSIC)
                        == AudioModePolicy.Mode.NORMAL_VIDEO,
                "Foreground video launch must outrank background Cast music");
        check(AudioModePolicy.arbitrate(AudioModePolicy.Mode.IGNORE, AudioModePolicy.Mode.NATIVE_MUSIC)
                        == AudioModePolicy.Mode.NATIVE_MUSIC,
                "Cast music should reclaim native mode after foreground launch clears");
        check(AudioModePolicy.forCast("other.package", 2, true, 951L) == AudioModePolicy.Mode.IGNORE,
                "Non-Cast packages should not change mode");
        check(AudioModePolicy.forCast("com.google.android.apps.mediashell", 2, false, 951L) == AudioModePolicy.Mode.IGNORE,
                "Paused Cast should not steal audio mode");
        System.out.println("PASS: launcher, Cast and foreground audio priority policy");
    }
}
