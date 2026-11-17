package com.boop.alpha1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.boop.shieldhome.ShieldSelfGrant;

/** Real app setup and settings. Body selection is owned by the installed shell. */
public final class BoopProfileActivity extends Activity {
    public static final String PREFS_LAUNCHER_CHOICE = "boop_launcher_choice_v1";
    public static final String KEY_USE_BUILTIN_LAUNCHER = "use_builtin_launcher";
    private Button homeLauncherBtn;
    private Button useBuiltinLauncherToggleBtn;
    private Button notificationAccessBtn;
    private Button homeServiceBtn;
    private Button mediaAccess;
    private boolean firstSetup;
    private final java.util.concurrent.ExecutorService roomExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();
    private long roomRequest;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        firstSetup = !BoopSetupState.complete(this);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.BLACK);
        scroll.setOverScrollMode(android.view.View.OVER_SCROLL_NEVER);
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(44), dp(28), dp(44), dp(28));
        column.setBackgroundColor(Color.BLACK);
        scroll.addView(column);

        TextView eyebrow = new TextView(this);
        eyebrow.setText(firstSetup ? "FIRST-RUN CONFIGURATION" : "SYSTEM & ROOM CONFIGURATION");
        eyebrow.setTextSize(11);
        eyebrow.setTypeface(com.boop.shieldhome.BoopTvChrome.FONT_MEDIUM);
        eyebrow.setLetterSpacing(0.08f);
        eyebrow.setTextColor(com.boop.shieldhome.BoopTvChrome.accentColor(this));
        column.addView(eyebrow);

        TextView title = new TextView(this);
        title.setText((firstSetup ? "Set up " : "Settings: ") + BoopAppIdentity.displayName(getPackageName()));
        title.setTextSize(28);
        title.setTypeface(com.boop.shieldhome.BoopTvChrome.FONT_MEDIUM);
        title.setLetterSpacing(-0.015f);
        title.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-1, -2);
        titleLp.topMargin = dp(2);
        column.addView(title, titleLp);

        TextView detail = new TextView(this);
        detail.setText(firstSetup
                ? "Connect your home and activate BOOP access with one click."
                : "Room, voice and system access settings for this BOOP app.");
        detail.setTextColor(com.boop.shieldhome.BoopTvChrome.TEXT_SECONDARY);
        detail.setTypeface(com.boop.shieldhome.BoopTvChrome.FONT_REGULAR);
        detail.setTextSize(15);
        LinearLayout.LayoutParams detailLp = new LinearLayout.LayoutParams(-1, -2);
        detailLp.topMargin = dp(4);
        detailLp.bottomMargin = dp(20);
        column.addView(detail, detailLp);

        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout leftCol = new LinearLayout(this);
        leftCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout rightCol = new LinearLayout(this);
        rightCol.setOrientation(LinearLayout.VERTICAL);

        LinearLayout homeCard = sectionCard("HOME & SMART ROOM");
        Button firstBtn = add(homeCard, "Home Assistant controls", () -> startActivity(new Intent()
                .setClassName(getPackageName(), "com.boop.shieldoverlay.BoopHomeActivity")));
        add(homeCard, "Set this device\u2019s room", this::room);
        leftCol.addView(homeCard, cardParams(14));

        LinearLayout voiceCard = sectionCard("VOICE & CHARACTER");
        add(voiceCard, "Voice settings", () -> startActivity(new Intent(this, MainActivity.class)
                .putExtra("boop_open_voice_settings", true)));
        add(voiceCard, "Eyes and animation", () -> startActivity(new Intent(this, BoopAppearanceActivity.class)));
        leftCol.addView(voiceCard, cardParams(0));

        boolean isShield = BoopDeviceProfile.resolve(this) == BoopDeviceProfile.Mode.SHIELD;
        LinearLayout accessCard = sectionCard(isShield ? "LAUNCHER & SYSTEM ACCESS" : "PHONE & SYSTEM ACCESS");
        useBuiltinLauncherToggleBtn = add(accessCard, "Launcher mode", this::toggleBuiltinLauncher);
        if (isShield) {
            homeLauncherBtn = add(accessCard, "Default Home launcher", this::activateAllAccess);
            notificationAccessBtn = add(accessCard, "Media & notification access", this::activateAllAccess);
            homeServiceBtn = add(accessCard, "Home button & remote service", this::activateAllAccess);
        } else {
            homeLauncherBtn = add(accessCard, "Default Home app settings", this::openHomeSettings);
            notificationAccessBtn = add(accessCard, "Notification & lock settings", () -> startActivity(new Intent(this, BoopNotificationSettingsActivity.class)));
            homeServiceBtn = null;
        }
        mediaAccess = add(accessCard, "Allow BOOP over other apps", () -> {
            if (Settings.canDrawOverlays(this)) {
                if (isShield) activateAllAccess();
                return;
            }
            if (isShield) {
                ShieldSelfGrant.ensureAllGrantedAsync(this, (ok, msg) -> {
                    refreshAccessLabels();
                    if (!Settings.canDrawOverlays(this)) {
                        openOverlaySettings();
                    } else {
                        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                openOverlaySettings();
            }
        });
        rightCol.addView(accessCard, cardParams(0));

        LinearLayout.LayoutParams leftLp = new LinearLayout.LayoutParams(0, -2, 1f);
        leftLp.rightMargin = dp(16);
        LinearLayout.LayoutParams rightLp = new LinearLayout.LayoutParams(0, -2, 1f);
        grid.addView(leftCol, leftLp);
        grid.addView(rightCol, rightLp);
        column.addView(grid, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout footer = new LinearLayout(this);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setGravity(android.view.Gravity.END);
        Button doneBtn = createButton(firstSetup ? "Continue" : "Done", this::finishSetupFromUser);
        doneBtn.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams doneLp = new LinearLayout.LayoutParams(dp(220), dp(52));
        doneLp.topMargin = dp(18);
        footer.addView(doneBtn, doneLp);
        column.addView(footer, new LinearLayout.LayoutParams(-1, -2));

        setContentView(scroll);
        refreshAccessLabels();
        firstBtn.post(firstBtn::requestFocus);
    }

    public static boolean isBuiltinLauncherEnabled(Context context) {
        return context.getSharedPreferences(PREFS_LAUNCHER_CHOICE, Context.MODE_PRIVATE)
                .getBoolean(KEY_USE_BUILTIN_LAUNCHER, true);
    }

    private void toggleBuiltinLauncher() {
        boolean current = isBuiltinLauncherEnabled(this);
        boolean next = !current;
        getSharedPreferences(PREFS_LAUNCHER_CHOICE, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_USE_BUILTIN_LAUNCHER, next).apply();
        refreshAccessLabels();
        android.widget.Toast.makeText(this,
                next ? "Built-in BOOP launcher enabled." : "Built-in launcher disabled (keeping stock/custom home).",
                android.widget.Toast.LENGTH_SHORT).show();
    }

    private void openHomeSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
        } catch (Exception e) {
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            } catch (Exception ignored) { }
        }
    }

    private void activateAllAccess() {
        android.widget.Toast.makeText(this, "Activating BOOP Home, Media & Remote access...",
                android.widget.Toast.LENGTH_SHORT).show();
        ShieldSelfGrant.ensureAllGrantedAsync(this, (ok, msg) -> {
            if (isFinishing() || isDestroyed()) return;
            refreshAccessLabels();
            android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void openOverlaySettings() {
        try {
            startActivity(new Intent().setComponent(new ComponentName(
                    "com.android.tv.settings",
                    "com.android.tv.settings.device.apps.specialaccess.SystemAlertActivity")));
            return;
        } catch (RuntimeException ignored) { }
        try {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:" + getPackageName())));
        } catch (RuntimeException ignored) { }
    }

    private void refreshAccessLabels() {
        boolean isShield = BoopDeviceProfile.resolve(this) == BoopDeviceProfile.Mode.SHIELD;
        if (useBuiltinLauncherToggleBtn != null) {
            boolean useLauncher = isBuiltinLauncherEnabled(this);
            useBuiltinLauncherToggleBtn.setText(useLauncher
                    ? "Built-in Launcher: ENABLED"
                    : "Built-in Launcher: DISABLED (Keep stock home)");
        }
        if (homeLauncherBtn != null) {
            if (isShield) {
                homeLauncherBtn.setText(ShieldSelfGrant.isDefaultHome(this)
                        ? "Default Home launcher: ON"
                        : "Default Home launcher: Activate");
            } else {
                homeLauncherBtn.setText("Default Home app settings");
            }
        }
        if (notificationAccessBtn != null && isShield) {
            notificationAccessBtn.setText(ShieldSelfGrant.isNotificationAccessGranted(this)
                    ? "Media & notification access: ON"
                    : "Media & notification access: Activate");
        }
        if (homeServiceBtn != null) {
            homeServiceBtn.setText(ShieldSelfGrant.isHomeAccessibilityEnabled(this)
                    ? "Home button & remote service: ON"
                    : "Home button & remote service: Activate");
        }
        if (mediaAccess != null) {
            mediaAccess.setText(Settings.canDrawOverlays(this)
                    ? "BOOP over other apps: ON"
                    : "Allow BOOP over other apps");
        }
    }

    private void finishSetupFromUser() {
        if (!firstSetup) { finish(); return; }
        if (!BoopSetupState.completeFromUser(this)) {
            new AlertDialog.Builder(this).setMessage("BOOP could not save setup. Please try Continue again.")
                    .setPositiveButton("OK", null).show();
            return;
        }
        startActivity(new Intent(this, UnifiedEntryActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    @Override protected void onResume() {
        super.onResume();
        boolean shield = BoopDeviceProfile.resolve(this) == BoopDeviceProfile.Mode.SHIELD;
        com.boop.shieldhome.BoopMediaBridge.configure(this, shield);
        refreshAccessLabels();
    }

    private LinearLayout sectionCard(String eyebrowText) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        card.setBackground(com.boop.shieldhome.BoopTvChrome.sectionPanel(this));
        TextView header = new TextView(this);
        header.setText(eyebrowText);
        header.setTextSize(11);
        header.setTypeface(com.boop.shieldhome.BoopTvChrome.FONT_MEDIUM);
        header.setLetterSpacing(0.08f);
        header.setTextColor(com.boop.shieldhome.BoopTvChrome.accentColor(this));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(10);
        card.addView(header, lp);
        return card;
    }

    private LinearLayout.LayoutParams cardParams(int bottomDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(bottomDp);
        return lp;
    }

    private Button createButton(String label, Runnable action) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(15.5f);
        button.setAllCaps(false);
        button.setGravity(android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL);
        button.setMinHeight(dp(50));
        button.setOnClickListener(v -> action.run());
        com.boop.shieldhome.BoopTvChrome.styleInteractive(button);
        return button;
    }

    private Button add(LinearLayout column, String label, Runnable action) {
        Button button = createButton(label, action);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(50));
        lp.bottomMargin = dp(8);
        column.addView(button, lp);
        return button;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void room() {
        if (BoopDeviceProfile.resolve(this) == BoopDeviceProfile.Mode.SHIELD) {
            startActivity(new Intent().setClassName(getPackageName(), "com.boop.shieldoverlay.BoopHomeActivity")
                    .putExtra("boop_choose_room", true));
            return;
        }
        SharedPreferences prefs = getSharedPreferences("boop_unified", MODE_PRIVATE);
        EditText name = new EditText(this); name.setSingleLine(true);
        name.setText(prefs.getString("room_name", "Living Room"));
        new AlertDialog.Builder(this).setTitle("Room name in Home Assistant").setView(name)
                .setNegativeButton("Cancel", null).setPositiveButton("Save", (d, w) -> {
                    String value = name.getText().toString().trim();
                    if (!value.isEmpty()) resolveRoom(value);
                }).show();
    }

    private void resolveRoom(String name) {
        SecureTokenStore tokens = new SecureTokenStore(this);
        if (!tokens.hasConnection()) {
            new AlertDialog.Builder(this).setMessage("Connect BOOP to Home Assistant first, then choose this device's room.")
                    .setPositiveButton("OK", null).show(); return;
        }
        final long request = ++roomRequest;
        android.widget.Toast.makeText(this, "Checking the room with Home Assistant...", android.widget.Toast.LENGTH_SHORT).show();
        roomExecutor.execute(() -> {
            BoopRoom resolved = null;
            String error = "I couldn't find that room in Home Assistant. Check its name.";
            try { resolved = HomeAssistantRoomLookup.find(tokens.getBaseUrl(), new HomeAssistantAuth(this, tokens).freshAccessToken(), name); }
            catch (Exception unavailable) { error = "I couldn't check the room. Check the Home Assistant connection and try again."; }
            final BoopRoom result = resolved; final String message = error;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed() || request != roomRequest) return;
                if (result == null) { new AlertDialog.Builder(this).setMessage(message).setPositiveButton("OK", null).show(); return; }
                getSharedPreferences("boop_unified", MODE_PRIVATE).edit()
                        .putString("room_id", result.id()).putString("room_name", result.name()).apply();
                com.boop.shared.BoopState.INSTANCE.room(result.id(), result.name());
                android.widget.Toast.makeText(this, "Room set to " + result.name(), android.widget.Toast.LENGTH_LONG).show();
            });
        });
    }

    @Override protected void onPause() { ++roomRequest; super.onPause(); }
    @Override protected void onDestroy() { ++roomRequest; roomExecutor.shutdownNow(); super.onDestroy(); }
}
