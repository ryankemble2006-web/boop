package com.boop.eyes;
/** Zero is original charcoal. 1..359 sweep coloured felt at constant luminance. */
public final class FeltPalette {
 private FeltPalette(){}
 public static int clamp(int value){return Math.max(0,Math.min(359,value));}
 public static float[] tint(int value){
  value=clamp(value);
  if(value==0)return new float[]{1f,1f,1f};
  float h=(value-1)*6f/359f;
  int sector=(int)h;float f=h-sector;
  float low=.52f,fall=1f-.48f*f,rise=.52f+.48f*f;
  float r,g,b;
  switch(sector){
   case 0:r=1;g=rise;b=low;break;
   case 1:r=fall;g=1;b=low;break;
   case 2:r=low;g=1;b=rise;break;
   case 3:r=low;g=fall;b=1;break;
   case 4:r=rise;g=low;b=1;break;
   default:r=1;g=low;b=fall;
  }
  float y=.213f*r+.715f*g+.072f*b;
  return new float[]{r/y,g/y,b/y};
 }
}
