package com.boop.shieldhome;
/**
 * Bass attack detector. Independent implementation of adaptive onset detection:
 * compare rises with a recent energy baseline, suppress repeat hits for 200ms.
 * This is not tempo prediction or guaranteed kick/source separation.
 */
final class BassOnset {
 private double baseline, previous;
 private long lastTime=Long.MIN_VALUE, lastHit=Long.MIN_VALUE;
 boolean update(float energy,long now){
  double value=Float.isFinite(energy)?Math.max(0,energy):0;
  if(lastTime==Long.MIN_VALUE||now<lastTime||now-lastTime>250){
   baseline=value;previous=value;lastTime=now;lastHit=Long.MIN_VALUE;
   if(value>=.001){lastHit=now;return true;}return false;
  }
  long dt=now-lastTime;
  if(dt==0)return false;
  boolean ready=lastHit==Long.MIN_VALUE||now-lastHit>=200;
  boolean hit=ready&&value>=.001&&value>baseline*1.8
      &&value-previous>Math.max(.0003,baseline*.22);
  double alpha=1-Math.exp(-dt/65.0);
  baseline+=(value-baseline)*alpha;
  previous=value;lastTime=now;
  if(hit)lastHit=now;
  return hit;
 }
}
