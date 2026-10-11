package com.boop.shieldhome;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.graphics.PixelFormat;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;
import com.boop.shieldturbo.power.AdbWire;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/** Owns only a cold Kodi launch. An already-running Kodi is never moved or closed. */
final class BackgroundKodiSync implements AutoCloseable {
    private final Context context;
    private final Handler main = new Handler(Looper.getMainLooper());
    private VirtualDisplay display;
    private ImageReader frames;
    private volatile boolean closed;
    BackgroundKodiSync(Context context) { this.context = context.getApplicationContext(); }

    // Called on the sync worker. Creation and disposal stay on the main thread.
    boolean open() throws Exception {
        closed = false;
        // Kodi Splash redirects Main to the physical display. Main is exported and
        // initialized already on installed Kodi; use it directly and fail closed.
        android.content.ComponentName player = new android.content.ComponentName("org.xbmc.kodi", "org.xbmc.kodi.Main");
        if (!context.getPackageManager().getActivityInfo(player, 0).exported) return false;
        final String component = player.flattenToShortString();
        FutureTask<Integer> create = new FutureTask<>(() -> {
            if (closed) return -1;
            // A non-resizable Kodi activity only accepts a display matching its TV configuration.
            android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
            context.getSystemService(android.view.WindowManager.class).getDefaultDisplay().getRealMetrics(metrics);
            frames = ImageReader.newInstance(metrics.widthPixels,metrics.heightPixels,PixelFormat.RGBA_8888,2);
            frames.setOnImageAvailableListener(reader -> {
                try (android.media.Image frame = reader.acquireLatestImage()) { }
                catch (IllegalStateException disposed) { }
            }, main);
            display = context.getSystemService(DisplayManager.class).createVirtualDisplay(
                    "BOOP-background-kodi-sync",metrics.widthPixels,metrics.heightPixels,metrics.densityDpi,frames.getSurface(),
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC | DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
                    | DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION | 256);
            return display == null ? -1 : display.getDisplay().getDisplayId();
        });
        main.post(create);
        int id = create.get(5,TimeUnit.SECONDS);
        if (id <= 0 || closed) { close(); return false; }
        String nonce = UUID.randomUUID().toString().replace("-", "");
        File marker = new File(context.getFilesDir(),"boop-kodi-" + nonce);
        try {
            Files.write(marker.toPath(),nonce.getBytes(StandardCharsets.US_ASCII));
            File key = new File(context.getNoBackupFilesDir(),"boop-unified-local-adb.key");
            if (!key.isFile()) throw new java.io.IOException("No approved local ADB identity");
            try (AdbWire adb = new AdbWire()) {
                adb.connect(5555,AdbWire.identity(key),1500,() -> {},false);
                AdbWire.Result binding = adb.execute("run-as " + context.getPackageName()
                        + " cat files/boop-kodi-" + nonce + " 2>/dev/null",1500);
                if (binding.exitCode != 0 || !nonce.equals(binding.output.trim()))
                    throw new java.io.IOException("Local hardware binding failed");
                if (closed) return false;
                AdbWire.Result result = adb.execute("am start --display " + id + " -f 0x18000000 -n " + component,5000);
                if (result.exitCode != 0 || result.output.contains("Error:") || result.output.contains("Exception")
                        || result.output.contains("does not support"))
                    throw new java.io.IOException("Hidden Kodi launch failed");
            }
            android.util.Log.i("BOOPKodiSync","Kodi started offscreen; waiting for Seren startup");
            return true;
        } catch (Exception failure) { close(); throw failure; }
        finally { marker.delete(); }
    }
    @Override public void close() {
        closed = true;
        main.post(() -> {
            if (display != null) { display.release(); display = null; }
            if (frames != null) { frames.close(); frames = null; }
        });
    }
}
