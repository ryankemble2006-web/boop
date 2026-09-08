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

    # patch-wake-partial-fallback.py has already inserted its accumulator reset
    # immediately after this line, so anchor only on the stable session assignment.
    replace_once(
        '        wakeAudioSession = session;\n',
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
        '    private void stopListening() {\n',
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

    # The partial-fallback materializer inserts its accumulator reset inside this
    # branch. Add diagnostics at the branch entrance and leave that behavior intact.
    replace_once(
        '        if (failedMode == RecognitionMode.WAKE) {\n',
        '''        if (failedMode == RecognitionMode.WAKE) {
            if (failedWakeTrace != null) {
                failedWakeTrace.error(error, SystemClock.elapsedRealtime());
                showWakeDiagnostic(failedWakeTrace, true);
            }
''',
        'wake error diagnostic')

    # Chat-mode materialization inserts a foreground guard immediately after the
    # onResults() signature. Anchor on the stable completed-mode assignment instead.
    replace_once(
        '        RecognitionMode completedMode = recognitionMode;\n',
        '''        RecognitionMode completedMode = recognitionMode;
        BoopWakeDiagnosticTrace completedWakeTrace = wakeDiagnosticTrace;
''',
        'result callback trace capture')

    # Capture the effective final transcript after the existing partial fallback
    # has promoted a useful partial, if Android's final result is empty.
    replace_once(
        '''        if (completedMode == RecognitionMode.WAKE) {
            best = wakeTranscriptAccumulator.chooseFinal(best);
            wakeTranscriptAccumulator.reset();
''',
        '''        if (completedMode == RecognitionMode.WAKE) {
            best = wakeTranscriptAccumulator.chooseFinal(best);
            wakeTranscriptAccumulator.reset();
            if (completedWakeTrace != null) {
                completedWakeTrace.result(best, SystemClock.elapsedRealtime());
                showWakeDiagnostic(completedWakeTrace, true);
            }
''',
        'wake final result diagnostic')

    # The partial fallback already handles WAKE and returns. Record the same raw
    # first partial before that branch, then let the existing accumulator own routing.
    replace_once(
        '''    public void onPartialResults(Bundle partialResults) {
''',
        '''    public void onPartialResults(Bundle partialResults) {
        if (recognitionMode == RecognitionMode.WAKE
                && wakeDiagnosticTrace != null
                && partialResults != null) {
            ArrayList<String> diagnosticMatches = partialResults.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
            if (diagnosticMatches != null && !diagnosticMatches.isEmpty()) {
                String diagnosticCandidate = diagnosticMatches.get(0);
                if (diagnosticCandidate != null && !diagnosticCandidate.isBlank()) {
                    wakeDiagnosticTrace.partial(
                            diagnosticCandidate, SystemClock.elapsedRealtime());
                }
            }
        }
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
