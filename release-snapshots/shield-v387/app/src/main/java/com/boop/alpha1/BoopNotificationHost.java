package com.boop.alpha1;

interface BoopNotificationHost {
    void show(BoopNotificationPresentation presentation, long timeoutMs);
    void update(BoopNotificationPresentation presentation, long timeoutMs);
    void hide();
}
