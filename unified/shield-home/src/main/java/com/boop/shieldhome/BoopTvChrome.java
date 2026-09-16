package com.boop.shieldhome;

import android.app.Activity;
import android.content.Context;
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
    public static final float FOCUSED_SCALE = 1.04f;
    private static final int NORMAL_FILL = Color.rgb(34, 34, 34);
    private static final long FOCUS_DURATION_MS = 120L;

    private static final Set<View> DECORATED = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<View> INSTALLED_ROOTS = Collections.newSetFromMap(new WeakHashMap<>());

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
            // A transparent foreground preserves native tracks, caret and input
            // handlers while matching the existing button's blue focus border.
            StateListDrawable outline = new StateListDrawable();
            outline.addState(new int[]{android.R.attr.state_focused},
                    filled(view.getContext(), Color.TRANSPARENT, CORNER_DP, true));
            outline.addState(new int[0],
                    filled(view.getContext(), Color.TRANSPARENT, CORNER_DP, false));
            view.setForeground(outline);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                decorateVoiceEditors(group.getChildAt(i));
            }
        }
    }

    public static int accentColor(Context context) {
        return Color.rgb(77, 184, 255);
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

    private static boolean isTelevision(Context context) {
        int uiMode = context.getResources().getConfiguration().uiMode;
        return (uiMode & Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    private static void decorateTree(View view) {
        if (view == null) return;
        if (eligible(view)) {
            synchronized (DECORATED) {
                DECORATED.add(view);
            }
            applyState((TextView) view, view.hasFocus());
        }
        if (!(view instanceof ViewGroup)) return;
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            decorateTree(group.getChildAt(i));
        }
    }

    private static boolean eligible(View view) {
        if (view == null) return false;
        if (view instanceof EditText) return false;
        if (view instanceof SeekBar) return false;
        if (view instanceof ImageView) return false;
        if (view instanceof CompoundButton) return false;
        if (!(view instanceof TextView)) return false;
        return view.isClickable() && view.isFocusable();
    }

    private static void applyState(TextView view, boolean focused) {
        if (view == null) return;
        view.setTextColor(Color.WHITE);
        view.setBackground(filled(view.getContext(), NORMAL_FILL, CORNER_DP, focused));
        view.animate()
                .scaleX(focused ? FOCUSED_SCALE : 1f)
                .scaleY(focused ? FOCUSED_SCALE : 1f)
                .setDuration(FOCUS_DURATION_MS)
                .start();
    }

    private static int dp(Context context, int value) {
        if (context == null) return value;
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
