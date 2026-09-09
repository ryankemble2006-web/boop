#!/usr/bin/env python3
from pathlib import Path

ROOT = Path('boop-build/BOOP-Alpha1')
MAIN = ROOT / 'app/src/main/java/com/boop/alpha1/MainActivity.java'
MANIFEST = ROOT / 'app/src/main/AndroidManifest.xml'
SETTINGS = ROOT / 'shield-lib/src/main/java/com/boop/shieldoverlay/TvSettingsView.java'


def once(text, old, new, label):
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'{label}: expected one anchor, found {count}')
    return text.replace(old, new, 1)


text = MAIN.read_text(encoding='utf-8')
text = once(text,
    '    private boolean pendingDiscoveryAfterPermission = false;\n',
    '    private boolean pendingDiscoveryAfterPermission = false;\n'
    '    private boolean assistantOneShot = false;\n'
    '    private boolean assistantOneShotStarted = false;\n',
    'assistant state fields')
text = once(text,
    '        handleAuthIntent(getIntent());\n    }\n',
    '        handleAuthIntent(getIntent());\n'
    '        acceptAssistantIntent(getIntent());\n'
    '    }\n',
    'assistant initial intent')
text = once(text,
    '        setIntent(intent);\n        handleAuthIntent(intent);\n    }\n',
    '        setIntent(intent);\n'
    '        handleAuthIntent(intent);\n'
    '        acceptAssistantIntent(intent);\n'
    '        maybeStartAssistantOneShot();\n'
    '    }\n',
    'assistant new intent')
text = once(text,
    '                checkWakeRecognitionSupport();\n            }\n        }\n    }\n',
    '                checkWakeRecognitionSupport();\n'
    '            }\n'
    '        }\n'
    '        maybeStartAssistantOneShot();\n'
    '    }\n',
    'assistant resume handoff')
text = once(text,
    '    private void beginTapToSpeak() {\n',
    '''    private void acceptAssistantIntent(Intent intent) {
        if (intent == null || !intent.getBooleanExtra(
                BoopVoiceInteractionSession.EXTRA_ONE_SHOT_ASSIST, false)) {
            return;
        }
        assistantOneShot = true;
        assistantOneShotStarted = false;
        int inputDeviceId = intent.getIntExtra(Intent.EXTRA_ASSIST_INPUT_DEVICE_ID, -1);
        android.util.Log.i("BOOP-Assist", "Assistant one-shot requested; inputDeviceId=" + inputDeviceId);
    }

    private void maybeStartAssistantOneShot() {
        if (!assistantOneShot || assistantOneShotStarted || voiceSettingsOpen || listening || thinking) {
            return;
        }
        assistantOneShotStarted = true;
        beginTapToSpeak();
    }

    private void finishAssistantOneShot() {
        if (!assistantOneShot) return;
        assistantOneShot = false;
        assistantOneShotStarted = false;
        assistantFollowUpAfterTts = false;
        assistantFollowUpListening = false;
        finish();
        overridePendingTransition(0, 0);
    }

    private void beginTapToSpeak() {
''',
    'assistant one-shot helpers')
text = once(text,
    '        if (sleepAfterTts) {\n            sleepFaceImmediately();\n            return;\n        }\n',
    '        if (assistantOneShot) {\n'
    '            finishAssistantOneShot();\n'
    '            return;\n'
    '        }\n'
    '        if (sleepAfterTts) {\n'
    '            sleepFaceImmediately();\n'
    '            return;\n'
    '        }\n',
    'assistant completes after response')
MAIN.write_text(text, encoding='utf-8')

text = MANIFEST.read_text(encoding='utf-8')
assistant_marker = 'android:name=".BoopVoiceInteractionService"'
if assistant_marker not in text:
    app_close = '    </application>'
    components = '''        <activity
            android:name=".BoopAssistantSetupActivity"
            android:exported="false"
            android:theme="@style/Theme.BOOP" />
        <service
            android:name=".BoopVoiceInteractionService"
            android:exported="true"
            android:permission="android.permission.BIND_VOICE_INTERACTION">
            <intent-filter>
                <action android:name="android.service.voice.VoiceInteractionService" />
            </intent-filter>
            <meta-data
                android:name="android.voice_interaction"
                android:resource="@xml/boop_voice_interaction_service" />
        </service>
        <service
            android:name=".BoopVoiceInteractionSessionService"
            android:exported="true"
            android:permission="android.permission.BIND_VOICE_INTERACTION" />
'''
    text = once(text, app_close, components + app_close, 'assistant application close')
else:
    for marker in (
            'android:name=".BoopAssistantSetupActivity"',
            'android:name=".BoopVoiceInteractionService"',
            'android:name=".BoopVoiceInteractionSessionService"'):
        if text.count(marker) != 1:
            raise SystemExit(f'assistant manifest components: expected one {marker}, found {text.count(marker)}')
MANIFEST.write_text(text, encoding='utf-8')

xml = ROOT / 'app/src/main/res/xml/boop_voice_interaction_service.xml'
xml.parent.mkdir(parents=True, exist_ok=True)
xml.write_text('''<?xml version="1.0" encoding="utf-8"?>
<voice-interaction-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:sessionService="com.boop.alpha1.BoopVoiceInteractionSessionService"
    android:supportsAssist="true"
    android:supportsLaunchVoiceAssistFromKeyguard="false"
    android:supportsLocalInteraction="true" />
''', encoding='utf-8')

text = SETTINGS.read_text(encoding='utf-8')
text = once(text, 'import android.content.Context;\n', 'import android.content.Context;\nimport android.content.Intent;\n', 'settings Intent import')
old = 'content.addView(section("VOICE")); wakeNameCard=card("BOOP\'s name",wakeName(),"Spoken wake name only. BOOP always works too.",left); wakeNameCard.setOnClickListener(v->showWakeNameDialog()); content.addView(wakeNameCard,spaced());\n'
new = old + '        SettingCard micButton=card("Shield microphone button","Choose assistant","Use BOOP or keep the current Android assistant",left); micButton.setOnClickListener(v->{try{getContext().startActivity(new Intent().setClassName(getContext().getPackageName(),"com.boop.alpha1.BoopAssistantSetupActivity"));}catch(RuntimeException ignored){}}); content.addView(micButton,spaced());\n'
text = once(text, old, new, 'reversible assistant setting')
SETTINGS.write_text(text, encoding='utf-8')

print('Official Android assistant role/session integration materialized; no device or visual checks run')
