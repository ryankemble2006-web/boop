#!/usr/bin/env python3
"""Narrow, fail-closed addition after the approved chat-mode materialization.

Raw MainActivity/face and their historical checks stay intact. This delta only
adds a visual idle scheduler, reads existing busy flags, and scales the drawn
eyelids. It never invokes interaction or changes the existing sleep scheduler.
"""
from pathlib import Path

ROOT = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1')
MAIN_MARKER = '        // BOOP_IDLE_BLINK_V1: read state only; never extend the idle deadline.\n'
MAIN_HOOK = MAIN_MARKER + '''        face.setIdleBlinkAllowed(() -> activityInForeground
                && presenceState != null && !presenceState.isIdleBlack()
                && !listening && !thinking && (tts == null || !tts.isSpeaking())
                && !voiceSettingsOpen && !chatModeOpen && !faceTouchActive);
'''
FACE_MARKER = '    // BOOP_IDLE_BLINK_V1: separate eyelid geometry, not a whole-view animator.\n'
FACE_FIELDS = FACE_MARKER + '''    private final java.util.Random idleBlinkRandom = new java.util.Random();
    private java.util.function.BooleanSupplier idleBlinkAllowed;
    private final Runnable idleBlinkRunnable = this::runIdleBlink;
    private ValueAnimator idleBlinkAnimator;
    private float idleBlinkOpenness = 1f;
    private boolean awakeForBlink;
    private long idleBlinkBlockedUntil;
'''
FACE_METHODS = '''    void setIdleBlinkAllowed(java.util.function.BooleanSupplier allowed) {
        idleBlinkAllowed = allowed;
    }

    private boolean canIdleBlink() {
        return awakeForBlink && isAttachedToWindow() && hasWindowFocus()
                && getWindowVisibility() == VISIBLE && isShown() && getAlpha() > 0f
                && android.os.SystemClock.uptimeMillis() >= idleBlinkBlockedUntil
                && !shakeMuppetActive
                && (thinkingAnimator == null || !thinkingAnimator.isRunning())
                && idleBlinkAllowed != null && idleBlinkAllowed.getAsBoolean()
                && ValueAnimator.areAnimatorsEnabled();
    }

    private void startIdleBlinking(long settlingMillis) {
        awakeForBlink = true;
        deferIdleBlink(settlingMillis);
    }

    private void deferIdleBlink(long settlingMillis) {
        idleBlinkBlockedUntil = android.os.SystemClock.uptimeMillis() + settlingMillis;
        cancelIdleBlinkFrame();
        queueNextIdleBlink();
    }

    private void queueNextIdleBlink() {
        if (idleBlinkRunnable == null) return;
        removeCallbacks(idleBlinkRunnable);
        if (awakeForBlink && isAttachedToWindow() && hasWindowFocus()
                && getWindowVisibility() == VISIBLE && isShown()) {
            postDelayed(idleBlinkRunnable, BoopIdleBlink.nextDelayMillis(idleBlinkRandom));
        }
    }

    private void runIdleBlink() {
        removeCallbacks(idleBlinkRunnable);
        if (!canIdleBlink()) {
            queueNextIdleBlink();
            return;
        }
        cancelIdleBlinkFrame();
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        idleBlinkAnimator = animator;
        animator.setDuration(BoopIdleBlink.DURATION_MS);
        animator.setInterpolator(new android.view.animation.LinearInterpolator());
        animator.addUpdateListener(frame -> {
            if (!canIdleBlink()) {
                cancelIdleBlinkFrame();
                queueNextIdleBlink();
                return;
            }
            idleBlinkOpenness = BoopIdleBlink.openness((float) frame.getAnimatedValue());
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator finished) {
                if (idleBlinkAnimator != finished) return;
                idleBlinkAnimator = null;
                idleBlinkOpenness = 1f;
                invalidate();
                queueNextIdleBlink();
            }
        });
        animator.start();
    }

    private void cancelIdleBlinkFrame() {
        ValueAnimator running = idleBlinkAnimator;
        idleBlinkAnimator = null; // Cancellation must not queue a replacement.
        if (running != null) running.cancel();
        idleBlinkOpenness = 1f;
        invalidate();
    }

    private void suspendIdleBlinking() {
        if (idleBlinkRunnable != null) removeCallbacks(idleBlinkRunnable);
        cancelIdleBlinkFrame();
    }

    private void stopIdleBlinking() {
        awakeForBlink = false;
        suspendIdleBlinking();
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        queueNextIdleBlink();
    }

    @Override protected void onDetachedFromWindow() {
        suspendIdleBlinking();
        super.onDetachedFromWindow();
    }

    @Override protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) queueNextIdleBlink();
        else suspendIdleBlinking();
    }

    @Override public void onWindowFocusChanged(boolean focused) {
        super.onWindowFocusChanged(focused);
        if (focused) queueNextIdleBlink();
        else suspendIdleBlinking();
    }

'''


def replace_once(text, old, new):
    if text.count(old) != 1:
        raise ValueError(f'idle blink patch: expected one source anchor: {old[:90]!r}')
    return text.replace(old, new, 1)


def patch_main(text):
    if MAIN_MARKER in text:
        if text.count(MAIN_HOOK) != 1:
            raise ValueError('idle blink patch: partial or modified MainActivity hook')
        return text
    if 'BOOP_CHAT_MODE_V1' not in text:
        raise ValueError('idle blink patch: the reviewed chat-mode patch must run first')
    anchor = '        face = new BoopFaceView(this);\n'
    return replace_once(text, anchor, anchor + MAIN_HOOK)


def patch_face(text):
    if FACE_MARKER in text:
        for part in (FACE_FIELDS, FACE_METHODS, 'eye.height() * idleBlinkOpenness / 2f',
                     'canvas.scale(1f, idleBlinkOpenness, getWidth() / 2f, eyelidCenterY);'):
            if text.count(part) != 1:
                raise ValueError('idle blink patch: partial or modified face')
        return text
    text = replace_once(text, '    private float shakeStrength;\n',
                        '    private float shakeStrength;\n' + FACE_FIELDS)
    text = replace_once(text, '    void showIdleBlackImmediately() {\n',
                        FACE_METHODS + '    void showIdleBlackImmediately() {\n        stopIdleBlinking();\n')
    text = replace_once(text, '    void goIdleBlack() {\n',
                        '    void goIdleBlack() {\n        stopIdleBlinking();\n')
    text = replace_once(text, '    void wakeFromIdle() {\n',
                        '    void wakeFromIdle() {\n        startIdleBlinking(WAKE_DURATION_MS);\n')
    text = replace_once(text, '        ObjectAnimator animator;\n',
                        '        deferIdleBlink(MEMBER_BERRY_DURATION_MS);\n        ObjectAnimator animator;\n')
    text = replace_once(text, '    void playShakeMuppet(float strength) {\n',
                        '    void playShakeMuppet(float strength) {\n        startIdleBlinking(SHAKE_MUPPET_DURATION_MS);\n')
    text = replace_once(text, '    void startThinking() {\n',
                        '    void startThinking() {\n        deferIdleBlink(THINKING_DURATION_MS);\n')
    text = replace_once(text, '    void stopThinking() {\n',
                        '    void stopThinking() {\n        cancelIdleBlinkFrame();\n')
    text = replace_once(text, '        float halfHeight = eye.height() / 2f;\n',
                        '        float halfHeight = eye.height() * idleBlinkOpenness / 2f;\n')
    old = '''        canvas.drawBitmap(
                faceBitmap,
                null,
                new RectF(left, top, left + width, top + height),
                paint);
'''
    new = '''        int eyelidSave = canvas.save();
        float eyelidCenterY = top + LEFT_SOURCE.exactCenterY() * scale;
        canvas.scale(1f, idleBlinkOpenness, getWidth() / 2f, eyelidCenterY);
''' + old + '''        canvas.restoreToCount(eyelidSave);
'''
    return replace_once(text, old, new)


def main():
    main_path, face_path = ROOT / 'MainActivity.java', ROOT / 'BoopFaceView.java'
    # Validate BOTH files before writing either, so a changed source fails closed.
    results = [(main_path, patch_main(main_path.read_text(encoding='utf-8'))),
               (face_path, patch_face(face_path.read_text(encoding='utf-8')))]
    for path, result in results:
        path.write_text(result, encoding='utf-8')


if __name__ == '__main__':
    main()
