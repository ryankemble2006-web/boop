package com.boop.shieldhdrdebug;

import android.view.WindowManager;

final class OverlayWindowSpec {
    private OverlayWindowSpec() { }

    static int type() {
        return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
    }

    static int flags() {
        return WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
    }
}
