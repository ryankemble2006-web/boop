package com.boop.alpha1;

final class BoopNotificationPolicy {
    private BoopNotificationPolicy() { }

    static boolean allows(
            BoopNotificationSettingsState state,
            String packageName,
            String channelId) {
        return state != null
                && state.masterEnabled()
                && state.isAppEnabled(packageName)
                && state.isChannelEnabled(packageName, channelId);
    }
}
