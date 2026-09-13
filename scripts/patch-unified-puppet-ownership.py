#!/usr/bin/env python3
"""Single visible puppet owner after the existing materialization passes."""
from pathlib import Path
ROOT = Path('boop-build/BOOP-Alpha1')
MAIN = ROOT / 'app/src/main/java/com/boop/alpha1'
path = MAIN / 'MainActivity.java'
text = path.read_text(encoding='utf-8')
def change(old, new, label):
    global text
    assert text.count(old) == 1, (label, text.count(old))
    text = text.replace(old, new, 1)
change('    private BoopCanonicalFaceView face;', '    private BoopCanonicalFaceView face;\n    private final Object voiceFaceCover = new Object();\n    private final Object developerFaceCover = new Object();', 'cover owners')
change('        voiceSettingsOpen = true;', '        voiceSettingsOpen = true;\n        if (face != null) face.setCovered(voiceFaceCover, true);', 'voice cover')
change('        wakeNameTrainingStatus = null;\n        voiceSettingsOpen = false;', '        wakeNameTrainingStatus = null;\n        voiceSettingsOpen = false;\n        if (face != null) face.setCovered(voiceFaceCover, false);', 'voice release')
change('        developerMenuOpen = true;', '        developerMenuOpen = true;\n        if (face != null) face.setCovered(developerFaceCover, true);', 'developer cover')
change('        developerMenuOpen = false;', '        developerMenuOpen = false;\n        if (face != null) face.setCovered(developerFaceCover, false);', 'developer release')
old = '''        developerMenuFace = new BoopCanonicalFaceView(this);
        LinearLayout.LayoutParams faceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f);
        preview.addView(developerMenuFace, faceParams);

'''
change(old, '', 'remove separate notification preview eyes')
change('        puppet.setFaceVisible(false);', '        // The notification scene owns its eye pair and sign together.', 'single notification owner')
old = '''        preview.addView(dismiss, dismissParams);

        developerMenuFace.post(() -> {
            if (!developerMenuOpen || developerMenuFace == null) {
                return;
            }
            developerMenuFace.showIdleBlackImmediately();
            developerMenuFace.wakeFromIdle();
        });'''
change(old, '        preview.addView(dismiss, dismissParams);', 'remove obsolete preview wake')
assert "onPostResume()" not in text
at=text.index("    private void showVoiceSettings() {")
method="    @Override protected void onPostResume() {\n        super.onPostResume();\n        String action=getIntent()==null?null:getIntent().getAction();\n        if (\"com.boop.alpha1.OPEN_VOICE_SETTINGS\".equals(action)) { getIntent().setAction(null); interactionSurface.post(this::showVoiceSettings); }\n        else if (\"com.boop.alpha1.OPEN_DEVELOPER_MENU\".equals(action)) { getIntent().setAction(null); interactionSurface.post(this::showDeveloperMenu); }\n    }\n\n"
text=text[:at]+method+text[at:]
change('        voiceSettingsOverlay.addView(title, titleParams);', '        voiceSettingsOverlay.addView(title, titleParams);\n' + '        Button characterControls = new Button(this);\n        characterControls.setText("Puppet controls");\n        characterControls.setOnClickListener(v -> { hideVoiceSettings(); startActivity(new Intent(this, BoopPuppetSettingsActivity.class)); });\n        voiceSettingsOverlay.addView(characterControls);\n', 'puppet controls entry')
path.write_text(text, encoding='utf-8')
print('Single-owner voice, developer and notification surfaces materialized')
