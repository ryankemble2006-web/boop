package local.johnnycastaway.shield;
import android.content.*;
import android.media.MediaMetadata;
import android.media.session.*;
import android.os.*;
import android.util.Log;
import java.util.*;
/** Observes live Android media sessions without transport controls or audio focus. */
public final class NowPlayingObserver {
 public interface PlaybackListener {void onPlayback(Object session,int state);}
 private final PlaybackListener playback;
 private final MediaSessionManager manager;
 private final ComponentName component;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final NowPlayingView view;
 private final List<MediaController> controllers=new ArrayList<>();
 private boolean running,registered;
 private final MediaSessionManager.OnActiveSessionsChangedListener changes=this::replace;
 private final MediaController.Callback callback=new MediaController.Callback(){
  @Override public void onMetadataChanged(MediaMetadata data){publish();}
  @Override public void onPlaybackStateChanged(PlaybackState state){publish();}
  @Override public void onSessionDestroyed(){refresh();}
 };
 public NowPlayingObserver(Context context,NowPlayingView target){this(context,target,null);}
 public NowPlayingObserver(Context context,NowPlayingView target,PlaybackListener listener){
  playback=listener;
  view=target;manager=(MediaSessionManager)context.getSystemService(Context.MEDIA_SESSION_SERVICE);
  component=new ComponentName(context,JohnnyMusicListener.class);
 }
 public void start(){if(running)return;running=true;view.begin();if(manager==null)return;
  try{manager.addOnActiveSessionsChangedListener(changes,component,handler);registered=true;refresh();}
  catch(SecurityException denied){view.bind(null,null,null);if(playback!=null)playback.onPlayback(null,0);Log.i("JohnnyMedia","Music information access not enabled");}}
 private void refresh(){if(!running||manager==null)return;
  try{replace(manager.getActiveSessions(component));}catch(SecurityException revoked){replace(Collections.emptyList());}}
 private void replace(List<MediaController> next){if(!running)return;
  for(MediaController c:controllers)c.unregisterCallback(callback);controllers.clear();
  if(next!=null)for(MediaController c:next){controllers.add(c);c.registerCallback(callback,handler);}publish();}
 private void publish(){if(!running)return;MediaController selected=null;int best=0;
  for(MediaController c:controllers){PlaybackState state=c.getPlaybackState();MediaMetadata data=c.getMetadata();
   if(state==null||data==null)continue;int rank=NowPlayingPolicy.rank(state.getState());
   if(rank>best){best=rank;selected=c;}}
  view.bind(selected==null?null:selected.getMetadata(),selected==null?null:selected.getPlaybackState(),selected==null?null:JohnnyMusicListener.artworkFor(selected.getPackageName()),selected==null?"":selected.getPackageName());
  if(playback!=null) {
   PlaybackState state=selected==null?null:selected.getPlaybackState();
   playback.onPlayback(selected==null?null:selected.getSessionToken(),state==null?0:state.getState());
  }
 }
 public void stop(){running=false;
  if(registered&&manager!=null){try{manager.removeOnActiveSessionsChangedListener(changes);}catch(RuntimeException ignored){}registered=false;}
  for(MediaController c:controllers){try{c.unregisterCallback(callback);}catch(RuntimeException ignored){}}
  controllers.clear();handler.removeCallbacksAndMessages(null);view.end();}
}
