package com.boop.alpha1;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.media.FaceDetector;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Size;

import java.nio.ByteBuffer;
import java.util.Arrays;

final class BoopPresencePeekController {
    interface Listener {
        void onPeekFinished(boolean personSeen);
    }

    private static final long PEEK_TIMEOUT_MS = 2_500L;
    private static final long TARGET_PIXELS = 640L * 480L;

    private final Activity activity;
    private final Listener listener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Object lock = new Object();

    private HandlerThread cameraThread;
    private Handler cameraHandler;
    private CameraDevice camera;
    private CameraCaptureSession session;
    private ImageReader reader;
    private boolean running;

    private final Runnable timeout = () -> finish(false, true);

    BoopPresencePeekController(Activity activity, Listener listener) {
        this.activity = activity;
        this.listener = listener;
    }

    boolean peek() {
        if (activity.checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        synchronized (lock) {
            if (running) {
                return true;
            }
            running = true;
            cameraThread = new HandlerThread("boop-presence-peek");
            cameraThread.start();
            cameraHandler = new Handler(cameraThread.getLooper());
        }
        mainHandler.postDelayed(timeout, PEEK_TIMEOUT_MS);
        cameraHandler.post(this::openCamera);
        return true;
    }

    void cancel() {
        finish(false, false);
    }

    void shutdown() {
        finish(false, false);
    }

    private void openCamera() {
        try {
            CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
            String cameraId = findFrontCamera(manager);
            if (cameraId == null) {
                finish(false, true);
                return;
            }
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            Size captureSize = chooseCaptureSize(characteristics);
            if (captureSize == null) {
                finish(false, true);
                return;
            }
            ImageReader created = ImageReader.newInstance(
                    captureSize.getWidth(),
                    captureSize.getHeight(),
                    ImageFormat.JPEG,
                    2);
            created.setOnImageAvailableListener(this::onImageAvailable, cameraHandler);
            synchronized (lock) {
                if (!running) {
                    created.close();
                    return;
                }
                reader = created;
            }
            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice device) {
                    synchronized (lock) {
                        if (!running) {
                            device.close();
                            return;
                        }
                        camera = device;
                    }
                    createCaptureSession(device);
                }

                @Override
                public void onDisconnected(CameraDevice device) {
                    device.close();
                    finish(false, true);
                }

                @Override
                public void onError(CameraDevice device, int error) {
                    device.close();
                    finish(false, true);
                }
            }, cameraHandler);
        } catch (CameraAccessException | SecurityException error) {
            finish(false, true);
        }
    }

    private void createCaptureSession(CameraDevice device) {
        ImageReader localReader;
        synchronized (lock) {
            localReader = reader;
        }
        if (localReader == null) {
            finish(false, true);
            return;
        }
        try {
            device.createCaptureSession(
                    Arrays.asList(localReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession configured) {
                            synchronized (lock) {
                                if (!running) {
                                    configured.close();
                                    return;
                                }
                                session = configured;
                            }
                            captureStill(device, configured, localReader);
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession configured) {
                            configured.close();
                            finish(false, true);
                        }
                    },
                    cameraHandler);
        } catch (CameraAccessException error) {
            finish(false, true);
        }
    }

    private void captureStill(
            CameraDevice device,
            CameraCaptureSession configured,
            ImageReader localReader) {
        try {
            CaptureRequest.Builder request = device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            request.addTarget(localReader.getSurface());
            request.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
            configured.capture(request.build(), new CaptureCallback() {
                @Override
                public void onCaptureCompleted(
                        CameraCaptureSession session,
                        CaptureRequest request,
                        TotalCaptureResult result) {
                    // ImageReader callback owns completion.
                }
            }, cameraHandler);
        } catch (CameraAccessException error) {
            finish(false, true);
        }
    }

    private void onImageAvailable(ImageReader source) {
        Image image = null;
        boolean personSeen = false;
        try {
            image = source.acquireLatestImage();
            if (image == null) {
                return;
            }
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap != null) {
                try {
                    personSeen = hasFaceAtAnyRotation(bitmap);
                } finally {
                    bitmap.recycle();
                }
            }
        } catch (RuntimeException ignored) {
            personSeen = false;
        } finally {
            if (image != null) {
                image.close();
            }
            finish(personSeen, true);
        }
    }

    private static boolean hasFaceAtAnyRotation(Bitmap source) {
        int[] rotations = {0, 90, 180, 270};
        for (int rotation : rotations) {
            Bitmap rotated = source;
            if (rotation != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);
                rotated = Bitmap.createBitmap(
                        source, 0, 0, source.getWidth(), source.getHeight(), matrix, false);
            }
            try {
                int width = rotated.getWidth() & ~1;
                int height = rotated.getHeight();
                if (width < 2 || height < 2) {
                    continue;
                }
                Bitmap even = rotated;
                if (width != rotated.getWidth()) {
                    even = Bitmap.createBitmap(rotated, 0, 0, width, height);
                }
                try {
                    Bitmap rgb565 = even.copy(Bitmap.Config.RGB_565, false);
                    if (rgb565 == null) {
                        continue;
                    }
                    try {
                        FaceDetector detector = new FaceDetector(
                                rgb565.getWidth(), rgb565.getHeight(), 1);
                        FaceDetector.Face[] faces = new FaceDetector.Face[1];
                        if (detector.findFaces(rgb565, faces) > 0) {
                            return true;
                        }
                    } finally {
                        rgb565.recycle();
                    }
                } finally {
                    if (even != rotated) {
                        even.recycle();
                    }
                }
            } finally {
                if (rotated != source) {
                    rotated.recycle();
                }
            }
        }
        return false;
    }

    private static String findFrontCamera(CameraManager manager) throws CameraAccessException {
        for (String id : manager.getCameraIdList()) {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
            Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                return id;
            }
        }
        return null;
    }

    private static Size chooseCaptureSize(CameraCharacteristics characteristics) {
        android.hardware.camera2.params.StreamConfigurationMap map =
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map == null) {
            return null;
        }
        Size[] sizes = map.getOutputSizes(ImageFormat.JPEG);
        if (sizes == null || sizes.length == 0) {
            return null;
        }
        Size best = sizes[0];
        long bestDistance = Math.abs((long) best.getWidth() * best.getHeight() - TARGET_PIXELS);
        for (Size size : sizes) {
            long distance = Math.abs((long) size.getWidth() * size.getHeight() - TARGET_PIXELS);
            if (distance < bestDistance) {
                best = size;
                bestDistance = distance;
            }
        }
        return best;
    }

    private void finish(boolean personSeen, boolean notify) {
        final HandlerThread thread;
        synchronized (lock) {
            if (!running && cameraThread == null) {
                return;
            }
            running = false;
            if (session != null) {
                session.close();
                session = null;
            }
            if (camera != null) {
                camera.close();
                camera = null;
            }
            if (reader != null) {
                reader.close();
                reader = null;
            }
            thread = cameraThread;
            cameraThread = null;
            cameraHandler = null;
        }
        mainHandler.removeCallbacks(timeout);
        if (thread != null) {
            thread.quitSafely();
        }
        if (notify && listener != null) {
            mainHandler.post(() -> listener.onPeekFinished(personSeen));
        }
    }
}
