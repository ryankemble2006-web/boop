#!/usr/bin/env python3
from pathlib import Path

path = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java")
text = path.read_text(encoding="utf-8")
spoken_marker = "// BOOP_DEVELOPER_MENU_SPOKEN_ENTRY_V4"
settings_marker = "// BOOP_DEVELOPER_MENU_SETTINGS_ENTRY_V4"
fields_marker = "// BOOP_DEVELOPER_MENU_IN_PLACE_FIELDS_V4"
methods_marker = "// BOOP_DEVELOPER_MENU_IN_PLACE_V4"
changed = False


def replace_once(source: str, old: str, new: str, label: str) -> str:
    count = source.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected one anchor, found {count}")
    return source.replace(old, new, 1)


if "import android.widget.HorizontalScrollView;\n" not in text:
    anchor = "import android.widget.FrameLayout;\n"
    text = replace_once(
        text,
        anchor,
        anchor + "import android.widget.HorizontalScrollView;\n",
        "HorizontalScrollView import",
    )
    changed = True

if fields_marker not in text:
    anchor = "    private LinearLayout voiceSettingsOverlay;\n"
    block = '''    // BOOP_DEVELOPER_MENU_IN_PLACE_FIELDS_V4
    private FrameLayout developerMenuOverlay;
    private BoopFaceView developerMenuFace;
    private boolean developerMenuOpen = false;
    private int developerAnimationScrollX = 0;
    private int developerNotificationScrollX = 0;
'''
    text = replace_once(text, anchor, anchor + block, "Voice Settings field")
    changed = True

if spoken_marker not in text:
    anchor = '''        if (BoopVoiceSettingsIntent.matches(transcript)) {
            showVoiceSettings();
            return;
        }

'''
    block = '''        // BOOP_DEVELOPER_MENU_SPOKEN_ENTRY_V4
        if (BoopDevMenuIntent.matches(transcript)) {
            showDeveloperMenu();
            return;
        }

'''
    text = replace_once(text, anchor, anchor + block, "local voice-settings speech")
    changed = True

if settings_marker not in text:
    anchor = "        Button done = new Button(this);\n"
    block = '''        // BOOP_DEVELOPER_MENU_SETTINGS_ENTRY_V4
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
    block = '''    // BOOP_DEVELOPER_MENU_IN_PLACE_V4
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
        stopDeveloperAnimation();
        developerMenuFace = null;
        developerMenuOverlay.removeAllViews();

        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        column.setBackgroundColor(Color.BLACK);
        column.setPadding(dp(24), dp(14), dp(24), dp(14));
        developerMenuOverlay.addView(column, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        TextView title = voiceSettingLabel("BOOP Dev Lab", 26f, true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, dp(4));
        column.addView(title, titleParams);

        developerMenuFace = new BoopFaceView(this);
        LinearLayout.LayoutParams faceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f);
        faceParams.setMargins(0, 0, 0, dp(4));
        column.addView(developerMenuFace, faceParams);
        developerMenuFace.post(() -> {
            if (!developerMenuOpen || developerMenuFace == null) {
                return;
            }
            developerMenuFace.showIdleBlackImmediately();
            developerMenuFace.wakeFromIdle();
        });

        for (BoopDevMenuModel.Shelf shelf : BoopDevMenuModel.shelves()) {
            boolean animationShelf = "Animations".equals(shelf.title());
            addDeveloperShelf(
                    column,
                    animationShelf ? "Animations" : "Notification doods",
                    shelf,
                    animationShelf);
        }

        Button done = new Button(this);
        done.setText("Done");
        done.setTextSize(20f);
        done.setTextColor(Color.WHITE);
        done.setBackgroundColor(Color.rgb(42, 42, 42));
        done.setOnClickListener(v -> hideDeveloperMenu());
        LinearLayout.LayoutParams doneParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(56));
        doneParams.setMargins(0, dp(4), 0, 0);
        column.addView(done, doneParams);
    }

    private void addDeveloperShelf(
            LinearLayout column,
            String title,
            BoopDevMenuModel.Shelf shelf,
            boolean animationShelf) {
        addDeveloperSection(column, title);

        HorizontalScrollView shelfScroll = new HorizontalScrollView(this);
        shelfScroll.setHorizontalScrollBarEnabled(false);
        shelfScroll.setFillViewport(false);
        shelfScroll.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        shelfScroll.setContentDescription(title + " horizontal selector");

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, dp(18), 0);

        for (BoopDevMenuModel.Item item : shelf.items()) {
            final String label = item.label();
            final BoopDevMenuModel.Action action = item.action();
            Button button = new Button(this);
            button.setText(label);
            button.setTextSize(18f);
            button.setTextColor(Color.WHITE);
            button.setBackgroundColor(Color.rgb(42, 42, 42));
            button.setAllCaps(false);
            button.setContentDescription((animationShelf ? "Run " : "Preview ") + label);
            button.setOnClickListener(v -> {
                if (animationShelf) {
                    runDeveloperAction(action);
                } else {
                    showDeveloperNotificationPreview(action);
                }
            });
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    dp(152),
                    dp(64));
            itemParams.setMargins(0, 0, dp(12), 0);
            row.addView(button, itemParams);
        }

        shelfScroll.addView(row, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));

        int savedScrollX = animationShelf
                ? developerAnimationScrollX
                : developerNotificationScrollX;
        shelfScroll.setOnScrollChangeListener((view, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (animationShelf) {
                developerAnimationScrollX = scrollX;
            } else {
                developerNotificationScrollX = scrollX;
            }
        });
        shelfScroll.post(() -> shelfScroll.scrollTo(savedScrollX, 0));

        LinearLayout.LayoutParams shelfParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(70));
        shelfParams.setMargins(0, 0, 0, dp(2));
        column.addView(shelfScroll, shelfParams);
    }

    private void addDeveloperSection(LinearLayout column, String text) {
        TextView label = voiceSettingLabel(text, 18f, true);
        label.setGravity(Gravity.START);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(2), 0, dp(4));
        column.addView(label, params);
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

        LinearLayout preview = new LinearLayout(this);
        preview.setOrientation(LinearLayout.VERTICAL);
        preview.setGravity(Gravity.CENTER_HORIZONTAL);
        preview.setBackgroundColor(Color.BLACK);
        preview.setContentDescription("BOOP notification dood preview");
        developerMenuOverlay.addView(preview, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        developerMenuFace = new BoopFaceView(this);
        LinearLayout.LayoutParams faceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f);
        preview.addView(developerMenuFace, faceParams);

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
        puppet.setFaceVisible(false);
        LinearLayout.LayoutParams puppetParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f);
        preview.addView(puppet, puppetParams);

        Button dismiss = new Button(this);
        dismiss.setText("Dismiss");
        dismiss.setTextSize(20f);
        dismiss.setTextColor(Color.WHITE);
        dismiss.setBackgroundColor(Color.rgb(42, 42, 42));
        dismiss.setOnClickListener(v -> showDeveloperMenuContent());
        LinearLayout.LayoutParams dismissParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(64));
        dismissParams.setMargins(dp(28), dp(8), dp(28), dp(18));
        preview.addView(dismiss, dismissParams);

        developerMenuFace.post(() -> {
            if (!developerMenuOpen || developerMenuFace == null) {
                return;
            }
            developerMenuFace.showIdleBlackImmediately();
            developerMenuFace.wakeFromIdle();
        });
    }

    private void hideDeveloperMenu() {
        stopDeveloperAnimation();
        developerMenuFace = null;
        developerMenuOpen = false;
        if (interactionSurface != null && developerMenuOverlay != null) {
            interactionSurface.removeView(developerMenuOverlay);
        }
        developerMenuOverlay = null;
        developerAnimationScrollX = 0;
        developerNotificationScrollX = 0;
        wakeFaceForInteraction();
    }

'''
    text = replace_once(text, anchor, block + anchor, "voice label helper")
    changed = True

if changed:
    path.write_text(text, encoding="utf-8")
    print("Pinned-face horizontal BOOP developer menu materialized")
else:
    print("Pinned-face horizontal BOOP developer menu already materialized")
