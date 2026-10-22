package uk.local.eastenders;

public final class WatchNowPolicyTest {
    public static void main(String[] args) {
        testProfileChooser();
        testProfileButton();
        testProgrammePage();
        testWatchNowButton();
        testSkipTrailer();
        testSkipIntro();
        testPlaybackIndicator();
        System.out.println("ALL WATCH NOW POLICY TESTS PASSED SUCCESSFULLY!");
    }

    private static void assertTrue(String msg, boolean val) {
        if (!val) throw new AssertionError("Test failed: " + msg);
    }

    private static void assertFalse(String msg, boolean val) {
        if (val) throw new AssertionError("Test failed: " + msg);
    }

    private static void testProfileChooser() {
        assertTrue("Description who's watching", WatchNowPolicy.isProfileChooser(null, "Select who's watching....", null));
        assertTrue("Text who's watching", WatchNowPolicy.isProfileChooser("Who's watching?", null, null));
        assertTrue("ViewId user-switcher", WatchNowPolicy.isProfileChooser(null, null, "user-switcher"));
        assertTrue("ViewId account-layout-title", WatchNowPolicy.isProfileChooser(null, null, "account-layout-title"));
        assertFalse("Random page", WatchNowPolicy.isProfileChooser("Home", "BBC iPlayer", "main-view"));
    }

    private static void testProfileButton() {
        assertTrue("Polly button with id", WatchNowPolicy.isProfileButton("avatar-7c3d3a33-8d3a-47c5-baac-2c5eb2cfe9ec", "Polly", null));
        assertTrue("Polly button text only", WatchNowPolicy.isProfileButton(null, "Polly", null));
        assertTrue("Ryan button text only", WatchNowPolicy.isProfileButton(null, "Ryan", null));
        assertFalse("Add child button", WatchNowPolicy.isProfileButton("avatar-add-profile", "Add child", null));
        assertFalse("Add adult button", WatchNowPolicy.isProfileButton("avatar-add-adult", "Add adult", null));
        assertFalse("Edit button", WatchNowPolicy.isProfileButton("edit-Polly", null, "Edit Polly"));
        assertFalse("Avatar label textview", WatchNowPolicy.isProfileButton("avatar-label-7c3d3a33-8d3a-47c5-baac-2c5eb2cfe9ec", "Polly", null));
        assertFalse("Language button", WatchNowPolicy.isProfileButton("language-switcher", "Language", null));
    }

    private static void testProgrammePage() {
        assertTrue("EastEnders text", WatchNowPolicy.isProgrammePage(null, "EastEnders"));
        assertTrue("Programme grid", WatchNowPolicy.isProgrammePage("programme-grid", null));
        assertTrue("Programme title", WatchNowPolicy.isProgrammePage("b006m86d-title", null));
        assertTrue("Programme unsliced", WatchNowPolicy.isProgrammePage("unsliced", null));
        assertFalse("Other page", WatchNowPolicy.isProgrammePage("other-view", "Coronation Street"));
    }

    private static void testWatchNowButton() {
        // Hero CTA button from real BBC iPlayer
        assertTrue("Hero CTA viewId", WatchNowPolicy.isWatchNowButton("cta:m00336w8", "EastEnders 22/10/2026 - 28 mins Watch now", null));
        assertTrue("Watch now text", WatchNowPolicy.isWatchNowButton(null, "Watch now", null));
        assertTrue("Watch from start", WatchNowPolicy.isWatchNowButton(null, "Watch from start", null));
        assertTrue("Resume button", WatchNowPolicy.isWatchNowButton(null, "Resume (14 mins remaining)", null));
        assertTrue("Play episode", WatchNowPolicy.isWatchNowButton(null, "Play episode", null));
        assertFalse("Watchlist button", WatchNowPolicy.isWatchNowButton("add-button", "Watchlist", null));
        assertFalse("Back button", WatchNowPolicy.isWatchNowButton("back-button", "Back", null));
        assertFalse("iPlayer brand", WatchNowPolicy.isWatchNowButton(null, "BBC iPlayer", null));
    }

    private static void testSkipTrailer() {
        assertTrue("Skip prerolls viewId", WatchNowPolicy.isSkipTrailer("skip-prerolls", "Skip trailer", null));
        assertTrue("Skip trailer text", WatchNowPolicy.isSkipTrailer(null, "Skip trailer", null));
        assertTrue("Skip pre-roll text", WatchNowPolicy.isSkipTrailer(null, "Skip pre-roll", null));
        assertFalse("Other button", WatchNowPolicy.isSkipTrailer("play-btn", "Play", null));
    }

    private static void testSkipIntro() {
        assertTrue("Skip intro button", WatchNowPolicy.isSkipIntro("m00336w8::0", "Skip intro", null));
        assertFalse("Subtitles button", WatchNowPolicy.isSkipIntro("subtitles", "Subtitles Off", null));
    }

    private static void testPlaybackIndicator() {
        assertTrue("Playback controls", WatchNowPolicy.isPlaybackIndicator("playback-controls"));
        assertTrue("Playback scrubbar", WatchNowPolicy.isPlaybackIndicator("playback-scrubbar"));
        assertTrue("Playback time", WatchNowPolicy.isPlaybackIndicator("playback-time"));
        assertTrue("Playback time VOD", WatchNowPolicy.isPlaybackIndicator("playback-time-vod"));
        assertFalse("Programme header", WatchNowPolicy.isPlaybackIndicator("b006m86d-header"));
    }
}
