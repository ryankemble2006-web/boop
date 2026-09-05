#!/usr/bin/env python3
from pathlib import Path


def replace_once(path, old, new, label):
    p = Path(path)
    text = p.read_text(encoding='utf-8')
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'{label}: expected one anchor, found {count}')
    p.write_text(text.replace(old, new, 1), encoding='utf-8')


# MainActivity: foreground accelerometer only, with a visual-only idle wake path.
replace_once(
    'source/MainActivity.java',
    'import android.graphics.Typeface;\nimport android.media.AudioManager;',
    'import android.graphics.Typeface;\nimport android.hardware.Sensor;\nimport android.hardware.SensorEvent;\nimport android.hardware.SensorEventListener;\nimport android.hardware.SensorManager;\nimport android.media.AudioManager;',
    'sensor imports',
)

replace_once(
    'source/MainActivity.java',
    'public final class MainActivity extends Activity implements RecognitionListener, TextToSpeech.OnInitListener {',
    'public final class MainActivity extends Activity implements RecognitionListener, TextToSpeech.OnInitListener, SensorEventListener {',
    'sensor listener interface',
)

replace_once(
    'source/MainActivity.java',
    '    private BoopPresenceState presenceState;\n    private Handler presenceHandler;\n    private SpeechRecognizer recognizer;',
    '    private BoopPresenceState presenceState;\n    private Handler presenceHandler;\n    private SensorManager sensorManager;\n    private Sensor shakeSensor;\n    private final BoopShakeDetector shakeDetector = new BoopShakeDetector();\n    private SpeechRecognizer recognizer;',
    'sensor fields',
)

replace_once(
    'source/MainActivity.java',
    '        presenceHandler = new Handler(Looper.getMainLooper());\n        presenceState = new BoopPresenceState();\n\n        interactionSurface = new FrameLayout(this);',
    '        presenceHandler = new Handler(Looper.getMainLooper());\n        presenceState = new BoopPresenceState();\n        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);\n        if (sensorManager != null) {\n            shakeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);\n        }\n\n        interactionSurface = new FrameLayout(this);',
    'sensor setup',
)

replace_once(
    'source/MainActivity.java',
    '    protected void onResume() {\n        super.onResume();\n        if (wakeCoordinator != null) {',
    '    protected void onResume() {\n        super.onResume();\n        shakeDetector.reset();\n        if (sensorManager != null && shakeSensor != null) {\n            sensorManager.registerListener(this, shakeSensor, SensorManager.SENSOR_DELAY_GAME);\n        }\n        if (wakeCoordinator != null) {',
    'register shake sensor',
)

replace_once(
    'source/MainActivity.java',
    '    protected void onPause() {\n        assistantFollowUpAfterTts = false;',
    '    protected void onPause() {\n        if (sensorManager != null) {\n            sensorManager.unregisterListener(this);\n        }\n        shakeDetector.reset();\n        assistantFollowUpAfterTts = false;',
    'unregister shake sensor',
)

replace_once(
    'source/MainActivity.java',
    '        super.onPause();\n    }\n\n    private void installTtsListener() {',
    '''        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event == null || event.sensor != shakeSensor || event.values == null || event.values.length < 3) {
            return;
        }
        if (presenceState == null
                || !presenceState.isIdleBlack()
                || voiceSettingsOpen
                || listening
                || thinking) {
            shakeDetector.reset();
            return;
        }

        long timestampMs = event.timestamp / 1_000_000L;
        if (shakeDetector.onAccelerometer(
                event.values[0], event.values[1], event.values[2], timestampMs)) {
            handleShakeWake(shakeDetector.lastTriggerStrength());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    private void handleShakeWake(float strength) {
        if (presenceState == null
                || face == null
                || !presenceState.isIdleBlack()
                || voiceSettingsOpen
                || listening
                || thinking) {
            return;
        }
        if (!presenceState.wake()) {
            return;
        }
        face.playShakeMuppet(strength);
        scheduleFaceIdle();
    }

    private void installTtsListener() {''',
    'shake callbacks',
)

replace_once(
    'source/MainActivity.java',
    '    protected void onDestroy() {\n        thinking = false;\n        voiceSettingsOpen = false;\n        closeWakeAudioSession();',
    '    protected void onDestroy() {\n        thinking = false;\n        voiceSettingsOpen = false;\n        if (sensorManager != null) {\n            sensorManager.unregisterListener(this);\n        }\n        shakeDetector.reset();\n        closeWakeAudioSession();',
    'destroy sensor cleanup',
)

# Face: independent eye ricochet using the actual view bounds as the invisible box.
replace_once(
    'source/BoopFaceView.java',
    'import android.animation.ObjectAnimator;',
    'import android.animation.Animator;\nimport android.animation.AnimatorListenerAdapter;\nimport android.animation.ObjectAnimator;',
    'animator imports',
)

replace_once(
    'source/BoopFaceView.java',
    '    private static final long MEMBER_BERRY_DURATION_MS = 300L;\n    private static final long THINKING_DURATION_MS = 1680L;',
    '    private static final long MEMBER_BERRY_DURATION_MS = 300L;\n    private static final long THINKING_DURATION_MS = 1680L;\n    private static final long SHAKE_MUPPET_DURATION_MS = 1_050L;',
    'shake duration',
)

replace_once(
    'source/BoopFaceView.java',
    '    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);\n    private ObjectAnimator thinkingAnimator;',
    '    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);\n    private ObjectAnimator thinkingAnimator;\n    private ValueAnimator shakeAnimator;\n    private boolean shakeMuppetActive;\n    private float shakeFraction;\n    private float shakeStrength;',
    'shake fields',
)

replace_once(
    'source/BoopFaceView.java',
    '    void startThinking() {',
    '''    void playShakeMuppet(float strength) {
        stopThinking();
        animate().cancel();
        resetPuppetTransform();
        setPivotX(getWidth() / 2f);
        setPivotY(getHeight() / 2f);
        setScaleY(1f);
        setAlpha(1f);

        shakeStrength = Math.max(0f, Math.min(1f, strength));
        shakeFraction = 0f;
        shakeMuppetActive = true;
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        shakeAnimator = animator;
        animator.setDuration(SHAKE_MUPPET_DURATION_MS);
        animator.addUpdateListener(valueAnimator -> {
            shakeFraction = (float) valueAnimator.getAnimatedValue();
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (shakeAnimator != animation) {
                    return;
                }
                shakeAnimator = null;
                shakeMuppetActive = false;
                shakeFraction = 0f;
                invalidate();
            }
        });
        animator.start();
    }

    void startThinking() {''',
    'shake animation method',
)

replace_once(
    'source/BoopFaceView.java',
    '    void stopThinking() {\n        if (thinkingAnimator != null) {\n            thinkingAnimator.cancel();\n            thinkingAnimator = null;\n        }\n        resetPuppetTransform();\n        setAlpha(1f);\n    }',
    '''    void stopThinking() {
        if (thinkingAnimator != null) {
            thinkingAnimator.cancel();
            thinkingAnimator = null;
        }
        cancelShakeMuppet();
        resetPuppetTransform();
        setAlpha(1f);
    }

    private void cancelShakeMuppet() {
        ValueAnimator animator = shakeAnimator;
        shakeAnimator = null;
        if (animator != null) {
            animator.cancel();
        }
        shakeMuppetActive = false;
        shakeFraction = 0f;
        invalidate();
    }''',
    'cancel shake with other face states',
)

replace_once(
    'source/BoopFaceView.java',
    '        BoopEyeLayout.Layout layout = BoopEyeLayout.calculate(getWidth(), getHeight());\n        if (!layout.landscape()) {',
    '''        BoopEyeLayout.Layout layout = BoopEyeLayout.calculate(getWidth(), getHeight());
        if (shakeMuppetActive) {
            EyeGeometry leftBase;
            EyeGeometry rightBase;
            if (layout.landscape()) {
                leftBase = EyeGeometry.from(layout.left());
                rightBase = EyeGeometry.from(layout.right());
            } else {
                leftBase = portraitEyeGeometry(LEFT_SOURCE);
                rightBase = portraitEyeGeometry(RIGHT_SOURCE);
            }
            drawShakeEye(canvas, LEFT_SOURCE, leftBase, true);
            drawShakeEye(canvas, RIGHT_SOURCE, rightBase, false);
            return;
        }
        if (!layout.landscape()) {''',
    'shake draw branch',
)

replace_once(
    'source/BoopFaceView.java',
    '    private void drawEye(Canvas canvas, Rect source, BoopEyeLayout.Eye eye) {',
    '''    private EyeGeometry portraitEyeGeometry(Rect source) {
        float scale = Math.min(
                getWidth() / (float) faceBitmap.getWidth(),
                getHeight() / (float) faceBitmap.getHeight());
        float faceWidth = faceBitmap.getWidth() * scale;
        float faceHeight = faceBitmap.getHeight() * scale;
        float faceLeft = (getWidth() - faceWidth) / 2f;
        float faceTop = (getHeight() - faceHeight) / 2f;
        return new EyeGeometry(
                faceLeft + source.exactCenterX() * scale,
                faceTop + source.exactCenterY() * scale,
                source.width() * scale,
                source.height() * scale);
    }

    private void drawShakeEye(Canvas canvas, Rect source, EyeGeometry base, boolean leftEye) {
        BoopShakeEyeMotion.Pose pose = BoopShakeEyeMotion.pose(
                leftEye,
                shakeFraction,
                getWidth(),
                getHeight(),
                base.centerX,
                base.centerY,
                base.width,
                base.height,
                shakeStrength);
        float halfWidth = base.width * pose.scaleX() / 2f;
        float halfHeight = base.height * pose.scaleY() / 2f;
        RectF destination = new RectF(
                pose.centerX() - halfWidth,
                pose.centerY() - halfHeight,
                pose.centerX() + halfWidth,
                pose.centerY() + halfHeight);
        int save = canvas.save();
        canvas.rotate(pose.rotation(), pose.centerX(), pose.centerY());
        canvas.drawBitmap(faceBitmap, source, destination, paint);
        canvas.restoreToCount(save);
    }

    private void drawEye(Canvas canvas, Rect source, BoopEyeLayout.Eye eye) {''',
    'shake eye drawing helpers',
)

replace_once(
    'source/BoopFaceView.java',
    '        canvas.drawBitmap(faceBitmap, source, destination, paint);\n    }\n}',
    '''        canvas.drawBitmap(faceBitmap, source, destination, paint);
    }

    private static final class EyeGeometry {
        final float centerX;
        final float centerY;
        final float width;
        final float height;

        EyeGeometry(float centerX, float centerY, float width, float height) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.width = width;
            this.height = height;
        }

        static EyeGeometry from(BoopEyeLayout.Eye eye) {
            return new EyeGeometry(eye.centerX(), eye.centerY(), eye.width(), eye.height());
        }
    }
}''',
    'eye geometry class',
)
