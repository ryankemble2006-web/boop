package com.boop.launcher;
/** Displacement-based navigation: slow drags and fast flings behave identically. */
public final class Swipe {
 private Swipe() {}
 public static String classify(float dx,float dy,float threshold){
  if(Math.max(Math.abs(dx),Math.abs(dy))<threshold)return "none";
  if(Math.abs(dx)>Math.abs(dy))return dx<0?"left":"right";
  return dy<0?"up":"down";
 }
}
