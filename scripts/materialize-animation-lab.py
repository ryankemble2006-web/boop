#!/usr/bin/env python3
"""Turn the materialized BOOP Unified runtime into the standalone animation lab."""
from pathlib import Path
import shutil

ROOT = Path("boop-build/BOOP-Alpha1")
APP = ROOT / "app"
JAVA = APP / "src/main/java/com/boop/alpha1"
BUILD = APP / "build.gradle"
MANIFEST = APP / "src/main/AndroidManifest.xml"
MODEL = JAVA / "BoopDevMenuModel.java"
ACTIVITY = JAVA / "BoopDevMenuActivity.java"
FACE = JAVA / "BoopFaceView.java"
SHIELD_PREVIEW = JAVA / "BoopShieldPreviewView.java"
SHIELD_SOURCE = Path("shield-overlay/app/src/main/java/com/boop/shieldoverlay")
SHIELD_DRAWABLE = Path("shield-overlay/app/src/main/res/drawable-nodpi")
APP_SHIELD_JAVA = APP / "src/main/java/com/boop/shieldoverlay"
APP_DRAWABLE = APP / "src/main/res/drawable-nodpi"

EXPECTED_DOODS = (
    "Facebook", "WhatsApp", "Gmail", "X / Twitter", "YouTube", "Messenger",
    "Instagram", "Discord", "Spotify", "Reddit", "Locked", "Bundle",
)

SHIELD_RUNTIME_ITEMS = (
    ("Shield Groove", "SHIELD_GROOVE"),
    ("Track Change", "SHIELD_TRACK_CHANGE"),
    ("Pause Settle", "SHIELD_PAUSE_SETTLE"),
    ("Cinema Hand", "SHIELD_CINEMA_HAND"),
)

SHIELD_WIP_ITEMS = (
    ("Open Palms", "WIP_OPEN_PALMS"),
    ("Wave", "WIP_WAVE"),
    ("Point", "WIP_POINT"),
    ("Grip", "WIP_GRIP"),
    ("Earcup Adjust", "WIP_EARCUP_ADJUST"),
    ("One-Cup Listen", "WIP_ONE_CUP_LISTEN"),
    ("Gaze + Depth", "WIP_GAZE_DEPTH"),
    ("Headphone Recoil", "WIP_HEADPHONE_RECOIL"),
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
        "application id",
    )
    text = replace_once(text, "versionCode 70", "versionCode 4", "version code")
    text = replace_once(
        text,
        'versionName "1.2.24-unified-dev-menu-doods"',
        'versionName "0.4-animation-lab-all-motion"',
        "version name",
    )
    # Standalone lab: keep no embedded Launcher or normal Shield Android component tree.
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
            android:enableOnBackInvokedCallback="false"
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
''',
        encoding="utf-8",
    )


def materialize_shield_motion() -> None:
    """Copy current Shield motion math/resources, without its services or manifest."""
    APP_SHIELD_JAVA.mkdir(parents=True, exist_ok=True)
    APP_DRAWABLE.mkdir(parents=True, exist_ok=True)

    for name in ("MediaPuppetMotion.java", "FullscreenPuppetMotion.java"):
        source = SHIELD_SOURCE / name
        if not source.is_file():
            raise SystemExit(f"animation lab missing current Shield source: {source}")
        shutil.copy2(source, APP_SHIELD_JAVA / name)

    headphone = SHIELD_DRAWABLE / "boop_headphones.png"
    if not headphone.is_file():
        raise SystemExit("animation lab missing current Shield boop_headphones.png")
    shutil.copy2(headphone, APP_DRAWABLE / "boop_headphones.png")

    bridge = APP_SHIELD_JAVA / "BoopShieldMotionBridge.java"
    bridge.write_text(
        '''package com.boop.shieldoverlay;

/** Public, test-only doorway onto the exact current Shield motion functions. */
public final class BoopShieldMotionBridge {
    private BoopShieldMotionBridge() { }

    public static long groovePeriodMs() {
        return MediaPuppetMotion.MUSIC_PERIOD_MS;
    }

    public static long cinemaPeriodMs() {
        return MediaPuppetMotion.CINEMA_PERIOD_MS;
    }

    public static long trackChangeDurationMs() {
        return FullscreenPuppetMotion.TRACK_CHANGE_DURATION_MS;
    }

    public static long settleDurationMs() {
        return FullscreenPuppetMotion.SETTLE_DURATION_MS;
    }

    public static float[] groove(long elapsedMs) {
        return copy(FullscreenPuppetMotion.groove(elapsedMs));
    }

    public static float[] trackChange(long elapsedMs) {
        return copy(FullscreenPuppetMotion.trackChange(elapsedMs));
    }

    public static float[] settle(long elapsedMs) {
        // Representative non-neutral live pose, then the exact Shield settle curve.
        return copy(FullscreenPuppetMotion.settle(FullscreenPuppetMotion.groove(900L), elapsedMs));
    }

    public static float[] cinema(long elapsedMs) {
        return copy(MediaPuppetMotion.cinema(elapsedMs));
    }

    public static float[] rest() {
        return copy(FullscreenPuppetMotion.rest());
    }

    private static float[] copy(MediaPuppetMotion.Pose pose) {
        return new float[] { pose.x, pose.y, pose.rotationDegrees, pose.kernelAlpha };
    }
}
''',
        encoding="utf-8",
    )


def patch_model() -> None:
    text = MODEL.read_text(encoding="utf-8")
    for dood in EXPECTED_DOODS:
        if f'new Item("{dood}"' not in text:
            raise SystemExit(f"v70 dood missing before lab materialization: {dood}")

    shield_actions = "".join(f"        {action},\n" for _, action in SHIELD_RUNTIME_ITEMS + SHIELD_WIP_ITEMS)
    text = replace_once(
        text,
        "        WAKE,\n        THINK,",
        "        WAKE,\n        IDLE_BLINK,\n        LISTENING,\n        THINK,\n" + shield_actions.rstrip("\n"),
        "animation action enum",
    )

    text = replace_once(
        text,
        '                    new Item("Wake", Action.WAKE),\n                    new Item("Think", Action.THINK),',
        '                    new Item("Wake", Action.WAKE),\n'
        '                    new Item("Idle Blink", Action.IDLE_BLINK),\n'
        '                    new Item("Listening / Reading", Action.LISTENING),\n'
        '                    new Item("Think", Action.THINK),',
        "animation shelf",
    )

    runtime_items = ",\n".join(
        f'                    new Item("{label}", Action.{action})'
        for label, action in SHIELD_RUNTIME_ITEMS
    )
    wip_items = ",\n".join(
        f'                    new Item("{label}", Action.{action})'
        for label, action in SHIELD_WIP_ITEMS
    )
    shield_shelves = (
        '            new Shelf("Shield runtime", List.of(\n'
        + runtime_items
        + ')),\n'
        + '            new Shelf("Shield WIP", List.of(\n'
        + wip_items
        + ')),\n'
    )
    text = replace_once(
        text,
        '            new Shelf("Notification demos", List.of(\n',
        shield_shelves + '            new Shelf("Notification demos", List.of(\n',
        "Shield shelves",
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
        text = replace_once(
            text,
            "    void showIdleBlackImmediately() {\n",
            method + "    void showIdleBlackImmediately() {\n",
            "single blink",
        )
    FACE.write_text(text, encoding="utf-8")


def patch_activity() -> None:
    text = ACTIVITY.read_text(encoding="utf-8")
    text = replace_once(
        text,
        'title.setText("BOOP Dev Lab");',
        'title.setText("BOOP Animation Lab");',
        "title",
    )
    text = replace_once(
        text,
        'subtitle.setText("Local previews only • no Android shade posts");',
        'subtitle.setText("Wall + Shield motion + notification doods • local only");',
        "subtitle",
    )

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
    text = replace_once(text, wake, expanded, "Wall animation dispatch")

    shield_cases = "".join(f"            case {action}:\n" for _, action in SHIELD_RUNTIME_ITEMS + SHIELD_WIP_ITEMS)
    shield_dispatch = shield_cases + '''                showShieldPreview(action);
                return;
'''
    text = replace_once(
        text,
        "            case NOTIFICATION_FACEBOOK:\n",
        shield_dispatch + "            case NOTIFICATION_FACEBOOK:\n",
        "Shield action dispatch",
    )

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
        "stop",
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
        "cancel",
    )

    preview_methods = '''    private void showShieldPreview(BoopDevMenuModel.Action action) {
        cancelActiveAnimation();
        previewShowing = true;
        face = null;
        root.removeAllViews();

        BoopShieldPreviewView preview = new BoopShieldPreviewView(this, shieldMode(action));
        root.addView(preview, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        Button back = new Button(this);
        styleButton(back, "Back to animation lab");
        back.setOnClickListener(v -> showMenu());
        FrameLayout.LayoutParams backParams = new FrameLayout.LayoutParams(
                dp(260), dp(62), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        backParams.bottomMargin = dp(22);
        root.addView(back, backParams);
    }

    private BoopShieldPreviewView.Mode shieldMode(BoopDevMenuModel.Action action) {
        switch (action) {
            case SHIELD_GROOVE: return BoopShieldPreviewView.Mode.SHIELD_GROOVE;
            case SHIELD_TRACK_CHANGE: return BoopShieldPreviewView.Mode.SHIELD_TRACK_CHANGE;
            case SHIELD_PAUSE_SETTLE: return BoopShieldPreviewView.Mode.SHIELD_PAUSE_SETTLE;
            case SHIELD_CINEMA_HAND: return BoopShieldPreviewView.Mode.SHIELD_CINEMA_HAND;
            case WIP_OPEN_PALMS: return BoopShieldPreviewView.Mode.WIP_OPEN_PALMS;
            case WIP_WAVE: return BoopShieldPreviewView.Mode.WIP_WAVE;
            case WIP_POINT: return BoopShieldPreviewView.Mode.WIP_POINT;
            case WIP_GRIP: return BoopShieldPreviewView.Mode.WIP_GRIP;
            case WIP_EARCUP_ADJUST: return BoopShieldPreviewView.Mode.WIP_EARCUP_ADJUST;
            case WIP_ONE_CUP_LISTEN: return BoopShieldPreviewView.Mode.WIP_ONE_CUP_LISTEN;
            case WIP_GAZE_DEPTH: return BoopShieldPreviewView.Mode.WIP_GAZE_DEPTH;
            case WIP_HEADPHONE_RECOIL: return BoopShieldPreviewView.Mode.WIP_HEADPHONE_RECOIL;
            default: throw new IllegalArgumentException("Not a Shield preview action: " + action);
        }
    }

'''
    text = replace_once(
        text,
        "    private void showNotificationPreview(BoopDevMenuModel.Action action) {\n",
        preview_methods + "    private void showNotificationPreview(BoopDevMenuModel.Action action) {\n",
        "Shield preview methods",
    )
    ACTIVITY.write_text(text, encoding="utf-8")


def validate() -> None:
    build = BUILD.read_text(encoding="utf-8")
    manifest = MANIFEST.read_text(encoding="utf-8")
    model = MODEL.read_text(encoding="utf-8")
    activity = ACTIVITY.read_text(encoding="utf-8")
    face = FACE.read_text(encoding="utf-8")
    preview = SHIELD_PREVIEW.read_text(encoding="utf-8")
    bridge = (APP_SHIELD_JAVA / "BoopShieldMotionBridge.java").read_text(encoding="utf-8")

    for token, body in (
        ("com.boop.animationlab", build),
        ("0.4-animation-lab-all-motion", build),
        ("BOOP Animation Lab", manifest),
        ('android:enableOnBackInvokedCallback="false"', manifest),
        ("IDLE_BLINK", model),
        ("LISTENING", model),
        ("playSingleIdleBlink", face),
        ("startListeningCue", activity),
        ("BoopShieldPreviewView", activity),
        ("BoopShieldMotionBridge", preview),
        ("FullscreenPuppetMotion.groove", bridge),
        ("MediaPuppetMotion.cinema", bridge),
        ("androidx.startup.InitializationProvider", manifest),
        ('tools:node="remove"', manifest),
    ):
        if token not in body:
            raise SystemExit(f"animation lab validation missing {token}")

    if "getWindow().getInsetsController()" in activity:
        raise SystemExit("animation lab still uses pre-decor Window.getInsetsController")
    if "decor.getWindowInsetsController()" not in activity:
        raise SystemExit("animation lab missing attached-decor insets controller")
    if not (APP_DRAWABLE / "boop_headphones.png").is_file():
        raise SystemExit("animation lab did not package current Shield headphones")
    for name in ("MediaPuppetMotion.java", "FullscreenPuppetMotion.java"):
        if not (APP_SHIELD_JAVA / name).is_file():
            raise SystemExit(f"animation lab did not copy current Shield motion source: {name}")

    for label, action in SHIELD_RUNTIME_ITEMS + SHIELD_WIP_ITEMS:
        if f'new Item("{label}", Action.{action})' not in model:
            raise SystemExit(f"animation lab missing Shield action: {label}")
    for dood in EXPECTED_DOODS:
        if f'new Item("{dood}"' not in model:
            raise SystemExit(f"animation lab lost v70 dood: {dood}")


if __name__ == "__main__":
    patch_build()
    remove_unneeded_unified_entries()
    patch_manifest()
    materialize_shield_motion()
    patch_model()
    patch_face()
    patch_activity()
    validate()
    print("BOOP Animation Lab v0.4 materialized with all Wall, Shield and WIP motion")
