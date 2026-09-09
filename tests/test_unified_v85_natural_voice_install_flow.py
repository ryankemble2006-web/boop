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
