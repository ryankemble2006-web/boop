package com.boop.shieldhome;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/** Translate the whole existing puppet, not its gaze or eyelids. Shared eye rendering is unchanged. */
final class MusicBounceRenderer implements GLSurfaceView.Renderer {
    private final GLSurfaceView.Renderer delegate;
    private volatile float heightFraction;
    private int width = 1, height = 1;

    MusicBounceRenderer(GLSurfaceView.Renderer delegate) { this.delegate = delegate; }

    void setHeightFraction(float value) {
        heightFraction = Float.isFinite(value)
                ? Math.max(0f, Math.min(MusicBounceEnvelope.MAX_HEIGHT_FRACTION, value)) * 2f : 0f;
    }

    @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        delegate.onSurfaceCreated(gl, config);
    }

    @Override public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        delegate.onSurfaceChanged(gl, width, height);
    }

    @Override public void onDrawFrame(GL10 gl) {
        // A zero offset is the exact original viewport. Surface bounds still clip the mascot bay.
        GLES20.glViewport(0, Math.round(heightFraction * height), width, height);
        delegate.onDrawFrame(gl);
    }
}
