"""Run the production session observer against controlled Android boundary doubles.

This tests event/subscription behavior, not appearance. The app code, recording
policy, immutable snapshot and transport capability policy are compiled unchanged.
No network, device permissions, commercial lyrics or production test hooks.
"""
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
LAB = ROOT / 'lyrics-lab/app/src/main/java/com/boop/shieldhome'
SHARED = ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome'
STUBS = {
'android/graphics/Bitmap.java': 'package android.graphics; public class Bitmap {}',
'android/content/ComponentName.java': 'package android.content; public class ComponentName { public ComponentName(Context c,Class<?> cls){} }',
'android/app/NotificationManager.java': '''package android.app;
public class NotificationManager { public boolean allowed=true;
 public boolean isNotificationListenerAccessGranted(android.content.ComponentName c){return allowed;} }
''',
'android/content/Context.java': '''package android.content;
public class Context {
 public final android.app.NotificationManager access=new android.app.NotificationManager();
 public final android.media.session.MediaSessionManager sessions=new android.media.session.MediaSessionManager();
 public Context getApplicationContext(){return this;}
 public <T> T getSystemService(Class<T> type){return type.cast(type==android.app.NotificationManager.class?access:sessions);}
}
''',
'android/os/Looper.java': 'package android.os; public class Looper { public static Looper getMainLooper(){return new Looper();} }',
'android/os/Handler.java': '''package android.os; public class Handler {
 public Handler(Looper looper){} public boolean post(Runnable r){r.run();return true;}
}''',
'android/os/SystemClock.java': 'package android.os; public class SystemClock { public static long elapsedRealtime(){return 10000;} }',
'android/media/MediaMetadata.java': '''package android.media;
import java.util.HashMap;
public class MediaMetadata {
 public static final String METADATA_KEY_TITLE="title",METADATA_KEY_DISPLAY_TITLE="displayTitle",
 METADATA_KEY_ARTIST="artist",METADATA_KEY_DISPLAY_SUBTITLE="displayArtist",METADATA_KEY_ALBUM_ARTIST="albumArtist",
 METADATA_KEY_DURATION="duration",METADATA_KEY_ALBUM="album";
 private final HashMap<String,Object> data=new HashMap<>();
 public MediaMetadata put(String key,Object value){data.put(key,value);return this;}
 public CharSequence getText(String key){return (CharSequence)data.get(key);}
 public long getLong(String key){Object v=data.get(key);return v instanceof Number?((Number)v).longValue():0;}
}
''',
'android/media/session/PlaybackState.java': '''package android.media.session;
public class PlaybackState {
 public static final int STATE_NONE=0,STATE_STOPPED=1,STATE_PAUSED=2,STATE_PLAYING=3,
 STATE_FAST_FORWARDING=4,STATE_REWINDING=5,STATE_BUFFERING=6,STATE_ERROR=7,STATE_CONNECTING=8,
 STATE_SKIPPING_TO_PREVIOUS=9,STATE_SKIPPING_TO_NEXT=10,STATE_SKIPPING_TO_QUEUE_ITEM=11;
 public static final long ACTION_STOP=1,ACTION_PAUSE=2,ACTION_PLAY=4,ACTION_REWIND=8,
 ACTION_SKIP_TO_PREVIOUS=16,ACTION_SKIP_TO_NEXT=32,ACTION_FAST_FORWARD=64,ACTION_SEEK_TO=256,ACTION_PLAY_PAUSE=512;
 public final int state; public long position=1000; public long actions=1023;
 public PlaybackState(int state){this.state=state;}
 public int getState(){return state;} public long getActions(){return actions;}
 public long getPosition(){return position;} public float getPlaybackSpeed(){return state==3?1:0;}
 public long getLastPositionUpdateTime(){return 10000;}
}
''',
'android/media/session/MediaSession.java': '''package android.media.session;
public class MediaSession { public static class Token {
 final String id; public Token(String id){this.id=id;}
 @Override public boolean equals(Object other){return other instanceof Token&&id.equals(((Token)other).id);}
 @Override public int hashCode(){return id.hashCode();}
} }
''',
'android/media/session/MediaController.java': '''package android.media.session;
import java.util.ArrayList;
import java.util.List;
import android.media.MediaMetadata;
public class MediaController {
 public static class Backend {
  public final MediaSession.Token token; public final String pkg;
  public PlaybackState state=new PlaybackState(3); public MediaMetadata metadata;
  public int registrations, removals, commands; public long sought=-1;
  private final List<Callback> listeners=new ArrayList<>();
  public Backend(String id,String pkg){token=new MediaSession.Token(id);this.pkg=pkg;}
  public void state(int state){this.state=state<0?null:new PlaybackState(state);
   for(Callback callback:new ArrayList<>(listeners))callback.onPlaybackStateChanged(this.state);}
  public void metadata(MediaMetadata value){metadata=value;
   for(Callback callback:new ArrayList<>(listeners))callback.onMetadataChanged(value);}
  public void destroyed(){for(Callback callback:new ArrayList<>(listeners))callback.onSessionDestroyed();}
 }
 private final Backend backend;
 public MediaController(Backend backend){this.backend=backend;}
 public String getPackageName(){return backend.pkg;} public MediaSession.Token getSessionToken(){return backend.token;}
 public PlaybackState getPlaybackState(){return backend.state;} public MediaMetadata getMetadata(){return backend.metadata;}
 public void registerCallback(Callback cb,android.os.Handler handler){backend.registrations++;backend.listeners.add(cb);}
 public void unregisterCallback(Callback cb){backend.removals++;backend.listeners.remove(cb);}
 public TransportControls getTransportControls(){return new TransportControls();}
 public class TransportControls {
  public void play(){backend.commands++;backend.state(3);} public void pause(){backend.commands++;backend.state(2);}
  public void skipToPrevious(){backend.commands++;} public void skipToNext(){backend.commands++;}
  public void seekTo(long target){backend.commands++;backend.sought=target;}
 }
 public static class Callback {
  public void onMetadataChanged(MediaMetadata metadata){} public void onPlaybackStateChanged(PlaybackState state){}
  public void onSessionDestroyed(){}
 }
}
''',
'android/media/session/MediaSessionManager.java': '''package android.media.session;
import java.util.ArrayList;
import java.util.List;
public class MediaSessionManager {
 public final List<MediaController.Backend> active=new ArrayList<>();
 private final List<OnActiveSessionsChangedListener> observers=new ArrayList<>();
 public interface OnActiveSessionsChangedListener { void onActiveSessionsChanged(List<MediaController> list); }
 public void addOnActiveSessionsChangedListener(OnActiveSessionsChangedListener l,android.content.ComponentName c,android.os.Handler h){observers.add(l);}
 public void removeOnActiveSessionsChangedListener(OnActiveSessionsChangedListener l){observers.remove(l);}
 public List<MediaController> getActiveSessions(android.content.ComponentName c){
  List<MediaController> result=new ArrayList<>();for(MediaController.Backend b:active)result.add(new MediaController(b));return result;
 }
 public void changed(){for(OnActiveSessionsChangedListener l:new ArrayList<>(observers))l.onActiveSessionsChanged(getActiveSessions(null));}
}
''',
'com/boop/shieldhome/BoundaryStubs.java': '''package com.boop.shieldhome;
final class LyricsLabMediaListener {}
final class NowPlayingArtworkResolver {
 NowPlayingArtworkResolver(android.content.Context c,android.os.Handler h,Runnable done){}
 android.graphics.Bitmap resolve(android.media.MediaMetadata m,android.graphics.Bitmap fallback){return null;}
 void clear(){}
}
''',
'com/boop/shieldhome/LyricsSessionCheck.java': '''package com.boop.shieldhome;
import android.content.Context;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import java.util.ArrayList;
import java.util.List;
public final class LyricsSessionCheck {
 static int checks;
 static void equal(Object expected,Object actual,String why){
  if(!java.util.Objects.equals(expected,actual))throw new AssertionError(why+": expected "+expected+", got "+actual);checks++;
 }
 static MediaMetadata metadata(String id){return new MediaMetadata()
  .put(MediaMetadata.METADATA_KEY_TITLE,"Invented track "+id).put(MediaMetadata.METADATA_KEY_ARTIST,"Fixture artist")
  .put(MediaMetadata.METADATA_KEY_ALBUM,"Fixture album").put(MediaMetadata.METADATA_KEY_DURATION,120000L)
  .put("com.deezer.METADATA_KEY_PLAYABLE_IDENTIFIER_TYPE","TRACK")
  .put("com.deezer.METADATA_KEY_PLAYABLE_IDENTIFIER_ID",id);}
 static final class Fixture {
  final Context context=new Context(); final MediaController.Backend player=new MediaController.Backend("live","deezer.android.app");
  final LyricsLabSession session; LyricsLabSession.Frame latest; boolean access; int events;
  Fixture() throws Exception {
   player.metadata=metadata("123");context.sessions.active.add(player);
   java.lang.reflect.Constructor<LyricsLabSession> ctor=LyricsLabSession.class.getDeclaredConstructor(Context.class);ctor.setAccessible(true);
   session=ctor.newInstance(context);start();
  }
  void start(){session.start((frame,allowed)->{latest=frame;access=allowed;events++;});}
  String id(){return latest==null?null:latest.trackId;}
 }
 public static void main(String[] args)throws Exception {
  for(int transition:new int[]{0,1,7,9,10,11,4,5,-1,6,8}){
   Fixture f=new Fixture();equal("123",f.id(),"Initial recording");long session=f.latest.snapshot.sessionId();
   f.player.state(transition);
   f.player.metadata(metadata("456"));f.player.state(3);
   equal("456",f.id(),"Active session must deliver new track after transient state "+transition+" without activity restart");
   equal(session,f.latest.snapshot.sessionId(),"Same Android token keeps the same observer identity");
   equal(1,f.player.registrations,"Do not recreate listener for a transport state");
   equal(0,f.player.removals,"Do not disconnect the only source of the next-track callback");
   f.session.stop();
  }
  Fixture f=new Fixture();
  f.player.metadata(metadata("789"));equal("789",f.id(),"Natural next-track metadata event without a button press");
  f.session.toggle();equal(2,f.latest.snapshot.playbackState(),"Pause callback reaches screen");
  f.session.toggle();equal(3,f.latest.snapshot.playbackState(),"Resume callback reaches screen");
  f.session.seek(10000);equal(11000L,f.player.sought,"Forward ten seconds");
  f.session.seek(-10000);equal(0L,f.player.sought,"Backward seek clamps at zero");
  f.player.state.position=119000;f.player.state(2);f.player.state.position=119000;f.context.sessions.changed();
  f.session.seek(10000);equal(120000L,f.player.sought,"Seek clamps at duration");
  int commands=f.player.commands;f.session.next();f.session.previous();equal(commands+2,f.player.commands,"Both transport commands reach player");
  f.player.metadata(null);equal(null,f.latest,"Temporary missing metadata clears old track");
  f.player.metadata(metadata("101"));equal("101",f.id(),"Metadata recovery does not require reopening");
  MediaController.Backend unrelated=new MediaController.Backend("cast","com.google.android.apps.mediashell");
  unrelated.metadata=metadata("999");f.context.sessions.active.add(0,unrelated);f.context.sessions.changed();
  equal("101",f.id(),"Other players never acquire Deezer lyrics");
  f.context.access.allowed=false;f.context.sessions.changed();equal(false,f.access,"Revoked access reported");
  int events=f.events;f.player.metadata(metadata("202"));equal(events,f.events,"Revoked observer detached");
  f.context.access.allowed=true;f.start();equal("202",f.id(),"Fresh foreground start reacquires consented access");
  f.context.sessions.active.remove(f.player);f.context.sessions.changed();equal(null,f.latest,"Removed session cannot retain old track");
  events=f.events;f.player.metadata(metadata("303"));equal(events,f.events,"Old removed session cannot publish");
  MediaController.Backend replacement=new MediaController.Backend("replacement","deezer.android.app");
  replacement.metadata=metadata("404");f.context.sessions.active.add(replacement);f.context.sessions.changed();
  equal("404",f.id(),"Replacement session attaches without reopening");
  f.session.stop();events=f.events;replacement.metadata(metadata("505"));f.context.sessions.changed();
  equal(events,f.events,"Hidden/stopped lab receives no events");
  f.start();equal("505",f.id(),"Return synchronizes latest track");f.session.stop();
  System.out.println("PASS: "+checks+" production session observation/transport/lifecycle assertions.");
 }
}
'''
}
with tempfile.TemporaryDirectory(prefix='boop-lyrics-session-') as folder:
    root=Path(folder)
    files=[]
    for name,content in STUBS.items():
        file=root/name
        file.parent.mkdir(parents=True,exist_ok=True)
        file.write_text(content,encoding='utf-8')
        files.append(str(file))
    files += [str(LAB/'LyricsLabSession.java'), str(LAB/'LyricsLabMediaPolicy.java'),
              str(SHARED/'NowPlayingSnapshot.java'),str(SHARED/'NowPlayingActionPolicy.java')]
    subprocess.run(['javac','-encoding','UTF-8','-d',folder,*files],check=True)
    subprocess.run(['java','-cp',folder,'com.boop.shieldhome.LyricsSessionCheck'],check=True)
