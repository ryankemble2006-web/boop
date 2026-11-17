package com.boop.shieldhome;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.boop.shieldturbo.power.AdbWire;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * True task termination helper.
 * Intercepts Android TV task switcher dismissals and executes local loopback
 * 'am force-stop' in 30ms, completely killing background Linux processes and Python threads.
 */
public final class BoopTaskKiller {
    private static final String TAG = "BOOPTaskKiller";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "BOOP-TaskKiller");
        t.setDaemon(true);
        return t;
    });

    private static final Map<String, String> KNOWN_PACKAGES = new HashMap<>();
    static {
        KNOWN_PACKAGES.put("kodinerds omega", "net.kodinerds.maven.kodi21");
        KNOWN_PACKAGES.put("kodinerds", "net.kodinerds.maven.kodi21");
        KNOWN_PACKAGES.put("kodi", "org.xbmc.kodi");
        KNOWN_PACKAGES.put("forki", "com.forki.app");
        KNOWN_PACKAGES.put("forked", "com.fork2.app");
        KNOWN_PACKAGES.put("forkedagain", "com.fork2.app");
        KNOWN_PACKAGES.put("fork2", "com.fork2.app");
        KNOWN_PACKAGES.put("deezer", "deezer.android.app");
        KNOWN_PACKAGES.put("youtube", "com.google.android.youtube.tv");
        KNOWN_PACKAGES.put("govee room tv reference", "local.govee.room.tvreference");
        KNOWN_PACKAGES.put("sleep tv timer", "com.neon.sleeptvtimerpro");
        KNOWN_PACKAGES.put("sleep tv timer pro", "com.neon.sleeptvtimerpro");
    }

    private BoopTaskKiller() {}

    public static void forceStopByTitle(Context context, String title) {
        if (title == null || title.trim().isEmpty()) return;
        EXECUTOR.execute(() -> {
            String pkg = resolvePackageName(context, title.trim());
            if (pkg != null && !pkg.isEmpty()) {
                forceStopPackageDirect(context, pkg);
            }
        });
    }

    public static void forceStopPackage(Context context, String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        EXECUTOR.execute(() -> forceStopPackageDirect(context, packageName));
    }

    private static void forceStopPackageDirect(Context context, String packageName) {
        try {
            File key = new File(context.getNoBackupFilesDir(), "boop-unified-local-adb.key");
            if (!key.isFile()) {
                Log.w(TAG, "No local ADB identity key available for task kill");
                return;
            }
            try (AdbWire adb = new AdbWire()) {
                adb.connect(5555, AdbWire.identity(key), 2000, () -> {}, false);
                AdbWire.Result res = adb.execute("am force-stop " + packageName.trim(), 2000);
                Log.i(TAG, "Chopped head off: " + packageName + " (exit: " + res.exitCode + ")");
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to force-stop " + packageName, e);
        }
    }

    public static String resolvePackageName(Context context, String title) {
        String lower = title.toLowerCase(java.util.Locale.ROOT);
        String known = KNOWN_PACKAGES.get(lower);
        if (known != null) return known;

        if (context != null) {
            try {
                PackageManager pm = context.getPackageManager();
                if (pm != null) {
                    List<ApplicationInfo> apps = pm.getInstalledApplications(0);
                    if (apps != null) {
                        for (ApplicationInfo app : apps) {
                            CharSequence label = pm.getApplicationLabel(app);
                            if (label != null && label.toString().equalsIgnoreCase(title)) {
                                return app.packageName;
                            }
                        }
                        // Partial match fallback
                        for (ApplicationInfo app : apps) {
                            CharSequence label = pm.getApplicationLabel(app);
                            if (label != null && label.toString().toLowerCase(java.util.Locale.ROOT).contains(lower)) {
                                return app.packageName;
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }
        return null;
    }

    public static void ensureAccessibilityEnabled(Context context) {
        EXECUTOR.execute(() -> {
            try {
                File key = new File(context.getNoBackupFilesDir(), "boop-unified-local-adb.key");
                if (!key.isFile()) return;
                try (AdbWire adb = new AdbWire()) {
                    adb.connect(5555, AdbWire.identity(key), 2000, () -> {}, false);
                    // 1. Ensure customize_menu_option is -1 (destroys the settings popup on burger key)
                    adb.execute("settings put global customize_menu_option -1", 2000);

                    // 2. Ensure boop accessibility service is enabled
                    AdbWire.Result r = adb.execute("settings get secure enabled_accessibility_services", 2000);
                    String current = r.output != null ? r.output.trim() : "";
                    String boopService = "com.boop.shieldoverlay/com.boop.alpha1.BoopHomeOverrideService";
                    if (!current.contains(boopService)) {
                        String updated = current.isEmpty() ? boopService : current + ":" + boopService;
                        adb.execute("settings put secure enabled_accessibility_services \"" + updated + "\"", 2000);
                        adb.execute("settings put secure accessibility_enabled 1", 2000);
                        Log.i(TAG, "Enabled BOOP accessibility service");
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to ensure accessibility service enabled", e);
            }
        });
    }

    public static void evictButtonMapper(Context context, Runnable onComplete) {
        EXECUTOR.execute(() -> {
            try {
                File key = new File(context.getNoBackupFilesDir(), "boop-unified-local-adb.key");
                if (!key.isFile()) return;
                try (AdbWire adb = new AdbWire()) {
                    adb.connect(5555, AdbWire.identity(key), 2000, () -> {}, false);
                    // Remove flar2.homebutton from enabled_accessibility_services
                    AdbWire.Result r = adb.execute("settings get secure enabled_accessibility_services", 2000);
                    String current = r.output != null ? r.output.trim() : "";
                    if (current.contains("flar2.homebutton")) {
                        String[] parts = current.split(":");
                        StringBuilder sb = new StringBuilder();
                        for (String p : parts) {
                            if (!p.contains("flar2.homebutton")) {
                                if (sb.length() > 0) sb.append(":");
                                sb.append(p);
                            }
                        }
                        adb.execute("settings put secure enabled_accessibility_services \"" + sb.toString() + "\"", 2000);
                    }
                    // Force stop & disable Button Mapper
                    adb.execute("am force-stop flar2.homebutton", 2000);
                    adb.execute("pm disable-user --user 0 flar2.homebutton", 2000);
                    Log.i(TAG, "Evicted Button Mapper cleanly");
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to evict Button Mapper", e);
            } finally {
                if (onComplete != null) {
                    try {
                        new Handler(Looper.getMainLooper()).post(onComplete);
                    } catch (Throwable t) {
                        onComplete.run();
                    }
                }
            }
        });
    }
}
