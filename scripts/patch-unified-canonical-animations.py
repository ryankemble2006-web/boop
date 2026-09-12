#!/usr/bin/env python3
from pathlib import Path
import hashlib
import shutil

ROOT = Path("boop-build/BOOP-Alpha1")
APP = ROOT / "app"
MAIN = APP / "src/main/java/com/boop/alpha1"
LIB = ROOT / "animation-lib"
JAVA = LIB / "src/main/java/com/boop/eyes"
ASSETS = LIB / "src/main/assets"
ANIM = Path("unified/animation")
MASTER = Path("unified/assets/boop-eyes/boopApprovedEyes.png")
HANDS = Path("unified/assets/boop-notifications/boop-yellow-hands-approved.png")
MASTER_SHA = "ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22"
HANDS_SHA = "26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1"


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def replace_once(text, old, new, label):
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected one anchor, found {count}")
    return text.replace(old, new, 1)

if digest(MASTER) != MASTER_SHA:
    raise SystemExit("Locked BOOP eye master changed")
if digest(HANDS) != HANDS_SHA:
    raise SystemExit("Locked BOOP notification hands changed")

shutil.rmtree(LIB, ignore_errors=True)
JAVA.mkdir(parents=True, exist_ok=True)
ASSETS.mkdir(parents=True, exist_ok=True)
shutil.copy2("unified/animation-lib.gradle", LIB / "build.gradle")
manifest_dir = LIB / "src/main"
manifest_dir.mkdir(parents=True, exist_ok=True)
(manifest_dir / "AndroidManifest.xml").write_text(
    '<manifest xmlns:android="http://schemas.android.com/apk/res/android" />\n',
    encoding="utf-8")

for source in (ANIM / "java/com/boop/eyes").glob("*.java"):
    shutil.copy2(source, JAVA / source.name)
for source in (ANIM / "assets").iterdir():
    if source.is_file():
        shutil.copy2(source, ASSETS / source.name)
shutil.copy2(MASTER, ASSETS / "boopApprovedEyes.png")
shutil.copy2(HANDS, ASSETS / "boop-notification-hands.png")

settings = ROOT / "settings.gradle"
settings_text = settings.read_text(encoding="utf-8")
if "include ':animation-lib'" not in settings_text:
    with settings.open("a", encoding="utf-8") as handle:
        handle.write("\ninclude ':animation-lib'\n")

if digest(ASSETS / "boopApprovedEyes.png") != MASTER_SHA:
    raise SystemExit("Materialized eye master changed")
if digest(ASSETS / "boop-notification-hands.png") != HANDS_SHA:
    raise SystemExit("Materialized hand master changed")

manifest = APP / "src/main/AndroidManifest.xml"
text = manifest.read_text(encoding="utf-8")
activity = '''        <activity
            android:name=".BoopCanonicalAnimationActivity"
            android:exported="false" />
'''
if ".BoopCanonicalAnimationActivity" not in text:
    anchor = '''        <activity
            android:name=".BoopDevMenuActivity"
            android:exported="false" />
'''
    text = replace_once(text, anchor, anchor + activity, "canonical activity manifest")
    manifest.write_text(text, encoding="utf-8")

model = MAIN / "BoopDevMenuModel.java"
text = model.read_text(encoding="utf-8")
if "CANONICAL_ANIMATIONS" not in text:
    text = replace_once(text, "        WAKE,\n", "        CANONICAL_ANIMATIONS,\n        WAKE,\n", "dev menu enum")
    text = replace_once(
        text,
        '            new Shelf("Animations", List.of(\n',
        '            new Shelf("Animations", List.of(\n                    new Item("Canonical set", Action.CANONICAL_ANIMATIONS),\n',
        "dev menu canonical item")
    model.write_text(text, encoding="utf-8")

menu = MAIN / "BoopDevMenuActivity.java"
text = menu.read_text(encoding="utf-8")
if "case CANONICAL_ANIMATIONS:" not in text:
    anchor = '''        switch (action) {
            case WAKE:
'''
    block = '''        switch (action) {
            case CANONICAL_ANIMATIONS:
                startActivity(new android.content.Intent(
                        this, BoopCanonicalAnimationActivity.class));
                return;
            case WAKE:
'''
    text = replace_once(text, anchor, block, "dev menu canonical route")
    menu.write_text(text, encoding="utf-8")

menu_test = APP / "src/test/java/com/boop/alpha1/BoopNotificationDevMenuModelTest.java"
if menu_test.is_file():
    text = menu_test.read_text(encoding="utf-8")
    if "BoopDevMenuModel.Action.CANONICAL_ANIMATIONS" not in text:
        text = replace_once(
            text,
            "                List.of(\n                        BoopDevMenuModel.Action.WAKE,\n",
            "                List.of(\n                        BoopDevMenuModel.Action.CANONICAL_ANIMATIONS,\n                        BoopDevMenuModel.Action.WAKE,\n",
            "dev menu unit-test canonical item")
        menu_test.write_text(text, encoding="utf-8")

required = [
    JAVA / "EyeMotion.java",
    JAVA / "EyeCatalogue.java",
    JAVA / "ProductionAnimationController.java",
    JAVA / "CanonicalEyeRenderer.java",
    JAVA / "SignMotion.java",
    JAVA / "FreddieMotion.java",
    JAVA / "NotificationSignView.java",
    ASSETS / "catalogue.json",
    ASSETS / "eyes.frag",
    ASSETS / "eyes.vert",
    ASSETS / "lid-rig.png",
]
for path in required:
    if not path.is_file() or path.stat().st_size == 0:
        raise SystemExit(f"Missing canonical animation runtime: {path}")
print("Canonical production animation library embedded in Unified; visual acceptance remains manual")
