package com.boop.lyricstest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public final class PlayerActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        EmulatorOnly.require();
        TextView label = new TextView(this);
        label.setText("BOOP local media-session fixture\nSynthetic session only. No audio, login or network.");
        label.setTextSize(24);
        setContentView(label);
        startForegroundService(new Intent(this, FixtureService.class));
    }
}
