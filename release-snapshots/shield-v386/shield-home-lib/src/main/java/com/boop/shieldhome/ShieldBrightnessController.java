package com.boop.shieldhome;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

/**
 * Native Shield & TV Display Brightness Controller.
 * Provides a software dimming scrim (down to 5% night mode) and a tactile bottom-center HUD slider
 * mapped directly to hardware remote actions (such as long-press Netflix button).
 */
public final class ShieldBrightnessController {
    private static final String TAG = "BOOPBrightness";
    private static final String PREF_NAME = "boop_brightness_prefs";
    private static final String KEY_BRIGHTNESS = "screen_brightness";
    private static final int DEFAULT_BRIGHTNESS = 100;
    private static final int MIN_BRIGHTNESS = 5;
    private static final int MAX_BRIGHTNESS = 100;
    private static final long AUTO_HIDE_DELAY_MS = 3500L;

    private static volatile ShieldBrightnessController instance;

    private final Context context;
    private final WindowManager windowManager;
    private final SharedPreferences prefs;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private int currentBrightness;
    private View dimmerScrimView;
    private FrameLayout hudRootView;
    private View progressBarView;
    private View thumbIndicatorView;
    private TextView percentageTextView;
    private boolean isHudVisible = false;

    private final Runnable autoHideRunnable = this::hide;

    public static ShieldBrightnessController get(Context ctx) {
        if (instance == null) {
            synchronized (ShieldBrightnessController.class) {
                if (instance == null) {
                    instance = new ShieldBrightnessController(ctx.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private ShieldBrightnessController(Context ctx) {
        this.context = ctx;
        this.windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        this.prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.currentBrightness = prefs.getInt(KEY_BRIGHTNESS, DEFAULT_BRIGHTNESS);
    }

    public synchronized void init() {
        mainHandler.post(() -> {
            ensureDimmerScrimAttached();
            applyDimmerLevel(currentBrightness);
        });
    }

    public boolean isHudVisible() {
        return isHudVisible;
    }

    public int getBrightness() {
        return currentBrightness;
    }

    public void toggle() {
        mainHandler.post(() -> {
            if (isHudVisible) {
                hide();
            } else {
                show();
            }
        });
    }

    public void show() {
        mainHandler.post(() -> {
            ensureDimmerScrimAttached();
            ensureHudAttached();
            updateHudViews(currentBrightness);

            hudRootView.removeCallbacks(autoHideRunnable);
            hudRootView.postDelayed(autoHideRunnable, AUTO_HIDE_DELAY_MS);

            if (!isHudVisible) {
                isHudVisible = true;
                hudRootView.setVisibility(View.VISIBLE);
                hudRootView.setAlpha(0f);
                hudRootView.setTranslationY(dp(16));
                hudRootView.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(180)
                        .setListener(null)
                        .start();
            }
        });
    }

    public void hide() {
        mainHandler.post(() -> {
            if (!isHudVisible || hudRootView == null) return;
            isHudVisible = false;
            hudRootView.removeCallbacks(autoHideRunnable);
            hudRootView.animate()
                    .alpha(0f)
                    .translationY(dp(16))
                    .setDuration(160)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (!isHudVisible && hudRootView != null) {
                                hudRootView.setVisibility(View.GONE);
                            }
                        }
                    })
                    .start();
        });
    }

    public void adjust(int delta) {
        mainHandler.post(() -> {
            int target = Math.max(MIN_BRIGHTNESS, Math.min(MAX_BRIGHTNESS, currentBrightness + delta));
            if (target != currentBrightness || !isHudVisible) {
                currentBrightness = target;
                prefs.edit().putInt(KEY_BRIGHTNESS, currentBrightness).apply();
                applyDimmerLevel(currentBrightness);
            }
            show();
        });
    }

    private void ensureDimmerScrimAttached() {
        if (dimmerScrimView != null) return;
        try {
            dimmerScrimView = new View(context);
            dimmerScrimView.setFocusable(false);
            dimmerScrimView.setClickable(false);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
            );
            lp.gravity = Gravity.FILL;
            windowManager.addView(dimmerScrimView, lp);
        } catch (Exception e) {
            // Window manager overlay fallback
        }
    }

    private void applyDimmerLevel(int brightness) {
        if (dimmerScrimView == null) return;
        if (brightness >= 100) {
            dimmerScrimView.setVisibility(View.GONE);
        } else {
            dimmerScrimView.setVisibility(View.VISIBLE);
            // Linear mapping: 99% down to 5% -> alpha 2 down to 215 (84% maximum scrim darkness)
            int alpha = (int) Math.round((100 - brightness) * (215.0 / 95.0));
            alpha = Math.max(0, Math.min(225, alpha));
            dimmerScrimView.setBackgroundColor(Color.argb(alpha, 0, 0, 0));
        }
    }

    private void ensureHudAttached() {
        if (hudRootView != null) return;
        int accent = BoopTvChrome.accentColor(context);

        hudRootView = new FrameLayout(context);
        hudRootView.setFocusable(false);
        hudRootView.setVisibility(View.GONE);

        // Card container
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(22), dp(12), dp(22), dp(12));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(BoopTvChrome.SURFACE_CARD);
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1), BoopTvChrome.BORDER_SUBTLE);
        card.setBackground(bg);

        // Sun Icon
        ImageView sunIcon = new ImageView(context);
        try {
            sunIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.boop_icon_sun));
        } catch (Throwable ignored) {}
        sunIcon.setColorFilter(accent);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(dp(26), dp(26));
        ip.rightMargin = dp(16);
        card.addView(sunIcon, ip);

        // Slider Track Frame
        int trackWidthDp = 300;
        int trackHeightDp = 8;
        FrameLayout trackContainer = new FrameLayout(context);
        LinearLayout.LayoutParams tcp = new LinearLayout.LayoutParams(dp(trackWidthDp), dp(24));
        tcp.gravity = Gravity.CENTER_VERTICAL;

        View trackBg = new View(context);
        GradientDrawable tbg = new GradientDrawable();
        tbg.setColor(Color.rgb(38, 43, 53));
        tbg.setCornerRadius(dp(4));
        trackBg.setBackground(tbg);
        FrameLayout.LayoutParams tbgP = new FrameLayout.LayoutParams(dp(trackWidthDp), dp(trackHeightDp));
        tbgP.gravity = Gravity.CENTER_VERTICAL;
        trackContainer.addView(trackBg, tbgP);

        progressBarView = new View(context);
        GradientDrawable pbg = new GradientDrawable();
        pbg.setColor(accent);
        pbg.setCornerRadius(dp(4));
        progressBarView.setBackground(pbg);
        FrameLayout.LayoutParams pbp = new FrameLayout.LayoutParams(0, dp(trackHeightDp));
        pbp.gravity = Gravity.CENTER_VERTICAL;
        trackContainer.addView(progressBarView, pbp);

        thumbIndicatorView = new View(context);
        GradientDrawable thBg = new GradientDrawable();
        thBg.setShape(GradientDrawable.OVAL);
        thBg.setColor(Color.WHITE);
        thumbIndicatorView.setBackground(thBg);
        FrameLayout.LayoutParams thp = new FrameLayout.LayoutParams(dp(16), dp(16));
        thp.gravity = Gravity.CENTER_VERTICAL;
        trackContainer.addView(thumbIndicatorView, thp);

        card.addView(trackContainer, tcp);

        // Percentage Text
        percentageTextView = new TextView(context);
        percentageTextView.setTextColor(Color.WHITE);
        percentageTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        percentageTextView.setTypeface(BoopTvChrome.FONT_MEDIUM);
        percentageTextView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(dp(54), ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.leftMargin = dp(14);
        card.addView(percentageTextView, tp);

        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cp.gravity = Gravity.CENTER;
        hudRootView.addView(card, cp);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );
        lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        lp.y = dp(70);

        try {
            windowManager.addView(hudRootView, lp);
        } catch (Exception e) {
            // Overlay window fallback
        }
    }

    private void updateHudViews(int brightness) {
        if (percentageTextView != null) {
            percentageTextView.setText(brightness + "%");
        }
        if (progressBarView != null && thumbIndicatorView != null) {
            int trackWidthPx = dp(300);
            float fraction = (float) (brightness - MIN_BRIGHTNESS) / (float) (MAX_BRIGHTNESS - MIN_BRIGHTNESS);
            fraction = Math.max(0f, Math.min(1f, fraction));
            int fillWidth = (int) (trackWidthPx * fraction);

            ViewGroup.LayoutParams lp = progressBarView.getLayoutParams();
            if (lp != null) {
                lp.width = fillWidth;
                progressBarView.setLayoutParams(lp);
            }

            int thumbOffset = Math.max(0, Math.min(trackWidthPx - dp(16), fillWidth - dp(8)));
            thumbIndicatorView.setTranslationX(thumbOffset);
        }
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }
}
