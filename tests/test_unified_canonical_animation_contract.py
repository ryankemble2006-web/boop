from pathlib import Path
import hashlib

ROOT = Path(__file__).resolve().parents[1]
BUILD = ROOT / "boop-build/BOOP-Alpha1"
MASTER = ROOT / "unified/assets/boop-eyes/boopApprovedEyes.png"


def test_materialized_canonical_animation_runtime_contract():
    java = BUILD / "app/src/main/java/com/boop/eyes/EyeMotion.java"
    catalogue = BUILD / "app/src/main/java/com/boop/eyes/EyeCatalogue.java"
    activity = BUILD / "app/src/main/java/com/boop/alpha1/BoopCanonicalAnimationActivity.java"
    manifest = (BUILD / "app/src/main/AndroidManifest.xml").read_text()
    assert java.is_file() and catalogue.is_file() and activity.is_file()
    assert ".BoopCanonicalAnimationActivity" in manifest


def test_locked_master_is_copied_byte_identically_for_animation_runtime():
    runtime = BUILD / "app/src/main/assets/boopApprovedEyes.png"
    assert runtime.read_bytes() == MASTER.read_bytes()
    assert hashlib.sha256(runtime.read_bytes()).hexdigest() == (
        "ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22"
    )
