package com.boop.bridge;

/** Waits for a real post-click state transition. The source blocks on frame arrivals;
 * it must never send input, poll the view tree, or repeat the favourite action.
 */
public final class DeezerHeartConfirmation {
    private DeezerHeartConfirmation() { }
    public static final class Frame {
        public final long sequence, receivedMs;
        public final int saved;
        public Frame(long sequence,long receivedMs,int saved) {
            this.sequence=sequence;this.receivedMs=receivedMs;this.saved=saved;
        }
    }
    public interface Source {
        Frame next(long afterSequence,long deadlineMs)throws Exception;
    }
    public static int awaitChange(int before,long afterSequence,long deadlineMs,Source source)throws Exception {
        if(before!=0&&before!=1)return -1;
        long cursor=afterSequence;
        for(int samples=0;samples<512;samples++) {
            if(Thread.currentThread().isInterrupted())throw new InterruptedException("Cancelled");
            Frame next=source.next(cursor,deadlineMs);
            if(next==null||next.sequence<=cursor||next.receivedMs<0||next.receivedMs>deadlineMs)return -1;
            cursor=next.sequence;
            if(next.saved==1-before)return next.saved;
        }
        return -1;
    }
}
