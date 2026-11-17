package com.boop.shieldhome;

import android.accessibilityservice.AccessibilityService;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Native Shield & TV Display Brightness Controller.
 * Provides a software dimming scrim (down to 5% night mode) and a tactile bottom-center HUD slider
 * dynamically tinted with the user's chosen highlight/accent colour.
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

    private Context context;
    private WindowManager windowManager;
    private SharedPreferences prefs;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private int currentBrightness = DEFAULT_BRIGHTNESS;
    private View dimmerScrimView;
    private View torchOverlayView;
    private FrameLayout hudRootView;
    private LinearLayout cardContainer;
    private ImageView sunIcon;
    private View progressBarView;
    private GradientDrawable progressDrawable;
    private View thumbIndicatorView;
    private TextView percentageTextView;

    private boolean isHudVisible = false;
    private boolean isHudAttached = false;
    private boolean isScrimAttached = false;
    private boolean isTorchAttached = false;
    private volatile boolean isTorchActive = false;
    private long lastInteractionTime = 0L;

    private final Runnable autoHideRunnable = this::hide;

    public static ShieldBrightnessController get(Context ctx) {
        if (instance == null) {
            synchronized (ShieldBrightnessController.class) {
                if (instance == null) {
                    instance = new ShieldBrightnessController(ctx);
                }
            }
        }
        if (ctx instanceof AccessibilityService) {
            instance.attachService((AccessibilityService) ctx);
        } else if (instance.context == null && ctx != null) {
            instance.context = ctx.getApplicationContext();
        }
        return instance;
    }

    private ShieldBrightnessController(Context ctx) {
        if (ctx instanceof AccessibilityService) {
            attachService((AccessibilityService) ctx);
        } else if (ctx != null) {
            this.context = ctx.getApplicationContext();
            try {
                this.windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
                this.prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                this.currentBrightness = prefs.getInt(KEY_BRIGHTNESS, DEFAULT_BRIGHTNESS);
            } catch (Throwable t) {
                Log.w(TAG, "Early context initialization note: " + t.getMessage());
            }
        }
    }

    public synchronized void attachService(AccessibilityService service) {
        if (service == null) return;
        this.context = service;
        this.windowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
        if (this.prefs == null) {
            this.prefs = service.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            this.currentBrightness = prefs.getInt(KEY_BRIGHTNESS, DEFAULT_BRIGHTNESS);
        }
    }

    public synchronized void detachService() {
        mainHandler.post(() -> {
            isHudVisible = false;
            mainHandler.removeCallbacks(autoHideRunnable);
            if (windowManager != null) {
                if (hudRootView != null && isHudAttached) {
                    try {
                        windowManager.removeViewImmediate(hudRootView);
                    } catch (Throwable ignored) {}
                    isHudAttached = false;
                }
                if (dimmerScrimView != null && isScrimAttached) {
                    try {
                        windowManager.removeViewImmediate(dimmerScrimView);
                    } catch (Throwable ignored) {}
                    isScrimAttached = false;
                }
                if (torchOverlayView != null && isTorchAttached) {
                    try {
                        windowManager.removeViewImmediate(torchOverlayView);
                    } catch (Throwable ignored) {}
                    isTorchAttached = false;
                }
            }
            hudRootView = null;
            cardContainer = null;
            sunIcon = null;
            progressBarView = null;
            progressDrawable = null;
            thumbIndicatorView = null;
            percentageTextView = null;
            dimmerScrimView = null;
            torchOverlayView = null;
            isTorchActive = false;
        });
    }

    public synchronized void init() {
        mainHandler.post(() -> {
            ensureDimmerScrimAttached();
            ensureTorchOverlayAttached();
            applyDimmerLevel(currentBrightness);
        });
    }

    public boolean isHudVisible() {
        if (!isHudVisible) return false;
        if (!isHudAttached || hudRootView == null) {
            isHudVisible = false;
            return false;
        }
        long now = SystemClock.elapsedRealtime();
        if (now - lastInteractionTime > AUTO_HIDE_DELAY_MS + 1000L) {
            isHudVisible = false;
            hide();
            return false;
        }
        return true;
    }

    public int getBrightness() {
        return currentBrightness;
    }

    public void toggle() {
        mainHandler.post(() -> {
            if (isHudVisible()) {
                hide();
            } else {
                show();
            }
        });
    }

    public void show() {
        mainHandler.post(() -> {
            if (context == null || windowManager == null) {
                Log.w(TAG, "Cannot show brightness HUD: context or windowManager is null");
                return;
            }
            ensureDimmerScrimAttached();
            ensureHudAttached();

            if (!isHudAttached || hudRootView == null) {
                Log.w(TAG, "HUD root view failed to attach; aborting show");
                isHudVisible = false;
                return;
            }

            lastInteractionTime = SystemClock.elapsedRealtime();
            updateHudViews(currentBrightness);

            mainHandler.removeCallbacks(autoHideRunnable);
            mainHandler.postDelayed(autoHideRunnable, AUTO_HIDE_DELAY_MS);

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
        });
    }

    public void hide() {
        mainHandler.post(() -> {
            isHudVisible = false;
            mainHandler.removeCallbacks(autoHideRunnable);
            if (hudRootView != null && isHudAttached) {
                hudRootView.animate()
                        .alpha(0f)
                        .translationY(dp(16))
                        .setDuration(160)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (hudRootView != null) {
                                    hudRootView.setVisibility(View.GONE);
                                }
                            }
                        })
                        .start();
            }
        });
    }

    public void adjust(int delta) {
        mainHandler.post(() -> {
            lastInteractionTime = SystemClock.elapsedRealtime();
            int target = Math.max(MIN_BRIGHTNESS, Math.min(MAX_BRIGHTNESS, currentBrightness + delta));
            if (target != currentBrightness || !isHudVisible()) {
                currentBrightness = target;
                if (prefs != null) {
                    prefs.edit().putInt(KEY_BRIGHTNESS, currentBrightness).apply();
                }
                applyDimmerLevel(currentBrightness);
            }
            show();
        });
    }

    public boolean isTorchActive() {
        return isTorchActive;
    }

    public void showTorch() {
        isTorchActive = true;
        mainHandler.post(() -> {
            if (context == null || windowManager == null) return;
            ensureTorchOverlayAttached();
            if (torchOverlayView == null || !isTorchAttached) {
                isTorchActive = false;
                return;
            }

            // Hide dimmer scrim so pure white shines at 100% full panel brightness
            if (dimmerScrimView != null) {
                dimmerScrimView.setVisibility(View.GONE);
            }
            hide(); // Hide brightness slider HUD if showing

            torchOverlayView.setVisibility(View.VISIBLE);
            torchOverlayView.setAlpha(1f);
            Log.i(TAG, "Screen Torch ACTIVATED: 100% white full brightness");
        });
    }

    public void hideTorch() {
        isTorchActive = false;
        mainHandler.post(() -> {
            if (torchOverlayView != null) {
                torchOverlayView.setVisibility(View.GONE);
            }
            applyDimmerLevel(currentBrightness);
            Log.i(TAG, "Screen Torch DEACTIVATED: Restored brightness to " + currentBrightness + "%");
        });
    }

    public void toggleTorch() {
        if (isTorchActive()) {
            hideTorch();
        } else {
            showTorch();
        }
    }

    private void ensureTorchOverlayAttached() {
        if (torchOverlayView != null && isTorchAttached) return;
        if (windowManager == null || context == null) return;
        try {
            if (torchOverlayView == null) {
                torchOverlayView = new View(context);
                torchOverlayView.setFocusable(false);
                torchOverlayView.setClickable(false);
                torchOverlayView.setBackgroundColor(Color.WHITE);
                torchOverlayView.setVisibility(View.GONE);
            }

            if (!isTorchAttached) {
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
                windowManager.addView(torchOverlayView, lp);
                isTorchAttached = true;
                Log.i(TAG, "Torch overlay successfully attached to WindowManager");
            }
        } catch (Throwable t) {
            Log.w(TAG, "Failed to attach torch overlay: " + t.getMessage());
            isTorchAttached = false;
        }
    }

    private void ensureDimmerScrimAttached() {
        if (dimmerScrimView != null && isScrimAttached) return;
        if (windowManager == null || context == null) return;
        try {
            if (dimmerScrimView == null) {
                dimmerScrimView = new View(context);
                dimmerScrimView.setFocusable(false);
                dimmerScrimView.setClickable(false);
            }

            if (!isScrimAttached) {
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
                isScrimAttached = true;
            }
        } catch (Throwable t) {
            Log.w(TAG, "Failed to attach dimmer scrim: " + t.getMessage());
            isScrimAttached = false;
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
        if (hudRootView != null && isHudAttached) return;
        if (windowManager == null || context == null) return;

        try {
            if (hudRootView == null) {
                int accent = BoopTvChrome.accentColor(context);

                hudRootView = new FrameLayout(context);
                hudRootView.setFocusable(false);
                hudRootView.setVisibility(View.GONE);

                // Card container
                cardContainer = new LinearLayout(context);
                cardContainer.setOrientation(LinearLayout.HORIZONTAL);
                cardContainer.setGravity(Gravity.CENTER_VERTICAL);
                cardContainer.setPadding(dp(22), dp(12), dp(22), dp(12));

                GradientDrawable bg = new GradientDrawable();
                bg.setColor(BoopTvChrome.SURFACE_CARD);
                bg.setCornerRadius(dp(16));
                bg.setStroke(dp(1), BoopTvChrome.tintSurface(BoopTvChrome.BORDER_SUBTLE, accent, 0.35f));
                cardContainer.setBackground(bg);

                // Sun Icon
                sunIcon = new ImageView(context);
                try {
                    sunIcon.setImageDrawable(context.getDrawable(R.drawable.boop_icon_sun));
                } catch (Throwable ignored) {}
                sunIcon.setColorFilter(accent);
                LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(dp(26), dp(26));
                ip.rightMargin = dp(16);
                cardContainer.addView(sunIcon, ip);

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
                progressDrawable = new GradientDrawable();
                progressDrawable.setColor(accent);
                progressDrawable.setCornerRadius(dp(4));
                progressBarView.setBackground(progressDrawable);
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

                cardContainer.addView(trackContainer, tcp);

                // Percentage Text
                percentageTextView = new TextView(context);
                percentageTextView.setTextColor(Color.WHITE);
                percentageTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                percentageTextView.setTypeface(BoopTvChrome.FONT_MEDIUM);
                percentageTextView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(dp(54), ViewGroup.LayoutParams.WRAP_CONTENT);
                tp.leftMargin = dp(14);
                cardContainer.addView(percentageTextView, tp);

                FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                cp.gravity = Gravity.CENTER;
                hudRootView.addView(cardContainer, cp);
            }

            if (!isHudAttached) {
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

                windowManager.addView(hudRootView, lp);
                isHudAttached = true;
                Log.i(TAG, "HUD overlay successfully attached to WindowManager");
            }
        } catch (Throwable t) {
            Log.w(TAG, "Failed to attach HUD overlay: " + t.getMessage());
            isHudAttached = false;
        }
    }

    private void updateHudViews(int brightness) {
        // Dynamically resolve and apply the user's chosen highlight/accent colour on every view update
        int accent = BoopTvChrome.accentColor(context);
        if (sunIcon != null) {
            sunIcon.setColorFilter(accent);
        }
        if (progressDrawable != null) {
            progressDrawable.setColor(accent);
        }
        if (cardContainer != null) {
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(BoopTvChrome.SURFACE_CARD);
            bg.setCornerRadius(dp(16));
            bg.setStroke(dp(1), BoopTvChrome.tintSurface(BoopTvChrome.BORDER_SUBTLE, accent, 0.35f));
            cardContainer.setBackground(bg);
        }

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
        if (context == null) return value * 2;
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }
}
