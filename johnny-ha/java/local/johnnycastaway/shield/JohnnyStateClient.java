package local.johnnycastaway.shield;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import org.json.JSONObject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
/** Polls only BOOP's fixed snapshot while this player is visible. Never controls HA. */
final class JohnnyStateClient {
 private final Context context;
 private final NativeJohnnyView player;
 private final Handler main=new Handler(Looper.getMainLooper());
 private final JohnnyStateEdges edges=new JohnnyStateEdges();
 private final JohnnyFanState fanState=new JohnnyFanState();
 private ScheduledExecutorService worker;
 private long generation;
 private String previous="";
 private int observations;
 JohnnyStateClient(Context c,NativeJohnnyView p){context=c.getApplicationContext();player=p;}
 void start(){
  if(worker!=null)return;
  final long session=++generation;edges.reset();fanState.reset();observations=0;
  worker=Executors.newSingleThreadScheduledExecutor(r->{Thread t=new Thread(r,"JohnnyHaState");t.setDaemon(true);return t;});
  final int[] skip={0};
  worker.scheduleWithFixedDelay(()->{
   if(skip[0]>0){skip[0]--;return;}
   String json;
   try{
    Bundle b=context.getContentResolver().call(Uri.parse("content://com.boop.alpha1.johnny_states"),"snapshot",null,null);
    json=b==null?null:b.getString("json");
   }catch(RuntimeException ignored){json=null;}
   try{if(json==null||!"ok".equals(new JSONObject(json).optString("status")))skip[0]=JohnnyPollPolicy.unavailableSkipPolls();}
   catch(Exception ignored){skip[0]=JohnnyPollPolicy.unavailableSkipPolls();}
   final String result=json;
   main.post(()->{
    if(worker==null||generation!=session)return;
    if(++observations%5==1)Log.i("JohnnyNative",player.getStatus());
    try{
     JSONObject snapshot=new JSONObject(result==null?"{}":result);
     if(!"ok".equals(snapshot.optString("status")))throw new IllegalStateException();
     int night=JohnnyStateEdges.night(snapshot.optString("lights"));
     if(night>=0)player.setNight(night==1);
     else player.cancelOi();
     if(edges.lightsOff(snapshot.optString("lights"))){
      boolean accepted=player.requestOi();
      Log.i("JohnnyHA","Lights-off edge: "+(accepted?"OI queued":"OI unavailable"));
     }
     if(fanState.update(snapshot.optString("fan"))){
      player.setFanState(fanState.on());
      Log.i("JohnnyHA","Fan level: "+(fanState.on()?"on":"off"));
     }
     String summary="lights="+snapshot.optString("lights")+" fan="+snapshot.optString("fan")
       +" lightEntities="+snapshot.optJSONArray("lightEntities")+" fanEntities="+snapshot.optJSONArray("fanEntities");
     if(!previous.equals(summary)){previous=summary;Log.i("JohnnyHA",summary);}
    }catch(Exception ignored){
     edges.reset();player.cancelOi();
     if(!"unavailable".equals(previous)){previous="unavailable";Log.i("JohnnyHA","State connection unavailable");}
    }
   });
  },0,JohnnyPollPolicy.healthyPollMs(),TimeUnit.MILLISECONDS);
 }
 void stop(){generation++;edges.reset();fanState.reset();player.cancelOi();if(worker!=null){worker.shutdownNow();worker=null;}main.removeCallbacksAndMessages(null);}
}
