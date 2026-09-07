#!/usr/bin/env python3
from pathlib import Path

main = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java')
text = main.read_text(encoding='utf-8')


def replace_once(old: str, new: str, label: str) -> None:
    global text
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'{label}: expected one anchor, found {count}')
    text = text.replace(old, new, 1)


replace_once(
    '    private static final int REQ_NEARBY_WIFI = 1002;\n',
    '    private static final int REQ_NEARBY_WIFI = 1002;\n'
    '    private static final int REQ_CAMERA = 1003;\n',
    'camera request code')

replace_once(
    '    private boolean wakeSupportCheckInFlight;\n',
    '    private boolean wakeSupportCheckInFlight;\n'
    '    private BoopDockModeController dockModeController;\n'
    '    private BoopPresencePeekController presencePeekController;\n'
    '    private BoopMirrorController mirrorController;\n'
    '    private boolean pendingMirrorAfterPermission;\n',
    'dock controller fields')

replace_once(
    '        createRecognizer();\n        createWakeObjects();\n\n        executor = Executors.newSingleThreadExecutor();\n',
    '''        createRecognizer();
        createWakeObjects();
        presencePeekController = new BoopPresencePeekController(
                this,
                personSeen -> {
                    if (personSeen) {
                        wakeFaceForInteraction();
                    }
                });
        mirrorController = new BoopMirrorController(
                this,
                interactionSurface,
                this::wakeFaceForInteraction);
        dockModeController = new BoopDockModeController(
                this,
                new BoopDockModeController.Listener() {
                    @Override
                    public void onWirelessDockChanged(boolean docked) {
                        applyDockWakePolicy(docked);
                    }

                    @Override
                    public void onPresenceNudge() {
                        handleDockPresenceNudge();
                    }
                });

        executor = Executors.newSingleThreadExecutor();
''',
    'dock controller creation')

old_resume = '''    @Override
    protected void onResume() {
        super.onResume();
        activityInForeground = true;
        if (wakeCoordinator != null) wakeCoordinator.setVoiceSettingsOpen(voiceSettingsOpen || chatModeOpen);
        shakeDetector.reset();
        if (sensorManager != null && shakeSensor != null) {
            sensorManager.registerListener(this, shakeSensor, SensorManager.SENSOR_DELAY_GAME);
        }
        if (wakeCoordinator != null) {
            wakeCoordinator.beginForegroundSession();
            boolean granted = checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED;
            wakeCoordinator.setMicrophonePermission(granted);
            if (granted) {
                checkWakeRecognitionSupport();
            }
        }
    }
'''
new_resume = '''    @Override
    protected void onResume() {
        super.onResume();
        activityInForeground = true;
        if (wakeCoordinator != null) wakeCoordinator.setVoiceSettingsOpen(voiceSettingsOpen || chatModeOpen);
        shakeDetector.reset();
        if (sensorManager != null && shakeSensor != null) {
            sensorManager.registerListener(this, shakeSensor, SensorManager.SENSOR_DELAY_GAME);
        }
        if (mirrorController != null) {
            mirrorController.onResume();
        }
        if (dockModeController != null) {
            dockModeController.onResume();
        }
        if (wakeCoordinator != null) {
            wakeCoordinator.beginForegroundSession();
            boolean docked = dockModeController != null && dockModeController.isWirelessDocked();
            wakeCoordinator.setWakeAllowed(docked);
            boolean granted = checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED;
            wakeCoordinator.setMicrophonePermission(granted);
            if (granted && docked) {
                checkWakeRecognitionSupport();
            }
        }
    }
'''
replace_once(old_resume, new_resume, 'onResume dock policy')

old_pause_tail = '''        cancelAssistantFollowUpSilenceTimeout();
        closeWakeAudioSession();
        if (wakeCoordinator != null) {
            wakeCoordinator.endForegroundSession();
        }
        super.onPause();
'''
new_pause_tail = '''        cancelAssistantFollowUpSilenceTimeout();
        closeWakeAudioSession();
        if (presencePeekController != null) {
            presencePeekController.cancel();
        }
        if (mirrorController != null) {
            mirrorController.onPause();
        }
        if (dockModeController != null) {
            dockModeController.onPause();
        }
        if (wakeCoordinator != null) {
            wakeCoordinator.endForegroundSession();
        }
        super.onPause();
'''
replace_once(old_pause_tail, new_pause_tail, 'onPause dock teardown')

pause_anchor = '''        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
'''
helpers = '''        super.onPause();
    }

    private void applyDockWakePolicy(boolean docked) {
        if (wakeCoordinator == null) {
            return;
        }
        wakeCoordinator.setWakeAllowed(docked);
        if (docked
                && checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            checkWakeRecognitionSupport();
        }
    }

    private void handleDockPresenceNudge() {
        if (dockModeController == null
                || !dockModeController.isWirelessDocked()
                || presenceState == null
                || !presenceState.isIdleBlack()
                || listening
                || thinking
                || voiceSettingsOpen
                || (mirrorController != null && mirrorController.isOpen())) {
            return;
        }
        if (presencePeekController != null
                && checkSelfPermission(Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
                && presencePeekController.peek()) {
            return;
        }
        wakeFaceForInteraction();
    }

    private void openMirror() {
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            pendingMirrorAfterPermission = true;
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
            return;
        }
        if (mirrorController == null || !mirrorController.open()) {
            speak("I can't open the mirror on this device.");
            return;
        }
        finishLocalWakeProcessing();
    }

    private void closeMirror() {
        if (mirrorController != null && mirrorController.isOpen()) {
            mirrorController.close();
        }
        finishLocalWakeProcessing();
        wakeFaceForInteraction();
    }

    private void finishLocalWakeProcessing() {
        if (wakeCoordinator != null
                && wakeCoordinator.state() == BoopWakeSessionState.State.PROCESSING) {
            wakeCoordinator.finishWakeProcessing();
        }
    }

    @Override
    public void onBackPressed() {
        if (mirrorController != null && mirrorController.isOpen()) {
            closeMirror();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
'''
replace_once(pause_anchor, helpers, 'dock helper methods')

replace_once(
    '    private void handleRecognizedSpeech(String transcript) {\n        if (BoopVoiceSettingsIntent.matches(transcript)) {\n',
    '''    private void handleRecognizedSpeech(String transcript) {
        BoopMirrorIntent.Action mirrorAction = BoopMirrorIntent.actionFor(transcript);
        if (mirrorAction == BoopMirrorIntent.Action.OPEN) {
            openMirror();
            return;
        }
        if (mirrorAction == BoopMirrorIntent.Action.CLOSE) {
            closeMirror();
            return;
        }
        if (BoopVoiceSettingsIntent.matches(transcript)) {
''',
    'mirror voice routing')

permission_anchor = '''        if (requestCode == REQ_NEARBY_WIFI) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
'''
permission_new = '''        if (requestCode == REQ_CAMERA) {
            boolean granted = grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean openAfterGrant = pendingMirrorAfterPermission;
            pendingMirrorAfterPermission = false;
            if (granted && openAfterGrant) {
                openMirror();
            } else if (!granted && openAfterGrant) {
                speak("I need camera permission to be a mirror.");
            }
            return;
        }

        if (requestCode == REQ_NEARBY_WIFI) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
'''
replace_once(permission_anchor, permission_new, 'camera permission result')

replace_once(
    '        closeWakeAudioSession();\n        if (wakeCoordinator != null) {\n            wakeCoordinator.shutdown();\n',
    '        closeWakeAudioSession();\n'
    '        if (dockModeController != null) {\n'
    '            dockModeController.onPause();\n'
    '            dockModeController = null;\n'
    '        }\n'
    '        if (presencePeekController != null) {\n'
    '            presencePeekController.shutdown();\n'
    '            presencePeekController = null;\n'
    '        }\n'
    '        if (mirrorController != null) {\n'
    '            mirrorController.shutdown();\n'
    '            mirrorController = null;\n'
    '        }\n'
    '        if (wakeCoordinator != null) {\n'
    '            wakeCoordinator.shutdown();\n',
    'destroy dock controllers')

main.write_text(text, encoding='utf-8')
print('Unified dock/mirror integration patched')
