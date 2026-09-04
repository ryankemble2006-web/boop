#!/usr/bin/env python3
from pathlib import Path

main_path = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java')
intent_path = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/BoopWakeRecognitionIntent.java')
text = main_path.read_text(encoding='utf-8')
intent = intent_path.read_text(encoding='utf-8')

import_anchor = 'import android.widget.TextView;\n'
if 'import android.widget.Toast;' not in text:
    if import_anchor not in text:
        raise SystemExit('diagnostic import anchor missing')
    text = text.replace(import_anchor, import_anchor + 'import android.widget.Toast;\n', 1)

error_anchor = '        if (failedMode == RecognitionMode.WAKE) {\n'
error_line = '            Toast.makeText(this, "WAKE ERR " + error + " " + speechErrorName(error), Toast.LENGTH_LONG).show();\n'
if error_line not in text:
    if error_anchor not in text:
        raise SystemExit('wake error anchor missing')
    text = text.replace(error_anchor, error_anchor + error_line, 1)

result_anchor = '        if (completedMode == RecognitionMode.WAKE) {\n'
result_line = '            Toast.makeText(this, "WAKE HEARD " + (best == null ? "<nothing>" : best), Toast.LENGTH_LONG).show();\n'
if result_line not in text:
    if result_anchor not in text:
        raise SystemExit('wake result anchor missing')
    text = text.replace(result_anchor, result_anchor + result_line, 1)

partial_anchor = '    @Override public void onPartialResults(Bundle partialResults) { }\n'
partial_replacement = '''    @Override\n    public void onPartialResults(Bundle partialResults) {\n        if (recognitionMode != RecognitionMode.WAKE) {\n            return;\n        }\n        ArrayList<String> partialMatches =\n                partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);\n        String partialBest = null;\n        if (partialMatches != null && !partialMatches.isEmpty()) {\n            String candidate = partialMatches.get(0).trim();\n            if (!candidate.isEmpty()) {\n                partialBest = candidate;\n            }\n        }\n        Toast.makeText(this, "WAKE PART " + (partialBest == null ? "<nothing>" : partialBest),\n                Toast.LENGTH_LONG).show();\n    }\n'''
if 'WAKE PART ' not in text:
    if partial_anchor not in text:
        raise SystemExit('partial-results anchor missing')
    text = text.replace(partial_anchor, partial_replacement, 1)

partial_false = 'intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);'
partial_true = 'intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);'
if partial_true not in intent:
    if partial_false not in intent:
        raise SystemExit('wake partial-results intent anchor missing')
    intent = intent.replace(partial_false, partial_true, 1)

main_path.write_text(text, encoding='utf-8')
intent_path.write_text(intent, encoding='utf-8')
