"""Nonvisual DSP/state regression tests; runs on GitHub only."""
import pathlib, subprocess, tempfile
ROOT=pathlib.Path(__file__).resolve().parents[1]
SRC=ROOT/"unified/shield-home/src/main/java/com/boop/shieldhome"
HARNESS=r'''package com.boop.shieldhome;
public final class BassHarness {
 static int checks;
 static void check(boolean yes,String message){checks++;if(!yes)throw new AssertionError(message);}
 static double tone(double hz,boolean opposite){
  BassEnergy filter=new BassEnergy(44100);short[] a=new short[882];double sum=0;
  for(int block=0;block<100;block++){
   for(int i=0;i<441;i++){short v=(short)(12000*Math.sin(2*Math.PI*hz*(block*441+i)/44100));a[i*2]=v;a[i*2+1]=(short)(opposite?-v:v);}
   float value=filter.process(a,a.length);check(Float.isFinite(value)&&value>=0&&value<=1,"bounded");
   if(block>40)sum+=value;
  }return sum/59;
 }
 public static void main(String[] args){
  double bass=tone(60,false),treble=tone(1000,false),rumble=tone(5,false);
  check(bass>treble*4,"60Hz must dominate 1kHz");
  check(bass>rumble*2,"reject subsonic drift");
  check(Math.abs(bass-tone(60,true))<0.001,"stereo anti-phase bass must not cancel");
  BassEnergy silent=new BassEnergy(44100);check(silent.process(new short[882],882)==0,"silence remains zero");
  check(silent.process(null,0)==0,"empty read safe");
  long owner=BassCaptureState.start();BassCaptureState.publish(owner,.7f,100);
  check(BassCaptureState.level(100)==.7f,"fresh sample");
  check(BassCaptureState.level(251)==0,"stale sample does not dance");
  BassCaptureState.stop(owner);check(!BassCaptureState.running&&BassCaptureState.level(100)==0,"stop clears sample");
  long next=BassCaptureState.start();BassCaptureState.publish(owner,1,100);BassCaptureState.stop(owner);
  check(BassCaptureState.running&&BassCaptureState.level(100)==0,"stale owner cannot publish or stop new session");BassCaptureState.stop(next);
  MusicBounceEnvelope old=new MusicBounceEnvelope(),fast=new MusicBounceEnvelope();
  old.update(0,0);fast.updateFast(0,0);
  check(fast.updateFast(1,16)>old.update(1,16)*1.5f,"capture attack removes old smoothing delay");
  System.out.println(checks+" bass DSP, sample lifetime and fast-attack checks passed");
 }
}'''
def main():
 with tempfile.TemporaryDirectory() as d:
  h=pathlib.Path(d)/"BassHarness.java";h.write_text(HARNESS)
  subprocess.run(["javac","-d",d,str(h),*[str(SRC/n) for n in ("BassEnergy.java","BassCaptureState.java","MusicBounceEnvelope.java")]],check=True)
  subprocess.run(["java","-cp",d,"com.boop.shieldhome.BassHarness"],check=True)
if __name__=="__main__":main()
