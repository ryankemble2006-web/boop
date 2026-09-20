package com.boop.bridge;
import java.io.IOException;
public final class HeartConfirmationProbe {
    static int checks;
    static void yes(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    static DeezerHeartConfirmation.Frame f(long seq,long ms,int saved){return new DeezerHeartConfirmation.Frame(seq,ms,saved);}
    static final class Feed implements DeezerHeartConfirmation.Source {
        final DeezerHeartConfirmation.Frame[] frames; int calls;
        Feed(DeezerHeartConfirmation.Frame... frames){this.frames=frames;}
        public DeezerHeartConfirmation.Frame next(long after,long deadline){return calls<frames.length?frames[calls++]:null;}
    }
    public static void main(String[] args)throws Exception {
        Feed slow = new Feed(f(11,300,0),f(12,1200,0),f(13,1800,-1),f(14,2600,1));
        yes(DeezerHeartConfirmation.awaitChange(0,10,6000,slow)==1,"Slow true save confirms beyond the old 1200ms sample");
        yes(slow.calls==4,"One event-driven pass, no action retries");
        yes(DeezerHeartConfirmation.awaitChange(1,20,6000,new Feed(f(21,1800,0)))==0,"Unfavourite is confirmed too");
        yes(DeezerHeartConfirmation.awaitChange(0,10,6000,new Feed(f(10,100,1)))==-1,"Pre-click frame is not confirmation");
        yes(DeezerHeartConfirmation.awaitChange(0,10,6000,new Feed(f(11,6001,1)))==-1,"Receipt after deadline rejected");
        yes(DeezerHeartConfirmation.awaitChange(0,10,6000,new Feed(f(11,100,0),f(12,200,-1)))==-1,"No fabricated state on timeout or hidden glyph");
        yes(DeezerHeartConfirmation.awaitChange(-1,10,6000,new Feed(f(11,100,1)))==-1,"Unknown initial state is not a toggle receipt");
        yes(DeezerHeartConfirmation.awaitChange(0,10,6000,new Feed(f(12,100,0),f(11,200,1)))==-1,"Out-of-order frame rejected");
        yes(DeezerHeartConfirmation.awaitChange(0,10,6000,new Feed(f(11,-1,1)))==-1,"Invalid timestamp rejected");
        try {DeezerHeartConfirmation.awaitChange(0,10,6000,(s,d)->{throw new IOException("track changed");});throw new AssertionError("Cancellation ignored");}
        catch(IOException expected){checks++;}
        Thread.currentThread().interrupt();
        try {DeezerHeartConfirmation.awaitChange(0,10,6000,new Feed(f(11,100,1)));throw new AssertionError("Interrupt ignored");}
        catch(InterruptedException expected){checks++;}finally{Thread.interrupted();}
        System.out.println("Heart confirmation: "+checks+" behavioural checks passed");
    }
}
