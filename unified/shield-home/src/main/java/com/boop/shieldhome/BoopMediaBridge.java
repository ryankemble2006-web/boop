package com.boop.shieldhome;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.provider.Settings;
import com.boop.shared.BoopState;

/** Adapts the existing Now Playing authority, without a second session tracker. */
public final class BoopMediaBridge {
    private static boolean initialized;
    private static boolean enabled;
    private BoopMediaBridge() { }
    public static synchronized void configure(Context context, boolean active) {
        enabled = active;
        initialize(context);
        publish(ShieldNowPlayingManager.get(context).state().current());
    }
    private static void publish(NowPlayingSnapshot snapshot) {
        BoopState.INSTANCE.media(!enabled || snapshot == null ? "" : Long.toString(snapshot.sessionId()),
                enabled && snapshot != null && snapshot.isPlaying(), SystemClock.uptimeMillis());
    }
    public static synchronized void initialize(Context context) {
        if (initialized) return;
        initialized = true;
        Context app = context.getApplicationContext();
        BoopState.INSTANCE.subscribe(snapshot -> {
            if(snapshot.owner != BoopState.Owner.MEDIA_CORNER) {
                BoopMediaCornerService.hideNow();
                app.stopService(new Intent(app,BoopMediaCornerService.class));
            } else if(Settings.canDrawOverlays(app)) {
                try { app.startForegroundService(new Intent(app,BoopMediaCornerService.class)); }
                catch(RuntimeException unavailable) { BoopMediaCornerService.hideNow(); }
            }
        });
        ShieldNowPlayingManager.get(app).state().subscribe(snapshot -> {
            publish(snapshot);
        });
        ShieldNowPlayingManager.get(app).refreshAccess();
    }
}
