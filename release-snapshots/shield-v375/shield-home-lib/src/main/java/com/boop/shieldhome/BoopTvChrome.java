package com.boop.shieldhome;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.KeyEvent;
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

/** Canonical BOOP focus chrome and modern OLED typography for every Android TV menu/action control. */
public final class BoopTvChrome {
    public static final int BORDER_DP = 4;
    public static final int FOCUS_STROKE_DP = 2;
    public static final int CORNER_DP = 10;
    public static final int MODERN_CORNER_DP = 14;
    public static final float FOCUSED_SCALE = 1.04f;

    public static final int SURFACE_PANEL = Color.rgb(14, 16, 20);
    public static final int SURFACE_CARD = Color.rgb(21, 24, 30);
    public static final int SURFACE_ELEVATED = Color.rgb(29, 34, 42);
    public static final int BORDER_SUBTLE = Color.rgb(42, 47, 57);
    public static final int TEXT_PRIMARY = Color.rgb(245, 247, 250);
    public static final int TEXT_SECONDARY = Color.rgb(154, 163, 176);
    public static final int TEXT_DIM = Color.rgb(108, 116, 128);

    public static final Typeface FONT_MEDIUM = Typeface.create("sans-serif-medium", Typeface.NORMAL);
    public static final Typeface FONT_REGULAR = Typeface.create("sans-serif", Typeface.NORMAL);
    public static final Typeface FONT_LIGHT = Typeface.create("sans-serif-light", Typeface.NORMAL);

    private static final int NORMAL_FILL = SURFACE_CARD;

    private record AppliedState(boolean focused, int accent, GradientDrawable background) { }
    private static final WeakHashMap<TextView, AppliedState> APPLIED_STATES = new WeakHashMap<>();
    private static final Set<View> INSTALLED_ROOTS = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<View> TEXT_ONLY_ACTIONS = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<View> CUSTOM_STYLED_VIEWS = Collections.newSetFromMap(new WeakHashMap<>());
    private static final WeakHashMap<SeekBar, Boolean> SLIDER_ACTIVE_STATES = new WeakHashMap<>();
    private static final WeakHashMap<SeekBar, ColorStateList> SLIDER_INACTIVE_THUMB_TINTS = new WeakHashMap<>();
    private static final Set<SeekBar> CONFIGURED_SLIDERS = Collections.newSetFromMap(new WeakHashMap<>());

    private BoopTvChrome() { }

    public static void markCustomStyled(View view) {
        if (view == null) return;
        synchronized (CUSTOM_STYLED_VIEWS) {
            CUSTOM_STYLED_VIEWS.add(view);
        }
    }

    private static boolean isCustomStyled(View view) {
        synchronized (CUSTOM_STYLED_VIEWS) {
            return CUSTOM_STYLED_VIEWS.contains(view);
        }
    }

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
        view.setTypeface(FONT_MEDIUM);
        view.setLetterSpacing(0.015f);
        view.setTextColor(new ColorStateList(
                new int[][] {new int[] {android.R.attr.state_focused}, new int[0]},
                new int[] {accentColor(view.getContext()), TEXT_PRIMARY}));
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
        if (view instanceof SeekBar) {
            styleSlider((SeekBar) view);
        } else if (view instanceof EditText) {
            applyEditorOutline(view);
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
                filled(view.getContext(), Color.TRANSPARENT, MODERN_CORNER_DP, true));
        outline.addState(new int[0],
                filled(view.getContext(), Color.TRANSPARENT, MODERN_CORNER_DP, false));
        view.setForeground(outline);
    }

    public static void tintSlider(SeekBar slider) {
        int accent = accentColor(slider.getContext());
        ColorStateList focusAccent = ColorStateList.valueOf(accent);
        ColorStateList trackBg = ColorStateList.valueOf(Color.rgb(46, 52, 62));
        slider.setThumbTintList(focusAccent);
        slider.setProgressTintList(focusAccent);
        slider.setProgressBackgroundTintList(trackBg);
    }

    public static int accentColor(Context context) {
        return context == null ? colorForHue(ShieldHomeStore.DEFAULT_ACCENT_HUE)
                : new ShieldHomeStore(context).accentColor();
    }

    public static int tintSurface(int baseColor, int accentColor, float ratio) {
        int r = Math.min(255, Math.round(Color.red(baseColor) * (1f - ratio) + Color.red(accentColor) * ratio));
        int g = Math.min(255, Math.round(Color.green(baseColor) * (1f - ratio) + Color.green(accentColor) * ratio));
        int b = Math.min(255, Math.round(Color.blue(baseColor) * (1f - ratio) + Color.blue(accentColor) * ratio));
        return Color.rgb(r, g, b);
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
        int accent = accentColor(context);
        if (fillColor == Color.TRANSPARENT) {
            background.setColor(Color.TRANSPARENT);
        } else if (focused) {
            background.setColor(tintSurface(SURFACE_ELEVATED, accent, 0.14f));
        } else {
            background.setColor(fillColor);
        }
        background.setCornerRadius(dp(context, cornerRadiusDp));
        if (focused) {
            background.setStroke(Math.max(2, dp(context, FOCUS_STROKE_DP)), accent);
        } else if (fillColor != Color.TRANSPARENT) {
            background.setStroke(Math.max(1, dp(context, 1)), BORDER_SUBTLE);
        }
        return background;
    }

    public static GradientDrawable cardSurface(Context context, int cornerRadiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(SURFACE_PANEL);
        d.setCornerRadius(dp(context, cornerRadiusDp));
        d.setStroke(Math.max(1, dp(context, 1)), Color.rgb(36, 40, 48));
        return d;
    }

    public static GradientDrawable sectionPanel(Context context) {
        return cardSurface(context, 16);
    }

    public static boolean isSliderActive(SeekBar slider) {
        return slider != null && Boolean.TRUE.equals(SLIDER_ACTIVE_STATES.get(slider));
    }

    public static void setSliderActive(SeekBar slider, boolean active) {
        if (slider == null) return;
        SLIDER_ACTIVE_STATES.put(slider, active);
        updateSliderChrome(slider);
    }

    private static GradientDrawable sliderOutline(Context context, int strokeWidthDp, int strokeColor) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(Color.TRANSPARENT);
        d.setCornerRadius(dp(context, MODERN_CORNER_DP));
        if (strokeWidthDp > 0) {
            d.setStroke(Math.max(1, dp(context, strokeWidthDp)), strokeColor);
        }
        return d;
    }

    public static void updateSliderChrome(SeekBar slider) {
        if (slider == null) return;
        Context context = slider.getContext();
        int accent = accentColor(context);
        boolean focused = slider.hasFocus();
        boolean active = focused && isSliderActive(slider);

        ColorStateList defaultThumbTint = SLIDER_INACTIVE_THUMB_TINTS.get(slider);
        if (defaultThumbTint == null) {
            defaultThumbTint = slider.getThumbTintList();
            if (defaultThumbTint == null) {
                defaultThumbTint = ColorStateList.valueOf(accent);
            }
            SLIDER_INACTIVE_THUMB_TINTS.put(slider, defaultThumbTint);
        }

        if (active) {
            // Selected for adjustment: bold retro focus stroke, glowing pure white thumb, subtle warm glow BEHIND track
            slider.setForeground(sliderOutline(context, FOCUS_STROKE_DP, accent));
            GradientDrawable activeBg = new GradientDrawable();
            activeBg.setColor(Color.argb(35, Color.red(accent), Color.green(accent), Color.blue(accent)));
            activeBg.setCornerRadius(dp(context, MODERN_CORNER_DP));
            slider.setBackground(activeBg);
            slider.setThumbTintList(ColorStateList.valueOf(Color.WHITE));
        } else if (focused) {
            // Highlighted (navigable): crisp 2dp accent outline, normal thumb tint, clear OLED background
            slider.setForeground(sliderOutline(context, 2, accent));
            slider.setBackground(null);
            slider.setThumbTintList(defaultThumbTint);
        } else {
            slider.setForeground(null);
            slider.setBackground(null);
            slider.setThumbTintList(defaultThumbTint);
        }
    }

    public static void styleSlider(SeekBar slider) {
        if (slider == null) return;
        if (slider.getThumbTintList() == null) {
            tintSlider(slider);
        }
        slider.setFocusable(true);
        slider.setClickable(true);
        slider.setDefaultFocusHighlightEnabled(false);
        slider.setSplitTrack(false);

        if (!CONFIGURED_SLIDERS.add(slider)) {
            updateSliderChrome(slider);
            return;
        }

        View.OnFocusChangeListener existingFocus = slider.getOnFocusChangeListener();
        slider.setOnFocusChangeListener((v, focused) -> {
            if (!focused) {
                SLIDER_ACTIVE_STATES.put(slider, false);
            }
            updateSliderChrome(slider);
            if (existingFocus != null) existingFocus.onFocusChange(v, focused);
        });

        slider.setOnClickListener(v -> setSliderActive(slider, !isSliderActive(slider)));

        slider.setOnKeyListener((v, keyCode, event) -> {
            boolean active = isSliderActive(slider);
            int action = event.getAction();

            // 1. Back or Escape key: if active, exit active mode and consume the back key
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
                if (active) {
                    if (action == KeyEvent.ACTION_UP) {
                        setSliderActive(slider, false);
                    }
                    return true;
                }
                return false;
            }

            // 2. Select / Enter / Click: toggle active mode
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                    || keyCode == KeyEvent.KEYCODE_ENTER
                    || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                if (action == KeyEvent.ACTION_UP) {
                    setSliderActive(slider, !active);
                }
                return true;
            }

            // 3. D-pad Left / Right
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (!active) {
                    // Not active: navigate focus! Do not adjust progress!
                    if (action == KeyEvent.ACTION_DOWN) {
                        int direction = (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
                                ? View.FOCUS_RIGHT : View.FOCUS_LEFT;
                        View next = slider.focusSearch(direction);
                        if (next != null && next != slider) {
                            next.requestFocus(direction);
                        }
                    }
                    return true;
                }
                // Active: pass key to AbsSeekBar so it adjusts progress with fromUser = true
                return false;
            }

            // 4. D-pad Up / Down
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (active) {
                    // Consume UP/DOWN while adjusting to avoid accidental focus jumps
                    return true;
                }
                return false;
            }

            return false;
        });

        updateSliderChrome(slider);
    }

    public static void styleInteractive(TextView view) {
        if (view == null) return;
        view.setFocusable(true);
        view.setClickable(true);
        view.setDefaultFocusHighlightEnabled(false);
        view.setStateListAnimator(null);
        view.setBackgroundTintList(null);
        view.setAllCaps(false);
        view.setTypeface(FONT_MEDIUM);
        view.setLetterSpacing(0.015f);
        view.setTextColor(Color.WHITE);
        applyButtonBackgroundAndPadding(view, filled(view.getContext(), NORMAL_FILL, MODERN_CORNER_DP, view.hasFocus()));
        view.setOnFocusChangeListener((v, focused) -> {
            if (v instanceof TextView) {
                applyState((TextView) v, focused);
            }
        });
    }

    private static void applyButtonBackgroundAndPadding(TextView view, GradientDrawable background) {
        Context ctx = view.getContext();
        boolean compact = view.getMinimumHeight() > 0 && view.getMinimumHeight() <= dp(ctx, 40);
        int minHPad = dp(ctx, compact ? 16 : 22);
        int minVPad = dp(ctx, compact ? 6 : 10);
        int leftPad = Math.max(view.getPaddingLeft(), minHPad);
        int rightPad = Math.max(view.getPaddingRight(), minHPad);
        int topPad = Math.max(view.getPaddingTop(), minVPad);
        int bottomPad = Math.max(view.getPaddingBottom(), minVPad);
        view.setBackground(background);
        view.setPadding(leftPad, topPad, rightPad, bottomPad);
    }

    public static GradientDrawable badgePill(Context context, boolean active) {
        int accent = accentColor(context);
        GradientDrawable pill = new GradientDrawable();
        pill.setCornerRadius(dp(context, 999));
        if (active) {
            pill.setColor(Color.argb(46, Color.red(accent), Color.green(accent), Color.blue(accent)));
            pill.setStroke(Math.max(1, dp(context, 1)), Color.argb(170, Color.red(accent), Color.green(accent), Color.blue(accent)));
        } else {
            pill.setColor(Color.rgb(28, 32, 38));
            pill.setStroke(Math.max(1, dp(context, 1)), Color.rgb(50, 56, 66));
        }
        return pill;
    }

    private static boolean isTelevision(Context context) {
        int uiMode = context.getResources().getConfiguration().uiMode;
        return (uiMode & Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    private static void decorateTree(View view) {
        if (view == null) return;
        if (view instanceof SeekBar) {
            styleSlider((SeekBar) view);
        }
        if (eligible(view)) {
            applyState((TextView) view, view.hasFocus());
        } else if (view instanceof TextView && !isCustomStyled(view)) {
            TextView tv = (TextView) view;
            Typeface tf = tv.getTypeface();
            if (tf == null || tf == Typeface.DEFAULT || tf == Typeface.DEFAULT_BOLD) {
                tv.setTypeface(tf != null && tf.isBold() ? FONT_MEDIUM : FONT_REGULAR);
                tv.setLetterSpacing(0.01f);
            }
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
        if (view == null || isTextOnlyAction(view) || isCustomStyled(view)) return false;
        if (view instanceof EditText) return false;
        if (view instanceof SeekBar) return false;
        if (view instanceof ImageView) return false;
        if (view instanceof CompoundButton) return false;
        if (!(view instanceof TextView)) return false;
        return view.isClickable() && view.isFocusable();
    }

    private static void applyState(TextView view, boolean focused) {
        if (view == null || isTextOnlyAction(view) || isCustomStyled(view)) return;
        int accent = accentColor(view.getContext());
        AppliedState previous = APPLIED_STATES.get(view);
        if (previous != null && previous.focused() == focused && previous.accent() == accent
                && view.getBackground() == previous.background()
                && view.getCurrentTextColor() == Color.WHITE
                && view.getScaleX() == 1f && view.getScaleY() == 1f) return;
        view.setDefaultFocusHighlightEnabled(false);
        view.setStateListAnimator(null);
        view.setBackgroundTintList(null);
        view.setAllCaps(false);
        view.setTypeface(FONT_MEDIUM);
        view.setLetterSpacing(0.015f);
        if (view.getScaleX() != 1f || view.getScaleY() != 1f) {
            view.animate().cancel();
            view.setScaleX(1f); view.setScaleY(1f);
        }
        view.setTextColor(Color.WHITE);
        GradientDrawable background = filled(view.getContext(), NORMAL_FILL, MODERN_CORNER_DP, focused);
        applyButtonBackgroundAndPadding(view, background);
        APPLIED_STATES.put(view, new AppliedState(focused, accent, background));
    }

    private static int dp(Context context, int value) {
        if (context == null) return value;
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
