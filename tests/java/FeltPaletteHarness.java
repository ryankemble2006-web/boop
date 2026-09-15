package com.boop.eyes;
public final class FeltPaletteHarness {
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 public static void main(String[] args){
  float[] c=FeltPalette.tint(0);
  check(c[0]==1f&&c[1]==1f&&c[2]==1f,"Default must exactly preserve charcoal");
  for(int i=1;i<=359;i++){
   float[] t=FeltPalette.tint(i);
   float y=.213f*t[0]+.715f*t[1]+.072f*t[2];
   check(Math.abs(y-1f)<.0001f,"Hue must not raise material luminance");
   for(float v:t)check(Float.isFinite(v)&&v>0f&&v<2.5f,"Bounded stable colour");
  }
  check(java.util.Arrays.equals(FeltPalette.tint(-99),c),"Clamp malformed low value");
  check(java.util.Arrays.equals(FeltPalette.tint(900),FeltPalette.tint(359)),"Clamp high value");
  check(!java.util.Arrays.equals(FeltPalette.tint(1),FeltPalette.tint(180)),"Distinct colours");
  System.out.println("PASS independent felt palette with luminance preservation");
 }
}
