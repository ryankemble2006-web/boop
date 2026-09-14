package com.boop.shieldhome;
/** Stereo bass energy, approximately 35-120 Hz; never changes playback audio. */
final class BassEnergy {
 private final Filter[] high=new Filter[2],low=new Filter[2];
 BassEnergy(int rate){for(int c=0;c<2;c++){high[c]=new Filter(rate,35,true);low[c]=new Filter(rate,120,false);}}
 float process(short[] pcm,int count){
  if(pcm==null||count<2)return 0;
  int end=Math.min(count,pcm.length)&~1;double power=0;
  for(int i=0;i<end;i++){int c=i&1;double v=low[c].run(high[c].run(pcm[i]/32768.0));power+=v*v;}
  double rms=Math.sqrt(power/end);
  return rms<0.0005?0:(float)Math.min(1,Math.sqrt(rms*3));
 }
 private static final class Filter{
  final double b0,b1,b2,a1,a2;double z1,z2;
  Filter(int rate,double hz,boolean high){
   double w=2*Math.PI*hz/rate,c=Math.cos(w),alpha=Math.sin(w)/Math.sqrt(2),a0=1+alpha;
   b0=(high?(1+c):(1-c))/2/a0;b1=(high?-(1+c):(1-c))/a0;b2=b0;a1=-2*c/a0;a2=(1-alpha)/a0;
  }
  double run(double x){double y=b0*x+z1;z1=b1*x-a1*y+z2;z2=b2*x-a2*y;return y;}
 }
}
