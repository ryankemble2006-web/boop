from pathlib import Path
import hashlib

ROOT = Path(__file__).resolve().parents[1]
BUILD = ROOT / "boop-build/BOOP-Alpha1"
MASTER = ROOT / "unified/assets/boop-eyes/boopApprovedEyes.png"
HANDS = ROOT / "unified/assets/boop-notifications/boop-yellow-hands-approved.png"
ANIMATION = BUILD / "animation-lib"


def test_materialized_canonical_animation_runtime_is_one_shared_library():
    eyes = ANIMATION / "src/main/java/com/boop/eyes/EyeMotion.java"
    catalogue = ANIMATION / "src/main/java/com/boop/eyes/EyeCatalogue.java"
    controller = ANIMATION / "src/main/java/com/boop/eyes/ProductionAnimationController.java"
    activity = BUILD / "app/src/main/java/com/boop/alpha1/BoopCanonicalAnimationActivity.java"
    settings = (BUILD / "settings.gradle").read_text()
    app_gradle = (BUILD / "app/build.gradle").read_text()
    home_gradle = (BUILD / "shield-home-lib/build.gradle").read_text()
    assert eyes.is_file() and catalogue.is_file() and controller.is_file() and activity.is_file()
    assert "include ':animation-lib'" in settings
    assert "implementation project(':animation-lib')" in app_gradle
    assert "implementation project(':animation-lib')" in home_gradle
    assert not (BUILD / "app/src/main/java/com/boop/eyes/EyeMotion.java").exists()


def test_locked_assets_are_copied_byte_identically_into_shared_animation_library():
    runtime_master = ANIMATION / "src/main/assets/boopApprovedEyes.png"
    runtime_hands = ANIMATION / "src/main/assets/boop-notification-hands.png"
    assert runtime_master.read_bytes() == MASTER.read_bytes()
    assert runtime_hands.read_bytes() == HANDS.read_bytes()
    assert hashlib.sha256(runtime_master.read_bytes()).hexdigest() == (
        "ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22"
    )
    assert hashlib.sha256(runtime_hands.read_bytes()).hexdigest() == (
        "26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1"
    )


def test_canonical_preview_activity_remains_private_in_unified_manifest():
    manifest = (BUILD / "app/src/main/AndroidManifest.xml").read_text()
    assert ".BoopCanonicalAnimationActivity" in manifest
    assert 'android:name=".BoopCanonicalAnimationActivity"' in manifest


def test_preview_activity_uses_the_same_production_controller():
    source = (ROOT / "source/BoopCanonicalAnimationActivity.java").read_text()
    assert "ProductionAnimationController" in source
    assert "new EyeMotion.Controller" not in source
