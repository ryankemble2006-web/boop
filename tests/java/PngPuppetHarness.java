package com.boop.eyes;
import javax.imageio.ImageIO;import java.io.File;import java.awt.image.BufferedImage;
public final class PngPuppetHarness {
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 public static void main(String[] args)throws Exception{
  BufferedImage b=ImageIO.read(new File(args[0]));
  int w=b.getWidth();int[] p=b.getRGB(0,0,w,640,null,0,w);
  byte[] rig=PngPuppetRig.create(p,w,640);
  byte[] baseline=PngPuppetRigBaseline.create(p,w,640);
  for(int[] range:new int[][]{{300,690},{845,1240}})for(int x=range[0];x<=range[1];x++)for(int y=0;y<640;y++)for(int c=0;c<4;c++){
   int at=(y*w+x)*4+c;
   check(rig[at]==baseline[at],"Accepted v184 central rig must remain byte-identical: "+x+","+y+" channel"+c);
  }
  System.out.println("PASS exact v184 central rig comparison");
  byte[] accepted185=PngPuppetRigV185.create(p,w,640);
  for(int x=0;x<w;x++){
   float oldEdge=((accepted185[x*4]&255)*256+(accepted185[x*4+1]&255))/32f;
   for(int y=0;y<640;y++){
    int at=(y*w+x)*4;
    check(rig[at+3]==accepted185[at+3],"Every v185 silhouette/opacity pixel stays exact: "+x+","+y);
    if(oldEdge<640)for(int c=0;c<3;c++)
     check(rig[at+c]==accepted185[at+c],"Existing v185 body geometry stays exact: "+x+","+y);
   }
  }
  System.out.println("PASS all v185 opacity and valid body geometry preserved");
  check(java.util.Arrays.equals(rig,PngPuppetRig.create(p,w,640)),"Stable photographic silhouette");
  check((rig[(440*w+470)*4+3]&255)==255,"Black pupil is opaque inside silhouette");
  check((rig[3]&255)==0,"Black exterior is transparent");
  check((rig[(440*w+1070)*4+3]&255)==255,"Right eye stays opaque through closure");
  for(int x:new int[]{400,470,550,1000,1070,1150}){
   int i=x*4;float edge=((rig[i]&255)*256+(rig[i+1]&255))/32f;
   check((rig[i+2]&255)>0,"Original lid crown anchor required");
   check(edge>140&&edge<310,"Open lid follows actual white boundary: "+edge);
  }
  for(int x:new int[]{242,243,1280,1281,1282,1284,1286}){
   int i=x*4;float edge=((rig[i]&255)*256+(rig[i+1]&255))/32f;
   check(edge>300&&edge<420,"Bright felt fibres must not cut a scar into coloured cap: "+x+" edge "+edge);
  }
  // These samples belong to the neutral eye shadow, not to the felt cap.
  int[][] shadow={{250,340},{400,204},{503,180},{550,183},{600,201},{880,241},{940,199},{1030,180},{1140,204},{1290,350}};
  for(int[] point:shadow){
   int i=point[0]*4;float edge=((rig[i]&255)*256+(rig[i+1]&255))/32f;
   check(edge<=point[1],"Sclera shadow must stay outside fabric recolour/warp: "+point[0]+","+point[1]+" edge "+edge);
  }
  // The photographed dark underside belongs to the solid puppet, not its backdrop.
  for(int[] point:new int[][]{{503,160},{503,174},{1030,170},{400,180},{600,183}}){
   int alpha=rig[(point[1]*w+point[0])*4+3]&255;
   System.out.println("Dark felt alpha "+point[0]+","+point[1]+" = "+alpha);
   check(alpha==255,"Dark felt underside must not remain a transparent fixed seam during stretch");
  }
  for(int y=0;y<640;y++){
   int pixel=p[y*w+768],peak=Math.max((pixel>>>16)&255,Math.max((pixel>>>8)&255,pixel&255));
   check(peak<=16,"Gap fixture remains background");
   check((rig[(y*w+768)*4+3]&255)==Math.min(255,peak*255/16),"Between-eye gap retains original soft transparency");
  }
  for(int y=0;y<20;y++)for(int x=0;x<w;x++){
   int pixel=p[y*w+x],peak=Math.max((pixel>>>16)&255,Math.max((pixel>>>8)&255,pixel&255));
   check((rig[(y*w+x)*4+3]&255)==Math.min(255,peak*255/16),"Outer background and distant fibres retain soft alpha");
  }
  // Outer cap columns have no eye whites: they still need a moving lower lip.
  int[][] corners={{200,350},{210,360},{220,350},{730,275},{800,270},{1320,360},{1340,390},{1360,380}};
  for(int[] point:corners){
   int x=point[0],i=x*4;
   float edge=((rig[i]&255)*256+(rig[i+1]&255))/32f;
   System.out.println("Corner "+x+" edge "+edge+" alpha "+(rig[(point[1]*w+x)*4+3]&255));
   check(edge>250&&edge<440,"Photographed outer cap needs a real edge, not stationary 640 sentinel: "+x+" = "+edge);
   check((rig[(point[1]*w+x)*4+3]&255)==255,"Dark corner body is solid material: "+x+","+point[1]);
   float top=(rig[i+2]&255)*640f/255f;
   float sourceAtHalf=top+(edge-4-top)*(edge-top)/((edge+642)/2-top);
   check(sourceAtHalf<edge-20,"Corner material must move with closure: "+x);
  }
  for(int[] range:new int[][]{{218,232},{718,732},{808,822},{1303,1322}}){
   for(int x=range[0];x<=range[1];x++){
    int at=x*4;float e=((rig[at]&255)*256+(rig[at+1]&255))/32f,t=(rig[at+2]&255)*640f/255f;
    float half=Math.min(e-2,t+(450-t)*(e-t)/((e+642)/2-t));
    float full=Math.min(e-2,t+(450-t)*(e-t)/(642-t));
    if(x>range[0]){
     int prior=(x-1)*4;float pe=((rig[prior]&255)*256+(rig[prior+1]&255))/32f;
     check(Math.abs(e-pe)<=20,"Adjacent photographed corner boundaries must join without a crease: "+x+" "+pe+" -> "+e);
    }
    System.out.printf(java.util.Locale.ROOT,"Join x%d edge%.1f top%.1f half%.1f full%.1f alpha300=%d alpha380=%d%n",x,e,t,half,full,rig[(300*w+x)*4+3]&255,rig[(380*w+x)*4+3]&255);
   }
  }
  for(int x:new int[]{720,722,809,811,812,814}){
   float e=((rig[x*4]&255)*256+(rig[x*4+1]&255))/32f;
   check(e<325,"Detached eye reflection must not become a felt corner boundary: "+x+" "+e);
  }
  for(int[] point:new int[][]{{200,418},{220,386},{730,305},{800,297},{810,304},{1320,411},{1340,412}}){
   int x=point[0];float e=((rig[x*4]&255)*256+(rig[x*4+1]&255))/32f;
   check(e-2<=point[1],"Smoothed lip sample must remain within photographed cap: "+x+" "+e);
  }
  for(int[] point:new int[][]{{739,251},{740,267},{741,268},{742,269},{743,262},{744,262},{790,264},{791,264},{792,263},{793,263},{794,262},{795,288},{796,264}}){
   int x=point[0];float edge=((rig[x*4]&255)*256+(rig[x*4+1]&255))/32f;
   check((rig[(point[1]*w+x)*4+3]&255)>0,"Actual visible inner fringe fixture");
   System.out.println("Inner fringe "+x+","+point[1]+" edge "+edge);
   check(edge>=point[1]+2&&edge<360,"Thin inner fringe needs its neighbouring cap motion, not edge640: "+x+" "+edge);
   float top=(rig[x*4+2]&255)*640f/255f;
   for(float closure:new float[]{.5f,1f}){
    float end=edge+(642-edge)*closure;
    float sample=Math.min(edge-2,top+(point[1]-top)*(edge-top)/(end-top));
    check(Math.abs(sample-point[1])>2,"Actual bright tip must move off its own old anchor: "+x+","+point[1]+" -> "+sample);
   }
  }
  int whites=0,irises=0;
  for(int y=140;y<640;y++)for(int x=0;x<w;x++){
   int at=(y*w+x)*4,col=p[y*w+x],red=(col>>>16)&255,green=(col>>>8)&255,blue=col&255;
   float edge=((rig[at]&255)*256+(rig[at+1]&255))/32f;
   float centre=x<768?475:1070,dx=x-centre,dy=y-437;
   // Ground-truth inner iris discs are clear of the photographed open lids.
   boolean iris=dx*dx+dy*dy<150*150;
   // Conservative bright-white interior region excludes silver felt fibres.
   boolean white=Math.abs(dx)<235&&y>=235&&y<600&&Math.min(red,Math.min(green,blue))>170;
   if(iris||white){
    check(edge<640&&y>=edge+2,"Eye pixels must not be resting felt: "+x+","+y+" edge "+edge);
    check((rig[at+3]&255)==255,"Eye interior opaque for closure: "+x+","+y);
    check(y<=640&&y>=edge-4,"Full closure covers eye region");
    if(iris)irises++;if(white)whites++;
   }
  }
  check(whites>10000&&irises>100000,"Whole eye-region checks executed");
  System.out.println("PASS eye-region mask: "+whites+" whites and "+irises+" iris/pupil pixels");
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
