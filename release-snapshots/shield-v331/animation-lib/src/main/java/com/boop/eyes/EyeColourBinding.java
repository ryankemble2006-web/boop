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
    private Integer focusHue;
    private int lastHue = Integer.MIN_VALUE, lastFelt = Integer.MIN_VALUE;
    private EyeColourBinding(GLSurfaceView surface, CanonicalEyeRenderer renderer) {
        this.surface = surface;
        this.renderer = renderer;
        preferences = surface.getContext().getSharedPreferences("boop_eyes", Context.MODE_PRIVATE);
        listener = (store, key) -> { if (key == null || "hue_degrees".equals(key) || FeltColourPreferences.KEY.equals(key)) apply(); };
    }
    /** Call once, after setRenderer. The view keeps this binding/listener strongly reachable. */
    public static EyeColourBinding install(GLSurfaceView surface, CanonicalEyeRenderer renderer) {
        if (surface == null || renderer == null) throw new IllegalArgumentException("Eye surface required");
        EyeColourBinding binding = new EyeColourBinding(surface, renderer);
        surface.addOnAttachStateChangeListener(binding);
        binding.apply();
        if (surface.isAttachedToWindow()) binding.onViewAttachedToWindow(surface);
        return binding;
    }
    /** A local focus preview: never writes the user's saved appearance preferences. */
    public void setFocusColour(Integer colour) {
        if (colour == null) focusHue = null;
        else {
            float[] hsv = new float[3];
            android.graphics.Color.colorToHSV(colour, hsv);
            focusHue = Math.round(hsv[0]) % 360;
        }
        apply();
    }
    private void apply() {
        int hue;
        try { hue = Math.max(0, Math.min(359, preferences.getInt("hue_degrees", 190))); }
        catch (ClassCastException malformed) { hue = 190; }
        if (focusHue != null) hue = focusHue;
        int felt=FeltColourPreferences.load(surface.getContext());
        if (lastHue == hue && lastFelt == felt) return;
        lastFelt=felt;
        renderer.setFeltColour(felt);
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
        setFocusColour(null);
        if (!watching) return;
        watching = false;
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
