package com.boop.shieldhome;
public final class SpectrumCheck {
 static float[] measure(double hz,boolean anti){return measure(hz,anti,44100);}
 static float[] measure(double hz,boolean anti,int rate){PcmSpectrum e=new PcmSpectrum(rate);short[] p=new short[rate*4];for(int i=0;i<p.length/2;i++){short s=(short)(16384*Math.sin(2*Math.PI*hz*i/rate));p[i*2]=s;p[i*2+1]=anti?(short)-s:s;}return e.feed(p,p.length);}
 static int strongest(float[] a){int p=0;for(int i=1;i<a.length;i++)if(a[i]>a[p])p=i;return p;}
 public static void main(String[] args){
  if(PcmSpectrum.edge(0)!=20 || Math.abs(PcmSpectrum.edge(PcmSpectrum.BANDS)-16000)>0.001)throw new AssertionError("wrong spectrum endpoints");
  if(PcmSpectrum.BANDS!=64)throw new AssertionError("expected 64 measured bands");
  float[] silence=new PcmSpectrum(44100).feed(new short[88200],88200);for(float v:silence)if(v!=0)throw new AssertionError("silence moved");
  for(double hz:new double[]{20,25,40,80,1000,8000}){float[] a=measure(hz,false);int band=strongest(a);if(!(hz>=PcmSpectrum.edge(band)&&hz<PcmSpectrum.edge(band+1)))throw new AssertionError("wrong frequency band "+hz+" -> "+band);float[] b=measure(hz,true);if(Math.abs(a[band]-b[band])>0.001)throw new AssertionError("stereo phase cancellation");}
  for(int rate:new int[]{44100,48000})for(int band=0;band<PcmSpectrum.BANDS;band++){double hz=Math.sqrt(PcmSpectrum.edge(band)*PcmSpectrum.edge(band+1));float[] measured=measure(hz,false,rate);if(strongest(measured)!=band)throw new AssertionError("centre tone resolved to wrong band: "+band+" at "+hz);double db=measured[band]*72-72;if(Math.abs(db+9.0309)>1)throw new AssertionError("incorrect calibrated RMS: "+db);}
  short[] mixed=new short[44100*2];for(int i=0;i<mixed.length/2;i++){mixed[i*2]=(short)(10000*Math.sin(2*Math.PI*80*i/44100));mixed[i*2+1]=(short)(10000*Math.sin(2*Math.PI*8000*i/44100));}
  float[] together=new PcmSpectrum(44100).feed(mixed,mixed.length);PcmSpectrum streamed=new PcmSpectrum(44100);float[] chunks=null;for(int i=0;i<mixed.length;i+=512){short[] block=java.util.Arrays.copyOfRange(mixed,i,Math.min(mixed.length,i+512));chunks=streamed.feed(block,block.length);}for(int i=0;i<together.length;i++)if(Math.abs(together[i]-chunks[i])>0.00001)throw new AssertionError("Read boundaries changed band "+i);
  int bass=strongest(measure(80,false)),treble=strongest(measure(8000,false));if(together[bass]<.7||together[treble]<.7)throw new AssertionError("Independent simultaneous stereo tones lost");
  java.util.Random noise=new java.util.Random(17);for(int i=0;i<mixed.length;i++)mixed[i]=(short)noise.nextInt();for(float v:streamed.feed(mixed,mixed.length))if(!Float.isFinite(v)||v<0||v>1)throw new AssertionError("Noise destabilized filters");
  long first=SpectrumState.start();SpectrumState.publish(first,measure(1000,false),1000);if(SpectrumState.levels(1100)[0]<0)throw new AssertionError();for(float v:SpectrumState.levels(1400))if(v!=0)throw new AssertionError("stale audio moved");
  long second=SpectrumState.start();SpectrumState.stop(first);if(!SpectrumState.running)throw new AssertionError("old worker stopped new capture");SpectrumState.publish(first,measure(1000,false),2000);for(float v:SpectrumState.levels(2000))if(v!=0)throw new AssertionError("old worker revived audio");SpectrumState.stop(second);
  System.out.println("PASS 64 tone centres at 44.1/48kHz, calibrated RMS, 20Hz endpoint, stereo phase, simultaneous tones, streaming continuity, noise stability, stale feed, capture ownership");
 }
}
