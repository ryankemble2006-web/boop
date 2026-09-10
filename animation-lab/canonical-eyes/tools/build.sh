#!/usr/bin/env bash
set -euo pipefail
BASE=animation-lab/canonical-eyes
OUT=work/canonical-eyes-build
SDK="${ANDROID_HOME}/platforms/android-36/android.jar"
TOOLS="${ANDROID_HOME}/build-tools/36.0.0"
python "$BASE/tools/prepare.py" --out "$OUT"
mkdir -p "$OUT/classes" "$OUT/dex"
find "$BASE/java" "$BASE/android" "$OUT/java" -name '*.java' > "$OUT/sources.txt"
javac -source 8 -target 8 -classpath "$SDK" -d "$OUT/classes" @"$OUT/sources.txt"
jar cf "$OUT/classes.jar" -C "$OUT/classes" .
"$TOOLS/d8" --lib "$SDK" --min-api 26 --output "$OUT/dex" "$OUT/classes.jar"
"$TOOLS/aapt" package -f -M "$BASE/android/AndroidManifest.xml" -I "$SDK" -A "$OUT/assets" -F "$OUT/unsigned.apk"
python - "$OUT" <<'PY'
import sys,zipfile
from pathlib import Path
out=Path(sys.argv[1])
with zipfile.ZipFile(out/'unsigned.apk','a',zipfile.ZIP_DEFLATED) as z:
    for dex in (out/'dex').glob('*.dex'):z.write(dex,dex.name)
PY
"$TOOLS/zipalign" -f -p 4 "$OUT/unsigned.apk" "$OUT/aligned.apk"
"$TOOLS/apksigner" sign --ks "$BOOP_SIGNING_STORE_FILE" --ks-key-alias boop-dev --ks-pass env:BOOP_DEV_STORE_PASSWORD --key-pass env:BOOP_DEV_KEY_PASSWORD --out "$OUT/BOOP-Animation-Lab-v9.apk" "$OUT/aligned.apk"
"$TOOLS/apksigner" verify --print-certs "$OUT/BOOP-Animation-Lab-v9.apk" > "$OUT/signer.txt"
grep -Fq 'f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde' "$OUT/signer.txt"
"$TOOLS/aapt" dump badging "$OUT/BOOP-Animation-Lab-v9.apk" > "$OUT/badging.txt"
grep -Fq "package: name='com.boop.animationlab' versionCode='9'" "$OUT/badging.txt"
grep -Fq "launchable-activity: name='com.boop.alpha1.BoopDevMenuActivity'" "$OUT/badging.txt"
if grep -q '^uses-permission:' "$OUT/badging.txt"; then echo 'Unexpected permission' >&2; exit 1; fi
git rev-parse HEAD > "$OUT/built-commit.txt"
sha256sum "$OUT/BOOP-Animation-Lab-v9.apk" > "$OUT/apk-sha256.txt"
python - "$OUT/BOOP-Animation-Lab-v9.apk" <<'PY'
import hashlib,sys,zipfile
with zipfile.ZipFile(sys.argv[1]) as z:
    assert z.testzip() is None
    assert hashlib.sha256(z.read('assets/boopApprovedEyes.png')).hexdigest()=='ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22'
    assert hashlib.sha256(z.read('assets/boop-notification-hands.png')).hexdigest()=='26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1'
print('APK package/signer/archive/source integrity passed; no visual acceptance.')
PY
