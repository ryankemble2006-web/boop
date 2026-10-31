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
    private static String visiblePackage="";
    private static NowPlayingSnapshot media;
    private BoopMediaBridge() { }
    public static synchronized void configure(Context context, boolean active) {
        enabled = active;
        initialize(context);
        publish(ShieldNowPlayingManager.get(context).state().current());
        reconcile(context.getApplicationContext(),BoopState.INSTANCE.snapshot());
    }
    private static void publish(NowPlayingSnapshot snapshot) {
        media=snapshot;
        updateCorner();
        BoopState.INSTANCE.media(!enabled || snapshot == null ? "" : Long.toString(snapshot.sessionId()),
                enabled && snapshot != null && snapshot.isPlaying(), SystemClock.uptimeMillis());
    }
    public static void foreground(String packageName) {
        visiblePackage=packageName == null ? "" : packageName;
        updateCorner();
    }
    private static void updateCorner() {
        BoopState.INSTANCE.cornerAllowed(enabled && media!=null
            && com.boop.shared.CastCornerPolicy.allowed(visiblePackage,media.packageName(),media.castAppName()));
    }
    public static synchronized void initialize(Context context) {
        if (initialized) return;
        initialized = true;
        Context app = context.getApplicationContext();
        BoopState.INSTANCE.subscribe(snapshot -> reconcile(app,snapshot));
        ShieldNowPlayingManager.get(app).state().subscribe(snapshot -> {
            publish(snapshot);
        });
        ShieldNowPlayingManager.get(app).refreshAccess();
    }
    private static void reconcile(Context app, BoopState.Snapshot snapshot) {
        if(snapshot.owner != BoopState.Owner.MEDIA_CORNER || !Settings.canDrawOverlays(app)) {
            BoopMediaCornerService.hideNow();
            app.stopService(new Intent(app,BoopMediaCornerService.class));
        } else {
            try { app.startForegroundService(new Intent(app,BoopMediaCornerService.class)); }
            catch(RuntimeException unavailable) {
                android.util.Log.w("BOOP-Media","E920: Android could not start the media corner",unavailable);
                BoopMediaCornerService.hideNow();
            }
        }
    }
}
