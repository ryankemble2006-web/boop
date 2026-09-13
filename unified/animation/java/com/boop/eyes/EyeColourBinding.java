package com.boop.eyes;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView;
import android.view.View;

/** Observes the existing hue preference. Does not replace the renderer or change motion. */
public final class EyeColourBinding implements View.OnAttachStateChangeListener {
    private final GLSurfaceView surface;
    private final CanonicalEyeRenderer renderer;
    private final SharedPreferences preferences;
    private final SharedPreferences.OnSharedPreferenceChangeListener listener;
    private boolean watching;
    private int lastHue = Integer.MIN_VALUE;
    private EyeColourBinding(GLSurfaceView surface, CanonicalEyeRenderer renderer) {
        this.surface = surface;
        this.renderer = renderer;
        preferences = surface.getContext().getSharedPreferences("boop_eyes", Context.MODE_PRIVATE);
        listener = (store, key) -> { if (key == null || "hue_degrees".equals(key)) apply(); };
    }
    /** Call once, after setRenderer. The view keeps this binding/listener strongly reachable. */
    public static void install(GLSurfaceView surface, CanonicalEyeRenderer renderer) {
        if (surface == null || renderer == null) throw new IllegalArgumentException("Eye surface required");
        EyeColourBinding binding = new EyeColourBinding(surface, renderer);
        surface.addOnAttachStateChangeListener(binding);
        binding.apply();
        if (surface.isAttachedToWindow()) binding.onViewAttachedToWindow(surface);
    }
    private void apply() {
        int hue;
        try { hue = Math.max(0, Math.min(359, preferences.getInt("hue_degrees", 190))); }
        catch (ClassCastException malformed) { hue = 190; }
        if (lastHue == hue) return;
        lastHue = hue;
        renderer.setHueRotationDegrees(hue - 190f);
        if (surface.isAttachedToWindow()) surface.requestRender();
    }
    @Override public void onViewAttachedToWindow(View view) {
        if (!watching) {
            preferences.registerOnSharedPreferenceChangeListener(listener);
            watching = true;
        }
        apply();
    }
    @Override public void onViewDetachedFromWindow(View view) {
        if (!watching) return;
        watching = false;
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
