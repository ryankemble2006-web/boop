#!/usr/bin/env python3
from pathlib import Path


MAIN = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java')
MARKER = '// BOOP_WAKE_ASR_DIAGNOSTIC_V1'


def apply() -> None:
    text = MAIN.read_text(encoding='utf-8')
    if MARKER in text:
        print('Wake ASR callback diagnostics already patched')
        return

    def replace_once(old: str, new: str, label: str) -> None:
        nonlocal text
        count = text.count(old)
        if count != 1:
            raise SystemExit(f'{label}: expected one anchor, found {count}')
        text = text.replace(old, new, 1)

    replace_once(
        'import android.os.ParcelFileDescriptor;\n',
        'import android.os.ParcelFileDescriptor;\nimport android.os.SystemClock;\n',
        'SystemClock import')

    replace_once(
        '    private BoopWakeAudioSession wakeAudioSession;\n',
        '    private BoopWakeAudioSession wakeAudioSession;\n'
        f'    {MARKER}\n'
        '    private BoopWakeDiagnosticTrace wakeDiagnosticTrace;\n',
        'diagnostic trace field')

    replace_once(
        '''        wakeAudioSession = session;
        recognitionMode = RecognitionMode.WAKE;
        listening = true;
''',
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
        recognitionMode = RecognitionMode.WAKE;
        listening = true;
''',
        'wake recognition diagnostic start')

    replace_once(
        '''        } catch (RuntimeException e) {
            closeWakeAudioSession();
''',
        '''        } catch (RuntimeException e) {
            if (wakeDiagnosticTrace == diagnosticTrace) {
                wakeDiagnosticTrace = null;
            }
            Toast.makeText(this,
                    "WAKE ASR START FAILED " + e.getClass().getSimpleName(),
                    Toast.LENGTH_LONG).show();
            closeWakeAudioSession();
''',
        'wake recognition start failure diagnostic')

    replace_once(
        '''    private void stopListening() {
''',
        '''    private void showWakeDiagnostic(BoopWakeDiagnosticTrace trace, boolean terminal) {
        if (trace == null) return;
        Toast.makeText(this, trace.summary(SystemClock.elapsedRealtime()), Toast.LENGTH_LONG).show();
        if (terminal && wakeDiagnosticTrace == trace) {
            wakeDiagnosticTrace = null;
        }
    }

    private void stopListening() {
''',
        'wake diagnostic toast helper')

    replace_once(
        '    @Override public void onReadyForSpeech(Bundle params) { }\n',
        '''    @Override
    public void onReadyForSpeech(Bundle params) {
        if (recognitionMode == RecognitionMode.WAKE && wakeDiagnosticTrace != null) {
            wakeDiagnosticTrace.ready(SystemClock.elapsedRealtime());
        }
    }
''',
        'ready callback diagnostic')

    replace_once(
        '''    public void onBeginningOfSpeech() {
        if (assistantFollowUpListening && recognitionMode == RecognitionMode.TAP) {
''',
        '''    public void onBeginningOfSpeech() {
        if (recognitionMode == RecognitionMode.WAKE && wakeDiagnosticTrace != null) {
            wakeDiagnosticTrace.begin(SystemClock.elapsedRealtime());
        }
        if (assistantFollowUpListening && recognitionMode == RecognitionMode.TAP) {
''',
        'begin speech callback diagnostic')

    replace_once(
        '''    public void onEndOfSpeech() {
        if (recognitionMode == RecognitionMode.WAKE && wakeAudioSession != null) {
''',
        '''    public void onEndOfSpeech() {
        if (recognitionMode == RecognitionMode.WAKE && wakeDiagnosticTrace != null) {
            wakeDiagnosticTrace.end(SystemClock.elapsedRealtime());
        }
        if (recognitionMode == RecognitionMode.WAKE && wakeAudioSession != null) {
''',
        'end speech callback diagnostic')

    replace_once(
        '''    public void onError(int error) {
        RecognitionMode failedMode = recognitionMode;
''',
        '''    public void onError(int error) {
        RecognitionMode failedMode = recognitionMode;
        BoopWakeDiagnosticTrace failedWakeTrace = wakeDiagnosticTrace;
''',
        'error callback trace capture')

    replace_once(
        '''        if (failedMode == RecognitionMode.WAKE) {
            closeWakeAudioSession();
''',
        '''        if (failedMode == RecognitionMode.WAKE) {
            if (failedWakeTrace != null) {
                failedWakeTrace.error(error, SystemClock.elapsedRealtime());
                showWakeDiagnostic(failedWakeTrace, true);
            }
            closeWakeAudioSession();
''',
        'wake error diagnostic')

    replace_once(
        '''    public void onResults(Bundle results) {
        RecognitionMode completedMode = recognitionMode;
''',
        '''    public void onResults(Bundle results) {
        RecognitionMode completedMode = recognitionMode;
        BoopWakeDiagnosticTrace completedWakeTrace = wakeDiagnosticTrace;
''',
        'result callback trace capture')

    replace_once(
        '''        if (completedMode == RecognitionMode.WAKE) {
            closeWakeAudioSession();
''',
        '''        if (completedMode == RecognitionMode.WAKE) {
            if (completedWakeTrace != null) {
                completedWakeTrace.result(best, SystemClock.elapsedRealtime());
                showWakeDiagnostic(completedWakeTrace, true);
            }
            closeWakeAudioSession();
''',
        'wake final result diagnostic')

    replace_once(
        '''    public void onPartialResults(Bundle partialResults) {
        if (!assistantFollowUpListening || recognitionMode != RecognitionMode.TAP) {
''',
        '''    public void onPartialResults(Bundle partialResults) {
        if (recognitionMode == RecognitionMode.WAKE && wakeDiagnosticTrace != null) {
            ArrayList<String> wakeMatches = partialResults.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
            if (wakeMatches != null && !wakeMatches.isEmpty()) {
                String wakeCandidate = wakeMatches.get(0);
                if (wakeCandidate != null && !wakeCandidate.isBlank()) {
                    wakeDiagnosticTrace.partial(wakeCandidate, SystemClock.elapsedRealtime());
                }
            }
        }
        if (!assistantFollowUpListening || recognitionMode != RecognitionMode.TAP) {
''',
        'wake partial result diagnostic')

    replace_once(
        '''        shakeDetector.reset();
        closeWakeAudioSession();
''',
        '''        shakeDetector.reset();
        wakeDiagnosticTrace = null;
        closeWakeAudioSession();
''',
        'destroy diagnostic cleanup')

    MAIN.write_text(text, encoding='utf-8')
    print('Wake ASR callback diagnostics materialized; recognizer behavior unchanged')


if __name__ == '__main__':
    apply()
