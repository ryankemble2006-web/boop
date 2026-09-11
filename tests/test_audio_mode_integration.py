from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def test_shield_audio_mode_is_wired():
    manifest = (ROOT / 'unified/shield-home-manifest.xml').read_text(encoding='utf-8-sig')
    launcher = (ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome/ShieldLauncherActivity.java').read_text(encoding='utf-8')
    manager = (ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingManager.java').read_text(encoding='utf-8')
    controller = (ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome/AudioModeController.java').read_text(encoding='utf-8')

    assert 'android.permission.MODIFY_AUDIO_SETTINGS' in manifest
    assert 'applyLaunch(AudioModePolicy.forLaunch(entry.packageName()))' in launcher
    assert 'clearForegroundLaunch()' in launcher
    assert 'nowPlayingManager.reapplyAudioMode()' in launcher
    assert 'void reapplyAudioMode()' in manager
    assert 'long actions = playback == null ? 0L : playback.getActions();' in manager
    assert 'applyCast(' in manager
    assert 'AudioModePolicy.forCast(binding.controller.getPackageName(), contentType, playing, actions)' in manager
    assert 'onAudioInfoChanged' in manager
    assert 'getParameters' in controller
    assert 'lastMode' not in controller
