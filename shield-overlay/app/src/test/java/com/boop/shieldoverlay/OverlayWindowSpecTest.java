package com.boop.shieldoverlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.view.WindowManager;
import org.junit.Test;

public final class OverlayWindowSpecTest {
    @Test public void usesApplicationOverlayAndNeverTakesInput() {
        assertEquals(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, OverlayWindowSpec.type());
        int flags = OverlayWindowSpec.flags();
        assertTrue((flags & WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) != 0);
        assertTrue((flags & WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) != 0);
    }
}
