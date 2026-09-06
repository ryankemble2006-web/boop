package com.boop.shieldoverlay;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;

public final class DeezerPuppetAccess {
    private static final String PREFS_NAME = "boop_deezer_puppet";
    private static final String KEY_ENABLED = "enabled_v1";
    private static final String ENABLED_LISTENERS = "enabled_notification_listeners";
    private static volatile DeezerPuppetAccess instance;

    private final Context applicationContext;
    private final SharedPreferences preferences;
    private final NotificationManager notificationManager;
    private final ComponentName listenerComponent;
    private final Handler mainHandler;
    private final MediaPuppetState state = new MediaPuppetState();
    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceListener;
    private final ContentObserver accessObserver;
    private boolean listenerConnected;

    public static DeezerPuppetAccess get(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is required");
        }
        DeezerPuppetAccess local = instance;
        if (local == null) {
            synchronized (DeezerPuppetAccess.class) {
                local = instance;
                if (local == null) {
                    local = new DeezerPuppetAccess(context.getApplicationContext());
                    instance = local;
                }
            }
        }
        return local;
    }

    private DeezerPuppetAccess(Context applicationContext) {
        this.applicationContext = applicationContext;
        preferences = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        notificationManager = applicationContext.getSystemService(NotificationManager.class);
        listenerComponent = new ComponentName(applicationContext, DeezerMediaListenerService.class);
        mainHandler = new Handler(Looper.getMainLooper());

        preferenceListener = (sharedPreferences, key) -> {
            if (KEY_ENABLED.equals(key)) {
                refresh();
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(preferenceListener);

        accessObserver = new ContentObserver(mainHandler) {
            @Override
            public void onChange(boolean selfChange) {
                refresh();
            }
        };
        try {
            applicationContext.getContentResolver().registerContentObserver(
                    Settings.Secure.getUriFor(ENABLED_LISTENERS), false, accessObserver);
        } catch (SecurityException denied) {
            // Status still refreshes on listener callbacks and Settings resume.
        }
        refresh();
    }

    public MediaPuppetState state() {
        return state;
    }

    public void setEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_ENABLED, enabled).apply();
        boolean granted = isAccessGranted();
        publish(enabled, granted, listenerConnected);
        if (enabled && granted) {
            try {
                NotificationListenerService.requestRebind(listenerComponent);
            } catch (SecurityException denied) {
                // Android remains the authority; the status stays Connecting until bound.
            }
        }
    }

    public void refresh() {
        publish(
                preferences.getBoolean(KEY_ENABLED, false),
                isAccessGranted(),
                listenerConnected);
    }

    public boolean openAccessSettings(Activity activity) {
        if (activity == null) {
            return false;
        }
        try {
            activity.startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
            return true;
        } catch (ActivityNotFoundException | SecurityException unavailable) {
            return false;
        }
    }

    void setListenerConnected(boolean connected) {
        listenerConnected = connected;
        refresh();
    }

    private boolean isAccessGranted() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                return notificationManager != null
                        && notificationManager.isNotificationListenerAccessGranted(
                                listenerComponent);
            }
            String enabled = Settings.Secure.getString(
                    applicationContext.getContentResolver(), ENABLED_LISTENERS);
            if (enabled == null || enabled.isEmpty()) {
                return false;
            }
            for (String flattened : enabled.split(":")) {
                ComponentName component = ComponentName.unflattenFromString(flattened);
                if (listenerComponent.equals(component)) {
                    return true;
                }
            }
            return false;
        } catch (SecurityException denied) {
            return false;
        }
    }

    private void publish(boolean enabled, boolean granted, boolean connected) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            state.updateAccess(enabled, granted, connected);
        } else {
            mainHandler.post(() -> state.updateAccess(enabled, granted, connected));
        }
    }
}
