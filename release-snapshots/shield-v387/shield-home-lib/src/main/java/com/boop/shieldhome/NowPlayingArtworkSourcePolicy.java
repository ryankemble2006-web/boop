package com.boop.shieldhome;

import android.app.Notification;

import java.net.URI;

/** Pure policy for safe Now Playing artwork sources. */
final class NowPlayingArtworkSourcePolicy {
    enum Kind { LOCAL, REMOTE_HTTPS, UNSUPPORTED }

    private NowPlayingArtworkSourcePolicy() { }

    static Kind kind(String rawUri) {
        if (rawUri == null || rawUri.trim().isEmpty()) {
            return Kind.UNSUPPORTED;
        }
        String scheme;
        try {
            scheme = URI.create(rawUri.trim()).getScheme();
        } catch (IllegalArgumentException malformed) {
            return Kind.UNSUPPORTED;
        }
        if (scheme == null) {
            return Kind.UNSUPPORTED;
        }
        if ("content".equalsIgnoreCase(scheme)
                || "file".equalsIgnoreCase(scheme)
                || "android.resource".equalsIgnoreCase(scheme)) {
            return Kind.LOCAL;
        }
        if ("https".equalsIgnoreCase(scheme)) {
            return Kind.REMOTE_HTTPS;
        }
        return Kind.UNSUPPORTED;
    }

    static boolean isMediaNotification(boolean hasMediaSession, String category) {
        return hasMediaSession || Notification.CATEGORY_TRANSPORT.equals(category);
    }
}
