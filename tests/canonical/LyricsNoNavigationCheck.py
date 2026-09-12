from pathlib import Path

src = Path("unified/shield-home/src/main/java/com/boop/shieldhome/StartupLocalBridge.java").read_text(encoding="utf-8")
start = src.index("public String openDeezerLyrics()")
end = src.index("private com.boop.shared.DeezerScreen dumpDeezerScreen", start)
method = src[start:end]
if "input keyevent" in method:
    raise AssertionError("Lyrics shortcut must not send D-pad/key navigation")
if "input tap " not in method:
    raise AssertionError("Lyrics shortcut must activate the semantic target directly")
print("LyricsNoNavigationCheck PASS")
