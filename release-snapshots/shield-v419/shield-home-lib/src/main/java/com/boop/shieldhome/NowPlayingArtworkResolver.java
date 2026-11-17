package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.net.Uri;
import android.os.Handler;

import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

/** Resolves MediaSession artwork without ever blocking the launcher main thread on network I/O. */
final class NowPlayingArtworkResolver {
    private static final int MAX_CACHE_ENTRIES = 12;
    private static final int MAX_REMOTE_BYTES = 8 * 1024 * 1024;

    private final Context context;
    private final Handler mainHandler;
    private final Runnable invalidate;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final LinkedHashMap<String, Bitmap> remoteCache = new LinkedHashMap<>();
    private final Set<String> remotePending = new HashSet<>();
    private final Set<String> remoteFailed = new HashSet<>();
    private int generation;

    NowPlayingArtworkResolver(Context context, Handler mainHandler, Runnable invalidate) {
        this.context = context;
        this.mainHandler = mainHandler;
        this.invalidate = invalidate;
    }

    Bitmap resolve(MediaMetadata metadata, Bitmap notificationFallback) {
        if (metadata == null) {
            return notificationFallback;
        }

        for (String key : new String[] {
                MediaMetadata.METADATA_KEY_ART,
                MediaMetadata.METADATA_KEY_ALBUM_ART,
                MediaMetadata.METADATA_KEY_DISPLAY_ICON
        }) {
            Bitmap bitmap = metadata.getBitmap(key);
            if (bitmap != null) {
                return bitmap;
            }
        }

        for (String key : new String[] {
                MediaMetadata.METADATA_KEY_ART_URI,
                MediaMetadata.METADATA_KEY_ALBUM_ART_URI,
                MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI
        }) {
            String rawUri = metadata.getString(key);
            NowPlayingArtworkSourcePolicy.Kind kind = NowPlayingArtworkSourcePolicy.kind(rawUri);
            if (kind == NowPlayingArtworkSourcePolicy.Kind.LOCAL) {
                Bitmap local = decodeLocal(rawUri);
                if (local != null) {
                    return local;
                }
            } else if (kind == NowPlayingArtworkSourcePolicy.Kind.REMOTE_HTTPS) {
                String cleanUri = rawUri.trim();
                Bitmap cached = remoteCache.get(cleanUri);
                if (cached != null) {
                    return cached;
                }
                scheduleRemote(cleanUri);
            }
        }
        return notificationFallback;
    }

    void clear() {
        generation++;
        remoteCache.clear();
        remotePending.clear();
        remoteFailed.clear();
    }

    private Bitmap decodeLocal(String rawUri) {
        try {
            Uri uri = Uri.parse(rawUri.trim());
            try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
                return stream == null ? null : BitmapFactory.decodeStream(stream);
            }
        } catch (Exception unavailable) {
            return null;
        }
    }

    private void scheduleRemote(String rawUri) {
        if (remoteCache.containsKey(rawUri)
                || remotePending.contains(rawUri)
                || remoteFailed.contains(rawUri)) {
            return;
        }
        remotePending.add(rawUri);
        int requestGeneration = generation;
        executor.execute(() -> {
            Bitmap bitmap = downloadHttps(rawUri);
            mainHandler.post(() -> {
                if (requestGeneration != generation) {
                    return;
                }
                remotePending.remove(rawUri);
                if (bitmap == null) {
                    remoteFailed.add(rawUri);
                    return;
                }
                remoteFailed.remove(rawUri);
                remoteCache.put(rawUri, bitmap);
                while (remoteCache.size() > MAX_CACHE_ENTRIES) {
                    String eldest = remoteCache.keySet().iterator().next();
                    remoteCache.remove(eldest);
                }
                if (invalidate != null) {
                    invalidate.run();
                }
            });
        });
    }

    private Bitmap downloadHttps(String rawUri) {
        HttpsURLConnection connection = null;
        try {
            URL url = new URL(rawUri);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(4000);
            connection.setReadTimeout(6000);
            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(true);
            connection.setRequestProperty("Accept", "image/*");
            connection.setRequestProperty("User-Agent", "BOOP-Shield-Home");
            int response = connection.getResponseCode();
            if (response < 200 || response >= 300) {
                return null;
            }
            if (!"https".equalsIgnoreCase(connection.getURL().getProtocol())) {
                return null;
            }
            int contentLength = connection.getContentLength();
            if (contentLength > MAX_REMOTE_BYTES) {
                return null;
            }
            String contentType = connection.getContentType();
            if (contentType != null && !contentType.toLowerCase().startsWith("image/")) {
                return null;
            }
            try (InputStream stream = connection.getInputStream()) {
                return BitmapFactory.decodeStream(stream);
            }
        } catch (Exception unavailable) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
