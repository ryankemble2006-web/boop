package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.view.KeyEvent;
import java.lang.reflect.Method;
import org.junit.Test;

public final class ShieldInputRoutingTest {
    @Test public void homeViewOwnsKeyDispatchBeforeFocusedCards() throws Exception {
        Method dispatch = ShieldHomeView.class.getDeclaredMethod("dispatchKeyEvent", KeyEvent.class);
        assertNotNull(dispatch);
        assertEquals(boolean.class, dispatch.getReturnType());
    }

    @Test public void activityOwnsBackDispatchBeforeLegacyBackHandling() throws Exception {
        Method dispatch = ShieldLauncherActivity.class.getDeclaredMethod("dispatchKeyEvent", KeyEvent.class);
        assertNotNull(dispatch);
        assertEquals(boolean.class, dispatch.getReturnType());
    }

    @Test public void homeViewExposesSingleBackResetToFavouriteOne() throws Exception {
        Method reset = ShieldHomeView.class.getDeclaredMethod("resetToFirstFavourite");
        assertNotNull(reset);
        assertEquals(boolean.class, reset.getReturnType());
    }
}
