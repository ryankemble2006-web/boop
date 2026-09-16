"""Final modal ownership fix after historical source materializers have run."""
from pathlib import Path
import runpy

ROOT = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1')

def replace_one(text, old, new):
    if text.count(old) != 1:
        raise SystemExit(f'Expected one single-face anchor: {old[:100]!r}')
    return text.replace(old,new,1)

main = ROOT / 'MainActivity.java'
text = main.read_text(encoding='utf-8')
if '// BOOP_SINGLE_FACE_OWNERSHIP_V148' not in text:
    text = replace_one(text, '        voiceSettingsOpen = true;',
        '        voiceSettingsOpen = true;\n        if (face != null) face.setOccluded("voice_settings", true);')
    text = replace_one(text, '        voiceSettingsOpen = false;\n        if (wakeCoordinator != null) {',
        '        voiceSettingsOpen = false;\n        if (face != null) face.setOccluded("voice_settings", false);\n        if (wakeCoordinator != null) {')
    text = replace_one(text, '    private void showDeveloperMenu() {',
        '    private void showDeveloperMenu() {\n        if (developerMenuOpen && developerMenuOverlay != null) {\n            showDeveloperMenuContent();\n            return;\n        }')
    text = replace_one(text, '        developerMenuOpen = true;',
        '        developerMenuOpen = true;\n        if (face != null) face.setOccluded("developer", true);')
    text = replace_one(text, '        developerMenuOpen = false;',
        '        developerMenuOpen = false;\n        if (face != null) face.setOccluded("developer", false);')
    start = text.index('    private void showDeveloperNotificationPreview(')
    end = text.index('    private void hideDeveloperMenu()', start)
    preview = text[start:end]
    redundant_start = preview.index('        developerMenuFace = new BoopCanonicalFaceView(this);')
    redundant_end = preview.index('        BoopNotificationPresentation presentation =', redundant_start)
    preview = preview[:redundant_start] + preview[redundant_end:]
    preview = replace_one(preview, '        puppet.setFaceVisible(false);\n', '')
    wake_start = preview.index('        developerMenuFace.post(() -> {')
    wake_end = preview.index('        });', wake_start) + len('        });\n')
    preview = preview[:wake_start] + preview[wake_end:]
    text = text[:start] + preview + text[end:]
    text += '\n// BOOP_SINGLE_FACE_OWNERSHIP_V148\n'
    main.write_text(text, encoding='utf-8')

puppet = ROOT / 'BoopNotificationPuppetView.java'
text = puppet.read_text(encoding='utf-8')
if 'com.boop.eyes.NotificationSignView' not in text:
    raise SystemExit('Notification stage is not Animation Lab canonical')

runpy.run_path('scripts/patch-unified-v200-voice-ui.py', run_name='__main__')
print('Single face ownership: voice hidden, one developer face, Animation Lab notifications')
