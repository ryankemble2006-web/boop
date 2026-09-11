package com.boop.shieldhome;

final class AudioModePolicy {
    enum Mode { IGNORE, NATIVE_MUSIC, NORMAL_VIDEO }

    private static final String DEEZER = "deezer.android.app";
    private static final String MEDIA_SHELL = "com.google.android.apps.mediashell";
    private static final int CONTENT_TYPE_UNKNOWN = 0;
    private static final int CONTENT_TYPE_MUSIC = 2;
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
        if (contentType == CONTENT_TYPE_UNKNOWN && actions == ACTION_STOP) return Mode.IGNORE;
        boolean trackSkipping = (actions & ACTION_SKIP_TO_PREVIOUS) != 0L && (actions & ACTION_SKIP_TO_NEXT) != 0L;
        if (contentType == CONTENT_TYPE_UNKNOWN && trackSkipping) return Mode.NATIVE_MUSIC;
        return Mode.NORMAL_VIDEO;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private AudioModePolicy() { }
}
