package uk.local.eastenders;

final class ClickGate {
    private static final long PROFILE_DEBOUNCE_MS = 1500L;
    private long deadline;
    private int profileClicks;
    private long lastProfileClickAt;

    void arm(long now) {
        deadline = now + 120000L;
        profileClicks = 0;
        lastProfileClickAt = 0;
    }

    void cancel() { deadline = 0; }

    void observePackage(String actual, String player, String launcher) {
        if (!actual.equals(player) && !actual.equals(launcher)
                && !actual.equals("android") && !actual.equals("com.android.systemui")) cancel();
    }

    boolean active(long now) { return deadline != 0 && now < deadline; }

    boolean claimProfile(long now, boolean chooser, boolean focusedExistingProfile) {
        if (!active(now) || profileClicks >= 2 || !chooser || !focusedExistingProfile) return false;
        if (profileClicks > 0 && now - lastProfileClickAt < PROFILE_DEBOUNCE_MS) return false;
        profileClicks++;
        lastProfileClickAt = now;
        return true;
    }

    boolean claimEpisode(long now, boolean eastEnders, boolean newestEpisode) {
        if (!active(now) || !eastEnders || !newestEpisode) return false;
        cancel();
        return true;
    }
}
