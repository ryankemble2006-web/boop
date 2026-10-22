package com.boop.eyes;

import android.content.SharedPreferences;
import android.view.View;
import java.util.function.DoubleConsumer;

/** One lifecycle-scoped observer per existing motion owner, retained by that view. */
public final class AnimationSpeedBinding implements View.OnAttachStateChangeListener {
    private final View owner;
    private final DoubleConsumer update;
    private final SharedPreferences preferences;
    private final SharedPreferences.OnSharedPreferenceChangeListener listener;
    private boolean watching;
    private double last = Double.NaN;
    private AnimationSpeedBinding(View owner, DoubleConsumer update) {
        this.owner = owner;
        this.update = update;
        preferences = AnimationSpeedPreferences.store(owner.getContext());
        listener = (store, key) -> {
            if (key == null || AnimationSpeedPreferences.KEY.equals(key)) apply();
        };
        owner.addOnAttachStateChangeListener(this);
        apply();
        if (owner.isAttachedToWindow()) onViewAttachedToWindow(owner);
    }
    public static void install(View owner, DoubleConsumer update) {
        if (owner == null || update == null) throw new IllegalArgumentException("Motion owner required");
        new AnimationSpeedBinding(owner, update);
    }
    private void apply() {
        double speed = AnimationSpeedPreferences.load(owner.getContext());
        if (Double.compare(speed, last) == 0) return;
        last = speed;
        update.accept(speed);
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
