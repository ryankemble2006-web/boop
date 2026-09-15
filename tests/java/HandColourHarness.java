package com.boop.eyes;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayDeque;
import java.util.concurrent.Executor;
public final class HandColourHarness {
 static long checks;
 static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
 static final class Queue implements Executor {
  final ArrayDeque<Runnable> q=new ArrayDeque<>();
  public void execute(Runnable r){q.add(r);}
  void next(){q.remove().run();}
 }
 public static void main(String[] args)throws Exception{
  check(HandColourPixels.apply(0xffffcc00,0)==0xffffcc00,"Original yellow exact");
  check(HandColourPixels.apply(0xffffcc00,120)==0xff00ff00,"Green can be drastic");
  check(HandColourPixels.apply(0xffffcc00,240)==0xff0000ff,"Blue can be drastic");
  for(int c:new int[]{0xffffffff,0xffeae2d6,0xff117e79,0xff000000,0x00ffcc00})
   for(int h=0;h<360;h++)check(HandColourPixels.apply(c,h)==c,"Non-hand material exact");
  for(String file:args){
   BufferedImage im=ImageIO.read(new File(file)); int changedLeft=0,changedRight=0;
   for(int y=0;y<im.getHeight();y++)for(int x=0;x<im.getWidth();x++){
    int original=im.getRGB(x,y), green=HandColourPixels.apply(original,120);
    check(HandColourPixels.apply(original,0)==original,"Reset preserves every pixel");
    check((green>>>24)==(original>>>24),"Alpha and fibre silhouette exact");
    if(green!=original){if(x<im.getWidth()/2)changedLeft++;else changedRight++;}
    if(file.contains("sign-blank")&&x>=400&&x<=1350)
     check(original==green,"Central ivory board and teal border exact");
   }
   check(changedLeft>30000&&changedRight>30000,"Both approved gloves recolour");
  }
  for(int h=1;h<360;h++){
   int c=HandColourPixels.apply(0x7fffcc00,h);
   check(c>>>24==0x7f,"All hues preserve translucency");
   int max=Math.max((c>>>16)&255,Math.max((c>>>8)&255,c&255));
   int min=Math.min((c>>>16)&255,Math.min((c>>>8)&255,c&255));
   check(max==255&&min==0,"Hue preserves value and saturation");
  }
  Queue worker=new Queue(),ui=new Queue(); String[] shown={"original"};int[] calls={0};
  HandColourWork<String> work=new HandColourWork<>("original",worker,ui,
    h->{calls[0]++;return "h"+h;},v->shown[0]=v);
  work.start(120);worker.next();work.request(240);ui.next();
  check(shown[0].equals("original"),"Newer request rejects old completion");
  worker.next();ui.next();check(shown[0].equals("h240"),"Latest colour applies");
  work.request(100);worker.next();work.request(0);ui.next();
  check(shown[0].equals("original"),"Reset wins over delayed result");
  work.request(200);worker.next();work.stop();ui.next();
  check(shown[0].equals("original"),"Detached view rejects pending result");
  work.start(90);worker.next();ui.next();check(shown[0].equals("h90"),"Reattach applies saved colour");
  work.request(100);work.request(110);work.request(130);
  check(worker.q.size()==1,"Slider changes coalesce into one worker");
  worker.next();ui.next();check(shown[0].equals("h130"),"Coalescing keeps newest");
  check(calls[0]==6,"Intermediate slider values not rendered");
  work.stop();work.request(60);check(worker.q.isEmpty(),"No detached work");
  System.out.println(checks+" hand pixel and stale-work checks passed");
 }
}
