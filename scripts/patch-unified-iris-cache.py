#!/usr/bin/env python3
"""Replace the existing generated hue setter; preserve all art and animation methods."""
from pathlib import Path

face = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/BoopFaceView.java')
text = face.read_text(encoding='utf-8')
field = '    private Bitmap faceBitmap;\n'
if 'private BoopIrisTint irisTint;' not in text:
    if text.count(field) != 1:
        raise SystemExit('Expected original-preserving hue bitmap field')
    text = text.replace(field, field + '    private BoopIrisTint irisTint;\n', 1)
signature = '    void setEyeHueDegrees(int hueDegrees) {'
if text.count(signature) != 1:
    raise SystemExit('Expected one materialized hue setter')
start = text.index(signature)
opening = text.index('{', start)
end = opening + 1
depth = 1
while depth and end < len(text):
    depth += (text[end] == '{') - (text[end] == '}')
    end += 1
if depth:
    raise SystemExit('Unclosed hue setter')
replacement = '''    void setEyeHueDegrees(int hueDegrees) {
        if (originalFaceBitmap == null) return;
        if (irisTint == null) irisTint = new BoopIrisTint(originalFaceBitmap);
        faceBitmap = irisTint.forHue(hueDegrees);
        paint.setColorFilter(null);
        invalidate();
    }'''
text = text[:start] + replacement + text[end:]
face.write_text(text, encoding='utf-8')
print('Iris-only cached renderer integrated; original art retained')
