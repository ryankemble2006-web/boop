"""Exercise real entry control flow with minimal Android/transport boundary stubs, not visual checks."""
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome'
STUBS = {
 'android/app/Activity.java': '''package android.app; public class Activity {
 public boolean focused=true; public int launches;
 public boolean hasWindowFocus(){return focused;}
 public boolean isFinishing(){return false;} public boolean isDestroyed(){return false;}
 public void startActivity(android.content.Intent intent){launches++;}
 public void overridePendingTransition(int a,int b){}
 }''',
 'android/content/Intent.java': '''package android.content; public class Intent {
 public Intent(android.app.Activity activity,Class<?> target){}
 }''',
 'android/widget/Toast.java': '''package android.widget; public class Toast {
 public static final int LENGTH_SHORT=0;
 public static Toast makeText(android.app.Activity activity,String text,int length){return new Toast();}
 public void show(){}
 }''',
 'android/R.java': '''package android; public final class R { public static final class anim {
 public static final int fade_in=1,fade_out=2; } }''',
 'com/boop/shieldhome/BoundaryStubs.java': '''package com.boop.shieldhome;
 import java.util.ArrayList; import java.util.List; import java.util.function.Consumer;
 final class NativeLyricsLoader {
 static NativeLyricsLoader latest; int loads; final List<Consumer<DeezerLyricsDocument>> callbacks=new ArrayList<>();
 NativeLyricsLoader(){latest=this;}
 void load(String id,String identity,Consumer<DeezerLyricsDocument> done){loads++;callbacks.add(done);}
 void cancel(){} void destroy(){}
 }
 final class DeezerLyricsDocument {
 enum Status {AVAILABLE,UNAVAILABLE,UNKNOWN} Status status(){return Status.AVAILABLE;}
 }
 final class NowPlayingSnapshot {
 final String id; NowPlayingSnapshot(String id){this.id=id;}
 String packageName(){return "deezer.android.app";} long sessionId(){return 1;}
 }
 final class ShieldNowPlayingManager {
 String id="123";
 String deezerLyricsTrackId(NowPlayingSnapshot requested){return requested.id.equals(id)?id:"";}
 }
 final class ShieldLyricsActivity {}
 ''',
 'com/boop/shieldhome/NativeLyricsEntryCheck.java': '''package com.boop.shieldhome;
 public final class NativeLyricsEntryCheck {
 static int checks;
 static void equal(int expected,int actual,String why){if(expected!=actual)throw new AssertionError(why+": "+actual);checks++;}
 public static void main(String[] args){
  android.app.Activity activity=new android.app.Activity();
  ShieldNowPlayingManager manager=new ShieldNowPlayingManager();
  NowPlayingSnapshot track=new NowPlayingSnapshot("123");
  DeezerLyricsBrowser browser=new DeezerLyricsBrowser(); NativeLyricsLoader loader=NativeLyricsLoader.latest;
  browser.open(activity,manager,track); browser.open(activity,manager,track);
  equal(1,loader.loads,"Repeated press coalesces one pending lookup");
  activity.focused=false; loader.callbacks.get(0).accept(new DeezerLyricsDocument());
  equal(0,activity.launches,"Losing host focus must not open a late screen");
  activity.focused=true; browser.open(activity,manager,track);
  equal(2,loader.loads,"A completed hidden-host lookup must not permanently latch the Lyrics button");
  loader.callbacks.get(1).accept(new DeezerLyricsDocument());
  equal(1,activity.launches,"Fresh press after focus returns opens BOOP lyrics");
  browser.open(activity,manager,track); int old=loader.callbacks.size()-1;
  manager.id="456"; browser.onTrackChanged(manager);
  browser.open(activity,manager,new NowPlayingSnapshot("456")); int fresh=loader.callbacks.size()-1;
  loader.callbacks.get(old).accept(new DeezerLyricsDocument());
  equal(1,activity.launches,"Late previous recording cannot open lyrics");
  loader.callbacks.get(fresh).accept(new DeezerLyricsDocument());
  equal(2,activity.launches,"New recording still opens");
  browser.open(activity,manager,new NowPlayingSnapshot("456")); int paused=loader.callbacks.size()-1;
  browser.onHostPaused(); loader.callbacks.get(paused).accept(new DeezerLyricsDocument());
  equal(2,activity.launches,"Paused host rejects late completion");
  System.out.println("PASS: "+checks+" real Lyrics entry control-flow checks.");
 }
 }'''
}
with tempfile.TemporaryDirectory(prefix='boop-lyrics-entry-') as folder:
    root = Path(folder)
    files = []
    for name, content in STUBS.items():
        file = root / name
        file.parent.mkdir(parents=True, exist_ok=True)
        file.write_text(content, encoding='utf-8')
        files.append(str(file))
    subprocess.run(['javac', '-encoding', 'UTF-8', '-d', folder, *files,
                    str(SOURCE / 'DeezerLyricsBrowser.java'), str(SOURCE / 'DeezerLyricsPolicy.java')], check=True)
    subprocess.run(['java', '-cp', folder, 'com.boop.shieldhome.NativeLyricsEntryCheck'], check=True)
