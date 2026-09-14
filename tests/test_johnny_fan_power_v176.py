from pathlib import Path
import subprocess,tempfile
src=Path(__file__).resolve().parents[1]/'unified/JohnnyStatePolicy.java'
harness='''package com.boop.alpha1;
public class JohnnyFanCheck {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 public static void main(String[] args){
 check(JohnnyStatePolicy.isFan("fan.living_room","Living Room"),"fan domain");
 check(JohnnyStatePolicy.isFan("switch.fan_power_switch","Fan Power"),"fan power switch");
 check(!JohnnyStatePolicy.isFan("switch.fan_oscillation_toggle","Fan Oscillation"),"oscillation is not fan power");
 check(!JohnnyStatePolicy.isFan("switch.swing","Fan oscillating"),"oscillating friendly name is not power");
 check(!JohnnyStatePolicy.isFan("switch.other","Other"),"unrelated switch");
 check("unknown".equals(JohnnyStatePolicy.lights(new String[]{"off","unknown"})),"unknown cannot become all-off");
 check("off".equals(JohnnyStatePolicy.lights(new String[]{"off","off"})),"all-off");
 System.out.println("Johnny fan power and conservative light policy passed");
 }
}'''
with tempfile.TemporaryDirectory() as d:
 p=Path(d)/'JohnnyFanCheck.java';p.write_text(harness)
 subprocess.run(['javac','-d',d,str(src),str(p)],check=True)
 subprocess.run(['java','-cp',d,'com.boop.alpha1.JohnnyFanCheck'],check=True)
