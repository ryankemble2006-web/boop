package local.boop.captureprobe;
import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.media.*;
import android.media.projection.*;
import android.os.*;
import android.util.Log;
import java.util.Locale;
/** Temporary feasibility probe: no saved samples, networking, microphone source or playback. */
public final class ProbeService extends Service {
 static volatile String status="Ready";
 static volatile boolean running;
 private volatile boolean stopped;
 private volatile AudioRecord recorder;
 private MediaProjection projection;
 private final Handler main=new Handler(Looper.getMainLooper());
 private final Runnable timeout=()->stopSelf();
 private final MediaProjection.Callback callback=new MediaProjection.Callback(){@Override public void onStop(){stopSelf();}};
 @Override public IBinder onBind(Intent intent){return null;}
 @Override public int onStartCommand(Intent intent,int flags,int id){
  if(running)return START_NOT_STICKY;
  try{
   NotificationManager nm=getSystemService(NotificationManager.class);
   nm.createNotificationChannel(new NotificationChannel("probe","Playback audio test",NotificationManager.IMPORTANCE_LOW));
   Notification note=new Notification.Builder(this,"probe").setSmallIcon(android.R.drawable.ic_media_play).setContentTitle("BOOP audio test").setContentText("Measuring Deezer for one minute").build();
   startForeground(17101,note,ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
   if(intent==null)throw new IllegalStateException("No consent");
   Intent consent=intent.getParcelableExtra("consent");
   if(consent==null)throw new IllegalStateException("No consent");
   projection=getSystemService(MediaProjectionManager.class).getMediaProjection(intent.getIntExtra("result",0),consent);
   if(projection==null)throw new IllegalStateException("Projection unavailable");
   projection.registerCallback(callback,main);
   int uid=getPackageManager().getApplicationInfo("deezer.android.app",0).uid;
   AudioPlaybackCaptureConfiguration capture=new AudioPlaybackCaptureConfiguration.Builder(projection).addMatchingUid(uid).addMatchingUsage(AudioAttributes.USAGE_MEDIA).build();
   int rate=44100;
   AudioFormat format=new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(rate).setChannelMask(AudioFormat.CHANNEL_IN_STEREO).build();
   int minimum=AudioRecord.getMinBufferSize(rate,AudioFormat.CHANNEL_IN_STEREO,AudioFormat.ENCODING_PCM_16BIT);
   if(minimum<=0)throw new IllegalStateException("Unsupported input buffer "+minimum);
   recorder=new AudioRecord.Builder().setAudioFormat(format).setBufferSizeInBytes(Math.max(minimum,17640)).setAudioPlaybackCaptureConfig(capture).build();
   if(recorder.getState()!=AudioRecord.STATE_INITIALIZED)throw new IllegalStateException("Recorder uninitialized");
   running=true;status="Starting capture";recorder.startRecording();
   main.postDelayed(timeout,60000);
   new Thread(this::measure,"BOOP-CaptureProbe").start();
  }catch(Exception e){status="Capture unavailable: "+e.getClass().getSimpleName()+": "+e.getMessage();Log.i("BOOP-CaptureProbe",status);stopSelf();}
  return START_NOT_STICKY;
 }
 private void measure(){
  android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
  short[] block=new short[882];long reads=0,nonzero=0,total=0,last=SystemClock.elapsedRealtime(),maxRead=0;
  double squares=0;int peak=0;
  try{
   while(!stopped){
    AudioRecord active=recorder;if(active==null)break;
    long start=SystemClock.elapsedRealtimeNanos();
    int count=active.read(block,0,block.length,AudioRecord.READ_BLOCKING);
    long elapsed=SystemClock.elapsedRealtimeNanos()-start;
    if(count<=0){if(!stopped)throw new IllegalStateException("Read "+count);break;}
    reads++;total+=count;maxRead=Math.max(maxRead,elapsed);
    for(int i=0;i<count;i++){int value=block[i];if(value!=0)nonzero++;peak=Math.max(peak,Math.abs(value));squares+=(double)value*value;}
    long now=SystemClock.elapsedRealtime();
    if(now-last>=1000){
     String report=String.format(Locale.US,"44100 Hz requested; %d reads; nonzero=%d/%d; RMS=%.5f; peak=%d; max read=%.1f ms",reads,nonzero,total,Math.sqrt(squares/Math.max(1,total))/32768.0,peak,maxRead/1000000.0);
     status=report;Log.i("BOOP-CaptureProbe",report);
     reads=nonzero=total=maxRead=0;squares=0;peak=0;last=now;
    }
   }
  }catch(Exception e){if(!stopped){status="Capture ended: "+e.getMessage();Log.i("BOOP-CaptureProbe",status);}}
  finally{main.post(()->stopSelf());}
 }
 @Override public void onDestroy(){
  stopped=true;running=false;main.removeCallbacks(timeout);
  AudioRecord old=recorder;recorder=null;
  if(old!=null){try{old.stop();}catch(Exception ignored){}old.release();}
  if(projection!=null){projection.unregisterCallback(callback);projection.stop();projection=null;}
  Log.i("BOOP-CaptureProbe","Probe stopped; resources released");
  stopForeground(true);super.onDestroy();
 }
}
