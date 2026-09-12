from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MENU = ROOT / "source/BoopDevMenuActivity.java"
MODEL = ROOT / "source/BoopDevMenuModel.java"
FACE = ROOT / "source/BoopCanonicalFaceView.java"
RENDERER = ROOT / "unified/animation/java/com/boop/eyes/CanonicalEyeRenderer.java"
SHADER = ROOT / "unified/animation/assets/eyes.frag"


def test_developer_menu_uses_canonical_catalogue_not_legacy_animation_actions():
    menu = MENU.read_text(encoding="utf-8")
    model = MODEL.read_text(encoding="utf-8")
    assert "BoopCanonicalFaceView" in menu
    assert "EyeCatalogue.ALL" in menu
    assert "playCanonicalClip" in menu
    assert 'new Shelf("Animations"' not in model
    for legacy in ("case WAKE:", "case THINK:", "case BERRY_1:", "case SHAKE:", "case SLEEP:"):
        assert legacy not in menu


def test_eye_hue_is_owned_by_canonical_renderer_and_shader():
    face = FACE.read_text(encoding="utf-8")
    renderer = RENDERER.read_text(encoding="utf-8")
    shader = SHADER.read_text(encoding="utf-8")
    assert "setHueRotationDegrees" in renderer
    assert "uHueRadians" in renderer
    assert "uHueRadians" in shader
    assert "irisMask" in shader
    assert "hueRotate" in shader
    assert "renderer.setHueRotationDegrees" in face
    assert "BoopEyeHue.loadHue(context)" in face
