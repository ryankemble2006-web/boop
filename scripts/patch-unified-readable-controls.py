"""Adapt the fully assembled settings, after all inherited rows have been added."""
from pathlib import Path

path = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java')
text = path.read_text(encoding='utf-8')
anchor = '        voiceSettingsScroll.addView(voiceSettingsOverlay, new FrameLayout.LayoutParams('
assert text.count(anchor) == 1, 'Voice settings attachment changed'
text = text.replace(anchor, '        BoopReadableControls.fitText(voiceSettingsOverlay);\n' + anchor, 1)
path.write_text(text, encoding='utf-8')
print('Voice controls retain minimum targets and grow for enlarged text')
