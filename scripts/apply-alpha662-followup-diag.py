from pathlib import Path

path = Path('source/MainActivity.java')
text = path.read_text(encoding='utf-8')

replacements = [
    (
        'import android.widget.TextView;\n',
        'import android.widget.TextView;\nimport android.widget.Toast;\n',
    ),
    (
        '    private boolean listenAfterTts = false;\n',
        '    private boolean listenAfterTts = false;\n'
        '    private boolean followUpDiagnosticActive = false;\n'
        '    private boolean followUpReadyForSpeech = false;\n'
        '    private boolean followUpBeginningOfSpeech = false;\n'
        '    private boolean followUpEndOfSpeech = false;\n',
    ),
    (
        '    private void speakThenListen(String text) {\n'
        '        listenAfterTts = true;\n'
        '        speak(text);\n'
        '    }\n',
        '    private void speakThenListen(String text) {\n'
        '        followUpDiagnosticActive = true;\n'
        '        followUpReadyForSpeech = false;\n'
        '        followUpBeginningOfSpeech = false;\n'
        '        followUpEndOfSpeech = false;\n'
        '        listenAfterTts = true;\n'
        '        speak(text);\n'
        '    }\n',
    ),
    (
        '    @Override public void onReadyForSpeech(Bundle params) { }\n'
        '    @Override public void onBeginningOfSpeech() { }\n',
        '    @Override\n'
        '    public void onReadyForSpeech(Bundle params) {\n'
        '        if (followUpDiagnosticActive && recognitionMode == RecognitionMode.TAP) {\n'
        '            followUpReadyForSpeech = true;\n'
        '        }\n'
        '    }\n\n'
        '    @Override\n'
        '    public void onBeginningOfSpeech() {\n'
        '        if (followUpDiagnosticActive && recognitionMode == RecognitionMode.TAP) {\n'
        '            followUpBeginningOfSpeech = true;\n'
        '        }\n'
        '    }\n',
    ),
    (
        '    public void onEndOfSpeech() {\n'
        '        if (recognitionMode == RecognitionMode.WAKE && wakeAudioSession != null) {\n',
        '    public void onEndOfSpeech() {\n'
        '        if (followUpDiagnosticActive && recognitionMode == RecognitionMode.TAP) {\n'
        '            followUpEndOfSpeech = true;\n'
        '        }\n'
        '        if (recognitionMode == RecognitionMode.WAKE && wakeAudioSession != null) {\n',
    ),
    (
        '    private String speechErrorName(int error) {\n',
        '    private void showFollowUpSpeechDiagnostic(int error) {\n'
        '        String message = "Follow-up ASR ready=" + followUpReadyForSpeech\n'
        '                + " speech=" + followUpBeginningOfSpeech\n'
        '                + " end=" + followUpEndOfSpeech\n'
        '                + " error=" + error;\n'
        '        Toast.makeText(this, message, Toast.LENGTH_LONG).show();\n'
        '    }\n\n'
        '    private String speechErrorName(int error) {\n',
    ),
    (
        '        if (failedMode == RecognitionMode.TAP) {\n'
        '            speak("Speech error " + error + ", " + speechErrorName(error) + ".");\n',
        '        if (failedMode == RecognitionMode.TAP) {\n'
        '            if (followUpDiagnosticActive) {\n'
        '                showFollowUpSpeechDiagnostic(error);\n'
        '                followUpDiagnosticActive = false;\n'
        '            }\n'
        '            speak("Speech error " + error + ", " + speechErrorName(error) + ".");\n',
    ),
    (
        '        if (completedMode == RecognitionMode.TAP) {\n'
        '            if (best != null) {\n',
        '        if (completedMode == RecognitionMode.TAP) {\n'
        '            if (best != null) {\n'
        '                followUpDiagnosticActive = false;\n',
    ),
]

for old, new in replacements:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'expected one match, found {count}: {old[:80]!r}')
    text = text.replace(old, new, 1)

path.write_text(text, encoding='utf-8')
