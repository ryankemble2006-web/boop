package com.boop.eyes;
/** Hue-only recolouring of original yellow felt; other materials and alpha stay exact. */
public final class HandColourPixels {
 private HandColourPixels(){}
 public static int applySign(int argb,int selected,int x,int width){
  // The approved 1774px photo has gloves only at the two ends. Protect the
  // complete central board, including low-alpha yellow contamination in its fringe.
  float sourceX=x*1774f/width;
  return sourceX>=390&&sourceX<=1380?argb:apply(argb,selected);
 }
 public static int apply(int argb,int selected){
  if(selected==0 || (argb>>>24)==0)return argb;
  if(selected<0||selected>359)throw new IllegalArgumentException("Hand hue");
  float r=(argb>>>16)&255,g=(argb>>>8)&255,b=argb&255;
  float max=Math.max(r,Math.max(g,b)),min=Math.min(r,Math.min(g,b)),d=max-min;
  if(max==0 || d/max<.2f)return argb;
  float hue=max==r?60*((g-b)/d):max==g?60*(2+(b-r)/d):60*(4+(r-g)/d);
  if(hue<0)hue+=360;
  // Teal trim, ivory cloth, lettering and black remain outside the yellow mask.
  if(hue<15||hue>85)return argb;
  float h=(hue+selected-48+360)%360/60;
  float x=d*(1-Math.abs(h%2-1));
  float nr,ng,nb;
  if(h<1){nr=d;ng=x;nb=0;}else if(h<2){nr=x;ng=d;nb=0;}
  else if(h<3){nr=0;ng=d;nb=x;}else if(h<4){nr=0;ng=x;nb=d;}
  else if(h<5){nr=x;ng=0;nb=d;}else{nr=d;ng=0;nb=x;}
  return (argb&0xff000000)|(Math.round(nr+min)<<16)|(Math.round(ng+min)<<8)|Math.round(nb+min);
 }
}
