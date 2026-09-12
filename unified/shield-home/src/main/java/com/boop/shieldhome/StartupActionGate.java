package com.boop.shieldhome;
/** Reject duplicate work and invalidate callbacks after Back or Activity destruction. */
public final class StartupActionGate {
    private long generation;
    private boolean busy;
    public synchronized long begin() { if(busy) return -1; busy=true; return ++generation; }
    public synchronized boolean current(long token) { return busy && generation==token; }
    public synchronized boolean complete(long token) { if(!current(token)) return false; busy=false; return true; }
    public synchronized void cancel() { ++generation; busy=false; }
    public synchronized boolean busy() { return busy; }
}
