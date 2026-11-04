package com.boop.shieldhome;
/** Playback-driven, deliberately non-beat-synchronized motion; no audio or Android animator dependency. */
final class PlaybackGroove {
 private long start=-1,last=-1;
 private float gain,level,sway;
 void update(boolean playing,long now){
  if(last<0||now<last||now-last>500){reset();start=now;last=now;return;}
  long dt=now-last;last=now;
  gain=Math.max(0,Math.min(1,gain+(playing?dt/700f:-dt/350f)));
  if(gain==0){level=0;sway=0;start=now;return;}
  double t=(now-start)/1000.0;
  double flourish=Math.pow(Math.max(0,Math.sin(t*.43-1.2)),12);
  level=gain*(float)(.08+.09*(1-Math.cos(t*2.3+.7*Math.sin(t*.43)))+.16*flourish);
  sway=gain*(float)(.022*Math.sin(t*1.1+.5*Math.sin(t*.31)));
 }
 float level(){return level;}
 float sway(){return sway;}
 void reset(){start=-1;last=-1;gain=level=sway=0;}
}
