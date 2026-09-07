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
        if (BoopDeviceProfile.resolve(this) != BoopDeviceProfile.Mode.SHIELD) {
            return;
        }

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityPreCreated(Activity activity, Bundle savedInstanceState) {
                Resources resources = activity.getResources();
                Configuration scaled = new Configuration(resources.getConfiguration());
                int baseDensity = scaled.densityDpi;
                scaled.densityDpi = Math.max(1, Math.round(baseDensity * SHIELD_UI_SCALE));
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
