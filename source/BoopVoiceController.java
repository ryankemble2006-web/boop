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
    static final String BACKEND_ANDROID = "android";
    static final String BACKEND_NATURAL = "natural";
    static final String NATURAL_STATE_VERIFIED = "verified";

    static final class NaturalVoice {
        private final String name;
        private final String key;
        private final int sid;

        NaturalVoice(String name, String key, int sid) {
            this.name = name;
            this.key = key;
            this.sid = sid;
        }

        String name() { return name; }
        String key() { return key; }
        int sid() { return sid; }
    }

    private static final String PREFS_NAME = "boop_voice";
    private static final String KEY_VOICE_NAME = "voice_name";
    private static final String KEY_PITCH = "pitch";
    private static final String KEY_SPEECH_RATE = "speech_rate";
    private static final String KEY_SELECTED_BACKEND = "selected_backend";
    private static final String KEY_NATURAL_SPEAKER_KEY = "natural_speaker_key";
    private static final String KEY_NATURAL_PACK_VERSION = "natural_pack_version";
    private static final String KEY_NATURAL_VERIFICATION_STATE = "natural_verification_state";
    private static final String KEY_NATURAL_RUNTIME_PROVEN_VERSION = "natural_runtime_proven_version";

    private static final NaturalVoice[] NATURAL_VOICES = {
            new NaturalVoice("Emma", "bf_emma", 21),
            new NaturalVoice("Isabella", "bf_isabella", 22),
            new NaturalVoice("George", "bm_george", 26),
            new NaturalVoice("Fable", "bm_fable", 25)
    };

    private final SharedPreferences preferences;
    private final List<Voice> localEnglishVoices = new ArrayList<>();

    private TextToSpeech tts;
    private int currentVoiceIndex = -1;
    private float currentPitch;
    private float currentSpeechRate;
    private String selectedBackend;
    private String naturalSpeakerKey;
    private String naturalPackVersion;
    private String naturalVerificationState;
    private String naturalRuntimeProvenVersion;
    private boolean naturalPackUsable;

    BoopVoiceController(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentPitch = preferences.getFloat(KEY_PITCH, BoopVoiceTuning.DEFAULT_PITCH);
        currentSpeechRate = preferences.getFloat(KEY_SPEECH_RATE, BoopVoiceTuning.DEFAULT_RATE);
        selectedBackend = preferences.getString(KEY_SELECTED_BACKEND, BACKEND_ANDROID);
        naturalSpeakerKey = preferences.getString(KEY_NATURAL_SPEAKER_KEY, NATURAL_VOICES[0].key());
        naturalPackVersion = preferences.getString(KEY_NATURAL_PACK_VERSION, "");
        naturalVerificationState = preferences.getString(KEY_NATURAL_VERIFICATION_STATE, "");
        naturalRuntimeProvenVersion = preferences.getString(KEY_NATURAL_RUNTIME_PROVEN_VERSION, "");
        if (!BACKEND_NATURAL.equals(selectedBackend)) selectedBackend = BACKEND_ANDROID;
        if (findNaturalVoice(naturalSpeakerKey) == null) naturalSpeakerKey = NATURAL_VOICES[0].key();
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

        if (naturalBackendSelectedAndUsable()) {
            int current = indexOfNaturalVoice(naturalSpeakerKey);
            int next = (current < 0 ? 0 : (current + 1) % NATURAL_VOICES.length);
            selectNaturalVoice(NATURAL_VOICES[next].key());
            return "This one?";
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

    void setNaturalPackUsable(boolean usable) {
        naturalPackUsable = usable;
    }

    void onNaturalPackVerified(String version) {
        naturalPackVersion = version == null ? "" : version;
        naturalVerificationState = NATURAL_STATE_VERIFIED;
        naturalPackUsable = !naturalPackVersion.isEmpty();
        preferences.edit()
                .putString(KEY_NATURAL_PACK_VERSION, naturalPackVersion)
                .putString(KEY_NATURAL_VERIFICATION_STATE, naturalVerificationState)
                .apply();
    }

    boolean naturalPackReadyForPreview() {
        return naturalPackUsable
                && NATURAL_STATE_VERIFIED.equals(naturalVerificationState)
                && naturalPackVersion != null
                && !naturalPackVersion.isEmpty();
    }

    void markNaturalPlaybackProven() {
        if (!naturalPackReadyForPreview()) return;
        naturalRuntimeProvenVersion = naturalPackVersion;
        preferences.edit()
                .putString(KEY_NATURAL_RUNTIME_PROVEN_VERSION, naturalRuntimeProvenVersion)
                .apply();
    }

    boolean naturalBackendSelectedAndUsable() {
        return BACKEND_NATURAL.equals(selectedBackend)
                && naturalPackReadyForPreview()
                && naturalRuntimeProvenVersion != null
                && naturalPackVersion.equals(naturalRuntimeProvenVersion);
    }

    boolean selectNaturalVoice(String key) {
        NaturalVoice voice = findNaturalVoice(key);
        if (voice == null || !naturalPackReadyForPreview()) return false;
        selectedBackend = BACKEND_NATURAL;
        naturalSpeakerKey = voice.key();
        preferences.edit()
                .putString(KEY_SELECTED_BACKEND, selectedBackend)
                .putString(KEY_NATURAL_SPEAKER_KEY, naturalSpeakerKey)
                .apply();
        return true;
    }

    NaturalVoice selectedNaturalVoice() {
        NaturalVoice voice = findNaturalVoice(naturalSpeakerKey);
        return voice == null ? NATURAL_VOICES[0] : voice;
    }

    String naturalPackVersion() {
        return naturalPackVersion;
    }

    static NaturalVoice[] naturalVoices() {
        return NATURAL_VOICES.clone();
    }

    static NaturalVoice findNaturalVoice(String key) {
        if (key == null) return null;
        for (NaturalVoice voice : NATURAL_VOICES) {
            if (key.equals(voice.key())) return voice;
        }
        return null;
    }

    private static int indexOfNaturalVoice(String key) {
        for (int i = 0; i < NATURAL_VOICES.length; i++) {
            if (NATURAL_VOICES[i].key().equals(key)) return i;
        }
        return -1;
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
