package com.boop.eyes;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.Arrays;
public final class FeltFringeHarness {
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception {
  BufferedImage eye=ImageIO.read(new File(args[0])),rig=ImageIO.read(new File(args[1]));
  int w=eye.getWidth(),h=eye.getHeight(),rw=rig.getWidth(),rh=rig.getHeight();
  int[] pixels=eye.getRGB(0,0,w,h,null,0,w), guide=rig.getRGB(0,0,rw,rh,null,0,rw);
  float[] mesh=FeltFringeMesh.create(pixels,w,h,guide,rw,rh);
  check(mesh.length>1000 && mesh.length%24==0,"Actual artwork needs triangle fringe geometry");
  check(Arrays.equals(mesh,FeltFringeMesh.create(pixels,w,h,guide,rw,rh)),"Fibres must be stable across context recreation");
  int outside=0,left=0,right=0;
  for(int i=0;i<mesh.length;i+=4){
   float x=mesh[i],y=mesh[i+1],light=mesh[i+2],alpha=mesh[i+3];
   check(Float.isFinite(x)&&Float.isFinite(y)&&Math.abs(x)<=1&&Math.abs(y)<=1,"Actual fringe stays inside padded surface");
   check(light>=0&&light<=1&&alpha>=0&&alpha<=1,"Bounded colour and alpha");
   int px=Math.max(0,Math.min(w-1,Math.round((x+1)*w/2)));
   int py=Math.max(0,Math.min(h-1,Math.round((1-y)*h/2)));
   if(alpha>.05 && (pixels[py*w+px]>>>24)<32){
    outside++;if(px<w/2)left++;else right++;
    boolean near=false;
    for(int dy=-18;dy<=18&&!near;dy++)for(int dx=-18;dx<=18;dx++){
     int xx=px+dx,yy=py+dy;
     if(xx>=0&&xx<w&&yy>=0&&yy<h&&(pixels[yy*w+xx]>>>24)>=128){near=true;break;}
    }
    check(near,"No detached distant specks");
   }
   check(py<h*.72,"No hair on lower cheeks or eye whites");
  }
  check(outside>100&&left>40&&right>40,"Visible fringe must extend beyond BOTH smooth lid silhouettes");
  check(FeltFringeMesh.create(new int[w*h],w,h,guide,rw,rh).length==0,"Transparent bitmap creates no floating hair");
  System.out.println("PASS actual-master fringe: "+mesh.length/4+" vertices, "+outside+" outside-alpha vertices; deterministic, bounded, both lids");
 }
}
