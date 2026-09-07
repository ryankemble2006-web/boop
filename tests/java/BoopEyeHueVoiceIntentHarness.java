package com.boop.alpha1;

public final class BoopEyeHueVoiceIntentHarness {
    private static void expect(boolean value, String label) {
        if (!value) throw new AssertionError(label);
    }

    private static void reject(boolean value, String label) {
        if (value) throw new AssertionError(label);
    }

    public static void main(String[] args) {
        expect(BoopEyeHueVoiceIntent.matches("change eye colour"), "basic UK phrase");
        expect(BoopEyeHueVoiceIntent.matches("change eye color"), "basic US phrase");
        expect(BoopEyeHueVoiceIntent.matches("BOOP, change eye colour"), "wake-word prefix");
        expect(BoopEyeHueVoiceIntent.matches("please adjust my eye colour"), "natural wording");
        expect(BoopEyeHueVoiceIntent.matches("set eyes color"), "plural eyes");
        expect(BoopEyeHueVoiceIntent.matches("change eye hue"), "hue wording");
        expect(BoopEyeHueVoiceIntent.matches("change i color"), "recognizer eye homophone");
        reject(BoopEyeHueVoiceIntent.matches("change voice colour"), "unrelated colour request");
        reject(BoopEyeHueVoiceIntent.matches("what colour are my eyes"), "ordinary question");
        reject(BoopEyeHueVoiceIntent.matches("turn the lights blue"), "house command");
        reject(BoopEyeHueVoiceIntent.matches(null), "null transcript");
    }
}
