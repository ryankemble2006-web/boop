package com.boop.rally;
import java.util.*;
public final class ControlsHarness {
    static int checks;
    static void check(boolean value, String why) { checks++; if (!value) throw new AssertionError(why); }
    public static void main(String[] args) {
        check(Controls.mapKey(19)==273, "D-pad up is DOS up");
        check(Controls.mapKey(20)==274, "D-pad down is DOS down");
        check(Controls.mapKey(21)==276, "D-pad left is DOS left");
        check(Controls.mapKey(22)==275, "D-pad right is DOS right");
        check(Controls.mapKey(23)==13, "Remote OK selects");
        check(Controls.mapKey(96)==13, "Controller A selects");
        check(Controls.mapKey(97)==27, "Controller B is DOS Escape");
        check(Controls.mapKey(99)==32, "Controller X is space");
        check(Controls.mapKey(105)==273, "R2 accelerates");
        check(Controls.mapKey(104)==274, "L2 brakes");
        check(Controls.mapKey(4)==0, "Android Back is reserved for menu");
        check(Controls.mapKey(3)==0, "Android Home is never mapped");
        check(Controls.mapKey(108)==0, "Controller Start is reserved for menu");
        List<String> events=new ArrayList<>();
        Controls c=new Controls((k,d)->events.add(k+":"+d));
        c.key(19,true); c.axes(0,-1,0,0);
        check(events.equals(Arrays.asList("273:true")), "Two sources press key only once");
        c.key(19,false);
        check(events.size()==1, "Releasing one source does not release held analog");
        c.axes(0,0,0,0);
        check(events.get(1).equals("273:false"), "Final source releases");
        events.clear(); c.axes(.24f,-.24f,.1f,.1f);
        check(events.isEmpty(), "Deadzone prevents drift");
        c.axes(.8f,0,0,.8f);
        check(events.contains("275:true") && events.contains("273:true"), "Steering and throttle can coexist");
        c.clear();
        check(events.contains("275:false") && events.contains("273:false"), "Pause clears every held input");
        int size=events.size(); c.clear();
        check(events.size()==size, "Repeated clear is safe");
        check(Controls.mapKey(29)==97 && Controls.mapKey(54)==122,"Keyboard letters");
        check(Controls.mapKey(7)==48 && Controls.mapKey(16)==57,"Keyboard digits");
        System.out.println("Controls: "+checks+" behavioral checks passed");
    }
}
