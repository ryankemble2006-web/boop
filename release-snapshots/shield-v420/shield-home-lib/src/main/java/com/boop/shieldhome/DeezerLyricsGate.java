package com.boop.shieldhome;

/** Single-use request ownership, exact-session pinning and a hard UI deadline. */
final class DeezerLyricsGate {
    static final long TIMEOUT_MS = 2500L;
    enum Action { OPEN, NO_LYRICS, CANNOT_CHECK, IGNORE }
    static final class Ticket {
        final String identity;
        final long deadline;
        Ticket(String identity, long deadline) { this.identity=identity; this.deadline=deadline; }
    }
    private Ticket active;
    synchronized Ticket begin(String identity, long now) {
        if (identity == null || identity.isEmpty()) return null;
        if (active != null && active.identity.equals(identity) && now < active.deadline) return null;
        active = new Ticket(identity, now + TIMEOUT_MS);
        return active;
    }
    synchronized boolean consume(Ticket ticket, String currentIdentity, long now) {
        if (ticket == null || active != ticket) return false;
        active = null;
        return now < ticket.deadline && ticket.identity.equals(currentIdentity);
    }
    synchronized Action resolve(Ticket ticket, String currentIdentity, long now,
            DeezerLyricsClient.Result result) {
        if (!consume(ticket, currentIdentity, now)) return Action.IGNORE;
        if (result == DeezerLyricsClient.Result.AVAILABLE) return Action.OPEN;
        if (result == DeezerLyricsClient.Result.UNAVAILABLE) return Action.NO_LYRICS;
        return Action.CANNOT_CHECK;
    }
    synchronized boolean expire(Ticket ticket, long now) {
        if (ticket == null || active != ticket || now < ticket.deadline) return false;
        active = null;
        return true;
    }
    synchronized void cancel() { active = null; }
}
