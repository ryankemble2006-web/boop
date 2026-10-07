package com.boop.shieldhome;
public final class SpectrumCheck {
 static float[] measure(double hz,boolean anti){PcmSpectrum e=new PcmSpectrum(44100);short[] p=new short[PcmSpectrum.N*2];for(int i=0;i<p.length/2;i++){short s=(short)(16384*Math.sin(2*Math.PI*hz*i/44100));p[i*2]=s;p[i*2+1]=anti?(short)-s:s;}return e.feed(p,p.length);}
 static int strongest(float[] a){int p=0;for(int i=1;i<a.length;i++)if(a[i]>a[p])p=i;return p;}
 public static void main(String[] args){
  if(PcmSpectrum.edge(0)!=20 || Math.abs(PcmSpectrum.edge(PcmSpectrum.BANDS)-16000)>0.001)throw new AssertionError("wrong spectrum endpoints");
  for(int band=0;band<PcmSpectrum.BANDS;band++)if(Math.ceil(PcmSpectrum.edge(band+1)*PcmSpectrum.N/44100)<=Math.ceil(PcmSpectrum.edge(band)*PcmSpectrum.N/44100))throw new AssertionError("empty FFT band "+band);
  if(PcmSpectrum.BANDS!=64)throw new AssertionError("expected 64 measured bands");
  float[] silence=new PcmSpectrum(44100).feed(new short[PcmSpectrum.N*2],PcmSpectrum.N*2);for(float v:silence)if(v!=0)throw new AssertionError("silence moved");
  for(double hz:new double[]{20,25,40,80,1000,8000}){float[] a=measure(hz,false);int band=strongest(a);if(!(hz>=PcmSpectrum.edge(band)&&hz<PcmSpectrum.edge(band+1)))throw new AssertionError("wrong frequency band "+hz+" -> "+band);float[] b=measure(hz,true);if(Math.abs(a[band]-b[band])>0.001)throw new AssertionError("stereo phase cancellation");}
  for(int band=0;band<PcmSpectrum.BANDS;band++){double hz=Math.sqrt(PcmSpectrum.edge(band)*PcmSpectrum.edge(band+1));if(strongest(measure(hz,false))!=band)throw new AssertionError("centre tone resolved to wrong band: "+band+" at "+hz);}
  long first=SpectrumState.start();SpectrumState.publish(first,measure(1000,false),1000);if(SpectrumState.levels(1100)[0]<0)throw new AssertionError();for(float v:SpectrumState.levels(1400))if(v!=0)throw new AssertionError("stale audio moved");
  long second=SpectrumState.start();SpectrumState.stop(first);if(!SpectrumState.running)throw new AssertionError("old worker stopped new capture");SpectrumState.publish(first,measure(1000,false),2000);for(float v:SpectrumState.levels(2000))if(v!=0)throw new AssertionError("old worker revived audio");SpectrumState.stop(second);
  System.out.println("PASS silence, 20/25/40/80/1000/8000Hz bands, stereo phase, stale feed, capture ownership");
 }
}
