#!/usr/bin/env python3
from pathlib import Path

path = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java')
text = path.read_text(encoding='utf-8')

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

path.write_text(text, encoding='utf-8')
