"""Non-visual real-code tests for Shield direct-output level parsing."""
import pathlib, subprocess, tempfile
ROOT = pathlib.Path(__file__).resolve().parents[1]
SRC = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome"
HARNESS = r'''
package com.boop.shieldhome;
import java.time.*;
public class DirectLevelsHarness {
 static int checks;
 static long now = ZonedDateTime.of(2026,9,14,16,40,6,0,ZoneId.systemDefault()).toInstant().toEpochMilli();
 static String row(String levels) { return "       09-14 16:40:05.900: "+levels+"\n"; }
 static String thread(boolean standby, String rows) {
  return "Output thread 0x1, name AudioOut_1, type 1 (DIRECT):\n  Standby: "+(standby?"yes":"no")+
   "\n  Sample rate: 44100 Hz\n      Signal power history:\n"+rows+"  1 Tracks of which 1 are active\n";
 }
 static void check(boolean value,String name) { checks++; if(!value) throw new AssertionError(name); }
 static float read(String text) { return DirectMusicLevels.parse(text,now); }
 public static void main(String[] args) {
  float quiet=read(thread(false,row("-40.0 -30.0")));
  float loud=read(thread(false,row("-40.0 -10.0")));
  check(quiet>0 && loud>quiet && loud<=1,"real last reading follows loudness");
  check(read(thread(false,row("-10.0 -100.0")))==0,"real silence returns zero");
  check(read(thread(true,row("-2.0")))<0,"standby history ignored");
  check(read(thread(false,"       09-14 16:39:50.000: -2.0\n"))<0,"stale history ignored");
  check(read(thread(false,"       09-14 16:40:09.000: -2.0\n"))<0,"future history ignored");
  check(read("Historical Thread Log\n"+thread(false,row("-2.0")))<0,"historical output ignored");
  check(read("Input thread x:\n Standby: no\n Signal power history:\n"+row("-2.0"))<0,"microphone ignored");
  check(read(thread(true,row("-2.0"))+thread(false,row("-30.0")))==quiet,"inactive output cannot mask active");
  check(read(thread(false,row("[ -10.0 -30.0 ] sum(99.9)")))==quiet,"sum is not a sample");
  check(read(thread(false,row("NaN Infinity garbage")))<0,"malformed rejected");
  check(read(thread(false,row("-130.0")))<0,"out of range rejected");
  check(read(thread(false,row("-2.0"))+"Input thread x:\n Signal power history:\n"+row("-100.0"))>0,"input cannot replace output");
  check(read(thread(false,row("-2.0"))+"Historical Thread Log\n"+thread(false,row("-100.0")))>0,"history cannot replace live");
  check(read(thread(false,row("-5.0")+row("-30.0")))==quiet,"last line selected");
  check(read(null)<0 && read("Permission Denial")<0,"unavailable rejected");
  long jan=ZonedDateTime.of(2027,1,1,0,0,0,100000000,ZoneId.systemDefault()).toInstant().toEpochMilli();
  check(DirectMusicLevels.parse(thread(false,"       12-31 23:59:59.900: -10.0\n"),jan)>0,"year boundary");
  System.out.println(checks+" actual diagnostic parser scenarios passed");
 }
}
'''
def main():
    source = SRC / "DirectMusicLevels.java"
    assert source.exists(), "Direct HDMI audio has no diagnostic level fallback parser"
    with tempfile.TemporaryDirectory() as d:
        harness=pathlib.Path(d)/"DirectLevelsHarness.java"
        harness.write_text(HARNESS)
        subprocess.run(["javac","-d",d,str(source),str(harness)],check=True)
        subprocess.run(["java","-cp",d,"com.boop.shieldhome.DirectLevelsHarness"],check=True)
    # Exact existing renderer behavior is covered by the inherited Java harness.
if __name__=="__main__": main()
