package com.boop.shieldhome;

/** UI-thread ownership: a cancelled or superseded lookup can never paint a new track. */
public final class LyricsRequestGate {
    private long generation;
    private String identity = "";
    public LyricsRequestGate() { }
    public long begin(String identity) {
        this.identity = identity == null ? "" : identity;
        return ++generation;
    }
    public boolean accepts(long ticket, String currentIdentity) {
        return ticket == generation && !identity.isEmpty() && identity.equals(currentIdentity);
    }
    public void cancel() { generation++; identity = ""; }
}
