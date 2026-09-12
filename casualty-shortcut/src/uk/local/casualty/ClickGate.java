package uk.local.casualty;

final class ClickGate {
    private static final long PROFILE_DEBOUNCE_MS = 1500L;
    private static final long RETURN_FOCUS_TIMEOUT_MS = 2L * 60L * 60L * 1000L;
    private static final long TRAILER_WINDOW_MS = 60000L;
    private long deadline;
    private long recoveryDeadline;
    private long trailerDeadline;
    private boolean trailerClicked;
    private boolean recoverySawPlayback;
    private int profileClicks;
    private long lastProfileClickAt;

    void arm(long now) {
        deadline = now + 120000L;
        recoveryDeadline = 0;
        trailerDeadline = 0;
        trailerClicked = false;
        recoverySawPlayback = false;
        profileClicks = 0;
        lastProfileClickAt = 0;
    }

    void cancel() {
        deadline = 0;
        recoveryDeadline = 0;
        trailerDeadline = 0;
        trailerClicked = false;
        recoverySawPlayback = false;
    }

    void observePackage(String actual, String player, String launcher) {
        if (!actual.equals(player) && !actual.equals(launcher)
                && !actual.equals("android") && !actual.equals("com.android.systemui")) cancel();
    }

    boolean launchActive(long now) { return deadline != 0 && now < deadline; }
    boolean recoveryActive(long now) { return recoveryDeadline != 0 && now < recoveryDeadline; }
    boolean active(long now) { return launchActive(now) || recoveryActive(now); }
    boolean trailerWatchActive(long now) {
        return recoveryActive(now) && !trailerClicked && trailerDeadline != 0 && now < trailerDeadline;
    }

    boolean claimTrailer(long now, boolean exactVisibleControl) {
        if (!trailerWatchActive(now) || !exactVisibleControl) return false;
        trailerClicked = true;
        return true;
    }

    boolean returnPageReady(long now, boolean casualtyPage) {
        if (!recoveryActive(now)) return false;
        if (!casualtyPage) {
            recoverySawPlayback = true;
            return false;
        }
        return recoverySawPlayback;
    }
    boolean claimProfile(long now, boolean chooser, boolean focusedExistingProfile) {
        if (!launchActive(now) || profileClicks >= 2 || !chooser || !focusedExistingProfile) return false;
        if (profileClicks > 0 && now - lastProfileClickAt < PROFILE_DEBOUNCE_MS) return false;
        profileClicks++;
        lastProfileClickAt = now;
        return true;
    }

    boolean claimEpisode(long now, boolean casualty, boolean newestEpisode) {
        if (!launchActive(now) || !casualty || !newestEpisode) return false;
        deadline = 0;
        recoveryDeadline = now + RETURN_FOCUS_TIMEOUT_MS;
        trailerDeadline = now + TRAILER_WINDOW_MS;
        trailerClicked = false;
        recoverySawPlayback = false;
        return true;
    }
}
