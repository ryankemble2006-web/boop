package com.boop.shieldoverlay;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class LaunchCrashRecorder {
    private static final String PREFS_NAME = "boop_launch_crash";
    private static final String KEY_REPORT = "pending_crash_report_v1";

    interface Store {
        String getString(String key);
        void putString(String key, String value);
        void remove(String key);
    }

    private final Store store;

    public LaunchCrashRecorder(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is required");
        }
        SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        store = new SharedPreferencesStore(preferences);
    }

    LaunchCrashRecorder(Store store) {
        if (store == null) {
            throw new IllegalArgumentException("store is required");
        }
        this.store = store;
    }

    public void installAsDefaultHandler() {
        final Thread.UncaughtExceptionHandler previous =
                Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                record(throwable);
            } catch (Throwable ignored) {
                // Diagnostics must never replace the original crash.
            }
            if (previous != null) {
                previous.uncaughtException(thread, throwable);
            }
        });
    }

    public void record(Throwable throwable) {
        if (throwable == null) {
            return;
        }
        StringWriter buffer = new StringWriter();
        PrintWriter writer = new PrintWriter(buffer);
        throwable.printStackTrace(writer);
        writer.flush();
        store.putString(KEY_REPORT, buffer.toString());
    }

    public String consume() {
        String report = store.getString(KEY_REPORT);
        if (report == null) {
            return null;
        }
        store.remove(KEY_REPORT);
        return report.trim().isEmpty() ? null : report;
    }

    private static final class SharedPreferencesStore implements Store {
        private final SharedPreferences preferences;

        private SharedPreferencesStore(SharedPreferences preferences) {
            this.preferences = preferences;
        }

        @Override
        public String getString(String key) {
            return preferences.getString(key, null);
        }

        @Override
        public void putString(String key, String value) {
            preferences.edit().putString(key, value).commit();
        }

        @Override
        public void remove(String key) {
            preferences.edit().remove(key).commit();
        }
    }
}
