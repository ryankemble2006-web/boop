package com.boop.eyes;
/** Fixed photographic grip: four joined fingers in front; one real opposing thumb behind.
 * This prop pose is not an articulated or validated sign-language rig. */
public final class FeltSignRig {
 public enum Digit { INDEX, MIDDLE, RING, LITTLE, THUMB }
 private static final String[] NAMES={"WHATSAPP","GMAIL","FACEBOOK","X"};
 private static final String[] WORDS={"MESSAGE!","MAIL'S HERE!","OVER HERE!","SOMETHING NEW!"};
 private FeltSignRig(){}
 public static boolean behindSign(Digit digit){return digit==Digit.THUMB;}
 public static float[] thumbBounds(boolean left){
  return left?new float[]{-240,-43,-211,43}:new float[]{211,-43,240,43};
 }
 public static String name(int style){return NAMES[Math.floorMod(style,4)];}
 public static String words(int style){return WORDS[Math.floorMod(style,4)];}
}
