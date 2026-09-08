#!/usr/bin/env python3
from pathlib import Path

MAIN = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java')
MARKER = '// BOOP_POWERED_WAKE_RESILIENCE_V1'


def apply() -> None:
    text = MAIN.read_text(encoding='utf-8')
    if MARKER in text:
        print('Powered wake resilience already patched')
        return

    def replace_once(old: str, new: str, label: str) -> None:
        nonlocal text
        count = text.count(old)
        if count != 1:
            raise SystemExit(f'{label}: expected one anchor, found {count}')
        text = text.replace(old, new, 1)

    replace_once(
        '''    // BOOP_WAKE_ASR_DIAGNOSTIC_V1
    private BoopWakeDiagnosticTrace wakeDiagnosticTrace;
''',
        '''    // BOOP_WAKE_ASR_DIAGNOSTIC_V1
    private BoopWakeDiagnosticTrace wakeDiagnosticTrace;
    ''' + MARKER + '''
    private String lastWakeDiagnosticSummary = "No wake diagnostic captured yet.";
    private String lastWakeDiagnosticRecovery = "none";
''',
        'diagnostic retained-state fields')

    replace_once(
        '''                    @Override
                    public void onWirelessDockChanged(boolean docked) {
                        applyDockWakePolicy(docked);
                    }
''',
        '''                    @Override
                    public void onWakePowerChanged(boolean powered) {
                        applyDockWakePolicy(powered);
                    }
''',
        'external-power listener')

    replace_once(
        '''            boolean docked = dockModeController != null && dockModeController.isWirelessDocked();
            wakeCoordinator.setWakeAllowed(docked);
            boolean granted = checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED;
            wakeCoordinator.setMicrophonePermission(granted);
            if (granted && docked) {
                checkWakeRecognitionSupport();
            }
''',
        '''            boolean powered = dockModeController != null && dockModeController.isWakePowered();
            wakeCoordinator.setWakeAllowed(powered);
            boolean granted = checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED;
            wakeCoordinator.setMicrophonePermission(granted);
            if (granted && powered) {
                checkWakeRecognitionSupport();
            }
''',
        'resume external-power policy')

    replace_once(
        '''    private void applyDockWakePolicy(boolean docked) {
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
''',
        '''    private void applyDockWakePolicy(boolean powered) {
        if (wakeCoordinator == null) {
            return;
        }
        wakeCoordinator.setWakeAllowed(powered);
        if (powered
                && checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            checkWakeRecognitionSupport();
        }
    }
''',
        'live external-power policy')

    replace_once(
        '''        final BoopWakeDiagnosticTrace diagnosticTrace =
                new BoopWakeDiagnosticTrace(SystemClock.elapsedRealtime());
        wakeDiagnosticTrace = diagnosticTrace;
        if (presenceHandler != null) {
            presenceHandler.postDelayed(() -> {
                if (wakeDiagnosticTrace == diagnosticTrace && !diagnosticTrace.terminal()) {
                    showWakeDiagnostic(diagnosticTrace, false);
                }
            }, 4_500L);
        }
        wakeAudioSession = session;
''',
        '''        final BoopWakeDiagnosticTrace diagnosticTrace =
                new BoopWakeDiagnosticTrace(SystemClock.elapsedRealtime());
        wakeDiagnosticTrace = diagnosticTrace;
        wakeAudioSession = session;
''',
        'remove automatic diagnostic timeout UI')

    replace_once(
        '''            diagnosticTrace.startFailure(
                    e.getClass().getSimpleName(), SystemClock.elapsedRealtime());
            showWakeDiagnostic(diagnosticTrace, true);
            closeWakeAudioSession();
            listening = false;
            recognitionMode = RecognitionMode.NONE;
            face.animate().alpha(1.0f).setDuration(120).start();
            if (wakeCoordinator != null) {
                wakeCoordinator.failWakeSession();
            }
''',
        '''            diagnosticTrace.startFailure(
                    e.getClass().getSimpleName(), SystemClock.elapsedRealtime());
            rememberWakeDiagnostic(diagnosticTrace);
            closeWakeAudioSession();
            listening = false;
            recognitionMode = RecognitionMode.NONE;
            face.animate().alpha(1.0f).setDuration(120).start();
            if (wakeCoordinator != null) {
                wakeCoordinator.recoverWakeSession();
                updateWakeRecoverySnapshot();
            }
''',
        'wake recognizer start recovery')

    old_helper = '''    private void showWakeDiagnostic(BoopWakeDiagnosticTrace trace, boolean terminal) {
        if (trace == null) return;
        String summary = trace.summary(SystemClock.elapsedRealtime());
        if (trace.requiresAcknowledgement()) {
            new AlertDialog.Builder(this)
                    .setTitle("BOOP wake diagnostic")
                    .setMessage(summary)
                    .setCancelable(false)
                    .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            Toast.makeText(this, summary, Toast.LENGTH_LONG).show();
        }
        if (terminal && wakeDiagnosticTrace == trace) {
            wakeDiagnosticTrace = null;
        }
    }

'''
    new_helper = r'''    private void rememberWakeDiagnostic(BoopWakeDiagnosticTrace trace) {
        if (trace == null) return;
        lastWakeDiagnosticSummary = trace.summary(SystemClock.elapsedRealtime());
        if (trace.terminal() && wakeDiagnosticTrace == trace) {
            wakeDiagnosticTrace = null;
        }
    }

    private void updateWakeRecoverySnapshot() {
        lastWakeDiagnosticRecovery = wakeCoordinator == null
                ? "no coordinator"
                : wakeCoordinator.state().name();
    }

    private void showRequestedWakeDiagnostics() {
        if (wakeCoordinator != null
                && wakeCoordinator.state() == BoopWakeSessionState.State.PROCESSING) {
            wakeCoordinator.finishWakeProcessing();
        }
        if (wakeCoordinator != null) {
            wakeCoordinator.setVoiceSettingsOpen(true);
        }
        boolean powered = dockModeController != null && dockModeController.isWakePowered();
        boolean wireless = dockModeController != null && dockModeController.isWirelessDocked();
        boolean micGranted = checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
        String wakeState = wakeCoordinator == null ? "none" : wakeCoordinator.state().name();
        String message = lastWakeDiagnosticSummary
                + "\n\nwakeName=" + BoopWakeNameStore.load(this)
                + "\nexternalPower=" + powered
                + "\nwirelessDock=" + wireless
                + "\nmicPermission=" + micGranted
                + "\nwakeState=" + wakeState
                + "\nrecognitionMode=" + recognitionMode.name()
                + "\nlistening=" + listening
                + "\nlastRecovery=" + lastWakeDiagnosticRecovery;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("BOOP diagnostics")
                .setMessage(message)
                .setPositiveButton("Close", (shown, which) -> shown.dismiss())
                .create();
        dialog.setOnDismissListener(shown -> {
            if (wakeCoordinator != null) {
                wakeCoordinator.setVoiceSettingsOpen(voiceSettingsOpen || chatModeOpen);
            }
            wakeFaceForInteraction();
        });
        dialog.show();
    }

'''
    replace_once(old_helper, new_helper, 'pull-only diagnostic helper')

    replace_once(
        '''            @Override
            public void onWakeFailure(String message) {
                runOnUiThread(() -> {
                    if (wakeCoordinator != null) {
                        wakeCoordinator.failWakeSession();
                    }
                });
            }
''',
        r'''            @Override
            public void onWakeFailure(String message) {
                runOnUiThread(() -> {
                    lastWakeDiagnosticSummary = "WAKE ENGINE FAILURE\n" + message;
                    if (wakeCoordinator != null) {
                        wakeCoordinator.failWakeSession();
                        updateWakeRecoverySnapshot();
                    }
                });
            }
''',
        'wake engine failure diagnostic without retry loop')

    replace_once(
        '''    private void handleRecognizedSpeech(String transcript) {
''',
        '''    private void handleRecognizedSpeech(String transcript) {
        if (BoopWakeDiagnosticsIntent.matches(transcript)) {
            showRequestedWakeDiagnostics();
            return;
        }
''',
        'hidden diagnostics command')

    old_error = '''        if (failedMode == RecognitionMode.WAKE) {
            if (failedWakeTrace != null) {
                failedWakeTrace.error(error, SystemClock.elapsedRealtime());
                showWakeDiagnostic(failedWakeTrace, true);
            }
            wakeTranscriptAccumulator.reset();
            closeWakeAudioSession();
            if (wakeCoordinator != null) {
                if (error == SpeechRecognizer.ERROR_NO_MATCH
                        || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    wakeCoordinator.cancelWakeCapture();
                } else {
                    wakeCoordinator.failWakeSession();
                }
            }
            scheduleFaceIdle();
            return;
        }
'''
    new_error = '''        if (failedMode == RecognitionMode.WAKE) {
            if (failedWakeTrace != null) {
                failedWakeTrace.error(error, SystemClock.elapsedRealtime());
                rememberWakeDiagnostic(failedWakeTrace);
            }
            wakeTranscriptAccumulator.reset();
            closeWakeAudioSession();
            if (wakeCoordinator != null) {
                wakeCoordinator.recoverWakeSession();
                updateWakeRecoverySnapshot();
            }
            scheduleFaceIdle();
            return;
        }
'''
    replace_once(old_error, new_error, 'silent wake command failure recovery')

    replace_once(
        '''            if (completedWakeTrace != null) {
                completedWakeTrace.result(best, SystemClock.elapsedRealtime());
                showWakeDiagnostic(completedWakeTrace, true);
            }
''',
        '''            if (completedWakeTrace != null) {
                completedWakeTrace.result(best, SystemClock.elapsedRealtime());
                rememberWakeDiagnostic(completedWakeTrace);
                lastWakeDiagnosticRecovery = "reply pending";
            }
''',
        'successful wake trace retention')

    MAIN.write_text(text, encoding='utf-8')
    print('External-power wake, silent real rearm and pull-only diagnostics materialized')


if __name__ == '__main__':
    apply()
