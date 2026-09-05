from pathlib import Path

# Triggered once after the bounded patch workflow exists.
path = Path('source/MainActivity.java')
text = path.read_text(encoding='utf-8')

replacements = [
    (
        '    private boolean listenAfterTts = false;\n',
        '    private boolean listenAfterTts = false;\n'
        '    private boolean followUpRecognitionActive = false;\n'
        '    private String latestFollowUpPartial = null;\n',
    ),
    (
        '        if (followUpListen) {\n'
        '            recognitionMode = RecognitionMode.TAP;\n'
        '            startListening();\n'
        '        }\n',
        '        if (followUpListen) {\n'
        '            followUpRecognitionActive = true;\n'
        '            latestFollowUpPartial = null;\n'
        '            recognitionMode = RecognitionMode.TAP;\n'
        '            startListening();\n'
        '        }\n',
    ),
    (
        '        if (wakeCoordinator != null) {\n'
        '            wakeCoordinator.onTapStarted();\n'
        '        }\n'
        '        recognitionMode = RecognitionMode.TAP;\n'
        '        startListening();\n'
        '    }\n\n'
        '    private void createRecognizer() {\n',
        '        followUpRecognitionActive = false;\n'
        '        latestFollowUpPartial = null;\n'
        '        if (wakeCoordinator != null) {\n'
        '            wakeCoordinator.onTapStarted();\n'
        '        }\n'
        '        recognitionMode = RecognitionMode.TAP;\n'
        '        startListening();\n'
        '    }\n\n'
        '    private void createRecognizer() {\n',
    ),
    (
        '        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);\n',
        '        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, followUpRecognitionActive);\n',
    ),
    (
        '        RecognitionMode stoppedMode = recognitionMode;\n'
        '        recognitionMode = RecognitionMode.NONE;\n'
        '        listening = false;\n',
        '        RecognitionMode stoppedMode = recognitionMode;\n'
        '        recognitionMode = RecognitionMode.NONE;\n'
        '        listening = false;\n'
        '        followUpRecognitionActive = false;\n'
        '        latestFollowUpPartial = null;\n',
    ),
    (
        '    public void onError(int error) {\n'
        '        RecognitionMode failedMode = recognitionMode;\n'
        '        recognitionMode = RecognitionMode.NONE;\n'
        '        listening = false;\n',
        '    public void onError(int error) {\n'
        '        RecognitionMode failedMode = recognitionMode;\n'
        '        boolean failedFollowUp = failedMode == RecognitionMode.TAP && followUpRecognitionActive;\n'
        '        String fallback = latestFollowUpPartial;\n'
        '        recognitionMode = RecognitionMode.NONE;\n'
        '        listening = false;\n'
        '        followUpRecognitionActive = false;\n'
        '        latestFollowUpPartial = null;\n',
    ),
    (
        '        if (failedMode == RecognitionMode.TAP) {\n'
        '            speak("Speech error " + error + ", " + speechErrorName(error) + ".");\n',
        '        if (failedFollowUp\n'
        '                && error == SpeechRecognizer.ERROR_NO_MATCH\n'
        '                && fallback != null\n'
        '                && !fallback.isBlank()) {\n'
        '            if (wakeCoordinator != null) {\n'
        '                wakeCoordinator.onTapFinished();\n'
        '            }\n'
        '            handleRecognizedSpeech(fallback);\n'
        '            return;\n'
        '        }\n\n'
        '        if (failedMode == RecognitionMode.TAP) {\n'
        '            speak("Speech error " + error + ", " + speechErrorName(error) + ".");\n',
    ),
    (
        '    public void onResults(Bundle results) {\n'
        '        RecognitionMode completedMode = recognitionMode;\n'
        '        recognitionMode = RecognitionMode.NONE;\n'
        '        listening = false;\n',
        '    public void onResults(Bundle results) {\n'
        '        RecognitionMode completedMode = recognitionMode;\n'
        '        recognitionMode = RecognitionMode.NONE;\n'
        '        listening = false;\n'
        '        followUpRecognitionActive = false;\n'
        '        latestFollowUpPartial = null;\n',
    ),
    (
        '    @Override public void onPartialResults(Bundle partialResults) { }\n',
        '    @Override\n'
        '    public void onPartialResults(Bundle partialResults) {\n'
        '        if (!followUpRecognitionActive || recognitionMode != RecognitionMode.TAP) {\n'
        '            return;\n'
        '        }\n'
        '        ArrayList<String> matches = partialResults.getStringArrayList(\n'
        '                SpeechRecognizer.RESULTS_RECOGNITION);\n'
        '        if (matches == null || matches.isEmpty()) {\n'
        '            return;\n'
        '        }\n'
        '        String candidate = matches.get(0);\n'
        '        if (candidate == null) {\n'
        '            return;\n'
        '        }\n'
        '        candidate = candidate.trim();\n'
        '        if (!candidate.isEmpty()) {\n'
        '            latestFollowUpPartial = candidate;\n'
        '        }\n'
        '    }\n',
    ),
]

for old, new in replacements:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'expected one match, found {count}: {old[:100]!r}')
    text = text.replace(old, new, 1)

path.write_text(text, encoding='utf-8')
