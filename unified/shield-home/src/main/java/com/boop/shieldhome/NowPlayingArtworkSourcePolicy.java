package com.boop.shieldhome;

import android.app.Notification;
import android.net.Uri;

/** Pure policy for safe Now Playing artwork sources. */
final class NowPlayingArtworkSourcePolicy {
    enum Kind { LOCAL, REMOTE_HTTPS, UNSUPPORTED }

    private NowPlayingArtworkSourcePolicy() { }

    static Kind kind(String rawUri) {
        if (rawUri == null || rawUri.trim().isEmpty()) {
            return Kind.UNSUPPORTED;
        }
        Uri uri;
        try {
            uri = Uri.parse(rawUri.trim());
        } catch (RuntimeException malformed) {
            return Kind.UNSUPPORTED;
        }
        String scheme = uri.getScheme();
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
