"""Non-visual integrity contract for Ryan's approved BOOP eye master."""
from pathlib import Path
import hashlib

ROOT_MASTER = Path("boopApprovedEyes.png")
CANONICAL_MASTER = Path("unified/assets/boop-eyes/boopApprovedEyes.png")
EXPECTED_SHA256 = "ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22"
EXPECTED_SIZE = 936803


def _assert_exact_master(path: Path):
    data = path.read_bytes()
    assert len(data) == EXPECTED_SIZE
    assert hashlib.sha256(data).hexdigest() == EXPECTED_SHA256


def test_approved_eye_master_is_exact_uploaded_png_and_canonical_copy():
    _assert_exact_master(ROOT_MASTER)
    _assert_exact_master(CANONICAL_MASTER)
    assert ROOT_MASTER.read_bytes() == CANONICAL_MASTER.read_bytes()


def test_wall_materializer_copies_master_directly_without_alpha_rebuild():
    materializer = Path("scripts/materialize-android.sh").read_text()
    assert 'APPROVED_EYES="unified/assets/boop-eyes/boopApprovedEyes.png"' in materializer
    assert 'cp "$APPROVED_EYES" "$ROOT/app/src/main/res/drawable-nodpi/boop_eyes.png"' in materializer
    assert "patch-approved-eye-geometry.py" in materializer
    assert "patch-unified-iris-cache.py" in materializer
    assert "make-locked-eyes-transparent.py" not in materializer


def test_unified_shield_reuses_materialized_master_and_shared_geometry():
    materializer = Path("scripts/materialize-unified.sh").read_text()
    shield_patch = Path("scripts/patch-unified-shield-presentation.py").read_text()
    geometry_patch = Path("scripts/patch-approved-eye-geometry.py").read_text()
    assert 'cp "$EYE_ASSET" "$ROOT/shield-lib/src/main/res/drawable-nodpi/boop_eyes.png"' in materializer
    assert "BoopApprovedEyeGeometry.java" in shield_patch
    assert "faceBitmap = BitmapFactory.decodeResource" in shield_patch
    assert "BoopApprovedEyeGeometry.LEFT_SOURCE" in geometry_patch
    assert "BoopApprovedEyeGeometry.RIGHT_SOURCE" in geometry_patch
    assert "make-locked-eyes-transparent.py" not in shield_patch
