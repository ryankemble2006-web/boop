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
        add(column,"Done", this::finish);
        setContentView(scroll);
    }
    private void add(LinearLayout column,String label,Runnable action) {
        Button button = new Button(this); button.setText(label); button.setTextSize(20); button.setMinHeight(Math.round(64*getResources().getDisplayMetrics().density));
        button.setOnClickListener(v -> action.run()); column.addView(button);
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
                prefs.edit().putString("room_id","").putString("room_name",value).apply();
                com.boop.shared.BoopState.INSTANCE.room("",value);
            }).show();
    }
}
