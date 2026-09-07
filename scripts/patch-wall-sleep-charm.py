#!/usr/bin/env python3
"""Replace only the materialized Wall sleep animation.

Runs after patch-wall-idle-blink.py so it can reuse the accepted eyelid geometry.
Wake, thinking, shake, Member Berry, hue and interaction code remain untouched.
"""
from pathlib import Path

FACE = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/BoopFaceView.java')
MARKER = '    // BOOP_SLEEP_CHARM_V1: accepted blink lead-in, then slow drowsy close.\n'


def replace_once(text, old, new):
    if text.count(old) != 1:
        raise ValueError(f'sleep charm patch: expected one source anchor: {old[:100]!r}')
    return text.replace(old, new, 1)


def patch(text):
    if MARKER in text:
        required = (
            'private ValueAnimator sleepCharmAnimator;',
            'BoopSleepCharm.TOTAL_DURATION_MS',
            'BoopSleepCharm.openness(progress)',
            'BoopSleepCharm.alpha(progress)',
            'cancelSleepCharm();',
        )
        for fragment in required:
            if text.count(fragment) < 1:
                raise ValueError('sleep charm patch: incomplete or altered prior patch')
        return text

    if 'BOOP_IDLE_BLINK_V1' not in text:
        raise ValueError('sleep charm patch: idle blink patch must run first')

    text = replace_once(
        text,
        '    private ValueAnimator idleBlinkAnimator;\n',
        '    private ValueAnimator idleBlinkAnimator;\n'
        + MARKER
        + '    private ValueAnimator sleepCharmAnimator;\n')

    cancel_method = '''    private void cancelSleepCharm() {
        ValueAnimator running = sleepCharmAnimator;
        sleepCharmAnimator = null;
        if (running != null) running.cancel();
        idleBlinkOpenness = 1f;
        setAlpha(1f);
        invalidate();
    }

'''
    text = replace_once(text, '    void showIdleBlackImmediately() {\n',
                        cancel_method + '    void showIdleBlackImmediately() {\n        cancelSleepCharm();\n')
    text = replace_once(text, '    void wakeFromIdle() {\n',
                        '    void wakeFromIdle() {\n        cancelSleepCharm();\n')

    old_sleep = '''    void goIdleBlack() {
        stopIdleBlinking();
        stopThinking();
        animate().cancel();
        resetPuppetTransform();
        setPivotX(getWidth() / 2f);
        setPivotY(getHeight() / 2f);
        animate()
                .alpha(0f)
                .scaleY(IDLE_SCALE_Y)
                .setDuration(SLEEP_DURATION_MS)
                .setInterpolator(new AccelerateInterpolator())
                .start();
    }
'''
    new_sleep = '''    void goIdleBlack() {
        stopIdleBlinking();
        stopThinking();
        animate().cancel();
        cancelSleepCharm();
        resetPuppetTransform();
        setPivotX(getWidth() / 2f);
        setPivotY(getHeight() / 2f);
        setScaleY(1f);
        setAlpha(1f);

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        sleepCharmAnimator = animator;
        animator.setDuration(BoopSleepCharm.TOTAL_DURATION_MS);
        animator.setInterpolator(new android.view.animation.LinearInterpolator());
        animator.addUpdateListener(frame -> {
            if (sleepCharmAnimator != animator) return;
            float progress = (float) frame.getAnimatedValue();
            idleBlinkOpenness = BoopSleepCharm.openness(progress);
            setAlpha(BoopSleepCharm.alpha(progress));
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator finished) {
                if (sleepCharmAnimator != finished) return;
                sleepCharmAnimator = null;
                idleBlinkOpenness = 1f;
                resetPuppetTransform();
                setPivotX(getWidth() / 2f);
                setPivotY(getHeight() / 2f);
                setScaleY(IDLE_SCALE_Y);
                setAlpha(0f);
                invalidate();
            }
        });
        animator.start();
    }
'''
    return replace_once(text, old_sleep, new_sleep)


def main():
    original = FACE.read_text(encoding='utf-8')
    result = patch(original)
    temp = FACE.with_suffix('.java.sleep-charm-tmp')
    temp.write_text(result, encoding='utf-8')
    temp.replace(FACE)


if __name__ == '__main__':
    main()
