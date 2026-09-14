import pathlib, subprocess, tempfile
root=pathlib.Path(__file__).resolve().parents[1]
harness=r'''package local.johnnycastaway.shield;
public class MusicHarness {
 static void check(boolean value,String name){if(!value)throw new AssertionError(name);}
 public static void main(String[] args){
  JohnnyMusicPolicy p=new JohnnyMusicPolicy();
  p.observe("a",3,0);check(p.poll(10000)==0,"initial playing is baseline");
  check(!p.observe(new String("a"),3,100),"same token equality and metadata");
  p.observe("a",2,1000);
  check(p.delay(1000)==900 && p.poll(1899)==0,"stable dwell required");
  check(p.poll(1900)==2 && p.poll(2100)==0,"pause once");
  p.observe("a",6,3000);check(p.poll(5000)==0,"buffering never pause");
  p.observe("a",3,4000);check(p.poll(4900)==1,"resume via buffering keeps paused baseline");
  p.observe("a",6,6000);p.observe("a",3,6100);
  check(p.poll(10000)==0,"playing buffer playing does not react");
  p.observe("b",2,12000);check(p.poll(14000)==0,"new source paused is baseline");
  p.observe(null,0,15000);p.observe("b",3,16000);
  check(p.poll(20000)==0,"session reconnect does not react");
  p.reset();p.observe("a",3,0);p.observe("a",2,100);p.observe("a",3,500);
  check(p.poll(1500)==0,"short pause flicker cancelled");
  p.observe("a",2,2000);check(p.poll(2900)==2,"real pause after flicker");
  p.observe("a",3,4000);check(p.poll(4900)==1,"opposite reaction allowed");
  p.observe("a",2,6000);check(p.poll(6900)==0,"same-kind cooldown suppresses spam");
  p.observe("a",3,13000);check(p.poll(13900)==1,"cooldown eventually permits play");
  p.observe("a",2,15000);p.observe(null,0,15500);
  check(p.poll(20000)==0,"lost session discards pending");
  p.reset();p.observe("a",6,0);p.observe("a",3,100);
  check(p.poll(2000)==0,"buffering initial connection not play event");
  p.observe("a",2,3000);p.reset();
  check(p.poll(5000)==0,"lifecycle reset removes pending");
  System.out.println("Stable music edges, buffering, source changes, jitter, cooldown and lifecycle passed");
 }
}'''
with tempfile.TemporaryDirectory() as d:
 p=pathlib.Path(d)/"MusicHarness.java";p.write_text(harness)
 subprocess.run(["javac","-d",d,str(root/"java/local/johnnycastaway/shield/JohnnyMusicPolicy.java"),str(p)],check=True)
 subprocess.run(["java","-cp",d,"local.johnnycastaway.shield.MusicHarness"],check=True)
