from pathlib import Path
import subprocess
SRC=Path(__file__).resolve().parents[1]/"unified/shield-home/src/main/java/com/boop/shieldhome/SerenTraktSync.java"
def test_waits_for_startup_without_second_sync(tmp_path):
    probe=tmp_path/"StartupProbe.java"
    probe.write_text(r'''package com.boop.shieldhome;
public class StartupProbe {
 static class Port implements SerenTraktSync.Port {
  long time; boolean ready, cancelled; int launches, mutations; String terminal="done";
  public boolean ready(){return ready;}
  public boolean launchKodi(){launches++;ready=true;return true;}
  public void returnToLauncher(){}
  public void execute(String id){mutations++;}
  public String status(String id){return time>=12000?terminal:"running";}
  public long nowMs(){return time;}
  public void sleep(long ms){time+=ms;}
  public boolean cancelled(){return cancelled;}
 }
 public static void main(String[] a)throws Exception {
  Port cold=new Port(); String status=SerenTraktSync.run(cold,"x");
  if(!status.equals("done")||cold.launches!=1||cold.mutations!=0||cold.time<12000)throw new AssertionError("cold startup duplicates sync or exits early");
  Port warm=new Port();warm.ready=true;warm.time=20000;SerenTraktSync.run(warm,"x");
  if(warm.launches!=0||warm.mutations!=0||warm.time<25000)throw new AssertionError("warm reuse/minimum dwell");
  Port failure=new Port();failure.terminal="failed";
  if(!SerenTraktSync.run(failure,"x").equals("failed"))throw new AssertionError("failure lost");
  Port unchanged=new Port();unchanged.terminal="unchanged";
  if(!SerenTraktSync.run(unchanged,"x").equals("unchanged"))throw new AssertionError("unchanged lost");
  Port timeout=new Port();timeout.terminal="running";
  try{SerenTraktSync.run(timeout,"x");throw new AssertionError("timeout ignored");}catch(java.io.IOException expected){}
  Port cancel=new Port();cancel.cancelled=true;
  try{SerenTraktSync.run(cancel,"x");throw new AssertionError("cancel ignored");}catch(InterruptedException expected){}
 }
}''',encoding="utf-8")
    subprocess.run(["javac","-d",str(tmp_path),str(SRC),str(probe)],check=True)
    subprocess.run(["java","-cp",str(tmp_path),"com.boop.shieldhome.StartupProbe"],check=True)
