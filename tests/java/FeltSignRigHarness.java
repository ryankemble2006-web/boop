package com.boop.eyes;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
public final class FeltSignRigHarness {
 static int checks;
 static void check(boolean ok,String why){checks++;if(!ok)throw new AssertionError(why);}
 public static void main(String[] args)throws Exception{
  check(FeltSignRig.Digit.values().length==5,"Exactly four fingers and one thumb");
  int front=0,rear=0;
  for(FeltSignRig.Digit d:FeltSignRig.Digit.values()){
   if(FeltSignRig.behindSign(d)){rear++;check(d==FeltSignRig.Digit.THUMB,"Only thumb is occluded");}
   else front++;
  }
  check(front==4&&rear==1,"Four front fingers, one rear thumb");
  BufferedImage image=ImageIO.read(new File(args[0]));
  check(image.getWidth()==1774&&image.getHeight()==887,"Fixed photographic prop geometry");
  check(image.getColorModel().hasAlpha(),"Real transparent sprite");
  check((image.getRGB(0,0)>>>24)==0,"No baked background");
  for(boolean left:new boolean[]{true,false}){
   float[] b=FeltSignRig.thumbBounds(left);
   check(b.length==4&&b[0]<b[2]&&b[1]<b[3],"Real opposing thumb has positive area");
   for(int y=(int)b[1];y<=b[3];y++)for(int x=(int)b[0];x<=b[2];x++){
    int px=Math.round((x+400)*1774/800f),py=Math.round((y+200)*887/400f);
    check((image.getRGB(px,py)>>>24)>=245,"Thumb must remain hidden inside the felt board");
   }
  }
  for(int style=-4;style<8;style++){
   check(!FeltSignRig.name(style).isEmpty()&&!FeltSignRig.words(style).isEmpty(),"Every existing sign has copy");
  }
  System.out.println(checks+" anatomy, real-alpha occlusion and four-style checks passed");
 }
}
