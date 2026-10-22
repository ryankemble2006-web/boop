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
    private static final long DOUBLE_TAP_TIMEOUT_MS = 280L;

    private long lastLaunchElapsed;
    private final Handler keyHandler = new Handler(Looper.getMainLooper());
    private BoopKeyMappingStore mappingStore;

    private int pendingKeyCode = -1;
    private boolean longPressTriggered = false;
    private Runnable pendingLongPress = null;

    private int lastTapCode = -1;
    private long lastTapUpElapsed = 0L;
    private Runnable pendingSingleTap = null;

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

        try {
            ShieldBrightnessController.get(this).attachService(this);
            ShieldBrightnessController.get(this).init();
        } catch (Throwable ignored) {}

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

        int keyCode = event.getKeyCode();
        int action = event.getAction();

        ShieldBrightnessController bc = ShieldBrightnessController.get(this);
        if (bc.isHudVisible()) {
            if (action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    bc.adjust(-5);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    bc.adjust(+5);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                        || keyCode == KeyEvent.KEYCODE_ENTER
                        || keyCode == KeyEvent.KEYCODE_BACK
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP
                        || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    bc.hide();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_HOME) {
                    bc.hide();
                    // Fall through to Home handler
                }
            } else if (action == KeyEvent.ACTION_UP) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                        || keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER
                        || keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_DPAD_UP
                        || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    return true;
                }
            }
        }

        if (mappingStore == null) {
            mappingStore = new BoopKeyMappingStore(this);
        }

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

                // If neither single, double, nor long press is configured, and not HOME, pass through
                if (BoopKeyMappingStore.ACTION_DEFAULT.equals(entry.singleTapAction)
                        && BoopKeyMappingStore.ACTION_DEFAULT.equals(entry.doubleTapAction)
                        && BoopKeyMappingStore.ACTION_DEFAULT.equals(entry.longPressAction)
                        && keyCode != KeyEvent.KEYCODE_HOME) {
                    return super.onKeyEvent(event);
                }

                long now = SystemClock.elapsedRealtime();
                if (pendingSingleTap != null) {
                    if (keyCode == lastTapCode && (now - lastTapUpElapsed < DOUBLE_TAP_TIMEOUT_MS)) {
                        // Start of potential double tap: suppress the pending single tap
                        keyHandler.removeCallbacks(pendingSingleTap);
                        pendingSingleTap = null;
                    } else {
                        // Different key or timed out: flush previous single tap immediately
                        keyHandler.removeCallbacks(pendingSingleTap);
                        Runnable st = pendingSingleTap;
                        pendingSingleTap = null;
                        st.run();
                    }
                }

                pendingKeyCode = keyCode;
                longPressTriggered = false;

                if (!BoopKeyMappingStore.ACTION_DEFAULT.equals(entry.longPressAction)) {
                    final String lpAction = entry.longPressAction;
                    final String targetComp = entry.targetComponent;
                    pendingLongPress = () -> {
                        longPressTriggered = true;
                        lastTapCode = -1; // holding key cancels double-tap sequence
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
                    lastTapCode = -1;
                    return true; // Already executed long-press action
                }

                BoopKeyMappingStore.KeyEntry entry = mappingStore.getEntry(code);
                if (entry == null) {
                    return false;
                }

                boolean hasDoubleTap = !BoopKeyMappingStore.ACTION_DEFAULT.equals(entry.doubleTapAction)
                        || code == KeyEvent.KEYCODE_HOME;

                if (hasDoubleTap) {
                    long now = SystemClock.elapsedRealtime();
                    if (code == lastTapCode && (now - lastTapUpElapsed < DOUBLE_TAP_TIMEOUT_MS)) {
                        // DOUBLE TAP DETECTED!
                        lastTapCode = -1;
                        lastTapUpElapsed = 0L;
                        if (pendingSingleTap != null) {
                            keyHandler.removeCallbacks(pendingSingleTap);
                            pendingSingleTap = null;
                        }

                        Log.i(TAG, "Hardware key " + code + " double-tap triggered");
                        if (code == KeyEvent.KEYCODE_HOME &&
                                (BoopKeyMappingStore.ACTION_DEFAULT.equals(entry.doubleTapAction)
                                        || BoopKeyMappingStore.ACTION_RECENTS.equals(entry.doubleTapAction))) {
                            if (!performGlobalAction(GLOBAL_ACTION_RECENTS)) {
                                BoopKeyActionExecutor.execute(this, BoopKeyMappingStore.ACTION_RECENTS, null);
                            }
                        } else {
                            BoopKeyActionExecutor.execute(this, entry.doubleTapAction, entry.targetComponent);
                        }
                        return true;
                    } else {
                        // FIRST TAP OF POTENTIAL DOUBLE TAP: schedule single tap after timeout
                        lastTapCode = code;
                        lastTapUpElapsed = now;
                        final String singleAction = entry.singleTapAction;
                        final String comp = entry.targetComponent;
                        pendingSingleTap = () -> {
                            lastTapCode = -1;
                            pendingSingleTap = null;
                            if (code == KeyEvent.KEYCODE_HOME && BoopKeyMappingStore.ACTION_DEFAULT.equals(singleAction)) {
                                performGlobalAction(GLOBAL_ACTION_HOME);
                            } else {
                                Log.i(TAG, "Hardware key " + code + " single-tap: " + singleAction);
                                BoopKeyActionExecutor.execute(this, singleAction, comp);
                            }
                        };
                        keyHandler.postDelayed(pendingSingleTap, DOUBLE_TAP_TIMEOUT_MS);
                        return true;
                    }
                } else {
                    // No double tap configured for this key: immediate execution
                    lastTapCode = -1;
                    if (code == KeyEvent.KEYCODE_HOME && BoopKeyMappingStore.ACTION_DEFAULT.equals(entry.singleTapAction)) {
                        performGlobalAction(GLOBAL_ACTION_HOME);
                    } else if (!BoopKeyMappingStore.ACTION_DEFAULT.equals(entry.singleTapAction)) {
                        Log.i(TAG, "Hardware key " + code + " single-tap: " + entry.singleTapAction);
                        BoopKeyActionExecutor.execute(this, entry.singleTapAction, entry.targetComponent);
                    }
                    return true;
                }
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
            if (resId != null && resId.toString().contains("dismiss_icon")) {
                // User clicked dismiss in RecentsTvActivity
                AccessibilityNodeInfo parent = source.getParent();
                String appTitle = null;
                while (parent != null) {
                    List<AccessibilityNodeInfo> titles = parent.findAccessibilityNodeInfosByViewId("com.android.systemui:id/card_title_text");
                    if (titles != null && !titles.isEmpty()) {
                        CharSequence txt = titles.get(0).getText();
                        if (txt != null) {
                            appTitle = txt.toString();
                            break;
                        }
                    }
                    parent = parent.getParent();
                }
                if (appTitle != null) {
                    Log.i(TAG, "Recents dismiss clicked for title: " + appTitle);
                    BoopTaskKiller.forceStopByTitle(this, appTitle);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error handling view click", e);
        }
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
        if (keyHandler != null) {
            keyHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onDestroy() {
        if (keyHandler != null) {
            keyHandler.removeCallbacksAndMessages(null);
        }
        try {
            ShieldBrightnessController.get(this).detachService();
        } catch (Throwable ignored) {}
        super.onDestroy();
    }
}
