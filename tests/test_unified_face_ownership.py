from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BUILT = ROOT / 'boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1'

def method(text, name):
    return text.split('private void ' + name + '(', 1)[1].split('\n    private ', 1)[0]

def test_voice_and_developer_overlays_suppress_the_underlying_face():
    text = (BUILT / 'MainActivity.java').read_text(encoding='utf-8')
    assert 'face.setOccluded("voice_settings", true)' in method(text, 'showVoiceSettings')
    assert 'face.setOccluded("voice_settings", false)' in method(text, 'hideVoiceSettings')
    assert 'face.setOccluded("developer", true)' in method(text, 'showDeveloperMenu')
    assert 'face.setOccluded("developer", false)' in method(text, 'hideDeveloperMenu')

def test_notification_preview_owns_exactly_one_face():
    text = (BUILT / 'MainActivity.java').read_text(encoding='utf-8')
    preview = method(text, 'showDeveloperNotificationPreview')
    assert 'new BoopCanonicalFaceView' not in preview
    assert 'puppet.setFaceVisible(false)' not in preview
    puppet = (BUILT / 'BoopNotificationPuppetView.java').read_text(encoding='utf-8')
    assert 'com.boop.eyes.NotificationSignView' in puppet
    assert 'new BoopFaceView' not in puppet
    assert 'boop_notification_hands' not in puppet

def test_hidden_face_cannot_be_reopened_by_queued_animation():
    text = (BUILT / 'BoopCanonicalFaceView.java').read_text(encoding='utf-8')
    assert 'BoopFacePresentationState' in text
    assert 'super.setVisibility(presentationState.effective())' in text
    assert 'isShown()' in text


def test_reopening_developer_menu_reuses_the_existing_overlay():
    text = (BUILT / 'MainActivity.java').read_text(encoding='utf-8')
    assert 'if (developerMenuOpen && developerMenuOverlay != null)' in method(text, 'showDeveloperMenu')
