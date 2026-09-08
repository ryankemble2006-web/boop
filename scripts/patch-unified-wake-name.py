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
replace_once('    private LinearLayout voiceSettingsOverlay;\n', '''    private LinearLayout voiceSettingsOverlay;
    private EditText wakeNameInput;
    private Button wakeNameTrainButton;
    private TextView wakeNameTrainingStatus;
    private boolean wakeEnrollmentActive;
    private boolean wakeEnrollmentPauseOwned;
    private String pendingWakeEnrollmentName;
''', 'wake-name fields')

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
        wakeNameParams.setMargins(0, dp(4), 0, dp(8));
        voiceSettingsOverlay.addView(wakeNameInput, wakeNameParams);

        wakeNameTrainButton = new Button(this);
        wakeNameTrainButton.setText("Train wake name · say it 5 times");
        wakeNameTrainButton.setTextSize(18f);
        wakeNameTrainButton.setAllCaps(false);
        wakeNameTrainButton.setContentDescription("Train custom wake name five times");
        wakeNameTrainButton.setOnClickListener(view -> {
            saveWakeNameFromSettings();
            String selected = BoopWakeNameStore.load(this);
            if (BoopWakeName.isDefault(selected)) {
                speak("BOOP is always ready.");
            } else {
                promptWakeNameEnrollment(selected);
            }
        });
        LinearLayout.LayoutParams wakeTrainParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(60));
        voiceSettingsOverlay.addView(wakeNameTrainButton, wakeTrainParams);
        wakeNameTrainingStatus = voiceSettingLabel(wakeNameTrainingStatusText(), 18f, false);
        LinearLayout.LayoutParams wakeStatusParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        wakeStatusParams.setMargins(0, dp(4), 0, dp(22));
        voiceSettingsOverlay.addView(wakeNameTrainingStatus, wakeStatusParams);

        TextView pitchLabel = voiceSettingLabel("Pitch", 22f, false);
''', 'voice wake-name setting and enrolment')

replace_once('''    private void hideVoiceSettings() {
        if (!voiceSettingsOpen) {
            return;
        }
        saveWakeNameFromSettings();
        if (interactionSurface != null && voiceSettingsOverlay != null) {
''', '''    private void hideVoiceSettings() {
        if (!voiceSettingsOpen) {
            return;
        }
        if (wakeEnrollmentActive) cancelWakeNameEnrollment(false);
        saveWakeNameFromSettings();
        if (interactionSurface != null && voiceSettingsOverlay != null) {
''', 'cancel wake-name enrolment on settings close')

replace_once('''        voiceSettingsOverlay = null;
        wakeNameInput = null;
        voiceSettingsOpen = false;
''', '''        voiceSettingsOverlay = null;
        wakeNameInput = null;
        wakeNameTrainButton = null;
        wakeNameTrainingStatus = null;
        voiceSettingsOpen = false;
''', 'clear wake-name controls')

replace_once('''        if (wakeWordController != null) {
            wakeWordController.reloadSensitivity();
        }
        if (wakeCoordinator != null) {
            wakeCoordinator.setVoiceSettingsOpen(false);
        }
''', '''        if (wakeCoordinator != null) {
            // Reload through the coordinator so its engineArmed bookkeeping cannot
            // claim the microphone is still armed after the controller was stopped.
            wakeCoordinator.reloadEngine();
            wakeCoordinator.setVoiceSettingsOpen(false);
        } else if (wakeWordController != null) {
            wakeWordController.reloadSensitivity();
        }
''', 'coordinated settings wake reload')

replace_once('''            @Override public void suspendAll() {
                if (wakeWordController != null) {
                    wakeWordController.suspendAll();
                }
            }

            @Override public void shutdown() {
''', '''            @Override public void suspendAll() {
                if (wakeWordController != null) {
                    wakeWordController.suspendAll();
                }
            }

            @Override public void reload() {
                if (wakeWordController != null) {
                    wakeWordController.reloadSensitivity();
                }
            }

            @Override public void shutdown() {
''', 'wake coordinator reload port')

replace_once('''    private void saveWakeNameFromSettings() {
        if (wakeNameInput == null) return;
        applyWakeName(wakeNameInput.getText() == null ? null : wakeNameInput.getText().toString(), false);
    }

    private void applyWakeName(String requestedName, boolean confirmByVoice) {
        String before = BoopWakeNameStore.load(this);
        String saved = BoopWakeNameStore.save(this, requestedName);
        if (!before.equals(saved)) {
            if (wakeCoordinator != null) wakeCoordinator.reloadEngine();
            else if (wakeWordController != null) wakeWordController.reloadSensitivity();
        }
        if (wakeNameInput != null && !saved.contentEquals(wakeNameInput.getText())) {
            wakeNameInput.setText(saved);
            wakeNameInput.setSelection(saved.length());
        }
        if (confirmByVoice) speak(saved + ". Got it.");
    }

    private int dp(int value) {
''', '''    private void saveWakeNameFromSettings() {
        if (wakeNameInput == null) return;
        applyWakeName(wakeNameInput.getText() == null ? null : wakeNameInput.getText().toString(), false);
    }

    private void applyWakeName(String requestedName, boolean confirmByVoice) {
        String before = BoopWakeNameStore.load(this);
        String saved = BoopWakeNameStore.save(this, requestedName);
        boolean changed = !before.equals(saved);
        if (changed) {
            // A pronunciation profile belongs only to the name it was trained for.
            BoopWakeEnrollmentStore.clear(this);
            if (wakeCoordinator != null) wakeCoordinator.reloadEngine();
            else if (wakeWordController != null) wakeWordController.reloadSensitivity();
        }
        if (wakeNameInput != null && !saved.contentEquals(wakeNameInput.getText())) {
            wakeNameInput.setText(saved);
            wakeNameInput.setSelection(saved.length());
        }
        updateWakeNameTrainingUi();
        if (!confirmByVoice) return;
        if (BoopWakeName.isDefault(saved)) {
            speak("BOOP. Got it.");
        } else {
            promptWakeNameEnrollment(saved);
        }
    }

    private String wakeNameTrainingStatusText() {
        String selected = BoopWakeNameStore.load(this);
        if (BoopWakeName.isDefault(selected)) return "BOOP stays available as the permanent fallback.";
        return BoopWakeEnrollmentStore.hasProfile(this, selected)
                ? selected + " is trained locally. BOOP still works too."
                : selected + " is not trained yet. Say it five times.";
    }

    private void updateWakeNameTrainingUi() {
        if (wakeNameTrainingStatus != null) wakeNameTrainingStatus.setText(wakeNameTrainingStatusText());
        if (wakeNameTrainButton != null) {
            String selected = BoopWakeNameStore.load(this);
            wakeNameTrainButton.setEnabled(!BoopWakeName.isDefault(selected) && !wakeEnrollmentActive);
        }
    }

    private void promptWakeNameEnrollment(String requestedName) {
        String selected = BoopWakeName.normalize(requestedName);
        if (BoopWakeName.isDefault(selected) || wakeEnrollmentActive) return;
        pendingWakeEnrollmentName = selected;
        speak("Say " + selected + " five times.");
        waitForWakeEnrollmentPrompt(selected, 0);
    }

    private void waitForWakeEnrollmentPrompt(String selected, int attempts) {
        if (!selected.equals(pendingWakeEnrollmentName) || wakeEnrollmentActive) return;
        if (tts != null && tts.isSpeaking() && attempts < 120) {
            interactionSurface.postDelayed(() -> waitForWakeEnrollmentPrompt(selected, attempts + 1), 100L);
            return;
        }
        pendingWakeEnrollmentName = null;
        beginWakeNameEnrollment(selected);
    }

    private void beginWakeNameEnrollment(String selected) {
        if (wakeWordController == null || wakeEnrollmentActive || BoopWakeName.isDefault(selected)) return;
        if (listening) {
            suppressNextRecognizerError = true;
            stopListening();
        }
        if (tts != null) tts.stop();
        wakeEnrollmentPauseOwned = !voiceSettingsOpen;
        if (wakeCoordinator != null) wakeCoordinator.setVoiceSettingsOpen(true);
        wakeEnrollmentActive = true;
        if (wakeNameTrainingStatus != null) wakeNameTrainingStatus.setText(selected + " · 0 / 5");
        if (wakeNameTrainButton != null) wakeNameTrainButton.setEnabled(false);

        boolean started = wakeWordController.startEnrollment(selected, new BoopWakeWordController.EnrollmentListener() {
            @Override public void onEnrollmentProgress(String name, int accepted, int required) {
                if (wakeNameTrainingStatus != null) wakeNameTrainingStatus.setText(name + " · " + accepted + " / " + required);
            }

            @Override public void onEnrollmentRetry(String name, int accepted, int required) {
                if (wakeNameTrainingStatus != null) wakeNameTrainingStatus.setText("Again please · " + accepted + " / " + required);
            }

            @Override public void onEnrollmentComplete(String name) {
                wakeEnrollmentActive = false;
                finishWakeNameEnrollmentPause();
                updateWakeNameTrainingUi();
                speak(name + ". Got it.");
            }

            @Override public void onEnrollmentFailure(String message) {
                wakeEnrollmentActive = false;
                finishWakeNameEnrollmentPause();
                updateWakeNameTrainingUi();
                speak("Training stopped. BOOP still works.");
            }
        });
        if (!started) {
            wakeEnrollmentActive = false;
            finishWakeNameEnrollmentPause();
            updateWakeNameTrainingUi();
        }
    }

    private void finishWakeNameEnrollmentPause() {
        if (wakeCoordinator != null) {
            wakeCoordinator.reloadEngine();
            if (wakeEnrollmentPauseOwned) wakeCoordinator.setVoiceSettingsOpen(false);
        } else if (wakeWordController != null) {
            wakeWordController.reloadSensitivity();
        }
        wakeEnrollmentPauseOwned = false;
    }

    private void cancelWakeNameEnrollment(boolean restoreWake) {
        pendingWakeEnrollmentName = null;
        if (wakeWordController != null) wakeWordController.suspendAll();
        wakeEnrollmentActive = false;
        if (restoreWake) finishWakeNameEnrollmentPause();
        wakeEnrollmentPauseOwned = false;
        updateWakeNameTrainingUi();
    }

    private int dp(int value) {
''', 'wake-name save/enrolment helpers')

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
print('Unified spoken wake-name UI/routing, five-sample local enrolment and coordinated reload patched')
