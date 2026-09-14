package com.boop.shieldhome;
import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.media.*;
import android.media.projection.*;
import android.os.*;
import android.util.Log;
/** Explicit ten-minute Deezer bass canary. Audio is processed in memory, never saved. */
public final class BassCaptureService extends Service {
 static volatile String status="Ready";
 private volatile boolean stopped;
 private volatile AudioRecord recorder;
 private Thread worker;
 private MediaProjection projection;
 private long owner;
 private final Handler main=new Handler(Looper.getMainLooper());
 private final Runnable timeout=()->stopSelf();
 private final MediaProjection.Callback callback=new MediaProjection.Callback(){@Override public void onStop(){stopSelf();}};
 @Override public IBinder onBind(Intent intent){return null;}
 @Override public int onStartCommand(Intent intent,int flags,int id){
  if(worker!=null)return START_NOT_STICKY;
  try{
   NotificationManager nm=getSystemService(NotificationManager.class);
   nm.createNotificationChannel(new NotificationChannel("bass-capture","Bass bounce test",NotificationManager.IMPORTANCE_LOW));
   Notification note=new Notification.Builder(this,"bass-capture").setSmallIcon(android.R.drawable.ic_media_play).setContentTitle("BOOP bass bounce").setContentText("Deezer bass test: stops after ten minutes").build();
   startForeground(17201,note,ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
   if(intent==null)throw new IllegalStateException("No consent");
   Intent consent=intent.getParcelableExtra("consent");
   if(consent==null)throw new IllegalStateException("No consent");
   projection=getSystemService(MediaProjectionManager.class).getMediaProjection(intent.getIntExtra("result",0),consent);
   if(projection==null)throw new IllegalStateException("Projection unavailable");
   projection.registerCallback(callback,main);
   int uid=getPackageManager().getApplicationInfo("deezer.android.app",0).uid;
   AudioPlaybackCaptureConfiguration capture=new AudioPlaybackCaptureConfiguration.Builder(projection).addMatchingUid(uid).addMatchingUsage(AudioAttributes.USAGE_MEDIA).build();
   AudioFormat format=new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(44100).setChannelMask(AudioFormat.CHANNEL_IN_STEREO).build();
   int minimum=AudioRecord.getMinBufferSize(44100,AudioFormat.CHANNEL_IN_STEREO,AudioFormat.ENCODING_PCM_16BIT);
   if(minimum<=0)throw new IllegalStateException("Unsupported capture buffer");
   AudioRecord active=new AudioRecord.Builder().setAudioFormat(format).setBufferSizeInBytes(Math.max(minimum,7056)).setAudioPlaybackCaptureConfig(capture).build();
   recorder=active;
   if(active.getState()!=AudioRecord.STATE_INITIALIZED)throw new IllegalStateException("Recorder uninitialized");
   active.startRecording();
   owner=BassCaptureState.start();status="Deezer bass capture active";
   Log.i("BOOP-BassCapture",status+"; requested 44100 stereo, 35-120Hz");
   main.postDelayed(timeout,600000);
   worker=new Thread(()->measure(active,id,owner),"BOOP-BassCapture");worker.start();
  }catch(Exception e){status="Capture unavailable: "+e.getMessage();Log.i("BOOP-BassCapture",status);stopSelf();}
  return START_NOT_STICKY;
 }
 private void measure(AudioRecord active,int startId,long token){
  android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
  short[] block=new short[882];BassEnergy bass=new BassEnergy(44100);
  long last=SystemClock.uptimeMillis(),reads=0;float maximum=0;
  try{
   while(!stopped){
    int count=active.read(block,0,block.length,AudioRecord.READ_BLOCKING);
    if(count<=0){if(!stopped)throw new IllegalStateException("Read "+count);break;}
    long now=SystemClock.uptimeMillis();float level=bass.process(block,count);
    BassCaptureState.publish(token,level,now);reads++;maximum=Math.max(maximum,level);
    if(now-last>=5000){Log.i("BOOP-BassCapture","Bass samples: reads="+reads+" peakLevel="+maximum);last=now;reads=0;maximum=0;}
   }
  }catch(Exception e){if(!stopped){status="Capture ended: "+e.getMessage();Log.i("BOOP-BassCapture",status);}}
  finally{
   BassCaptureState.stop(token);
   try{active.release();}catch(Exception ignored){}
   main.post(()->stopSelf(startId));
  }
 }
 @Override public void onDestroy(){
  stopped=true;main.removeCallbacks(timeout);BassCaptureState.stop(owner);
  AudioRecord old=recorder;recorder=null;
  if(old!=null){try{old.stop();}catch(Exception ignored){}if(worker==null)old.release();}
  if(projection!=null){projection.unregisterCallback(callback);projection.stop();projection=null;}
  status="Bass capture stopped";Log.i("BOOP-BassCapture",status);
  stopForeground(true);super.onDestroy();
 }
}
