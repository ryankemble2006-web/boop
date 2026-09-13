from pathlib import Path
import json
import re


def test_v85_combines_tablet_routing_with_natural_voice_install_flow():
    profile = Path("unified/BoopDeviceProfile.java").read_text(encoding="utf-8")
    downloader = Path("source/BoopNaturalVoiceDownloader.java").read_text(encoding="utf-8")
    assert "TABLET_MIN_SMALLEST_WIDTH_DP = 600" in profile
    assert "smallestScreenWidthDp >= TABLET_MIN_SMALLEST_WIDTH_DP" in profile
    assert "MessageDigest" in downloader
    assert "digest.update(buffer, 0, count)" in downloader
    assert "Installing natural voices" in downloader
    assert "default void onInstallProgress" in downloader
    assert 'onStatus("Installing natural voices… " + safePercent + "%")' in downloader
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
    assert "if (naturalPackReady)" in body
    assert "voiceController.onNaturalPackVerified(naturalVoiceManifest.version());" in body


def test_failed_preview_cannot_persist_a_dead_natural_backend_for_normal_speech():
    patch = Path("scripts/patch-unified-natural-voices.py").read_text(encoding="utf-8")
    handler = re.search(
        r"private void prepareNaturalVoiceButton\(.*?\) \{(?P<body>.*?)\n    \}\n\n    private void previewNaturalVoice",
        patch,
        re.DOTALL,
    )
    assert handler, "Could not locate natural voice row handler"
    body = handler.group("body")
    assert "previewNaturalVoice(key, preview, naturalStatus, button)" in body
    assert "selectNaturalVoice(key)" not in body
    assert "speak(preview)" not in body
    preview = re.search(
        r"private void previewNaturalVoice\(String key, String text, TextView naturalStatus, Button button\) \{(?P<body>.*?)\n    \}\n\n    private void setNaturalVoiceChoicesVisible",
        patch,
        re.DOTALL,
    )
    assert preview, "Could not locate guarded natural voice preview path"
    preview_body = preview.group("body")
    assert "BoopVoiceController.findNaturalVoice(key)" in preview_body
    assert "voiceController.naturalPackReadyForPreview()" in preview_body
    assert "naturalSpeechBackend.speak(" in preview_body
    assert "voiceController.selectNaturalVoice(key)" in preview_body
    assert "voiceController.markNaturalPlaybackProven()" in preview_body
    assert 'naturalStatus.setText("Selected: " + button.getText())' in preview_body
    assert "speakWithAndroidTts" not in preview_body
    assert "Android voice kept" in preview_body


def test_normal_natural_speech_requires_a_physically_proven_runtime_backend():
    controller = Path("source/BoopVoiceController.java").read_text(encoding="utf-8")
    assert '"natural_runtime_proven_version"' in controller
    assert "boolean naturalPackReadyForPreview()" in controller
    assert "void markNaturalPlaybackProven()" in controller
    gate = re.search(
        r"boolean naturalBackendSelectedAndUsable\(\) \{(?P<body>.*?)\n    \}",
        controller,
        re.DOTALL,
    )
    assert gate, "Could not locate natural backend runtime gate"
    body = gate.group("body")
    assert "naturalPackReadyForPreview()" in body
    assert "naturalRuntimeProvenVersion" in body
    assert "naturalPackVersion.equals(naturalRuntimeProvenVersion)" in body


def test_kokoro_v1_multilang_keeps_valid_lexicon_frontend_configuration():
    backend = Path("source/BoopNaturalSpeechBackend.java").read_text(encoding="utf-8")
    assert 'kokoro.setLexicon(lexicon.getAbsolutePath())' in backend


def test_android_kokoro_avoids_sherpa_jni_callback_crash_path():
    backend = Path("source/BoopNaturalSpeechBackend.java").read_text(encoding="utf-8")
    assert "generateWithConfigAndCallback" not in backend
    assert "tts.generateWithConfig(text, generation)" in backend


def test_natural_playback_preserves_pcm16_path_with_optional_pitch():
    backend = Path("source/BoopNaturalSpeechBackend.java").read_text(encoding="utf-8")
    # Keep the accepted PCM16/static playback. Only an explicit non-neutral pitch
    # applies PlaybackParams; synthesis still owns cadence independently.
    assert "AudioFormat.ENCODING_PCM_16BIT" in backend
    assert "toPcm16" in backend
    assert "short[] pcm" in backend
    assert "track.write(pcm" in backend
    assert "AudioFormat.ENCODING_PCM_FLOAT" not in backend
    assert "if (pitch != 1f)" in backend
    assert ".setSpeed(1f).setPitch(pitch)" in backend
    assert "AUDIO_FALLBACK_MODE_FAIL" in backend
    assert "pitchForPlayback(pitch)" in backend


def test_natural_failure_reports_whether_synthesis_or_playback_failed():
    backend = Path("source/BoopNaturalSpeechBackend.java").read_text(encoding="utf-8")
    assert 'NaturalSpeechException("synthesis"' in backend
    assert 'NaturalSpeechException("playback"' in backend


def test_v89_natural_runtime_diagnostics_split_failure_before_another_fix():
    backend = Path("source/BoopNaturalSpeechBackend.java").read_text(encoding="utf-8")
    diagnostics = Path("scripts/patch-v89-natural-diagnostics.py").read_text(encoding="utf-8")
    materialize = Path("scripts/materialize-unified.sh").read_text(encoding="utf-8")
    assert re.search(r'NaturalSpeechException\s*\(\s*"files"', backend)
    assert re.search(r'NaturalSpeechException\s*\(\s*"initialization"', backend)
    assert re.search(r'NaturalSpeechException\s*\(\s*"synthesis"', backend)
    assert re.search(r'NaturalSpeechException\s*\(\s*"playback"', backend)
    assert "runtimeFilesReadyForSherpa" in backend
    assert "candidate.sampleRate()" in backend
    assert "candidate.numSpeakers()" in backend
    assert '"BOOP DEV E890"' in diagnostics
    assert '"BOOP DEV E891"' in diagnostics
    assert '"BOOP DEV E892"' in diagnostics
    assert '"BOOP DEV E893"' in diagnostics
    assert "showNaturalVoiceDevDiagnostic" in diagnostics
    assert "BuildConfig.DEBUG" in diagnostics
    assert "Android voice kept" in Path("scripts/patch-unified-natural-voices.py").read_text(encoding="utf-8")
    assert "python3 scripts/patch-v89-natural-diagnostics.py" in materialize


def test_v90_preflight_accepts_exact_official_kokoro_runtime_inputs_only():
    manifest = json.loads(Path("natural-voices/manifest.json").read_text(encoding="utf-8"))
    backend = Path("source/BoopNaturalSpeechBackend.java").read_text(encoding="utf-8")
    expected_files = {
        item for item in manifest["requiredFiles"] if item != "espeak-ng-data"
    }
    list_match = re.search(
        r"SHERPA_RUNTIME_FILES\s*=\s*\{(?P<body>.*?)\};",
        backend,
        re.DOTALL,
    )
    assert list_match, "Could not locate Sherpa runtime file preflight list"
    actual_files = set(re.findall(r'"([^"]+)"', list_match.group("body")))
    assert actual_files == expected_files
    assert not any(path.startswith("inno/") for path in actual_files)


def test_v91_static_track_accepts_android_no_static_data_state_before_write():
    backend = Path("source/BoopNaturalSpeechBackend.java").read_text(encoding="utf-8")
    play = re.search(
        r"private void play\(RequestState request, float\[\] samples, int sampleRate, float pitch\).*?\{(?P<body>.*?)\n    \}\n\n    private void stopLocked",
        backend,
        re.DOTALL,
    )
    assert play, "Could not locate natural playback method"
    body = play.group("body")
    assert ".setTransferMode(AudioTrack.MODE_STATIC)" in body
    assert "track.getState() == AudioTrack.STATE_UNINITIALIZED" in body
    assert "track.getState() != AudioTrack.STATE_INITIALIZED" not in body
