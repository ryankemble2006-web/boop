from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]


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


def test_canonical_eye_stack_uses_finished_procedural_renderer_and_sclera_patches():
    materialize = (ROOT / "scripts/materialize-unified.sh").read_text(encoding="utf-8")
    reading = (ROOT / "scripts/patch-unified-reading-eyes.py").read_text(encoding="utf-8")

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


def test_colour_changer_drives_procedural_iris_hue_without_recolouring_the_eye_bitmap():
    reading = (ROOT / "scripts/patch-unified-reading-eyes.py").read_text(encoding="utf-8")
    setter = _method_body(reading, "void setEyeHueDegrees(int hueDegrees)")

    assert "proceduralIrisHueDegrees = BoopEyeHueMath.clampHue(hueDegrees);" in setter
    assert "faceBitmap = recoloured" not in setter
    assert "setColorFilter(new" not in setter

    assert "private int irisColour(float saturation, float value)" in reading
    assert re.search(
        r"Color\.HSVToColor\(new float\[\] \{\s*proceduralIrisHueDegrees, saturation, value\s*\}\)",
        reading,
    )
