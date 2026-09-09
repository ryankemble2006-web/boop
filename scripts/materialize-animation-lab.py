#!/usr/bin/env python3
"""Turn the already-materialized BOOP Unified v70 tree into a standalone test APK.

This runs only after scripts/materialize-unified.sh. It deliberately reuses the
finished BOOP face, animation and notification-preview implementation, then gives
that generated app a separate package and one launcher activity: BOOP Animation Lab.
"""
from pathlib import Path

ROOT = Path("boop-build/BOOP-Alpha1")
APP = ROOT / "app"
JAVA = APP / "src/main/java/com/boop/alpha1"
BUILD = APP / "build.gradle"
MANIFEST = APP / "src/main/AndroidManifest.xml"
MODEL = JAVA / "BoopDevMenuModel.java"
ACTIVITY = JAVA / "BoopDevMenuActivity.java"
FACE = JAVA / "BoopFaceView.java"

EXPECTED_DOODS = (
    "Facebook",
    "WhatsApp",
    "Gmail",
    "X / Twitter",
    "YouTube",
    "Messenger",
    "Instagram",
    "Discord",
    "Spotify",
    "Reddit",
    "Locked",
    "Bundle",
)


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected one anchor, found {count}")
    return text.replace(old, new, 1)


def patch_build() -> None:
    text = BUILD.read_text(encoding="utf-8")
    text = replace_once(
        text,
        "applicationId 'com.boop.alpha1'",
        "applicationId 'com.boop.animationlab'",
        "animation lab application id",
    )
    text = replace_once(text, "versionCode 70", "versionCode 1", "animation lab version code")
    text = replace_once(
        text,
        'versionName "1.2.24-unified-dev-menu-doods"',
        'versionName "0.1-animation-lab-v70"',
        "animation lab version name",
    )

    # The standalone lab needs the Wall classes/resources only. Dropping the two
    # embedded app libraries also prevents their Android components being merged.
    text = text.replace("    implementation project(':launcher-lib')\n", "")
    text = text.replace("    implementation project(':shield-lib')\n", "")
    BUILD.write_text(text, encoding="utf-8")


def patch_manifest() -> None:
    MANIFEST.write_text(
        '''<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:allowBackup="false"
        android:icon="@drawable/boop_eyes"
        android:label="BOOP Animation Lab"
        android:supportsRtl="true"
        android:theme="@style/Theme.BOOP">
        <activity
            android:name=".BoopDevMenuActivity"
            android:configChanges="keyboardHidden|orientation|screenSize"
            android:exported="true"
            android:launchMode="singleTask">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
''',
        encoding="utf-8",
    )


def patch_model() -> None:
    text = MODEL.read_text(encoding="utf-8")
    for dood in EXPECTED_DOODS:
        if f'new Item("{dood}"' not in text:
            raise SystemExit(f"animation lab: v70 dood missing before materialization: {dood}")

    text = replace_once(
        text,
        "        WAKE,\n        THINK,",
        "        WAKE,\n        IDLE_BLINK,\n        LISTENING,\n        THINK,",
        "animation action enum",
    )
    text = replace_once(
        text,
        '                    new Item("Wake", Action.WAKE),\n                    new Item("Think", Action.THINK),',
        '                    new Item("Wake", Action.WAKE),\n'
        '                    new Item("Idle Blink", Action.IDLE_BLINK),\n'
        '                    new Item("Listening / Reading", Action.LISTENING),\n'
        '                    new Item("Think", Action.THINK),',
        "animation shelf items",
    )
    MODEL.write_text(text, encoding="utf-8")


def patch_face() -> None:
    text = FACE.read_text(encoding="utf-8")
    if "BOOP_ANIMATION_LAB_SINGLE_BLINK_V1" in text:
        return
    if "void startListeningCue()" not in text or "BoopIdleBlink.DURATION_MS" not in text:
        raise SystemExit("animation lab requires the finished Unified listening/blink face")

    method = '''    // BOOP_ANIMATION_LAB_SINGLE_BLINK_V1: explicit one-shot access to the real blink geometry.
    void playSingleIdleBlink() {
        stopListeningCue();
        stopThinking();
        animate().cancel();
        setAlpha(1f);
        cancelIdleBlinkFrame();
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        idleBlinkAnimator = animator;
        animator.setDuration(BoopIdleBlink.DURATION_MS);
        animator.setInterpolator(new android.view.animation.LinearInterpolator());
        animator.addUpdateListener(frame -> {
            if (idleBlinkAnimator != frame) return;
            idleBlinkOpenness = BoopIdleBlink.openness((float) frame.getAnimatedValue());
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator finished) {
                if (idleBlinkAnimator != finished) return;
                idleBlinkAnimator = null;
                idleBlinkOpenness = 1f;
                invalidate();
            }
        });
        animator.start();
    }

'''
    text = replace_once(
        text,
        "    void showIdleBlackImmediately() {\n",
        method + "    void showIdleBlackImmediately() {\n",
        "single idle blink insertion",
    )
    FACE.write_text(text, encoding="utf-8")


def patch_activity() -> None:
    text = ACTIVITY.read_text(encoding="utf-8")
    text = replace_once(text, 'title.setText("BOOP Dev Lab");', 'title.setText("BOOP Animation Lab");', "lab title")
    text = replace_once(
        text,
        'subtitle.setText("Local previews only • no Android shade posts");',
        'subtitle.setText("Real v70 animations + notification doods • local only");',
        "lab subtitle",
    )

    wake_block = '''            case WAKE:
                cancelActiveAnimation();
                if (face != null) {
                    face.showIdleBlackImmediately();
                    face.wakeFromIdle();
                }
                return;
'''
    expanded = wake_block + '''            case IDLE_BLINK: {
                cancelActiveAnimation();
                BoopFaceView activeFace = face;
                if (activeFace != null) {
                    activeFace.wakeFromIdle();
                    activeFace.postDelayed(() -> {
                        if (face == activeFace) activeFace.playSingleIdleBlink();
                    }, 430L);
                }
                return;
            }
            case LISTENING: {
                cancelActiveAnimation();
                BoopFaceView activeFace = face;
                if (activeFace != null) {
                    activeFace.wakeFromIdle();
                    activeFace.postDelayed(() -> {
                        if (face == activeFace) activeFace.startListeningCue();
                    }, 430L);
                }
                return;
            }
'''
    text = replace_once(text, wake_block, expanded, "animation lab dispatch")

    text = replace_once(
        text,
        '''            case STOP:
                cancelActiveAnimation();
                return;
''',
        '''            case STOP:
                cancelActiveAnimation();
                if (face != null) face.wakeFromIdle();
                return;
''',
        "stop reset action",
    )
    text = replace_once(
        text,
        '''    private void cancelActiveAnimation() {
        if (face != null) face.stopThinking();
    }
''',
        '''    private void cancelActiveAnimation() {
        if (face != null) {
            face.stopListeningCue();
            face.stopThinking();
        }
    }
''',
        "lab animation cancellation",
    )
    ACTIVITY.write_text(text, encoding="utf-8")


def validate() -> None:
    build = BUILD.read_text(encoding="utf-8")
    manifest = MANIFEST.read_text(encoding="utf-8")
    model = MODEL.read_text(encoding="utf-8")
    activity = ACTIVITY.read_text(encoding="utf-8")
    face = FACE.read_text(encoding="utf-8")

    required = (
        ("com.boop.animationlab", build),
        ("BOOP Animation Lab", manifest),
        ("IDLE_BLINK", model),
        ("LISTENING", model),
        ("playSingleIdleBlink", face),
        ("startListeningCue", activity),
        ("BoopDevMenuActivity", manifest),
    )
    for token, text in required:
        if token not in text:
            raise SystemExit(f"animation lab validation missing {token}")
    for dood in EXPECTED_DOODS:
        if f'new Item("{dood}"' not in model:
            raise SystemExit(f"animation lab lost v70 dood: {dood}")


if __name__ == "__main__":
    patch_build()
    patch_manifest()
    patch_model()
    patch_face()
    patch_activity()
    validate()
    print("BOOP Animation Lab materialized from the finished Unified v70 runtime")
