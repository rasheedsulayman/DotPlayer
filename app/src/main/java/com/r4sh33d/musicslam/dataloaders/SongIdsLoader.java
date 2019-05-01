package com.r4sh33d.musicslam.dataloaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.r4sh33d.musicslam.db.SmartPlaylistType;
import com.r4sh33d.musicslam.models.Playlist;

public class SongIdsLoader {

    private static long[] sEmptyList = {};

    private static long[] getSongIdsListForSmartPlaylist(final Context context,
                                                         final SmartPlaylistType type) {
        Cursor cursor = null;
        try {
            switch (type) {
                case LastAdded:
                    cursor = LastAddedLoader.makeLastAddedCursor(context);
                    break;
                case RecentlyPlayed:
                    cursor = TopTracksLoader.makeRecentTracksCursor(context);
                    break;
                case TopTracks:
                    cursor = TopTracksLoader.makeTopTracksCursor(context);
                    break;
            }
            return getSongsIdListFromCursor(cursor);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }


    private static long[] getSongIdsListForNormalPlaylist(final Context context, final long playlistId) {
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        final String[] projection = {MediaStore.Audio.Playlists.Members.AUDIO_ID};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        long[] list = new long[0];
        if (cursor != null) {
            list = getSongsIdListFromCursor(cursor);
            cursor.close();
            return list;
        }
        return list;
    }

    public static long[] getSongListForGenres(final Context context, final long genresId) {
        final String[] projection = {String.valueOf(BaseColumns._ID)};
        Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external", genresId);

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        long[] list = new long[0];
        if (cursor != null) {
            list = getSongsIdListFromCursor(cursor);
            cursor.close();
            return list;
        }
        return list;
    }


    public static long[] getSongIdsListForArtist(final Context context, long artistId) {
        final String[] projection = {String.valueOf(BaseColumns._ID)};
        String[] selectionArgs = {String.valueOf(artistId)};
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, SongLoader.ARTIST_SELECTION, selectionArgs,
                MediaStore.Audio.AudioColumns.ALBUM_KEY + "," + MediaStore.Audio.AudioColumns.TRACK);
        return getSongsIdListFromCursor(cursor);
    }

    public static long[] getSongsIdListFromCursor(Cursor cursor) {
        if (cursor != null) {
            final int cursorLength = cursor.getCount();
            final long[] songsIdList = new long[cursorLength];
            int idColumn;
            try {
                idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            } catch (final IllegalArgumentException isaplaylist) {
                //Playlist uses AUDIO_ID instead of _ID
                idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
            }
            cursor.moveToFirst();
            for (int i = 0; i < cursorLength; i++) {
                songsIdList[i] = cursor.getLong(idColumn);
                cursor.moveToNext();
            }
            cursor.close();
            return songsIdList;
        }
        return sEmptyList;
    }

    public static long[] getSongIdsListForAlbum(Context context, long albumId) {
        final String[] projection = new String[]{MediaStore.Audio.Media._ID};
        String[] selectionArgs = {String.valueOf(albumId)};
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, SongLoader.ALBUM_SELECTION, selectionArgs,
                MediaStore.Audio.AudioColumns.TRACK + ", " + MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        return getSongsIdListFromCursor(cursor);
    }

    @SuppressWarnings("ConstantConditions")
    public static long[] getSongIdsListForPlaylist(Context context, Playlist playlist) {
        if (playlist.id < 0) {
            //smartPlaylist
            return getSongIdsListForSmartPlaylist(context, SmartPlaylistType.getTypeById(playlist.id));
        } else {
            return SongIdsLoader.getSongIdsListForNormalPlaylist(context, playlist.id);
        }
    }
}
