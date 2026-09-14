import pathlib,subprocess,tempfile
ROOT=pathlib.Path(__file__).resolve().parents[1]
SRC=ROOT/"unified/shield-home/src/main/java/com/boop/shieldhome"
def main():
 assert (SRC/"PlaybackGroove.java").exists(),"Missing relaxed playback groove"
 harness=r'''package com.boop.shieldhome;
public class GrooveHarness {
 static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 public static void main(String[] args){
  PlaybackGroove g=new PlaybackGroove();g.update(false,0);
  check(g.level()==0&&g.sway()==0,"inactive rests");
  float oldLevel=0,oldSway=0;int directionChanges=0;float previousDelta=0;
  for(long t=16;t<180000;t+=16){
   g.update(true,t);
   check(Float.isFinite(g.level())&&g.level()>=0&&g.level()<=.5,"gentle bounded lift");
   check(Float.isFinite(g.sway())&&Math.abs(g.sway())<=.03,"bounded sway");
   check(Math.abs(g.level()-oldLevel)<.025&&Math.abs(g.sway()-oldSway)<.003,"no abrupt motion jumps");
   float delta=g.sway()-oldSway;if(delta*previousDelta<0)directionChanges++;
   previousDelta=delta;oldLevel=g.level();oldSway=g.sway();
  }
  check(directionChanges>20,"groove varies direction");
  for(long t=180000;t<182000;t+=16)g.update(false,t);
  check(g.level()==0&&g.sway()==0,"pause settles fully");
  g.reset();check(g.level()==0&&g.sway()==0,"hidden resets");
  g.update(true,500000);check(g.level()==0&&g.sway()==0,"resume starts gently");
  g.update(true,499000);check(g.level()==0&&g.sway()==0,"backwards time resets safely");
  System.out.println("Relaxed groove bounds, continuity, pause, visibility and restart checks passed");
 }
}''';
 with tempfile.TemporaryDirectory() as d:
  p=pathlib.Path(d)/"GrooveHarness.java";p.write_text(harness)
  subprocess.run(["javac","-d",d,str(SRC/"PlaybackGroove.java"),str(p)],check=True)
  subprocess.run(["java","-cp",d,"com.boop.shieldhome.GrooveHarness"],check=True)
if __name__=="__main__":main()
