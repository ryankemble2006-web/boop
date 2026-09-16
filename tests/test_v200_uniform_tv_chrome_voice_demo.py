from pathlib import Path
import subprocess

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


def test_v201_build_identity_and_workflow_gate():
    gradle = text("unified/app-build.gradle")
    assert "versionCode 201" in gradle
    assert 'versionName "1.2.201-voice-surface-ownership"' in gradle

    workflow = text(".github/workflows/build-boop-v191-hand-colour.yml")
    assert "Build BOOP v201 Voice surface ownership" in workflow
    assert "tests/test_v200_uniform_tv_chrome_voice_demo.py" in workflow
    assert "BOOP-Unified-v201-Voice-Surface-Ownership" in workflow


def java_method(source: str, signature: str) -> str:
    """Extract actual production visibility methods, not copies of their logic."""
    start = source.index(signature)
    opening = source.index("{", start)
    depth = 0
    for index in range(opening, len(source)):
        if source[index] == "{":
            depth += 1
        elif source[index] == "}":
            depth -= 1
            if depth == 0:
                return source[start:index + 1]
    raise AssertionError("Unclosed production method: " + signature)


def test_face_visibility_reaches_the_actual_graphics_surface(tmp_path):
    """Exercise real visibility/occlusion code for both wrapper and child surface.

    The tiny View stand-in records direct visibility writes. It intentionally
    does not pretend to reproduce Android composition. The hardware screenshot
    remains the separate proof that the top-layer eyes actually disappear.
    """
    roots = [ROOT / "source"]
    built = ROOT / "boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1"
    if built.is_dir():
        roots.append(built)
    for number, root in enumerate(roots):
        face = (root / "BoopCanonicalFaceView.java").read_text(encoding="utf-8")
        methods = "\n".join(java_method(face, signature) for signature in (
            "void setOccluded(String owner, boolean hidden)",
            "public void setVisibility(int visibility)",
            "private void applyPresentationVisibility()",
        ))
        harness = '''package com.boop.alpha1;
class View {
    static final int VISIBLE = 0, INVISIBLE = 4, GONE = 8;
    private int visibility = VISIBLE;
    public void setVisibility(int value) { visibility = value; }
    public int getVisibility() { return visibility; }
}
public final class FaceVisibilityProbe extends View {
    private BoopFacePresentationState presentationState = new BoopFacePresentationState();
    private View surface = new View();
    private void updateVisibleLifecycle() { }
    // PRODUCTION_METHODS
    private static void check(FaceVisibilityProbe face, int expected, String stage) {
        if (face.getVisibility() != expected)
            throw new AssertionError(stage + " wrapper: expected " + expected
                    + " but was " + face.getVisibility());
        if (face.surface.getVisibility() != expected)
            throw new AssertionError(stage + " surface: expected " + expected
                    + " but was " + face.surface.getVisibility());
    }
    public static void main(String[] args) {
        FaceVisibilityProbe face = new FaceVisibilityProbe();
        face.setVisibility(VISIBLE);
        check(face, VISIBLE, "awake");
        face.setOccluded("voice_settings", true);
        check(face, GONE, "voice settings");
        face.setVisibility(VISIBLE);
        check(face, GONE, "queued wake while voice settings is open");
        face.setOccluded("developer", true);
        face.setOccluded("voice_settings", false);
        check(face, GONE, "another modal still owns the screen");
        face.setVisibility(INVISIBLE);
        face.setOccluded("developer", false);
        check(face, INVISIBLE, "restore idle black without reviving eyes");
        face.setVisibility(VISIBLE);
        check(face, VISIBLE, "wake after closing all modals");
        face.setOccluded("voice_settings", true);
        face.setVisibility(GONE);
        face.setOccluded("voice_settings", false);
        check(face, GONE, "preserve requested gone");
        face.setVisibility(VISIBLE);
        check(face, VISIBLE, "subsequent wake");
        face.surface = null;
        face.setVisibility(INVISIBLE);
        if (face.getVisibility() != INVISIBLE)
            throw new AssertionError("construction-time null surface");
        face.presentationState = null;
        face.setVisibility(GONE);
        if (face.getVisibility() != GONE)
            throw new AssertionError("construction-time null presentation state");
        System.out.println("PASS: direct eye-surface visibility and modal ownership");
    }
}
'''.replace("// PRODUCTION_METHODS", methods)
        work = tmp_path / str(number)
        work.mkdir()
        (work / "FaceVisibilityProbe.java").write_text(harness, encoding="utf-8")
        (work / "BoopFacePresentationState.java").write_text(
            (root / "BoopFacePresentationState.java").read_text(encoding="utf-8"),
            encoding="utf-8",
        )
        compiled = subprocess.run(
            ["javac", "-d", str(work), str(work / "FaceVisibilityProbe.java"),
             str(work / "BoopFacePresentationState.java")],
            text=True, capture_output=True, timeout=60,
        )
        assert compiled.returncode == 0, compiled.stdout + compiled.stderr
        result = subprocess.run(
            ["java", "-cp", str(work), "com.boop.alpha1.FaceVisibilityProbe"],
            text=True, capture_output=True, timeout=30,
        )
        assert result.returncode == 0, str(root) + "\n" + result.stdout + result.stderr
        assert "PASS: direct eye-surface visibility" in result.stdout
