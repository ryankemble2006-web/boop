package uk.local.casualty;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

public final class WatchNowService extends AccessibilityService {
    interface CleanupCallback { void done(boolean ok,String error); }
    private static final String SETTINGS_PACKAGE="com.android.tv.settings";
    private static final String PLAYER_TITLE="BBC iPlayer";
    private static WatchNowService instance;
    private final ClickGate gate = new ClickGate();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String expectedPackage;
    private String lastState;
    private boolean preparing;
    private boolean returningHome;
    private CleanupCallback cleanupCallback;
    private int stoppedObservations;
    private long cleanupLastAction;

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

    private static final class CleanupPage {
        boolean appInfo;
        boolean open;
        boolean uninstall;
        boolean breadcrumb;
        boolean confirm;
        AccessibilityNodeInfo forceStop;
        AccessibilityNodeInfo ok;
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
                    && UiPolicy.isExistingProfile(viewId, text)) page.profile = AccessibilityNodeInfo.obtain(node);
            if (text != null && "Casualty".contentEquals(text)) page.title = true;
            if (insideNewestRow && page.episode == null && node.isEnabled() && node.isClickable()
                    && UiPolicy.isEpisodeCard(viewId, description)) page.episode = AccessibilityNodeInfo.obtain(node);
            if (page.trailer == null && node.isEnabled() && node.isClickable()
                    && UiPolicy.isSkipTrailer(text, description)) page.trailer = AccessibilityNodeInfo.obtain(node);
        }
        for (int i = 0; i < node.getChildCount() && page.visited < 1500; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                try { scan(child, page, depth + 1, insideNewestRow); }
                finally { child.recycle(); }
            }
        }
    }

    private AccessibilityNodeInfo clickableAncestor(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo current=AccessibilityNodeInfo.obtain(node);
        for(int depth=0; current!=null && depth<8; depth++) {
            if(current.isEnabled() && current.isClickable()) return current;
            AccessibilityNodeInfo parent=current.getParent();
            current.recycle();
            current=parent;
        }
        if(current!=null) current.recycle();
        return null;
    }

    private void scanCleanup(AccessibilityNodeInfo node,CleanupPage page,int depth) {
        if(depth>40 || ++page.visited>1500) return;
        if(node.isVisibleToUser()) {
            String id=node.getViewIdResourceName();
            CharSequence text=node.getText();
            if("com.android.tv.settings:id/decor_title".equals(id) && PLAYER_TITLE.contentEquals(text)) page.appInfo=true;
            if("android:id/title".equals(id) && "Open".contentEquals(text)) page.open=true;
            if("android:id/title".equals(id) && "Uninstall".contentEquals(text)) page.uninstall=true;
            if(page.forceStop==null && "android:id/title".equals(id) && "Force stop".contentEquals(text))
                page.forceStop=clickableAncestor(node);
            if("com.android.tv.settings:id/guidance_breadcrumb".equals(id) && PLAYER_TITLE.contentEquals(text)) page.breadcrumb=true;
            if("com.android.tv.settings:id/guidance_title".equals(id) && "Force stop".contentEquals(text)) page.confirm=true;
            if(page.ok==null && "com.android.tv.settings:id/guidedactions_item_title".equals(id) && "OK".contentEquals(text))
                page.ok=clickableAncestor(node);
        }
        for(int i=0;i<node.getChildCount() && page.visited<1500;i++) {
            AccessibilityNodeInfo child=node.getChild(i);
            if(child!=null) {
                try { scanCleanup(child,page,depth+1); }
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

    private final Runnable cleanupPoll = new Runnable() {
        @Override public void run() {
            if(!(preparing || returningHome)) return;
            inspectCleanup();
            if(preparing || returningHome) handler.postDelayed(this,250);
        }
    };

    private final Runnable cleanupTimeout = () -> finishCleanup(false,"Could not verify that BBC iPlayer stopped in Android TV settings.");

    static boolean ready() { return instance != null; }

    static void prepareColdStart(CleanupCallback callback) {
        if(instance==null) { callback.done(false,"Enable the shortcut auto-play helper first"); return; }
        WatchNowService self=instance;
        self.stop();
        self.beginCleanup(false,callback);
    }

    static void cancel() {
        if (instance != null) {
            if (instance.gate.active(SystemClock.elapsedRealtime())) Log.i("Casualty", "Auto-play cancelled by launcher");
            instance.stop();
        }
    }

    private void beginCleanup(boolean forHome,CleanupCallback callback) {
        gate.cancel();
        handler.removeCallbacks(check);
        preparing=!forHome;
        returningHome=forHome;
        cleanupCallback=callback;
        expectedPackage=PlayerReset.PLAYER;
        stoppedObservations=0;
        cleanupLastAction=0;
        handler.removeCallbacks(cleanupPoll);
        handler.removeCallbacks(cleanupTimeout);
        try {
            Intent appInfo=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:"+PlayerReset.PLAYER))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(appInfo);
            report(forHome ? "Stopping iPlayer before Home" : "Opening iPlayer App info for a clean start");
            handler.postDelayed(cleanupPoll,200);
            handler.postDelayed(cleanupTimeout,12000);
        } catch(RuntimeException unavailable) {
            finishCleanup(false,"Android TV could not open BBC iPlayer App info.");
        }
    }

    private void clearCleanup() {
        handler.removeCallbacks(cleanupPoll);
        handler.removeCallbacks(cleanupTimeout);
        preparing=false;
        returningHome=false;
        cleanupCallback=null;
        stoppedObservations=0;
        cleanupLastAction=0;
    }

    private void finishCleanup(boolean ok,String error) {
        if(!(preparing || returningHome)) return;
        boolean wasPreparing=preparing;
        boolean wasReturning=returningHome;
        CleanupCallback callback=cleanupCallback;
        clearCleanup();
        if(!ok) {
            if(wasPreparing) {
                if(callback!=null) callback.done(false,error);
            } else if(wasReturning) {
                Toast.makeText(this,error,Toast.LENGTH_LONG).show();
                goHome(false);
            }
            return;
        }
        if(wasPreparing) {
            performGlobalAction(GLOBAL_ACTION_BACK);
            handler.postDelayed(() -> {
                lastState=null;
                gate.arm(SystemClock.elapsedRealtime());
                handler.postDelayed(check,500);
                report("Clean iPlayer start verified; macro armed");
                if(callback!=null) callback.done(true,"");
            },250);
        } else if(wasReturning) {
            goHome(true);
        }
    }

    private void stop() {
        gate.cancel();
        handler.removeCallbacks(check);
        clearCleanup();
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
            if(event!=null && event.getPackageName()!=null) {
                String pkg=event.getPackageName().toString();
                if(SETTINGS_PACKAGE.equals(pkg)) inspectCleanup();
                else if(!pkg.equals(expectedPackage) && !pkg.equals(getPackageName())
                        && !pkg.equals("android") && !pkg.equals("com.android.systemui")
                        && !isConfiguredHome(pkg)) {
                    finishCleanup(false,"iPlayer cleanup was interrupted by another app.");
                }
            }
            return;
        }
        if (!gate.active(SystemClock.elapsedRealtime())) return;
        if (event != null && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                && event.getPackageName() != null) {
            gate.observePackage(event.getPackageName().toString(), expectedPackage, getPackageName());
            if (!gate.active(SystemClock.elapsedRealtime())) { stop(); return; }
        }
        inspect();
    }

    private void inspectCleanup() {
        if(!(preparing || returningHome)) return;
        AccessibilityNodeInfo root=getRootInActiveWindow();
        if(root==null) return;
        try {
            if(!SETTINGS_PACKAGE.equals(String.valueOf(root.getPackageName()))) return;
            CleanupPage page=new CleanupPage();
            try {
                scanCleanup(root,page,0);
                long now=SystemClock.elapsedRealtime();
                if(page.breadcrumb && page.confirm) {
                    stoppedObservations=0;
                    if(page.ok!=null && now-cleanupLastAction>=400) {
                        cleanupLastAction=now;
                        boolean clicked=page.ok.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        report(clicked ? "Confirmed iPlayer Force stop" : "Waiting to confirm iPlayer Force stop");
                    }
                    return;
                }
                if(page.appInfo && page.open && page.uninstall) {
                    if(page.forceStop!=null) {
                        stoppedObservations=0;
                        if(now-cleanupLastAction>=400) {
                            cleanupLastAction=now;
                            boolean clicked=page.forceStop.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            report(clicked ? "Requested iPlayer Force stop" : "Waiting for iPlayer Force stop control");
                        }
                    } else {
                        stoppedObservations++;
                        report("Verifying iPlayer Force stop");
                        if(stoppedObservations>=2) finishCleanup(true,"");
                    }
                } else stoppedObservations=0;
            } finally {
                if(page.forceStop!=null) page.forceStop.recycle();
                if(page.ok!=null) page.ok.recycle();
            }
        } finally { root.recycle(); }
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
        if (root == null) { report("Waiting for accessible window"); return; }
        try {
            String pkg = String.valueOf(root.getPackageName());
            gate.observePackage(pkg, expectedPackage, getPackageName());
            if (!pkg.equals(expectedPackage)) {
                if (!gate.active(SystemClock.elapsedRealtime())) { Log.i("Casualty", "Auto-play cancelled on foreground change: " + pkg); stop(); }
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
                        beginCleanup(true,null);
                    } else report(page.title ? "Waiting for programme return after playback" : "Playback active; Home return armed");
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
        } finally { root.recycle(); }
    }
}
