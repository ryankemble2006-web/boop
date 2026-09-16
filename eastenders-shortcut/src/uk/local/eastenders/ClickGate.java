package uk.local.eastenders;

final class ClickGate {
    private static final long RETURN_HOME_TIMEOUT_MS = 2L * 60L * 60L * 1000L;
    private static final long TRAILER_WINDOW_MS = 60000L;
    private long deadline;
    private long recoveryDeadline;
    private long trailerDeadline;
    private boolean trailerClicked;
    private boolean recoverySawPlayback;
    private boolean profileClicked;

    void arm(long now) {
        deadline = now + 120000L;
        recoveryDeadline = 0;
        trailerDeadline = 0;
        trailerClicked = false;
        recoverySawPlayback = false;
        profileClicked = false;
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

    boolean returnPageReady(long now, boolean eastEndersPage) {
        if (!recoveryActive(now)) return false;
        if (!eastEndersPage) {
            recoverySawPlayback = true;
            return false;
        }
        return recoverySawPlayback;
    }
    boolean claimReturnHome(long now, boolean programmePage, boolean episodeCard) {
        if (!returnPageReady(now, programmePage) || !episodeCard) return false;
        cancel();
        return true;
    }

    boolean claimProfile(long now, boolean chooser, boolean existingProfile) {
        if (!launchActive(now) || profileClicked || !chooser || !existingProfile) return false;
        profileClicked = true;
        return true;
    }

    boolean claimEpisode(long now, boolean eastEnders, boolean newestEpisode) {
        if (!launchActive(now) || !eastEnders || !newestEpisode) return false;
        deadline = 0;
        recoveryDeadline = now + RETURN_HOME_TIMEOUT_MS;
        trailerDeadline = now + TRAILER_WINDOW_MS;
        trailerClicked = false;
        recoverySawPlayback = false;
        return true;
    }
}
