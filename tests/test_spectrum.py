"""Real PCM frequency accuracy and onset/release latency regression checks."""
import pathlib
import subprocess
import tempfile

ROOT = pathlib.Path(__file__).resolve().parents[1]
SRC = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome"


def test_spectrum():
    with tempfile.TemporaryDirectory() as output:
        sources = [SRC / "PcmSpectrum.java", SRC / "SpectrumState.java"]
        sources += [ROOT / "tests/java" / name for name in
                    ("SpectrumCheck.java", "SpectrumTimingCheck.java")]
        subprocess.run(["javac", "-encoding", "UTF-8", "-d", output,
                        *map(str, sources)], check=True)
        for check in ("SpectrumCheck", "SpectrumTimingCheck"):
            subprocess.run(["java", "-cp", output,
                            "com.boop.shieldhome." + check], check=True)


if __name__ == "__main__":
    test_spectrum()
