package com.boop.alpha1;

final class BoopNotificationIntakePolicy {
    enum Mode {
        OBSERVE_CHANNEL_ONLY,
        READ_RICH_CONTENT
    }

    private BoopNotificationIntakePolicy() { }

    static Mode decide(
            BoopNotificationSettingsState state,
            String packageName,
            String channelId) {
        return BoopNotificationPolicy.allows(state, packageName, channelId)
                ? Mode.READ_RICH_CONTENT
                : Mode.OBSERVE_CHANNEL_ONLY;
    }
}
