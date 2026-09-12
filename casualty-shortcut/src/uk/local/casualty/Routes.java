package uk.local.casualty;

final class Routes {
    static final String WEB_URL = "https://www.bbc.co.uk/iplayer/episodes/b006m8wd/casualty";
    static final String TV_URL = "https://www.live.bbctvapps.co.uk/tap/telly/iplayer?deeplink=tv%2Fprogrammes%2Fb006m8wd";
    interface AppOpener { boolean open(String packageName, String url); }
    interface BrowserOpener { boolean open(); }
    static boolean open(AppOpener app, BrowserOpener browser) {
        if (app.open("com.nvidia.bbciplayer", TV_URL)) return true;
        if (app.open("bbc.iplayer.android", TV_URL)) return true;
        if (app.open("bbc.iplayer.android", WEB_URL)) return true;
        if (app.open("uk.co.bbc.iplayer", WEB_URL)) return true;
        return browser.open();
    }
}
