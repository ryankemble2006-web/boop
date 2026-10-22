package com.boop.alpha1;

final class BoopAssistantLaunchPolicy {
    static boolean shouldLaunch(String action, boolean chosen, boolean roleHeld, boolean recreated) {
        return chosen && roleHeld && !recreated
                && ("android.intent.action.ASSIST".equals(action)
                    || "android.intent.action.VOICE_ASSIST".equals(action));
    }
}
