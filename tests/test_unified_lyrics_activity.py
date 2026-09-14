"""Exercise the unchanged internal Activity and real Unified state bus at Android boundaries.

Controlled service/renderer/loader doubles isolate the integration. These are
functional JVM assertions, not device, audio, rendering or catalogue acceptance.
"""
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome'
STUBS = {
'android/app/Activity.java': '''package android.app;
public class Activity {
 private boolean finishing;
 protected void onPause(){} protected void onCreate(android.os.Bundle state){} protected void onStart(){} protected void onStop(){} protected void onDestroy(){}
 public android.view.Window getWindow(){return new android.view.Window();}
 public void setContentView(android.view.View view){} public boolean isFinishing(){return finishing;}
 public void finish(){finishing=true;} public void overridePendingTransition(int a,int b){}
 public boolean dispatchKeyEvent(android.view.KeyEvent e){if(e.getKeyCode()==4){finish();return true;}return false;}
}''',
'android/os/Bundle.java': 'package android.os; public class Bundle {}',
'android/R.java': 'package android; public class R { public static class anim {public static final int fade_in=1,fade_out=2;} }',
'android/view/View.java': '''package android.view; public class View {
 public static final int SYSTEM_UI_FLAG_FULLSCREEN=1,SYSTEM_UI_FLAG_HIDE_NAVIGATION=2,SYSTEM_UI_FLAG_IMMERSIVE_STICKY=4,
 SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN=8,SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION=16,SYSTEM_UI_FLAG_LAYOUT_STABLE=32;
 public void setSystemUiVisibility(int flags){}
}''',
'android/view/Window.java': 'package android.view; public class Window {public void addFlags(int f){} public View getDecorView(){return new View();}}',
'android/view/WindowManager.java': 'package android.view; public class WindowManager {public static class LayoutParams {public static final int FLAG_KEEP_SCREEN_ON=128;}}',
'android/view/KeyEvent.java': '''package android.view; public class KeyEvent {
 public static final int ACTION_DOWN=0,KEYCODE_MEDIA_PLAY_PAUSE=85,KEYCODE_MEDIA_NEXT=87,KEYCODE_MEDIA_PREVIOUS=88;
 private final int key,repeat; public KeyEvent(int key,int repeat){this.key=key;this.repeat=repeat;}
 public int getAction(){return 0;} public int getRepeatCount(){return repeat;} public int getKeyCode(){return key;}
}''',
'android/media/session/PlaybackState.java': '''package android.media.session; public class PlaybackState {
 public long getPosition(){return 1000;} public long getLastPositionUpdateTime(){return 100;}
}''',
'android/media/session/MediaSession.java': 'package android.media.session; public class MediaSession { public static class Token {} }',
'android/media/session/MediaController.java': '''package android.media.session; public class MediaController {
 public MediaController(android.app.Activity activity,MediaSession.Token token){}
 public PlaybackState getPlaybackState(){return new PlaybackState();}
}''',
'com/boop/shieldhome/BoundaryStubs.java': '''package com.boop.shieldhome;
import java.util.ArrayList; import java.util.List; import java.util.function.Consumer;
final class NowPlayingSnapshot {
 final String track,pkg; final int state; final long session;
 NowPlayingSnapshot(String track,String pkg,int state,long session){this.track=track;this.pkg=pkg;this.state=state;this.session=session;}
 String packageName(){return pkg;} long sessionId(){return session;} int playbackState(){return state;}
}
final class NowPlayingSelectionPolicy {static boolean eligible(int state){return state==2||state==3||state==6||state==8;}}
final class FocusChrome {static int accentColor(android.app.Activity activity){return 0;}}
final class DeezerLyricsDocument {
 enum Status {AVAILABLE,UNAVAILABLE,UNKNOWN} final String id; final Status value;
 DeezerLyricsDocument(String id,Status value){this.id=id;this.value=value;} Status status(){return value;}
}
final class NativeLyricsLoader {
 static NativeLyricsLoader latest;
 final List<Consumer<DeezerLyricsDocument>> callbacks=new ArrayList<>(); final List<String> ids=new ArrayList<>();
 boolean destroyed; int cancellations;
 NativeLyricsLoader(){latest=this;}
 void load(String id,String identity,Consumer<DeezerLyricsDocument> done){ids.add(id);callbacks.add(done);}
 void cancel(){cancellations++;} void destroy(){destroyed=true;}
 void reply(int index,DeezerLyricsDocument.Status result){callbacks.get(index).accept(new DeezerLyricsDocument(ids.get(index),result));}
}
final class DeezerAlbumBrowser {
 static int opens,cancellations; static NowPlayingSnapshot requested;
 void open(android.app.Activity a,ShieldNowPlayingManager m,NowPlayingSnapshot s){opens++;requested=s;}
 void cancel(){cancellations++;}
}
final class ShieldNowPlayingManager {
 static ShieldNowPlayingManager instance; final NowPlayingState bus=new NowPlayingState();
 int refreshes,previous,next,toggle,sourceOpens; boolean openSource(android.app.Activity activity){sourceOpens++;return true;} long seek;
 static ShieldNowPlayingManager get(android.app.Activity activity){return instance;}
 NowPlayingState state(){return bus;} void refreshAccess(){refreshes++;}
 String deezerLyricsTrackId(NowPlayingSnapshot snapshot){return DeezerLyricsPolicy.available(snapshot.pkg)?snapshot.track:"";}
 android.media.session.MediaSession.Token sessionToken(long id){return new android.media.session.MediaSession.Token();}
 void previous(){previous++;} void next(){next++;} void togglePlayPause(){toggle++;} void seekBy(long delta){seek=delta;}
}
final class ShieldLyricsView extends android.view.View {
 interface Controls {void previous();void playPause();void next();void seek(long delta);void close();default void browseAlbum(){} }
 static ShieldLyricsView latest; final Controls controls; NowPlayingSnapshot snapshot; DeezerLyricsDocument document;
 String status=""; boolean running; int updates;
 ShieldLyricsView(android.app.Activity activity,int accent,Controls controls){latest=this;this.controls=controls;}
 void setRunning(boolean value){running=value;}
 void setSnapshot(NowPlayingSnapshot snapshot,boolean known){this.snapshot=snapshot;updates++;}
 void setDocument(DeezerLyricsDocument value){document=value;} void setStatus(String text){status=text;}
}
''',
'com/boop/shieldhome/UnifiedLyricsActivityCheck.java': '''package com.boop.shieldhome;
public final class UnifiedLyricsActivityCheck {
 static int checks;
 static void eq(Object a,Object b,String why){if(!java.util.Objects.equals(a,b))throw new AssertionError(why+": expected "+a+", got "+b);checks++;}
 static NowPlayingSnapshot track(String id){return new NowPlayingSnapshot(id,"deezer.android.app",3,1);}
 public static void main(String[] args){
  ShieldNowPlayingManager manager=new ShieldNowPlayingManager();ShieldNowPlayingManager.instance=manager;
  manager.bus.update(track("123"));
  ShieldLyricsActivity activity=new ShieldLyricsActivity();activity.onCreate(null);activity.onStart();
  NativeLyricsLoader loader=NativeLyricsLoader.latest;ShieldLyricsView view=ShieldLyricsView.latest;
  eq(1,loader.ids.size(),"Existing Unified recording loads on entry");eq("123",loader.ids.get(0),"Exact ID from Unified manager");
  loader.reply(0,DeezerLyricsDocument.Status.AVAILABLE);eq("123",view.document.id,"Initial timed document");
  manager.bus.update(new NowPlayingSnapshot("123","deezer.android.app",2,1));
  eq(1,loader.ids.size(),"Pause updates snapshot without redownloading");eq(2,view.snapshot.playbackState(),"Pause reaches renderer");
  manager.bus.update(null);eq(null,view.document,"Transition clears previous lyrics");
  manager.bus.update(track("456"));eq(2,loader.ids.size(),"Transition recovers on same open Activity");
  loader.reply(0,DeezerLyricsDocument.Status.AVAILABLE);eq(null,view.document,"Previous-recording response discarded");
  loader.reply(1,DeezerLyricsDocument.Status.AVAILABLE);eq("456",view.document.id,"New lyrics replace old without reopening");
  manager.bus.update(track("789"));loader.reply(2,DeezerLyricsDocument.Status.UNAVAILABLE);
  eq(null,view.document,"Absent lyrics do not retain previous recording");eq("No lyrics for this track.",view.status,"Absent state reported");
  manager.bus.update(track("101"));loader.reply(3,DeezerLyricsDocument.Status.UNKNOWN);
  eq("Couldn't check lyrics just now.",view.status,"Service uncertainty is not catalogue absence");
  manager.bus.update(track("202"));loader.reply(4,DeezerLyricsDocument.Status.AVAILABLE);
  eq("202",view.document.id,"Later available track recovers automatically");
  int requests=loader.ids.size();manager.bus.update(new NowPlayingSnapshot("303","other.player",3,2));
  eq(requests,loader.ids.size(),"Non-Deezer never requests a Deezer ID");eq(null,view.document,"Other player clears lyrics");
  manager.bus.update(track("404"));int waiting=loader.ids.size()-1;activity.onStop();int updates=view.updates;
  loader.reply(waiting,DeezerLyricsDocument.Status.AVAILABLE);eq(null,view.document,"Hidden Activity rejects late completion");
  manager.bus.update(track("505"));eq(updates,view.updates,"Stopped Activity unsubscribes from real state bus");eq(false,view.running,"Hidden drawing stopped");
  activity.onStart();eq("505",loader.ids.get(loader.ids.size()-1),"Return uses latest Unified recording");
  loader.reply(loader.ids.size()-1,DeezerLyricsDocument.Status.AVAILABLE);eq("505",view.document.id,"Return restores current lyrics");
  view.controls.previous();view.controls.next();view.controls.playPause();view.controls.seek(10000);
  eq(1,manager.previous,"Previous uses Unified manager");eq(1,manager.next,"Next uses Unified manager");
  eq(1,manager.toggle,"Play/Pause uses Unified manager");eq(10000L,manager.seek,"Seek delta preserved");
  activity.dispatchKeyEvent(new android.view.KeyEvent(87,0));eq(2,manager.next,"Hardware Next forwarded");
  activity.dispatchKeyEvent(new android.view.KeyEvent(87,1));eq(2,manager.next,"Repeated hardware event not duplicated");
  view.controls.browseAlbum();eq(1,DeezerAlbumBrowser.opens,"Album click reaches existing browser");
  eq("505",DeezerAlbumBrowser.requested.track,"Album uses current selected recording");
  manager.bus.update(track("606"));view.controls.browseAlbum();
  eq("606",DeezerAlbumBrowser.requested.track,"Album click reads latest snapshot");
  manager.bus.update(null);view.controls.browseAlbum();
  eq(2,DeezerAlbumBrowser.opens,"Null selection cannot browse stale album");
  manager.bus.update(new NowPlayingSnapshot("707","other.player",3,2));view.controls.browseAlbum();
  eq(1,manager.sourceOpens,"Other player uses source fallback");
  int cancelled=DeezerAlbumBrowser.cancellations;activity.onPause();
  eq(cancelled+1,DeezerAlbumBrowser.cancellations,"Pause cancels pending album lookup");
  cancelled=DeezerAlbumBrowser.cancellations;activity.onStop();
  eq(cancelled+1,DeezerAlbumBrowser.cancellations,"Stop cancels pending album lookup");
  activity.dispatchKeyEvent(new android.view.KeyEvent(4,0));eq(true,activity.isFinishing(),"Back retains Activity default path");
  activity.onStop();activity.onDestroy();eq(true,loader.destroyed,"Destroyed owner releases loader");
  System.out.println("PASS: "+checks+" internal Unified lyrics Activity/state/lifecycle/transport assertions.");
 }
}
'''
}
with tempfile.TemporaryDirectory(prefix='boop-unified-lyrics-activity-') as folder:
    root=Path(folder)
    files=[]
    for name,content in STUBS.items():
        path=root/name
        path.parent.mkdir(parents=True,exist_ok=True)
        path.write_text(content,encoding='utf-8')
        files.append(str(path))
    files.extend(str(SRC/name) for name in ('ShieldLyricsActivity.java','NowPlayingState.java','DeezerLyricsPolicy.java'))
    subprocess.run(['javac','-encoding','UTF-8','-d',folder,*files],check=True)
    subprocess.run(['java','-cp',folder,'com.boop.shieldhome.UnifiedLyricsActivityCheck'],check=True)
