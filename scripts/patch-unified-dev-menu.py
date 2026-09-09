#!/usr/bin/env python3
from pathlib import Path

path = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java")
text = path.read_text(encoding="utf-8")
spoken_marker = "// BOOP_DEVELOPER_MENU_SPOKEN_ENTRY_V2"
settings_marker = "// BOOP_DEVELOPER_MENU_SETTINGS_ENTRY_V2"
fields_marker = "// BOOP_DEVELOPER_MENU_IN_PLACE_FIELDS_V2"
methods_marker = "// BOOP_DEVELOPER_MENU_IN_PLACE_V2"
changed = False


def replace_once(source: str, old: str, new: str, label: str) -> str:
    count = source.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected one anchor, found {count}")
    return source.replace(old, new, 1)


if fields_marker not in text:
    anchor = "    private LinearLayout voiceSettingsOverlay;\n"
    block = '''    // BOOP_DEVELOPER_MENU_IN_PLACE_FIELDS_V2
    private FrameLayout developerMenuOverlay;
    private BoopFaceView developerMenuFace;
    private boolean developerMenuOpen = false;
'''
    text = replace_once(text, anchor, anchor + block, "Voice Settings field")
    changed = True

if spoken_marker not in text:
    anchor = '''        if (BoopVoiceSettingsIntent.matches(transcript)) {
            showVoiceSettings();
            return;
        }

'''
    block = '''        // BOOP_DEVELOPER_MENU_SPOKEN_ENTRY_V2
        if (BoopDevMenuIntent.matches(transcript)) {
            showDeveloperMenu();
            return;
        }

'''
    text = replace_once(text, anchor, anchor + block, "local voice-settings speech")
    changed = True

if settings_marker not in text:
    anchor = "        Button done = new Button(this);\n"
    block = '''        // BOOP_DEVELOPER_MENU_SETTINGS_ENTRY_V2
        Button devMenu = new Button(this);
        devMenu.setText("Developer menu");
        devMenu.setTextSize(19f);
        devMenu.setTextColor(Color.WHITE);
        devMenu.setBackgroundColor(Color.rgb(42, 42, 42));
        devMenu.setContentDescription("Open BOOP developer demos");
        devMenu.setOnClickListener(v -> {
            hideVoiceSettings();
            showDeveloperMenu();
        });
        LinearLayout.LayoutParams devMenuParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(64));
        devMenuParams.setMargins(0, 0, 0, dp(12));
        voiceSettingsOverlay.addView(devMenu, devMenuParams);

'''
    text = replace_once(text, anchor, block + anchor, "Voice Done button")
    changed = True

if methods_marker not in text:
    anchor = "    private TextView voiceSettingLabel(String text, float sizeSp, boolean bold) {\n"
    block = '''    // BOOP_DEVELOPER_MENU_IN_PLACE_V2
    private void showDeveloperMenu() {
        if (interactionSurface == null) {
            return;
        }
        if (wakeCoordinator != null) {
            wakeCoordinator.finishWakeProcessing();
        }
        if (voiceSettingsOpen) {
            hideVoiceSettings();
        }
        cancelMemberBerryHold();
        developerMenuOpen = true;
        developerMenuOverlay = new FrameLayout(this);
        developerMenuOverlay.setBackgroundColor(Color.BLACK);
        developerMenuOverlay.setClickable(true);
        developerMenuOverlay.setContentDescription("BOOP developer menu");
        interactionSurface.addView(developerMenuOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        developerMenuOverlay.bringToFront();
        showDeveloperMenuContent();
    }

    private void showDeveloperMenuContent() {
        if (!developerMenuOpen || developerMenuOverlay == null) {
            return;
        }
        developerMenuOverlay.removeAllViews();

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        column.setPadding(dp(28), dp(28), dp(28), dp(36));
        scroll.addView(column, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));
        developerMenuOverlay.addView(scroll, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        TextView title = voiceSettingLabel("BOOP Dev Lab", 30f, true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, dp(8));
        column.addView(title, titleParams);

        TextView subtitle = voiceSettingLabel("Local previews only", 16f, false);
        subtitle.setTextColor(Color.LTGRAY);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        subtitleParams.setMargins(0, 0, 0, dp(16));
        column.addView(subtitle, subtitleParams);

        developerMenuFace = new BoopFaceView(this);
        LinearLayout.LayoutParams faceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(230));
        faceParams.setMargins(0, 0, 0, dp(18));
        column.addView(developerMenuFace, faceParams);
        developerMenuFace.post(() -> {
            if (developerMenuFace == null || !developerMenuOpen) return;
            developerMenuFace.showIdleBlackImmediately();
            developerMenuFace.wakeFromIdle();
        });

        addDeveloperSection(column, "Animation");
        addDeveloperActionButton(column, "Wake", BoopDevMenuModel.Action.WAKE);
        addDeveloperActionButton(column, "Think", BoopDevMenuModel.Action.THINK);
        addDeveloperActionButton(column, "Stop", BoopDevMenuModel.Action.STOP);
        addDeveloperActionButton(column, "Berry 1", BoopDevMenuModel.Action.BERRY_1);
        addDeveloperActionButton(column, "Berry 2", BoopDevMenuModel.Action.BERRY_2);
        addDeveloperActionButton(column, "Berry 3", BoopDevMenuModel.Action.BERRY_3);
        addDeveloperActionButton(column, "Shake", BoopDevMenuModel.Action.SHAKE);
        addDeveloperActionButton(column, "Sleep", BoopDevMenuModel.Action.SLEEP);

        addDeveloperSection(column, "Notification doods");
        addDeveloperActionButton(column, "Facebook", BoopDevMenuModel.Action.NOTIFICATION_FACEBOOK);
        addDeveloperActionButton(column, "WhatsApp", BoopDevMenuModel.Action.NOTIFICATION_WHATSAPP);
        addDeveloperActionButton(column, "Gmail", BoopDevMenuModel.Action.NOTIFICATION_GMAIL);
        addDeveloperActionButton(column, "X / Twitter", BoopDevMenuModel.Action.NOTIFICATION_X);
        addDeveloperActionButton(column, "YouTube", BoopDevMenuModel.Action.NOTIFICATION_YOUTUBE);
        addDeveloperActionButton(column, "Messenger", BoopDevMenuModel.Action.NOTIFICATION_MESSENGER);
        addDeveloperActionButton(column, "Instagram", BoopDevMenuModel.Action.NOTIFICATION_INSTAGRAM);
        addDeveloperActionButton(column, "Discord", BoopDevMenuModel.Action.NOTIFICATION_DISCORD);
        addDeveloperActionButton(column, "Spotify", BoopDevMenuModel.Action.NOTIFICATION_SPOTIFY);
        addDeveloperActionButton(column, "Reddit", BoopDevMenuModel.Action.NOTIFICATION_REDDIT);
        addDeveloperActionButton(column, "Locked", BoopDevMenuModel.Action.NOTIFICATION_LOCKED);
        addDeveloperActionButton(column, "Bundle", BoopDevMenuModel.Action.NOTIFICATION_BUNDLE);

        Button done = new Button(this);
        done.setText("Done");
        done.setTextSize(21f);
        done.setTextColor(Color.WHITE);
        done.setBackgroundColor(Color.rgb(42, 42, 42));
        done.setOnClickListener(v -> hideDeveloperMenu());
        LinearLayout.LayoutParams doneParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(64));
        doneParams.setMargins(0, dp(14), 0, 0);
        column.addView(done, doneParams);
    }

    private void addDeveloperSection(LinearLayout column, String text) {
        TextView label = voiceSettingLabel(text, 20f, true);
        label.setGravity(Gravity.START);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(12), 0, dp(8));
        column.addView(label, params);
    }

    private void addDeveloperActionButton(
            LinearLayout column, String label, BoopDevMenuModel.Action action) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(18f);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(Color.rgb(42, 42, 42));
        button.setOnClickListener(v -> runDeveloperAction(action));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(60));
        params.setMargins(0, 0, 0, dp(8));
        column.addView(button, params);
    }

    private void runDeveloperAction(BoopDevMenuModel.Action action) {
        switch (action) {
            case WAKE:
                stopDeveloperAnimation();
                if (developerMenuFace != null) {
                    developerMenuFace.showIdleBlackImmediately();
                    developerMenuFace.wakeFromIdle();
                }
                return;
            case THINK:
                stopDeveloperAnimation();
                if (developerMenuFace != null) developerMenuFace.startThinking();
                return;
            case STOP:
                stopDeveloperAnimation();
                return;
            case BERRY_1:
                stopDeveloperAnimation();
                if (developerMenuFace != null) developerMenuFace.playMemberBerry(0);
                return;
            case BERRY_2:
                stopDeveloperAnimation();
                if (developerMenuFace != null) developerMenuFace.playMemberBerry(1);
                return;
            case BERRY_3:
                stopDeveloperAnimation();
                if (developerMenuFace != null) developerMenuFace.playMemberBerry(2);
                return;
            case SHAKE:
                stopDeveloperAnimation();
                if (developerMenuFace != null) developerMenuFace.playShakeMuppet(0.85f);
                return;
            case SLEEP:
                stopDeveloperAnimation();
                if (developerMenuFace != null) developerMenuFace.goIdleBlack();
                return;
            case NOTIFICATION_FACEBOOK:
            case NOTIFICATION_WHATSAPP:
            case NOTIFICATION_GMAIL:
            case NOTIFICATION_X:
            case NOTIFICATION_YOUTUBE:
            case NOTIFICATION_MESSENGER:
            case NOTIFICATION_INSTAGRAM:
            case NOTIFICATION_DISCORD:
            case NOTIFICATION_SPOTIFY:
            case NOTIFICATION_REDDIT:
            case NOTIFICATION_LOCKED:
            case NOTIFICATION_BUNDLE:
                showDeveloperNotificationPreview(action);
                return;
            default:
                return;
        }
    }

    private void stopDeveloperAnimation() {
        if (developerMenuFace != null) {
            developerMenuFace.stopThinking();
            developerMenuFace.animate().cancel();
        }
    }

    private void showDeveloperNotificationPreview(BoopDevMenuModel.Action action) {
        if (developerMenuOverlay == null) {
            return;
        }
        stopDeveloperAnimation();
        developerMenuFace = null;
        developerMenuOverlay.removeAllViews();
        BoopNotificationPresentation presentation =
                BoopDevNotificationPreview.presentation(action, System.currentTimeMillis());
        BoopNotificationPuppetView puppet = new BoopNotificationPuppetView(
                this,
                presentation,
                new BoopNotificationPuppetView.Callback() {
                    @Override public void onOpen(String notificationKey) {
                        showDeveloperMenuContent();
                    }
                    @Override public void onOpenBundle() {
                        showDeveloperMenuContent();
                    }
                    @Override public void onDismiss() {
                        showDeveloperMenuContent();
                    }
                });
        developerMenuOverlay.addView(puppet, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        Button back = new Button(this);
        back.setText("Back to developer menu");
        back.setTextSize(18f);
        back.setTextColor(Color.WHITE);
        back.setBackgroundColor(Color.rgb(42, 42, 42));
        back.setOnClickListener(v -> showDeveloperMenuContent());
        FrameLayout.LayoutParams backParams = new FrameLayout.LayoutParams(
                dp(280), dp(60), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        backParams.bottomMargin = dp(22);
        developerMenuOverlay.addView(back, backParams);
    }

    private void hideDeveloperMenu() {
        stopDeveloperAnimation();
        developerMenuFace = null;
        developerMenuOpen = false;
        if (interactionSurface != null && developerMenuOverlay != null) {
            interactionSurface.removeView(developerMenuOverlay);
        }
        developerMenuOverlay = null;
        wakeFaceForInteraction();
    }

'''
    text = replace_once(text, anchor, block + anchor, "voice label helper")
    changed = True

if changed:
    path.write_text(text, encoding="utf-8")
    print("In-place BOOP developer menu materialized")
else:
    print("In-place BOOP developer menu already materialized")
