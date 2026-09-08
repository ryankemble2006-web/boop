package com.boop.shieldhome;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TvProviderRows {
    private TvProviderRows() {}

    public interface QuerySource {
        List<HomeContentCard> loadWatchNextCards();
        List<HomeRow> loadAppChannelRows();
    }

    public static OptionalRowRegistry.ProviderFactory factory(Context context) {
        return factory(new ContentResolverSource(context.getApplicationContext()));
    }

    static OptionalRowRegistry.ProviderFactory factory(QuerySource source) {
        return key -> {
            if (key == OptionalRowRegistry.Key.PLAY_NEXT) {
                return () -> loadPlayNext(source);
            }
            return () -> safeRows(source::loadAppChannelRows);
        };
    }

    private static List<HomeRow> loadPlayNext(QuerySource source) {
        List<HomeContentCard> cards;
        try {
            cards = source.loadWatchNextCards();
        } catch (RuntimeException ignored) {
            return Collections.emptyList();
        }
        if (cards == null || cards.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new HomeRow("Play Next", cards));
    }

    private static List<HomeRow> safeRows(RowLoader loader) {
        try {
            List<HomeRow> rows = loader.load();
            return rows == null ? Collections.emptyList() : new ArrayList<>(rows);
        } catch (RuntimeException ignored) {
            return Collections.emptyList();
        }
    }

    @FunctionalInterface
    private interface RowLoader {
        List<HomeRow> load();
    }

    private static final class ContentResolverSource implements QuerySource {
        private static final String[] PROGRAM_PROJECTION = {
                TvContract.PreviewPrograms.COLUMN_TITLE,
                TvContract.PreviewPrograms.COLUMN_INTENT_URI,
                TvContract.PreviewPrograms.COLUMN_POSTER_ART_URI
        };
        private static final String[] WATCH_NEXT_PROJECTION = {
                TvContract.WatchNextPrograms.COLUMN_TITLE,
                TvContract.WatchNextPrograms.COLUMN_INTENT_URI,
                TvContract.WatchNextPrograms.COLUMN_POSTER_ART_URI
        };
        private static final String[] CHANNEL_PROJECTION = {
                TvContract.Channels._ID,
                TvContract.Channels.COLUMN_DISPLAY_NAME,
                TvContract.Channels.COLUMN_TYPE,
                TvContract.Channels.COLUMN_BROWSABLE
        };

        private final ContentResolver resolver;

        ContentResolverSource(Context context) {
            resolver = context.getContentResolver();
        }

        @Override public List<HomeContentCard> loadWatchNextCards() {
            return queryCards(TvContract.WatchNextPrograms.CONTENT_URI, WATCH_NEXT_PROJECTION);
        }

        @Override public List<HomeRow> loadAppChannelRows() {
            ArrayList<HomeRow> rows = new ArrayList<>();
            try (Cursor cursor = resolver.query(
                    TvContract.Channels.CONTENT_URI,
                    CHANNEL_PROJECTION,
                    null,
                    null,
                    null)) {
                if (cursor == null) {
                    return rows;
                }
                int idColumn = cursor.getColumnIndexOrThrow(TvContract.Channels._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(TvContract.Channels.COLUMN_DISPLAY_NAME);
                int typeColumn = cursor.getColumnIndexOrThrow(TvContract.Channels.COLUMN_TYPE);
                int browsableColumn = cursor.getColumnIndexOrThrow(TvContract.Channels.COLUMN_BROWSABLE);
                while (cursor.moveToNext()) {
                    if (cursor.getInt(browsableColumn) == 0
                            || !TvContract.Channels.TYPE_PREVIEW.equals(cursor.getString(typeColumn))) {
                        continue;
                    }
                    long channelId = cursor.getLong(idColumn);
                    List<HomeContentCard> cards = queryCards(
                            TvContract.buildPreviewProgramsUriForChannel(channelId),
                            PROGRAM_PROJECTION);
                    if (cards.isEmpty()) {
                        continue;
                    }
                    String name = cursor.getString(nameColumn);
                    rows.add(new HomeRow(name == null ? "" : name, cards));
                }
            }
            return rows;
        }

        private List<HomeContentCard> queryCards(Uri uri, String[] projection) {
            ArrayList<HomeContentCard> cards = new ArrayList<>();
            try (Cursor cursor = resolver.query(uri, projection, null, null, null)) {
                if (cursor == null) {
                    return cards;
                }
                int titleColumn = cursor.getColumnIndexOrThrow(projection[0]);
                int intentColumn = cursor.getColumnIndexOrThrow(projection[1]);
                int posterColumn = cursor.getColumnIndexOrThrow(projection[2]);
                while (cursor.moveToNext()) {
                    String title = cursor.getString(titleColumn);
                    String intentUri = cursor.getString(intentColumn);
                    String posterUri = cursor.getString(posterColumn);
                    if ((title == null || title.isEmpty())
                            && (intentUri == null || intentUri.isEmpty())) {
                        continue;
                    }
                    cards.add(new HomeContentCard(title, intentUri, posterUri));
                }
            }
            return cards;
        }
    }
}
