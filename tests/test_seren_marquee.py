from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]


def test_seren_marquee_speed_full_cycle_and_refresh_continuity(tmp_path):
    state = ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome/SerenMarqueeState.java'
    probe = ROOT / 'tests/java/SerenMarqueeProbe.java'
    subprocess.run(['javac', '-d', str(tmp_path), str(state), str(probe)], check=True)
    subprocess.run(['java', '-cp', str(tmp_path), 'com.boop.shieldhome.SerenMarqueeProbe'], check=True)
