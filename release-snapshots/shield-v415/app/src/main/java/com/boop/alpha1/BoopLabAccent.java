package com.boop.alpha1;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/** Reads only the selected Home accent; absent or inaccessible Home uses its default cyan. */
public final class BoopLabAccent {
    public static final String AUTHORITY = "com.boop.shieldoverlay.home_accent";
    public static final Uri URI = Uri.parse("content://" + AUTHORITY + "/accent");
    public static final int DEFAULT_ACCENT = 0xff4db8ff;

    private BoopLabAccent() { }

    public static int fromShieldHome(Context context) {
        try (Cursor cursor = context.getContentResolver().query(URI,
                new String[] {"accent"}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int column = cursor.getColumnIndex("accent");
                if (column >= 0) return cursor.getInt(column) | 0xff000000;
            }
        } catch (RuntimeException unavailable) {
            // Missing Home, different signing identity, or a stopped provider.
        }
        return DEFAULT_ACCENT;
    }
}
