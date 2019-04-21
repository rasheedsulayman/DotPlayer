package com.r4sh33d.musicslam.dataloaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.r4sh33d.musicslam.models.Genres;

import java.util.ArrayList;
import java.util.List;

public class GenresLoader {
    public static String[] projection = {
            MediaStore.Audio.Genres.NAME,
            MediaStore.Audio.Genres._ID,
    };

    public static Cursor makeGeneresCursor(Context context, String sortOder) {
        if (TextUtils.isEmpty(sortOder)) {
            sortOder = MediaStore.Audio.Genres.DEFAULT_SORT_ORDER;
        }
        try {
            return context.getContentResolver().query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                    projection, null, null, sortOder);
        } catch (SecurityException ignored) {
            return null;
        }

    }

    public static ArrayList<Genres> getGenresListFromCursor(Cursor cursor, Context context) {
        ArrayList<Genres> genresArrayList = new ArrayList<>();
        if (cursor != null) {
            int idColumn = cursor.getColumnIndex(MediaStore.Audio.Genres._ID);
            int nameColumn = cursor.getColumnIndex(MediaStore.Audio.Genres.NAME);
            while (cursor.moveToNext()) {
                long genres_id = cursor.getLong(idColumn);
                int songCount = getSongCountForGenres(context, genres_id);
                String name = cursor.getString(nameColumn);
                genresArrayList.add(new Genres(name, genres_id, songCount));
            }
            cursor.close();
        }
        return genresArrayList;
    }

    public static int getSongCountForGenres(final Context context, final long genresId) {
        Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Genres.Members.getContentUri("external", genresId),
                new String[]{BaseColumns._ID}, SongLoader.MUSIC_ONLY_SELECTION, null, null);
        if (c != null) {
            int count = c.getCount();
            c.close();
            return count;
        }
        return 0;
    }

    public static class GenresAsynctaskLoader extends WrappedAsyncTaskLoader<List<Genres>> {
        private String sortOrder;

        /**
         * Constructor of <code>WrappedAsyncTaskLoader</code>
         *
         * @param context The {@link Context} to use.
         */
        public GenresAsynctaskLoader(Context context) {
            this(context, null);
        }

        public GenresAsynctaskLoader(Context context, String sortOrder) {
            super(context);
            this.sortOrder = sortOrder;
        }


        @Override
        public List<Genres> loadInBackground() {
            return getGenresListFromCursor(makeGeneresCursor(getContext(), sortOrder), getContext());
        }
    }
}
