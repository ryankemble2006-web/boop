"""Real worker tests for native bounce plus playback-driven fallback; no Android launch."""
import pathlib, tempfile, subprocess
ROOT=pathlib.Path(__file__).resolve().parents[1]
SRC=ROOT/"unified/shield-home/src/main/java/com/boop/shieldhome"

def main():
    text=(SRC/"MusicBounceSource.java").read_text()
    assert "DirectMusicSource" not in text, "Delayed diagnostic polling is still driving the dance"
    stubs={
      "android/Manifest.java": 'package android; public class Manifest {public static class permission {public static final String RECORD_AUDIO="record";}}',
      "android/content/Context.java": 'package android.content; public class Context {public static int permission=0; public Context getApplicationContext(){return this;} public int checkSelfPermission(String s){return permission;}}',
      "android/content/pm/PackageManager.java": 'package android.content.pm; public class PackageManager {public static final int PERMISSION_GRANTED=0;}',
      "android/os/SystemClock.java": 'package android.os; public class SystemClock {public static long now=1000; public static long uptimeMillis(){return now;}}',
      "android/os/Process.java": 'package android.os; public class Process {public static final int THREAD_PRIORITY_BACKGROUND=10;}',
      "android/os/HandlerThread.java": 'package android.os; public class HandlerThread {public HandlerThread(String s,int p){} public void start(){} public Object getLooper(){return null;} public void quitSafely(){}}',
      "android/os/Handler.java": 'package android.os; public class Handler {public static java.util.ArrayDeque<Runnable> q=new java.util.ArrayDeque<>(); public static long delay; public Handler(Object o){} public void post(Runnable r){q.add(r);} public void postDelayed(Runnable r,long n){delay=n;q.add(r);} public void removeCallbacks(Runnable r){q.removeIf(x->x==r);} public static void next(){q.remove().run();}}',
      "android/util/Log.java": 'package android.util; public class Log {public static int i(String t,String s){return 0;} public static int w(String t,String s){return 0;}}',
      "android/media/audiofx/Visualizer.java": 'package android.media.audiofx; public class Visualizer {public static boolean fail=true,silent=false; public static int opens,reads; public static final int SUCCESS=0,SCALING_MODE_AS_PLAYED=1; public Visualizer(int s){opens++;if(fail)throw new RuntimeException("DIRECT");} public static int[] getCaptureSizeRange(){return new int[]{8,512};} public boolean getEnabled(){return false;} public int setEnabled(boolean b){return 0;} public int setCaptureSize(int n){return 0;} public int setScalingMode(int n){return 0;} public int getWaveForm(byte[] a){reads++;for(int i=0;i<a.length;i++)a[i]=(byte)(silent?128:(i%2==0?90:166));return 0;} public void release(){}}',
      "com/boop/shieldhome/PlaybackDanceHarness.java": r'''package com.boop.shieldhome;
import android.os.*; import android.content.Context; import android.media.audiofx.Visualizer;
public class PlaybackDanceHarness {
 static int checks;
 static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static void stop(MusicBounceSource s){s.stop();while(!Handler.q.isEmpty())Handler.next();}
 public static void main(String[] args){
  MusicBounceSource s=new MusicBounceSource(new Context());s.setActive(true);Handler.next();
  check(Handler.delay>=5000,"blocked native capture is not polled every frame");
  float low=s.level(1000), high=s.level(1200), rest=s.level(1500);
  check(low==0 && high>.5f && rest==0,"fallback is a complete clock-driven hop and rest");
  check(!s.unavailable(),"working playback dance does not raise unavailable toast");
  int opens=Visualizer.opens; for(long t=1000;t<5000;t+=16)s.level(t);
  check(Visualizer.opens==opens,"render frames do not perform native or shell work");
  stop(s); check(s.level(1200)==0,"pause/inactive session stops immediately");
  SystemClock.now=8000;s.setActive(true);Handler.next();
  check(s.level(8000)==0 && s.level(8200)>.5f,"resume starts a fresh independent dance");
  Visualizer.fail=false;SystemClock.now=18000;Handler.next();
  byte[] actual=new byte[512];for(int i=0;i<actual.length;i++)actual[i]=(byte)(i%2==0?90:166);
  check(s.level(18000)==MusicBounceEnvelope.levelOf(actual),"native success restores exact original level mapping");
  check(Handler.delay==33,"native source retains original fast cadence");
  check(s.level(18300)==0,"stale native sample never animates");
  stop(s);
  Visualizer.fail=true;Context.permission=-1;s.setActive(true);Handler.next();
  check(s.level(20000)==0,"no sampling or fallback with revoked permission");
  stop(s);Context.permission=0;Visualizer.fail=false;Visualizer.silent=true;
  SystemClock.now=30000;s.setActive(true);Handler.next();SystemClock.now=31200;Handler.next();
  check(s.level(31400)>.5f,"silent native route can recover to playback dance");
  stop(s);
  check(PlaybackMusicDance.level(10,20)==0,"backwards clock is safe");
  for(long t=0;t<10000;t+=7){float v=PlaybackMusicDance.level(t,0);check(Float.isFinite(v)&&v>=0&&v<=1,"bounded clock curve");}
  long owner=BassCaptureState.start();s.setActive(true);
  BassCaptureState.publish(owner,.6f,40000);
  check(s.level(40000)==.6f && Handler.q.isEmpty(),"capture bypasses native sampler and synthetic fallback");
  check(s.level(40151)==0,"capture freshness guard");
  s.setActive(false);check(s.level(40000)==0,"paused view gates live capture");
  BassCaptureState.stop(owner);
  System.out.println(checks+" native/playback worker and curve checks passed");
 }
}'''
    }
    with tempfile.TemporaryDirectory() as d:
        files=[]
        for path,content in stubs.items():
            p=pathlib.Path(d)/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(content);files.append(str(p))
        files += [str(SRC/x) for x in ["MusicBounceSource.java","MusicBounceEnvelope.java","PlaybackMusicDance.java","BassCaptureState.java"]]
        subprocess.run(["javac","-d",d,*files],check=True)
        subprocess.run(["java","-cp",d,"com.boop.shieldhome.PlaybackDanceHarness"],check=True)
    allowed={
      # v207 explicitly separates application shells; focused split contracts cover these exact files.
      "unified/BoopProfileActivity.java",
      "unified/UnifiedEntryActivity.java",
      "unified/BoopDeviceProfile.java",
      "source/BoopAppIdentity.java",
      "source/BoopSetupState.java",
      "scripts/materialize-split.py",
      "scripts/audit-split-input.py",
      # v201 reviewed five-line modal visibility propagation; voice-surface regression covers this file.
      "source/BoopCanonicalFaceView.java",
      # v200 intentionally adds uniform TV menu focus chrome, opaque voice settings and audible natural pitch.
      "source/BoopNaturalSpeechBackend.java",
      "scripts/patch-unified-v148-single-face.py",
      "scripts/patch-unified-v200-voice-ui.py",
      "unified/shield-home/src/main/java/com/boop/shieldhome/BoopTvChrome.java",
      "unified/shield-home/src/main/java/com/boop/shieldhome/FocusChrome.java",
      # v198 explicitly adds Build a Boop voice tuning and separate opt-in voice-profile sharing.
      "source/BoopVoiceController.java",
      "source/SharedVoiceProfileProtocol.java",
      "source/SharedVoiceProfileState.java",
      "source/SharedVoiceProfileHaProtocol.java",
      "source/SharedVoiceProfileLink.java",
      "source/BoopSharedVoiceProfileRuntime.java",
      # v205 intentionally adjusts visible Home favourite banner spacing only.
      "unified/shield-home/src/main/java/com/boop/shieldhome/TvAppCardView.java",
      # v192+ Home layout/assistant hosting is separately scoped by Home tests.
      "unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeView.java",
      # v191 requested independent hand colour; locked eyes and sign geometry unchanged.
      "unified/animation/java/com/boop/eyes/HandColourPixels.java",
      "unified/animation/java/com/boop/eyes/HandColourWork.java",
      "unified/animation/java/com/boop/eyes/HandColourPreferences.java",
      "unified/animation/java/com/boop/eyes/HandColourBinding.java",
      "source/BoopSharedHandColourRuntime.java",
      "source/SharedHandColourProtocol.java",
      "source/SharedHandColourHaProtocol.java",
      "source/SharedHandColourLink.java",
      # v190 user-approved coherent felt signs; v189 default now separately hash-locked.
      "unified/animation/java/com/boop/eyes/NotificationSignView.java",
      "unified/animation/java/com/boop/eyes/FeltSignProp.java",
      "unified/animation/java/com/boop/eyes/FeltSignRig.java",
      "unified/animation/assets/boop-felt-sign-blank.png",
      "unified/animation/accepted-felt-v189.json",
      "scripts/verify-accepted-felt.py",
      "scripts/patch-unified-canonical-animations.py",
      # v187 user-requested current felt lab pause/seek; production animation remains unchanged.
      "source/AnimationReviewTimeline.java",
      "source/BoopCanonicalAnimationActivity.java",
      # v181 authorized photographic PNG rig.
      "unified/animation/java/com/boop/eyes/PngPuppetRig.java",
      "unified/animation/assets/boop-png-study.png",
      "unified/animation/assets/boop-hidden-felt.png",
      # v180 explicitly authorized independent felt colour channel.
      "source/SharedFeltColourProtocol.java",
      "source/SharedFeltColourHaProtocol.java",
      "source/SharedFeltColourLink.java",
      "source/BoopSharedFeltColourRuntime.java",
      "unified/UnifiedApplication.java",
      "unified/animation/java/com/boop/eyes/EyeColourBinding.java",
      "unified/animation/java/com/boop/eyes/FeltPalette.java",
      "unified/animation/java/com/boop/eyes/FeltColourPreferences.java",
      # v178 authorized fringe renderer and live appearance preview.
      "source/BoopAppearanceActivity.java",
      "unified/animation/java/com/boop/eyes/CanonicalEyeRenderer.java",
      "unified/animation/java/com/boop/eyes/FeltFringeMesh.java",
      "unified/animation/assets/felt-fringe.vert",
      "unified/animation/assets/felt-fringe.frag",
      # v177 user-approved lid material; scoped separately by test_felt_eyelids_v177.py.
      "unified/animation/assets/eyes.frag",
      "unified/JohnnyStatePolicy.java","unified/JohnnyStateProvider.java","scripts/materialize-unified.sh",
      "unified/app-build.gradle","unified/shield-home-manifest.xml",
      *{str((SRC/x).relative_to(ROOT)).replace("\\","/") for x in [
       "ShieldLyricsView.java","ShieldLyricsActivity.java","BassEnergy.java","BassOnset.java","PlaybackGroove.java","MusicBounceRenderer.java","BassCaptureState.java","BassCaptureActivity.java","BassCaptureService.java",
       "MusicBounceSource.java","MusicBounceEnvelope.java","ShieldNowPlayingPuppetView.java","ShieldHomeSettingsView.java"]}
    }
    # v208 permits only Ryan's artist-text styling delta, not arbitrary media UI edits.
    artist_path="unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingView.java"
    before=subprocess.check_output(["git","show","1b6816f611f88db67abf548eebe67057c01f5bab:"+artist_path],cwd=ROOT)
    old_colour=b"subtitle = text(18, Color.LTGRAY);"
    old_focus=(b"        subtitle.setOnFocusChangeListener((v, focused) -> subtitle.setTextColor(\n"
               b"                focused ? FocusChrome.accentColor(getContext()) : Color.LTGRAY));")
    assert before.count(old_colour)==1 and before.count(old_focus)==1,"Artist baseline changed"
    expected=before.replace(old_colour,b"subtitle = text(18, Color.WHITE);").replace(
        old_focus,b"        BoopTvChrome.useTextOnlyFocus(subtitle);")
    assert (ROOT/artist_path).read_bytes()==expected,"Now Playing changed beyond approved artist styling"
    allowed.add(artist_path)
    changed=subprocess.check_output(["git","diff","--name-only","33f3a77dd5c52f9ddfe3427ade66103ca4fcaa62","HEAD","--","source","unified","scripts","launcher","shield-overlay"],cwd=ROOT,text=True).splitlines()
    assert set(changed)<=allowed,"Unexpected production changes: "+str(set(changed)-allowed)
    print("Audio routing including HA lab, voice repair, renderer and approved artwork preserved")
if __name__=="__main__": main()
