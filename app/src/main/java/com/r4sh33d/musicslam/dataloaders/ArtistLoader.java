package com.r4sh33d.musicslam.dataloaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.r4sh33d.musicslam.models.Artist;

import java.util.ArrayList;
import java.util.List;

public class ArtistLoader {
    public static String[] projection = new String[]{
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
    };

    public static class ArtistAsynctaskLoader extends WrappedAsyncTaskLoader<List<Artist>> {
        private final String sortOrder;
        private final String selection;

        /**
         * Constructor of <code>WrappedAsyncTaskLoader</code>
         *
         * @param context The {@link Context} to use.
         */
        public ArtistAsynctaskLoader(Context context, String sortOrder, String selection) {
            super(context);
            this.sortOrder = sortOrder;
            this.selection = selection;
        }

        public ArtistAsynctaskLoader(Context context) {
            this(context, null, null);
        }

        @Override
        public List<Artist> loadInBackground() {
            return getArtistsFromCursor(makeArtistsCursor(getContext(), sortOrder, selection));
        }
    }

    public static ArrayList<Artist> getArtistsFromCursor(Cursor cursor) {
        ArrayList<Artist> arrayList = new ArrayList<>();
        if (cursor != null) {
            int idColumn = cursor.getColumnIndex(MediaStore.Audio.Artists._ID);
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST);
            int songCountColumn = cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS);
            int albumNumberColumn = cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
            while (cursor.moveToNext()) {
                arrayList.add(new Artist(
                        cursor.getInt(albumNumberColumn),
                        cursor.getLong(idColumn),
                        cursor.getString(titleColumn),
                        cursor.getInt(songCountColumn)));
            }
            cursor.close();
        }
        return arrayList;
    }

    public static Cursor makeArtistsCursor(Context context, String sortOrder, String selection) {
        return makeArtistsCursor(context, sortOrder, selection, null);
    }

    public static Cursor makeArtistsCursor(Context context, String sortOrder, String selection, String[] selectionArgs) {
        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = MediaStore.Audio.Artists.ARTIST + " ASC";
        }
        try {
            return context.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                    projection, selection, selectionArgs, sortOrder);
        }catch (SecurityException ignored){
            return null;
        }

    }

    public static Artist getArtist(long artistId, Context context) {
        String selection = MediaStore.Audio.Artists._ID + " = " + String.valueOf(artistId);
        Cursor cursor = makeArtistsCursor(context, null, selection);
        Artist artist = new Artist();
        if (cursor != null) {
            int idColumn = cursor.getColumnIndex(MediaStore.Audio.Artists._ID);
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST);
            int songCountColumn = cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS);
            int albumNumberColumn = cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
            if (cursor.moveToNext()) {
                artist = new Artist(
                        cursor.getInt(albumNumberColumn),
                        cursor.getLong(idColumn),
                        cursor.getString(titleColumn),
                        cursor.getInt(songCountColumn));
            }
            cursor.close();
        }
        return artist;
    }

    public static ArrayList<Artist> searchArtists(Context context, String searchQuery) {
        return getArtistsFromCursor(makeArtistsCursor(
                context,
                null,
                MediaStore.Audio.AudioColumns.ARTIST + " LIKE ?",
                new String[]{"%" + searchQuery + "%"}
        ));
    }
}
