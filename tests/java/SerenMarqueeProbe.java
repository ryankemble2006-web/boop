package com.boop.shieldhome;
public final class SerenMarqueeProbe {
  static int checks;
  static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
  public static void main(String[] args){
    SerenMarqueeState s=new SerenMarqueeState();
    s.setText("A long description with its ending intact");
    s.tick(0);s.tick(1000);
    check(Math.abs(s.position(12000,400,1.6f)-50.4)<0.001,"exactly five percent faster than 30dp/s");
    float last=0;
    for(int t=1100;t<=180000;t+=100){
      s.setText("A long description with its ending intact");
      s.tick(t);float offset=s.position(12000,400,1.6f);
      check(offset>=last,"repeated refresh must not reset halfway");last=offset;
    }
    check(last>9000,"long description continues past repeated refreshes");
    float before=s.position(12000,400,1.6f);s.pause();s.tick(300000);
    check(s.position(12000,400,1.6f)==before,"visibility/focus pause preserves position");
    s.tick(301000);check(s.position(12000,400,1.6f)>before,"resume continues");
    s.setText("New episode");check(s.position(12000,400,1.6f)==0,"new episode begins at start");
    s.tick(0);float previous=0;boolean reachedEnd=false,looped=false;
    for(int t=100;t<100000;t+=100){s.tick(t);float offset=s.position(2000,400,1.6f);
      if(offset>=1600)reachedEnd=true;
      if(offset<previous){check(reachedEnd,"must show the entire tail before looping");looped=true;break;}
      previous=offset;
    }
    check(looped,"still repeats after complete text");
    check(s.position(200,400,1.6f)==0,"short labels stay still");
    System.out.println(checks+" marquee assertions passed");
  }
}
