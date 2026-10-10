package com.boop.shieldhome;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import com.boop.shared.DeezerArtistMatch;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/** An explicit artist click opens a page. No playback command or background lookup. */
final class DeezerArtistBrowser {
    private long generation;
    private boolean busy;
    private NowPlayingSnapshot pending;

    synchronized void cancel() { generation++; busy = false; pending = null; }
    synchronized void onTrackChanged(NowPlayingSnapshot current) {
        if (busy && !sameRequest(pending, current)) cancel();
    }

    synchronized void open(Activity activity, ShieldNowPlayingManager manager,
            NowPlayingSnapshot requested) {
        if (busy || requested == null) return;
        if (requested.title().isEmpty() || requested.subtitle().isEmpty()) {
            message(activity, "Artist details aren't available for this track."); return;
        }
        final long operation = ++generation;
        // This existing guard validates recording identity against the live UI snapshot.
        final String recordingId = manager.deezerLyricsTrackId(requested);
        busy = true;
        pending = requested;
        message(activity, "Opening artist...");
        new Thread(() -> {
            long found = 0;
            try { found = lookup(requested, recordingId); }
            catch (Exception unavailable) { /* Plain failure on the main thread below. */ }
            final long artistId = found;
            activity.runOnUiThread(() -> {
                synchronized (DeezerArtistBrowser.this) {
                    if (operation != generation) return;
                    busy = false;
                    pending = null;
                    if (activity.isFinishing() || activity.isDestroyed()
                            || !activity.hasWindowFocus()) return;
                    if (!sameRequest(requested, manager.state().current())) return;
                    if (artistId <= 0) {
                        message(activity, "I couldn't identify this exact artist."); return;
                    }
                    try {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://www.deezer.com/artist/" + artistId))
                                .setPackage("deezer.android.app"));
                    } catch (RuntimeException unavailable) {
                        message(activity, "Deezer couldn't open that artist.");
                    }
                }
            });
        }, "boop-artist-browse").start();
    }

    private static boolean sameRequest(NowPlayingSnapshot a, NowPlayingSnapshot b) {
        return a != null && b != null && a.sessionId() == b.sessionId()
                && a.trackKey().equals(b.trackKey()) && a.album().equals(b.album())
                && a.durationMs() == b.durationMs();
    }

    private static long lookup(NowPlayingSnapshot track, String recordingId) throws Exception {
        String query = track.title() + " " + track.subtitle();
        if (query.length() > 500 || track.album().length() > 500) return 0;
        List<DeezerArtistMatch.Row> rows = new ArrayList<>();
        if (recordingId != null && recordingId.matches("[1-9][0-9]{0,18}")) {
            JSONObject exact = readJson("https://api.deezer.com/track/" + recordingId);
            if (exact == null || !recordingId.equals(Long.toString(exact.optLong("id")))) return 0;
            addRecording(rows, exact);
        } else {
            JSONObject result = readJson("https://api.deezer.com/search/track?q="
                    + URLEncoder.encode(query, "UTF-8") + "&limit=100");
            JSONArray data = result == null ? null : result.optJSONArray("data");
            for (int i = 0; data != null && i < data.length(); i++) {
                addRecording(rows, data.optJSONObject(i));
            }
        }
        return DeezerArtistMatch.resolve(track.title(), track.subtitle(), track.album(), rows);
    }

    private static void addRecording(List<DeezerArtistMatch.Row> rows, JSONObject recording) {
        if (recording == null) return;
        JSONObject artist = recording.optJSONObject("artist"), album = recording.optJSONObject("album");
        if (artist == null || album == null) return;
        long id = artist.optLong("id");
        String name = artist.optString("name"), release = album.optString("title");
        rows.add(new DeezerArtistMatch.Row(recording.optString("title"), name, release, id));
        // Deezer can publish an abbreviated title to MediaSession. Both names belong to this recording.
        rows.add(new DeezerArtistMatch.Row(recording.optString("title_short"), name, release, id));
    }

    private static JSONObject readJson(String address) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(4000);
        connection.setReadTimeout(4000);
        try {
            if (connection.getResponseCode() != 200) return null;
            StringBuilder body = new StringBuilder();
            try (Reader reader = new InputStreamReader(connection.getInputStream(), "UTF-8")) {
                char[] buffer = new char[4096]; int count;
                while ((count = reader.read(buffer)) != -1) {
                    if (body.length() + count > 1_048_576) return null;
                    body.append(buffer, 0, count);
                }
            }
            return new JSONObject(body.toString());
        } finally { connection.disconnect(); }
    }
    private static void message(Activity activity, String text) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
    }
}
