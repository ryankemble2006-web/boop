package com.boop.shieldhome;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;

/** Offer the existing permission flow once, only from the user's visible foreground music screen. */
final class MusicBouncePermission {
    private MusicBouncePermission() { }

    static Activity foregroundActivity(Context context) {
        Context current = context;
        for (int i = 0; i < 16; i++) {
            if (current instanceof Activity) {
                Activity activity = (Activity) current;
                return !activity.isFinishing() && !activity.isDestroyed() && activity.hasWindowFocus()
                        ? activity : null;
            }
            if (!(current instanceof ContextWrapper)) return null;
            Context next = ((ContextWrapper) current).getBaseContext();
            if (next == current) return null;
            current = next;
        }
        return null;
    }

    static void promptOnce(Context context) {
        if (MusicAudioPermissionActivity.hasAudioAccess(context)) return;
        Activity activity = foregroundActivity(context);
        if (activity == null) return; // Never open an Activity from the floating media service.
        SharedPreferences preferences = activity.getSharedPreferences("boop_music_bounce", Context.MODE_PRIVATE);
        if (preferences.getBoolean("audio_explanation_seen", false)) return;
        preferences.edit().putBoolean("audio_explanation_seen", true).apply();
        try { activity.startActivity(new Intent(activity, MusicAudioPermissionActivity.class)); }
        catch (RuntimeException unavailable) {
            android.util.Log.w("BOOP-MusicBounce", "Music audio access can be opened from Launcher Settings");
        }
    }
}
