"""Run the actual Java radius calculation, without an emulator or visual checks."""
import os
from pathlib import Path
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]


def test_artwork_corner_geometry():
    sdk = os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT")
    assert sdk, "Android SDK required for compilation"
    android_jar = Path(sdk) / "platforms/android-36/android.jar"
    java_home = os.environ.get("JAVA_HOME")
    def tool(name):
        suffix = ".exe" if os.name == "nt" else ""
        return str(Path(java_home) / "bin" / (name + suffix)) if java_home else shutil.which(name)
    with tempfile.TemporaryDirectory(prefix="boop-artframe-") as output:
        subprocess.run([tool("javac"), "-cp", str(android_jar), "-d", output,
            str(ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome/FocusChrome.java"),
            str(ROOT / "tests/java/ArtworkFrameGeometryHarness.java")], check=True)
        subprocess.run([tool("java"), "-cp", output + os.pathsep + str(android_jar),
            "com.boop.shieldhome.ArtworkFrameGeometryHarness"], check=True)


if __name__ == "__main__":
    test_artwork_corner_geometry()
