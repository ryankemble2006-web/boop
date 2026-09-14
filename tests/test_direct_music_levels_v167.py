"""Non-visual real-code tests for Shield direct-output level parsing."""
import pathlib, subprocess, tempfile
ROOT = pathlib.Path(__file__).resolve().parents[1]
SRC = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome"
HARNESS = r'''
package com.boop.shieldhome;
import java.time.*;
public class DirectLevelsHarness {
 static int checks;
 static long now = ZonedDateTime.of(2026,9,14,16,40,6,0,ZoneId.systemDefault()).toInstant().toEpochMilli();
 static String row(String levels) { return "       09-14 16:40:05.900: "+levels+"\n"; }
 static String thread(boolean standby, String rows) {
  return "Output thread 0x1, name AudioOut_1, type 1 (DIRECT):\n  Standby: "+(standby?"yes":"no")+
   "\n  Sample rate: 44100 Hz\n      Signal power history:\n"+rows+"  1 Tracks of which 1 are active\n";
 }
 static void check(boolean value,String name) { checks++; if(!value) throw new AssertionError(name); }
 static float read(String text) { return DirectMusicLevels.parse(text,now); }
 public static void main(String[] args) {
  float quiet=read(thread(false,row("-40.0 -30.0")));
  float loud=read(thread(false,row("-40.0 -10.0")));
  check(quiet>0 && loud>quiet && loud<=1,"real last reading follows loudness");
  check(read(thread(false,row("-10.0 -100.0")))==0,"real silence returns zero");
  check(read(thread(true,row("-2.0")))<0,"standby history ignored");
  check(read(thread(false,"       09-14 16:39:50.000: -2.0\n"))<0,"stale history ignored");
  check(read(thread(false,"       09-14 16:40:09.000: -2.0\n"))<0,"future history ignored");
  check(read("Historical Thread Log\n"+thread(false,row("-2.0")))<0,"historical output ignored");
  check(read("Input thread x:\n Standby: no\n Signal power history:\n"+row("-2.0"))<0,"microphone ignored");
  check(read(thread(true,row("-2.0"))+thread(false,row("-30.0")))==quiet,"inactive output cannot mask active");
  check(read(thread(false,row("[ -10.0 -30.0 ] sum(99.9)")))==quiet,"sum is not a sample");
  check(read(thread(false,row("NaN Infinity garbage")))<0,"malformed rejected");
  check(read(thread(false,row("-130.0")))<0,"out of range rejected");
  check(read(thread(false,row("-2.0"))+"Input thread x:\n Signal power history:\n"+row("-100.0"))>0,"input cannot replace output");
  check(read(thread(false,row("-2.0"))+"Historical Thread Log\n"+thread(false,row("-100.0")))>0,"history cannot replace live");
  check(read(thread(false,row("-5.0")+row("-30.0")))==quiet,"last line selected");
  check(read(null)<0 && read("Permission Denial")<0,"unavailable rejected");
  long jan=ZonedDateTime.of(2027,1,1,0,0,0,100000000,ZoneId.systemDefault()).toInstant().toEpochMilli();
  check(DirectMusicLevels.parse(thread(false,"       12-31 23:59:59.900: -10.0\n"),jan)>0,"year boundary");
  System.out.println(checks+" actual diagnostic parser scenarios passed");
 }
}
'''
def main():
    source = SRC / "DirectMusicLevels.java"
    assert source.exists(), "Direct HDMI audio has no diagnostic level fallback parser"
    with tempfile.TemporaryDirectory() as d:
        harness=pathlib.Path(d)/"DirectLevelsHarness.java"
        harness.write_text(HARNESS)
        subprocess.run(["javac","-d",d,str(source),str(harness)],check=True)
        subprocess.run(["java","-cp",d,"com.boop.shieldhome.DirectLevelsHarness"],check=True)
    # Exact existing renderer behavior is covered by the inherited Java harness.
if __name__=="__main__": main()

# Compile production worker and diagnostic transport adapter against deterministic platform boundaries.
def lifecycle():
    import textwrap
    stubs = {
      "android/Manifest.java": 'package android; public class Manifest { public static class permission { public static final String RECORD_AUDIO="record"; }}',
      "android/content/pm/PackageManager.java": 'package android.content.pm; public class PackageManager { public static final int PERMISSION_GRANTED=0; }',
      "android/content/Context.java": 'package android.content; public class Context { public static int permission=0; public static java.io.File dir; public Context getApplicationContext(){return this;} public int checkSelfPermission(String x){return permission;} public java.io.File getNoBackupFilesDir(){return dir;} }',
      "android/os/SystemClock.java": 'package android.os; public class SystemClock { public static long now=1000; public static long uptimeMillis(){return now;} }',
      "android/os/Process.java": 'package android.os; public class Process { public static final int THREAD_PRIORITY_BACKGROUND=10; }',
      "android/os/HandlerThread.java": 'package android.os; public class HandlerThread { public HandlerThread(String n,int p){} public void start(){} public Object getLooper(){return null;} public void quitSafely(){} }',
      "android/os/Handler.java": 'package android.os; public class Handler { public static java.util.ArrayDeque<Runnable> q=new java.util.ArrayDeque<>(); public Handler(Object o){} public void post(Runnable r){q.add(r);} public void postDelayed(Runnable r,long n){q.add(r);} public void removeCallbacks(Runnable r){q.removeIf(x->x==r);} public static void next(){q.remove().run();} }',
      "android/util/Log.java": 'package android.util; public class Log { public static int i(String t,String m){return 0;} public static int w(String t,String m){return 0;} }',
      "android/media/audiofx/Visualizer.java": 'package android.media.audiofx; public class Visualizer { public static boolean fail=true, silent=false; public static final int SUCCESS=0,SCALING_MODE_AS_PLAYED=1; public Visualizer(int s){if(fail)throw new RuntimeException("DIRECT");} public static int[] getCaptureSizeRange(){return new int[]{8,512};} public boolean getEnabled(){return false;} public int setEnabled(boolean b){return 0;} public int setCaptureSize(int s){return 0;} public int setScalingMode(int s){return 0;} public int getWaveForm(byte[] a){for(int i=0;i<a.length;i++)a[i]=(byte)(silent?128:(i%2==0?90:166)); return 0;} public void release(){} }',
      "com/boop/shieldturbo/power/AdbWire.java": '''package com.boop.shieldturbo.power;
import java.io.*; import java.security.*;
public class AdbWire implements Closeable {
 public static int dumps,connections,closes; public static float db=-30f; public static boolean stale,allow;
 public static KeyPair identity(File f){return null;}
 public void connect(int port,KeyPair k,int timeout,Runnable prompt,boolean newApproval) throws Exception {
   if(port!=5555||newApproval)throw new AssertionError("must reuse trusted loopback identity"); connections++;
 }
 public static class Result { public int exitCode=0; public String output; Result(String s){output=s;} }
 public Result execute(String command,int timeout) throws IOException {
  if(command.equals("id -u")) return new Result("2000");
  if(!command.equals("dumpsys media.audio_flinger"))throw new AssertionError("unexpected command");
  dumps++;
  String stamp=new java.text.SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new java.util.Date(System.currentTimeMillis()-(stale?5000:50)));
  return new Result("Output thread x, type 1 (DIRECT):\\n Standby: no\\n Signal power history:\\n "+stamp+": -30.0 "+db+"\\n");
 }
 public void close() throws IOException {closes++;}
}''',
      "com/boop/shieldhome/WorkerHarness.java": '''package com.boop.shieldhome;
import android.content.Context; import android.os.Handler; import android.os.SystemClock;
import android.media.audiofx.Visualizer; import com.boop.shieldturbo.power.AdbWire;
public class WorkerHarness {
 static void check(boolean b,String name){if(!b)throw new AssertionError(name);}
 static void stop(MusicBounceSource s){s.stop(); while(!Handler.q.isEmpty())Handler.next();}
 public static void main(String[] args) throws Exception {
  Context.dir=new java.io.File(args[0]); Context.dir.mkdirs();
  new java.io.File(Context.dir,"boop-unified-local-adb.key").createNewFile();
  MusicBounceSource s=new MusicBounceSource(new Context()); s.setActive(true); Handler.next();
  check(!s.unavailable(),"DIRECT rejection reaches real diagnostic reader");
  check(AdbWire.dumps==1 && AdbWire.connections==1,"one trusted connection and one bounded read");
  s.level(SystemClock.now); AdbWire.db=-10f; SystemClock.now+=150; Handler.next();
  check(s.level(SystemClock.now)>0,"real diagnostic onset reaches renderer-facing pulse");
  SystemClock.now+=300; check(s.level(SystemClock.now)==0,"stale worker sample expires");
  Handler.next(); check(AdbWire.connections==1,"connection reused");
  AdbWire.db=-30f; Handler.next(); s.level(SystemClock.now);
  SystemClock.now+=350; check(s.level(SystemClock.now)==0,"aged input cannot keep puppet raised");
  AdbWire.db=-2f; Handler.next();
  check(s.level(SystemClock.now)>0,"spaced diagnostic measurements retain onset baseline");
  Context.permission=-1; int before=AdbWire.dumps; Handler.next();
  check(AdbWire.dumps==before && s.level(SystemClock.now)==0,"revoked audio access stops diagnostic reads");
  Context.permission=0; Handler.next();
  check(AdbWire.dumps==before+1 && !s.unavailable(),"regrant resumes real levels");
  stop(s); check(s.level(SystemClock.now)==0 && AdbWire.closes>0,"stop closes diagnostic transport");
  Context.permission=0; AdbWire.stale=true;
  s=new MusicBounceSource(new Context()); s.setActive(true); Handler.next();
  check(s.level(SystemClock.now)==0 && s.unavailable(),"stale diagnostic becomes unavailable");
  stop(s); AdbWire.stale=false; Visualizer.fail=false; before=AdbWire.dumps;
  s=new MusicBounceSource(new Context()); s.setActive(true); Handler.next();
  check(!s.unavailable() && AdbWire.dumps==before,"working visualizer retains existing path");
  Visualizer.silent=true; Handler.next(); SystemClock.now+=1200; Handler.next();
  check(AdbWire.dumps>before && !s.unavailable(),"silent visualizer after route switch recovers real direct levels");
  stop(s);
  System.out.println("13 actual worker/transport lifecycle scenarios passed");
 }
}'''
    }
    with tempfile.TemporaryDirectory() as d:
        files=[]
        for path,content in stubs.items():
            target=pathlib.Path(d)/path
            target.parent.mkdir(parents=True,exist_ok=True)
            target.write_text(textwrap.dedent(content))
            files.append(str(target))
        files += [str(SRC/n) for n in ["MusicBounceSource.java","MusicBounceEnvelope.java","DirectMusicSource.java","DirectMusicLevels.java","MusicBeatPulse.java"]]
        subprocess.run(["javac","-d",d,*files],check=True)
        subprocess.run(["java","-cp",d,"com.boop.shieldhome.WorkerHarness",str(pathlib.Path(d)/"identity")],check=True)

if __name__=="__main__":
    lifecycle()

def preservation():
    allowed = {
      "unified/app-build.gradle",
      "unified/shield-home/src/main/java/com/boop/shieldhome/MusicBeatPulse.java",
      "unified/shield-home/src/main/java/com/boop/shieldhome/MusicBounceSource.java",
      "unified/shield-home/src/main/java/com/boop/shieldhome/MusicBounceRenderer.java",
      "unified/shield-home/src/main/java/com/boop/shieldhome/DirectMusicSource.java",
      "unified/shield-home/src/main/java/com/boop/shieldhome/DirectMusicLevels.java",
    }
    changed = subprocess.check_output(["git","diff","--name-only","9acfd266e394a0a9fa6b8b16910628fba5f24179","HEAD","--","source","unified","scripts","launcher","shield-overlay","shield-clean-launcher"],cwd=ROOT,text=True).splitlines()
    assert set(changed)<=allowed, "Unexpected change outside dance input: "+str(set(changed)-allowed)
    print("Accepted voice, audio routing, artwork and independent animation sources preserved")
if __name__=="__main__": preservation()

def beat_pulses():
    source=SRC/"MusicBeatPulse.java"
    assert source.exists(), "Music still tracks continuous loudness instead of distinct onset-driven hops"
    harness=r'''
package com.boop.shieldhome;
public class BeatPulseHarness {
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 public static void main(String[] args){
  MusicBeatPulse p=new MusicBeatPulse();
  check(p.update(.2f,1000,1000)==0,"first level establishes baseline");
  check(p.update(.6f,1100,1100)>.5f,"sharp real rise makes a hop");
  check(p.update(.6f,1100,1250)>0,"hop decays between input samples");
  check(p.update(.6f,1350,1350)==0,"steady loud music does not hold puppet up");
  check(p.update(.1f,1400,1400)==0,"falling volume does not trigger");
  check(p.update(.7f,1500,1500)>.5f,"next rising beat makes new hop");
  float before=p.update(.1f,1520,1520);
  check(p.update(.9f,1540,1540)<=before,"refractory window rejects rapid double triggers");
  check(p.update(.9f,1540,1800)==0,"same sample cannot retrigger");
  check(p.update(Float.NaN,1810,1810)==0,"invalid level clears pulse");
  check(p.update(.8f,1400,2000)==0,"old sample ignored");
  p.reset();
  check(p.update(.8f,2010,2010)==0,"reset discards prior musical state");
  System.out.println("11 actual onset-hop scenarios passed");
 }
}
'''
    with tempfile.TemporaryDirectory() as d:
        h=pathlib.Path(d)/"BeatPulseHarness.java"; h.write_text(harness)
        subprocess.run(["javac","-d",d,str(source),str(h)],check=True)
        subprocess.run(["java","-cp",d,"com.boop.shieldhome.BeatPulseHarness"],check=True)
if __name__=="__main__": beat_pulses()
