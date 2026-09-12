package uk.local.eastenders;

final class ClickGate {
    private static final long PROFILE_DEBOUNCE_MS = 1500L;
    private static final long RETURN_FOCUS_TIMEOUT_MS = 2L * 60L * 60L * 1000L;
    private long deadline;
    private long recoveryDeadline;
    private boolean recoverySawPlayback;
    private int profileClicks;
    private long lastProfileClickAt;

    void arm(long now) {
        deadline = now + 120000L;
        recoveryDeadline = 0;
        recoverySawPlayback = false;
        profileClicks = 0;
        lastProfileClickAt = 0;
    }

    void cancel() {
        deadline = 0;
        recoveryDeadline = 0;
        recoverySawPlayback = false;
    }

    void observePackage(String actual, String player, String launcher) {
        if (!actual.equals(player) && !actual.equals(launcher)
                && !actual.equals("android") && !actual.equals("com.android.systemui")) cancel();
    }

    boolean launchActive(long now) { return deadline != 0 && now < deadline; }
    boolean recoveryActive(long now) { return recoveryDeadline != 0 && now < recoveryDeadline; }
    boolean active(long now) { return launchActive(now) || recoveryActive(now); }

    boolean returnPageReady(long now, boolean eastEndersPage) {
        if (!recoveryActive(now)) return false;
        if (!eastEndersPage) {
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

    boolean claimEpisode(long now, boolean eastEnders, boolean newestEpisode) {
        if (!launchActive(now) || !eastEnders || !newestEpisode) return false;
        deadline = 0;
        recoveryDeadline = now + RETURN_FOCUS_TIMEOUT_MS;
        recoverySawPlayback = false;
        return true;
    }
}
