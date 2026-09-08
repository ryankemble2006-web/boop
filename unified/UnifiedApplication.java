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
        BoopDeviceProfile.Mode mode = BoopDeviceProfile.resolve(this);
        if (BoopNotificationRuntime.shouldInitializeForMode(mode)) {
            BoopNotificationRuntime.initialize(this);
            return;
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
                if (current.densityDpi == shieldDensity) {
                    return;
                }
                Configuration scaled = new Configuration(current);
                scaled.densityDpi = shieldDensity;
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
