package uk.local.casualty;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.content.Intent;
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
    private PlayerBridgeClient bridge;
    private boolean preparing;
    private boolean returningHome;

    private void report(String state) {
        if (!state.equals(lastState)) {
            lastState = state;
            Log.i("Casualty", state);
        }
    }

    private static final class Page {
        boolean chooser;
        boolean title;
        AccessibilityNodeInfo profile;
        AccessibilityNodeInfo episode;
        AccessibilityNodeInfo trailer;
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
            if (text != null && "Casualty".contentEquals(text)) page.title = true;
            if (insideNewestRow && page.episode == null && node.isEnabled() && node.isClickable()
                    && UiPolicy.isEpisodeCard(viewId, description)) {
                page.episode = AccessibilityNodeInfo.obtain(node);
            }
            if (page.trailer == null && node.isEnabled() && node.isClickable()
                    && UiPolicy.isSkipTrailer(text, description)) {
                page.trailer = AccessibilityNodeInfo.obtain(node);
            }
        }

        for (int i = 0; i < node.getChildCount() && page.visited < 1500; i++) {
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
            long now = SystemClock.elapsedRealtime();
            if (gate.launchActive(now) || gate.trailerWatchActive(now)) handler.postDelayed(this, 500);
        }
    };

    static boolean ready() { return instance != null; }

    static void prepareColdStart(PlayerBridgeClient.Callback callback) {
        if(instance==null) { callback.done(false,"Enable the shortcut auto-play helper first"); return; }
        WatchNowService self=instance;
        self.stop(); self.preparing=true; self.expectedPackage=PlayerReset.PLAYER;
        PlayerBridgeClient client=new PlayerBridgeClient(self,() -> self.stop()); self.bridge=client;
        client.prepare((ok,error) -> {
            if(self.bridge!=client || !self.preparing) return;
            self.preparing=false;
            if(ok) {
                self.lastState=null; self.gate.arm(SystemClock.elapsedRealtime());
                self.handler.postDelayed(self.check,500);
                self.report("Clean iPlayer start verified; macro armed");
            } else self.stop();
            callback.done(ok,error);
        });
    }

    static void cancel() {
        if (instance != null) {
            if (instance.gate.active(SystemClock.elapsedRealtime())) Log.i("Casualty", "Auto-play cancelled by launcher");
            instance.stop();
        }
    }

    private void stop() {
        gate.cancel();
        handler.removeCallbacks(check);
        preparing=false; returningHome=false;
        if(bridge!=null) { PlayerBridgeClient old=bridge; bridge=null; old.close(); }
    }

    @Override protected void onServiceConnected() { instance = this; }
    @Override public void onInterrupt() { stop(); }

    @Override public void onDestroy() {
        stop();
        if (instance == this) instance = null;
        super.onDestroy();
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if(preparing || returningHome) {
            if(event!=null && event.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                    && event.getPackageName()!=null) {
                String pkg=event.getPackageName().toString();
                if(!pkg.equals(expectedPackage) && !pkg.equals(getPackageName())
                        && !pkg.equals("android") && !pkg.equals("com.android.systemui")
                        && !(returningHome && isConfiguredHome(pkg))) stop();
            }
            return;
        }
        if (!gate.active(SystemClock.elapsedRealtime())) return;
        // Observe foreground departures even if the root has already changed again.
        // Only iPlayer roots are inspected below; other apps can only cancel this session.
        if (event != null && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                && event.getPackageName() != null) {
            gate.observePackage(event.getPackageName().toString(), expectedPackage, getPackageName());
            if (!gate.active(SystemClock.elapsedRealtime())) {
                stop();
                return;
            }
        }
        inspect();
    }

    private boolean isConfiguredHome(String pkg) {
        android.content.pm.ResolveInfo home=getPackageManager().resolveActivity(
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),0);
        return home!=null && home.activityInfo!=null && pkg.equals(home.activityInfo.packageName);
    }

    private void goHome(boolean clean) {
        report(clean ? "iPlayer fully stopped; returning Home" : "Cleanup unconfirmed; returning Home");
        stop();
        if(!performGlobalAction(GLOBAL_ACTION_HOME)) {
            try { startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); }
            catch(RuntimeException rejected) { report("Use the remote Home button"); }
        }
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
                    Log.i("Casualty", "Auto-play cancelled on foreground change: " + pkg);
                    stop();
                }
                return;
            }

            Page page = new Page();
            try {
                scan(root, page, 0, false);
                long now = SystemClock.elapsedRealtime();
                if (gate.recoveryActive(now)) {
                    if (gate.claimTrailer(now, page.trailer != null)) {
                        boolean skipped = page.trailer.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Log.i("Casualty", "Skip trailer click accepted: " + skipped);
                        report(skipped ? "Trailer skipped" : "Skip trailer control rejected click");
                        return;
                    }
                    if (gate.claimReturnHome(now, page.title, page.episode != null)) {
                        handler.removeCallbacks(check);
                        returningHome=true;
                        PlayerBridgeClient client=bridge;
                        if(client==null) { goHome(false); return; }
                        client.stopPlayer((ok,error) -> {
                            if(bridge!=client || !returningHome) return;
                            if(!ok) android.widget.Toast.makeText(this,error,android.widget.Toast.LENGTH_LONG).show();
                            goHome(ok);
                        });
                    } else {
                        report(page.title ? "Waiting for programme return after playback" : "Playback active; Home return armed");
                    }
                    return;
                }
                if (gate.claimProfile(SystemClock.elapsedRealtime(), page.chooser, page.profile != null)) {
                    boolean clicked = page.profile.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Log.i("Casualty", "Existing iPlayer profile click accepted: " + clicked);
                    report("Profile selected; waiting for Casualty");
                    return;
                }
                if (!page.title) {
                    report(page.chooser ? "Waiting for existing iPlayer profile" : "Waiting for Casualty title; nodes: " + page.visited);
                    return;
                }
                report("Casualty title found; waiting for newest episode");
                if (gate.claimEpisode(SystemClock.elapsedRealtime(), true, page.episode != null)) {
                    boolean clicked = page.episode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Log.i("Casualty", "Newest episode click accepted: " + clicked);
                    if (!clicked) stop();
                }
            } finally {
                if (page.profile != null) page.profile.recycle();
                if (page.episode != null) page.episode.recycle();
                if (page.trailer != null) page.trailer.recycle();
            }
        } finally {
            root.recycle();
        }
    }
}