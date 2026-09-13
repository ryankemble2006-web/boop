"""Artist identity and action wiring; no visual CI judgement or network."""
from pathlib import Path
import os
import runpy
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
SHIELD = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome"


def test_artist_identity():
    model = ROOT / "unified/shared/DeezerArtistMatch.java"
    assert model.is_file(), "Exact artist matching has not been implemented"
    def tool(name):
        home = os.environ.get("JAVA_HOME")
        suffix = ".exe" if os.name == "nt" else ""
        return str(Path(home) / "bin" / (name + suffix)) if home else shutil.which(name)
    with tempfile.TemporaryDirectory(prefix="boop-artist-") as out:
        subprocess.run([tool("javac"), "-encoding", "UTF-8", "-d", out,
                        str(model), str(ROOT / "tests/java/DeezerArtistMatchHarness.java")], check=True)
        subprocess.run([tool("java"), "-cp", out, "DeezerArtistMatchHarness"], check=True)


def test_artist_action_is_wired():
    view = (SHIELD / "ShieldNowPlayingView.java").read_text(encoding="utf-8")
    home = (SHIELD / "ShieldHomeView.java").read_text(encoding="utf-8")
    activity = (SHIELD / "ShieldLauncherActivity.java").read_text(encoding="utf-8")
    assert "callbacks.onBrowseNowPlayingArtist()" in view, "Artist has no click action"
    assert "default void onBrowseNowPlayingArtist()" in home
    assert "artistBrowser.open(" in activity
    assert "subtitle.setOnKeyListener(this::handleArtistKey)" in view
    assert "subtitle.requestFocus()" in view
    assert "artistBrowser.onTrackChanged(snapshot)" in activity
    assert activity.count("artistBrowser.cancel();") >= activity.count("albumBrowser.cancel();") - 1


def test_artist_browser_is_exact_and_cancellable():
    file = SHIELD / "DeezerArtistBrowser.java"
    assert file.is_file(), "Artist browsing is missing"
    browser = file.read_text(encoding="utf-8")
    for required in ["DeezerArtistMatch.resolve", "operation != generation",
                     "activity.hasWindowFocus()", "sameRequest(", "deezerLyricsTrackId(requested)",
                     '"https://www.deezer.com/artist/"', '.setPackage("deezer.android.app")',
                     "setInstanceFollowRedirects(false)", "connection.disconnect()"]:
        assert required in browser, required
    for forbidden in ["skipTo", "seekTo", "getTransportControls", "startActivityForResult"]:
        assert forbidden not in browser, "Artist browsing must not control playback"


if __name__ == "__main__":
    for name, function in list(globals().items()):
        if name.startswith("test_") and callable(function):
            function()
            print("PASS", name)
