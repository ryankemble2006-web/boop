from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome"


def test_lrclib_is_second_source_after_deezer_and_uses_synced_only():
    loader = (SRC / "NativeLyricsLoader.java").read_text(encoding="utf-8")
    client = (SRC / "LrclibLyricsClient.java").read_text(encoding="utf-8")
    document = (SRC / "DeezerLyricsDocument.java").read_text(encoding="utf-8")
    assert "client.load(id, request, primaryDeadline)" in loader
    assert "fallback.load(track, id, request, fallbackDeadline)" in loader
    assert 'https://lrclib.net/api' in client
    assert '"/get?track_name="' in client
    assert '"/search?track_name="' in client
    assert 'candidate.optString("syncedLyrics","")' in client
    assert "plainLyrics" not in client
    assert "DeezerLyricsDocument.fromLrc(" in client
    assert "static DeezerLyricsDocument fromLrc(" in document


def test_lrclib_candidate_is_identity_checked_and_duration_bounded():
    client = (SRC / "LrclibLyricsClient.java").read_text(encoding="utf-8")
    assert 'sameText(row.optString("trackName"), track.title())' in client
    assert 'sameText(row.optString("artistName"), track.subtitle())' in client
    assert "Math.abs(duration*1000.0-track.durationMs()) <= 3500.0" in client


def test_home_preflight_and_open_lyrics_activity_both_use_metadata_fallback():
    browser = (SRC / "DeezerLyricsBrowser.java").read_text(encoding="utf-8")
    activity = (SRC / "ShieldLyricsActivity.java").read_text(encoding="utf-8")
    assert "loader.load(requested, cacheId, requestIdentity, document ->" in browser
    assert "loader.load(snapshot, cacheId, next, document ->" in activity
    assert '"meta:" + Integer.toHexString' in browser
    assert '"meta:" + Integer.toHexString' in activity


def test_lrclib_gets_its_own_fallback_window_and_broad_search():
    loader = (SRC / "NativeLyricsLoader.java").read_text(encoding="utf-8")
    client = (SRC / "LrclibLyricsClient.java").read_text(encoding="utf-8")
    assert "private static final long WAIT_MS = 9000L;" in loader
    assert "private static final long LRCLIB_WAIT_MS = 6000L;" in loader
    assert "fallbackDeadline = Math.min(deadline, DeezerLyricsClient.nowMs() + LRCLIB_WAIT_MS)" in loader
    assert '"/search?track_name=" + enc(track.title())' in client
    assert '"&q=" + enc(track.title())' in client
    assert "request(url, call, deadline, true)" in client
    assert 'if (left.startsWith("the ")) left = left.substring(4);' in client
