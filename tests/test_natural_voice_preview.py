"""Run the production backend against controllable audio and native boundaries."""
from pathlib import Path
import subprocess
import hashlib
import json
import struct

ROOT = Path(__file__).resolve().parents[1]


def test_shipped_demos_match_the_verified_model_and_all_four_voices():
    folder = ROOT / "natural-voices/previews"
    receipt = json.loads((folder / "receipt.json").read_text(encoding="utf-8"))
    pack = json.loads((ROOT / "natural-voices/manifest.json").read_text(encoding="utf-8"))
    assert receipt["archiveSha256"] == pack["sha256"]
    assert receipt["packVersion"] == pack["version"]
    assert receipt["sampleRate"] == 24000
    assert receipt["generationSpeed"] == 1.0
    assert receipt["silenceScale"] == 0.2
    assert len(receipt["clips"]) == 8
    for voice in pack["voices"]:
        clips = [c for c in receipt["clips"] if c["speakerId"] == voice["sid"]]
        assert {c["text"] for c in clips} == {f"Hello. I'm {voice['name']}.", "This is how BOOP sounds."}
        for clip in clips:
            raw = (folder / clip["file"]).read_bytes()
            assert hashlib.sha256(raw).hexdigest() == clip["sha256"]
            assert len(raw) == 2 * clip["samples"]
            assert 24000 < clip["samples"] < 8 * 24000
            samples = struct.unpack(f"<{clip['samples']}h", raw)
            assert max(map(abs, samples)) > 1000, "Demo is silent"
            first = next(i for i, sample in enumerate(samples) if abs(sample) > 150)
            assert first < 0.3 * 24000, "Leading silence reintroduces preview latency"


def test_preview_does_not_wait_for_neural_inference(tmp_path):
    stubs = {
        "android/os/SystemClock.java": "package android.os; public class SystemClock { public static long elapsedRealtime(){return System.nanoTime()/1000000;} }",
        "android/util/Log.java": "package android.util; public class Log {public static int i(String t,String s){return 0;} public static int w(String t,String s,Throwable e){return 0;} }",
        "android/media/AudioManager.java": "package android.media; public class AudioManager {public static final int AUDIO_SESSION_ID_GENERATE=0;}",
        "android/media/AudioFormat.java": "package android.media; public class AudioFormat {public static final int CHANNEL_OUT_MONO=1,ENCODING_PCM_16BIT=2; public static class Builder {public Builder setEncoding(int n){return this;}public Builder setSampleRate(int n){return this;}public Builder setChannelMask(int n){return this;}public AudioFormat build(){return new AudioFormat();}}}",
        "android/media/AudioAttributes.java": "package android.media; public class AudioAttributes {public static final int USAGE_ASSISTANCE_ACCESSIBILITY=1,CONTENT_TYPE_SPEECH=1;public static class Builder {public Builder setUsage(int n){return this;}public Builder setContentType(int n){return this;}public AudioAttributes build(){return new AudioAttributes();}}}",
        "android/media/PlaybackParams.java": "package android.media; public class PlaybackParams {public float pitch,speed;public PlaybackParams allowDefaults(){return this;}public PlaybackParams setPitch(float n){pitch=n;return this;}public PlaybackParams setSpeed(float n){speed=n;return this;}}",
        "android/media/AudioTrack.java": '''package android.media;
import java.util.concurrent.*;
public class AudioTrack {
 public static final int MODE_STATIC=0,STATE_UNINITIALIZED=0,PLAYSTATE_PLAYING=3,WRITE_BLOCKING=0;
 public static final BlockingQueue<AudioTrack> played=new LinkedBlockingQueue<>();
 public static volatile boolean hold=false; public PlaybackParams params; public int length; public volatile boolean released;
 public static int getMinBufferSize(int a,int b,int c){return 4096;} public int getState(){return 1;}
 public int write(short[] p,int o,int n,int b){if(released)throw new AssertionError("write after release");length=n;return n;}
 public void setPlaybackParams(PlaybackParams p){if(released)throw new AssertionError("params after release");params=p;}
 public void play(){if(released)throw new AssertionError("play after release");played.add(this);}
 public void release(){released=true;} public void pause(){} public void flush(){} public void stop(){}
 public int getPlayState(){return PLAYSTATE_PLAYING;} public int getPlaybackHeadPosition(){return hold?0:length;}
 public static class Builder {public Builder setAudioAttributes(AudioAttributes a){return this;}public Builder setAudioFormat(AudioFormat a){return this;}public Builder setBufferSizeInBytes(int n){return this;}public Builder setTransferMode(int n){return this;}public Builder setSessionId(int n){return this;}public AudioTrack build(){return new AudioTrack();}}
}''',
        "com/boop/alpha1/BoopNaturalVoicePack.java": '''package com.boop.alpha1;
import java.io.*;
class BoopNaturalVoicePack {final File root; boolean installed=true; boolean brokenPreview;
 BoopNaturalVoicePack(File r){root=r;} boolean isInstalled(){return installed;} File activeDirectory(){return root;}
 InputStream openPreview(String name)throws IOException {if(brokenPreview)throw new IOException("missing");return new ByteArrayInputStream(new byte[48000]);}
}''',
        "com/k2fsa/sherpa/onnx/GenerationConfig.java": "package com.k2fsa.sherpa.onnx; public class GenerationConfig {public float speed;public void setSid(int n){}public void setSpeed(float n){speed=n;}public void setSilenceScale(float n){}}",
        "com/k2fsa/sherpa/onnx/GeneratedAudio.java": "package com.k2fsa.sherpa.onnx;public class GeneratedAudio {public float[] getSamples(){return new float[24000];}public int getSampleRate(){return 24000;}}",
        "com/k2fsa/sherpa/onnx/OfflineTtsKokoroModelConfig.java": "package com.k2fsa.sherpa.onnx;public class OfflineTtsKokoroModelConfig {public void setModel(String s){}public void setVoices(String s){}public void setTokens(String s){}public void setDataDir(String s){}public void setLexicon(String s){}}",
        "com/k2fsa/sherpa/onnx/OfflineTtsModelConfig.java": "package com.k2fsa.sherpa.onnx;public class OfflineTtsModelConfig {public void setKokoro(OfflineTtsKokoroModelConfig c){}public void setNumThreads(int n){}public void setDebug(boolean b){}public void setProvider(String s){}}",
        "com/k2fsa/sherpa/onnx/OfflineTtsConfig.java": "package com.k2fsa.sherpa.onnx;public class OfflineTtsConfig {public void setModel(OfflineTtsModelConfig c){}public void setSilenceScale(float f){}}",
        "com/k2fsa/sherpa/onnx/OfflineTts.java": '''package com.k2fsa.sherpa.onnx;
import java.util.concurrent.*;
public class OfflineTts {
 public static int opened,generated;public static float speed;public static volatile boolean busy,released;
 public static CountDownLatch entered=new CountDownLatch(1),gate=new CountDownLatch(0),closed=new CountDownLatch(1);
 public OfflineTts(Object a,OfflineTtsConfig c){opened++;} public int sampleRate(){return 24000;}public int numSpeakers(){return 54;}
 public GeneratedAudio generateWithConfig(String text,GenerationConfig config){generated++;speed=config.speed;busy=true;entered.countDown();try{gate.await();}catch(InterruptedException e){throw new RuntimeException(e);}finally{busy=false;}if(released)throw new AssertionError("native released during synthesis");return new GeneratedAudio();}
 public void release(){if(busy)throw new AssertionError("native release raced synthesis");released=true;closed.countDown();}
}''',
        "com/boop/alpha1/VoicePreviewHarness.java": '''package com.boop.alpha1;
import java.io.*;import java.nio.file.*;import java.util.concurrent.*;import android.media.*;import com.k2fsa.sherpa.onnx.*;
public class VoicePreviewHarness {
 static class Result implements BoopSpeechBackend.Callback {int done,cancel;Throwable error;CountDownLatch terminal=new CountDownLatch(1);public void onDone(){done++;terminal.countDown();}public void onError(Throwable t){error=t;terminal.countDown();}public void onCancelled(){cancel++;terminal.countDown();}}
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 static AudioTrack played()throws Exception{AudioTrack t=AudioTrack.played.poll(500,TimeUnit.MILLISECONDS);check(t!=null,"Preview waited for model/synthesis instead of starting immediately");return t;}
 static void done(Result r)throws Exception{check(r.terminal.await(1,TimeUnit.SECONDS),"missing callback");check(r.error==null,"speech failed: "+r.error);}
 public static void main(String[] args)throws Exception {
  File root=Files.createTempDirectory("voice-pack").toFile();for(String n:new String[]{"model.onnx","voices.bin","tokens.txt","lexicon-gb-en.txt","espeak-ng-data/phontab"}){File f=new File(root,n);f.getParentFile().mkdirs();Files.write(f.toPath(),new byte[]{1});}
  BoopNaturalVoicePack pack=new BoopNaturalVoicePack(root);BoopNaturalSpeechBackend backend=new BoopNaturalSpeechBackend(pack);
  Result first=new Result();check(backend.speak("Hello. I'm Emma.",21,1.45f,0.7f,first),"preview rejected");AudioTrack a=played();done(first);
  check(OfflineTts.opened==0 && OfflineTts.generated==0,"Fixed previews invoked the neural model");
  check(a.params.pitch==1.45f && a.params.speed==0.7f,"sliders not applied independently");
  check(first.done==1,"preview completion count");
  Result whimsy=new Result();backend.speak("This is how BOOP sounds.",21,2.90f,2.50f,whimsy);AudioTrack w=played();done(whimsy);
  check(w.params.pitch==2.90f && w.params.speed==2.50f,"Doubled pitch/cadence silently clamped");
  check(OfflineTts.opened==0 && OfflineTts.generated==0,"Maximum whimsy lost instant preview path");
  // A live utterance is blocked inside native code. A demo must still replace it immediately.
  OfflineTts.gate=new CountDownLatch(1);Result live=new Result();backend.speak("A new generated reply.",21,1.12f,1.25f,live);
  check(OfflineTts.entered.await(1,TimeUnit.SECONDS),"no synthesis");AudioTrack.hold=true;
  Result replacement=new Result();backend.speak("This is how BOOP sounds.",26,0.75f,1.25f,replacement);AudioTrack b=played();
  done(live);check(live.cancel==1 && live.done==0,"replaced speech reported success");
  check(b.params.pitch==0.75f && b.params.speed==1.25f,"latest tuning lost");
  Result third=new Result();backend.speak("Hello. I'm Fable.",25,1.3f,0.9f,third);AudioTrack c=played();done(replacement);
  check(replacement.cancel==1 && replacement.done==0 && b.released,"old preview survived replacement");
  backend.stop();done(third);check(third.cancel==1 && third.done==0 && c.released,"stop did not cancel preview");
  backend.release();check(!OfflineTts.released,"release raced native inference");OfflineTts.gate.countDown();
  check(OfflineTts.closed.await(1,TimeUnit.SECONDS),"native model leaked after safe release");
  check(AudioTrack.played.poll(100,TimeUnit.MILLISECONDS)==null,"cancelled synthesis played late");
  check(OfflineTts.speed==1f,"generated replies and previews use different cadence paths");
  check(!backend.speak("Hello. I'm Emma.",21,1f,1f,new Result()),"closed backend accepted speech");
  System.out.println("PASS fast previews, independent sliders, replacement, cancellation and native lifetime");
 }
}''',
    }
    for name, body in stubs.items():
        path = tmp_path / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(body, encoding="utf-8")
    for name in ["BoopNaturalSpeechBackend", "BoopSpeechBackend", "BoopVoiceTuning", "BoopNaturalVoicePreview"]:
        path = ROOT / "source" / (name + ".java")
        if path.exists():
            (tmp_path / "com/boop/alpha1" / path.name).write_text(path.read_text(encoding="utf-8"), encoding="utf-8")
    subprocess.run(["javac", "-d", str(tmp_path), *map(str, tmp_path.rglob("*.java"))], check=True, capture_output=True, text=True)
    result = subprocess.run(["java", "-cp", str(tmp_path), "com.boop.alpha1.VoicePreviewHarness"], timeout=15, capture_output=True, text=True)
    assert result.returncode == 0, result.stdout + result.stderr
