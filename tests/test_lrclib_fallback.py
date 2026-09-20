from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome"


def test_lrclib_is_second_source_after_deezer_and_uses_synced_only():
    loader = (SRC / "NativeLyricsLoader.java").read_text(encoding="utf-8")
    client = (SRC / "LrclibLyricsClient.java").read_text(encoding="utf-8")
    document = (SRC / "DeezerLyricsDocument.java").read_text(encoding="utf-8")
    assert "client.load(id, request, primaryDeadline)" in loader
    assert "fallback.load(track, id, request, deadline)" in loader
    assert 'https://lrclib.net/api' in client
    assert '"/get?track_name="' in client
    assert '"/search?track_name="' in client
    assert 'candidate.optString("syncedLyrics","")' in client
    assert "plainLyrics" not in client
    assert "DeezerLyricsDocument.fromLrc(" in client
    assert "static DeezerLyricsDocument fromLrc(" in document


def test_lrclib_candidate_is_identity_checked_and_duration_bounded():
    client = (SRC / "LrclibLyricsClient.java").read_text(encoding="utf-8")
    assert 'norm(row.optString("trackName")).equals(norm(track.title()))' in client
    assert 'norm(row.optString("artistName")).equals(norm(track.subtitle()))' in client
    assert "Math.abs(duration*1000.0-track.durationMs()) <= 3500.0" in client


def test_home_preflight_and_open_lyrics_activity_both_use_metadata_fallback():
    browser = (SRC / "DeezerLyricsBrowser.java").read_text(encoding="utf-8")
    activity = (SRC / "ShieldLyricsActivity.java").read_text(encoding="utf-8")
    assert "loader.load(requested, cacheId, requestIdentity, document ->" in browser
    assert "loader.load(snapshot, cacheId, next, document ->" in activity
    assert '"meta:" + Integer.toHexString' in browser
    assert '"meta:" + Integer.toHexString' in activity
