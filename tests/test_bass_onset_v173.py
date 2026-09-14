#!/usr/bin/env python3
"""Functional onset fixtures; synthetic audio is not physical rhythm acceptance."""
import pathlib, subprocess, tempfile
ROOT=pathlib.Path(__file__).resolve().parents[1]
SRC=ROOT/"unified/shield-home/src/main/java/com/boop/shieldhome"
def main():
    assert (SRC/"BassOnset.java").exists(), "Missing bass attack detector"
    harness=r'''package com.boop.shieldhome;
public class OnsetHarness {
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 public static void main(String[] args){
  for(float gain:new float[]{.02f,.1f,.5f}){
   BassOnset d=new BassOnset();int hits=0;
   for(int t=0;t<5000;t+=6)if(d.update(gain,t))hits++;
   check(hits<=1,"steady bass must not retrigger: "+hits);
   d=new BassOnset();hits=0;
   for(int t=0;t<6000;t+=6){
    int phase=t%432;
    float energy=gain*(.12f+(float)Math.exp(-phase/45.0));
    if(d.update(energy,t)){hits++;check(phase<42,"late onset at "+phase);}
   }
   check(hits>=13&&hits<=14,"one attack per 139 bpm kick: "+hits);
   for(int t=6000;t<7000;t+=6)check(!d.update(0,t),"silence ghost");
  }
  BassOnset d=new BassOnset();
  for(int t=0;t<3000;t+=6)check(!d.update(.00001f,t),"noise floor");
  check(d.update(.2f,4000),"fresh attack after gap");
  check(!d.update(.3f,4006),"no double trigger");
  for(double hz:new double[]{40,60,100}){
   BassEnergy filter=new BassEnergy(44100);BassOnset detector=new BassOnset();int count=0;
   short[] pcm=new short[512];
   for(int b=0;b<860;b++){
    for(int i=0;i<256;i++){
     short v=(short)(10000*Math.sin(2*Math.PI*hz*(b*256+i)/44100));
     pcm[2*i]=v;pcm[2*i+1]=(short)-v;
    }
    if(detector.update(filter.raw(pcm,512),(long)(b*256000.0/44100))&&b>100)count++;
   }
   check(count==0,"steady PCM "+hz+"Hz falsely hits "+count);
  }
  System.out.println("Onset fixtures passed: steady bass, gain range, 139 bpm, silence, gap, double trigger");
 }
}''';
    with tempfile.TemporaryDirectory() as d:
        p=pathlib.Path(d)/"OnsetHarness.java";p.write_text(harness)
        subprocess.run(["javac","-d",d,str(SRC/"BassOnset.java"),str(SRC/"BassEnergy.java"),str(p)],check=True)
        subprocess.run(["java","-cp",d,"com.boop.shieldhome.OnsetHarness"],check=True)
if __name__=="__main__":main()
