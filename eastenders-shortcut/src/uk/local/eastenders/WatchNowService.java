package uk.local.eastenders;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public final class WatchNowService extends AccessibilityService {
    private static WatchNowService instance;
    private final ClickGate gate = new ClickGate();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String expectedPackage;
    private String lastState;

    private void report(String state) {
        if (!state.equals(lastState)) {
            lastState = state;
            Log.i("EastEnders", state);
        }
    }

    private static final class Page {
        boolean chooser;
        boolean title;
        AccessibilityNodeInfo profile;
        AccessibilityNodeInfo episode;
        int visited;
    }

    private void scan(AccessibilityNodeInfo node, Page page, int depth, boolean newestRow) {
        if (depth > 40 || ++page.visited > 1500) return;
        boolean visible = node.isVisibleToUser();
        String viewId = node.getViewIdResourceName();
        CharSequence text = node.getText();
        CharSequence description = node.getContentDescription();
        boolean insideNewestRow = newestRow || UiPolicy.isEpisodeRow(viewId);

        if (visible) {
            if (UiPolicy.isProfileChooser(description)) page.chooser = true;
            if (page.profile == null && node.isEnabled() && node.isClickable() && node.isFocused()
                    && UiPolicy.isExistingProfile(viewId, text)) {
                page.profile = AccessibilityNodeInfo.obtain(node);
            }
            if (text != null && "EastEnders".contentEquals(text)) page.title = true;
            if (insideNewestRow && page.episode == null && node.isEnabled() && node.isClickable()
                    && UiPolicy.isEpisodeCard(viewId, description)) {
                page.episode = AccessibilityNodeInfo.obtain(node);
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                try { scan(child, page, depth + 1, insideNewestRow); }
                finally { child.recycle(); }
            }
        }
    }

    private final Runnable check = new Runnable() {
        @Override public void run() {
            inspect();
            if (gate.active(SystemClock.elapsedRealtime())) handler.postDelayed(this, 500);
        }
    };

    static boolean ready() { return instance != null; }

    static void arm(String pkg) {
        if (instance == null) return;
        instance.stop();
        instance.expectedPackage = pkg;
        instance.lastState = null;
        instance.gate.arm(SystemClock.elapsedRealtime());
        Log.i("EastEnders", "Auto-play armed");
        instance.handler.postDelayed(instance.check, 500);
    }

    static void cancel() {
        if (instance != null) {
            if (instance.gate.active(SystemClock.elapsedRealtime())) Log.i("EastEnders", "Auto-play cancelled by launcher");
            instance.stop();
        }
    }

    private void stop() {
        gate.cancel();
        handler.removeCallbacks(check);
    }

    @Override protected void onServiceConnected() { instance = this; }
    @Override public void onInterrupt() { stop(); }

    @Override public void onDestroy() {
        stop();
        if (instance == this) instance = null;
        super.onDestroy();
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!gate.active(SystemClock.elapsedRealtime())) return;
        inspect();
    }

    private void inspect() {
        if (!gate.active(SystemClock.elapsedRealtime())) return;
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            report("Waiting for accessible window");
            return;
        }
        try {
            String pkg = String.valueOf(root.getPackageName());
            gate.observePackage(pkg, expectedPackage, getPackageName());
            if (!pkg.equals(expectedPackage)) {
                if (!gate.active(SystemClock.elapsedRealtime())) {
                    Log.i("EastEnders", "Auto-play cancelled on foreground change: " + pkg);
                    stop();
                }
                return;
            }

            Page page = new Page();
            try {
                scan(root, page, 0, false);
                if (gate.claimProfile(SystemClock.elapsedRealtime(), page.chooser, page.profile != null)) {
                    boolean clicked = page.profile.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Log.i("EastEnders", "Existing iPlayer profile click accepted: " + clicked);
                    report("Profile selected; waiting for EastEnders");
                    return;
                }
                if (!page.title) {
                    report(page.chooser ? "Waiting for existing iPlayer profile" : "Waiting for EastEnders title; nodes: " + page.visited);
                    return;
                }
                report("EastEnders title found; waiting for newest episode");
                if (gate.claimEpisode(SystemClock.elapsedRealtime(), true, page.episode != null)) {
                    boolean clicked = page.episode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Log.i("EastEnders", "Newest episode click accepted: " + clicked);
                    handler.removeCallbacks(check);
                }
            } finally {
                if (page.profile != null) page.profile.recycle();
                if (page.episode != null) page.episode.recycle();
            }
        } finally {
            root.recycle();
        }
    }
}