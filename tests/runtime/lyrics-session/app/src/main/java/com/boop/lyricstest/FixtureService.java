package com.boop.lyricstest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;

/** Real Android Binder session with deterministic data, not a replacement renderer. */
public final class FixtureService extends Service {
    private static FixtureService instance;
    private final Handler main = new Handler(Looper.getMainLooper());
    private MediaSession session;
    private int index;
    private int state = PlaybackState.STATE_PAUSED;
    private long position = 1000;
    private long updated;
    private int generation;
    private static final long ACTIONS = PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PAUSE
            | PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_SKIP_TO_NEXT
            | PlaybackState.ACTION_SKIP_TO_PREVIOUS | PlaybackState.ACTION_SEEK_TO;
    @Override public void onCreate() {
        super.onCreate();
        EmulatorOnly.require();
        instance = this;
        NotificationManager notifications = getSystemService(NotificationManager.class);
        notifications.createNotificationChannel(new NotificationChannel("fixture", "Local test session", NotificationManager.IMPORTANCE_LOW));
        startForeground(1, new Notification.Builder(this, "fixture")
                .setSmallIcon(android.R.drawable.ic_media_play).setContentTitle("BOOP emulator test session")
                .setContentText("Synthetic metadata only; no real audio or account.").build());
        createSession();
    }
    private void createSession() {
        if (session != null) session.release();
        session = new MediaSession(this, "BOOP local transition fixture");
        session.setCallback(new MediaSession.Callback() {
            @Override public void onPlay() { position = currentPosition(); publishState(PlaybackState.STATE_PLAYING); }
            @Override public void onPause() { position = currentPosition(); publishState(PlaybackState.STATE_PAUSED); }
            @Override public void onSeekTo(long target) { position = Math.max(0, Math.min(120000, target)); publishState(state); }
            @Override public void onSkipToNext() { transition((index + 1) % 3, PlaybackState.STATE_NONE); }
            @Override public void onSkipToPrevious() { transition((index + 2) % 3, PlaybackState.STATE_STOPPED); }
        }, main);
        metadata();
        publishState(PlaybackState.STATE_PAUSED);
        session.setActive(true);
    }
    private long currentPosition() {
        return Math.min(120000, position + (state == PlaybackState.STATE_PLAYING ? SystemClock.elapsedRealtime() - updated : 0));
    }
    private void metadata() {
        session.setMetadata(new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, "Local track " + (index + 1))
                .putString(MediaMetadata.METADATA_KEY_ARTIST, "BOOP test session")
                .putString(MediaMetadata.METADATA_KEY_ALBUM, "Invented test recordings")
                .putLong(MediaMetadata.METADATA_KEY_DURATION, 120000)
                .putString("com.deezer.METADATA_KEY_PLAYABLE_IDENTIFIER_TYPE", "TRACK")
                .putString("com.deezer.METADATA_KEY_PLAYABLE_IDENTIFIER_ID", Integer.toString(990001 + index)).build());
    }
    private void publishState(int next) {
        state = next;
        updated = SystemClock.elapsedRealtime();
        session.setPlaybackState(next < 0 ? null : new PlaybackState.Builder().setActions(ACTIONS)
                .setState(next, position, next == PlaybackState.STATE_PLAYING ? 1f : 0f, updated).build());
    }
    private void transition(int next, int transientState) {
        int operation = ++generation;
        index = next;
        publishState(transientState);
        main.postDelayed(() -> {
            if (generation != operation) return;
            position = 1000;
            metadata();
            publishState(PlaybackState.STATE_PAUSED);
        }, 800);
    }
    public static final class Commands extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            EmulatorOnly.require();
            FixtureService fixture = instance;
            if (fixture == null) return;
            String command = intent.getStringExtra("command");
            if ("transition".equals(command)) fixture.transition(intent.getIntExtra("index", 0), intent.getIntExtra("state", 0));
            else if ("configure".equals(command)) {
                fixture.generation++;
                fixture.index = intent.getIntExtra("index", 0);
                fixture.position = 1000;
                fixture.metadata(); fixture.publishState(PlaybackState.STATE_PAUSED);
            } else if ("missing".equals(command)) fixture.session.setMetadata(null);
            else if ("restore".equals(command)) fixture.metadata();
            else if ("replace".equals(command)) {
                fixture.generation++; fixture.index = intent.getIntExtra("index", 0); fixture.position = 1000; fixture.createSession();
            } else if ("close".equals(command)) context.stopService(new Intent(context, FixtureService.class));
        }
    }
    @Override public int onStartCommand(Intent intent, int flags, int startId) { return START_NOT_STICKY; }
    @Override public IBinder onBind(Intent intent) { return null; }
    @Override public void onDestroy() {
        generation++; main.removeCallbacksAndMessages(null);
        if (session != null) session.release();
        instance = null;
        super.onDestroy();
    }
}
