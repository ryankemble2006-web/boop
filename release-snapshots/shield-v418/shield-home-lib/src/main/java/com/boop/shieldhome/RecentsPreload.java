package com.boop.shieldhome;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.util.Log;
import com.boop.shieldturbo.power.AdbWire;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/** Native App Switch DOWN preloads cards before UP opens Recents. */
public final class RecentsPreload {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final AtomicBoolean PENDING = new AtomicBoolean();
    private static final class NoPrompt implements Runnable {
        NoPrompt() { }
        @Override public void run() { }
    }
    private RecentsPreload() { }

    public static void open(Context context) {
        if (context == null || !PENDING.compareAndSet(false, true)) return;
        try {
            EXECUTOR.execute(new Request(context));
        } catch (RuntimeException error) {
            PENDING.set(false);
            fallback(context);
        }
    }

    private static void fallback(Context context) {
        if (context instanceof AccessibilityService) {
            ((AccessibilityService) context).performGlobalAction(
                    AccessibilityService.GLOBAL_ACTION_RECENTS);
        }
    }

    private static final class Request implements Runnable {
        private final Context context;
        Request(Context context) { this.context = context; }
        @Override public void run() {
            boolean submitted = false;
            try {
                File key = new File(context.getNoBackupFilesDir(), "boop-unified-local-adb.key");
                if (!key.isFile()) throw new IOException("No trusted loopback ADB identity");
                try (AdbWire adb = new AdbWire()) {
                    adb.connect(5555, AdbWire.identity(key), 2000, new NoPrompt(), false);
                    // After submission a lost result is ambiguous: never toggle twice.
                    submitted = true;
                    AdbWire.Result result = adb.execute("input keyevent --longpress 187", 3000);
                    if (result.exitCode != 0) throw new IOException("App Switch dispatch failed");
                }
                Log.i("BOOPRecents", "Native App Switch dispatched with preload");
            } catch (Exception error) {
                Log.w("BOOPRecents", submitted ? "Native dispatch result uncertain; no second toggle"
                        : "Native preload unavailable; using accessibility fallback", error);
                if (!submitted) fallback(context);
            } finally {
                PENDING.set(false);
            }
        }
    }
}
