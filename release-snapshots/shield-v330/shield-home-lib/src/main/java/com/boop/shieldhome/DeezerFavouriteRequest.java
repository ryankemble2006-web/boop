package com.boop.shieldhome;

/** One in-flight explicit request. Dispatch is not a success receipt. */
public final class DeezerFavouriteRequest {
    public static final long TIMEOUT_MS = 3000L;
    private long session, started;
    private String track;
    private boolean pending, target;

    public boolean begin(long session, String track, boolean target, long now) {
        if (pending || session <= 0 || track == null || track.isEmpty()) return false;
        this.session = session;
        this.track = track;
        this.target = target;
        started = now;
        pending = true;
        return true;
    }
    public boolean owns(long session, String track) {
        return pending && DeezerFavouritePolicy.sameTrack(this.session, this.track, session, track);
    }
    public boolean confirm(long session, String track, int providerState) {
        if (!owns(session, track) || providerState != (target ? 1 : 0)) return false;
        cancel();
        return true;
    }
    public boolean expired(long now) { return pending && now - started >= TIMEOUT_MS; }
    public boolean pending() { return pending; }
    public boolean target() { return target; }
    public void cancel() { pending = false; session = 0; track = null; }
}
