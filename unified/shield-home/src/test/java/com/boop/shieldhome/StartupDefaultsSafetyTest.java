package com.boop.shieldhome;
import java.util.*;
public final class StartupDefaultsSafetyTest {
    static void check(boolean v,String why){if(!v)throw new AssertionError(why);}
    public static void main(String[] args) {
        var caps=StartupDefaultsCoordinatorTest.CAPS;
        Set<String> ready=new HashSet<>(caps.protectedPackages().keySet());
        String home=StartupRecoveryPolicy.STOCK_LAUNCHER;
        check(StartupDefaultsSafety.reason(home,true,true,"com.boop.alpha1",caps,ready,Set.of(),true).isEmpty(),"stock launcher manageable with recovery ready");
        check(!StartupDefaultsSafety.reason(home,false,true,"com.boop.alpha1",caps,ready,Set.of(),true).isEmpty(),"not generic Android preset");
        check(!StartupDefaultsSafety.reason(home,true,false,"com.boop.alpha1",caps,ready,Set.of(),true).isEmpty(),"unfinished setup protected");
        check(!StartupDefaultsSafety.reason(home,true,true,home,caps,ready,Set.of(),true).isEmpty(),"foreground must not be disabled");
        check(!StartupDefaultsSafety.reason(home,true,true,null,caps,ready,Set.of(),true).isEmpty(),"unknown foreground rejected");
        check(!StartupDefaultsSafety.reason(home,true,true,"com.boop.alpha1",caps,ready,Set.of(),false).isEmpty(),"BOOP HOME must resolve enabled");
        check(!StartupDefaultsSafety.reason("flar2.homebutton",true,true,"com.boop.alpha1",caps,ready,Set.of("flar2.homebutton"),true).isEmpty(),"active remote/accessibility path protected");
        ready.remove("com.android.bluetooth");
        check(!StartupDefaultsSafety.reason(home,true,true,"com.boop.alpha1",caps,ready,Set.of(),true).isEmpty(),"Bluetooth recovery must remain enabled");
        System.out.println("StartupDefaultsSafetyTest PASS: 8 scenarios");
    }
}
