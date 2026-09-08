package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.app.Notification;
import org.junit.Test;

public final class NowPlayingArtworkSourcePolicyTest {
    @Test public void mediaMetadataAllowsLocalAndHttpsArtworkUris() {
        assertEquals(
                NowPlayingArtworkSourcePolicy.Kind.LOCAL,
                NowPlayingArtworkSourcePolicy.kind("content://player/art/1"));
        assertEquals(
                NowPlayingArtworkSourcePolicy.Kind.LOCAL,
                NowPlayingArtworkSourcePolicy.kind("android.resource://player/drawable/cover"));
        assertEquals(
                NowPlayingArtworkSourcePolicy.Kind.REMOTE_HTTPS,
                NowPlayingArtworkSourcePolicy.kind("https://cdn.example/cover.jpg"));
    }

    @Test public void artworkUriPolicyRejectsUnsafeOrMissingSchemes() {
        assertEquals(
                NowPlayingArtworkSourcePolicy.Kind.UNSUPPORTED,
                NowPlayingArtworkSourcePolicy.kind("http://cdn.example/cover.jpg"));
        assertEquals(
                NowPlayingArtworkSourcePolicy.Kind.UNSUPPORTED,
                NowPlayingArtworkSourcePolicy.kind("javascript:wat"));
        assertEquals(
                NowPlayingArtworkSourcePolicy.Kind.UNSUPPORTED,
                NowPlayingArtworkSourcePolicy.kind(""));
    }

    @Test public void notificationArtworkAcceptsAndroidMediaOrTransportClassification() {
        assertTrue(NowPlayingArtworkSourcePolicy.isMediaNotification(true, null));
        assertTrue(NowPlayingArtworkSourcePolicy.isMediaNotification(
                false, Notification.CATEGORY_TRANSPORT));
        assertFalse(NowPlayingArtworkSourcePolicy.isMediaNotification(false, "message"));
    }
}
