from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / 'unified/deezer-bridge'


def test_confirmation_uses_new_frames_not_one_fixed_delay(tmp_path):
    policy = SRC / 'DeezerHeartConfirmation.java'
    assert policy.exists(), 'Native confirmation still samples only once after a fixed delay'
    harness = ROOT / 'tests/java/HeartConfirmationProbe.java'
    subprocess.run(['javac', '-encoding', 'UTF-8', '-d', str(tmp_path), str(policy), str(harness)], check=True)
    subprocess.run(['java', '-cp', str(tmp_path), 'com.boop.bridge.HeartConfirmationProbe'], check=True)
    bridge = (SRC / 'DeezerHeartBridge.java').read_text(encoding='utf-8')
    assert 'DeezerHeartConfirmation.awaitChange(' in bridge
    assert 'pixelsLock.wait(' in bridge
    assert 'frameReceivedAt=SystemClock.elapsedRealtime()' in bridge
    assert 'int after=state(heart)' not in bridge


def test_lyrics_heading_removed_without_reflowing_accepted_content():
    text = (ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome/ShieldLyricsView.java').read_text(encoding='utf-8')
    assert 'eyebrow' not in text, 'Remove the decorative heading, not the accepted music geometry'
    assert 'MUSIC_COLUMN_SHIFT = 38f' in text
    assert 'place(artwork, artLeft, 116f * unit, artSize, artSize);' in text
    assert 'place(queueButton, progressCenter - 56f * unit, 703f * unit, 112f * unit, 38f * unit);' in text


def test_uncertain_toggle_gets_one_read_only_reconciliation(tmp_path):
    source = ROOT / 'unified/DeezerHeartReceiptRecovery.java'
    assert source.exists(), 'Unconfirmed applied toggles have no read-only reconciliation'
    harness = ROOT / 'tests/java/HeartRecoveryProbe.java'
    subprocess.run(['javac', '-encoding', 'UTF-8', '-d', str(tmp_path), str(source), str(harness)], check=True)
    subprocess.run(['java', '-cp', str(tmp_path), 'com.boop.alpha1.HeartRecoveryProbe'], check=True)
    backend = (ROOT / 'unified/BoopDeezerHeartBackend.java').read_text(encoding='utf-8')
    assert 'DeezerHeartReceiptRecovery.resolve(' in backend
    assert 'readValues.put("operation","read")' in backend
    assert 'readValues.put("nonce",UUID.randomUUID()' in backend
    assert 'readValues.put("expected_saved","-1")' in backend
