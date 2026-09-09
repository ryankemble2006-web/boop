"""Non-visual integrity and runtime-plumbing contracts for Ryan's approved BOOP eyes."""
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


def _method_body(text: str, signature: str) -> str:
    start = text.index(signature)
    opening = text.index("{", start)
    depth = 1
    pos = opening + 1
    while depth and pos < len(text):
        depth += (text[pos] == "{") - (text[pos] == "}")
        pos += 1
    assert depth == 0, f"unclosed method for {signature}"
    return text[opening + 1 : pos - 1]


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
    dashboard_patch = Path("scripts/patch-unified-shield-dashboard.py").read_text()
    geometry_patch = Path("scripts/patch-approved-eye-geometry.py").read_text()
    assert 'cp "$EYE_ASSET" "$ROOT/shield-lib/src/main/res/drawable-nodpi/boop_eyes.png"' in materializer
    assert "BoopApprovedEyeGeometry.java" in shield_patch
    assert "faceBitmap = BitmapFactory.decodeResource" in shield_patch
    assert "BoopApprovedEyeGeometry.LEFT_SOURCE" in geometry_patch
    assert "BoopApprovedEyeGeometry.RIGHT_SOURCE" in geometry_patch
    assert "make-locked-eyes-transparent.py" not in shield_patch
    assert "make-locked-eyes-transparent.py" not in dashboard_patch


def test_canonical_materialization_includes_finished_v65_procedural_eye_stack():
    materialize = Path("scripts/materialize-unified.sh").read_text(encoding="utf-8")
    reading = Path("scripts/patch-unified-reading-eyes.py").read_text(encoding="utf-8")

    reading_step = "python3 scripts/patch-unified-reading-eyes.py"
    clean_step = "python3 scripts/patch-v64-procedural-sclera.py"
    feather_step = "python3 scripts/patch-v65-feathered-sclera.py"

    assert reading_step in materialize
    assert clean_step in materialize
    assert feather_step in materialize
    assert materialize.index(reading_step) < materialize.index(clean_step) < materialize.index(feather_step)
    assert "BOOP_PROCEDURAL_IRISES_V3" in reading
    assert "drawProceduralIris(" in reading
    assert "buildProceduralEyeBase(" in reading


def test_eye_colour_control_drives_procedural_iris_state_only():
    reading = Path("scripts/patch-unified-reading-eyes.py").read_text(encoding="utf-8")
    setter = _method_body(reading, "void setEyeHueDegrees(int hueDegrees)")
    colourer = _method_body(reading, "private int irisColour(float saturation, float value)")

    assert "proceduralIrisHueDegrees = BoopEyeHueMath.clampHue(hueDegrees);" in setter
    assert "faceBitmap = recoloured" not in setter
    assert "setColorFilter(new" not in setter
    assert "android.graphics.Color.HSVToColor(new float[] {" in colourer
    assert "proceduralIrisHueDegrees, saturation, value" in colourer
