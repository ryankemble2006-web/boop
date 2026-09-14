// SPDX-License-Identifier: GPL-3.0-or-later
package local.johnnycastaway.shield;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.io.InputStream;
import java.io.IOException;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import java.io.File;

/** Silent original-resource player. Call controls from UI or HA worker threads. */
public final class NativeJohnnyView extends View {
    static { System.loadLibrary("johnny"); }
    private final Object guard = new Object();
    private final int[] pixels = new int[640 * 480];
    private final Bitmap bitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
    private final Paint paint = new Paint();
    private final Rect destination = new Rect();
    private long handle;
    private boolean desiredRunning, night;
    private String lastStatus = "stopped";

    public NativeJohnnyView(Context context) {
        super(context);
        setBackgroundColor(Color.BLACK);
        paint.setFilterBitmap(false);
    }
    public void start() {
        synchronized (guard) {
            desiredRunning = true;
            if (handle != 0) return;
            final long generation = nCreate(new File(getContext().getFilesDir(), "johnny").getAbsolutePath(), night);
            if (generation == 0) { lastStatus = "native allocation failed"; return; }
            handle = generation;
            new Thread(() -> {
                try {
                    loadOiAssets(generation);
                    nRun(generation);
                }
                finally {
                    synchronized (guard) {
                        lastStatus = nStatus(generation);
                        nDestroy(generation);
                        handle = 0;
                        // A stop/start during teardown requests a new generation.
                        // Engine errors must remain visible instead of a restart loop.
                        if (desiredRunning && lastStatus.startsWith("stopped")) {
                            post(() -> { synchronized (guard) { if (desiredRunning && handle == 0) start(); } });
                        } else { desiredRunning = false; }
                    }
                }
            }, "Johnny-native").start();
        }
        postInvalidate();
    }
    /** Nonblocking cancellation. Worker frees all native resources before a new generation. */
    public void stop() {
        synchronized (guard) {
            desiredRunning = false;
            if (handle != 0) nStop(handle);
        }
    }
    public void setNight(boolean value) {
        synchronized (guard) {
            night = value;
            if (handle != 0) nNight(handle, value);
        }
    }
    /** One full fan routine for this request; false if stopped or queue (8) is full. */
    public boolean requestFan() {
        synchronized (guard) {
            return desiredRunning && handle != 0 && nFan(handle);
        }
    }
    public boolean requestOi() {
        synchronized (guard) { return desiredRunning && handle != 0 && nOi(handle); }
    }
    public void cancelOi() {
        synchronized (guard) { if (handle != 0) nCancelOi(handle); }
    }
    private void loadOiAssets(long generation) {
        int[] assetPixels = new int[5 * 80 * 90 + 92 * 64];
        int offset = 0;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        try {
            for (int i = 0; i < 6; i++) {
                String name = i == 5 ? "OI-BUBBLE.png" : "SHRUG.00" + i + ".png";
                int width = i == 5 ? 92 : 80, height = i == 5 ? 64 : 90;
                Bitmap pose;
                try (InputStream stream = getContext().getAssets().open("oi/" + name)) {
                    pose = BitmapFactory.decodeStream(stream, null, options);
                }
                if (pose == null) throw new IOException("Invalid OI asset");
                try {
                    if (pose.getWidth() != width || pose.getHeight() != height)
                        throw new IOException("Unexpected OI asset dimensions");
                    pose.getPixels(assetPixels, offset, width, 0, 0, width, height);
                    offset += width * height;
                } finally { pose.recycle(); }
            }
            synchronized (guard) {
                if (handle == generation && desiredRunning) nOiAssets(generation, assetPixels);
            }
        } catch (IOException | RuntimeException error) {
            Log.e("JohnnyHA", "OI assets unavailable; original story remains enabled");
        }
    }
    public String getStatus() {
        synchronized (guard) { return handle == 0 ? lastStatus : nStatus(handle); }
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        boolean again;
        synchronized (guard) {
            again = desiredRunning && handle != 0;
            if (handle != 0 && nCopy(handle, pixels))
                bitmap.setPixels(pixels, 0, 640, 0, 0, 640, 480);
        }
        float scale = Math.min(getWidth() / 640f, getHeight() / 480f);
        int w = Math.round(640 * scale), h = Math.round(480 * scale);
        int x = (getWidth() - w) / 2, y = (getHeight() - h) / 2;
        destination.set(x, y, x + w, y + h);
        canvas.drawBitmap(bitmap, null, destination, paint);
        if (again) postInvalidateDelayed(33);
    }
    @Override protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }
    private static native long nCreate(String directory, boolean night);
    private static native void nRun(long handle);
    private static native void nStop(long handle);
    private static native void nNight(long handle, boolean night);
    private static native boolean nFan(long handle);
    private static native boolean nOiAssets(long handle, int[] pixels);
    private static native boolean nOi(long handle);
    private static native void nCancelOi(long handle);
    private static native boolean nCopy(long handle, int[] pixels);
    private static native String nStatus(long handle);
    private static native void nDestroy(long handle);
}
