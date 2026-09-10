package com.boop.alpha1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Explicit profile selection. Never changes Android's default HOME or grants access. */
public final class BoopProfileActivity extends Activity {
    private Button mediaAccess;
    private final java.util.concurrent.ExecutorService roomExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();
    private long roomRequest;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        LinearLayout column = new LinearLayout(this); column.setOrientation(LinearLayout.VERTICAL);
        int pad = Math.round(24 * getResources().getDisplayMetrics().density);
        column.setPadding(pad,pad,pad,pad); column.setBackgroundColor(Color.BLACK); scroll.addView(column);
        TextView title = new TextView(this); title.setText("Choose how BOOP opens"); title.setTextSize(28); title.setTextColor(Color.WHITE); column.addView(title);
        TextView detail = new TextView(this); detail.setText("You can change this later in BOOP settings. Your voices and Home Assistant connection stay saved."); detail.setTextColor(Color.WHITE); detail.setTextSize(18); column.addView(detail);
        add(column,"Automatic for this device", () -> choose(null));
        add(column,"Wall / tablet", () -> choose(BoopDeviceProfile.Mode.WALL));
        add(column,"Phone launcher", () -> choose(BoopDeviceProfile.Mode.LAUNCHER));
        add(column,"Shield / TV Home", () -> choose(BoopDeviceProfile.Mode.SHIELD));
        add(column,"Set this device’s room", this::room);
        add(column,"Home Assistant controls", () -> startActivity(new Intent().setClassName(getPackageName(),"com.boop.shieldoverlay.BoopHomeActivity")));
        add(column,"Voice settings", () -> { startActivity(new Intent(this,MainActivity.class).putExtra("boop_open_voice_settings",true)); });
        if (BoopDeviceProfile.resolve(this) == BoopDeviceProfile.Mode.SHIELD) {
            add(column,"Cast corner visibility", () -> {
                try { startActivity(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
                catch(RuntimeException unavailable) { new AlertDialog.Builder(this).setMessage("Open Android Accessibility settings and enable BOOP Cast corner.").setPositiveButton("OK",null).show(); }
            });
            mediaAccess = add(column,"Allow BOOP over media apps", () -> {
                try { startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        android.net.Uri.parse("package:"+getPackageName()))); }
                catch (RuntimeException unavailable) { new AlertDialog.Builder(this)
                        .setMessage("Open Android settings and allow BOOP to display over other apps.")
                        .setPositiveButton("OK",null).show(); }
            });
        }
        add(column,"Done", this::finish);
        setContentView(scroll);
    }
    @Override protected void onResume() {
        super.onResume();
        boolean shield = BoopDeviceProfile.resolve(this) == BoopDeviceProfile.Mode.SHIELD;
        com.boop.shieldhome.BoopMediaBridge.configure(this,shield);
        if (mediaAccess != null) mediaAccess.setText(android.provider.Settings.canDrawOverlays(this)
                ? "BOOP over media apps: allowed" : "Allow BOOP over media apps");
    }
    private Button add(LinearLayout column,String label,Runnable action) {
        Button button = new Button(this); button.setText(label); button.setTextSize(20); button.setMinHeight(Math.round(64*getResources().getDisplayMetrics().density));
        button.setOnClickListener(v -> action.run()); column.addView(button); return button;
    }
    private void choose(BoopDeviceProfile.Mode mode) {
        BoopDeviceProfile.setOverride(this,mode);
        getSharedPreferences("boop_unified",MODE_PRIVATE).edit().putBoolean("profile_choice_seen",true).apply();
        startActivity(new Intent(this,UnifiedEntryActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK)); finish();
    }
    private void room() {
        SharedPreferences prefs = getSharedPreferences("boop_unified",MODE_PRIVATE);
        EditText name = new EditText(this); name.setSingleLine(true); name.setText(prefs.getString("room_name","Living Room"));
        new AlertDialog.Builder(this).setTitle("Room name in Home Assistant").setView(name)
            .setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w) -> {
                String value=name.getText().toString().trim();
                if(value.isEmpty()) return;
                resolveRoom(value);
            }).show();
    }
    private void resolveRoom(String name) {
        SecureTokenStore tokens = new SecureTokenStore(this);
        if (!tokens.hasConnection()) {
            new AlertDialog.Builder(this).setMessage("Connect BOOP to Home Assistant first, then choose this device's room.")
                    .setPositiveButton("OK",null).show(); return;
        }
        final long request = ++roomRequest;
        android.widget.Toast.makeText(this,"Checking the room with Home Assistant...",android.widget.Toast.LENGTH_SHORT).show();
        roomExecutor.execute(() -> {
            BoopRoom resolved = null;
            String error = "I couldn't find that room in Home Assistant. Check its name.";
            try { resolved = HomeAssistantRoomLookup.find(tokens.getBaseUrl(),new HomeAssistantAuth(this,tokens).freshAccessToken(),name); }
            catch(Exception unavailable) { error = "I couldn't check the room. Check the Home Assistant connection and try again."; }
            final BoopRoom result = resolved; final String message = error;
            runOnUiThread(() -> {
                if(isFinishing() || isDestroyed() || request != roomRequest) return;
                if(result == null) { new AlertDialog.Builder(this).setMessage(message).setPositiveButton("OK",null).show(); return; }
                getSharedPreferences("boop_unified",MODE_PRIVATE).edit().putString("room_id",result.id()).putString("room_name",result.name()).apply();
                com.boop.shared.BoopState.INSTANCE.room(result.id(),result.name());
                android.widget.Toast.makeText(this,"Room set to "+result.name(),android.widget.Toast.LENGTH_LONG).show();
            });
        });
    }
    @Override protected void onPause() { ++roomRequest; super.onPause(); }
    @Override protected void onDestroy() { ++roomRequest; roomExecutor.shutdownNow(); super.onDestroy(); }
}
