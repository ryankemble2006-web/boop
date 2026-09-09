#!/usr/bin/env python3
"""Turn the already-materialized BOOP Unified v70 tree into a standalone test APK."""
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
    "Facebook", "WhatsApp", "Gmail", "X / Twitter", "YouTube", "Messenger",
    "Instagram", "Discord", "Spotify", "Reddit", "Locked", "Bundle",
)


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected one anchor, found {count}")
    return text.replace(old, new, 1)


def patch_build() -> None:
    text = BUILD.read_text(encoding="utf-8")
    text = replace_once(text, "applicationId 'com.boop.alpha1'", "applicationId 'com.boop.animationlab'", "application id")
    text = replace_once(text, "versionCode 70", "versionCode 2", "version code")
    text = replace_once(text, 'versionName "1.2.24-unified-dev-menu-doods"', 'versionName "0.2-animation-lab-pixel-launch"', "version name")
    text = text.replace("    implementation project(':launcher-lib')\n", "")
    text = text.replace("    implementation project(':shield-lib')\n", "")
    BUILD.write_text(text, encoding="utf-8")


def remove_unneeded_unified_entries() -> None:
    for name in ("UnifiedApplication.java", "UnifiedEntryActivity.java"):
        path = JAVA / name
        if path.exists():
            path.unlink()


def patch_manifest() -> None:
    MANIFEST.write_text(
        '''<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
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
        <provider
            android:name="androidx.startup.InitializationProvider"
            android:authorities="${applicationId}.androidx-startup"
            tools:node="remove" />
        <receiver
            android:name="androidx.profileinstaller.ProfileInstallReceiver"
            tools:node="remove" />
    </application>
</manifest>
''', encoding="utf-8")


def patch_model() -> None:
    text = MODEL.read_text(encoding="utf-8")
    for dood in EXPECTED_DOODS:
        if f'new Item("{dood}"' not in text:
            raise SystemExit(f"v70 dood missing: {dood}")
    text = replace_once(text, "        WAKE,\n        THINK,", "        WAKE,\n        IDLE_BLINK,\n        LISTENING,\n        THINK,", "action enum")
    text = replace_once(
        text,
        '                    new Item("Wake", Action.WAKE),\n                    new Item("Think", Action.THINK),',
        '                    new Item("Wake", Action.WAKE),\n'
        '                    new Item("Idle Blink", Action.IDLE_BLINK),\n'
        '                    new Item("Listening / Reading", Action.LISTENING),\n'
        '                    new Item("Think", Action.THINK),',
        "animation shelf",
    )
    MODEL.write_text(text, encoding="utf-8")


def patch_face() -> None:
    text = FACE.read_text(encoding="utf-8")
    if "BOOP_ANIMATION_LAB_SINGLE_BLINK_V1" not in text:
        if "void startListeningCue()" not in text or "BoopIdleBlink.DURATION_MS" not in text:
            raise SystemExit("finished Unified listening/blink face missing")
        method = '''    // BOOP_ANIMATION_LAB_SINGLE_BLINK_V1
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
        text = replace_once(text, "    void showIdleBlackImmediately() {\n", method + "    void showIdleBlackImmediately() {\n", "single blink")
    FACE.write_text(text, encoding="utf-8")


def patch_activity() -> None:
    text = ACTIVITY.read_text(encoding="utf-8")
    text = replace_once(text, 'title.setText("BOOP Dev Lab");', 'title.setText("BOOP Animation Lab");', "title")
    text = replace_once(text, 'subtitle.setText("Local previews only • no Android shade posts");', 'subtitle.setText("Real v70 animations + notification doods • local only");', "subtitle")

    wake = '''            case WAKE:
                cancelActiveAnimation();
                if (face != null) {
                    face.showIdleBlackImmediately();
                    face.wakeFromIdle();
                }
                return;
'''
    expanded = wake + '''            case IDLE_BLINK: {
                cancelActiveAnimation();
                BoopFaceView activeFace = face;
                if (activeFace != null) {
                    activeFace.wakeFromIdle();
                    activeFace.postDelayed(() -> { if (face == activeFace) activeFace.playSingleIdleBlink(); }, 430L);
                }
                return;
            }
            case LISTENING: {
                cancelActiveAnimation();
                BoopFaceView activeFace = face;
                if (activeFace != null) {
                    activeFace.wakeFromIdle();
                    activeFace.postDelayed(() -> { if (face == activeFace) activeFace.startListeningCue(); }, 430L);
                }
                return;
            }
'''
    text = replace_once(text, wake, expanded, "dispatch")
    text = replace_once(text, '''            case STOP:
                cancelActiveAnimation();
                return;
''', '''            case STOP:
                cancelActiveAnimation();
                if (face != null) face.wakeFromIdle();
                return;
''', "stop")
    text = replace_once(text, '''    private void cancelActiveAnimation() {
        if (face != null) face.stopThinking();
    }
''', '''    private void cancelActiveAnimation() {
        if (face != null) {
            face.stopListeningCue();
            face.stopThinking();
        }
    }
''', "cancel")

    # BOOP_ANIMATION_LAB_LAUNCH_GUARD_V2: Pixel/Android 16 launch must never die silently.
    old_create = '''    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyImmersiveUi();
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        setContentView(root);
        showMenu();
    }
'''
    new_create = '''    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        try {
            applyImmersiveUi();
            setContentView(root);
            showMenu();
        } catch (Throwable launchFailure) {
            android.util.Log.e("BOOP-Animation-Lab", "BOOP_ANIMATION_LAB_LAUNCH_GUARD_V2", launchFailure);
            root.removeAllViews();
            TextView failure = new TextView(this);
            failure.setTextColor(Color.WHITE);
            failure.setTextSize(18f);
            failure.setPadding(dp(24), dp(24), dp(24), dp(24));
            failure.setText("BOOP Animation Lab launch fault\\n\\n"
                    + launchFailure.getClass().getSimpleName() + ": "
                    + String.valueOf(launchFailure.getMessage()));
            root.addView(failure, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            setContentView(root);
        }
    }
'''
    text = replace_once(text, old_create, new_create, "Pixel launch guard")
    ACTIVITY.write_text(text, encoding="utf-8")


def validate() -> None:
    build = BUILD.read_text(encoding="utf-8")
    manifest = MANIFEST.read_text(encoding="utf-8")
    model = MODEL.read_text(encoding="utf-8")
    activity = ACTIVITY.read_text(encoding="utf-8")
    face = FACE.read_text(encoding="utf-8")
    for token, body in (
        ("com.boop.animationlab", build), ("BOOP Animation Lab", manifest),
        ("IDLE_BLINK", model), ("LISTENING", model), ("playSingleIdleBlink", face),
        ("startListeningCue", activity), ("BOOP_ANIMATION_LAB_LAUNCH_GUARD_V2", activity),
        ("androidx.startup.InitializationProvider", manifest), ("tools:node=\"remove\"", manifest),
    ):
        if token not in body:
            raise SystemExit(f"animation lab validation missing {token}")
    for dood in EXPECTED_DOODS:
        if f'new Item("{dood}"' not in model:
            raise SystemExit(f"animation lab lost v70 dood: {dood}")


if __name__ == "__main__":
    patch_build()
    remove_unneeded_unified_entries()
    patch_manifest()
    patch_model()
    patch_face()
    patch_activity()
    validate()
    print("BOOP Animation Lab v0.2 materialized with Pixel launch guard")
