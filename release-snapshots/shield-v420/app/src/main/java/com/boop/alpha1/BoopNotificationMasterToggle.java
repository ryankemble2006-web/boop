package com.boop.alpha1;

final class BoopNotificationMasterToggle {
    enum Action {
        DISABLE,
        ENABLE_NOW,
        REQUEST_LISTENER_ACCESS
    }

    private BoopNotificationMasterToggle() { }

    static Action action(boolean requestedEnabled, boolean listenerGranted) {
        if (!requestedEnabled) {
            return Action.DISABLE;
        }
        return listenerGranted ? Action.ENABLE_NOW : Action.REQUEST_LISTENER_ACCESS;
    }
}
