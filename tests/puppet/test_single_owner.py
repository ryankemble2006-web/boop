from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
def test_notification_scene_has_exactly_one_eye_surface_and_no_runtime_demo_shortcut():
    scene=(ROOT/'unified/animation/java/com/boop/eyes/CanonicalSignScene.java').read_text()
    listener=(ROOT/'source/BoopNotificationListenerService.java').read_text()
    assert scene.count('new GLSurfaceView(')==1
    assert 'SignMotion.sample(' in scene
    assert 'runtime.post(record,' in listener
    assert 'BoopDevNotificationPreview' not in listener

def test_underlying_surface_relinquishes_gl_visibility_and_multiple_covers_are_used():
    face=(ROOT/'source/BoopCanonicalFaceView.java').read_text()
    assert 'surface.setVisibility(cover.covered() ? View.INVISIBLE : View.VISIBLE)' in face
    assert 'isShown() && !cover.covered()' in face
    assert 'areAnimatorsEnabled()' not in face
    assert 'PuppetPreferences.speed' in face
