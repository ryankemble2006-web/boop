package com.boop.alpha1;

/** Decides when a changed custom wake name should flow straight into local enrolment. */
final class BoopWakeTrainingPolicy {
    private BoopWakeTrainingPolicy() { }

    static boolean shouldPromptAfterSettings(
            String previousName, String savedName, boolean matchingProfilePresent) {
        String before = BoopWakeName.normalize(previousName);
        String after = BoopWakeName.normalize(savedName);
        return !BoopWakeName.isDefault(after)
                && !before.equalsIgnoreCase(after)
                && !matchingProfilePresent;
    }
}
