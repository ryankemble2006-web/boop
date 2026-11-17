package com.boop.shieldhome;
/** In-memory session identity prevents an old capture worker reviving stale movement. */
final class BassCaptureState {
 static volatile boolean running;
 private static long generation;
 private static volatile Sample sample=new Sample(0,-1);
 private static final class Sample{final float value;final long time;Sample(float v,long t){value=v;time=t;}}
 static synchronized long start(){generation++;sample=new Sample(0,-1);running=true;return generation;}
 static synchronized void publish(long owner,float value,long now){if(running&&generation==owner)sample=new Sample(Float.isFinite(value)?Math.max(0,Math.min(1,value)):0,now);}
 static synchronized void stop(long owner){if(generation==owner){running=false;sample=new Sample(0,-1);}}
 static float level(long now){Sample s=sample;return running&&s.time>=0&&now>=s.time&&now-s.time<=150?s.value:0;}
 private BassCaptureState(){}
}
