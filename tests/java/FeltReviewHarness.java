package com.boop.alpha1;
import com.boop.eyes.*;
public final class FeltReviewHarness {
 private static void check(boolean ok,String detail){if(!ok)throw new AssertionError(detail);}
 private static void near(double a,double b,String detail){check(Math.abs(a-b)<.0001,detail+": "+a+" != "+b);}
 public static void main(String[] args){
  AnimationReviewTimeline t=new AnimationReviewTimeline();
  t.halfBlink();near(t.pose().left,.5,"left exactly half");near(t.pose().right,.5,"right exactly half");
  double held=t.positionMs();t.advance(500);near(t.positionMs(),held,"pause holds");
  t.setPaused(false);t.advance(10);near(t.positionMs(),held+10,"resume continues");
  t.select("sleep");t.seek(600);near(t.pose().left,EyeCatalogue.find("sleep").sample(600).left,"same real sleep");
  check(t.isPaused(),"scrub pauses");t.step(-1);near(t.positionMs(),599,"one ms back");
  t.seek(-100);near(t.positionMs(),0,"negative clamp");
  t.seek(999999);near(t.positionMs(),1200,"end clamp");t.setPaused(false);t.advance(10);check(t.isPaused(),"one shot ends paused");
  t.select("idle");t.advance(4050);near(t.positionMs(),50,"loop wraps");
  t.select("blink");t.setSlow(true);t.advance(100);near(t.positionMs(),15,"slow review");
  t.setSlow(false);t.seek(20);t.setPaused(false);t.advance(Double.NaN);near(t.positionMs(),20,"invalid delta");
  t.select("sleep");check(!t.isPaused(),"new clip plays");near(t.positionMs(),0,"new clip starts zero");
  t.halfBlink();double at=t.positionMs();t.select("blink");t.seek(at);near(t.pose().left,.5,"restorable half blink");
  for(EyeMotion.Clip c:EyeCatalogue.ALL){t.select(c.id);t.seek(c.duration*.371);EyeMotion.Pose p=c.sample(c.duration*.371);near(t.pose().left,p.left,c.id+" left");near(t.pose().right,p.right,c.id+" right");near(t.pose().x,p.x,c.id+" x");near(t.pose().y,p.y,c.id+" y");}
  System.out.println("Review timeline: held poses, resume, bounds, slow rate and all 26 catalogue clips passed");
 }
}
