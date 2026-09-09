from pathlib import Path
import re


def test_v85_combines_tablet_routing_with_natural_voice_install_flow():
    profile = Path("unified/BoopDeviceProfile.java").read_text(encoding="utf-8")
    downloader = Path("source/BoopNaturalVoiceDownloader.java").read_text(encoding="utf-8")
    patch = Path("scripts/patch-unified-natural-voices.py").read_text(encoding="utf-8")

    # The combined candidate must retain the already-accepted generic tablet route.
    assert "TABLET_MIN_SMALLEST_WIDTH_DP = 600" in profile
    assert "smallestScreenWidthDp >= TABLET_MIN_SMALLEST_WIDTH_DP" in profile

    # Post-download verification must not reread the full ~350 MB archive while
    # the UI sits on one static Verifying label. Digest the exact bytes as they
    # are written, then expose extraction as a distinct install phase with life signs.
    assert "MessageDigest" in downloader
    assert "digest.update(buffer, 0, count)" in downloader
    assert "Installing natural voices" in downloader
    assert "onInstallProgress" in downloader
    assert "onInstallProgress" in patch

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
