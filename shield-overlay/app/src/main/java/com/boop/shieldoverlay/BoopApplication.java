package com.boop.shieldoverlay;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.os.Bundle;

public final class BoopApplication extends Application {
    private static final float UI_SCALE = 0.80f;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityPreCreated(Activity activity, Bundle savedInstanceState) {
                Configuration override = new Configuration();
                int baseDensity = getResources().getConfiguration().densityDpi;
                override.densityDpi = Math.max(1, Math.round(baseDensity * UI_SCALE));
                activity.applyOverrideConfiguration(override);
            }

            @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) { }
            @Override public void onActivityStarted(Activity activity) { }
            @Override public void onActivityResumed(Activity activity) { }
            @Override public void onActivityPaused(Activity activity) { }
            @Override public void onActivityStopped(Activity activity) { }
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
            @Override public void onActivityDestroyed(Activity activity) { }
        });

        LaunchCrashRecorder crashRecorder = new LaunchCrashRecorder(this);
        crashRecorder.installAsDefaultHandler();
    }
}
