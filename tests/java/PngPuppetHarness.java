package com.boop.eyes;
import javax.imageio.ImageIO;import java.io.File;import java.awt.image.BufferedImage;
public final class PngPuppetHarness {
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 public static void main(String[] args)throws Exception{
  BufferedImage b=ImageIO.read(new File(args[0]));
  int w=b.getWidth();int[] p=b.getRGB(0,0,w,640,null,0,w);
  byte[] rig=PngPuppetRig.create(p,w,640);
  check(java.util.Arrays.equals(rig,PngPuppetRig.create(p,w,640)),"Stable photographic silhouette");
  check((rig[(440*w+470)*4+3]&255)==255,"Black pupil is opaque inside silhouette");
  check((rig[3]&255)==0,"Black exterior is transparent");
  check((rig[(440*w+1070)*4+3]&255)==255,"Right eye stays opaque through closure");
  for(int x:new int[]{400,470,550,1000,1070,1150}){
   int i=x*4;float edge=((rig[i]&255)*256+(rig[i+1]&255))/32f;
   check(edge>140&&edge<310,"Open lid follows actual white boundary: "+edge);
  }
  float[] neutral=FeltPalette.tint(0);
  check(neutral.length==4&&neutral[3]==0,"Original photographic charcoal bypasses recolouring");
  for(int value:new int[]{120,240}){
   float[] c=FeltPalette.tint(value);
   float hi=Math.max(c[0],Math.max(c[1],c[2])),lo=Math.min(c[0],Math.min(c[1],c[2]));
   check(hi>=.99f&&lo<.2f&&c[3]==1,"Vivid fabric, not luminance-normalized charcoal tint");
  }
  System.out.println("PASS actual PNG silhouette, pupil opacity, lid boundaries and vivid palette");
 }
}
