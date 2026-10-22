package com.boop.shieldhome;
final class SpectrumState {
 static volatile boolean running;private static long generation;private static volatile Frame frame=new Frame(new float[PcmSpectrum.BANDS],-1);
 private static final class Frame{final float[] bands;final long time;Frame(float[] b,long t){bands=b;time=t;}}
 static synchronized long start(){generation++;frame=new Frame(new float[PcmSpectrum.BANDS],-1);running=true;return generation;}
 static synchronized void publish(long owner,float[] bands,long now){if(!running||owner!=generation)return;float[] safe=new float[PcmSpectrum.BANDS];for(int i=0;i<safe.length&&i<bands.length;i++)safe[i]=Float.isFinite(bands[i])?Math.max(0,Math.min(1,bands[i])):0;frame=new Frame(safe,now);}
 static synchronized void stop(long owner){if(owner==generation){running=false;frame=new Frame(new float[PcmSpectrum.BANDS],-1);}}
 static float[] levels(long now){float[] out=new float[PcmSpectrum.BANDS];copyLevels(now,out);return out;}
 static long copyLevels(long now,float[] out){Frame f=frame;if(running&&f.time>=0&&now>=f.time&&now-f.time<=250){System.arraycopy(f.bands,0,out,0,PcmSpectrum.BANDS);return now-f.time;}java.util.Arrays.fill(out,0);return -1;}
}
