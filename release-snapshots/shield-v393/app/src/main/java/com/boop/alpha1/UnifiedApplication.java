package com.boop.alpha1;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;

public final class UnifiedApplication extends Application {
    // Reference 1080p baseline: 1080 / 675dp viewport = 1.6 scale factor = 256 DPI.
    // At 4K (2160p): 2160 / 675dp viewport = 3.2 scale factor = 512 DPI.
    // Locking this keeps visual proportions 100% constant across all resolutions and wake cycles.
    public static int computeShieldTargetDensity(Context context) {
        if (context == null) return 256;
        Resources res = context.getResources();
        if (res == null) return 256;
        DisplayMetrics dm = res.getDisplayMetrics();
        int height = Math.min(dm.widthPixels, dm.heightPixels);
        if (height <= 0) height = 1080;
        return Math.max(1, Math.round(height * (160f / 675f)));
    }

    public static void applyShieldMetrics(Context context) {
        if (context == null) return;
        if (BoopDeviceProfile.resolve(context) != BoopDeviceProfile.Mode.SHIELD) return;
        applyToResources(context.getResources(), context);
        Context app = context.getApplicationContext();
        if (app != null && app != context) {
            applyToResources(app.getResources(), context);
        }
    }

    private static void applyToResources(Resources res, Context context) {
        if (res == null) return;
        int targetDpi = computeShieldTargetDensity(context);
        float targetDensity = targetDpi / 160f;
        Configuration config = res.getConfiguration();
        DisplayMetrics dm = res.getDisplayMetrics();
        float fontScale = config.fontScale > 0 ? config.fontScale : 1.0f;
        float targetScaledDensity = targetDensity * fontScale;

        boolean configChanged = config.densityDpi != targetDpi;
        boolean metricsChanged = dm.densityDpi != targetDpi
                || Math.abs(dm.density - targetDensity) > 0.001f
                || Math.abs(dm.scaledDensity - targetScaledDensity) > 0.001f;

        if (configChanged || metricsChanged) {
            config.densityDpi = targetDpi;
            dm.densityDpi = targetDpi;
            dm.density = targetDensity;
            dm.scaledDensity = targetScaledDensity;
            try {
                res.updateConfiguration(config, dm);
            } catch (Throwable ignored) {}
        }
    }

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

        applyShieldMetrics(this);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityPreCreated(Activity activity, Bundle savedInstanceState) {
                applyShieldMetrics(activity);
            }

            @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                applyShieldMetrics(activity);
            }
            @Override public void onActivityStarted(Activity activity) {
                applyShieldMetrics(activity);
            }
            @Override public void onActivityResumed(Activity activity) {
                applyShieldMetrics(activity);
                com.boop.shieldhome.BoopTvChrome.install(activity);
                if (BoopDeviceProfile.resolve(activity) == BoopDeviceProfile.Mode.SHIELD)
                    com.boop.shieldhome.LauncherRemoteNavigation.install(activity);
            }
            @Override public void onActivityPaused(Activity activity) {
                com.boop.shieldhome.LauncherRemoteNavigation.cancel(activity);
            }
            @Override public void onActivityStopped(Activity activity) { }
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
            @Override public void onActivityDestroyed(Activity activity) { }
        });

        new com.boop.shieldoverlay.LaunchCrashRecorder(this).installAsDefaultHandler();
        BoopSharedEyeColourRuntime.initialize(this);
        BoopSharedFeltColourRuntime.initialize(this);
        BoopSharedHandColourRuntime.initialize(this);
        BoopSharedVoiceProfileRuntime.initialize(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyShieldMetrics(this);
    }
}
