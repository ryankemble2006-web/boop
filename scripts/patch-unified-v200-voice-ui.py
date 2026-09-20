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

        TextView naturalStatus = addNaturalVoiceSettings();
        BoopVoiceSharingControls.install(this, voiceSettingsOverlay, voiceController,
                pitchSlider, cadenceSlider, naturalStatus);
""",
    "TEST VOICE placement",
)
text = once(
    text,
    "        voiceSettingsScroll.bringToFront();",
    """        voiceSettingsScroll.bringToFront();
        com.boop.shieldhome.BoopTvChrome.prepareVoiceSettings(
                voiceSettingsScroll, voiceSettingsOverlay, pitchSlider);""",
    "v202 TV child focus after scroll attachment",
)

helper = r'''    private void testCurrentVoice() {
        final int previewGeneration = ++naturalPreviewGeneration;
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
                            @Override public void onCancelled() { }
                            @Override public void onDone() {
                                runOnUiThread(() -> {
                                    if (previewGeneration == naturalPreviewGeneration && voiceSettingsOpen
                                            && activityInForeground && !isFinishing() && !isDestroyed()) finishTtsUtterance();
                                });
                            }

                            @Override public void onError(Throwable error) {
                                android.util.Log.w(
                                        "BOOP-NaturalVoice",
                                        "Natural voice demo failed; using Android TTS",
                                        error);
                                runOnUiThread(() -> {
                                    if (previewGeneration == naturalPreviewGeneration && voiceSettingsOpen
                                            && activityInForeground && !isFinishing() && !isDestroyed()) speakWithAndroidTts(phrase);
                                });
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
    "    private TextView addNaturalVoiceSettings() {\n",
    helper + "    private TextView addNaturalVoiceSettings() {\n",
    "TEST VOICE helper",
)
text = once(
    text,
    """    private void previewNaturalVoice(String key, String text, TextView naturalStatus, Button button) {
        wakeFaceForInteraction();
        final int previewGeneration = ++naturalPreviewGeneration;
        if (wakeCoordinator != null) {
""",
    """    private void previewNaturalVoice(String key, String text, TextView naturalStatus, Button button) {
        // v200 remove preview face wake: Voice Settings owns the whole screen.
        final int previewGeneration = ++naturalPreviewGeneration;
        if (wakeCoordinator != null) {
""",
    "remove preview face wake",
)

hide_start = text.find("    private void hideVoiceSettings() {")
hide_end = text.find("    private int dp(", hide_start)
if hide_start < 0 or hide_end < 0:
    raise SystemExit("Voice Settings dismissal bounds not found")
hide = text[hide_start:hide_end]
hide = once(
    hide,
    "        voiceSettingsOpen = false;",
    """        if (face != null) {
            face.setVisibility(View.VISIBLE);
            face.showIdleBlackImmediately();
        }
        voiceSettingsOpen = false;""",
    "restore face after Voice Settings",
)
text = text[:hide_start] + hide + text[hide_end:]

text += "\n" + MARKER + "\n"
MAIN.write_text(text, encoding="utf-8")
print("Voice Settings: opaque, face-hidden, TEST VOICE and TV child focus enabled")
