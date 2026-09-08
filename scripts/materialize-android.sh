#!/usr/bin/env bash
set -euo pipefail
rm -rf boop-build
unzip -q BOOP-Alpha1-project.zip -d boop-build
ROOT=boop-build/BOOP-Alpha1

# Ryan's permanent approved face. Copy the exact repository bytes directly into
# the Android resource tree. No transparency reconstruction or image conversion.
APPROVED_EYES="unified/assets/boop-eyes/boopApprovedEyes.png"
test -s "$APPROVED_EYES"
mkdir -p "$ROOT/app/src/main/res/drawable-nodpi"
cp "$APPROVED_EYES" "$ROOT/app/src/main/res/drawable-nodpi/boop_eyes.png"

cp gradle.properties "$ROOT/gradle.properties"
MAIN="$ROOT/app/src/main/java/com/boop/alpha1"
TEST="$ROOT/app/src/test/java/com/boop/alpha1"
mkdir -p "$MAIN" "$TEST"
cp source/*.java "$MAIN"/
cp source/companion/*.java "$MAIN"/
python3 scripts/patch-approved-eye-geometry.py
python3 scripts/patch-wake-partial-fallback.py
python3 scripts/patch-toast-easter-egg.py
python3 scripts/patch-wall-chat-mode.py
python3 scripts/patch-wall-openai-relay.py
python3 scripts/patch-wall-idle-blink.py
python3 scripts/patch-wall-sleep-charm.py
python3 scripts/patch-wall-eye-hue.py
python3 scripts/patch-unified-iris-cache.py
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
