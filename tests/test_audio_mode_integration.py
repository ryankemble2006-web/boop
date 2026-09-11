from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def test_shield_audio_mode_is_wired():
    manifest = (ROOT / 'unified/shield-home-manifest.xml').read_text(encoding='utf-8-sig')
    launcher = (ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome/ShieldLauncherActivity.java').read_text(encoding='utf-8')
    manager = (ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingManager.java').read_text(encoding='utf-8')

    assert 'android.permission.MODIFY_AUDIO_SETTINGS' in manifest
    assert 'AudioModePolicy.forLaunch(entry.packageName())' in launcher
    assert 'PlaybackState.ACTION_SKIP_TO_PREVIOUS' in manager
    assert 'PlaybackState.ACTION_SKIP_TO_NEXT' in manager
    assert 'supportsPrevious, supportsNext' in manager
    assert 'onAudioInfoChanged' in manager
