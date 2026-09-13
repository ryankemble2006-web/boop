package com.boop.shieldhome;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import org.json.JSONArray;
import org.json.JSONObject;

/** Separate emulator-only fixture. This source is never included in the BOOP application. */
public final class LyricsPreviewActivity extends Activity {
    private ShieldLyricsView view;
    private DeezerLyricsDocument document;
    private Bitmap artwork;
    private long position;
    private long updated;
    private boolean playing = true;
    private int track;
    private long actions = PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_PLAY
            | PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_SKIP_TO_NEXT
            | PlaybackState.ACTION_SKIP_TO_PREVIOUS | PlaybackState.ACTION_SEEK_TO;

    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        artwork = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(artwork);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new LinearGradient(0, 0, 600, 600, Color.rgb(4, 76, 99), Color.rgb(62, 24, 85), Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, 600, 600, paint);
        paint.setShader(null); paint.setColor(Color.rgb(69, 195, 216)); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(2);
        for (int i = 0; i < 12; i++) canvas.drawCircle(300, 300, 45 + i * 18, paint);
        view = new ShieldLyricsView(this, Color.rgb(41, 216, 243), new ShieldLyricsView.Controls() {
            public void previous() { track = (track + 2) % 3; changeTrack(); }
            public void next() { track = (track + 1) % 3; changeTrack(); }
            public void playPause() { toggle(); }
            public void seek(long delta) { position = Math.max(0, Math.min(120000, currentPosition() + delta)); updated = SystemClock.elapsedRealtime(); publish("seek"); }
            public void close() { finish(); }
        });
        setContentView(view);
        if (saved != null) {
            track = saved.getInt("track"); position = saved.getLong("position"); playing = saved.getBoolean("playing");
            setDocument(); updated = SystemClock.elapsedRealtime(); publish("restored");
        } else configure(getIntent());
    }
    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent); setIntent(intent); configure(intent); }
    private void configure(Intent intent) {
        track = Math.max(0, Math.min(2, intent.getIntExtra("fixture_track", 0)));
        playing = !intent.getBooleanExtra("fixture_paused", false);
        position = Math.max(0, Math.min(120000, intent.getLongExtra("fixture_position", 4000L)));
        updated = SystemClock.elapsedRealtime(); setDocument(); publish("configured");
    }
    private long currentPosition() {
        return Math.min(120000, position + (playing ? SystemClock.elapsedRealtime() - updated : 0));
    }
    private void toggle() { position = currentPosition(); updated = SystemClock.elapsedRealtime(); playing = !playing; publish("play-pause"); }
    private void changeTrack() { position = 0; updated = SystemClock.elapsedRealtime(); setDocument(); publish("track-change"); }
    private void setDocument() {
        if (track == 2) { document = null; view.setDocument(null); view.setStatus("No lyrics for this track."); return; }
        try {
            String[] phrases = track == 0 ? new String[]{"A quiet start to the evening", "The room begins to glow", "Every line follows the music", "Nothing else needs to open", "", "Pause here, then carry on", "A little further down the song", "Back where we started", "The final notes drift away"}
                    : new String[]{"This longer invented line wraps naturally without being cut off at the edge", "Another line with room to breathe", "Cymraeg: mae'r gerddoriaeth yn parhau", "Still following the same playback clock", "", "A different track, a fresh set of words"};
            JSONArray lines = new JSONArray();
            for (int i = 0; i < phrases.length; i++) lines.put(new JSONObject().put("line", phrases[i])
                    .put("milliseconds", 2000 + i * 6000).put("duration", 4800));
            JSONObject lyrics = new JSONObject().put("synchronizedLines", lines).put("synchronizedWordByWordLines", JSONObject.NULL)
                    .put("licence", "LOCAL TEST FIXTURE · Invented text, no commercial song lyrics");
            String id = Integer.toString(123 + track);
            String body = new JSONObject().put("data", new JSONObject().put("track", new JSONObject().put("id", id).put("lyrics", lyrics))).toString();
            document = DeezerLyricsDocument.parse(body, id);
            if (document.status() != DeezerLyricsDocument.Status.AVAILABLE) throw new IllegalStateException("Fixture parse failed");
            view.setDocument(document); view.setStatus("");
        } catch (Exception failed) { throw new IllegalStateException(failed); }
    }
    private void publish(String action) {
        NowPlayingSnapshot snapshot = new NowPlayingSnapshot(1, "fixture.local", track == 0 ? "Evening / Local playback test" : track == 1 ? "A second recording" : "Instrumental fixture",
                "BOOP runtime fixture", playing ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED,
                actions, position, 120000, playing ? 1f : 0f, updated, artwork, "", "Invented test album");
        view.setSnapshot(snapshot, true);
        Log.i("BoopLyricsFixture", "action=" + action + " track=" + track + " playing=" + playing
                + " position=" + currentPosition() + " active=" + (document == null ? -1 : document.activeIndex(currentPosition())));
    }
    @Override protected void onStart() { super.onStart(); view.setRunning(true); }
    @Override protected void onStop() { view.setRunning(false); super.onStop(); }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putInt("track", track); state.putLong("position", currentPosition()); state.putBoolean("playing", playing);
        super.onSaveInstanceState(state);
    }
    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) { toggle(); return true; }
            if (event.getKeyCode() == KeyEvent.KEYCODE_MEDIA_NEXT) { track = (track + 1) % 3; changeTrack(); return true; }
            if (event.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PREVIOUS) { track = (track + 2) % 3; changeTrack(); return true; }
        }
        return super.dispatchKeyEvent(event);
    }
}
