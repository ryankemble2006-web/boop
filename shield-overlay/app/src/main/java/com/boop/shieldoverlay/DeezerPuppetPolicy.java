package com.boop.shieldoverlay;

import java.util.List;

public final class DeezerPuppetPolicy {
    private static final String DEEZER_PACKAGE = "deezer.android.app";

    public enum Mode {
        EYES,
        HEADPHONES_REST,
        HEADPHONES_PLAYING
    }

    public static final class Session {
        public final long id;
        public final String packageName;
        public final Integer playbackState;

        public Session(long id, String packageName, Integer playbackState) {
            this.id = id;
            this.packageName = packageName;
            this.playbackState = playbackState;
        }
    }

    private DeezerPuppetPolicy() {}

    public static Mode mode(
            boolean enabled, boolean granted, boolean connected, Integer playbackState) {
        if (!enabled || !granted || !connected || playbackState == null) {
            return Mode.EYES;
        }
        switch (playbackState) {
            case 3:
                return Mode.HEADPHONES_PLAYING;
            case 2:
            case 4:
            case 5:
            case 6:
            case 8:
            case 9:
            case 10:
            case 11:
                return Mode.HEADPHONES_REST;
            default:
                return Mode.EYES;
        }
    }

    public static long select(List<Session> sessions, long currentId) {
        long firstEligibleId = 0L;
        long firstPlayingId = 0L;
        boolean currentEligible = false;
        boolean currentPlaying = false;

        if (sessions == null) {
            return 0L;
        }
        for (Session session : sessions) {
            if (session == null || !DEEZER_PACKAGE.equals(session.packageName)) {
                continue;
            }
            Mode sessionMode = mode(true, true, true, session.playbackState);
            if (sessionMode == Mode.EYES) {
                continue;
            }
            if (firstEligibleId == 0L) {
                firstEligibleId = session.id;
            }
            if (session.id == currentId) {
                currentEligible = true;
            }
            if (sessionMode == Mode.HEADPHONES_PLAYING) {
                if (firstPlayingId == 0L) {
                    firstPlayingId = session.id;
                }
                if (session.id == currentId) {
                    currentPlaying = true;
                }
            }
        }

        if (currentPlaying) {
            return currentId;
        }
        if (firstPlayingId != 0L) {
            return firstPlayingId;
        }
        if (currentEligible) {
            return currentId;
        }
        return firstEligibleId;
    }
}
