package com.boop.alpha1;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

public final class UnifiedApplication extends Application {
    private static final float SHIELD_UI_SCALE = 0.80f;

    @Override
    public void onCreate() {
        super.onCreate();
        stopService(new android.content.Intent(this,com.boop.shieldoverlay.BoopOverlayService.class));
        BoopDeviceProfile.Mode mode = BoopDeviceProfile.resolve(this);
        com.boop.shieldhome.BoopMediaBridge.configure(this, mode == BoopDeviceProfile.Mode.SHIELD);
        new com.boop.shieldoverlay.BoopPreferences(this); // migrate existing dashboard room first
        BoopRoom room = new BoopRoomPreferences(this).currentRoom();
        com.boop.shared.BoopState.INSTANCE.room(room.id(), room.name());
        if (BoopNotificationRuntime.shouldInitializeForMode(mode)) {
            BoopNotificationRuntime.initialize(this);
        }

        Resources applicationResources = getResources();
        int configuredDensity = applicationResources.getConfiguration().densityDpi;
        final int baseDensity = configuredDensity > 0
                ? configuredDensity
                : applicationResources.getDisplayMetrics().densityDpi;
        final int shieldDensity = Math.max(1, Math.round(baseDensity * SHIELD_UI_SCALE));

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityPreCreated(Activity activity, Bundle savedInstanceState) {
                Resources resources = activity.getResources();
                Configuration current = resources.getConfiguration();
                int targetDensity = BoopDeviceProfile.resolve(activity) == BoopDeviceProfile.Mode.SHIELD
                        ? shieldDensity : baseDensity;
                if (current.densityDpi == targetDensity) {
                    return;
                }
                Configuration scaled = new Configuration(current);
                scaled.densityDpi = targetDensity;
                resources.updateConfiguration(scaled, resources.getDisplayMetrics());
            }

            @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) { }
            @Override public void onActivityStarted(Activity activity) { }
            @Override public void onActivityResumed(Activity activity) { }
            @Override public void onActivityPaused(Activity activity) { }
            @Override public void onActivityStopped(Activity activity) { }
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
            @Override public void onActivityDestroyed(Activity activity) { }
        });

        new com.boop.shieldoverlay.LaunchCrashRecorder(this).installAsDefaultHandler();
    }
}
