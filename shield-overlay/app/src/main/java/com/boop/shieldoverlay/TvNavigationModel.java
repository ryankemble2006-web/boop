package com.boop.shieldoverlay;

public final class TvNavigationModel {
    public enum Page {
        HOME,
        ROUTINES,
        SETTINGS
    }

    public enum Focus {
        RAIL,
        CONTENT
    }

    private static final Page[] RAIL_ORDER = {
            Page.HOME,
            Page.ROUTINES,
            Page.SETTINGS
    };

    private Page page = Page.HOME;
    private Focus focus = Focus.RAIL;

    public Page[] railOrder() {
        return RAIL_ORDER.clone();
    }

    public Page page() {
        return page;
    }

    public Focus focus() {
        return focus;
    }

    public void selectRail(Page page) {
        if (page == null) {
            throw new IllegalArgumentException("page is required");
        }
        this.page = page;
        focus = Focus.RAIL;
    }

    public void enterContent() {
        focus = Focus.CONTENT;
    }

    public void onContentLeft() {
        focus = Focus.RAIL;
    }

    public boolean onBack() {
        if (page != Page.HOME || focus == Focus.CONTENT) {
            page = Page.HOME;
            focus = Focus.RAIL;
            return false;
        }
        return true;
    }
}
