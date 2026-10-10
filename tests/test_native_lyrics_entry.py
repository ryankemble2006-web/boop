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
 void load(NowPlayingSnapshot track,String id,String identity,Consumer<DeezerLyricsDocument> done){load(id,identity,done);}
 void reload(NowPlayingSnapshot track,String id,String identity,Consumer<DeezerLyricsDocument> done){load(id,identity,done);}
 void cancel(){} void destroy(){}
 }
 final class DeezerLyricsDocument {
 enum Status {AVAILABLE,UNAVAILABLE,UNKNOWN} Status value=Status.AVAILABLE; Status status(){return value;}
 DeezerLyricsDocument(){} DeezerLyricsDocument(Status s){value=s;}
 }
 final class NowPlayingSnapshot {
 final String id,album; final long duration; NowPlayingSnapshot(String id){this(id,"",218000);}
 NowPlayingSnapshot(String id,String album,long duration){this.id=id;this.album=album;this.duration=duration;}
 String album(){return album;} long durationMs(){return duration;}
 String packageName(){return "deezer.android.app";} long sessionId(){return 1;} String trackKey(){return id;}
 }
 final class ShieldNowPlayingManager {
 String id="123",album=""; long duration=218000; boolean metadataOnly;
 NowPlayingState state(){return new NowPlayingState(new NowPlayingSnapshot(id,album,duration));}
 String deezerLyricsTrackId(NowPlayingSnapshot requested){return !metadataOnly&&requested.id.equals(id)?id:"";}
 }
 final class NowPlayingState {final NowPlayingSnapshot snapshot; NowPlayingState(NowPlayingSnapshot s){snapshot=s;} NowPlayingSnapshot current(){return snapshot;}}
 final class ShieldLyricsActivity {}
 final class LyricsLookupDialog {
 static LyricsLookupDialog latest; boolean showing; final Consumer<NowPlayingSnapshot> search; Consumer<Object> dismiss;
 LyricsLookupDialog(android.app.Activity a,NowPlayingSnapshot s,String message,Consumer<NowPlayingSnapshot> search){latest=this;this.search=search;}
 void setOnDismissListener(Consumer<Object> c){dismiss=c;} void show(){showing=true;} boolean isShowing(){return showing;}
 void dismiss(){showing=false;}
 void submit(NowPlayingSnapshot query){dismiss();search.accept(query);if(dismiss!=null)dismiss.accept(this);}
 }
 ''',
 'com/boop/shieldhome/NativeLyricsEntryCheck.java': """package com.boop.shieldhome;
 public final class NativeLyricsEntryCheck {
 static void equal(int expected,int actual,String why){if(expected!=actual)throw new AssertionError(why+": "+actual);}
 public static void main(String[] args){
  android.app.Activity activity=new android.app.Activity();
  ShieldNowPlayingManager manager=new ShieldNowPlayingManager();
  DeezerLyricsBrowser browser=new DeezerLyricsBrowser();
  browser.open(activity,manager,new NowPlayingSnapshot("123"));
  equal(1,activity.launches,"Home must open presentation immediately without preflight lookup");
  if(NativeLyricsLoader.latest!=null && NativeLyricsLoader.latest.loads!=0)throw new AssertionError("Lookup belongs to presentation");
  activity.focused=false;browser.open(activity,manager,new NowPlayingSnapshot("456"));
  equal(1,activity.launches,"Hidden host cannot launch presentation");
  activity.focused=true;browser.open(activity,manager,new NowPlayingSnapshot("456"));
  equal(2,activity.launches,"Entry recovers after focus returns");
  browser.open(activity,manager,null);equal(2,activity.launches,"Null track ignored");
  browser.onHostPaused();browser.onTrackChanged(manager);browser.destroy();
  System.out.println("PASS immediate Home lyrics/spectrum entry");
 }
 }"""

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
