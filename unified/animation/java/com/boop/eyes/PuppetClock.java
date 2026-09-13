package com.boop.eyes;
/** One monotonic BOOP clock. No dependency on Android animation scales. */
public final class PuppetClock {
    private long realTime;
    private double logicalTime, speed=1.0;
    private boolean paused;
    public PuppetClock(long now){realTime=now;logicalTime=now;}
    public long now(long realNow){
        long delta=Math.max(0,realNow-realTime);
        realTime=Math.max(realTime,realNow);
        if(!paused)logicalTime+=delta*speed;
        return (long)logicalTime;
    }
    public void setSpeed(double value,long realNow){
        now(realNow);
        speed=Double.isFinite(value)&&value>=0&&value<=2?value:1.0;
    }
    public double speed(){return speed;}
    public void pause(long realNow){now(realNow);paused=true;}
    public void resume(long realNow){realTime=realNow;paused=false;}
}
