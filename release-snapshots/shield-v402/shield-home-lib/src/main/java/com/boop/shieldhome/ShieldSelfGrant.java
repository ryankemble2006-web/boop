package com.boop.shieldhome;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.view.accessibility.AccessibilityManager;
import com.boop.shieldturbo.power.AdbWire;
import java.io.File;
import java.security.KeyPair;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Self-grants BOOP's Media/Notification Listener, Accessibility Home/Remote Service,
 * Overlay permission, Audio capture, and Default Home role on NVIDIA Shield TV
 * via WRITE_SECURE_SETTINGS (if already granted) or local loopback ADB (127.0.0.1:5555),
 * avoiding broken or missing Android TV Settings intents.
 */
public final class ShieldSelfGrant {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private ShieldSelfGrant() { }

    public static ComponentName notificationListenerComponent(Context context) {
        return new ComponentName(context.getPackageName(),
                "com.boop.shieldhome.ShieldNowPlayingListenerService");
    }

    public static ComponentName homeOverrideComponent(Context context) {
        return new ComponentName(context.getPackageName(),
                "com.boop.alpha1.BoopHomeOverrideService");
    }

    public static ComponentName defaultHomeComponent(Context context) {
        return new ComponentName(context.getPackageName(),
                "com.boop.alpha1.UnifiedEntryActivity");
    }

    public static boolean isNotificationAccessGranted(Context context) {
        try {
            ComponentName cn = notificationListenerComponent(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                NotificationManager nm = context.getSystemService(NotificationManager.class);
                if (nm != null && nm.isNotificationListenerAccessGranted(cn)) {
                    return true;
                }
            }
            String enabled = Settings.Secure.getString(
                    context.getContentResolver(), "enabled_notification_listeners");
            if (enabled != null) {
                for (String item : enabled.split(":")) {
                    if (cn.equals(ComponentName.unflattenFromString(item))) {
                        return true;
                    }
                }
            }
        } catch (Throwable ignored) { }
        return false;
    }

    public static boolean isHomeAccessibilityEnabled(Context context) {
        try {
            String pkg = context.getPackageName();
            String enabled = Settings.Secure.getString(
                    context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (enabled != null && (enabled.contains(pkg + "/com.boop.alpha1.BoopHomeOverrideService")
                    || enabled.contains(pkg + "/com.boop.shieldhome.ShieldHomeOverrideService"))) {
                return true;
            }
            AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
            if (am != null) {
                List<AccessibilityServiceInfo> list = am.getEnabledAccessibilityServiceList(
                        AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
                if (list != null) {
                    for (AccessibilityServiceInfo info : list) {
                        ResolveInfo ri = info == null ? null : info.getResolveInfo();
                        if (ri != null && ri.serviceInfo != null && pkg.equals(ri.serviceInfo.packageName)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Throwable ignored) { }
        return false;
    }

    public static boolean isDefaultHome(Context context) {
        try {
            Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
            ResolveInfo resolved = context.getPackageManager().resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY);
            return resolved != null && resolved.activityInfo != null
                    && context.getPackageName().equals(resolved.activityInfo.packageName);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isAllReady(Context context) {
        return isNotificationAccessGranted(context)
                && isHomeAccessibilityEnabled(context)
                && Settings.canDrawOverlays(context);
    }

    /** Attempts fast in-process grant if WRITE_SECURE_SETTINGS is already held. */
    public static boolean tryDirectSecureSettingsGrant(Context context) {
        try {
            if (context.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS")
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            String pkg = context.getPackageName();
            // 1. Notification listener
            String listenerFlat = notificationListenerComponent(context).flattenToString();
            String existingListeners = Settings.Secure.getString(
                    context.getContentResolver(), "enabled_notification_listeners");
            Set<String> listeners = new LinkedHashSet<>();
            if (existingListeners != null && !existingListeners.isBlank()) {
                for (String s : existingListeners.split(":")) {
                    if (!s.isBlank()) listeners.add(s);
                }
            }
            listeners.add(listenerFlat);
            Settings.Secure.putString(context.getContentResolver(),
                    "enabled_notification_listeners", String.join(":", listeners));

            // 2. Accessibility services
            String existingA11y = Settings.Secure.getString(
                    context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            Set<String> a11y = new LinkedHashSet<>();
            if (existingA11y != null && !existingA11y.isBlank()) {
                for (String s : existingA11y.split(":")) {
                    if (!s.isBlank()) a11y.add(s);
                }
            }
            a11y.add(pkg + "/com.boop.alpha1.BoopHomeOverrideService");
            a11y.add("uk.local.eastenders/uk.local.eastenders.EastEndersAccessibilityService");
            a11y.add("uk.local.casualty/uk.local.casualty.CasualtyAccessibilityService");
            Settings.Secure.putString(context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, String.join(":", a11y));
            Settings.Secure.putInt(context.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED, 1);

            try {
                NotificationListenerService.requestRebind(notificationListenerComponent(context));
            } catch (Throwable ignored) { }
            return isNotificationAccessGranted(context) && isHomeAccessibilityEnabled(context);
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Ensures Media/Notification listener, Accessibility Home/Remote service,
     * Overlay permission, Audio projection, and Default Home are all granted,
     * falling back to BOOP's local loopback ADB (127.0.0.1:5555) and prompting
     * for one-time ADB approval on first boot if needed.
     */
    public static void ensureAllGrantedAsync(Context context, Callback callback) {
        final Context app = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            tryDirectSecureSettingsGrant(app);
            if (isAllReady(app) && isDefaultHome(app)) {
                try {
                    NotificationListenerService.requestRebind(notificationListenerComponent(app));
                } catch (Throwable ignored) { }
                MAIN.post(() -> {
                    ShieldNowPlayingManager.get(app).refreshAccess();
                    if (callback != null) callback.onResult(true, "All BOOP permissions and Home access active.");
                });
                return;
            }

            String pkg = app.getPackageName();
            File keyFile = new File(app.getNoBackupFilesDir(), "boop-unified-local-adb.key");
            try (AdbWire adb = new AdbWire()) {
                KeyPair identity = AdbWire.identity(keyFile);
                adb.connect(5555, identity, 45_000, () -> {}, true);
                String cmd = "pm grant " + pkg + " android.permission.WRITE_SECURE_SETTINGS >/dev/null 2>&1; "
                        + "pm grant " + pkg + " android.permission.RECORD_AUDIO >/dev/null 2>&1; "
                        + "cmd notification allow_listener " + pkg + "/com.boop.shieldhome.ShieldNowPlayingListenerService >/dev/null 2>&1; "
                        + "appops set " + pkg + " SYSTEM_ALERT_WINDOW allow >/dev/null 2>&1; "
                        + "appops set " + pkg + " PROJECT_MEDIA allow >/dev/null 2>&1; "
                        + "appops set " + pkg + " GET_USAGE_STATS allow >/dev/null 2>&1; "
                        + "settings put secure enabled_accessibility_services "
                        + pkg + "/com.boop.alpha1.BoopHomeOverrideService:"
                        + "uk.local.eastenders/uk.local.eastenders.EastEndersAccessibilityService:"
                        + "uk.local.casualty/uk.local.casualty.CasualtyAccessibilityService >/dev/null 2>&1; "
                        + "settings put secure accessibility_enabled 1 >/dev/null 2>&1; "
                        + "cmd package set-home-activity " + pkg + "/com.boop.alpha1.UnifiedEntryActivity >/dev/null 2>&1; "
                        + "pm disable-user --user 0 com.google.android.tvlauncher >/dev/null 2>&1; "
                        + "pm disable-user --user 0 com.google.android.tvrecommendations >/dev/null 2>&1; "
                        + "pm disable-user --user 0 com.google.android.tungsten.setupwraith >/dev/null 2>&1; "
                        + "echo BOOP_GRANT_OK";
                adb.execute(cmd, 20_000);
                tryDirectSecureSettingsGrant(app);
                try {
                    NotificationListenerService.requestRebind(notificationListenerComponent(app));
                } catch (Throwable ignored) { }
                boolean ok = isNotificationAccessGranted(app);
                MAIN.post(() -> {
                    ShieldNowPlayingManager.get(app).refreshAccess();
                    if (callback != null) {
                        callback.onResult(ok, ok
                                ? "BOOP Media, Home & System access enabled."
                                : "Could not verify Media access.");
                    }
                });
            } catch (Exception e) {
                MAIN.post(() -> {
                    if (callback != null) {
                        callback.onResult(false, "Enable Network Debugging in Developer Options and approve BOOP.");
                    }
                });
            }
        });
    }

    public interface Callback {
        void onResult(boolean success, String message);
    }
}
