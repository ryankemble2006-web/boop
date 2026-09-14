#!/usr/bin/env python3
"""Keep the real reply alive while Android initializes TTS; preserve natural synthesis/selection."""
from pathlib import Path
MAIN=Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java")
text=MAIN.read_text()
def once(old,new):
    global text
    if text.count(old)!=1:
        raise SystemExit(f"Speech startup anchor count {text.count(old)}: {old[:80]}")
    text=text.replace(old,new,1)
once("    private void speak(String text) {",
     "    private void speak(String text) {\n        if (isFinishing() || isDestroyed()) return;")
once("    private void speakWithAndroidTts(String text) {\n        if (ttsReady && androidSpeechBackend != null && voiceController != null) {",
     "    private void speakWithAndroidTts(String text) {\n        if (isFinishing() || isDestroyed()) return;\n        if (androidSpeechBackend != null && voiceController != null) {")
once("""    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS && tts != null) {
            int result = tts.setLanguage(Locale.getDefault());
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
            if (ttsReady) {
                voiceController.initialize(tts, Locale.getDefault());
            }
        }
    }""",
"""    public void onInit(int status) {
        if (isFinishing() || isDestroyed()) return;
        ttsReady = false;
        if (status == TextToSpeech.SUCCESS && tts != null) {
            int result = tts.setLanguage(Locale.getDefault());
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
            if (ttsReady && voiceController != null) {
                voiceController.initialize(tts, Locale.getDefault());
            }
        }
        if (androidSpeechBackend != null) androidSpeechBackend.onInitializationFinished(ttsReady);
    }""")
once("    protected void onPause() {",
     "    protected void onPause() {\n        if (androidSpeechBackend != null) androidSpeechBackend.cancelPending();")
MAIN.write_text(text)
print("Reply startup waiting and lifecycle cancellation integrated; natural voice route preserved")
