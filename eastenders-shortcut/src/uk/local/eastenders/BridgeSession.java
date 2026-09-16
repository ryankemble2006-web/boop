package uk.local.eastenders;
/** One owner for the shared player; stale requests cannot affect a newer programme. */
final class BridgeSession {
    private long generation;
    private int owner=-1;
    synchronized long begin(int uid) { owner=uid; return ++generation; }
    synchronized boolean current(int uid,long id) { return owner==uid && generation==id; }
    synchronized void cancel(int uid,long id) { if(current(uid,id)) { owner=-1; generation++; } }
}
