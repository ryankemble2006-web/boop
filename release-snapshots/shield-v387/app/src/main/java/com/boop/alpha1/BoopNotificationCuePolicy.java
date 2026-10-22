package com.boop.alpha1;

final class BoopNotificationCuePolicy {
    private BoopNotificationCuePolicy() { }

    static boolean shouldPlay(
            boolean coordinatorRequestsCue,
            BoopNotificationChannelInfo channel) {
        return coordinatorRequestsCue
                && channel != null
                && channel.nativeEffectsSilent();
    }
}
