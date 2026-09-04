#!/usr/bin/env python3
from pathlib import Path

path = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java')
text = path.read_text(encoding='utf-8')

field_anchor = '    private BoopWakeAudioSession wakeAudioSession;\n'
field_line = '    private final BoopWakeTranscriptAccumulator wakeTranscriptAccumulator = new BoopWakeTranscriptAccumulator();\n'
if field_line not in text:
    if field_anchor not in text:
        raise SystemExit('wake accumulator field anchor missing')
    text = text.replace(field_anchor, field_anchor + field_line, 1)

start_anchor = '        wakeAudioSession = session;\n'
start_line = '        wakeTranscriptAccumulator.reset();\n'
if start_line not in text:
    if start_anchor not in text:
        raise SystemExit('wake recognition start anchor missing')
    text = text.replace(start_anchor, start_anchor + start_line, 1)

error_anchor = '        if (failedMode == RecognitionMode.WAKE) {\n'
error_line = '            wakeTranscriptAccumulator.reset();\n'
if error_line not in text:
    if error_anchor not in text:
        raise SystemExit('wake error anchor missing')
    text = text.replace(error_anchor, error_anchor + error_line, 1)

result_anchor = '        if (completedMode == RecognitionMode.WAKE) {\n'
result_lines = (
    '            best = wakeTranscriptAccumulator.chooseFinal(best);\n'
    '            wakeTranscriptAccumulator.reset();\n'
)
if 'wakeTranscriptAccumulator.chooseFinal(best)' not in text:
    if result_anchor not in text:
        raise SystemExit('wake result anchor missing')
    text = text.replace(result_anchor, result_anchor + result_lines, 1)

partial_anchor = '    @Override public void onPartialResults(Bundle partialResults) { }\n'
partial_method = '''    @Override\n    public void onPartialResults(Bundle partialResults) {\n        if (recognitionMode != RecognitionMode.WAKE || partialResults == null) {\n            return;\n        }\n        ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);\n        if (matches != null && !matches.isEmpty()) {\n            wakeTranscriptAccumulator.rememberPartial(matches.get(0));\n        }\n    }\n'''
if 'wakeTranscriptAccumulator.rememberPartial' not in text:
    if partial_anchor not in text:
        raise SystemExit('wake partial-results anchor missing')
    text = text.replace(partial_anchor, partial_method, 1)

path.write_text(text, encoding='utf-8')
