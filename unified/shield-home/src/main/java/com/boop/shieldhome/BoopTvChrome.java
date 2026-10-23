package com.boop.shieldhome;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/** Canonical BOOP focus chrome for every Android TV menu/action control. */
public final class BoopTvChrome {
    public static final int BORDER_DP = 4;
    public static final int CORNER_DP = 10;
    public static final float FOCUSED_SCALE = 1f;
    private static final int NORMAL_FILL = Color.rgb(34, 34, 34);

    private record AppliedState(boolean focused, int accent, GradientDrawable background) { }
    private static final WeakHashMap<TextView, AppliedState> APPLIED_STATES = new WeakHashMap<>();
    private static final Set<View> INSTALLED_ROOTS = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<View> TEXT_ONLY_ACTIONS = Collections.newSetFromMap(new WeakHashMap<>());

    private BoopTvChrome() { }

    public static void install(Activity activity) {
        if (activity == null || !isTelevision(activity)) return;
        View root = activity.findViewById(android.R.id.content);
        if (root == null) return;
        decorateTree(root);
        synchronized (INSTALLED_ROOTS) {
            if (!INSTALLED_ROOTS.add(root)) return;
        }
        ViewTreeObserver observer = root.getViewTreeObserver();
        observer.addOnGlobalLayoutListener((ViewTreeObserver.OnGlobalLayoutListener) () -> decorateTree(root));
        observer.addOnGlobalFocusChangeListener((ViewTreeObserver.OnGlobalFocusChangeListener) (oldFocus, newFocus) -> {
            if (eligible(oldFocus)) oldFocus.post(() -> applyState((TextView) oldFocus, false));
            if (eligible(newFocus)) newFocus.post(() -> applyState((TextView) newFocus, true));
        });
    }

    /** Opt in before attachment: text colour alone owns this action's focus. */
    public static void useTextOnlyFocus(TextView view) {
        if (view == null) return;
        synchronized (TEXT_ONLY_ACTIONS) {
            TEXT_ONLY_ACTIONS.add(view);
        }
        view.setBackground(null);
        view.setDefaultFocusHighlightEnabled(false);
        view.setTextColor(new ColorStateList(
                new int[][] {new int[] {android.R.attr.state_focused}, new int[0]},
                new int[] {accentColor(view.getContext()), Color.WHITE}));
    }

    private static boolean isTextOnlyAction(View view) {
        synchronized (TEXT_ONLY_ACTIONS) {
            return TEXT_ONLY_ACTIONS.contains(view);
        }
    }

    /** Voice's clickable panel blocks touch-through but must not own D-pad focus. */
    public static void prepareVoiceSettings(
            ViewGroup scroll, ViewGroup content, View initialFocus) {
        if (scroll == null || content == null || initialFocus == null
                || !isTelevision(content.getContext())) return;
        prepareVoiceContainer(scroll);
        prepareVoiceContainer(content);
        decorateVoiceEditors(content);
        initialFocus.setFocusableInTouchMode(true);
        initialFocus.post(() -> {
            if (content.isAttachedToWindow() && initialFocus.isShown() && initialFocus.isEnabled()) {
                initialFocus.requestFocus();
            }
        });
    }

    private static void prepareVoiceContainer(ViewGroup group) {
        group.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        group.setFocusableInTouchMode(false);
        group.setFocusable(false);
    }

    private static void decorateVoiceEditors(View view) {
        if (view instanceof SeekBar || view instanceof EditText) {
            applyEditorOutline(view);
        }
        if (view instanceof SeekBar) {
            tintSlider((SeekBar) view);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                decorateVoiceEditors(group.getChildAt(i));
            }
        }
    }

    private static void applyEditorOutline(View view) {
        StateListDrawable outline = new StateListDrawable();
        outline.addState(new int[]{android.R.attr.state_focused},
                filled(view.getContext(), Color.TRANSPARENT, CORNER_DP, true));
        outline.addState(new int[0],
                filled(view.getContext(), Color.TRANSPARENT, CORNER_DP, false));
        view.setForeground(outline);
    }

    private static void tintSlider(SeekBar slider) {
        int blue = accentColor(slider.getContext());
        ColorStateList focusAccent = ColorStateList.valueOf(blue);
        slider.setThumbTintList(focusAccent);
        slider.setProgressTintList(focusAccent);
        slider.setProgressBackgroundTintList(focusAccent);
    }

    public static int accentColor(Context context) {
        return context == null ? colorForHue(ShieldHomeStore.DEFAULT_ACCENT_HUE)
                : new ShieldHomeStore(context).accentColor();
    }

    public static int colorForHue(int hue) {
        int stable = Math.max(0, Math.min(359, hue));
        if (stable == ShieldHomeStore.DEFAULT_ACCENT_HUE) {
            return Color.rgb(77, 184, 255);
        }
        return Color.HSVToColor(new float[] {stable, 0.6980392f, 1f});
    }

    public static GradientDrawable filled(
            Context context,
            int fillColor,
            int cornerRadiusDp,
            boolean focused) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(fillColor);
        background.setCornerRadius(dp(context, cornerRadiusDp));
        if (focused) {
            background.setStroke(dp(context, BORDER_DP), accentColor(context));
        }
        return background;
    }

    public static boolean isTelevision(Context context) {
        int uiMode = context.getResources().getConfiguration().uiMode;
        return (uiMode & Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    private static void decorateTree(View view) {
        if (view == null) return;
        if (view instanceof SeekBar) {
            tintSlider((SeekBar) view);
        }
        if (eligible(view)) {
            applyState((TextView) view, view.hasFocus());
        }
        if (!(view instanceof ViewGroup)) return;
        ViewGroup group = (ViewGroup) view;
        if (group instanceof android.widget.ScrollView || group instanceof android.widget.HorizontalScrollView)
            group.setOverScrollMode(View.OVER_SCROLL_NEVER);
        for (int i = 0; i < group.getChildCount(); i++) {
            decorateTree(group.getChildAt(i));
        }
    }

    private static boolean eligible(View view) {
        if (view == null || isTextOnlyAction(view)) return false;
        if (view instanceof EditText) return false;
        if (view instanceof SeekBar) return false;
        if (view instanceof ImageView) return false;
        if (view instanceof CompoundButton) return false;
        if (!(view instanceof TextView)) return false;
        return view.isClickable() && view.isFocusable();
    }

    private static void applyState(TextView view, boolean focused) {
        if (view == null || isTextOnlyAction(view)) return;
        int accent = accentColor(view.getContext());
        AppliedState previous = APPLIED_STATES.get(view);
        // Global layout runs during scrolling. Unchanged chrome must not request
        // another layout or restart a property animation on every frame.
        if (previous != null && previous.focused() == focused && previous.accent() == accent
                && view.getBackground() == previous.background()
                && view.getCurrentTextColor() == Color.WHITE
                && view.getScaleX() == 1f && view.getScaleY() == 1f) return;
        view.setDefaultFocusHighlightEnabled(false);
        view.setStateListAnimator(null);
        view.setBackgroundTintList(null);
        if (view.getScaleX() != 1f || view.getScaleY() != 1f) {
            view.animate().cancel();
            view.setScaleX(1f); view.setScaleY(1f);
        }
        view.setTextColor(Color.WHITE);
        GradientDrawable background = filled(view.getContext(), NORMAL_FILL, CORNER_DP, focused);
        view.setBackground(background);
        APPLIED_STATES.put(view, new AppliedState(focused, accent, background));
    }

    private static int dp(Context context, int value) {
        if (context == null) return value;
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    public static int computeTargetDensity(Context context) {
        if (context == null) return 256;
        android.content.res.Resources res = context.getResources();
        if (res == null) return 256;
        android.util.DisplayMetrics dm = res.getDisplayMetrics();
        int height = Math.min(dm.widthPixels, dm.heightPixels);
        if (height <= 0) height = 1080;
        return Math.max(1, Math.round(height * (160f / 675f)));
    }

    public static void applyShieldMetrics(Context context) {
        if (context == null) return;
        applyToResources(context.getResources(), context);
        Context app = context.getApplicationContext();
        if (app != null && app != context) {
            applyToResources(app.getResources(), context);
        }
    }

    private static void applyToResources(android.content.res.Resources res, Context context) {
        if (res == null) return;
        int targetDpi = computeTargetDensity(context);
        float targetDensity = targetDpi / 160f;
        Configuration config = res.getConfiguration();
        android.util.DisplayMetrics dm = res.getDisplayMetrics();
        float fontScale = config.fontScale > 0 ? config.fontScale : 1.0f;
        float targetScaledDensity = targetDensity * fontScale;

        boolean configChanged = config.densityDpi != targetDpi;
        boolean metricsChanged = dm.densityDpi != targetDpi
                || Math.abs(dm.density - targetDensity) > 0.001f
                || Math.abs(dm.scaledDensity - targetScaledDensity) > 0.001f;

        if (configChanged || metricsChanged) {
            config.densityDpi = targetDpi;
            dm.densityDpi = targetDpi;
            dm.density = targetDensity;
            dm.scaledDensity = targetScaledDensity;
            try {
                res.updateConfiguration(config, dm);
            } catch (Throwable ignored) {}
        }
    }

}
