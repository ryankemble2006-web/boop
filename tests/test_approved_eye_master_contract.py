"""Non-visual integrity contract for Ryan's approved BOOP eye master."""
from pathlib import Path
import hashlib

MASTER = Path("boopApprovedEyes.png")
EXPECTED_SHA256 = "ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22"
EXPECTED_SIZE = 936803


def test_approved_eye_master_is_exact_uploaded_png():
    data = MASTER.read_bytes()
    assert len(data) == EXPECTED_SIZE
    assert hashlib.sha256(data).hexdigest() == EXPECTED_SHA256


def test_unified_materializer_uses_master_directly_for_wall_and_shield():
    materializer = Path("scripts/materialize-unified.sh").read_text()
    assert 'APPROVED_EYES="boopApprovedEyes.png"' in materializer
    assert 'cp "$APPROVED_EYES" "$APP/src/main/res/drawable-nodpi/boop_eyes.png"' in materializer
    assert 'cp "$APPROVED_EYES" "$ROOT/shield-lib/src/main/res/drawable-nodpi/boop_eyes.png"' in materializer
    assert "make-locked-eyes-transparent.py" not in materializer


def test_approved_eye_geometry_is_shared_by_wall_and_shield():
    geometry = Path("source/BoopApprovedEyeGeometry.java").read_text()
    face = Path("source/BoopFaceView.java").read_text()
    shield_patch = Path("scripts/patch-unified-shield-presentation.py").read_text()
    assert "LEFT_SOURCE = new Rect(102, 60, 825, 828)" in geometry
    assert "RIGHT_SOURCE = new Rect(947, 60, 1671, 828)" in geometry
    assert "BoopApprovedEyeGeometry.LEFT_SOURCE" in face
    assert "BoopApprovedEyeGeometry.RIGHT_SOURCE" in face
    assert "BoopApprovedEyeGeometry.java" in shield_patch
