package com.boop.eyes;

/** Finite local performance; no media access or track detection. */
public final class FreddieMotion {
    public static SignMotion.Pose sample(double elapsed){
        double t=Double.isFinite(elapsed)?Math.max(0,elapsed):0;
        float lift=EyeMotion.smooth((float)(t/1100));
        float energy=EyeMotion.smooth((float)((t-1200)/500))*(1-EyeMotion.smooth((float)((t-7200)/1800)));
        double beat=(t-1200)/1000;
        float angle=energy*(9*(float)Math.sin(beat*3.4)+5*(float)Math.sin(beat*1.7));
        float sway=energy*12*(float)Math.sin(beat*1.7);
        float bob=energy*8*(float)Math.sin(beat*6.8);
        EyeMotion.Pose eyes=new EyeMotion.Pose(.05f*energy,.13f*energy,angle/38,.12f*energy);
        eyes=eyes.blink(Math.max(EyeMotion.blink(t-1500),EyeMotion.blink(t-6500)));
        return new SignMotion.Pose(lift,angle,sway,bob,0,eyes);
    }
}
