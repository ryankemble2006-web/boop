package com.boop.eyes;
import javax.imageio.ImageIO;import java.awt.image.BufferedImage;import java.io.File;
public final class PngIrisHarness {
 public static void main(String[] a)throws Exception{
  BufferedImage b=ImageIO.read(new File(a[0]));float lx=Float.parseFloat(a[1]),ly=Float.parseFloat(a[2]),rx=Float.parseFloat(a[3]),ry=Float.parseFloat(a[4]),radius=Float.parseFloat(a[5]),inner=Float.parseFloat(a[6]);int count=0;
  for(int y=200;y<640;y++)for(int x=200;x<1380;x++){
   int c=b.getRGB(x,y),r=(c>>>16)&255,blue=c&255;
   if(blue>r+70&&r<40){
    float dx=x-(x<768?lx:rx),dy=y-(x<768?ly:ry);
    if(Math.hypot(dx,dy)>radius*inner)throw new AssertionError("Saturated iris escapes hue coverage: "+x+","+y);
    count++;
   }
  }
  if(count<10000)throw new AssertionError("Insufficient actual blue iris samples: "+count);
  System.out.println("PASS complete hue coverage for "+count+" actual saturated iris pixels");
 }
}
