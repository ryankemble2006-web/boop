#!/usr/bin/env bash
set -euo pipefail
rm -rf boop-build
unzip -q BOOP-Alpha1-project.zip -d boop-build
ROOT=boop-build/BOOP-Alpha1
cp gradle.properties "$ROOT/gradle.properties"
MAIN="$ROOT/app/src/main/java/com/boop/alpha1"
TEST="$ROOT/app/src/test/java/com/boop/alpha1"
mkdir -p "$MAIN" "$TEST"
cp source/*.java "$MAIN"/
cp source/companion/*.java "$MAIN"/
python3 scripts/patch-wake-partial-fallback.py
python3 scripts/patch-toast-easter-egg.py
python3 scripts/patch-wall-chat-mode.py
python3 scripts/patch-wall-openai-relay.py
python3 scripts/patch-wall-idle-blink.py
python3 - <<'PY'
from pathlib import Path

root = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1')
main = root / 'MainActivity.java'
text = main.read_text(encoding='utf-8')
marker = '        Button done = new Button(this);\n'
insert = '        BoopEyeHueSettings.addSlider(this, voiceSettingsOverlay, face);\n\n'
if insert not in text:
    if text.count(marker) != 1:
        raise SystemExit(f'BOOP eye hue settings anchor expected once, found {text.count(marker)}')
    main.write_text(text.replace(marker, insert + marker, 1), encoding='utf-8')

face = root / 'BoopFaceView.java'
face_text = face.read_text(encoding='utf-8')
constructor_anchor = '        faceBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.boop_eyes);\n'
constructor_insert = constructor_anchor + '        setEyeHueDegrees(BoopEyeHue.loadHue(context));\n'
if 'setEyeHueDegrees(BoopEyeHue.loadHue(context));' not in face_text:
    if face_text.count(constructor_anchor) != 1:
        raise SystemExit('BOOP eye hue face constructor anchor changed')
    face_text = face_text.replace(constructor_anchor, constructor_insert, 1)
method_anchor = '    void showIdleBlackImmediately() {\n'
method = '''    void setEyeHueDegrees(int hueDegrees) {
        paint.setColorFilter(BoopEyeHue.colorFilterForHue(hueDegrees));
        invalidate();
    }

'''
if 'void setEyeHueDegrees(int hueDegrees)' not in face_text:
    if face_text.count(method_anchor) != 1:
        raise SystemExit('BOOP eye hue face method anchor changed')
    face_text = face_text.replace(method_anchor, method + method_anchor, 1)
face.write_text(face_text, encoding='utf-8')

(root / 'BoopEyeHueMath.java').write_text('''package com.boop.alpha1;

final class BoopEyeHueMath {
    static final int PROGRESS_MAX = 359;
    static final int DEFAULT_HUE_DEGREES = 190;

    private BoopEyeHueMath() { }

    static int clampHue(int hueDegrees) {
        return Math.max(0, Math.min(PROGRESS_MAX, hueDegrees));
    }

    static float rotationDegreesForHue(int hueDegrees) {
        return clampHue(hueDegrees) - DEFAULT_HUE_DEGREES;
    }

    static float[] matrixForHue(int hueDegrees) {
        int bounded = clampHue(hueDegrees);
        if (bounded == DEFAULT_HUE_DEGREES) {
            return null;
        }
        double radians = Math.toRadians(rotationDegreesForHue(bounded));
        float cosine = (float) Math.cos(radians);
        float sine = (float) Math.sin(radians);
        return new float[]{
                0.213f + cosine * 0.787f - sine * 0.213f,
                0.715f - cosine * 0.715f - sine * 0.715f,
                0.072f - cosine * 0.072f + sine * 0.928f, 0f, 0f,
                0.213f - cosine * 0.213f + sine * 0.143f,
                0.715f + cosine * 0.285f + sine * 0.140f,
                0.072f - cosine * 0.072f - sine * 0.283f, 0f, 0f,
                0.213f - cosine * 0.213f - sine * 0.787f,
                0.715f - cosine * 0.715f + sine * 0.715f,
                0.072f + cosine * 0.928f + sine * 0.072f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
        };
    }
}
''', encoding='utf-8')

(root / 'BoopEyeHue.java').write_text('''package com.boop.alpha1;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ColorFilter;

final class BoopEyeHue {
    private static final String PREFS_NAME = "boop_eyes";
    private static final String KEY_HUE_DEGREES = "hue_degrees";

    private BoopEyeHue() { }

    static int loadHue(Context context) {
        if (context == null) return BoopEyeHueMath.DEFAULT_HUE_DEGREES;
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return BoopEyeHueMath.clampHue(preferences.getInt(KEY_HUE_DEGREES, BoopEyeHueMath.DEFAULT_HUE_DEGREES));
    }

    static void saveHue(Context context, int hueDegrees) {
        if (context == null) return;
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putInt(KEY_HUE_DEGREES, BoopEyeHueMath.clampHue(hueDegrees)).apply();
    }

    static ColorFilter colorFilterForHue(int hueDegrees) {
        float[] values = BoopEyeHueMath.matrixForHue(hueDegrees);
        return values == null ? null : new ColorMatrixColorFilter(new ColorMatrix(values));
    }
}
''', encoding='utf-8')

(root / 'BoopEyeHueSettings.java').write_text('''package com.boop.alpha1;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

final class BoopEyeHueSettings {
    private BoopEyeHueSettings() { }

    static void addSlider(Activity activity, LinearLayout overlay, BoopFaceView face) {
        if (activity == null || overlay == null || face == null) return;
        TextView label = new TextView(activity);
        label.setText("Eye colour");
        label.setTextColor(Color.WHITE);
        label.setTextSize(22f);
        label.setGravity(Gravity.CENTER);
        overlay.addView(label);

        SeekBar slider = new SeekBar(activity);
        slider.setMax(BoopEyeHueMath.PROGRESS_MAX);
        slider.setProgress(BoopEyeHue.loadHue(activity));
        slider.setContentDescription("Eye colour hue");
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                BoopEyeHue.saveHue(activity, progress);
                face.setEyeHueDegrees(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(activity, 64));
        params.setMargins(0, dp(activity, 4), 0, dp(activity, 28));
        overlay.addView(slider, params);
    }

    private static int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
''', encoding='utf-8')
PY
python3 - <<'PY'
from pathlib import Path

main = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java')
text = main.read_text(encoding='utf-8')
old = '        startActivity(launcherIntent);\n    }\n'
new = '''        android.app.ActivityOptions options = android.app.ActivityOptions.makeCustomAnimation(
                this, R.anim.boop_launcher_enter_from_right, R.anim.boop_wall_exit_to_left);
        startActivity(launcherIntent, options.toBundle());
    }
'''
if text.count(old) != 1:
    raise SystemExit(f'Wall launcher transition patch expected one launch anchor, found {text.count(old)}')
main.write_text(text.replace(old, new, 1), encoding='utf-8')

anim = Path('boop-build/BOOP-Alpha1/app/src/main/res/anim')
anim.mkdir(parents=True, exist_ok=True)
(anim / 'boop_launcher_enter_from_right.xml').write_text('''<?xml version="1.0" encoding="utf-8"?>
<translate xmlns:android="http://schemas.android.com/apk/res/android"
    android:fromXDelta="100%p"
    android:toXDelta="0%p"
    android:duration="220" />
''', encoding='utf-8')
(anim / 'boop_wall_exit_to_left.xml').write_text('''<?xml version="1.0" encoding="utf-8"?>
<translate xmlns:android="http://schemas.android.com/apk/res/android"
    android:fromXDelta="0%p"
    android:toXDelta="-100%p"
    android:duration="220" />
''', encoding='utf-8')
PY
cp source/AndroidManifest.xml "$ROOT/app/src/main/AndroidManifest.xml"
cp source/app-build.gradle "$ROOT/app/build.gradle"
if compgen -G 'source-test/*.java' > /dev/null; then
  cp source-test/*.java "$TEST"/
fi
if compgen -G 'source-android-test/*.java' > /dev/null; then
  ANDROID_TEST="$ROOT/app/src/androidTest/java/com/boop/alpha1"
  mkdir -p "$ANDROID_TEST"
  cp source-android-test/*.java "$ANDROID_TEST"/
fi
bash scripts/fetch-wake-assets.sh "$ROOT/app"
