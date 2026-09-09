package com.boop.alpha1;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.boop.shieldoverlay.BoopShieldMotionBridge;

/**
 * Test-only Shield animation surface. Real Shield motion math is copied into the
 * lab at materialization time; saved 2.5D ideas remain isolated prototypes here.
 * The production Shield renderer is not changed by this class.
 */
final class BoopShieldPreviewView extends FrameLayout {
    enum Mode {
        SHIELD_GROOVE,
        SHIELD_TRACK_CHANGE,
        SHIELD_PAUSE_SETTLE,
        SHIELD_CINEMA_HAND,
        WIP_OPEN_PALMS,
        WIP_WAVE,
        WIP_POINT,
        WIP_GRIP,
        WIP_EARCUP_ADJUST,
        WIP_ONE_CUP_LISTEN,
        WIP_GAZE_DEPTH,
        WIP_HEADPHONE_RECOIL
    }

    private final PuppetLayer layer;
    private final TextView label;
    private ValueAnimator animator;

    BoopShieldPreviewView(Context context, Mode initialMode) {
        super(context);
        setBackgroundColor(Color.BLACK);
        setClipChildren(false);
        setClipToPadding(false);

        layer = new PuppetLayer(context);
        addView(layer, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        label = new TextView(context);
        label.setTextColor(Color.LTGRAY);
        label.setTextSize(16f);
        label.setGravity(Gravity.CENTER);
        label.setPadding(dp(18), dp(14), dp(18), dp(14));
        FrameLayout.LayoutParams labelParams = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.TOP);
        addView(label, labelParams);

        post(() -> play(initialMode));
    }

    void play(Mode mode) {
        stop();
        layer.setMode(mode);
        label.setText(labelFor(mode));

        ValueAnimator running = ValueAnimator.ofFloat(0f, 1f);
        animator = running;
        running.setDuration(durationFor(mode));
        running.setRepeatCount(ValueAnimator.INFINITE);
        running.setRepeatMode(ValueAnimator.RESTART);
        running.setInterpolator(new LinearInterpolator());
        running.addUpdateListener(frame -> {
            if (animator != frame) return;
            layer.setProgress((float) frame.getAnimatedValue());
        });
        running.start();
    }

    void stop() {
        ValueAnimator running = animator;
        animator = null;
        if (running != null) running.cancel();
        layer.setProgress(0f);
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }

    private static long durationFor(Mode mode) {
        switch (mode) {
            case SHIELD_GROOVE:
                return BoopShieldMotionBridge.groovePeriodMs();
            case SHIELD_TRACK_CHANGE:
                return BoopShieldMotionBridge.trackChangeDurationMs() + 650L;
            case SHIELD_PAUSE_SETTLE:
                return BoopShieldMotionBridge.settleDurationMs() + 850L;
            case SHIELD_CINEMA_HAND:
                return BoopShieldMotionBridge.cinemaPeriodMs();
            case WIP_OPEN_PALMS:
                return 3000L;
            case WIP_WAVE:
                return 2400L;
            case WIP_POINT:
                return 2200L;
            case WIP_GRIP:
                return 2800L;
            case WIP_EARCUP_ADJUST:
                return 3200L;
            case WIP_ONE_CUP_LISTEN:
                return 3600L;
            case WIP_GAZE_DEPTH:
                return 4200L;
            case WIP_HEADPHONE_RECOIL:
                return 2600L;
            default:
                return 3000L;
        }
    }

    private static String labelFor(Mode mode) {
        switch (mode) {
            case SHIELD_GROOVE: return "Shield runtime • music groove";
            case SHIELD_TRACK_CHANGE: return "Shield runtime • track-change perk";
            case SHIELD_PAUSE_SETTLE: return "Shield runtime • pause settle";
            case SHIELD_CINEMA_HAND: return "Shield runtime • cinema hand";
            case WIP_OPEN_PALMS: return "Shield WIP • open palms";
            case WIP_WAVE: return "Shield WIP • wave";
            case WIP_POINT: return "Shield WIP • point motion study";
            case WIP_GRIP: return "Shield WIP • curved grip";
            case WIP_EARCUP_ADJUST: return "Shield WIP • earcup adjust";
            case WIP_ONE_CUP_LISTEN: return "Shield WIP • one-cup listen / settle";
            case WIP_GAZE_DEPTH: return "Shield WIP • gaze-led 2.5D depth";
            case WIP_HEADPHONE_RECOIL: return "Shield WIP • headphone lag / recoil";
            default: return "Shield animation";
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class PuppetLayer extends View {
        private static final double TWO_PI = Math.PI * 2.0;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private final Bitmap headphones;
        private final Bitmap hands;
        private final Rect headphoneSource;
        private final Rect leftHandSource;
        private final Rect rightHandSource;
        private Mode mode = Mode.SHIELD_GROOVE;
        private float progress;

        PuppetLayer(Context context) {
            super(context);
            setBackgroundColor(Color.BLACK);
            headphones = BitmapFactory.decodeResource(getResources(), R.drawable.boop_headphones);
            hands = BitmapFactory.decodeResource(getResources(), R.drawable.boop_notification_hands);
            headphoneSource = headphones == null
                    ? new Rect() : new Rect(0, 0, headphones.getWidth(), headphones.getHeight());
            if (hands == null) {
                leftHandSource = new Rect();
                rightHandSource = new Rect();
            } else {
                int split = hands.getWidth() / 2;
                leftHandSource = new Rect(0, 0, split, hands.getHeight());
                rightHandSource = new Rect(split, 0, hands.getWidth(), hands.getHeight());
            }
        }

        void setMode(Mode mode) {
            this.mode = mode == null ? Mode.SHIELD_GROOVE : mode;
            progress = 0f;
            invalidate();
        }

        void setProgress(float progress) {
            this.progress = clamp(progress);
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(Color.BLACK);
            if (getWidth() <= 0 || getHeight() <= 0) return;

            Motion motion = motionFor(mode, progress);
            float unit = getWidth() / 1080f;
            drawHeadphones(canvas, motion, unit);
            drawHands(canvas, motion, unit);
        }

        private void drawHeadphones(Canvas canvas, Motion motion, float unit) {
            if (headphones == null) return;
            float width = getWidth() * 0.84f;
            float height = width * headphones.getHeight() / (float) headphones.getWidth();
            float maxHeight = getHeight() * 0.48f;
            if (height > maxHeight) {
                float scaleDown = maxHeight / height;
                width *= scaleDown;
                height *= scaleDown;
            }
            float centerX = getWidth() / 2f + motion.headX * unit;
            float centerY = getHeight() * 0.36f + motion.headY * unit;
            int save = canvas.save();
            canvas.translate(centerX, centerY);
            canvas.rotate(motion.headRotation);
            canvas.scale(motion.headScale, motion.headScale);
            canvas.drawBitmap(headphones, headphoneSource,
                    new RectF(-width / 2f, -height / 2f, width / 2f, height / 2f), paint);
            canvas.restoreToCount(save);
        }

        private void drawHands(Canvas canvas, Motion motion, float unit) {
            if (hands == null) return;
            float pairWidth = getWidth() * 0.96f;
            float pairHeight = pairWidth * hands.getHeight() / (float) hands.getWidth();
            float maxHeight = getHeight() * 0.39f;
            if (pairHeight > maxHeight) {
                float scaleDown = maxHeight / pairHeight;
                pairWidth *= scaleDown;
                pairHeight *= scaleDown;
            }
            float halfWidth = pairWidth / 2f;
            float baseY = getHeight() * 0.63f;
            drawHand(canvas, leftHandSource,
                    getWidth() / 2f - pairWidth / 4f + motion.leftX * unit,
                    baseY + motion.leftY * unit,
                    halfWidth, pairHeight,
                    motion.leftRotation, motion.leftScale, motion.leftAlpha);
            drawHand(canvas, rightHandSource,
                    getWidth() / 2f + pairWidth / 4f + motion.rightX * unit,
                    baseY + motion.rightY * unit,
                    halfWidth, pairHeight,
                    motion.rightRotation, motion.rightScale, motion.rightAlpha);
        }

        private void drawHand(
                Canvas canvas,
                Rect source,
                float centerX,
                float centerY,
                float width,
                float height,
                float rotation,
                float scale,
                float alpha) {
            int save = canvas.save();
            canvas.translate(centerX, centerY);
            canvas.rotate(rotation);
            canvas.scale(scale, scale);
            int oldAlpha = paint.getAlpha();
            paint.setAlpha(Math.round(255f * clamp(alpha)));
            canvas.drawBitmap(hands, source,
                    new RectF(-width / 2f, -height / 2f, width / 2f, height / 2f), paint);
            paint.setAlpha(oldAlpha);
            canvas.restoreToCount(save);
        }

        private static Motion motionFor(Mode mode, float p) {
            Motion m = new Motion();
            double phase = TWO_PI * p;
            switch (mode) {
                case SHIELD_GROOVE: {
                    float[] pose = BoopShieldMotionBridge.groove(
                            Math.round(p * BoopShieldMotionBridge.groovePeriodMs()));
                    m.headX = pose[0];
                    m.headY = pose[1];
                    m.headRotation = pose[2];
                    m.leftX = -2.5f * (float) Math.sin(phase + 0.4);
                    m.rightX = 2.5f * (float) Math.sin(phase + 0.4);
                    m.leftY = 4f * (float) Math.sin(phase * 2.0 + 0.6);
                    m.rightY = 4f * (float) Math.sin(phase * 2.0 + 0.9);
                    return m;
                }
                case SHIELD_TRACK_CHANGE: {
                    long active = BoopShieldMotionBridge.trackChangeDurationMs();
                    long elapsed = Math.round(p * (active + 650L));
                    float[] pose = elapsed <= active
                            ? BoopShieldMotionBridge.trackChange(elapsed)
                            : BoopShieldMotionBridge.rest();
                    m.headX = pose[0];
                    m.headY = pose[1];
                    m.headRotation = pose[2];
                    float perk = elapsed <= active ? pulse(elapsed / (float) active) : 0f;
                    m.leftY = -12f * perk;
                    m.rightY = -12f * perk;
                    return m;
                }
                case SHIELD_PAUSE_SETTLE: {
                    long active = BoopShieldMotionBridge.settleDurationMs();
                    long elapsed = Math.round(p * (active + 850L));
                    float[] pose = elapsed <= active
                            ? BoopShieldMotionBridge.settle(elapsed)
                            : BoopShieldMotionBridge.rest();
                    m.headX = pose[0];
                    m.headY = pose[1];
                    m.headRotation = pose[2];
                    m.leftX = -pose[0] * 0.20f;
                    m.rightX = -pose[0] * 0.20f;
                    return m;
                }
                case SHIELD_CINEMA_HAND: {
                    float[] pose = BoopShieldMotionBridge.cinema(
                            Math.round(p * BoopShieldMotionBridge.cinemaPeriodMs()));
                    m.rightX = pose[0] * 0.70f;
                    m.rightY = pose[1] * 0.58f;
                    m.rightRotation = pose[2];
                    m.rightAlpha = pose[3];
                    m.headRotation = pose[2] * 0.10f;
                    return m;
                }
                case WIP_OPEN_PALMS: {
                    float breathe = (float) Math.sin(phase);
                    m.leftY = -8f - 6f * breathe;
                    m.rightY = -8f + 6f * breathe;
                    m.leftRotation = -3f + 2f * breathe;
                    m.rightRotation = 3f + 2f * breathe;
                    m.headY = -3f * (float) Math.sin(phase * 2.0);
                    return m;
                }
                case WIP_WAVE: {
                    float envelope = pulse(p);
                    m.rightY = -72f * envelope;
                    m.rightX = -20f * envelope;
                    m.rightRotation = 22f * (float) Math.sin(phase * 4.0) * envelope;
                    m.rightScale = 1f + 0.06f * envelope;
                    m.headRotation = -2.5f * envelope;
                    m.leftY = 6f * envelope;
                    return m;
                }
                case WIP_POINT: {
                    float reach = pulse(p);
                    m.rightX = 92f * reach;
                    m.rightY = -46f * reach;
                    m.rightRotation = -14f * reach;
                    m.rightScale = 1f + 0.10f * reach;
                    m.leftX = -18f * reach;
                    m.headX = 12f * reach;
                    m.headRotation = 3f * reach;
                    return m;
                }
                case WIP_GRIP: {
                    float grip = pulse(p);
                    m.leftX = 92f * grip;
                    m.rightX = -92f * grip;
                    m.leftY = -118f * grip;
                    m.rightY = -118f * grip;
                    m.leftRotation = 12f * grip;
                    m.rightRotation = -12f * grip;
                    m.leftScale = 1f - 0.06f * grip;
                    m.rightScale = 1f - 0.06f * grip;
                    m.headScale = 1f + 0.015f * grip;
                    return m;
                }
                case WIP_EARCUP_ADJUST: {
                    float grip = smoothPulse(p);
                    float adjust = (float) Math.sin(phase * 3.0) * grip;
                    m.leftX = 92f * grip;
                    m.rightX = -92f * grip;
                    m.leftY = -120f * grip - 10f * adjust;
                    m.rightY = -120f * grip + 10f * adjust;
                    m.leftRotation = 12f * grip + 3f * adjust;
                    m.rightRotation = -12f * grip + 3f * adjust;
                    m.headY = -5f * adjust;
                    m.headRotation = 1.7f * adjust;
                    return m;
                }
                case WIP_ONE_CUP_LISTEN: {
                    float lift = stagedHold(p);
                    m.rightX = -94f * lift;
                    m.rightY = -146f * lift;
                    m.rightRotation = -18f * lift;
                    m.leftY = 12f * lift;
                    m.headX = -16f * lift;
                    m.headY = -8f * lift;
                    m.headRotation = -6f * lift;
                    m.headScale = 1f + 0.018f * lift;
                    return m;
                }
                case WIP_GAZE_DEPTH: {
                    float sway = (float) Math.sin(phase);
                    float depth = (float) Math.sin(phase + Math.PI / 2.0);
                    m.headX = 22f * sway;
                    m.headY = -8f * depth;
                    m.headRotation = 2.6f * sway;
                    m.headScale = 1f + 0.035f * depth;
                    m.leftX = -10f * sway;
                    m.rightX = -14f * sway;
                    m.leftY = 5f * depth;
                    m.rightY = 8f * depth;
                    m.leftScale = 1f - 0.012f * depth;
                    m.rightScale = 1f - 0.018f * depth;
                    return m;
                }
                case WIP_HEADPHONE_RECOIL: {
                    float hit = pulse(Math.min(1f, p * 1.35f));
                    float recoil = (float) (Math.exp(-4.2 * p) * Math.sin(TWO_PI * 3.0 * p));
                    float handLag = (float) (Math.exp(-3.6 * p) * Math.sin(TWO_PI * 3.0 * Math.max(0f, p - 0.055f)));
                    m.headX = 42f * recoil + 10f * hit;
                    m.headY = -18f * hit;
                    m.headRotation = 6.2f * recoil;
                    m.leftX = -20f * handLag;
                    m.rightX = -20f * handLag;
                    m.leftRotation = -5f * handLag;
                    m.rightRotation = -5f * handLag;
                    return m;
                }
                default:
                    return m;
            }
        }

        private static float pulse(float p) {
            return (float) Math.sin(Math.PI * clamp(p));
        }

        private static float smoothPulse(float p) {
            float value = pulse(p);
            return value * value * (3f - 2f * value);
        }

        private static float stagedHold(float p) {
            float t = clamp(p);
            if (t < 0.24f) return smooth(t / 0.24f);
            if (t < 0.70f) return 1f;
            return 1f - smooth((t - 0.70f) / 0.30f);
        }

        private static float smooth(float value) {
            float t = clamp(value);
            return t * t * (3f - 2f * t);
        }

        private static float clamp(float value) {
            if (!Float.isFinite(value)) return 0f;
            return Math.max(0f, Math.min(1f, value));
        }
    }

    private static final class Motion {
        float headX;
        float headY;
        float headRotation;
        float headScale = 1f;
        float leftX;
        float leftY;
        float leftRotation;
        float leftScale = 1f;
        float leftAlpha = 1f;
        float rightX;
        float rightY;
        float rightRotation;
        float rightScale = 1f;
        float rightAlpha = 1f;
    }
}
