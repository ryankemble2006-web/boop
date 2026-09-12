from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MAIN = ROOT / "boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java"


def test_real_phone_developer_entry_uses_canonical_face_and_catalogue():
    text = MAIN.read_text(encoding="utf-8")
    assert "BoopDevMenuIntent.matches(transcript)" in text
    assert "private BoopCanonicalFaceView developerMenuFace;" in text
    assert "new BoopFaceView(this)" not in text
    assert text.count("developerMenuFace = new BoopCanonicalFaceView(this);") == 2
    content = text[text.index("private void showDeveloperMenuContent()"):
                   text.index("private void addDeveloperShelf(")]
    assert "addCanonicalDeveloperShelf(column);" in content
    assert "EyeCatalogue.ALL" in content
    assert "developerMenuFace.playCanonicalClip(clip.id)" in content


def test_materialized_phone_initialization_keeps_render_request_out_of_constructor():
    source = (MAIN.parent / "BoopCanonicalFaceView.java").read_text(encoding="utf-8")
    constructor = source[source.index("BoopCanonicalFaceView(Context context)"):
                         source.index("void showIdleBlackImmediately()")]
    assert "setEyeHueDegrees(BoopEyeHue.loadHue(context))" not in constructor
    assert "surface.requestRender()" not in constructor
    assert "renderer.setHueRotationDegrees(" in constructor
