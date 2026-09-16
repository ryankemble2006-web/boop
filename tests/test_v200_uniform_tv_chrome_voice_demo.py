from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def text(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def test_uniform_tv_chrome_is_canonical_and_runtime_wide():
    path = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome/BoopTvChrome.java"
    assert path.is_file(), "shared BOOP TV chrome is missing"
    chrome = path.read_text(encoding="utf-8")
    assert "public final class BoopTvChrome" in chrome
    assert "Color.rgb(77, 184, 255)" in chrome
    assert "BORDER_DP = 4" in chrome
    assert "CORNER_DP = 10" in chrome
    assert "FOCUSED_SCALE = 1.04f" in chrome
    assert "Configuration.UI_MODE_TYPE_TELEVISION" in chrome
    assert "instanceof EditText" in chrome
    assert "instanceof SeekBar" in chrome
    assert "instanceof ImageView" in chrome
    assert "instanceof CompoundButton" in chrome
    assert "OnGlobalLayoutListener" in chrome
    assert "OnGlobalFocusChangeListener" in chrome
    assert "setOnFocusChangeListener" not in chrome

    app = text("unified/UnifiedApplication.java")
    assert "BoopTvChrome.install(activity)" in app

    home = text("unified/shield-home/src/main/java/com/boop/shieldhome/FocusChrome.java")
    assert "BoopTvChrome.accentColor" in home
    assert "BoopTvChrome.filled" in home


def test_voice_settings_owns_screen_and_has_demo_button():
    path = ROOT / "scripts/patch-unified-v200-voice-ui.py"
    assert path.is_file(), "v200 voice UI patch is missing"
    patch = path.read_text(encoding="utf-8")
    assert "face.setVisibility(View.INVISIBLE);" in patch
    assert "voiceSettingsOverlay.setBackgroundColor(Color.BLACK);" in patch
    assert "face.setVisibility(View.VISIBLE);" in patch
    assert "face.showIdleBlackImmediately();" in patch
    assert 'testVoice.setText("TEST VOICE")' in patch
    assert '"This is how BOOP sounds."' in patch
    assert "testCurrentVoice()" in patch
    assert "voiceController.naturalBackendSelectedAndUsable()" in patch
    assert "voiceController.selectedNaturalVoice()" in patch
    assert "speakWithAndroidTts" in patch
    assert "previewNaturalVoice" in patch
    assert "remove preview face wake" in patch


def test_natural_voice_pitch_is_applied_only_at_pcm_playback():
    backend = text("source/BoopNaturalSpeechBackend.java")
    assert "PlaybackParams" in backend
    assert ".setPitch(pitchForPlayback(pitch))" in backend
    assert ".setSpeed(1.0f)" in backend
    assert "Natural pitch unavailable; playing original PCM" in backend
    assert "speedForRate(rate)" in backend


def test_v200_build_identity_and_workflow_gate():
    gradle = text("unified/app-build.gradle")
    assert "versionCode 200" in gradle
    assert 'versionName "1.2.200-uniform-tv-chrome-voice-demo"' in gradle

    workflow = text(".github/workflows/build-boop-v191-hand-colour.yml")
    assert "Build BOOP v200 uniform TV chrome and voice demo" in workflow
    assert "tests/test_v200_uniform_tv_chrome_voice_demo.py" in workflow
    assert "BOOP-Unified-v200-Uniform-TV-Chrome-Voice-Demo" in workflow
