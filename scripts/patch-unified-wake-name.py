#!/usr/bin/env python3
from pathlib import Path
from shutil import copy2

main = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java')
text = main.read_text(encoding='utf-8')

def replace_once(old: str, new: str, label: str) -> None:
    global text
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'{label}: expected one anchor, found {count}')
    text = text.replace(old, new, 1)

replace_once('import android.widget.Button;\n', 'import android.widget.Button;\nimport android.widget.EditText;\n', 'EditText import')
replace_once('    private LinearLayout voiceSettingsOverlay;\n', '    private LinearLayout voiceSettingsOverlay;\n    private EditText wakeNameInput;\n', 'wake-name field')

replace_once('''        voiceSettingsOverlay.addView(title, titleParams);

        TextView pitchLabel = voiceSettingLabel("Pitch", 22f, false);
''', '''        voiceSettingsOverlay.addView(title, titleParams);

        TextView wakeNameLabel = voiceSettingLabel("BOOP's name", 22f, false);
        voiceSettingsOverlay.addView(wakeNameLabel);
        wakeNameInput = new EditText(this);
        wakeNameInput.setSingleLine(true);
        wakeNameInput.setText(BoopWakeNameStore.load(this));
        wakeNameInput.setSelectAllOnFocus(true);
        wakeNameInput.setTextColor(Color.WHITE);
        wakeNameInput.setHintTextColor(Color.GRAY);
        wakeNameInput.setHint("BOOP");
        wakeNameInput.setTextSize(22f);
        wakeNameInput.setContentDescription("BOOP's spoken wake name");
        wakeNameInput.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) saveWakeNameFromSettings();
        });
        LinearLayout.LayoutParams wakeNameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(60));
        wakeNameParams.setMargins(0, dp(4), 0, dp(22));
        voiceSettingsOverlay.addView(wakeNameInput, wakeNameParams);

        TextView pitchLabel = voiceSettingLabel("Pitch", 22f, false);
''', 'voice wake-name setting')

replace_once('''    private void hideVoiceSettings() {
        if (!voiceSettingsOpen) {
            return;
        }
        if (interactionSurface != null && voiceSettingsOverlay != null) {
''', '''    private void hideVoiceSettings() {
        if (!voiceSettingsOpen) {
            return;
        }
        saveWakeNameFromSettings();
        if (interactionSurface != null && voiceSettingsOverlay != null) {
''', 'save wake name on settings close')

replace_once('        voiceSettingsOverlay = null;\n        voiceSettingsOpen = false;\n', '        voiceSettingsOverlay = null;\n        wakeNameInput = null;\n        voiceSettingsOpen = false;\n', 'clear wake-name field')

replace_once('''    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
''', '''    private void saveWakeNameFromSettings() {
        if (wakeNameInput == null) return;
        applyWakeName(wakeNameInput.getText() == null ? null : wakeNameInput.getText().toString(), false);
    }

    private void applyWakeName(String requestedName, boolean confirmByVoice) {
        String before = BoopWakeNameStore.load(this);
        String saved = BoopWakeNameStore.save(this, requestedName);
        if (!before.equals(saved) && wakeWordController != null) wakeWordController.reloadSensitivity();
        if (wakeNameInput != null && !saved.contentEquals(wakeNameInput.getText())) {
            wakeNameInput.setText(saved);
            wakeNameInput.setSelection(saved.length());
        }
        if (confirmByVoice) speak(saved + ". Got it.");
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
''', 'wake-name helpers')

replace_once('''    private void handleRecognizedSpeech(String transcript) {
        BoopMirrorIntent.Action mirrorAction = BoopMirrorIntent.actionFor(transcript);
''', '''    private void handleRecognizedSpeech(String transcript) {
        BoopWakeNameIntent.Result wakeNameChange = BoopWakeNameIntent.parse(transcript);
        if (wakeNameChange.action() == BoopWakeNameIntent.Action.SET
                || wakeNameChange.action() == BoopWakeNameIntent.Action.RESET) {
            applyWakeName(wakeNameChange.name(), true);
            return;
        }

        BoopMirrorIntent.Action mirrorAction = BoopMirrorIntent.actionFor(transcript);
''', 'verbal wake-name routing')

replace_once(
    'BoopWakeTranscriptNormalizer.stripLeadingWakeWord(best);',
    'BoopWakeTranscriptNormalizer.stripLeadingWakeWord(best, BoopWakeNameStore.load(this));',
    'custom call prefix before command routing')

main.write_text(text, encoding='utf-8')

model = Path('.cache/boop-wake/sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01/bpe.model')
destination = Path('boop-build/BOOP-Alpha1/app/src/main/assets/boop-kws/bpe.model')
if not model.is_file():
    raise SystemExit(f'Wake-name BPE model missing from verified model cache: {model}')
destination.parent.mkdir(parents=True, exist_ok=True)
copy2(model, destination)
print('Unified spoken wake-name UI/routing and BPE runtime asset patched')
