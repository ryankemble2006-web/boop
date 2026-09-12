package com.boop.shieldhome;

import android.app.Activity;
import android.widget.Toast;

/** Opens Deezer's current-player notification target, then clicks its exact Lyrics control. */
final class DeezerLyricsBrowser {
    private long generation;
    synchronized void cancel() { generation++; }

    synchronized void open(Activity activity, ShieldNowPlayingManager manager, NowPlayingSnapshot requested) {
        if (activity == null || manager == null || requested == null
                || !DeezerLyricsPolicy.available(requested.packageName())) return;
        final long operation = ++generation;
        if (!manager.openNotificationSource()) {
            message(activity, "Deezer player isn't available yet.");
            return;
        }
        new Thread(() -> {
            boolean opened = false;
            StartupLocalBridge bridge = new StartupLocalBridge(activity);
            for (int attempt = 0; attempt < 4 && !opened; attempt++) {
                try {
                    Thread.sleep(attempt == 0 ? 350L : 250L);
                    bridge.openDeezerLyrics();
                    opened = true;
                } catch (Exception ignored) { }
            }
            final boolean success = opened;
            activity.runOnUiThread(() -> {
                synchronized (DeezerLyricsBrowser.this) {
                    if (operation != generation || activity.isFinishing() || activity.isDestroyed()) return;
                    if (!success) message(activity, "Deezer didn't expose Lyrics for this track.");
                }
            });
        }, "boop-deezer-lyrics").start();
    }

    private static void message(Activity activity, String text) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
    }
}
