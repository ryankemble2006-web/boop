import json
from pathlib import Path


MANIFEST = Path("natural-voices/manifest.json")
MATERIALIZER = Path("scripts/materialize-unified.sh")
MAIN = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java")
ANDROID_MANIFEST = Path("boop-build/BOOP-Alpha1/app/src/main/AndroidManifest.xml")
VOICE_CONTROLLER = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/BoopVoiceController.java")
ANDROID_BACKEND = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/BoopAndroidSpeechBackend.java")
NATURAL_BACKEND = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/BoopNaturalSpeechBackend.java")
PACK = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/BoopNaturalVoicePack.java")
DOWNLOADER = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/BoopNaturalVoiceDownloader.java")

EXPECTED_URL = (
    "https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/"
    "kokoro-multi-lang-v1_0.tar.bz2"
)
EXPECTED_SHA256 = "c5f7e2d2caf082bc1d20fb70334a61d99d20b484500aad32e7cf84c128ea3298"
EXPECTED_SIZE = 349_906_910
EXPECTED_VOICES = [
    ("Emma", "bf_emma", 21),
    ("Isabella", "bf_isabella", 22),
    ("George", "bm_george", 26),
    ("Fable", "bm_fable", 25),
]


def load_manifest() -> dict:
    return json.loads(MANIFEST.read_text(encoding="utf-8"))


def test_pack_manifest_pins_current_official_artifact_and_four_british_voices() -> None:
    manifest = load_manifest()
    assert manifest["url"] == EXPECTED_URL
    assert manifest["sha256"] == EXPECTED_SHA256
    assert manifest["archiveSizeBytes"] == EXPECTED_SIZE
    assert manifest["url"].startswith("https://")
    assert [(v["name"], v["key"], v["sid"]) for v in manifest["voices"]] == EXPECTED_VOICES
    required = set(manifest["requiredFiles"])
    assert {"model.onnx", "voices.bin", "tokens.txt", "espeak-ng-data", "lexicon-gb-en.txt"} <= required
    assert manifest["minimumFreeBytes"] > EXPECTED_SIZE


def test_materializer_wires_natural_voice_patch_after_v70_scroll_repair() -> None:
    source = MATERIALIZER.read_text(encoding="utf-8")
    scroll = source.index("patch-unified-v70-regressions.py")
    natural = source.index("patch-unified-natural-voices.py")
    notifications = source.index("patch-unified-notifications.py")
    assert scroll < natural < notifications


def test_materialized_voice_settings_keeps_scroll_and_has_one_in_app_download_entry() -> None:
    source = MAIN.read_text(encoding="utf-8")
    assert "// BOOP_VOICE_SETTINGS_SCROLL_V70_FIX" in source
    assert 'setText("Natural voices")' in source
    assert source.count('setText("Download natural voices")') == 1
    assert 'setText("Cancel download")' in source
    for name, _, _ in EXPECTED_VOICES:
        assert f'setText("{name}")' in source


def test_natural_download_stays_private_and_never_uses_browser_or_shared_storage() -> None:
    downloader = DOWNLOADER.read_text(encoding="utf-8")
    pack = PACK.read_text(encoding="utf-8")
    manifest = ANDROID_MANIFEST.read_text(encoding="utf-8")

    assert "OkHttpClient" in downloader
    assert "getNoBackupFilesDir" in pack or "getFilesDir" in pack
    assert "ACTION_VIEW" not in downloader
    assert "ACTION_CREATE_DOCUMENT" not in downloader
    assert "ACTION_OPEN_DOCUMENT" not in downloader
    assert "Environment.getExternalStorage" not in downloader
    assert "WRITE_EXTERNAL_STORAGE" not in manifest
    assert "READ_EXTERNAL_STORAGE" not in manifest
    assert "MANAGE_EXTERNAL_STORAGE" not in manifest


def test_pack_installation_verifies_hash_and_rejects_path_traversal_before_atomic_activation() -> None:
    pack = PACK.read_text(encoding="utf-8")
    assert "SHA-256" in pack
    assert "getCanonicalFile" in pack
    assert "isAbsolute" in pack
    assert "TarArchiveInputStream" in pack
    assert "BZip2CompressorInputStream" in pack
    assert "renameTo" in pack or "Files.move" in pack
    assert "Download didn't verify. Try again." in pack or "Download didn't verify. Try again." in DOWNLOADER.read_text(encoding="utf-8")


def test_controller_persists_natural_state_without_replacing_android_voice_keys() -> None:
    source = VOICE_CONTROLLER.read_text(encoding="utf-8")
    assert '"voice_name"' in source
    assert '"pitch"' in source
    assert '"speech_rate"' in source
    assert '"selected_backend"' in source
    assert '"natural_speaker_key"' in source
    assert '"natural_pack_version"' in source
    assert '"natural_verification_state"' in source
    for _, key, sid in EXPECTED_VOICES:
        assert key in source
        assert str(sid) in source


def test_speak_remains_single_lifecycle_entry_and_natural_failure_falls_back_same_utterance() -> None:
    source = MAIN.read_text(encoding="utf-8")
    start = source.index("private void speak(String text)")
    end = source.index("private void keepAwakeAndHideSystemUi()", start)
    block = source[start:end]

    assert block.count("wakeCoordinator.onTtsStarting()") == 1
    assert "naturalSpeechBackend" in block
    assert "speakWithAndroidTts" in block
    assert "finishTtsUtterance()" in source

    android = ANDROID_BACKEND.read_text(encoding="utf-8")
    assert "tts.speak(text" in android
    assert "UtteranceProgressListener" in android


def test_natural_backend_uses_local_sherpa_and_existing_pitch_rate_controls() -> None:
    source = NATURAL_BACKEND.read_text(encoding="utf-8")
    assert "OfflineTtsKokoroModelConfig" in source
    assert "OfflineTtsModelConfig" in source
    assert "GenerationConfig" in source
    assert "setSid" in source
    assert "setSpeed" in source
    assert "AudioTrack" in source
    assert "PlaybackParams" in source
    assert "setPitch" in source
    assert "lexicon-gb-en.txt" in source


def test_no_cloud_tts_or_second_microphone_owner_is_added() -> None:
    joined = "\n".join(
        path.read_text(encoding="utf-8")
        for path in (NATURAL_BACKEND, PACK, DOWNLOADER)
    )
    forbidden = [
        "SpeechRecognizer",
        "AudioRecord",
        "RECORD_AUDIO",
        "openai.com",
        "elevenlabs",
        "googleapis.com/texttospeech",
        "API_KEY",
    ]
    for token in forbidden:
        assert token not in joined
