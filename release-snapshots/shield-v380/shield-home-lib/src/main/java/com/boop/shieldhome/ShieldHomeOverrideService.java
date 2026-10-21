package com.boop.shieldhome;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

/**
 * Native Shield remote hardware key interception and task killer.
 * Intercepts mapped hardware remote keys natively without third-party tools,
 * provides fast force-stop on Android TV task switcher dismissals, and maintains
 * BOOP as the foreground TV launcher.
 */
public class ShieldHomeOverrideService extends AccessibilityService {
    private static final String TAG = "BOOPHomeOverride";
    private static final long RELAUNCH_GUARD_MS = 350L;
    private static final long LONG_PRESS_MS = 400L;

    private long lastLaunchElapsed;
    private final Handler keyHandler = new Handler(Looper.getMainLooper());
    private BoopKeyMappingStore mappingStore;

    private int pendingKeyCode = -1;
    private boolean longPressTriggered = false;
    private Runnable pendingLongPress = null;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        mappingStore = new BoopKeyMappingStore(this);

        AccessibilityServiceInfo info = getServiceInfo();
        if (info != null) {
            info.eventTypes |= AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_VIEW_CLICKED;
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
            info.flags |= AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
                    | AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
            info.notificationTimeout = 0;
            setServiceInfo(info);
        }

        if (!homeProfileEnabled()) return;

        int boot = Settings.Global.getInt(getContentResolver(), Settings.Global.BOOT_COUNT, -1);
        SharedPreferences prefs = getSharedPreferences("home-rearm", MODE_PRIVATE);
        if (HomeOverridePolicy.shouldRearmOnConnect(homeProfileEnabled(), boot, prefs.getInt("boot", -1))) {
            prefs.edit().putInt("boot", boot).apply();
            lastLaunchElapsed = SystemClock.elapsedRealtime();
            bringBoopToFront();
        }
    }

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        if (!homeProfileEnabled() || event == null) {
            return super.onKeyEvent(event);
        }

        if (mappingStore == null) {
            mappingStore = new BoopKeyMappingStore(this);
        }

        int keyCode = event.getKeyCode();
        int action = event.getAction();

        if (action == KeyEvent.ACTION_DOWN) {
            if (event.getRepeatCount() == 0) {
                if (pendingLongPress != null) {
                    keyHandler.removeCallbacks(pendingLongPress);
                    pendingLongPress = null;
                }

                BoopKeyMappingStore.KeyEntry entry = mappingStore.getEntry(keyCode);
                if (entry == null) {
                    return super.onKeyEvent(event);
                }

                // If neither single tap nor long press is configured, and not HOME, pass through
                if (BoopKeyMappingStore.ACTION_DEFAULT.equals(entry.singleTapAction)
                        && BoopKeyMappingStore.ACTION_DEFAULT.equals(entry.longPressAction)
                        && keyCode != KeyEvent.KEYCODE_HOME) {
                    return super.onKeyEvent(event);
                }

                pendingKeyCode = keyCode;
                longPressTriggered = false;

                if (!BoopKeyMappingStore.ACTION_DEFAULT.equals(entry.longPressAction)) {
                    final String lpAction = entry.longPressAction;
                    final String targetComp = entry.targetComponent;
                    pendingLongPress = () -> {
                        longPressTriggered = true;
                        Log.i(TAG, "Hardware key " + keyCode + " long-press triggered: " + lpAction);
                        BoopKeyActionExecutor.execute(this, lpAction, targetComp);
                    };
                    keyHandler.postDelayed(pendingLongPress, LONG_PRESS_MS);
                }
                return true;
            } else {
                // Key held down, repeat events arriving
                if (pendingKeyCode == keyCode) {
                    return true;
                }
                return super.onKeyEvent(event);
            }
        } else if (action == KeyEvent.ACTION_UP) {
            if (pendingKeyCode == keyCode) {
                if (pendingLongPress != null) {
                    keyHandler.removeCallbacks(pendingLongPress);
                    pendingLongPress = null;
                }

                int code = pendingKeyCode;
                boolean wasLong = longPressTriggered;
                pendingKeyCode = -1;
                longPressTriggered = false;

                if (wasLong) {
                    return true; // Already executed long-press action
                }

                BoopKeyMappingStore.KeyEntry entry = mappingStore.getEntry(code);
                if (entry != null) {
                    if (!BoopKeyMappingStore.ACTION_DEFAULT.equals(entry.singleTapAction)) {
                        Log.i(TAG, "Hardware key " + code + " single-tap: " + entry.singleTapAction);
                        BoopKeyActionExecutor.execute(this, entry.singleTapAction, entry.targetComponent);
                        return true;
                    } else if (code == KeyEvent.KEYCODE_HOME) {
                        performGlobalAction(GLOBAL_ACTION_HOME);
                        return true;
                    }
                }
                return false;
            }
        }

        return super.onKeyEvent(event);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!homeProfileEnabled() || event == null) return;

        int type = event.getEventType();
        if (type == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            handleViewClicked(event);
            return;
        }

        if (type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            CharSequence packageName = event.getPackageName();
            String foregroundPackage = packageName == null ? null : packageName.toString();
            if (!HomeOverridePolicy.shouldReplaceForeground(homeProfileEnabled(), foregroundPackage, getPackageName())) {
                return;
            }

            long now = SystemClock.elapsedRealtime();
            if (now - lastLaunchElapsed < RELAUNCH_GUARD_MS) {
                return;
            }
            lastLaunchElapsed = now;
            bringBoopToFront();
        }
    }

    private void handleViewClicked(AccessibilityEvent event) {
        AccessibilityNodeInfo source = event.getSource();
        if (source == null) return;
        try {
            CharSequence resId = source.getViewIdResourceName();
            if (resId != null && "com.android.systemui:id/dismiss_icon".contentEquals(resId)) {
                String title = extractAppTitleFromDismissNode(source);
                if (title != null && !title.trim().isEmpty()) {
                    Log.i(TAG, "Recents dismiss clicked for: " + title);
                    BoopTaskKiller.forceStopByTitle(this, title.trim());
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error handling click event", e);
        } finally {
            source.recycle();
        }
    }

    private String extractAppTitleFromDismissNode(AccessibilityNodeInfo dismissNode) {
        if (dismissNode == null) return null;
        AccessibilityNodeInfo current = dismissNode.getParent();
        for (int depth = 0; depth < 3 && current != null; depth++) {
            CharSequence desc = current.getContentDescription();
            if (desc != null && desc.length() > 0) {
                String text = desc.toString().trim();
                current.recycle();
                return text;
            }
            List<AccessibilityNodeInfo> titles = current.findAccessibilityNodeInfosByViewId("com.android.systemui:id/card_title_text");
            if (titles != null && !titles.isEmpty()) {
                try {
                    CharSequence title = titles.get(0).getText();
                    if (title != null && title.length() > 0) {
                        return title.toString().trim();
                    }
                } finally {
                    for (AccessibilityNodeInfo n : titles) n.recycle();
                }
            }
            AccessibilityNodeInfo next = current.getParent();
            current.recycle();
            current = next;
        }
        return null;
    }

    private void bringBoopToFront() {
        if (!homeProfileEnabled()) return;
        Intent intent = homeIntent()
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException ignored) {
            // Fail closed. Android TV Home remains available as the recovery launcher.
        }
    }

    /** Unified supplies its current profile authority; standalone behavior is preserved. */
    protected boolean homeProfileEnabled() { return true; }

    protected Intent homeIntent() { return new Intent(this, ShieldLauncherActivity.class); }

    @Override
    public void onInterrupt() {
        if (pendingLongPress != null) {
            keyHandler.removeCallbacks(pendingLongPress);
            pendingLongPress = null;
        }
        pendingKeyCode = -1;
        longPressTriggered = false;
    }
}
