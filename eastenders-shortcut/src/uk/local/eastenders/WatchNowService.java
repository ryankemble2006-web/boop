package uk.local.eastenders;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public final class WatchNowService extends AccessibilityService {
    private static final String TAG = "EastEnders";
    private static WatchNowService instance;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private String expectedPackage;
    private boolean armed;
    private long armTimestamp;

    // Retry trackers with debouncing
    private long lastProfileClickTime;
    private int profileClickAttempts;
    private long lastEpisodeClickTime;
    private int episodeClickAttempts;
    private long lastTrailerClickTime;

    // Playback state trackers
    private boolean playbackStarted;
    private long playbackStartTime;
    private long playbackEndedCandidateTime;
    private boolean returningHome;

    private static final long MAX_LAUNCH_WAIT_MS = 60_000L; // 60s max to reach active playback
    private static final long MAX_TOTAL_SESSION_MS = 3L * 60L * 60L * 1000L; // 3 hours max session
    private static final long MIN_PLAYBACK_FOR_AUTO_HOME_MS = 20_000L; // 20s minimum playback before returning home
    private static final long CLICK_DEBOUNCE_MS = 1_200L; // wait 1.2s before re-attempting click

    private static final class ParsedScreen {
        boolean isProfileChooser;
        AccessibilityNodeInfo firstProfile;
        boolean isEastEndersPage;
        AccessibilityNodeInfo watchNowButton;
        AccessibilityNodeInfo skipTrailerButton;
        AccessibilityNodeInfo skipIntroButton;
        boolean isPlaybackActive;
    }

    private final Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            if (!armed) return;
            inspect();
            if (armed) {
                handler.postDelayed(this, 500);
            }
        }
    };

    public static boolean ready() {
        return instance != null;
    }

    public static void arm(String pkg) {
        if (instance == null) return;
        instance.stop();
        instance.expectedPackage = pkg;
        instance.armed = true;
        instance.armTimestamp = SystemClock.elapsedRealtime();
        Log.i(TAG, "Auto-play armed for " + pkg);
        instance.handler.postDelayed(instance.pollingRunnable, 400);
    }

    public static void cancel() {
        if (instance != null) {
            instance.stop();
        }
    }

    private void stop() {
        armed = false;
        handler.removeCallbacks(pollingRunnable);
        expectedPackage = null;
        lastProfileClickTime = 0;
        profileClickAttempts = 0;
        lastEpisodeClickTime = 0;
        episodeClickAttempts = 0;
        lastTrailerClickTime = 0;
        playbackStarted = false;
        playbackStartTime = 0;
        playbackEndedCandidateTime = 0;
        returningHome = false;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Log.i(TAG, "WatchNowService connected and ready.");
    }

    @Override
    public void onInterrupt() {
        stop();
    }

    @Override
    public void onDestroy() {
        stop();
        if (instance == this) {
            instance = null;
        }
        super.onDestroy();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!armed || returningHome) return;

        if (event != null && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            CharSequence pkg = event.getPackageName();
            if (pkg != null) {
                observeWindow(pkg.toString());
            }
            if (!armed) return;
        }

        inspect();
    }

    private void observeWindow(String pkg) {
        if (pkg == null) return;
        if (expectedPackage != null && pkg.equals(expectedPackage)) {
            return;
        }
        if (pkg.equals(getPackageName()) || pkg.equals("android") || pkg.equals("com.android.systemui")) {
            return;
        }
        // User switched to another app (Home, Netflix, Kodi, Deezer, etc.)
        Log.i(TAG, "Auto-play disarmed on window focus change to: " + pkg);
        stop();
    }

    private void inspect() {
        if (!armed || returningHome) return;
        long now = SystemClock.elapsedRealtime();

        if (now - armTimestamp > MAX_TOTAL_SESSION_MS) {
            Log.i(TAG, "Session timed out after 3 hours — disarming.");
            stop();
            return;
        }

        if (!playbackStarted && (now - armTimestamp > MAX_LAUNCH_WAIT_MS)) {
            Log.w(TAG, "Launch timeout: playback did not start within 60s — disarming.");
            stop();
            return;
        }

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        ParsedScreen screen = new ParsedScreen();
        try {
            CharSequence rootPkg = root.getPackageName();
            if (rootPkg != null) {
                observeWindow(rootPkg.toString());
            }
            if (!armed) return;

            scanNode(root, screen, 0);

            // PHASE 1: Profile Chooser
            if (screen.isProfileChooser && screen.firstProfile != null) {
                if (now - lastProfileClickTime > CLICK_DEBOUNCE_MS && profileClickAttempts < 6) {
                    lastProfileClickTime = now;
                    profileClickAttempts++;
                    Log.i(TAG, "Selecting iPlayer profile (attempt " + profileClickAttempts + "/6)");
                    clickNode(screen.firstProfile);
                }
                return;
            }

            // PHASE 2: Skip Trailer / Pre-roll
            if (screen.skipTrailerButton != null) {
                if (now - lastTrailerClickTime > CLICK_DEBOUNCE_MS) {
                    lastTrailerClickTime = now;
                    Log.i(TAG, "Trailer detected — skipping trailer");
                    clickNode(screen.skipTrailerButton);
                }
                return;
            }

            // PHASE 3: EastEnders Programme Page & Newest Episode CTA
            if (!playbackStarted && (screen.isEastEndersPage || screen.watchNowButton != null)) {
                if (screen.watchNowButton != null) {
                    if (now - lastEpisodeClickTime > CLICK_DEBOUNCE_MS && episodeClickAttempts < 6) {
                        lastEpisodeClickTime = now;
                        episodeClickAttempts++;
                        Log.i(TAG, "Newest episode CTA found — clicking Watch Now (attempt " + episodeClickAttempts + "/6)");
                        clickNode(screen.watchNowButton);
                    }
                    return;
                }
            }

            // PHASE 4: Playback Active
            if (screen.isPlaybackActive) {
                if (!playbackStarted) {
                    playbackStarted = true;
                    playbackStartTime = now;
                    playbackEndedCandidateTime = 0;
                    Log.i(TAG, "EastEnders playback started successfully!");
                }
                if (screen.skipIntroButton != null) {
                    Log.i(TAG, "Intro detected — skipping intro");
                    clickNode(screen.skipIntroButton);
                }
                return;
            }

            // PHASE 5: Playback Completed -> Return Home
            if (playbackStarted) {
                long duration = now - playbackStartTime;
                if (duration >= MIN_PLAYBACK_FOR_AUTO_HOME_MS) {
                    if (screen.isEastEndersPage || !screen.isPlaybackActive) {
                        if (playbackEndedCandidateTime == 0) {
                            playbackEndedCandidateTime = now;
                            Log.i(TAG, "Playback ended candidate detected; debouncing before return to Home...");
                        } else if (now - playbackEndedCandidateTime >= 2000L) {
                            Log.i(TAG, "EastEnders playback completed (" + (duration / 1000) + "s) — returning to Home!");
                            goHome();
                        }
                    } else {
                        playbackEndedCandidateTime = 0;
                    }
                }
            }

        } finally {
            recycleScreen(screen);
            root.recycle();
        }
    }

    private void scanNode(AccessibilityNodeInfo node, ParsedScreen screen, int depth) {
        if (node == null || depth > 35) return;

        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        String viewId = node.getViewIdResourceName();

        if (WatchNowPolicy.isProfileChooser(text, desc, viewId)) {
            screen.isProfileChooser = true;
        }

        if (screen.firstProfile == null && node.isClickable() && node.isEnabled()
                && WatchNowPolicy.isProfileButton(viewId, text, desc)) {
            screen.firstProfile = AccessibilityNodeInfo.obtain(node);
        }

        if (WatchNowPolicy.isProgrammePage(viewId, text)) {
            screen.isEastEndersPage = true;
        }

        if (screen.watchNowButton == null && node.isEnabled()
                && WatchNowPolicy.isWatchNowButton(viewId, text, desc)) {
            screen.watchNowButton = AccessibilityNodeInfo.obtain(node);
        }

        if (screen.skipTrailerButton == null && node.isEnabled()
                && WatchNowPolicy.isSkipTrailer(viewId, text, desc)) {
            screen.skipTrailerButton = AccessibilityNodeInfo.obtain(node);
        }

        if (screen.skipIntroButton == null && node.isEnabled()
                && WatchNowPolicy.isSkipIntro(viewId, text, desc)) {
            screen.skipIntroButton = AccessibilityNodeInfo.obtain(node);
        }

        if (WatchNowPolicy.isPlaybackIndicator(viewId)) {
            screen.isPlaybackActive = true;
        }

        int count = node.getChildCount();
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                try {
                    scanNode(child, screen, depth + 1);
                } finally {
                    child.recycle();
                }
            }
        }
    }

    private void clickNode(AccessibilityNodeInfo node) {
        if (node == null) return;
        try {
            boolean clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            if (!clicked && node.getParent() != null) {
                clicked = node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
            node.performAction(AccessibilityNodeInfo.ACTION_SELECT);
            if (!node.isFocused()) {
                node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            }

            Rect bounds = new Rect();
            node.getBoundsInScreen(bounds);
            if (bounds.width() > 0 && bounds.height() > 0) {
                dispatchTap(bounds.centerX(), bounds.centerY());
            }
        } catch (Throwable t) {
            Log.w(TAG, "clickNode error: " + t.getMessage());
        }
    }

    private void dispatchTap(int x, int y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Path path = new Path();
            path.moveTo(x, y);
            GestureDescription.Builder builder = new GestureDescription.Builder();
            builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 50));
            try {
                dispatchGesture(builder.build(), null, null);
            } catch (Throwable t) {
                Log.w(TAG, "dispatchGesture not supported on this device: " + t.getMessage());
            }
        }
    }

    private void goHome() {
        returningHome = true;
        stop();
        if (!performGlobalAction(GLOBAL_ACTION_HOME)) {
            try {
                startActivity(new Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_HOME)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (Throwable rejected) {
                Log.w(TAG, "Failed to navigate Home: " + rejected.getMessage());
            }
        }
    }

    private void recycleScreen(ParsedScreen screen) {
        if (screen.firstProfile != null) screen.firstProfile.recycle();
        if (screen.watchNowButton != null) screen.watchNowButton.recycle();
        if (screen.skipTrailerButton != null) screen.skipTrailerButton.recycle();
        if (screen.skipIntroButton != null) screen.skipIntroButton.recycle();
    }
}
