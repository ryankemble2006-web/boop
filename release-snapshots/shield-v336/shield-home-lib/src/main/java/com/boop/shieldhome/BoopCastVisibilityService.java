package com.boop.shieldhome;
import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
/** Window-package events only. No content retrieval, gestures, Home launch or playback control. */
public final class BoopCastVisibilityService extends AccessibilityService {
    @Override protected void onServiceConnected() { BoopMediaBridge.foreground(""); }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if(event==null || event.getEventType()!=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)return;
        CharSequence pkg=event.getPackageName();
        CharSequence name=event.getClassName();
        BoopMediaBridge.foreground(com.boop.shared.CastCornerPolicy.foreground(
            pkg==null ? "" : pkg.toString(),name==null ? "" : name.toString()));
    }
    @Override public void onInterrupt() { BoopMediaBridge.foreground(""); }
    @Override public boolean onUnbind(Intent intent) { BoopMediaBridge.foreground(""); return super.onUnbind(intent); }
    @Override public void onDestroy() { BoopMediaBridge.foreground(""); super.onDestroy(); }
}
