package com.boop.alpha1;

/** UI-thread state: acknowledgements belong to the selected phone; Stop is never dropped. */
public final class BoopPreviewSendGate {
    public static final class Ticket {
        final int endpoint;
        final String command;
        Ticket(int endpoint, String command) { this.endpoint=endpoint; this.command=command; }
    }
    private int endpoint;
    private Ticket inFlight;
    private boolean paired, pendingStop;
    public void select() { endpoint++; paired=false; pendingStop=false; }
    public boolean paired() { return paired; }
    public boolean busy() { return inFlight!=null; }
    public Ticket begin(String command) {
        if (busy()) throw new IllegalStateException("Request already in flight");
        return inFlight=new Ticket(endpoint,command);
    }
    public boolean queueStopIfBusy() { if (!busy()) return false; pendingStop=true; return true; }
    public boolean finish(Ticket ticket, boolean success) {
        if (ticket!=inFlight) return false;
        inFlight=null;
        if (ticket.endpoint!=endpoint) return false;
        if ("PING".equals(ticket.command)) paired=success;
        if (!success) { paired=false; pendingStop=false; }
        return true;
    }
    public boolean takeStop() { boolean send=pendingStop&&paired; pendingStop=false; return send; }
    public void close() { select(); inFlight=null; }
}
