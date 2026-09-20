#!/usr/bin/env python3
"""Execute materialized reply/onInit methods and the real Android backend with a delayed platform TTS."""
from pathlib import Path
import subprocess
import tempfile
import sys
import ast

ROOT = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1")
def method(text, signature):
    start = text.index(signature)
    brace = text.index("{", start)
    depth = 1
    end = brace + 1
    while depth:
        depth += (text[end] == "{") - (text[end] == "}")
        end += 1
    return text[start:end]

STUBS = {
"com/boop/shared/BoopState.java": """package com.boop.shared;
public class BoopState {public static final BoopState INSTANCE=new BoopState();public void speech(boolean listening,boolean speaking){}}""",

"android/os/Looper.java": """package android.os;
public class Looper { public static Looper getMainLooper() { return new Looper(); } }""",
"android/os/Handler.java": """package android.os;
import java.util.*;
public class Handler {
  static long now;
  static class Task { Runnable r; long at; Task(Runnable r,long at){this.r=r;this.at=at;} }
  static final List<Task> tasks=new ArrayList<>();
  public Handler(Looper l){}
  public boolean post(Runnable r){tasks.add(new Task(r,now));return true;}
  public boolean postDelayed(Runnable r,long ms){tasks.add(new Task(r,now+ms));return true;}
  public void removeCallbacks(Runnable r){tasks.removeIf(t->t.r==r);}
  public void removeCallbacksAndMessages(Object token){tasks.clear();}
  public static void advance(long ms){now+=ms;for(;;){Task found=null;for(Task t:tasks)if(t.at<=now){found=t;break;}if(found==null)return;tasks.remove(found);found.r.run();}}
  public static void reset(){now=0;tasks.clear();}
}""",
"android/util/Log.java": """package android.util;
public class Log {public static int i(String t,String s){return 0;} public static int w(String t,String s){return 0;} public static int w(String t,String s,Throwable x){return 0;}}""",
"android/speech/tts/UtteranceProgressListener.java": """package android.speech.tts;
public abstract class UtteranceProgressListener {
 public abstract void onStart(String id); public abstract void onDone(String id);
 public abstract void onError(String id); public void onError(String id,int code){onError(id);}
 public void onStop(String id,boolean interrupted){}
}""",
"android/speech/tts/TextToSpeech.java": """package android.speech.tts;
import java.util.*;
public class TextToSpeech {
 public interface OnInitListener {void onInit(int status);}
 public static final int SUCCESS=0, ERROR=-1, LANG_MISSING_DATA=-1, LANG_NOT_SUPPORTED=-2, QUEUE_FLUSH=0;
 public boolean ready, reject, missing; public int calls; public float pitch,rate;
 public String spoken,id; public UtteranceProgressListener listener;
 public int setOnUtteranceProgressListener(UtteranceProgressListener l){listener=l;return 0;}
 public int setLanguage(Locale l){return missing ? LANG_MISSING_DATA : ready ? 0 : ERROR;}
 public int setPitch(float p){pitch=p;return 0;} public int setSpeechRate(float r){rate=r;return 0;}
 public int speak(String s,int q,Object p,String id){calls++;if(!ready||reject)return ERROR;this.spoken=s;this.id=id;listener.onStart(id);return SUCCESS;}
 public int stop(){if(id!=null)listener.onStop(id,true);return 0;}
 public void complete(){listener.onDone(id);}
}""",
}
HARNESS = r"""
package com.boop.alpha1;
import android.speech.tts.TextToSpeech;
import android.os.Handler;
import java.util.Locale;
class StubActivity {protected void onPause(){}}
public class SpeechStartupHarness extends StubActivity implements TextToSpeech.OnInitListener {
 final MusicClient haClient=new MusicClient();
 static class MusicClient {boolean cancelled;void cancelMusicClarification(){cancelled=true;}}
 boolean activityInForeground=true;
 boolean assistantFollowUpAfterTts,sleepFaceAfterTts,assistantFollowUpListening,faceTouchActive;
 String latestAssistantFollowUpPartial;
 final Noop shakeDetector=new Noop(),recipeSession=new Noop();
 Noop face,sensorManager,recipePanel,notificationInPlaceController,presencePeekController,mirrorController,dockModeController,chatModeDialog;
 static class Noop {void stopListeningCue(){}void reset(){}void cancel(){}void cancelPending(){}void close(){}void dismiss(){}void onPause(){}void unregisterListener(Object x){}}
 static class BoopNotificationRuntime {static BoopNotificationRuntime get(Object x){return new BoopNotificationRuntime();}void unregisterWallHost(Object x){}}
 void cancelAssistantFollowUpSilenceTimeout(){}void closeWakeAudioSession(){}void cancelFaceHolds(){}void wakeFaceForInteraction(){}
 final Wake wakeCoordinator=new Wake();
 static class Wake {
  final BoopWakeSessionState state=new BoopWakeSessionState();
  void onTtsStarting(){state.setTtsSpeaking(true);}
  void onTtsFinished(){state.setTtsSpeaking(false);}
  void endForegroundSession(){state.endForegroundSession();}
  void resume(){state.beginForegroundSession();state.setWakeAllowed(true);state.setMicrophonePermission(true);state.setRecognitionSupported(true);}
 }
 BoopSpeechBackend naturalSpeechBackend;

 boolean ttsReady, finishing, destroyed;
 final TextToSpeech tts=new TextToSpeech();
 final BoopAndroidSpeechBackend androidSpeechBackend=new BoopAndroidSpeechBackend(tts);
 final VoiceController voiceController=new VoiceController();
 int completions;
 static class VoiceController {
  float pitch(){return 1.12f;} float speechRate(){return .96f;}
  void initialize(TextToSpeech t,Locale l){}
  boolean natural; boolean naturalBackendSelectedAndUsable(){return natural;}
  BoopVoiceController.NaturalVoice selectedNaturalVoice(){return new BoopVoiceController.NaturalVoice();}
 }
 boolean isFinishing(){return finishing;} boolean isDestroyed(){return destroyed;}
 void runOnUiThread(Runnable r){r.run();}
 void finishTtsUtterance(){completions++;wakeCoordinator.onTtsFinished();}
 __METHODS__
 static void check(boolean yes,String message){if(!yes)throw new AssertionError(message);}
 void ready(){tts.ready=true;onInit(TextToSpeech.SUCCESS);Handler.advance(0);}
 static SpeechStartupHarness fresh(){Handler.reset();SpeechStartupHarness a=new SpeechStartupHarness();a.wakeCoordinator.resume();return a;}
 static void cold(){
  SpeechStartupHarness a=fresh();a.speakWithAndroidTts("Done");
  check(a.completions==0,"reply discarded before speech engine became ready");
  check(a.tts.calls==0,"platform speak called before initialization");
  Handler.advance(3600);a.ready();
  check("Done".equals(a.tts.spoken),"queued reply was not played after readiness");
  check(a.completions==0,"assistant closed before playback completed");
  a.tts.complete();Handler.advance(0);
  check(a.completions==1,"completion was not delivered exactly once");
  a.tts.complete();Handler.advance(0);check(a.completions==1,"duplicate callback finished twice");
 }
 static void warm(){
  SpeechStartupHarness a=fresh();a.ready();a.speakWithAndroidTts("Done");
  check(a.tts.calls==1&&"Done".equals(a.tts.spoken),"ready engine failed to speak immediately");
  check(a.tts.pitch==1.12f&&a.tts.rate==.96f,"saved voice tuning was lost");
  check(a.completions==0,"warm reply completed before playback");
  a.tts.complete();Handler.advance(0);check(a.completions==1,"warm playback never completed");
 }
 static void timeout(){
  SpeechStartupHarness a=fresh();a.speakWithAndroidTts("Done");Handler.advance(11000);
  check(a.completions==1,"engine timeout did not end the reply once");
  a.ready();check(a.tts.calls==0,"timed-out reply replayed after late readiness");
  a.speakWithAndroidTts("Next");check("Next".equals(a.tts.spoken),"late ready engine cannot handle a new reply");
 }
 static void initFailure(){
  SpeechStartupHarness a=fresh();a.speakWithAndroidTts("Done");a.onInit(TextToSpeech.ERROR);Handler.advance(0);
  check(a.completions==1,"initialization failure did not end waiting reply");
  Handler.advance(11000);check(a.completions==1,"failure and timeout both finished reply");
 }
 static void languageFailure(){
  SpeechStartupHarness a=fresh();a.speakWithAndroidTts("Done");a.tts.missing=true;a.ready();
  check(a.completions==1&&a.tts.calls==0,"missing language did not fail safely");
 }
 static void replace(){
  SpeechStartupHarness a=fresh();a.speakWithAndroidTts("Old");a.speakWithAndroidTts("New");a.ready();
  check(a.tts.calls==1&&"New".equals(a.tts.spoken),"superseded pending reply was played");
  check(a.completions==0,"superseded reply closed the assistant");
 }
 static void cancel(){
  SpeechStartupHarness a=fresh();a.speakWithAndroidTts("Done");a.androidSpeechBackend.stop();a.ready();Handler.advance(11000);
  check(a.tts.calls==0&&a.completions==0,"cancelled reply spoke or completed later");
 }
 static void release(){
  SpeechStartupHarness a=fresh();a.speakWithAndroidTts("Done");a.destroyed=true;a.androidSpeechBackend.release();a.ready();Handler.advance(11000);
  check(a.tts.calls==0&&a.completions==0,"destroyed activity received a reply");
 }
 static void stale(){
  SpeechStartupHarness a=fresh();a.ready();a.speakWithAndroidTts("Old");String old=a.tts.id;
  a.speakWithAndroidTts("New");a.tts.listener.onDone(old);Handler.advance(0);
  check(a.completions==0,"stale playback callback closed newer reply");
  a.tts.complete();Handler.advance(0);check(a.completions==1,"new playback did not finish");
 }
 static void rejected(){
  SpeechStartupHarness a=fresh();a.tts.reject=true;a.speakWithAndroidTts("Done");a.ready();
  check(a.completions==1,"rejected queued speech left assistant waiting");
 }
 static class Natural implements BoopSpeechBackend {
  Callback callback;String text;int sid;float pitch,rate;
  public boolean speak(String text,int sid,float pitch,float rate,Callback cb){this.text=text;this.sid=sid;this.pitch=pitch;this.rate=rate;this.callback=cb;return true;}
  public void stop(){if(callback!=null)callback.onCancelled();}public void release(){}
 }
 static void natural(){
  SpeechStartupHarness a=fresh();Natural n=new Natural();a.naturalSpeechBackend=n;a.voiceController.natural=true;
  a.speak("Natural reply");
  check("Natural reply".equals(n.text)&&n.sid==21&&n.pitch==1.12f&&n.rate==.96f,"selected natural reply/tuning was not preserved");
  check(a.tts.calls==0&&a.completions==0,"natural reply used Android or finished early");
  n.callback.onDone();check(a.completions==1,"natural completion boundary was lost");
 }
 static void naturalFallback(){
  SpeechStartupHarness a=fresh();Natural n=new Natural();a.naturalSpeechBackend=n;a.voiceController.natural=true;
  a.speak("Same reply");n.callback.onError(new IllegalStateException("test platform failure"));
  check(a.completions==0&&a.tts.calls==0,"natural fallback was discarded before Android initialization");
  a.ready();check("Same reply".equals(a.tts.spoken),"natural fallback changed or lost reply");
  a.tts.complete();Handler.advance(0);check(a.completions==1,"fallback completion failed");
 }
 static void paused(){
  SpeechStartupHarness a=fresh();a.speak("Done");
  check(a.wakeCoordinator.state.state()==BoopWakeSessionState.State.SPEAKING,"test did not enter speaking state");
  a.activityInForeground=false;a.onPause();a.ready();Handler.advance(11000);
  if(__MUSIC_CANCEL__)check(a.haClient.cancelled,"pause retained a pending music clarification");
  a.activityInForeground=true;a.wakeCoordinator.resume();
  check(a.wakeCoordinator.state.state()==BoopWakeSessionState.State.ARMED,"cancelled reply left wake recognition stuck speaking");
  check(a.tts.calls==0&&a.completions==0,"paused reply spoke or fired completion");
 }
 static void late(){
  SpeechStartupHarness a=fresh();a.activityInForeground=false;a.onPause();
  a.speak("Late result");a.speakWithAndroidTts("Late fallback");a.ready();Handler.advance(11000);
  check(a.tts.calls==0&&a.completions==0,"late response played or completed while backgrounded");
 }
 static void naturalPaused(){
  SpeechStartupHarness a=fresh();Natural n=new Natural();a.naturalSpeechBackend=n;a.voiceController.natural=true;
  a.speak("Natural reply");
  check(a.wakeCoordinator.state.state()==BoopWakeSessionState.State.SPEAKING,"natural did not enter speaking");
  a.activityInForeground=false;a.onPause();
  a.activityInForeground=true;a.wakeCoordinator.resume();
  check(a.wakeCoordinator.state.state()==BoopWakeSessionState.State.ARMED,"natural pause left wake recognition stuck speaking");
  check(a.completions==0,"cancelling natural speech reported successful completion");
 }
 public static void main(String[] args){
  int failures=0;
  for(String name:new String[]{"cold","warm","timeout","initFailure","languageFailure","replace","cancel","release","stale","rejected","paused","late","natural","naturalFallback","naturalPaused"}){
   try{SpeechStartupHarness.class.getDeclaredMethod(name).invoke(null);System.out.println("PASS "+name);}
   catch(Throwable x){failures++;System.out.println("FAIL "+name+": "+x.getCause());}
  }
  if(failures>0)throw new AssertionError(failures+" speech lifecycle regressions");
  System.out.println("15 speech startup/playback scenarios passed");
 }
}
"""
if "--source-only" in sys.argv:
    ROOT = Path("source")
    main = (ROOT/"MainActivity.java").read_text()
    nodes = ast.parse(Path("scripts/patch-unified-natural-voices.py").read_text()).body
    speech = next(ast.literal_eval(n.value) for n in nodes if isinstance(n, ast.Assign)
                  and any(isinstance(x, ast.Name) and x.id == "new_speak" for x in n.targets))
    main = main.replace(method(main, "private void speak(String text)"), speech.strip(), 1)
    patch = Path("scripts/patch-unified-voice-startup.py").resolve()
    if patch.exists():
        with tempfile.TemporaryDirectory() as fixture:
            target = Path(fixture)/"boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java"
            target.parent.mkdir(parents=True)
            target.write_text(main)
            subprocess.run([sys.executable,str(patch)],cwd=fixture,check=True)
            main = target.read_text()
else:
    main = (ROOT/"MainActivity.java").read_text()

with tempfile.TemporaryDirectory() as folder:
    out = Path(folder)
    for path, content in STUBS.items():
        target=out/path
        target.parent.mkdir(parents=True,exist_ok=True)
        target.write_text(content)
    pkg=out/"com/boop/alpha1"
    pkg.mkdir(parents=True)
    for name in ("BoopAndroidSpeechBackend.java","BoopSpeechBackend.java","BoopVoiceTuning.java","BoopWakeSessionState.java"):
        (pkg/name).write_text((ROOT/name).read_text())
    (pkg/"BoopVoiceController.java").write_text("package com.boop.alpha1;class BoopVoiceController {static class NaturalVoice {int sid(){return 21;}}}")
    methods=method(main,"private void speakWithAndroidTts(String text)")+"\n"+method(main,"public void onInit(int status)")+"\n"+method(main,"private void speak(String text)")+"\n"+method(main,"protected void onPause()")
    (pkg/"SpeechStartupHarness.java").write_text(HARNESS.replace("__METHODS__",methods).replace(
        "__MUSIC_CANCEL__",str("haClient.cancelMusicClarification()" in main).lower()))
    subprocess.run(["javac","-d",str(out),*[str(p) for p in out.rglob("*.java")]],check=True)
    subprocess.run(["java","-cp",str(out),"com.boop.alpha1.SpeechStartupHarness"],check=True)
