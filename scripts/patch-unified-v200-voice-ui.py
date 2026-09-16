#!/usr/bin/env python3
from pathlib import Path

MAIN = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java")
MARKER = "// BOOP_V200_VOICE_UI"


def once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected one anchor, found {count}")
    return text.replace(old, new, 1)


text = MAIN.read_text(encoding="utf-8")
if MARKER in text:
    print("v200 Voice Settings UI already materialized")
    raise SystemExit(0)

text = once(
    text,
    "        wakeFaceForInteraction();\n        voiceSettingsOpen = true;",
    "        if (face != null) face.setVisibility(View.INVISIBLE);\n        voiceSettingsOpen = true;",
    "hide full-screen face when Voice Settings opens",
)
text = once(
    text,
    "        voiceSettingsOverlay.setBackgroundColor(Color.argb(236, 0, 0, 0));",
    "        voiceSettingsOverlay.setBackgroundColor(Color.BLACK);",
    "opaque Voice Settings background",
)
text = once(
    text,
    """        voiceSettingsOverlay.addView(cadenceSlider, cadenceParams);

        addNaturalVoiceSettings();
""",
    """        voiceSettingsOverlay.addView(cadenceSlider, cadenceParams);

        Button testVoice = new Button(this);
        testVoice.setText("TEST VOICE");
        testVoice.setTextSize(19f);
        testVoice.setTextColor(Color.WHITE);
        testVoice.setBackgroundColor(Color.rgb(42, 42, 42));
        testVoice.setContentDescription("Test current BOOP voice");
        testVoice.setOnClickListener(v -> testCurrentVoice());
        LinearLayout.LayoutParams testVoiceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(60));
        testVoiceParams.setMargins(0, dp(6), 0, dp(12));
        voiceSettingsOverlay.addView(testVoice, testVoiceParams);

        addNaturalVoiceSettings();
""",
    "TEST VOICE placement",
)

helper = r'''    private void testCurrentVoice() {
        final String phrase = "This is how BOOP sounds.";
        if (wakeCoordinator != null) {
            wakeCoordinator.onTtsStarting();
        }
        if (voiceController != null
                && voiceController.naturalBackendSelectedAndUsable()
                && naturalSpeechBackend != null) {
            BoopVoiceController.NaturalVoice voice = voiceController.selectedNaturalVoice();
            if (voice != null) {
                boolean started = naturalSpeechBackend.speak(
                        phrase,
                        voice.sid(),
                        voiceController.pitch(),
                        voiceController.speechRate(),
                        new BoopSpeechBackend.Callback() {
                            @Override public void onDone() {
                                runOnUiThread(() -> finishTtsUtterance());
                            }

                            @Override public void onError(Throwable error) {
                                android.util.Log.w(
                                        "BOOP-NaturalVoice",
                                        "Natural voice demo failed; using Android TTS",
                                        error);
                                runOnUiThread(() -> speakWithAndroidTts(phrase));
                            }
                        });
                if (started) return;
            }
        }
        speakWithAndroidTts(phrase);
    }

'''
text = once(
    text,
    "    private void addNaturalVoiceSettings() {\n",
    helper + "    private void addNaturalVoiceSettings() {\n",
    "TEST VOICE helper",
)
text = once(
    text,
    """    private void previewNaturalVoice(String key, String text, TextView naturalStatus, Button button) {
        wakeFaceForInteraction();
        if (wakeCoordinator != null) {
""",
    """    private void previewNaturalVoice(String key, String text, TextView naturalStatus, Button button) {
        // v200 remove preview face wake: Voice Settings owns the whole screen.
        if (wakeCoordinator != null) {
""",
    "remove preview face wake",
)
text = once(
    text,
    """        voiceSettingsOverlay = null;
        voiceSettingsOpen = false;
""",
    """        voiceSettingsOverlay = null;
        if (face != null) {
            face.setVisibility(View.VISIBLE);
            face.showIdleBlackImmediately();
        }
        voiceSettingsOpen = false;
""",
    "restore face after Voice Settings",
)

text += "\n" + MARKER + "\n"
MAIN.write_text(text, encoding="utf-8")
print("v200 Voice Settings: opaque, face-hidden, TEST VOICE enabled")
