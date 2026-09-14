package local.johnnycastaway.shield;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
/** No audio capture, transport controls or focus. Reactions are lower-priority visual cameos. */
final class JohnnyMusicReactions {
 private final NativeJohnnyView player;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final JohnnyMusicPolicy policy=new JohnnyMusicPolicy();
 private boolean running;
 private final Runnable settled=this::deliver;
 private void deliver(){
  if(!running)return;
  int kind=policy.poll(SystemClock.uptimeMillis());
  if(kind!=0)Log.i("JohnnyMedia","Reaction "+kind+": "+(player.requestMusic(kind)?"accepted":"skipped"));
 }
 JohnnyMusicReactions(NativeJohnnyView player){this.player=player;}
 void start(){running=true;policy.reset();handler.removeCallbacks(settled);}
 void observe(Object session,int state){
  if(!running)return;
  long now=SystemClock.uptimeMillis();
  if(policy.observe(session,state,now))player.cancelMusic();
  handler.removeCallbacks(settled);
  long delay=policy.delay(now);
  if(delay>=0)handler.postDelayed(settled,delay);
 }
 void stop(){running=false;handler.removeCallbacks(settled);policy.reset();player.cancelMusic();}
}
