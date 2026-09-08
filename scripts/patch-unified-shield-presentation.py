#!/usr/bin/env python3
"""Reuse the permanent approved BOOP eye master, geometry and blink on Shield.

Only generated unified sources are patched. No image generation or visual tests.
The supplied PNG alpha is drawn directly; the old flood-fill isolation path is not
used for the approved master.
"""
from pathlib import Path

ROOT = Path('boop-build/BOOP-Alpha1')
APP_SHARED = ROOT / 'app/src/main/java/com/boop/alpha1'
SHIELD = ROOT / 'shield-lib/src/main/java/com/boop/shieldoverlay'
MARKER = '// BOOP_SHARED_SHIELD_EYES_V2'


def once(text, old, new, label):
    if text.count(old) != 1:
        raise ValueError(f'{label}: expected exactly one source anchor, got {text.count(old)}')
    return text.replace(old, new, 1)


BLINK_FIELDS = '''    // BOOP_SHARED_SHIELD_EYES_V2
    private final java.util.Random eyeBlinkRandom = new java.util.Random();
    private final Runnable eyeBlinkRunnable = this::runEyeBlink;
    private android.animation.ValueAnimator eyeBlinkAnimator;
    private float eyeBlinkOpenness = 1f;
    private boolean eyeBlinkQueued;
'''

BLINK_METHODS = '''    private boolean eyeBlinkAllowed() {
        // The overlay never takes focus. Visibility/display are the lifecycle gates.
        android.os.PowerManager manager = getContext().getSystemService(android.os.PowerManager.class);
        boolean powerSave = manager != null && manager.isPowerSaveMode();
        return attached && displayActive && isShown()
                && getWindowVisibility() == VISIBLE && getAlpha() > 0f
                && puppetMode == DeezerPuppetPolicy.Mode.EYES
                && !powerSave
                && android.animation.ValueAnimator.areAnimatorsEnabled()
                && Settings.Global.getFloat(getContext().getContentResolver(),
                        Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f;
    }

    private void syncEyeBlink() {
        if (eyeBlinkRunnable == null) return; // View construction can call visibility hooks.
        if (!eyeBlinkAllowed()) {
            stopEyeBlink();
        } else if (!eyeBlinkQueued && eyeBlinkAnimator == null) {
            eyeBlinkQueued = true;
            postDelayed(eyeBlinkRunnable, BoopIdleBlink.nextDelayMillis(eyeBlinkRandom));
        }
    }

    private void runEyeBlink() {
        eyeBlinkQueued = false;
        if (!eyeBlinkAllowed()) {
            syncEyeBlink();
            return;
        }
        android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofFloat(0f, 1f);
        eyeBlinkAnimator = animator;
        animator.setDuration(BoopIdleBlink.DURATION_MS);
        animator.setInterpolator(new android.view.animation.LinearInterpolator());
        animator.addUpdateListener(frame -> {
            if (!eyeBlinkAllowed()) {
                stopEyeBlink();
                return;
            }
            eyeBlinkOpenness = BoopIdleBlink.openness((float) frame.getAnimatedValue());
            invalidate();
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(android.animation.Animator finished) {
                if (eyeBlinkAnimator != finished) return;
                eyeBlinkAnimator = null;
                eyeBlinkOpenness = 1f;
                invalidate();
                syncEyeBlink();
            }
        });
        animator.start();
    }

    private void stopEyeBlink() {
        if (eyeBlinkRunnable != null) removeCallbacks(eyeBlinkRunnable);
        eyeBlinkQueued = false;
        android.animation.ValueAnimator animator = eyeBlinkAnimator;
        eyeBlinkAnimator = null;
        if (animator != null) animator.cancel();
        if (eyeBlinkOpenness != 1f) {
            eyeBlinkOpenness = 1f;
            invalidate();
        }
    }

    private void drawLockedEyes(Canvas canvas) {
        // Use the same approved geometry as Wall, uniformly framed inside the TV slot.
        BoopEyeLayout.Layout layout = BoopEyeLayout.calculate(
                Math.max(getWidth(), getHeight() + 1), getHeight());
        if (!layout.landscape() || faceBitmap == null) return;
        BoopEyeLayout.Eye left = layout.left();
        BoopEyeLayout.Eye right = layout.right();
        float pairLeft = left.centerX() - left.width() / 2f;
        float pairRight = right.centerX() + right.width() / 2f;
        float pairWidth = pairRight - pairLeft;
        float pairHeight = Math.max(left.height(), right.height());
        if (pairWidth <= 0f || pairHeight <= 0f) return;
        float fit = Math.min(getWidth() / pairWidth, getHeight() / pairHeight);
        float centreX = (pairLeft + pairRight) / 2f;
        float centreY = left.centerY();
        int save = canvas.save();
        canvas.translate(getWidth() / 2f, getHeight() / 2f);
        canvas.scale(fit, fit);
        canvas.translate(-centreX, -centreY);
        drawLockedEye(canvas, LEFT_SOURCE, left);
        drawLockedEye(canvas, RIGHT_SOURCE, right);
        canvas.restoreToCount(save);
    }

    private void drawLockedEye(Canvas canvas, Rect source, BoopEyeLayout.Eye eye) {
        if (faceBitmap == null) return;
        float halfWidth = eye.width() / 2f;
        float halfHeight = eye.height() * eyeBlinkOpenness / 2f;
        canvas.drawBitmap(faceBitmap, source, new RectF(
                eye.centerX() - halfWidth, eye.centerY() - halfHeight,
                eye.centerX() + halfWidth, eye.centerY() + halfHeight), paint);
    }

'''


def patch_overlay(text):
    if '// BOOP_SHARED_SHIELD_EYES_V1' in text or MARKER in text:
        raise ValueError('Shield presentation already patched; materialize a fresh tree')
    text = once(
        text,
        '    private static final Rect LEFT_SOURCE = new Rect(90, 600, 419, 993);\n'
        '    private static final Rect RIGHT_SOURCE = new Rect(525, 600, 854, 993);\n',
        '    private static final Rect LEFT_SOURCE = new Rect(BoopApprovedEyeGeometry.LEFT_SOURCE);\n'
        '    private static final Rect RIGHT_SOURCE = new Rect(BoopApprovedEyeGeometry.RIGHT_SOURCE);\n',
        'approved master source rectangles')
    text = once(text, '    private final Bitmap leftEye;\n    private final Bitmap rightEye;\n',
                '    private final Bitmap faceBitmap;\n', 'approved eye bitmap field')
    text = once(text,
                '        Bitmap source = BitmapFactory.decodeResource(getResources(), R.drawable.boop_eyes);\n'
                '        leftEye = isolateEye(source, LEFT_SOURCE);\n'
                '        rightEye = isolateEye(source, RIGHT_SOURCE);\n',
                '        faceBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.boop_eyes);\n',
                'approved eye bitmap load')
    text = once(text, '    private final HeadphoneRenderer headphoneRenderer;\n',
                BLINK_FIELDS + '    private final HeadphoneRenderer headphoneRenderer;\n', 'blink fields')
    start = text.index('        float scale = Math.min(getWidth() / (float) PAIR_WIDTH,')
    end = text.index('    private MediaPuppetMotion.Pose currentPuppetPose()', start)
    text = text[:start] + '        drawLockedEyes(canvas);\n    }\n\n' + BLINK_METHODS + text[end:]
    for line in ('    private static final int PAIR_WIDTH = 764;\n',
                 '    private static final int PAIR_HEIGHT = 393;\n',
                 '    private static final int RIGHT_OFFSET = 435;\n'):
        text = once(text, line, '', 'remove duplicate eye geometry')
    text = once(text, '                    this::powerSaveActiveOrUnknown);\n        }\n    }',
                '                    this::powerSaveActiveOrUnknown);\n        }\n        syncEyeBlink();\n    }',
                'blink visibility and power gates')
    text = once(text, '        frameLoop.detach();\n        animate().cancel();',
                '        frameLoop.detach();\n        stopEyeBlink();\n        animate().cancel();', 'blink detach')
    return text


def main():
    updates = {}
    # Copy the already materialized Wall helpers so Shield receives the exact
    # same approved geometry and blink without a circular module dependency.
    for name in ('BoopApprovedEyeGeometry.java', 'BoopEyeLayout.java', 'BoopIdleBlink.java'):
        shared = (APP_SHARED / name).read_text(encoding='utf-8')
        updates[SHIELD / name] = once(shared, 'package com.boop.alpha1;',
                                     'package com.boop.shieldoverlay;', 'shared helper namespace')
    path = SHIELD / 'BoopOverlayView.java'
    updates[path] = patch_overlay(path.read_text(encoding='utf-8'))
    # Validate every anchor before touching the generated tree.
    for path, content in updates.items():
        path.write_text(content, encoding='utf-8')
    print('Approved BOOP bitmap/geometry/blink materialized on Shield; no visual checks run')


if __name__ == '__main__':
    main()
