#!/usr/bin/env python3
from pathlib import Path


def replace_once(path, old, new, label):
    p = Path(path)
    text = p.read_text(encoding='utf-8')
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'{label}: expected one anchor, found {count}')
    p.write_text(text.replace(old, new, 1), encoding='utf-8')


# Make the accepted-wake cue audible on the same user-controlled media volume path.
replace_once(
    'source/MainActivity.java',
    'tone = new ToneGenerator(AudioManager.STREAM_SYSTEM, 35);',
    'tone = new ToneGenerator(AudioManager.STREAM_MUSIC, 55);',
    'wake cue stream',
)

# Add wake sensitivity beneath Cadence in the existing Voice settings drawer.
anchor = '''        voiceSettingsOverlay.addView(cadenceSlider, cadenceParams);\n\n        Button done = new Button(this);'''
replacement = '''        voiceSettingsOverlay.addView(cadenceSlider, cadenceParams);\n\n        TextView wakeSensitivityLabel = voiceSettingLabel("Wake sensitivity", 22f, false);\n        voiceSettingsOverlay.addView(wakeSensitivityLabel);\n\n        SeekBar wakeSensitivitySlider = new SeekBar(this);\n        wakeSensitivitySlider.setMax(BoopWakeSensitivity.PROGRESS_MAX);\n        wakeSensitivitySlider.setProgress(BoopWakeSensitivity.loadProgress(this));\n        wakeSensitivitySlider.setContentDescription("Wake sensitivity");\n        wakeSensitivitySlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {\n            @Override\n            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {\n                if (fromUser) {\n                    BoopWakeSensitivity.saveProgress(MainActivity.this, progress);\n                }\n            }\n\n            @Override public void onStartTrackingTouch(SeekBar seekBar) { }\n            @Override public void onStopTrackingTouch(SeekBar seekBar) { }\n        });\n        LinearLayout.LayoutParams wakeSensitivityParams = new LinearLayout.LayoutParams(\n                LinearLayout.LayoutParams.MATCH_PARENT,\n                dp(64));\n        wakeSensitivityParams.setMargins(0, dp(4), 0, dp(28));\n        voiceSettingsOverlay.addView(wakeSensitivitySlider, wakeSensitivityParams);\n\n        Button done = new Button(this);'''
replace_once('source/MainActivity.java', anchor, replacement, 'wake sensitivity slider')

# Reload the local spotter while settings are still holding the wake engine disarmed.
anchor = '''        voiceSettingsOverlay = null;\n        voiceSettingsOpen = false;\n        if (wakeCoordinator != null) {\n            wakeCoordinator.setVoiceSettingsOpen(false);\n        }'''
replacement = '''        voiceSettingsOverlay = null;\n        voiceSettingsOpen = false;\n        if (wakeWordController != null) {\n            wakeWordController.reloadSensitivity();\n        }\n        if (wakeCoordinator != null) {\n            wakeCoordinator.setVoiceSettingsOpen(false);\n        }'''
replace_once('source/MainActivity.java', anchor, replacement, 'reload sensitivity before rearm')

# Do not reopen the eyes when the expected cancel callback follows a silence sleep.
anchor = '''        recognitionMode = RecognitionMode.NONE;\n        listening = false;\n        face.animate().alpha(1.0f).setDuration(120).start();\n\n        if (suppressNextRecognizerError) {\n            suppressNextRecognizerError = false;\n            return;\n        }'''
replacement = '''        recognitionMode = RecognitionMode.NONE;\n        listening = false;\n\n        if (suppressNextRecognizerError) {\n            suppressNextRecognizerError = false;\n            sleepFaceImmediately();\n            return;\n        }\n\n        face.animate().alpha(1.0f).setDuration(120).start();'''
replace_once('source/MainActivity.java', anchor, replacement, 'suppressed recognizer sleep race')

# Avoid the brief alpha restore immediately before cancelling a silent follow-up.
anchor = '''        recognitionMode = RecognitionMode.NONE;\n        listening = false;\n        if (face != null) {\n            face.animate().alpha(1.0f).setDuration(120).start();\n        }\n        if (recognizer != null) {\n            recognizer.cancel();\n        }'''
replacement = '''        recognitionMode = RecognitionMode.NONE;\n        listening = false;\n        if (recognizer != null) {\n            recognizer.cancel();\n        }'''
replace_once('source/MainActivity.java', anchor, replacement, 'silent follow-up pre-cancel alpha')

replace_once(
    'source/BoopSherpaWakeSpotter.java',
    'config.setKeywordsScore(1.5f);',
    'config.setKeywordsScore(BoopWakeSensitivity.keywordScore(context));',
    'persisted keyword score',
)

anchor = '''    void shutdown() {\n        suspendAll();'''
replacement = '''    void reloadSensitivity() {\n        suspendAll();\n        BoopSherpaWakeSpotter spotterToClose;\n        synchronized (lock) {\n            spotterToClose = spotter;\n            spotter = null;\n            preRoll = null;\n        }\n        if (spotterToClose != null) {\n            try {\n                spotterToClose.close();\n            } catch (Throwable error) {\n                Log.w(TAG, "Wake sensitivity reload failed", error);\n            }\n        }\n    }\n\n    void shutdown() {\n        suspendAll();'''
replace_once('source/BoopWakeWordController.java', anchor, replacement, 'wake sensitivity reload')

replace_once('source/app-build.gradle', 'versionCode 26', 'versionCode 27', 'version code')
replace_once(
    'source/app-build.gradle',
    'versionName "0.4.9-alpha6.5.3"',
    'versionName "0.4.9-alpha6.5.4"',
    'version name',
)
