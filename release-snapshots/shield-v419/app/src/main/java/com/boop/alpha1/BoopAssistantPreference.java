package com.boop.alpha1;

import android.content.Context;
import android.content.SharedPreferences;

final class BoopAssistantPreference {
    private static final String PREFS = "boop_assistant";
    private static final String KEY_CHOICE = "shield_mic_button_choice_v1";

    private BoopAssistantPreference() { }

    static BoopAssistantIntegrationPolicy.Choice load(Context context) {
        String value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_CHOICE, null);
        if ("use_boop".equals(value)) return BoopAssistantIntegrationPolicy.Choice.USE_BOOP;
        if ("keep_current".equals(value)) return BoopAssistantIntegrationPolicy.Choice.KEEP_CURRENT;
        return null;
    }

    static void save(Context context, BoopAssistantIntegrationPolicy.Choice choice) {
        String value = choice == BoopAssistantIntegrationPolicy.Choice.USE_BOOP
                ? "use_boop" : "keep_current";
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_CHOICE, value).apply();
    }
}
