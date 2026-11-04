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
            return ShieldHomeOverrideService.isServiceBound();
        } catch (Throwable ignored) {
            return false;
        }
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
            String boopService = pkg + "/com.boop.alpha1.BoopHomeOverrideService";
            String castService = pkg + "/com.boop.shieldhome.BoopCastVisibilityService";
            boolean hadBoop = a11y.contains(boopService);
            if (hadBoop && !ShieldHomeOverrideService.isServiceBound()) {
                // If registered but not bound (e.g. after force-stop or upgrade crash timeout),
                // briefly strip component to force SettingsProvider onChange and clear mCrashedServices
                Set<String> stripped = new LinkedHashSet<>(a11y);
                stripped.remove(boopService);
                stripped.remove(castService);
                Settings.Secure.putString(context.getContentResolver(),
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, String.join(":", stripped));
            }
            a11y.add(boopService);
            a11y.add(castService);
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

    private static volatile long lastDisplayCheckMs = 0L;
    private static volatile long lastSofabatonWakeMs = 0L;

    /**
     * Compatibility hook retained for existing lifecycle callers.
     * Display mode and system density belong to Android/NVIDIA. Forcing them
     * here reconfigures foreground players and leaves stale embedded surfaces.
     * BOOP scales its own resources through BoopTvChrome.applyShieldMetrics.
     */
    public static void ensureNative4kDisplayAsync(Context context) {
        // Intentionally does not change the global display, even on wake.
    }

    public static boolean isSofabatonConnected(Context context) {
        if (context == null) return false;
        try {
            android.hardware.input.InputManager im =
                    (android.hardware.input.InputManager) context.getSystemService(Context.INPUT_SERVICE);
            if (im == null) return false;
            int[] ids = im.getInputDeviceIds();
            if (ids == null) return false;
            for (int id : ids) {
                android.view.InputDevice dev = im.getInputDevice(id);
                if (dev != null && dev.getName() != null
                        && dev.getName().toLowerCase(java.util.Locale.ROOT).contains("sofabaton")) {
                    return true;
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }

    /**
     * Automatically wakes and reconnects the SofaBaton X1S Bluetooth HID link (Sofabaton 0D30)
     * by firing button.x1s_hub_home via Home Assistant whenever BOOP Home starts/resumes
     * or wakes from sleep and the remote is missing from InputManager.
     */
    public static void ensureSofabatonConnectedAsync(Context context) {
        if (context == null) return;
        if (isSofabatonConnected(context)) return;
        long now = android.os.SystemClock.elapsedRealtime();
        if (now - lastSofabatonWakeMs < 5000L) return;
        lastSofabatonWakeMs = now;
        final Context app = context.getApplicationContext();
        EXECUTOR.execute(() -> triggerSofabatonWakeWithRetry(app));
    }

    private static void triggerSofabatonWakeWithRetry(Context app) {
        if (isSofabatonConnected(app)) return;
        fireSofabatonHomeViaHa(app);
        MAIN.postDelayed(() -> {
            if (!isSofabatonConnected(app)) {
                EXECUTOR.execute(() -> {
                    if (!isSofabatonConnected(app)) {
                        fireSofabatonHomeViaHa(app);
                    }
                });
            }
        }, 4000L);
    }

    private static void fireSofabatonHomeViaHa(Context app) {
        try {
            com.boop.shieldoverlay.StoredHomeAssistantCredential stored =
                    new com.boop.shieldoverlay.SecureCredentialStore(app).load();
            if (stored == null) return;
            String base = stored.baseUrl();
            if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
            String accessToken = stored.refreshToken();
            if (accessToken == null || (!accessToken.startsWith("eyJ") && !accessToken.contains("."))) {
                java.net.HttpURLConnection authConn = (java.net.HttpURLConnection)
                        new java.net.URL(base + "/auth/token").openConnection();
                authConn.setConnectTimeout(5000);
                authConn.setReadTimeout(6000);
                authConn.setRequestMethod("POST");
                authConn.setDoOutput(true);
                authConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                String form = "grant_type=refresh_token&refresh_token="
                        + java.net.URLEncoder.encode(stored.refreshToken(), "UTF-8")
                        + "&client_id=" + java.net.URLEncoder.encode(stored.clientId(), "UTF-8");
                try (java.io.OutputStream os = authConn.getOutputStream()) {
                    os.write(form.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
                if (authConn.getResponseCode() >= 200 && authConn.getResponseCode() < 300) {
                    try (java.io.InputStream is = authConn.getInputStream()) {
                        String respStr = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                        org.json.JSONObject json = new org.json.JSONObject(respStr);
                        accessToken = json.optString("access_token", null);
                    }
                }
                authConn.disconnect();
            }
            if (accessToken == null || accessToken.isEmpty()) return;
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
                    new java.net.URL(base + "/api/services/button/press").openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(6000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            byte[] payload = "{\"entity_id\":\"button.x1s_hub_home\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(payload);
            }
            int code = conn.getResponseCode();
            android.util.Log.i("BOOPSelfGrant", "Sofabaton X1S wake dispatched via HA: HTTP " + code);
            conn.disconnect();
        } catch (Throwable t) {
            android.util.Log.w("BOOPSelfGrant", "Unable to dispatch Sofabaton X1S wake via HA", t);
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
                        + pkg + "/com.boop.shieldhome.BoopCastVisibilityService:"
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
