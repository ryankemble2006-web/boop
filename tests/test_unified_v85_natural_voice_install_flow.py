from pathlib import Path
import re


def test_v85_combines_tablet_routing_with_natural_voice_install_flow():
    profile = Path("unified/BoopDeviceProfile.java").read_text(encoding="utf-8")
    downloader = Path("source/BoopNaturalVoiceDownloader.java").read_text(encoding="utf-8")

    # The combined candidate must retain the already-accepted generic tablet route.
    assert "TABLET_MIN_SMALLEST_WIDTH_DP = 600" in profile
    assert "smallestScreenWidthDp >= TABLET_MIN_SMALLEST_WIDTH_DP" in profile

    # Post-download verification must not reread the full ~350 MB archive while
    # the UI sits on one static Verifying label. Digest the exact bytes as they
    # are written, then expose extraction as a distinct install phase with life signs.
    assert "MessageDigest" in downloader
    assert "digest.update(buffer, 0, count)" in downloader
    assert "Installing natural voices" in downloader
    assert "default void onInstallProgress" in downloader
    assert 'onStatus("Installing natural voices… " + safePercent + "%")' in downloader

    # Cancel from Voice Settings must be cooperative. It may set the cancellation
    # flag / cancel the HTTP call, but must not synchronously enter pack cleanup
    # while installVerifiedArchive owns the pack monitor.
    cancel_match = re.search(
        r"\n    void cancel\(\) \{(?P<body>.*?)\n    \}\n\n    boolean isRunning\(\)",
        downloader,
        re.DOTALL,
    )
    assert cancel_match, "Could not locate natural voice downloader cancel()"
    assert "finishCancelled(" not in cancel_match.group("body")


def test_existing_current_natural_pack_restores_verified_backend_before_preview():
    patch = Path("scripts/patch-unified-natural-voices.py").read_text(encoding="utf-8")
    startup = re.search(
        r"boolean naturalPackReady = naturalVoicePack\.isInstalled\(\);(?P<body>.*?)naturalSpeechBackend =",
        patch,
        re.DOTALL,
    )
    assert startup, "Could not locate natural voice startup reconciliation"
    body = startup.group("body")

    # isInstalled() already validates the active pack's required files and current
    # manifest version. On a later app launch, restore the controller's verified
    # state before any natural row can select a profile and call speak().
    assert "if (naturalPackReady)" in body
    assert "voiceController.onNaturalPackVerified(naturalVoiceManifest.version());" in body


def test_natural_preview_uses_kokoro_directly_and_never_masks_failure_with_android_tts():
    patch = Path("scripts/patch-unified-natural-voices.py").read_text(encoding="utf-8")
    handler = re.search(
        r"private void prepareNaturalVoiceButton\(.*?\) \{(?P<body>.*?)\n    \}\n\n    private void setNaturalVoiceChoicesVisible",
        patch,
        re.DOTALL,
    )
    assert handler, "Could not locate natural voice row handler"
    body = handler.group("body")

    # A voice-name tap is both the selector and the demo. It must route through a
    # dedicated natural preview path, never generic speak(), whose safety fallback
    # is Android TTS and would make every demo sound like the Android selection.
    assert "selectNaturalVoice(key)" in body
    assert "previewNaturalVoice(preview)" in body
    assert "speak(preview)" not in body
    assert "Selected: " in body

    preview = re.search(
        r"private void previewNaturalVoice\(String text\) \{(?P<body>.*?)\n    \}\n\n    private void setNaturalVoiceChoicesVisible",
        patch,
        re.DOTALL,
    )
    assert preview, "Could not locate dedicated natural voice preview path"
    assert "naturalSpeechBackend.speak(" in preview.group("body")
    assert "speakWithAndroidTts" not in preview.group("body")


def test_kokoro_v1_multilang_uses_lexicon_frontend_without_invalid_eng_override():
    backend = Path("source/BoopNaturalSpeechBackend.java").read_text(encoding="utf-8")

    # sherpa-onnx 1.13.7 Kokoro >=1.0 accepts ISO-style values such as `en`, or an
    # empty lang when an explicit lexicon is supplied. `eng` is not a Kokoro lang
    # value and can make synthesis fail before playback.
    assert 'kokoro.setLang("eng")' not in backend
    assert 'kokoro.setLexicon(lexicon.getAbsolutePath())' in backend


def test_android_kokoro_avoids_sherpa_jni_callback_crash_path():
    backend = Path("source/BoopNaturalSpeechBackend.java").read_text(encoding="utf-8")

    # sherpa-onnx 1.13.7's Android JNI callback bridge captures thread-local JNI
    # state. Kokoro can abort the whole Android process before Java fallback runs.
    assert "generateWithConfigAndCallback" not in backend
    assert "tts.generateWithConfig(text, generation)" in backend
