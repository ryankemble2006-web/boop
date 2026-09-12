from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FACE = ROOT / "source/BoopCanonicalFaceView.java"


def test_constructor_does_not_request_render_before_renderer_setup():
    source = FACE.read_text(encoding="utf-8")
    constructor = source[source.index("BoopCanonicalFaceView(Context context)"):
                         source.index("void showIdleBlackImmediately()")]
    assert "setEyeHueDegrees(BoopEyeHue.loadHue(context))" not in constructor
    assert "eyeHueDegrees = BoopEyeHueMath.clampHue(BoopEyeHue.loadHue(context))" in constructor
    assert "renderer.setHueRotationDegrees(" in constructor
    assert constructor.index("renderer.setHueRotationDegrees(") < constructor.index("surface.setRenderer(renderer)")
