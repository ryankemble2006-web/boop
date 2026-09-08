#!/usr/bin/env python3
from pathlib import Path


def apply() -> None:
    main = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java')
    text = main.read_text(encoding='utf-8')
    marker = '// BOOP_WAKE_AUTO_ENROLMENT_V1'
    if marker in text:
        print('Automatic five-say custom-name flow already patched')
        return

    def replace_once(old: str, new: str, label: str) -> None:
        nonlocal text
        count = text.count(old)
        if count != 1:
            raise SystemExit(f'{label}: expected one anchor, found {count}')
        text = text.replace(old, new, 1)

    replace_once(
        '    private String pendingWakeEnrollmentName;\n',
        '    private String pendingWakeEnrollmentName;\n'
        '    // BOOP_WAKE_AUTO_ENROLMENT_V1\n'
        '    private String pendingWakeEnrollmentAfterSettings;\n',
        'pending settings enrolment field')

    replace_once(
        '''        wakeNameTrainButton.setOnClickListener(view -> {
            saveWakeNameFromSettings();
            String selected = BoopWakeNameStore.load(this);
''',
        '''        wakeNameTrainButton.setOnClickListener(view -> {
            saveWakeNameFromSettings();
            pendingWakeEnrollmentAfterSettings = null;
            String selected = BoopWakeNameStore.load(this);
''',
        'manual train clears automatic pending enrolment')

    replace_once(
        '''        if (wakeEnrollmentActive) cancelWakeNameEnrollment(false);
        saveWakeNameFromSettings();
        if (interactionSurface != null && voiceSettingsOverlay != null) {
''',
        '''        if (wakeEnrollmentActive) cancelWakeNameEnrollment(false);
        saveWakeNameFromSettings();
        String trainAfterClose = pendingWakeEnrollmentAfterSettings;
        pendingWakeEnrollmentAfterSettings = null;
        if (interactionSurface != null && voiceSettingsOverlay != null) {
''',
        'capture changed name before settings close')

    replace_once(
        '''        wakeFaceForInteraction();
    }

    private void saveWakeNameFromSettings() {
''',
        '''        wakeFaceForInteraction();
        if (trainAfterClose != null) promptWakeNameEnrollment(trainAfterClose);
    }

    private void saveWakeNameFromSettings() {
''',
        'start automatic enrolment after settings close')

    start = text.index('    private void applyWakeName(String requestedName, boolean confirmByVoice) {')
    end = text.index('    private String wakeNameTrainingStatusText() {', start)
    replacement = '''    private void applyWakeName(String requestedName, boolean confirmByVoice) {
        String before = BoopWakeNameStore.load(this);
        String saved = BoopWakeNameStore.save(this, requestedName);
        boolean matchingProfilePresent = BoopWakeEnrollmentStore.hasProfile(this, saved);
        boolean autoTrainAfterSettings = BoopWakeTrainingPolicy.shouldPromptAfterSettings(
                before, saved, matchingProfilePresent);
        boolean changed = !before.equals(saved);
        if (changed) {
            if (!matchingProfilePresent) BoopWakeEnrollmentStore.clear(this);
            if (wakeCoordinator != null) wakeCoordinator.reloadEngine();
            else if (wakeWordController != null) wakeWordController.reloadSensitivity();
        }
        if (wakeNameInput != null && !saved.contentEquals(wakeNameInput.getText())) {
            wakeNameInput.setText(saved);
            wakeNameInput.setSelection(saved.length());
        }
        updateWakeNameTrainingUi();
        if (confirmByVoice) {
            pendingWakeEnrollmentAfterSettings = null;
            if (BoopWakeName.isDefault(saved)) speak("BOOP. Got it.");
            else if (matchingProfilePresent) speak(saved + ". Got it.");
            else promptWakeNameEnrollment(saved);
            return;
        }
        if (BoopWakeName.isDefault(saved)) {
            pendingWakeEnrollmentAfterSettings = null;
        } else if (autoTrainAfterSettings) {
            pendingWakeEnrollmentAfterSettings = saved;
        }
    }

'''
    text = text[:start] + replacement + text[end:]

    replace_once(
        '''    private void promptWakeNameEnrollment(String requestedName) {
        String selected = BoopWakeName.normalize(requestedName);
        if (BoopWakeName.isDefault(selected) || wakeEnrollmentActive) return;
        pendingWakeEnrollmentName = selected;
''',
        '''    private void promptWakeNameEnrollment(String requestedName) {
        String selected = BoopWakeName.normalize(requestedName);
        if (BoopWakeName.isDefault(selected) || wakeEnrollmentActive) return;
        pendingWakeEnrollmentAfterSettings = null;
        pendingWakeEnrollmentName = selected;
''',
        'prompt consumes automatic pending enrolment')

    main.write_text(text, encoding='utf-8')
    print('Unified changed custom names now flow directly into five-say local enrolment')


if __name__ == '__main__':
    apply()
