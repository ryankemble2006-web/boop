package com.boop.alpha1;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import com.boop.shieldhome.BoopTvChrome;

/** One read-only value, protected by a signature permission in the Shield manifest. */
public final class BoopHomeAccentProvider extends ContentProvider {
    @Override public boolean onCreate() { return true; }

    @Override public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        requireAccent(uri);
        MatrixCursor result = new MatrixCursor(new String[] {"accent"});
        result.addRow(new Object[] {BoopTvChrome.accentColor(getContext())});
        return result;
    }

    @Override public String getType(Uri uri) {
        requireAccent(uri);
        return "vnd.android.cursor.item/vnd.boop.home-accent";
    }

    private static void requireAccent(Uri uri) {
        if (!BoopLabAccent.URI.equals(uri)) throw new IllegalArgumentException("Unknown accent URI");
    }

    @Override public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Read-only accent");
    }
    @Override public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Read-only accent");
    }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Read-only accent");
    }
}
