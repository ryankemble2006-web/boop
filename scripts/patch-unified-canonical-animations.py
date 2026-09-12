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

menu = MAIN / "BoopDevMenuActivity.java"
menu_text = menu.read_text(encoding="utf-8")
if "EyeCatalogue.ALL" not in menu_text:
    model = MAIN / "BoopDevMenuModel.java"
    text = model.read_text(encoding="utf-8")
    if "CANONICAL_ANIMATIONS" not in text:
        text = replace_once(text, "        WAKE,\n", "        CANONICAL_ANIMATIONS,\n        WAKE,\n", "dev menu enum")
        text = replace_once(text, '            new Shelf("Animations", List.of(\n',
                '            new Shelf("Animations", List.of(\n                    new Item("Canonical set", Action.CANONICAL_ANIMATIONS),\n',
                "dev menu canonical item")
        model.write_text(text, encoding="utf-8")
else:
    print("Developer menu already uses canonical catalogue directly")

# Final production swap: older materialization patches may consume BoopFaceView,
# but the built APK uses the canonical Animation Lab-backed face.
wall = MAIN / "MainActivity.java"
text = wall.read_text(encoding="utf-8")
text = replace_once(text, "    private BoopFaceView face;",
                    "    private BoopCanonicalFaceView face;", "canonical Wall face field")
text = replace_once(text, "        face = new BoopFaceView(this);",
                    "        face = new BoopCanonicalFaceView(this);", "canonical Wall face constructor")
# Spoken/settings entry is in MainActivity, not the standalone preview activity.
text = replace_once(text, "    private BoopFaceView developerMenuFace;",
        "    private BoopCanonicalFaceView developerMenuFace;", "in-place developer face field")
old_dev = "developerMenuFace = new BoopFaceView(this);"
if text.count(old_dev) != 2:
    raise SystemExit("Expected both in-place developer face constructors")
text = text.replace(old_dev, "developerMenuFace = new BoopCanonicalFaceView(this);")
shelf_anchor = "        for (BoopDevMenuModel.Shelf shelf : BoopDevMenuModel.shelves()) {"
text = replace_once(text, shelf_anchor,
        '        addCanonicalDeveloperShelf(column);\n\n' + shelf_anchor, "in-place canonical shelf")
canonical_shelf = '''
    private void addCanonicalDeveloperShelf(LinearLayout column) {
        addDeveloperSection(column, "Canonical animations");
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setContentDescription("Canonical animations horizontal selector");
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (com.boop.eyes.EyeMotion.Clip clip : com.boop.eyes.EyeCatalogue.ALL) {
            Button button = new Button(this);
            button.setText(clip.label); button.setAllCaps(false);
            button.setTextSize(18f); button.setTextColor(Color.WHITE);
            button.setBackgroundColor(Color.rgb(42, 42, 42));
            button.setContentDescription(clip.label + " canonical animation");
            button.setOnClickListener(v -> {
                if (developerMenuFace != null) developerMenuFace.playCanonicalClip(clip.id);
            });
            LinearLayout.LayoutParams item = new LinearLayout.LayoutParams(dp(180), dp(64));
            item.setMargins(0, 0, dp(12), 0); row.addView(button, item);
        }
        scroll.addView(row);
        scroll.setOnScrollChangeListener((v, x, y, ox, oy) -> developerAnimationScrollX = x);
        int saved = developerAnimationScrollX;
        scroll.post(() -> scroll.scrollTo(saved, 0));
        column.addView(scroll, new LinearLayout.LayoutParams(-1, dp(70)));
    }

'''
text = replace_once(text, "    private void addDeveloperShelf(",
        canonical_shelf + "    private void addDeveloperShelf(", "canonical shelf method")

wall.write_text(text, encoding="utf-8")

hue = MAIN / "BoopEyeHueOverlay.java"
text = hue.read_text(encoding="utf-8")
count = text.count("BoopFaceView")
if count != 3:
    raise SystemExit(f"canonical hue overlay face type: expected 3, found {count}")
text = text.replace("BoopFaceView", "BoopCanonicalFaceView")
hue.write_text(text, encoding="utf-8")

notice = MAIN / "BoopNotificationPuppetView.java"
text = notice.read_text(encoding="utf-8")
text = replace_once(text, "    private final BoopFaceView faceView;",
                    "    private final BoopCanonicalFaceView faceView;", "canonical notification face field")
text = replace_once(text, "        faceView = new BoopFaceView(context);",
                    "        faceView = new BoopCanonicalFaceView(context);", "canonical notification face constructor")
old_entrance = '''        faceView.post(() -> {
            faceView.showIdleBlackImmediately();
            faceView.wakeFromIdle();
        });
'''
new_entrance = '''        faceView.post(() -> {
            faceView.showIdleBlackImmediately();
            faceView.playNotification();
        });
'''
text = replace_once(text, old_entrance, new_entrance, "canonical notification clip")
notice.write_text(text, encoding="utf-8")

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
