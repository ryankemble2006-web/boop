package com.boop.alpha1;

public final class BoopFacePresentationHarness {
    private static void expect(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        BoopFacePresentationState state=new BoopFacePresentationState();
        state.request(0);
        expect(state.effective()==0,"normal face visible");
        state.occlude("voice",true);
        expect(state.effective()==8,"voice settings have no floating face");
        state.request(0);
        expect(state.effective()==8,"a delayed wake cannot resurrect a hidden face");
        state.occlude("developer",true);
        state.occlude("voice",false);
        expect(state.effective()==8,"developer menu still owns the stage");
        state.occlude("notification",true);
        state.occlude("developer",false);
        expect(state.effective()==8,"notification retains exclusive ownership");
        state.occlude("notification",false);
        expect(state.effective()==0,"main face restored after all overlays close");
        state.occlude("preview",true);
        state.request(4);
        state.occlude("preview",false);
        expect(state.effective()==4,"intentional sleeping face remains invisible");
        System.out.println("8 face ownership scenarios passed");
    }
}
