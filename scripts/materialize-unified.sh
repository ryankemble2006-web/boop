#!/usr/bin/env bash
set -euo pipefail

bash scripts/materialize-android.sh
python3 scripts/patch-unified-dock-mirror.py
python3 scripts/patch-unified-wake-name.py
python3 scripts/patch-unified-wake-arm.py
python3 scripts/patch-unified-shield-dashboard.py
ROOT=boop-build/BOOP-Alpha1
APP="$ROOT/app"

rm -rf "$ROOT/launcher-lib" "$ROOT/shield-lib"
mkdir -p "$ROOT/launcher-lib/src/main" "$ROOT/shield-lib/src/main"

cp unified/app-build.gradle "$APP/build.gradle"
cp unified/launcher-lib.gradle "$ROOT/launcher-lib/build.gradle"
cp unified/shield-lib.gradle "$ROOT/shield-lib/build.gradle"
cp unified/launcher-manifest.xml "$ROOT/launcher-lib/src/main/AndroidManifest.xml"
cp unified/shield-manifest.xml "$ROOT/shield-lib/src/main/AndroidManifest.xml"

cp -R launcher/app/src/main/java "$ROOT/launcher-lib/src/main/java"
cp -R launcher/app/src/main/res "$ROOT/launcher-lib/src/main/res"
if [ -d launcher/app/src/main/assets ]; then
    cp -R launcher/app/src/main/assets "$ROOT/launcher-lib/src/main/assets"
fi

cp -R shield-overlay/app/src/main/java "$ROOT/shield-lib/src/main/java"
cp -R shield-overlay/app/src/main/res "$ROOT/shield-lib/src/main/res"
if [ -d shield-overlay/app/src/main/assets ]; then
    cp -R shield-overlay/app/src/main/assets "$ROOT/shield-lib/src/main/assets"
fi

# Re-run the Shield dashboard patch against the copied library tree too.
python3 scripts/patch-unified-shield-dashboard.py

# Shield's own CI materializes the approved BOOP eye artwork from the Wall source.
# Do the same here so the unified module uses the identical accepted bitmap.
EYE_ASSET="$(find "$APP/src/main/res" -type f -name 'boop_eyes.png' -print -quit)"
test -n "$EYE_ASSET"
mkdir -p "$ROOT/shield-lib/src/main/res/drawable-nodpi"
cp "$EYE_ASSET" "$ROOT/shield-lib/src/main/res/drawable-nodpi/boop_eyes.png"

# Keep only structural/non-visual build guards here. Ryan owns visual acceptance.
test -s "$APP/src/main/assets/boop-kws/bpe.model"

MAIN="$APP/src/main/java/com/boop/alpha1"
TEST="$APP/src/test/java/com/boop/alpha1"
cp unified/BoopDeviceProfile.java "$MAIN/BoopDeviceProfile.java"
cp unified/ShieldEntryRoute.java "$MAIN/ShieldEntryRoute.java"
cp unified/UnifiedEntryActivity.java "$MAIN/UnifiedEntryActivity.java"
cp unified/UnifiedApplication.java "$MAIN/UnifiedApplication.java"
cp unified/BoopDeviceProfileTest.java "$TEST/BoopDeviceProfileTest.java"
cp unified/ShieldEntryRouteTest.java "$TEST/ShieldEntryRouteTest.java"

cat >> "$ROOT/settings.gradle" <<'EOF'
include ':launcher-lib'
include ':shield-lib'
EOF

python3 - <<'PY'
from pathlib import Path

root = Path('boop-build/BOOP-Alpha1')

# Wall -> Launcher is now an internal activity hop inside one APK.
wall = root / 'app/src/main/java/com/boop/alpha1/MainActivity.java'
text = wall.read_text(encoding='utf-8')
old = 'Intent launcherIntent = getPackageManager().getLaunchIntentForPackage("com.boop.launcher");'
new = 'Intent launcherIntent = new Intent().setClassName(getPackageName(), "com.boop.launcher.MainActivity");'
if text.count(old) != 1:
    raise SystemExit(f'Expected one Wall launcher lookup, found {text.count(old)}')
wall.write_text(text.replace(old, new, 1), encoding='utf-8')

# Launcher -> Wall is also internal. Keep the accepted black-safe no-animation path.
launcher = root / 'launcher-lib/src/main/java/com/boop/launcher/MainActivity.java'
text = launcher.read_text(encoding='utf-8')
old = 'Intent i=getPackageManager().getLaunchIntentForPackage("com.boop.alpha1");if(i==null){Toast.makeText(this,"BOOP Wall is not installed",Toast.LENGTH_SHORT).show();return;}'
new = 'Intent i=new Intent().setClassName(getPackageName(),"com.boop.alpha1.MainActivity");'
if text.count(old) != 1:
    raise SystemExit(f'Expected one Launcher Wall lookup, found {text.count(old)}')
launcher.write_text(text.replace(old, new, 1), encoding='utf-8')

# Do not show BOOP itself in its own All Apps list now that there is one package.
repo = root / 'launcher-lib/src/main/java/com/boop/launcher/AppRepository.java'
text = repo.read_text(encoding='utf-8')
old = 'if(r.activityInfo==null)continue;\n   ComponentName c='
new = 'if(r.activityInfo==null)continue;\n   if("com.boop.alpha1".equals(r.activityInfo.packageName))continue;\n   ComponentName c='
if text.count(old) != 1:
    raise SystemExit(f'Expected one launcher app-list anchor, found {text.count(old)}')
repo.write_text(text.replace(old, new, 1), encoding='utf-8')

# Unified Shield first boot: Nvidia firmware variants do not all expose the
# package-specific overlay-permission screen. Preserve the latest Shield behavior
# but fall back cleanly instead of allowing ActivityNotFoundException to kill BOOP.
shield = root / 'shield-lib/src/main/java/com/boop/shieldoverlay/MainActivity.java'
text = shield.read_text(encoding='utf-8')
old = '''    private void launchOverlayPermission() {\n        permissionScreenLaunched = true;\n        Intent intent = new Intent(\n                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,\n                Uri.parse("package:" + getPackageName()));\n        startActivity(intent);\n    }\n'''
new = '''    private void launchOverlayPermission() {\n        permissionScreenLaunched = true;\n        try {\n            startActivity(new Intent(\n                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,\n                    Uri.parse("package:" + getPackageName())));\n            return;\n        } catch (RuntimeException ignored) {\n            // Some Shield firmware does not expose the package-detail route.\n        }\n        try {\n            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));\n            return;\n        } catch (RuntimeException ignored) {\n            // Last-resort route below.\n        }\n        try {\n            startActivity(new Intent(Settings.ACTION_SETTINGS));\n        } catch (RuntimeException ignored) {\n            permissionScreenLaunched = false;\n            finish();\n        }\n    }\n'''
if text.count(old) != 1:
    raise SystemExit(f'Expected one Shield overlay permission method, found {text.count(old)}')
shield.write_text(text.replace(old, new, 1), encoding='utf-8')

# One exported entry point owns launcher, HOME and Leanback routing.
manifest = root / 'app/src/main/AndroidManifest.xml'
text = manifest.read_text(encoding='utf-8')
app_anchor = '    <application\n        android:allowBackup="false"'
if text.count(app_anchor) != 1:
    raise SystemExit('Could not find Wall application anchor')
text = text.replace(app_anchor, '    <application\n        android:name=".UnifiedApplication"\n        android:allowBackup="false"', 1)
old_filter = '''            <intent-filter>\n                <action android:name="android.intent.action.MAIN" />\n                <category android:name="android.intent.category.LAUNCHER" />\n            </intent-filter>\n'''
if text.count(old_filter) != 1:
    raise SystemExit(f'Expected one Wall launcher filter, found {text.count(old_filter)}')
text = text.replace(old_filter, '', 1)
entry = '''        <activity\n            android:name=".UnifiedEntryActivity"\n            android:exported="true"\n            android:launchMode="singleTask"\n            android:theme="@style/Theme.BOOP">\n            <intent-filter>\n                <action android:name="android.intent.action.MAIN" />\n                <category android:name="android.intent.category.LAUNCHER" />\n                <category android:name="android.intent.category.LEANBACK_LAUNCHER" />\n            </intent-filter>\n            <intent-filter>\n                <action android:name="android.intent.action.MAIN" />\n                <category android:name="android.intent.category.HOME" />\n                <category android:name="android.intent.category.DEFAULT" />\n            </intent-filter>\n        </activity>\n'''
main_anchor = '        <activity\n            android:name=".MainActivity"'
if text.count(main_anchor) != 1:
    raise SystemExit('Could not find Wall MainActivity manifest anchor')
text = text.replace(main_anchor, entry + main_anchor, 1)
manifest.write_text(text, encoding='utf-8')
PY

# Share the locked phone eyes/blink and complete the approved Shield Home wiring.
python3 scripts/patch-unified-shield-presentation.py
