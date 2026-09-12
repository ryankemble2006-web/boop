package com.boop.shieldhome;
public final class StartupActionGateRegressionTest {
    static void check(boolean v,String why){if(!v)throw new AssertionError(why);}
    public static void main(String[] args){
        StartupActionGate gate=new StartupActionGate(); long first=gate.begin();
        check(first>=0 && gate.busy(),"operation is marked busy before scheduling");
        check(gate.begin()<0,"repeated remote presses do not queue destructive work");
        gate.cancel(); check(!gate.current(first),"Back cancels authorization and ignores late callbacks");
        long next=gate.begin(); check(next>first,"a fresh operation has a new identity");
        check(!gate.complete(first) && gate.busy(),"stale completion cannot unlock a later operation");
        check(gate.complete(next) && !gate.busy(),"only current completion unlocks");
        System.out.println("StartupActionGateRegressionTest PASS");
    }
}
