package com.boop.alpha1;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;

final class BoopMirrorController {
    interface Listener {
        void onClosed();
    }

    private final Activity activity;
    private final FrameLayout host;
    private final Listener listener;

    private FrameLayout overlay;
    private TextureView texture;
    private TextView insideValues;
    private TextView outsideValues;
    private HandlerThread cameraThread;
    private Handler cameraHandler;
    private CameraDevice camera;
    private CameraCaptureSession session;
    private boolean open;
    private boolean resumed;

    BoopMirrorController(Activity activity, FrameLayout host, Listener listener) {
        this.activity = activity;
        this.host = host;
        this.listener = listener;
    }

    boolean isOpen() {
        return open;
    }

    boolean open() {
        if (open) {
            return true;
        }
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (findFrontCamera(manager) == null) {
                return false;
            }
        } catch (CameraAccessException error) {
            return false;
        }

        open = true;
        buildOverlay();
        if (resumed && texture.isAvailable()) {
            startPreview(texture.getSurfaceTexture(), texture.getWidth(), texture.getHeight());
        }
        return true;
    }

    void close() {
        if (!open) {
            return;
        }
        open = false;
        stopCamera();
        if (overlay != null && overlay.getParent() == host) {
            host.removeView(overlay);
        }
        overlay = null;
        texture = null;
        insideValues = null;
        outsideValues = null;
        if (listener != null) {
            listener.onClosed();
        }
    }

    void onResume() {
        resumed = true;
        if (open && texture != null && texture.isAvailable()) {
            startPreview(texture.getSurfaceTexture(), texture.getWidth(), texture.getHeight());
        }
    }

    void onPause() {
        resumed = false;
        stopCamera();
    }

    void shutdown() {
        open = false;
        stopCamera();
        if (overlay != null && overlay.getParent() == host) {
            host.removeView(overlay);
        }
        overlay = null;
        texture = null;
        insideValues = null;
        outsideValues = null;
    }

    void setSensorText(String inside, String outside) {
        if (insideValues != null) {
            insideValues.setText(cleanSensorText(inside));
        }
        if (outsideValues != null) {
            outsideValues.setText(cleanSensorText(outside));
        }
    }

    private void buildOverlay() {
        overlay = new FrameLayout(activity);
        overlay.setBackgroundColor(Color.BLACK);
        overlay.setContentDescription("BOOP mirror. Tap the mirror to return to BOOP.");

        texture = new TextureView(activity);
        texture.setScaleX(-1f);
        texture.setOpaque(false);
        texture.setOnClickListener(view -> close());
        texture.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                if (open && resumed) {
                    startPreview(surface, width, height);
                }
            }

            @Override public void onSurfaceTextureSizeChanged(
                    SurfaceTexture surface, int width, int height) { }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                stopCamera();
                return true;
            }

            @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) { }
        });
        overlay.addView(texture, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        overlay.addView(sensorRail("INSIDE", true), railParams(Gravity.START));
        overlay.addView(sensorRail("OUTSIDE", false), railParams(Gravity.END));

        host.addView(overlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.bringToFront();
    }

    private LinearLayout sensorRail(String heading, boolean inside) {
        LinearLayout rail = new LinearLayout(activity);
        rail.setOrientation(LinearLayout.VERTICAL);
        rail.setGravity(Gravity.CENTER_VERTICAL);
        rail.setPadding(dp(22), dp(28), dp(22), dp(28));
        rail.setBackgroundColor(Color.argb(170, 0, 0, 0));

        TextView label = new TextView(activity);
        label.setText(heading);
        label.setTextColor(Color.rgb(61, 220, 255));
        label.setTextSize(18f);
        label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        label.setLetterSpacing(0.08f);
        rail.addView(label);

        TextView values = new TextView(activity);
        values.setText("Waiting for sensors");
        values.setTextColor(Color.WHITE);
        values.setTextSize(23f);
        values.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        values.setGravity(Gravity.START);
        LinearLayout.LayoutParams valuesParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        valuesParams.topMargin = dp(14);
        rail.addView(values, valuesParams);
        if (inside) {
            insideValues = values;
        } else {
            outsideValues = values;
        }
        return rail;
    }

    private FrameLayout.LayoutParams railParams(int gravity) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                dp(190),
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = gravity;
        return params;
    }

    private void startPreview(SurfaceTexture surfaceTexture, int width, int height) {
        if (!open || !resumed || surfaceTexture == null || camera != null) {
            return;
        }
        stopCamera();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = findFrontCamera(manager);
            if (cameraId == null) {
                return;
            }
            surfaceTexture.setDefaultBufferSize(Math.max(1, width), Math.max(1, height));
            cameraThread = new HandlerThread("boop-mirror-camera");
            cameraThread.start();
            cameraHandler = new Handler(cameraThread.getLooper());
            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice device) {
                    if (!open || !resumed || texture == null || !texture.isAvailable()) {
                        device.close();
                        return;
                    }
                    camera = device;
                    createPreviewSession(device);
                }

                @Override
                public void onDisconnected(CameraDevice device) {
                    device.close();
                    if (camera == device) {
                        camera = null;
                    }
                }

                @Override
                public void onError(CameraDevice device, int error) {
                    device.close();
                    if (camera == device) {
                        camera = null;
                    }
                }
            }, cameraHandler);
        } catch (CameraAccessException | SecurityException ignored) {
            stopCamera();
        }
    }

    private void createPreviewSession(CameraDevice device) {
        if (texture == null || !texture.isAvailable()) {
            return;
        }
        SurfaceTexture surfaceTexture = texture.getSurfaceTexture();
        if (surfaceTexture == null) {
            return;
        }
        Surface surface = new Surface(surfaceTexture);
        try {
            CaptureRequest.Builder request = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            request.addTarget(surface);
            request.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
            device.createCaptureSession(
                    Collections.singletonList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession configured) {
                            if (!open || !resumed || camera != device) {
                                configured.close();
                                surface.release();
                                return;
                            }
                            session = configured;
                            try {
                                configured.setRepeatingRequest(request.build(), null, cameraHandler);
                            } catch (CameraAccessException error) {
                                configured.close();
                                session = null;
                                surface.release();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession configured) {
                            configured.close();
                            surface.release();
                        }
                    },
                    cameraHandler);
        } catch (CameraAccessException error) {
            surface.release();
            stopCamera();
        }
    }

    private void stopCamera() {
        if (session != null) {
            session.close();
            session = null;
        }
        if (camera != null) {
            camera.close();
            camera = null;
        }
        HandlerThread thread = cameraThread;
        cameraThread = null;
        cameraHandler = null;
        if (thread != null) {
            thread.quitSafely();
        }
    }

    private static String findFrontCamera(CameraManager manager) throws CameraAccessException {
        for (String id : manager.getCameraIdList()) {
            Integer facing = manager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.LENS_FACING);
            if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                return id;
            }
        }
        return null;
    }

    private static String cleanSensorText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Waiting for sensors";
        }
        return value.trim();
    }

    private int dp(int value) {
        return Math.max(1, Math.round(value * activity.getResources().getDisplayMetrics().density));
    }
}
