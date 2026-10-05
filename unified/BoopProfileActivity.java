package com.boop.alpha1;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.role.RoleManager;
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

/** Real app setup and settings. Body selection is owned by the installed shell. */
public final class BoopProfileActivity extends Activity {
    private Button mediaAccess;
    private boolean firstSetup;
    private final java.util.concurrent.ExecutorService roomExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();
    private long roomRequest;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        firstSetup = !BoopSetupState.complete(this);
        ScrollView scroll = new ScrollView(this);
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        int pad = Math.round(24 * getResources().getDisplayMetrics().density);
        column.setPadding(pad, pad, pad, pad);
        column.setBackgroundColor(Color.BLACK);
        scroll.addView(column);
        TextView title = new TextView(this);
        title.setText((firstSetup ? "Set up " : "Settings: ") + BoopAppIdentity.displayName(getPackageName()));
        title.setTextSize(28); title.setTextColor(Color.WHITE); column.addView(title);
        TextView detail = new TextView(this);
        detail.setText(firstSetup
                ? "Connect your home and choose the access you want. Every Android permission and Home-app choice is yours. Nothing is enabled automatically."
                : "Room, voice and access settings for this BOOP app.");
        detail.setTextColor(Color.WHITE); detail.setTextSize(18); column.addView(detail);
        add(column, "Home Assistant controls", () -> startActivity(new Intent()
                .setClassName(getPackageName(), "com.boop.shieldoverlay.BoopHomeActivity")));
        add(column, "Set this device’s room", this::room);
        add(column, "Voice settings", () -> startActivity(new Intent(this, MainActivity.class)
                .putExtra("boop_open_voice_settings", true)));
        add(column, "Home button setup", this::homeSetup);
        if (BoopDeviceProfile.resolve(this) != BoopDeviceProfile.Mode.SHIELD) {
            add(column, "Notifications", () -> startActivity(new Intent(this, BoopNotificationSettingsActivity.class)));
        } else {
            add(column, "Notification access", () -> openSettings(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        }
        if (BoopDeviceProfile.resolve(this) == BoopDeviceProfile.Mode.SHIELD) {
            add(column, "Home button compatibility", () -> new AlertDialog.Builder(this)
                    .setMessage("Use this only when Android does not offer BOOP as the Home app. Android Accessibility settings lets you choose BOOP Home button. This does not enable access automatically.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Open settings", (dialog, which) -> openSettings(Settings.ACTION_ACCESSIBILITY_SETTINGS)).show());
            add(column, "Cast corner visibility", () -> openSettings(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }
        mediaAccess = add(column, "Allow BOOP over other apps", () -> {
            try { startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:" + getPackageName()))); }
            catch (RuntimeException unavailable) { openSettings(Settings.ACTION_MANAGE_OVERLAY_PERMISSION); }
        });
        add(column, "Eyes and animation", () -> startActivity(new Intent(this, BoopAppearanceActivity.class)));
        add(column, firstSetup ? "Continue" : "Done", this::finishSetupFromUser);
        setContentView(scroll);
    }

    private void homeSetup() {
        try {
            RoleManager roles = (RoleManager) getSystemService(ROLE_SERVICE);
            if (roles != null && roles.isRoleAvailable(RoleManager.ROLE_HOME)
                    && !roles.isRoleHeld(RoleManager.ROLE_HOME)) {
                startActivity(roles.createRequestRoleIntent(RoleManager.ROLE_HOME));
                return;
            }
        } catch (RuntimeException unavailable) { /* Use the ordinary settings screen. */ }
        openSettings(Settings.ACTION_HOME_SETTINGS);
    }

    private void openSettings(String action) {
        try { startActivity(new Intent(action)); }
        catch (RuntimeException unavailable) {
            new AlertDialog.Builder(this)
                    .setMessage("This Android version does not expose that page directly. Open Android settings to choose the access for BOOP.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Android settings", (dialog, which) -> {
                        try { startActivity(new Intent(Settings.ACTION_SETTINGS)); }
                        catch (RuntimeException ignored) { }
                    }).show();
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
        if (mediaAccess != null) mediaAccess.setText(Settings.canDrawOverlays(this)
                ? "BOOP over other apps: allowed" : "Allow BOOP over other apps");
    }

    private Button add(LinearLayout column, String label, Runnable action) {
        Button button = new Button(this); button.setText(label); button.setTextSize(20);
        button.setMinHeight(Math.round(64 * getResources().getDisplayMetrics().density));
        button.setOnClickListener(v -> action.run()); column.addView(button); return button;
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
