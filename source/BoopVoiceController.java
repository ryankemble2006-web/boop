package com.boop.alpha1;

import android.content.Context;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class BoopVoiceController {
    private static final String PREFS_NAME = "boop_voice";
    private static final String KEY_VOICE_NAME = "voice_name";
    private static final String KEY_PITCH = "pitch";
    private static final String KEY_SPEECH_RATE = "speech_rate";

    private final SharedPreferences preferences;
    private final List<Voice> localEnglishVoices = new ArrayList<>();

    private TextToSpeech tts;
    private int currentVoiceIndex = -1;
    private float currentPitch;
    private float currentSpeechRate;

    BoopVoiceController(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentPitch = preferences.getFloat(KEY_PITCH, BoopVoiceTuning.DEFAULT_PITCH);
        currentSpeechRate = preferences.getFloat(KEY_SPEECH_RATE, BoopVoiceTuning.DEFAULT_RATE);
    }

    void initialize(TextToSpeech tts, Locale preferredLocale) {
        this.tts = tts;
        applyPuppetCadence();
        refreshVoices(preferredLocale == null ? Locale.ENGLISH : preferredLocale);

        if (localEnglishVoices.isEmpty()) {
            return;
        }

        String savedName = preferences.getString(KEY_VOICE_NAME, null);
        int savedIndex = indexOfVoice(savedName);
        int targetIndex = savedIndex >= 0 ? savedIndex : 0;
        if (applyVoice(targetIndex) && savedIndex < 0) {
            saveCurrentVoice();
        }
    }

    String maybeChangeVoice(String text) {
        if (!BoopVoiceIntent.matches(text)) {
            return null;
        }

        if (tts == null || localEnglishVoices.size() <= 1) {
            return "I've only got this one.";
        }

        int start = currentVoiceIndex >= 0 ? currentVoiceIndex : 0;
        for (int offset = 1; offset <= localEnglishVoices.size(); offset++) {
            int next = (start + offset) % localEnglishVoices.size();
            if (next == currentVoiceIndex) {
                continue;
            }
            if (applyVoice(next)) {
                saveCurrentVoice();
                return "This one?";
            }
        }

        return "I've only got this one.";
    }

    void setPitch(float pitch) {
        currentPitch = BoopVoiceTuning.clampPitch(pitch);
        preferences.edit().putFloat(KEY_PITCH, currentPitch).apply();
        if (tts != null) {
            tts.setPitch(currentPitch);
        }
    }

    void setSpeechRate(float speechRate) {
        currentSpeechRate = BoopVoiceTuning.clampRate(speechRate);
        preferences.edit().putFloat(KEY_SPEECH_RATE, currentSpeechRate).apply();
        if (tts != null) {
            tts.setSpeechRate(currentSpeechRate);
        }
    }

    float pitch() {
        return currentPitch;
    }

    float speechRate() {
        return currentSpeechRate;
    }

    private void refreshVoices(Locale preferredLocale) {
        localEnglishVoices.clear();
        currentVoiceIndex = -1;

        Set<Voice> voices = tts == null ? null : tts.getVoices();
        if (voices == null) {
            return;
        }

        for (Voice voice : voices) {
            Locale locale = voice.getLocale();
            if (locale == null || !"en".equalsIgnoreCase(locale.getLanguage())) {
                continue;
            }
            if (voice.isNetworkConnectionRequired()) {
                continue;
            }
            localEnglishVoices.add(voice);
        }

        localEnglishVoices.sort(
                Comparator.comparingInt((Voice voice) -> localeScore(voice.getLocale(), preferredLocale))
                        .reversed()
                        .thenComparing(Comparator.comparingInt(Voice::getQuality).reversed())
                        .thenComparing(Voice::getName));
    }

    private static int localeScore(Locale voiceLocale, Locale preferredLocale) {
        if (voiceLocale == null || preferredLocale == null) {
            return 0;
        }
        if (voiceLocale.equals(preferredLocale)) {
            return 3;
        }
        if (voiceLocale.getCountry().equalsIgnoreCase(preferredLocale.getCountry())
                && !voiceLocale.getCountry().isEmpty()) {
            return 2;
        }
        return "en".equalsIgnoreCase(voiceLocale.getLanguage()) ? 1 : 0;
    }

    private int indexOfVoice(String voiceName) {
        if (voiceName == null || voiceName.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < localEnglishVoices.size(); i++) {
            if (voiceName.equals(localEnglishVoices.get(i).getName())) {
                return i;
            }
        }
        return -1;
    }

    private boolean applyVoice(int index) {
        if (tts == null || index < 0 || index >= localEnglishVoices.size()) {
            return false;
        }
        Voice voice = localEnglishVoices.get(index);
        if (tts.setVoice(voice) != TextToSpeech.SUCCESS) {
            return false;
        }
        currentVoiceIndex = index;
        applyPuppetCadence();
        return true;
    }

    private void saveCurrentVoice() {
        if (currentVoiceIndex < 0 || currentVoiceIndex >= localEnglishVoices.size()) {
            return;
        }
        preferences.edit()
                .putString(KEY_VOICE_NAME, localEnglishVoices.get(currentVoiceIndex).getName())
                .apply();
    }

    private void applyPuppetCadence() {
        if (tts == null) {
            return;
        }
        tts.setPitch(currentPitch);
        tts.setSpeechRate(currentSpeechRate);
    }
}
