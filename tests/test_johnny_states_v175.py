"""State/caller policy tests run on JVM; no credentials or device control."""
from pathlib import Path
import subprocess,tempfile
ROOT=Path(__file__).resolve().parents[1]
HARNESS=r'''package com.boop.alpha1;
public class JohnnyPolicyHarness {
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 public static void main(String[] ignored){
  check(JohnnyStatePolicy.trusted("local.johnnycastaway.halab",JohnnyStatePolicy.SIGNER),"correct identity");
  check(!JohnnyStatePolicy.trusted("local.johnnycastaway.shield",JohnnyStatePolicy.SIGNER),"other variant rejected");
  check(!JohnnyStatePolicy.trusted("local.johnnycastaway.halab","wrong"),"wrong signer rejected");
  check(!JohnnyStatePolicy.trusted(null,null),"missing identity rejected");
  check("unknown".equals(JohnnyStatePolicy.lights(new String[]{})),"empty not off");
  check("unknown".equals(JohnnyStatePolicy.lights(new String[]{"off","unavailable"})),"unavailable not off");
  check("off".equals(JohnnyStatePolicy.lights(new String[]{"off","off"})),"all off");
  check("on".equals(JohnnyStatePolicy.lights(new String[]{"on","unknown"})),"any on");
  check(JohnnyStatePolicy.isFan("fan.living","Ceiling"),"fan domain");
  check(JohnnyStatePolicy.isFan("switch.living","Living room fan"),"fan switch");
  check(!JohnnyStatePolicy.isFan("switch.living","Fancy lights"),"not substring fan");
  check(!JohnnyStatePolicy.isFan("light.fan","Fan"),"not light");
  System.out.println("Johnny scoped state/caller policy passed");
 }
}'''
with tempfile.TemporaryDirectory() as d:
 p=Path(d)/"JohnnyPolicyHarness.java";p.write_text(HARNESS)
 subprocess.run(["javac","-d",d,str(ROOT/"unified/JohnnyStatePolicy.java"),str(p)],check=True)
 subprocess.run(["java","-cp",d,"com.boop.alpha1.JohnnyPolicyHarness"],check=True)
