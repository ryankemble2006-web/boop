from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FACE = ROOT / "boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/BoopFaceView.java"


def test_wall_idle_scheduler_uses_exact_double_blink_sequence():
    text = FACE.read_text(encoding="utf-8")
    assert "idleBlinksRemaining" in text
    assert "BoopIdleBlink.shouldDoubleBlink(idleBlinkRandom) ? 2 : 1" in text
    assert "postDelayed(idleBlinkRunnable, BoopIdleBlink.DOUBLE_GAP_MS)" in text
    assert "if (idleBlinksRemaining > 0)" in text
