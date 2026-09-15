package com.boop.eyes;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
public final class SeasonedFeltHarness {
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 public static void main(String[] a)throws Exception {
  BufferedImage eye=ImageIO.read(new File(a[0])),rig=ImageIO.read(new File(a[1]));
  int w=eye.getWidth(),h=eye.getHeight(),rw=rig.getWidth(),rh=rig.getHeight();
  int[] p=eye.getRGB(0,0,w,h,null,0,w),r=rig.getRGB(0,0,rw,rh,null,0,rw);
  float[] m=FeltFringeMesh.create(p,w,h,r,rw,rh);
  check(Arrays.equals(m,FeltFringeMesh.create(p,w,h,r,rw,rh)),"Wear must persist across context recreation");
  check(m.length/7<7000,"Dense v179 fringe crowds the small Shield face");
  double energy=0,max=0;int n=0;
  for(int i=0;i<m.length;i+=7){double e=m[i+4]*m[i+5];energy+=e;max=Math.max(max,e);n++;}
  check(n>500,"Retain actual loose fibres");
  check(energy/n<.06 && max<.18,"Fibre light must not wash out charcoal");
  System.out.println("PASS sparse stable mesh: "+n+" vertices, average premultiplied light "+energy/n);
 }
}
