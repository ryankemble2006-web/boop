package com.boop.eyes;

/** Pure sign performance state; host owns time, drawing and lifecycle. */
public final class SignMotion {
    public static final class Pose {
        public final float lift,angle,sway,bob,wrist;
        public final EyeMotion.Pose eyes;
        Pose(float lift,float angle,float sway,float bob,float wrist,EyeMotion.Pose eyes){
            this.lift=lift;this.angle=angle;this.sway=sway;this.bob=bob;this.wrist=wrist;this.eyes=eyes;
        }
    }
    public static Pose sample(double elapsed,int style){
        double t=Double.isFinite(elapsed)?Math.max(0,elapsed):0;
        float lift=EyeMotion.smooth((float)((t-220)/900));
        float envelope=EyeMotion.smooth((float)((t-1400)/350))*(1-EyeMotion.smooth((float)((t-5500)/1800)));
        double beat=(t-1400)/1000;
        int variant=Math.floorMod(style,4);
        float wave=(float)Math.sin(beat*(variant==1?7.2:5.4));
        float flourish=(float)Math.sin(Math.PI*EyeMotion.smooth((float)((t-4800)/1250)));
        float angle=envelope*((variant==2?13:10)*wave+4*flourish);
        float sway=envelope*16*(float)Math.sin(beat*2.7);
        float bob=envelope*(7*(float)Math.sin(beat*10.8)+9*flourish);
        float wrist=envelope*3*(float)Math.sin(beat*5.4-0.5);
        float gaze=0.28f*envelope;
        EyeMotion.Pose eyes=new EyeMotion.Pose(0.03f*envelope,0.06f*envelope,angle/35,gaze+0.28f*(1-lift));
        eyes=eyes.blink(Math.max(EyeMotion.blink(t-1200),EyeMotion.blink(t-6100)));
        return new Pose(lift,angle,sway,bob,wrist,eyes);
    }
}
