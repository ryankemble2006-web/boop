package com.boop.shieldhome;

final class AudioModePolicy {
    enum Mode { IGNORE, NATIVE_MUSIC, NORMAL_VIDEO }

    private static final String DEEZER = "deezer.android.app";
    private static final String MEDIA_SHELL = "com.google.android.apps.mediashell";
    private static final int CONTENT_TYPE_MUSIC = 2;
    private static final int CONTENT_TYPE_UNKNOWN = 0;
    private static final long ACTION_STOP = 1L;
    private static final long ACTION_SKIP_TO_PREVIOUS = 16L;
    private static final long ACTION_SKIP_TO_NEXT = 32L;

    static Mode forLaunch(String packageName) {
        if (DEEZER.equals(clean(packageName))) return Mode.NATIVE_MUSIC;
        return Mode.NORMAL_VIDEO;
    }

    static Mode forCast(String packageName, int contentType, boolean playing, long actions) {
        if (!MEDIA_SHELL.equals(clean(packageName)) || !playing) return Mode.IGNORE;
        if (contentType == CONTENT_TYPE_MUSIC) return Mode.NATIVE_MUSIC;
        if (contentType != CONTENT_TYPE_UNKNOWN) return Mode.NORMAL_VIDEO;
        if (actions == ACTION_STOP) return Mode.IGNORE;
        boolean previous = (actions & ACTION_SKIP_TO_PREVIOUS) != 0L;
        boolean next = (actions & ACTION_SKIP_TO_NEXT) != 0L;
        return previous && next ? Mode.NATIVE_MUSIC : Mode.NORMAL_VIDEO;
    }

    static Mode arbitrate(Mode foregroundLaunch, Mode castMode) {
        if (foregroundLaunch != null && foregroundLaunch != Mode.IGNORE) return foregroundLaunch;
        return castMode == null ? Mode.IGNORE : castMode;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private AudioModePolicy() { }
}
