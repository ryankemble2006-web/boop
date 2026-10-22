package com.boop.shieldhome;
import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.media.*;
import android.media.projection.*;
import android.os.*;
import android.util.Log;
/** Consent-based live playback spectrum. PCM stays in memory and is never saved. */
public final class BassCaptureService extends Service {
 static volatile String status="Ready";
 private volatile boolean stopped;
 private volatile AudioRecord recorder;
 private Thread worker;
 private MediaProjection projection;
 private long owner; private long spectrumOwner;
 private final Handler main=new Handler(Looper.getMainLooper());

 private final MediaProjection.Callback callback=new MediaProjection.Callback(){@Override public void onStop(){stopSelf();}};
 @Override public IBinder onBind(Intent intent){return null;}
 @Override public int onStartCommand(Intent intent,int flags,int id){
  if(worker!=null)return START_NOT_STICKY;
  try{
   NotificationManager nm=getSystemService(NotificationManager.class);
   nm.createNotificationChannel(new NotificationChannel("bass-capture","Music spectrum",NotificationManager.IMPORTANCE_LOW));
   Notification note=new Notification.Builder(this,"bass-capture").setSmallIcon(android.R.drawable.ic_media_play).setContentTitle("Music spectrum").setContentText("Real playback audio; stop in spectrum settings").build();
   startForeground(17201,note,ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
   if(intent==null)throw new IllegalStateException("No consent");
   Intent consent=intent.getParcelableExtra("consent");
   if(consent==null)throw new IllegalStateException("No consent");
   projection=getSystemService(MediaProjectionManager.class).getMediaProjection(intent.getIntExtra("result",0),consent);
   if(projection==null)throw new IllegalStateException("Projection unavailable");
   projection.registerCallback(callback,main);

   AudioPlaybackCaptureConfiguration capture=new AudioPlaybackCaptureConfiguration.Builder(projection).addMatchingUsage(AudioAttributes.USAGE_MEDIA).addMatchingUsage(AudioAttributes.USAGE_GAME).addMatchingUsage(AudioAttributes.USAGE_UNKNOWN).build();
   AudioFormat format=new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(44100).setChannelMask(AudioFormat.CHANNEL_IN_STEREO).build();
   int minimum=AudioRecord.getMinBufferSize(44100,AudioFormat.CHANNEL_IN_STEREO,AudioFormat.ENCODING_PCM_16BIT);
   if(minimum<=0)throw new IllegalStateException("Unsupported capture buffer");
   AudioRecord active=new AudioRecord.Builder().setAudioFormat(format).setBufferSizeInBytes(minimum).setAudioPlaybackCaptureConfig(capture).build();
   recorder=active;
   if(active.getState()!=AudioRecord.STATE_INITIALIZED)throw new IllegalStateException("Recorder uninitialized");
   active.startRecording();
   owner=BassCaptureState.start();spectrumOwner=SpectrumState.start();status="Live PCM spectrum active";
   Log.w("BOOP-BassCapture",status+"; "+active.getSampleRate()+"Hz stereo PCM; "+PcmSpectrum.BANDS+" continuous logarithmic bands; native buffer="+active.getBufferSizeInFrames()+" frames; read=256 frames");

   worker=new Thread(()->measure(active,id,owner,spectrumOwner),"BOOP-BassCapture");worker.start();
  }catch(Exception e){status="Capture unavailable: "+e.getMessage();Log.w("BOOP-BassCapture",status);stopSelf();}
  return START_NOT_STICKY;
 }
 private void measure(AudioRecord active,int startId,long token,long spectrumToken){
  android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
  int rate=active.getSampleRate();short[] block=new short[512];PcmSpectrum spectrum=new PcmSpectrum(rate);BassEnergy bass=new BassEnergy(rate);BassOnset onset=new BassOnset();
  long hitAt=Long.MIN_VALUE,hits=0,frames=0;
  long last=SystemClock.uptimeMillis(),reads=0,dspNanos=0,maxDspNanos=0;
  AudioTimestamp timestamp=new AudioTimestamp();
  try{
   while(!stopped){
    int count=active.read(block,0,block.length,AudioRecord.READ_BLOCKING);
    if(count<=0){if(!stopped)throw new IllegalStateException("Read "+count);break;}
    long now=SystemClock.uptimeMillis(),dspStart=System.nanoTime();float energy=bass.raw(block,count);
    SpectrumState.publish(spectrumToken,spectrum.feed(block,count),now);frames+=count/2;
    if(onset.update(energy,frames*1000/rate)){hitAt=now;hits++;}
    float level=hitAt==Long.MIN_VALUE?0:Math.max(0,1-(now-hitAt)/100f);
    BassCaptureState.publish(token,level,now);reads++;
    long elapsed=System.nanoTime()-dspStart;dspNanos+=elapsed;maxDspNanos=Math.max(maxDspNanos,elapsed);
    if(now-last>=5000){boolean timed=active.getTimestamp(timestamp,AudioTimestamp.TIMEBASE_MONOTONIC)==AudioRecord.SUCCESS;Log.w("BOOP-BassCapture","Live spectrum: reads="+reads+" dspMeanMs="+(dspNanos/1e6/reads)+" dspMaxMs="+(maxDspNanos/1e6)+" timestamp="+(timed?"available":"unavailable")+" bassHits="+hits);last=now;reads=0;hits=0;dspNanos=0;maxDspNanos=0;}
   }
  }catch(Exception e){if(!stopped){status="Capture ended: "+e.getMessage();Log.w("BOOP-BassCapture",status);}}
  finally{
   BassCaptureState.stop(token);SpectrumState.stop(spectrumToken);
   try{active.release();}catch(Exception ignored){}
   main.post(()->stopSelf(startId));
  }
 }
 @Override public void onDestroy(){
  stopped=true;BassCaptureState.stop(owner);SpectrumState.stop(spectrumOwner);
  AudioRecord old=recorder;recorder=null;
  if(old!=null){try{old.stop();}catch(Exception ignored){}if(worker==null)old.release();}
  if(projection!=null){projection.unregisterCallback(callback);projection.stop();projection=null;}
  status="Spectrum capture stopped";Log.w("BOOP-BassCapture",status);
  stopForeground(true);super.onDestroy();
 }
}
