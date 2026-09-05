package com.boop.shieldoverlay;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class TvNavigationModelTest {
    @Test
    public void railOrderIsHomeRoutinesSettingsAndHomeIsDefault() {
        TvNavigationModel model = new TvNavigationModel();
        assertArrayEquals(
                new TvNavigationModel.Page[]{
                        TvNavigationModel.Page.HOME,
                        TvNavigationModel.Page.ROUTINES,
                        TvNavigationModel.Page.SETTINGS
                },
                model.railOrder());
        assertEquals(TvNavigationModel.Page.HOME, model.page());
        assertEquals(TvNavigationModel.Focus.RAIL, model.focus());
    }

    @Test
    public void enteringContentThenLeftReturnsToRailWithoutChangingPage() {
        TvNavigationModel model = new TvNavigationModel();
        model.selectRail(TvNavigationModel.Page.ROUTINES);
        model.enterContent();
        assertEquals(TvNavigationModel.Focus.CONTENT, model.focus());

        model.onContentLeft();

        assertEquals(TvNavigationModel.Focus.RAIL, model.focus());
        assertEquals(TvNavigationModel.Page.ROUTINES, model.page());
    }

    @Test
    public void backFromNestedPageReturnsHomeAndDoesNotFinish() {
        TvNavigationModel model = new TvNavigationModel();
        model.selectRail(TvNavigationModel.Page.SETTINGS);
        model.enterContent();

        assertFalse(model.onBack());
        assertEquals(TvNavigationModel.Page.HOME, model.page());
        assertEquals(TvNavigationModel.Focus.RAIL, model.focus());
    }

    @Test
    public void backFromHomeRootRequestsActivityFinish() {
        TvNavigationModel model = new TvNavigationModel();
        assertTrue(model.onBack());
    }
}
