package com.boop.alpha1;
public final class VoiceCharacterTest {
    static void check(boolean ok,String m){if(!ok)throw new AssertionError(m);}
    public static void main(String[] args){
        check(BoopVoiceTuning.DEFAULT_PITCH==1f,"neutral default pitch");
        check(BoopVoiceTuning.DEFAULT_RATE==1f,"regular default pace");
        check(BoopVoiceTuning.clampPitch(.5f)==.5f,"deep character voice");
        check(BoopVoiceTuning.clampPitch(2f)==2f,"high character voice");
        check(BoopVoiceTuning.clampRate(2f)==2f,"fast puppet delivery");
        check(BoopVoiceTuning.pitchFromProgress(500)==1f,"centre means normal");
        check(BoopVoiceTuning.rateFromProgress(500)==1f,"rate centre means normal");
        check(BoopVoiceTuning.clampPitch(Float.NaN)==1f,"corrupt setting is neutral");
        for(int i=0;i<=1000;i++) {
            check(Math.abs(BoopVoiceTuning.progressFromPitch(BoopVoiceTuning.pitchFromProgress(i))-i)<=1,"pitch roundtrip");
            check(Math.abs(BoopVoiceTuning.progressFromRate(BoopVoiceTuning.rateFromProgress(i))-i)<=1,"rate roundtrip");
        }
        System.out.println("VoiceCharacterTest PASS");
    }
}
