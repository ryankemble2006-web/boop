"""Deterministic Android boundary doubles, never shipped in the application.

These model callback delivery separately from commands so tests must supply
provider confirmation; dispatch deliberately cannot manufacture a saved state.
"""
STUBS = {
'android/content/Context.java': '''package android.content;
public class Context { public Context getApplicationContext() { return this; } }''',
'android/graphics/Bitmap.java': 'package android.graphics; public class Bitmap {}',
'android/os/Looper.java': '''package android.os;
public final class Looper {
 private static final Looper MAIN = new Looper();
 public static Looper getMainLooper(){return MAIN;}
 public static Looper myLooper(){return MAIN;}
}''',
'android/os/SystemClock.java': '''package android.os;
public final class SystemClock { public static long now; public static long elapsedRealtime(){return now;} }''',
'android/os/Bundle.java': 'package android.os; public final class Bundle {}',
'android/os/Handler.java': '''package android.os;
import java.util.*;
public final class Handler {
 private static final Map<Runnable,Long> tasks=new LinkedHashMap<>();
 public Handler(Looper ignored){}
 public boolean post(Runnable r){r.run();return true;}
 public boolean postDelayed(Runnable r,long delay){tasks.put(r,SystemClock.now+delay);return true;}
 public void removeCallbacks(Runnable r){tasks.remove(r);}
 public static void advance(long ms){SystemClock.now+=ms;
  for(Runnable r:new ArrayList<>(tasks.keySet()))if(tasks.containsKey(r)&&tasks.get(r)<=SystemClock.now){tasks.remove(r);r.run();}
 }
}''',
'android/widget/Toast.java': '''package android.widget;
import android.content.Context; import java.util.*;
public final class Toast {
 public static final int LENGTH_SHORT=0; public static final List<String> messages=new ArrayList<>();
 private String text;
 public static Toast makeText(Context c,String text,int duration){Toast t=new Toast();t.text=text;return t;}
 public void show(){messages.add(text);}
}''',
'android/media/Rating.java': '''package android.media;
public final class Rating {
 public static final int RATING_HEART=1,RATING_THUMB_UP_DOWN=2;
 private final int style;private final boolean rated,heart;
 public Rating(int s,boolean rated,boolean heart){style=s;this.rated=rated;this.heart=heart;}
 public static Rating newHeartRating(boolean heart){return new Rating(RATING_HEART,true,heart);}
 public static Rating newUnratedRating(int style){return new Rating(style,false,false);}
 public int getRatingStyle(){return style;}public boolean isRated(){return rated;}public boolean hasHeart(){return heart;}
}''',
'android/media/MediaMetadata.java': '''package android.media;
import java.util.*;
public final class MediaMetadata {
 public static final String METADATA_KEY_TITLE="title",METADATA_KEY_DISPLAY_TITLE="displayTitle",
 METADATA_KEY_ARTIST="artist",METADATA_KEY_DISPLAY_SUBTITLE="subtitle",METADATA_KEY_ALBUM_ARTIST="albumArtist",
 METADATA_KEY_ALBUM="album",METADATA_KEY_DURATION="duration",METADATA_KEY_MEDIA_ID="id",METADATA_KEY_USER_RATING="rating";
 public final Map<String,Object> data=new HashMap<>();
 public CharSequence getText(String k){return (CharSequence)data.get(k);}
 public long getLong(String k){return data.containsKey(k)?((Number)data.get(k)).longValue():0;}
 public Rating getRating(String k){return (Rating)data.get(k);}
}''',
'android/media/session/PlaybackState.java': '''package android.media.session;
import java.util.*;
public final class PlaybackState {
 public static final int STATE_PAUSED=2,STATE_PLAYING=3;
 public static final long ACTION_SKIP_TO_PREVIOUS=16,ACTION_SKIP_TO_NEXT=32,ACTION_REWIND=8,
 ACTION_FAST_FORWARD=64,ACTION_PAUSE=2,ACTION_PLAY=4,ACTION_PLAY_PAUSE=512,ACTION_SEEK_TO=256,ACTION_SET_RATING=128;
 public long actions; public final List<CustomAction> custom=new ArrayList<>();
 public long getActions(){return actions;}public List<CustomAction> getCustomActions(){return custom;}
 public static final class CustomAction {
  public final String id,label; public CustomAction(String id,String label){this.id=id;this.label=label;}
  public String getAction(){return id;}public CharSequence getName(){return label;}
 }
}''',
'android/media/session/MediaSession.java': '''package android.media.session;
public final class MediaSession { public static final class Token { public final long id;public Token(long id){this.id=id;}
 public boolean equals(Object o){return o instanceof Token&&id==((Token)o).id;}public int hashCode(){return Long.hashCode(id);}
}}''',
'android/media/session/MediaController.java': '''package android.media.session;
import android.content.*;import android.media.*;import android.os.*;import java.util.*;
public final class MediaController {
 public static final class Session {
  public MediaMetadata metadata;public PlaybackState playback=new PlaybackState();public int ratingType;
  public final List<Callback> callbacks=new ArrayList<>();public final List<Rating> ratings=new ArrayList<>();
  public final List<String> commands=new ArrayList<>(); public boolean throwing;
  public void changed(){for(Callback c:new ArrayList<>(callbacks))c.onMetadataChanged(metadata);}
  public void actionsChanged(){for(Callback c:new ArrayList<>(callbacks))c.onPlaybackStateChanged(playback);}
  public void destroy(){for(Callback c:new ArrayList<>(callbacks))c.onSessionDestroyed();}
 }
 public static final Map<Long,Session> sessions=new HashMap<>();private final Session s;
 public MediaController(Context c,MediaSession.Token token){s=sessions.get(token.id);if(s==null)throw new IllegalArgumentException();}
 public MediaMetadata getMetadata(){return s.metadata;}public PlaybackState getPlaybackState(){return s.playback;}
 public int getRatingType(){return s.ratingType;}
 public void registerCallback(Callback cb,Handler h){s.callbacks.add(cb);}public void unregisterCallback(Callback cb){s.callbacks.remove(cb);}
 public TransportControls getTransportControls(){return new TransportControls();}
 public final class TransportControls {
  public void setRating(Rating rating){if(s.throwing)throw new IllegalStateException();s.ratings.add(rating);}
  public void sendCustomAction(PlaybackState.CustomAction action,Bundle b){if(s.throwing)throw new IllegalStateException();s.commands.add(action.id);}
 }
 public static class Callback {
  public void onMetadataChanged(MediaMetadata m){}public void onPlaybackStateChanged(PlaybackState p){}public void onSessionDestroyed(){}
 }
}''',
'com/boop/shieldhome/ShieldNowPlayingManager.java': '''package com.boop.shieldhome;
import android.content.*;import android.media.session.*;
public final class ShieldNowPlayingManager {
 private static final ShieldNowPlayingManager INSTANCE=new ShieldNowPlayingManager();
 private final NowPlayingState state=new NowPlayingState();
 public static ShieldNowPlayingManager get(Context c){return INSTANCE;}
 public NowPlayingState state(){return state;}
 public MediaSession.Token sessionToken(long id){return MediaController.sessions.containsKey(id)?new MediaSession.Token(id):null;}
}'''
}
PROBE = '''package com.boop.shieldhome;
import android.content.*;import android.media.*;import android.media.session.*;import android.os.*;import android.widget.Toast;
public final class FavouriteControllerProbe {
 static int assertions;
 static void check(boolean result,String why){assertions++;if(!result)throw new AssertionError(why);}
 static MediaMetadata meta(String title,String id,Boolean saved){
  MediaMetadata m=new MediaMetadata();m.data.put(MediaMetadata.METADATA_KEY_TITLE,title);
  m.data.put(MediaMetadata.METADATA_KEY_ARTIST,"Artist");m.data.put(MediaMetadata.METADATA_KEY_ALBUM,"Album");
  m.data.put(MediaMetadata.METADATA_KEY_DURATION,180000L);m.data.put(MediaMetadata.METADATA_KEY_MEDIA_ID,id);
  if(saved!=null)m.data.put(MediaMetadata.METADATA_KEY_USER_RATING,Rating.newHeartRating(saved));return m;
 }
 static NowPlayingSnapshot track(long id,String title,String pkg){
  return new NowPlayingSnapshot(id,pkg,title,"Artist",3,128,0,180000L,1,1,null,"","Album");
 }
 public static void main(String[]args){
  Context ctx=new Context();ShieldNowPlayingManager manager=ShieldNowPlayingManager.get(ctx);
  MediaController.Session player=new MediaController.Session();player.metadata=meta("Song","100",false);
  player.ratingType=Rating.RATING_HEART;player.playback.actions=128;MediaController.sessions.put(9L,player);
  NowPlayingSnapshot song=track(9,"Song","deezer.android.app");manager.state().update(song);
  DeezerFavouriteController c=DeezerFavouriteController.get(ctx);
  DeezerFavouriteController.State[] a={null},b={null};
  Runnable endA=c.subscribe(s->a[0]=s),endB=c.subscribe(s->b[0]=s);
  check(player.callbacks.size()==1,"one shared provider observer");
  check(a[0].saved==0&&a[0].canAdd&&a[0].canRemove,"advertised heart metadata");
  c.change(song,a[0],true);
  check(player.ratings.size()==1&&player.ratings.get(0).hasHeart(),"actual Android add path");
  check(a[0].pending&&a[0].saved==0,"no optimistic favourite");
  check(b[0].pending,"both screens share pending");
  c.change(song,a[0],false);check(player.ratings.size()==1,"no double sends");
  player.metadata=meta("Song","100",true);player.changed();
  check(a[0].saved==1&&!a[0].pending&&b[0].saved==1,"provider callback confirms both screens");
  c.change(song,a[0],null);
  check(player.ratings.size()==2&&!player.ratings.get(1).hasHeart(),"Home toggle removes saved track");
  player.metadata=meta("Song","100",false);player.changed();
  check(a[0].saved==0&&!a[0].pending,"provider callback confirms removal");
  c.change(song,a[0],false);check(player.ratings.size()==2,"remove already removed is idempotent");
  DeezerFavouriteController.State stale=a[0];
  player.metadata=meta("Another","101",true);manager.state().update(track(9,"Another","deezer.android.app"));
  c.change(song,stale,true);check(player.ratings.size()==2,"stale displayed track cannot send");
  player.metadata=meta("Song","100",false);manager.state().update(song);
  stale=a[0];player.metadata=meta("Song","102",false);player.changed();
  c.change(song,stale,true);check(player.ratings.size()==2,"different media ID cannot send");
  c.change(song,a[0],true);check(a[0].pending,"pending before timeout");
  Handler.advance(3000);check(!a[0].pending&&a[0].saved==0,"timeout never fabricates saved state");
  check(Toast.messages.contains("Deezer didn't confirm the favourite change."),"visible timeout result");
  player.ratingType=Rating.RATING_THUMB_UP_DOWN;player.actionsChanged();
  int sent=player.ratings.size();c.change(song,a[0],true);
  check(player.ratings.size()==sent,"never reinterpret thumbs as favourites");
  player.playback.custom.add(new PlaybackState.CustomAction("provider:add:real","Add to favourites"));player.actionsChanged();
  c.change(song,a[0],true);check(player.commands.size()==1&&player.commands.get(0).equals("provider:add:real"),"uses actual published action ID");
  player.metadata=meta("Song","102",null);player.playback.custom.clear();
  player.playback.custom.add(new PlaybackState.CustomAction("provider:remove:real","Remove from favourites"));player.actionsChanged();
  check(a[0].saved==1&&!a[0].pending,"opposite provider action confirms change");
  c.change(song,a[0],false);check(player.commands.get(1).equals("provider:remove:real"),"explicit custom remove");
  player.playback.custom.clear();player.playback.custom.add(new PlaybackState.CustomAction("dislike","Don't recommend this track"));player.actionsChanged();
  check(!a[0].canAdd&&!a[0].canRemove&&a[0].saved==-1,"dislike not used to infer favourites");
  Handler.advance(3000);
  c.change(song,a[0],null);check(player.commands.size()==2,"unknown toggle is not guessed");
  manager.state().update(track(9,"Song","com.google.android.apps.mediashell"));
  check(a[0].session==0&&!a[0].canAdd,"Cast is not native Deezer");
  manager.state().update(song);player.metadata=meta("Song","102",false);
  player.ratingType=Rating.RATING_HEART;player.playback.custom.clear();player.changed();
  player.throwing=true;c.change(song,a[0],true);check(!a[0].pending,"provider exception clears pending");player.throwing=false;
  endA.run();check(player.callbacks.size()==1,"remaining screen keeps observer");
  endB.run();check(player.callbacks.isEmpty(),"no observers after both screens close");
  endA=c.subscribe(s->a[0]=s);c.change(song,a[0],true);endA.run();
  check(player.callbacks.size()==1,"in flight receipt remains observed during screen handoff");
  Handler.advance(3000);check(player.callbacks.isEmpty(),"timeout releases idle binding");
  endA=c.subscribe(s->a[0]=s);c.change(song,a[0],true);player.destroy();
  check(!a[0].pending&&a[0].session==0,"session destruction clears stale favourite");endA.run();
  System.out.println("Favourite Android boundary: "+assertions+" assertions passed");
 }
}'''
