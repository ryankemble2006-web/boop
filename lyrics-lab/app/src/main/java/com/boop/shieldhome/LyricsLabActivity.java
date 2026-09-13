package com.boop.shieldhome;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

/** Side-by-side shell using the unchanged, user-approved production lyrics presentation. */
public final class LyricsLabActivity extends Activity {
    private final NativeLyricsLoader loader = new NativeLyricsLoader();
    private LyricsLabSession media;
    private ShieldLyricsView presentation;
    private TextView access;
    private String identity = "";
    private boolean resumed;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        media = LyricsLabSession.get(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        FrameLayout root = new FrameLayout(this);
        presentation = new ShieldLyricsView(this, Color.rgb(41, 216, 243), new ShieldLyricsView.Controls() {
            public void previous() { media.previous(); }
            public void playPause() { media.toggle(); }
            public void next() { media.next(); }
            public void seek(long milliseconds) { media.seek(milliseconds); }
            public void close() { finish(); }
        });
        root.addView(presentation, new FrameLayout.LayoutParams(-1, -1));
        access = new TextView(this);
        access.setText("Allow music access");
        access.setTextColor(Color.rgb(41, 216, 243));
        access.setTextSize(20);
        access.setGravity(Gravity.CENTER);
        access.setFocusable(true); access.setClickable(true);
        access.setContentDescription("Allow BOOP Lyrics Lab to follow Deezer. Opens Android notification access settings.");
        access.setOnClickListener(v -> openAccess());
        access.setOnFocusChangeListener((v, focused) -> {
            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.rgb(12, 29, 35)); background.setCornerRadius(dp(26));
            background.setStroke(dp(focused ? 2 : 1), focused ? Color.rgb(41, 216, 243) : Color.rgb(44, 65, 74));
            v.setBackground(background);
        });
        FrameLayout.LayoutParams button = new FrameLayout.LayoutParams(dp(280), dp(54), Gravity.RIGHT | Gravity.BOTTOM);
        button.rightMargin = dp(50); button.bottomMargin = dp(120);
        root.addView(access, button);
        setContentView(root);
    }
    @Override protected void onResume() {
        super.onResume(); resumed = true; identity = "";
        presentation.setRunning(true);
        media.start(this::changed);
    }
    private void changed(LyricsLabSession.Frame frame, boolean allowed) {
        if (!resumed || isFinishing()) return;
        access.setVisibility(allowed ? View.GONE : View.VISIBLE);
        presentation.setSnapshot(frame == null ? null : frame.snapshot, frame != null && frame.clockKnown);
        String next = frame == null ? "" : frame.identity();
        if (allowed && !next.isEmpty() && next.equals(identity)) return;
        identity = next; loader.cancel(); presentation.setDocument(null);
        if (!allowed) {
            presentation.setStatus("Allow music access to follow Deezer.");
            access.post(access::requestFocus);
        } else if (frame == null) {
            presentation.setStatus("Play something in Deezer.\nLyrics will appear here when available.");
        } else if (frame.trackId.isEmpty()) {
            presentation.setStatus("Couldn't identify this track for lyrics.");
        } else {
            presentation.setStatus("Finding lyrics…");
            loader.load(frame.trackId, next, document -> {
                if (!resumed || isFinishing() || !next.equals(identity)) return;
                if (document.status() == DeezerLyricsDocument.Status.AVAILABLE) {
                    presentation.setDocument(document); presentation.setStatus("");
                    Log.i("BoopLyricsLab", "Timed document ready: lines=" + document.lines().size());
                } else {
                    presentation.setDocument(null);
                    presentation.setStatus(document.status() == DeezerLyricsDocument.Status.UNAVAILABLE
                            ? "No lyrics for this track." : "Couldn't check lyrics just now.");
                    Log.i("BoopLyricsLab", "Timed document result=" + document.status());
                }
            });
        }
    }
    private void openAccess() {
        for (NowPlayingAccessSettingsPlan.Route route : NowPlayingAccessSettingsPlan.routesForSdk(Build.VERSION.SDK_INT)) {
            Intent intent;
            if (route == NowPlayingAccessSettingsPlan.Route.TV_EXACT) {
                intent = new Intent().setComponent(new ComponentName(NowPlayingAccessSettingsPlan.tvSettingsPackage(),
                        NowPlayingAccessSettingsPlan.tvNotificationAccessClassName()));
            } else if (route == NowPlayingAccessSettingsPlan.Route.DETAIL) {
                intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS)
                        .putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, media.accessComponent().flattenToString());
            } else intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            try { startActivity(intent); return; } catch (RuntimeException unavailable) { }
        }
        presentation.setStatus("Open Shield settings, then enable notification access for BOOP Lyrics Lab.");
    }
    @Override protected void onPause() {
        resumed = false; loader.cancel(); media.stop(); presentation.setRunning(false);
        super.onPause();
    }
    @Override protected void onDestroy() { loader.destroy(); super.onDestroy(); }
    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE: media.toggle(); return true;
                case KeyEvent.KEYCODE_MEDIA_NEXT: media.next(); return true;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS: media.previous(); return true;
                default: break;
            }
        }
        return super.dispatchKeyEvent(event);
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
