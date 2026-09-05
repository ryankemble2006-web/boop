package com.boop.shieldoverlay;

import android.app.Application;

public final class BoopApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LaunchCrashRecorder crashRecorder = new LaunchCrashRecorder(this);
        crashRecorder.installAsDefaultHandler();
    }
}
