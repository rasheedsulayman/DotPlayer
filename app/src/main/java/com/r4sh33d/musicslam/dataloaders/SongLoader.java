package com.r4sh33d.musicslam.dataloaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.r4sh33d.musicslam.db.MusicPlaybackQueueStore;
import com.r4sh33d.musicslam.db.SmartPlaylistType;
import com.r4sh33d.musicslam.models.Song;
import com.r4sh33d.musicslam.playback.Constants;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class SongLoader {

    public static final String MUSIC_ONLY_SELECTION = MediaStore.Audio.AudioColumns.IS_MUSIC + "=1"
            + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''";
    public static final String DEFAULT_ALBUM_SORT_ORDER = MediaStore.Audio.AudioColumns.TRACK + ", "
            + MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
    public static String ARTIST_SELECTION = MediaStore.Audio.Media.ARTIST_ID + " = ?";
    public static String ALBUM_SELECTION = MediaStore.Audio.Media.ALBUM_ID + " = ?";

    public static String[] projection = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_MODIFIED
    };

    public static String[] playlistProjection = new String[]{
            MediaStore.Audio.Playlists.Members.AUDIO_ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_MODIFIED

    };

    public static List<Song> getSongsInPlaylist(long playListId, Context context) {
        if (playListId < 0) {
            //noinspection ConstantConditions
            switch (SmartPlaylistType.getTypeById(playListId)) {
                case LastAdded:
                    return LastAddedLoader.getLastAddedSongs(context);
                case RecentlyPlayed:
                    TopTracksLoader recentPlayedLoader = new TopTracksLoader(context,
                            TopTracksLoader.QueryType.RecentSongs);
                    return recentPlayedLoader.getTracks();
                case TopTracks:
                    TopTracksLoader topTracksLoader = new TopTracksLoader(context,
                            TopTracksLoader.QueryType.TopTracks);
                    return topTracksLoader.getTracks();
            }
        }
        return getSongsListFromCursor(makePlayListSongCursor(context, playListId, null, null));
    }

    public static Cursor makeSongsCursor(String selection, String[] selectionArgs, String sortOrder, Context context) {
        return makeSongsCursor(null, selection, selectionArgs, sortOrder, context);
    }

    public static Cursor makeSongsCursor(Uri uri, String selection, String[] selectionArgs, String sortOrder,
                                         Context context) {
        String selectionStatement = MUSIC_ONLY_SELECTION;
        if (!TextUtils.isEmpty(selection)) {
            selectionStatement += " AND " + selection;
        }

        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
        }
        if (uri == null) {
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }
        try {
            return context.getContentResolver().query(
                    uri,
                    projection,
                    selectionStatement,
                    selectionArgs,
                    sortOrder);
        } catch (SecurityException ignored) {
            return null;
        }
    }

    public static Cursor makeGenresSongCusor(Context context, long genresId, String sortOrder, String selection) {
        Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external", genresId);
        String selectionStatement = MUSIC_ONLY_SELECTION;
        if (!TextUtils.isEmpty(selection)) {
            selectionStatement += " AND " + selection;
        }
        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
        }
        try {
            return context.getContentResolver().query(
                    uri,
                    projection,
                    selectionStatement,
                    null,
                    sortOrder
            );
        } catch (SecurityException ignored) {
            return null;
        }
    }

    public static ArrayList<Song> getSongsInAlbum(long albumId, Context context) {
        return getSongsListFromCursor(
                makeSongsCursor(
                        ALBUM_SELECTION,
                        new String[]{String.valueOf(albumId)},
                        SongLoader.DEFAULT_ALBUM_SORT_ORDER,
                        context
                )
        );
    }

    public static ArrayList<Song> getSongsForArtist(long artistId, Context context) {
        return getSongsListFromCursor(
                makeSongsCursor(
                        ARTIST_SELECTION,
                        new String[]{String.valueOf(artistId)},
                        null,
                        context
                )
        );
    }

    public static ArrayList<Song> getSongsForGenres(long genresId, Context context) {
        return getSongsListFromCursor(
                makeGenresSongCusor(
                        context,
                        genresId,
                        null,
                        null
                )
        );
    }

    public static ArrayList<Song> getSongsInNormalPlaylist(long playlistId, Context context) {
        return getSongsListFromCursor(
                makePlayListSongCursor(
                        context,
                        playlistId,
                        null,
                        null
                )
        );
    }

    public static Cursor makePlayListSongCursor(Context context, long playListId, String sortOrder, String selection) {
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playListId);
        String selectionStatement = MUSIC_ONLY_SELECTION;
        if (!TextUtils.isEmpty(selection)) {
            selectionStatement += " AND " + selection;
        }
        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
        }

        try {
            return context.getContentResolver().query(
                    uri,
                    playlistProjection,
                    selectionStatement,
                    null,
                    sortOrder
            );
        } catch (SecurityException ignored) {
            return null;
        }

    }

    public static ArrayList<Song> searchSongs(Context context, String searchQuery) {
        return getSongsListFromCursor(makeSongsCursor(
                MediaStore.Audio.AudioColumns.TITLE + " LIKE ?",
                new String[]{"%" + searchQuery + "%"},
                null,
                context
        ));
    }


    public static Song getFirstSongInAlbum(Context context, long albumId) {
        Song song = Song.getEmptySong();
        final String[] projection = new String[]{MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DATE_MODIFIED};
        String[] selectionArgs = {String.valueOf(albumId)};
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, SongLoader.ALBUM_SELECTION, selectionArgs,
                DEFAULT_ALBUM_SORT_ORDER);
        if (cursor != null) {
            int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int dateModifiedColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);
            if (cursor.moveToNext()) {
                song.data = cursor.getString(dataColumn);
                song.dateModified = cursor.getLong(dateModifiedColumn);
            }
            cursor.close();
        }
        return song;
    }

    public static String getArtworkPathForAlbum(Context context, long albumId) {
        String result = "";
        final String[] projection = new String[]{MediaStore.Audio.Media.DATA};
        String[] selectionArgs = {String.valueOf(albumId)};
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, SongLoader.ALBUM_SELECTION, selectionArgs,
                DEFAULT_ALBUM_SORT_ORDER);
        if (cursor != null) {
            int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            if (cursor.moveToNext()) {
                result = cursor.getString(dataColumn);
            }
            cursor.close();
        }
        return result;
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    public static ArrayList<Song> getSongFromLocalUri(Uri uri, Context context) {
        Cursor cursor = null;
        try {
            switch (uri.getScheme()) {
                case Constants.SCHEME_CONTENT:
                    Timber.d("Scheme content");
                    cursor = makeSongsCursor(
                            uri,
                            null,
                            null,
                            null,
                            context
                    );
                    break;
                case Constants.SCHEME_FILE:
                    cursor = makeSongsCursor(
                            MediaStore.Audio.Media.DATA + " = ?",
                            new String[]{uri.getPath()},
                            null, context
                    );
                    Timber.d("Scheme file");
                    break;
            }
            return getSongsListFromCursor(cursor);
        } catch (IllegalArgumentException ignored) {
            //TODO find why _id column is missing for SCHEME_CONTENT on some devices
            //TODO usually when a song is copied to another location on newer samsung devices
            return new ArrayList<>();
        }
    }

    public static ArrayList<Song> getSongsListFromCursor(Cursor cursor) {
        ArrayList<Song> arrayList = new ArrayList<>();
        if (cursor != null) {
            int idColumn;
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int artistIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID);
            int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int trackNumberColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
            int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int mimeTypeColumn = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE);
            int songFileSizeColumn = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE);
            int dateModifiedColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);

            try {
                idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            } catch (IllegalArgumentException isAPlaylist) {
                //Playlist uses AUDIO_ID instead of _ID
                idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
            }

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String title = cursor.getString(titleColumn);
                String artist = cursor.getString(artistColumn);
                String album = cursor.getString(albumColumn);
                long duration = cursor.getInt(durationColumn);
                int trackNumber = cursor.getInt(trackNumberColumn);
                long artistId = cursor.getInt(artistIdColumn);
                long albumId = cursor.getLong(albumIdColumn);
                String data = cursor.getString(dataColumn);
                String mimeType = cursor.getString(mimeTypeColumn);
                long fileSize = cursor.getLong(songFileSizeColumn);
                long dateModified = cursor.getLong(dateModifiedColumn);
                arrayList.add(new Song(albumId, album, artistId, artist, duration, id, title,
                        trackNumber, data, mimeType, fileSize, dateModified));
            }
            cursor.close();
        }
        return arrayList;
    }

    public static class SongsAsyncTaskLoader extends WrappedAsyncTaskLoader<List<Song>> {
        private final String[] selectionArgs;
        private String selection;
        private String sortOrder;

        /**
         * Constructor of <code>WrappedAsyncTaskLoader</code>
         *
         * @param context The {@link Context} to use.
         */
        public SongsAsyncTaskLoader(Context context) {
            this(context, null, null, null);
        }

        public SongsAsyncTaskLoader(Context context, String selection, String[] selectionArgs, String sortOrder) {
            super(context);
            this.selection = selection;
            this.selectionArgs = selectionArgs;
            this.sortOrder = sortOrder;
        }

        @Override
        public List<Song> loadInBackground() {
            ArrayList<Song> songs = getSongsListFromCursor(makeSongsCursor(selection, selectionArgs,
                    sortOrder, getContext()));
            if (selection == null && songs.size() == 0) {
                //No song on the device.
                //Delete our NowPlaying queue store as well
                MusicPlaybackQueueStore.getInstance(getContext()).deleteAllEntries();
            }
            return songs;
        }
    }

    public static class GenresSongAsyncTaskLoader extends WrappedAsyncTaskLoader<List<Song>> {
        private final long genresId;
        private String selection;
        private String sortOrder;

        /**
         * Constructor of <code>WrappedAsyncTaskLoader</code>
         *
         * @param context The {@link Context} to use.
         */
        public GenresSongAsyncTaskLoader(Context context, long genresId) {
            this(context, genresId, null, null);
        }

        public GenresSongAsyncTaskLoader(Context context, long genresId, String selection, String sortOrder) {
            super(context);
            this.genresId = genresId;
            this.selection = selection;
            this.sortOrder = sortOrder;
        }

        @Override
        public List<Song> loadInBackground() {
            return getSongsListFromCursor(makeGenresSongCusor(getContext(), genresId, sortOrder, selection));
        }
    }

    public static class PlayListSongAsysnctaskLoader extends WrappedAsyncTaskLoader<List<Song>> {

        private final long playListId;

        public PlayListSongAsysnctaskLoader(Context context, long playListId) {
            super(context);
            this.playListId = playListId;
        }

        @Override
        public List<Song> loadInBackground() {
            return SongLoader.getSongsInPlaylist(playListId, getContext());
        }
    }
}
