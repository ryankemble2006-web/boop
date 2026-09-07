#!/usr/bin/env python3
"""Apply the approved Wall UI delta during the existing materialization pipeline.

The checked-in Wall baseline remains intact, as with the existing wake and toast
patches. Every replacement must match exactly once. No partial file is written
when a source anchor changes. This module is also exercised by source/JVM/CI tests.
"""
from pathlib import Path
import sys

TARGET = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java')
MARKER = '    // BOOP_CHAT_MODE_V1: local control is never switched to a web provider.\n'

OLD_TOUCH = '''    private boolean onFaceTouch(View view, MotionEvent event) {
        if (voiceSettingsOpen) {
            return true;
        }
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            memberBerryConsumed = false;
            faceTouchDownX = event.getX();
            faceTouchDownY = event.getY();
            swipeHadMultiplePointers = false;
            wakeFaceForInteraction();
            scheduleMemberBerryHold();
            return true;
        }
        if (action == MotionEvent.ACTION_POINTER_DOWN) {
            swipeHadMultiplePointers = true;
            cancelMemberBerryHold();
            return true;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            float movedX = Math.abs(event.getX() - faceTouchDownX);
            float movedY = Math.abs(event.getY() - faceTouchDownY);
            if (movedX > ViewConfiguration.get(this).getScaledTouchSlop()
                    || movedY > ViewConfiguration.get(this).getScaledTouchSlop()) {
                cancelMemberBerryHold();
            }
            return true;
        }
        if (action == MotionEvent.ACTION_CANCEL) {
            cancelMemberBerryHold();
            memberBerryConsumed = false;
            swipeHadMultiplePointers = false;
            return true;
        }
        if (action != MotionEvent.ACTION_UP) {
            return true;
        }

        cancelMemberBerryHold();
        if (memberBerryConsumed) {
            memberBerryConsumed = false;
            return true;
        }

        if (BoopLauncherSwipeGesture.shouldOpenLauncher(
                faceTouchDownX, faceTouchDownY, event.getX(), event.getY(), false,
                swipeHadMultiplePointers, dp(96))) {
            swipeHadMultiplePointers = false;
            launchLauncher();
            return true;
        }
        swipeHadMultiplePointers = false;
        if (listening) {
            stopListening();
        } else {
            beginTapToSpeak();
        }
        return true;
    }
'''

NEW_TOUCH = '''    private boolean onFaceTouch(View view, MotionEvent event) {
        if (voiceSettingsOpen || chatModeOpen) return true;
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            cancelFaceHolds();
            faceTouchActive = true;
            faceGestureMoved = false;
            memberBerryConsumed = false;
            swipeHadMultiplePointers = false;
            faceTouchDownX = event.getX();
            faceTouchDownY = event.getY();
            wakeFaceForInteraction();
            scheduleMemberBerryHold();
            chatModeHold.begin(android.os.SystemClock.uptimeMillis(),
                    faceTouchDownX, faceTouchDownY,
                    ViewConfiguration.get(this).getScaledTouchSlop());
            if (presenceHandler != null) {
                presenceHandler.postDelayed(chatModeHoldRunnable, BoopChatModeHold.HOLD_MS);
            }
            return true;
        }
        if (action == MotionEvent.ACTION_POINTER_DOWN) {
            swipeHadMultiplePointers = true;
            cancelFaceHolds();
            return true;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            chatModeHold.move(event.getX(), event.getY(), event.getPointerCount());
            float movedX = Math.abs(event.getX() - faceTouchDownX);
            float movedY = Math.abs(event.getY() - faceTouchDownY);
            if (event.getPointerCount() != 1) swipeHadMultiplePointers = true;
            if (swipeHadMultiplePointers
                    || movedX > ViewConfiguration.get(this).getScaledTouchSlop()
                    || movedY > ViewConfiguration.get(this).getScaledTouchSlop()) {
                faceGestureMoved = true;
                cancelFaceHolds();
            }
            return true;
        }
        if (action == MotionEvent.ACTION_CANCEL) {
            cancelFaceHolds();
            faceTouchActive = false;
            memberBerryConsumed = false;
            swipeHadMultiplePointers = false;
            return true;
        }
        if (action != MotionEvent.ACTION_UP) return true;
        cancelFaceHolds();
        if (!faceTouchActive) return true;
        faceTouchActive = false;
        if (memberBerryConsumed) {
            memberBerryConsumed = false;
            return true;
        }
        if (BoopLauncherSwipeGesture.shouldOpenLauncher(
                faceTouchDownX, faceTouchDownY, event.getX(), event.getY(), false,
                swipeHadMultiplePointers, dp(96))) {
            swipeHadMultiplePointers = false;
            launchLauncher();
            return true;
        }
        boolean ignoreRelease = swipeHadMultiplePointers || faceGestureMoved;
        swipeHadMultiplePointers = false;
        if (ignoreRelease) return true;
        if (listening) stopListening();
        else beginTapToSpeak();
        return true;
    }
'''

FIELDS = MARKER + '''    private BoopChatModeStore chatModeStore;
    private AlertDialog chatModeDialog;
    private boolean chatModeOpen;
    private boolean activityInForeground;
    private volatile int chatModeRevision;
    private boolean faceTouchActive;
    private boolean faceGestureMoved;
    private final BoopChatModeHold chatModeHold = new BoopChatModeHold();
    private final Runnable chatModeHoldRunnable = () -> {
        if (!activityInForeground || !faceTouchActive || voiceSettingsOpen || chatModeOpen
                || isFinishing() || isDestroyed()) return;
        if (chatModeHold.tryOpen(android.os.SystemClock.uptimeMillis())) {
            memberBerryConsumed = true;
            cancelMemberBerryHold();
            showChatModeMenu();
        }
    };
'''

METHODS = '''    private void cancelFaceHolds() {
        cancelMemberBerryHold();
        chatModeHold.cancel();
        if (presenceHandler != null) presenceHandler.removeCallbacks(chatModeHoldRunnable);
    }

    private void showChatModeMenu() {
        if (chatModeOpen || voiceSettingsOpen || !activityInForeground || chatModeStore == null) return;
        cancelFaceHolds();
        memberBerryConsumed = true;
        chatModeOpen = true;
        assistantFollowUpAfterTts = false;
        sleepFaceAfterTts = false;
        cancelAssistantFollowUpSilenceTimeout();
        if (wakeCoordinator != null) wakeCoordinator.setVoiceSettingsOpen(true);
        if (listening) {
            suppressNextRecognizerError = true;
            stopListening();
        }
        if (tts != null) tts.stop();
        interactionSurface.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

        LinearLayout choices = new LinearLayout(this);
        choices.setOrientation(LinearLayout.VERTICAL);
        choices.setPadding(dp(20), dp(8), dp(20), dp(8));
        choices.addView(chatModeButton(BoopChatMode.OPENCODE, "OpenCode"));
        choices.addView(chatModeButton(BoopChatMode.FREE_CHAT, "Free Chat"));
        TextView explanation = voiceSettingLabel(
                "Free Chat opens ChatGPT in your browser and copies your question for you to paste. Its own limits apply.",
                17f, false);
        explanation.setPadding(0, dp(14), 0, dp(8));
        choices.addView(explanation);
        android.widget.ScrollView scroll = new android.widget.ScrollView(this);
        scroll.addView(choices);
        chatModeDialog = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle("Chat mode")
                .setView(scroll)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create();
        chatModeDialog.setOnDismissListener(dialog -> {
            chatModeDialog = null;
            chatModeOpen = false;
            if (wakeCoordinator != null) wakeCoordinator.setVoiceSettingsOpen(voiceSettingsOpen);
            if (activityInForeground) wakeFaceForInteraction();
        });
        chatModeDialog.show();
    }

    private Button chatModeButton(BoopChatMode mode, String label) {
        Button button = new Button(this);
        button.setAllCaps(false);
        boolean selected = chatModeStore.load() == mode;
        button.setText(selected ? label + "  \\u2713" : label);
        button.setTextSize(22f);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(selected ? Color.rgb(28, 76, 86) : Color.rgb(42, 42, 42));
        button.setContentDescription("Use " + label);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(64));
        params.setMargins(0, dp(6), 0, dp(6));
        button.setLayoutParams(params);
        button.setOnClickListener(view -> {
            BoopChatMode previous = chatModeStore.load();
            if (!chatModeStore.save(mode)) {
                Toast.makeText(this, "That choice didn't save. Try again.", Toast.LENGTH_LONG).show();
                return;
            }
            if (previous != mode) chatModeRevision++;
            stopAssistantThinking();
            if (chatModeDialog != null) chatModeDialog.dismiss();
            Toast.makeText(this, label + " selected.", Toast.LENGTH_SHORT).show();
        });
        return button;
    }

    private void openFreeChat(String question) {
        cancelFaceHolds();
        assistantFollowUpAfterTts = false;
        sleepFaceAfterTts = false;
        cancelAssistantFollowUpSilenceTimeout();
        stopAssistantThinking();
        if (wakeCoordinator != null) {
            wakeCoordinator.setVoiceSettingsOpen(true);
            wakeCoordinator.finishWakeProcessing();
        }
        if (!BoopFreeChat.open(this, question)) {
            if (wakeCoordinator != null) wakeCoordinator.setVoiceSettingsOpen(false);
            speak("I couldn't open Free Chat. Please check that a browser is installed.");
        }
    }

'''


def replace_once(text, old, new):
    count = text.count(old)
    if count != 1:
        raise ValueError(f'chat mode patch: expected one source anchor, found {count}: {old[:85]!r}')
    return text.replace(old, new, 1)


def patch_text(text):
    if MARKER in text:
        for fragment in (FIELDS, NEW_TOUCH, METHODS, 'commandRouter.process(transcript, () ->',
                         'openFreeChat(transcript);', 'chatModeStore = new BoopChatModeStore(this);'):
            if text.count(fragment) != 1:
                raise ValueError('chat mode patch: incomplete or altered previously patched source')
        return text
    text = replace_once(text, '    private boolean swipeHadMultiplePointers = false;\n',
                        '    private boolean swipeHadMultiplePointers = false;\n' + FIELDS)
    text = replace_once(text, '        presenceHandler = new Handler(Looper.getMainLooper());\n',
                        '        chatModeStore = new BoopChatModeStore(this);\n'
                        '        presenceHandler = new Handler(Looper.getMainLooper());\n')
    text = replace_once(text, '        super.onResume();\n',
                        '        super.onResume();\n        activityInForeground = true;\n'
                        '        if (wakeCoordinator != null) wakeCoordinator.setVoiceSettingsOpen(voiceSettingsOpen || chatModeOpen);\n')
    text = replace_once(text, '    protected void onPause() {\n',
                        '    protected void onPause() {\n        activityInForeground = false;\n'
                        '        cancelFaceHolds();\n        faceTouchActive = false;\n'
                        '        if (chatModeDialog != null) chatModeDialog.dismiss();\n')
    text = replace_once(text, OLD_TOUCH, NEW_TOUCH)
    text = replace_once(text, '    private void launchLauncher() {\n', METHODS + '    private void launchLauncher() {\n')
    text = replace_once(text, '        if (listening || thinking || voiceSettingsOpen) {\n',
                        '        if (listening || thinking || voiceSettingsOpen || chatModeOpen) {\n')
    text = replace_once(text,
                        '        if (interactionSurface == null || face == null || listening || voiceSettingsOpen) {\n',
                        '        if (interactionSurface == null || face == null || listening || voiceSettingsOpen || chatModeOpen) {\n')
    text = replace_once(text,
                        '        if (interactionSurface == null || voiceController == null || voiceSettingsOpen) {\n',
                        '        if (interactionSurface == null || voiceController == null || voiceSettingsOpen || chatModeOpen) {\n')
    text = replace_once(text,
                        '        executor.execute(() -> {\n            HomeAssistantDeviceSetup.SetupResult setup = tokenStore.hasHaDeviceIdentity()\n',
                        '        final BoopChatMode requestChatMode = chatModeStore.load();\n'
                        '        final int requestChatRevision = chatModeRevision;\n'
                        '        executor.execute(() -> {\n            HomeAssistantDeviceSetup.SetupResult setup = tokenStore.hasHaDeviceIdentity()\n')
    text = replace_once(text,
                        '            CommandOutcome outcome = commandRouter.process(transcript);\n            runOnUiThread(() -> {\n',
                        '            CommandOutcome outcome = commandRouter.process(transcript, () ->\n'
                        '                    requestChatRevision == chatModeRevision && chatModeStore.load() == BoopChatMode.OPENCODE);\n'
                        '            runOnUiThread(() -> {\n'
                        '                if (!activityInForeground || chatModeOpen || isFinishing() || isDestroyed()\n'
                        '                        || requestChatRevision != chatModeRevision) return;\n'
                        '                if (requestChatMode == BoopChatMode.FREE_CHAT\n'
                        '                        && outcome.status() == CommandOutcome.Status.NO_MATCH) {\n'
                        '                    openFreeChat(transcript);\n                    return;\n                }\n')
    text = replace_once(text, '        super.onConfigurationChanged(newConfig);\n',
                        '        super.onConfigurationChanged(newConfig);\n        cancelFaceHolds();\n        faceTouchActive = false;\n')
    text = replace_once(text, '        super.onWindowFocusChanged(hasFocus);\n',
                        '        super.onWindowFocusChanged(hasFocus);\n'
                        '        if (!hasFocus) {\n            cancelFaceHolds();\n            faceTouchActive = false;\n        }\n')
    text = replace_once(text, '    public void onResults(Bundle results) {\n',
                        '    public void onResults(Bundle results) {\n'
                        '        if (chatModeOpen || !activityInForeground) {\n'
                        '            suppressNextRecognizerError = true;\n'
                        '            stopListening();\n            return;\n        }\n')
    text = replace_once(text, '    protected void onDestroy() {\n',
                        '    protected void onDestroy() {\n        activityInForeground = false;\n'
                        '        cancelFaceHolds();\n        if (chatModeDialog != null) chatModeDialog.dismiss();\n')
    return text


if __name__ == '__main__':
    path = Path(sys.argv[1]) if len(sys.argv) > 1 else TARGET
    original = path.read_text(encoding='utf-8')
    result = patch_text(original)
    temporary = path.with_suffix('.java.chat-mode-tmp')
    temporary.write_text(result, encoding='utf-8')
    temporary.replace(path)
