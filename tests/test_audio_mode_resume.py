"""Exercise actual audio policy/controller against the Android audio boundary."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome'

FILES = {
    'android/content/Context.java': '''package android.content;
public class Context {
  public final android.media.AudioManager audio = new android.media.AudioManager();
  public Context getApplicationContext() { return this; }
  public <T> T getSystemService(Class<T> type) { return type.cast(audio); }
}''',
    'android/media/AudioManager.java': '''package android.media;
public class AudioManager {
  public String value = "nv_param_audio_native_sample_rate_select=0";
  public int writes;
  public boolean reject;
  public String getParameters(String key) { return value; }
  public void setParameters(String next) {
    if (reject) throw new IllegalStateException("platform rejected");
    value = next; writes++;
  }
}''',
    'android/util/Log.java': '''package android.util;
public class Log {
  public static int i(String tag, String text) { return 0; }
  public static int w(String tag, String text) { return 0; }
}''',
    'com/boop/shieldhome/AudioResumeProbe.java': '''package com.boop.shieldhome;
public class AudioResumeProbe {
  static int checks;
  static void check(boolean ok, String why) { checks++; if (!ok) throw new AssertionError(why); }
  public static void main(String[] args) {
    android.content.Context context = new android.content.Context();
    android.media.AudioManager audio = context.audio;
    AudioModeController controller = AudioModeController.get(context);
    controller.applyLaunch(AudioModePolicy.forLaunch("deezer.android.app"));
    controller.applyPlayback(AudioModePolicy.forPlayback("deezer.android.app", 0, true, 0));
    controller.applyLaunch(AudioModePolicy.forLaunch("org.xbmc.kodi"));
    controller.applyPlayback(AudioModePolicy.forPlayback("deezer.android.app", 2, true, 0));
    check(audio.value.endsWith("=0"), "foreground Kodi outranks a delayed Deezer callback");
    controller.clearForegroundLaunch();
    controller.applyPlayback(AudioModePolicy.forPlayback("deezer.android.app", 2, false, 0));
    check(audio.value.endsWith("=0"), "Home does not replay stale music or resume paused Deezer");
    int[] plays = {0};
    controller.resume("deezer.android.app", () -> {
      check(audio.value.endsWith("=1"), "native mode must be restored BEFORE Deezer play dispatch");
      plays[0]++;
    });
    check(plays[0] == 1, "resume dispatches once without skip or replay");
    int writes = audio.writes;
    controller.applyPlayback(AudioModePolicy.forPlayback(" deezer.android.app ", 0, true, 0));
    check(audio.writes == writes, "same-mode session callbacks do not reset audio");
    audio.value = "nv_param_audio_native_sample_rate_select=0";
    controller.applyPlayback(AudioModePolicy.forPlayback("deezer.android.app", 0, true, 0));
    check(audio.value.endsWith("=1"), "native playback callbacks recover a changed system mode");
    controller.applyLaunch(AudioModePolicy.forLaunch("org.xbmc.kodi"));
    controller.clearForegroundLaunch();
    controller.resume("org.xbmc.kodi", () -> check(audio.value.endsWith("=0"), "Kodi resume stays in video mode"));
    controller.applyPlayback(AudioModePolicy.forPlayback("other.player", 2, true, 951));
    check(audio.value.endsWith("=0"), "unrelated apps cannot enable native music mode");
    controller.applyPlayback(AudioModePolicy.forPlayback("com.google.android.apps.mediashell", 2, true, 951));
    check(audio.value.endsWith("=1"), "live Cast music still restores native mode at Home");
    controller.resume("com.google.android.apps.mediashell", () -> check(audio.value.endsWith("=1"), "Cast resume must not guess an unknown content type"));
    controller.applyPlayback(AudioModePolicy.forPlayback("com.google.android.apps.mediashell", 1, true, 951));
    check(audio.value.endsWith("=0"), "Cast video still selects normal mode");
    audio.reject = true;
    controller.resume("deezer.android.app", () -> plays[0]++);
    check(plays[0] == 2, "unavailable platform setting cannot swallow the user's play request");
    System.out.println(checks + " audio resume assertions passed");
  }
}''',
}


def test_kodi_home_deezer_resume_restores_native_before_play(tmp_path):
    generated = []
    for name, content in FILES.items():
        path = tmp_path / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding='utf-8')
        generated.append(path)
    sources = [SRC / 'AudioModePolicy.java', SRC / 'AudioModeController.java',
               ROOT / 'source-test/AudioModePolicyTest.java', *generated]
    subprocess.run(['javac', '-encoding', 'UTF-8', '-d', str(tmp_path),
                    *map(str, sources)], check=True)
    for main in ('AudioModePolicyTest', 'AudioResumeProbe'):
        subprocess.run(['java', '-cp', str(tmp_path), 'com.boop.shieldhome.' + main],
                       check=True, timeout=15)
