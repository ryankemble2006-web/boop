package com.boop.eyes;

import java.util.Random;

/** Pure animation state. No Android, bitmap, microphone or application ownership. */
public final class EyeMotion {
    public static final long BLINK_MS = 183, DOUBLE_GAP_MS = 110;
    public static final Pose OPEN = new Pose(0, 0, 0, 0);
    private EyeMotion() { }

    public static final class Pose {
        public final float left, right, x, y;
        public Pose(float left, float right, float x, float y) {
            this.left = clamp(left, 0, 1); this.right = clamp(right, 0, 1);
            this.x = clamp(x, -1, 1); this.y = clamp(y, -1, 1);
        }
        public Pose blink(float closure) {
            return new Pose(left + (1-left)*closure, right + (1-right)*closure, x, y);
        }
    }

    public static final class Clip {
        public final String id, label, family;
        public final boolean loop, ambientBlink;
        private final float[][] keys;
        public final float duration;
        public Clip(String id, String label, String family, boolean loop,
                    boolean ambientBlink, float[][] input) {
            if (input.length < 2) throw new IllegalArgumentException("Two keys required");
            this.id=id; this.label=label; this.family=family;
            this.loop=loop; this.ambientBlink=ambientBlink;
            keys=new float[input.length][];
            for (int i=0;i<input.length;i++) {
                if(input[i].length!=5) throw new IllegalArgumentException("Five channels required");
                keys[i]=input[i].clone();
                for(float value:keys[i]) if(!Float.isFinite(value)) throw new IllegalArgumentException("Finite keys required");
                if((i==0 && keys[i][0]!=0) || (i>0 && keys[i][0]<=keys[i-1][0])) throw new IllegalArgumentException("Ordered times required");
            }
            duration=keys[keys.length-1][0];
        }
        public Pose sample(double elapsed) {
            double t=Double.isFinite(elapsed)?Math.max(0,elapsed):0;
            if(loop) t=t%duration;
            if(t>=duration)return pose(keys[keys.length-1]);
            for(int i=1;i<keys.length;i++)if(t<=keys[i][0]) {
                float amount=(float)((t-keys[i-1][0])/(keys[i][0]-keys[i-1][0]));
                return mix(pose(keys[i-1]),pose(keys[i]),smooth(amount));
            }
            return OPEN;
        }
        private Pose pose(float[] key){return new Pose(key[1],key[2],key[3],key[4]);}
    }

    public static float blink(double elapsed) {
        if(elapsed<=0 || elapsed>=183 || !Double.isFinite(elapsed))return 0;
        if(elapsed<73.2)return smooth((float)(elapsed/73.2));
        if(elapsed<=81.2)return 1;
        return 1-smooth((float)((elapsed-81.2)/101.8));
    }
    public static Pose mix(Pose a,Pose b,float t) {
        t=clamp(t,0,1);
        return new Pose(a.left+(b.left-a.left)*t,a.right+(b.right-a.right)*t,
                        a.x+(b.x-a.x)*t,a.y+(b.y-a.y)*t);
    }
    public static float smooth(float x){x=clamp(x,0,1);return x*x*(3-2*x);}
    private static float clamp(float x,float lo,float hi){return Float.isFinite(x)?Math.max(lo,Math.min(hi,x)):0;}

    /** Caller supplies monotonic milliseconds. Pause freezes time; resume never catches up. */
    public static final class Controller {
        private Clip clip;
        private final Random random;
        private double start, transitionStart, nextBlink, activeBlink=-1, pauseAt;
        private boolean paused, doubleBlink, ambientBlinkEnabled=true;
        private Pose from=OPEN;
        private float transitionMs;
        public Controller(Clip initial,long now,long seed){random=new Random(seed);select(initial,now,0);}
        public void select(Clip next,long now,float blendMs) {
            if(next==null)throw new IllegalArgumentException("Clip required");
            double t=paused?pauseAt:now;
            from=clip==null?OPEN:sample(now);
            clip=next; start=t;transitionStart=t;
            transitionMs=Math.max(0,blendMs);activeBlink=-1;
            nextBlink=t+3000+random.nextInt(4001);
        }
        public Pose sample(long now) {
            double t=paused?pauseAt:now;
            Pose p=clip.sample(t-start);
            if(clip.ambientBlink && ambientBlinkEnabled) {
                if(activeBlink<0 && t>=nextBlink){activeBlink=t;doubleBlink=random.nextInt(100)<18;}
                if(activeBlink>=0) {
                    double elapsed=t-activeBlink;
                    double total=doubleBlink?476:183;
                    if(elapsed>=total){activeBlink=-1;nextBlink=t+3000+random.nextInt(4001);}
                    else p=p.blink(blink(elapsed>=293?elapsed-293:elapsed));
                }
            }
            if(transitionMs>0 && t-transitionStart<transitionMs)
                p=mix(from,p,smooth((float)((t-transitionStart)/transitionMs)));
            return p;
        }
        public void pause(long now){if(!paused){pauseAt=now;paused=true;}}
        public void resume(long now){if(paused){double delta=Math.max(0,now-pauseAt);start+=delta;transitionStart+=delta;nextBlink+=delta;if(activeBlink>=0)activeBlink+=delta;paused=false;}}
        public void setAmbientBlinkEnabled(boolean enabled){ambientBlinkEnabled=enabled;if(!enabled)activeBlink=-1;}
        public boolean ambientBlinkEnabled(){return ambientBlinkEnabled;}
        public Clip clip(){return clip;}
    }
}
