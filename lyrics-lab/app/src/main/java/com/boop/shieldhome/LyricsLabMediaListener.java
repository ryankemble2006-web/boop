package com.boop.shieldhome;

import android.service.notification.NotificationListenerService;

/** Android's media-session access boundary. No notification bodies are read or stored. */
public final class LyricsLabMediaListener extends NotificationListenerService {
    @Override public void onListenerConnected() { LyricsLabSession.accessChanged(); }
    @Override public void onListenerDisconnected() { LyricsLabSession.accessChanged(); }
}
