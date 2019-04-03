package com.r4sh33d.musicslam.dataloaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.r4sh33d.musicslam.models.Album;

import java.util.ArrayList;
import java.util.List;

public class AlbumLoader {
    public static String[] projection = new String[]{MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS,
            MediaStore.Audio.Albums.FIRST_YEAR
    };

    public static class AlbumAsyncTaskLoader extends WrappedAsyncTaskLoader<List<Album>> {
        private Long artistId;
        private final String sortOrder;
        private final String selection;

        /**
         * Constructor of <code>WrappedAsyncTaskLoader</code>
         *
         * @param context The {@link Context} to use.
         */
        public AlbumAsyncTaskLoader(Context context, String sortOrder, String selection) {
            super(context);
            this.sortOrder = sortOrder;
            this.selection = selection;
            this.artistId = null;
        }

        public AlbumAsyncTaskLoader(Context context, Long artistId) {
            this(context, null, null);
            this.artistId = artistId;
        }

        public AlbumAsyncTaskLoader(Context context) {
            this(context, null, null);
        }

        @Override
        public List<Album> loadInBackground() {
            return getAlbumsFromCursor(getContext(), makeAlbumsCursor(getContext(), artistId, selection, sortOrder));
        }
    }

    public static ArrayList<Album> getAlbumsFromCursor(Context context, Cursor cursor) {
        ArrayList<Album> arrayList = new ArrayList<>();
        if (cursor != null) {
            int idColumn = cursor.getColumnIndex(MediaStore.Audio.Albums._ID);
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
            int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST);
            int songCountColumn = cursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS);
            int albumYearColumn = cursor.getColumnIndex(MediaStore.Audio.Albums.FIRST_YEAR);
            while (cursor.moveToNext()) {
                Album album = new Album(
                        cursor.getString(artistColumn),
                        cursor.getLong(idColumn),
                        cursor.getInt(songCountColumn),
                        cursor.getString(titleColumn),
                        cursor.getInt(albumYearColumn)
                );
                album.firstSongPath = SongLoader.getArtworkPathForAlbum(context, album.id);
                arrayList.add(album);
            }
            cursor.close();
        }
        return arrayList;
    }

    public static Cursor makeAlbumsCursor(Context context, Long artistId, String selection, String sortOrder) {
        return makeAlbumsCursor(context, artistId, selection, null, sortOrder);
    }

    public static Cursor makeAlbumsCursor(Context context, Long artistId, String selection,
                                          String[] selectionArgs, String sortOrder) {
        Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        if (artistId != null) {
            uri = MediaStore.Audio.Artists.Albums.getContentUri("external", artistId);
        }
        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = MediaStore.Audio.Media.ALBUM + " ASC";
        }
        try {
            return context.getContentResolver().query(uri,
                    projection, selection, selectionArgs, sortOrder);
        }catch (SecurityException ignored){
            return null;
        }
    }

    public static Album getAlbum(long albumId, Context context) {
        String selection = MediaStore.Audio.Albums._ID + " = " + String.valueOf(albumId);
        Cursor cursor = makeAlbumsCursor(context, null, selection, null);
        Album album = new Album();
        if (cursor != null) {
            int idColumn = cursor.getColumnIndex(MediaStore.Audio.Albums._ID);
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
            int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST);
            int songCountColumn = cursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS);
            int albumYearColumn = cursor.getColumnIndex(MediaStore.Audio.Albums.FIRST_YEAR);
            if (cursor.moveToNext()) {
                album = new Album(
                        cursor.getString(artistColumn),
                        cursor.getLong(idColumn),
                        cursor.getInt(songCountColumn),
                        cursor.getString(titleColumn),
                        cursor.getInt(albumYearColumn));
                album.firstSongPath = SongLoader.getArtworkPathForAlbum(context, album.id);
            }
            cursor.close();
        }
        return album;
    }


    public static ArrayList<Album> searchAlbums (Context context, String searchQuery) {
        return getAlbumsFromCursor(context, makeAlbumsCursor(
                context,
                null,
                MediaStore.Audio.AudioColumns.ALBUM + " LIKE ?",
                new String[]{"%" + searchQuery + "%"},
                null
        ));
    }
}
