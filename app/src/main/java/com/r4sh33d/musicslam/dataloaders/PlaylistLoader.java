package com.r4sh33d.musicslam.dataloaders;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.PlaylistsColumns;
import android.text.TextUtils;

import com.r4sh33d.musicslam.db.SmartPlaylistType;
import com.r4sh33d.musicslam.models.Playlist;
import com.r4sh33d.musicslam.utils.PlayListHelper;

import java.util.ArrayList;
import java.util.List;

public class PlaylistLoader {

    public static String[] projection = {
            BaseColumns._ID,
            PlaylistsColumns.NAME
    };

    public static ArrayList<Playlist> loadPlayLists(Cursor cursor, Context context, boolean withSmartPlaylists) {
        ArrayList<Playlist> playlistArrayList = new ArrayList<>();
        if (cursor != null) {
            int idColumn = cursor.getColumnIndex(MediaStore.Audio.Playlists._ID);
            int nameColumn = cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME);
            while (cursor.moveToNext()) {
                final long id = cursor.getLong(idColumn);
                final String name = cursor.getString(nameColumn);
                final int songCount = getSongCountForPlaylist(context, id);
                playlistArrayList.add(new Playlist(id, name, songCount));
            }
            cursor.close();
        }
        //move Slam favourite to the top of the list
        moveSlamFavouriteToTheTopOfTheList(playlistArrayList);
        if (withSmartPlaylists) {
            // Add smart playlist to the playlist
            makeDefaultPlaylists(playlistArrayList, context);
        }

        return playlistArrayList;
    }

    private static void moveSlamFavouriteToTheTopOfTheList(ArrayList<Playlist> playlists) {
        Playlist slamPlaylist = null;
        for (Playlist playlist : playlists) {
            if (playlist.name.equals(PlayListHelper.SLAM_FAVOURITE_PLAYLIST_NAME)) {
                slamPlaylist = playlist;
                break;
            }
        }
        if (slamPlaylist != null) {
            playlists.remove(slamPlaylist);
            playlists.add(0, slamPlaylist);
        }
    }

    private static void makeDefaultPlaylists(ArrayList<Playlist> playlistArrayList, Context context) {
        final Resources resources = context.getResources();

        final Playlist topTracks = new Playlist(SmartPlaylistType.TopTracks.mId,
                resources.getString(SmartPlaylistType.TopTracks.mTitleId), -1);
        playlistArrayList.add(0, topTracks);

        final Playlist lastAdded = new Playlist(SmartPlaylistType.LastAdded.mId,
                resources.getString(SmartPlaylistType.LastAdded.mTitleId),
                -1);
        playlistArrayList.add(0, lastAdded);

        final Playlist recentlyPlayed = new Playlist(SmartPlaylistType.RecentlyPlayed.mId,
                resources.getString(SmartPlaylistType.RecentlyPlayed.mTitleId), -1);
        playlistArrayList.add(0, recentlyPlayed);
    }

    public static Cursor makePlaylistCursor(final Context context, String selection, String sortOrder) {
        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER;
        }
        try {
            return context.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    projection, selection, null, sortOrder);
        } catch (SecurityException ignored) {
            return null;
        }

    }

    public static final int getSongCountForPlaylist(final Context context, final long playlistId) {
        Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                new String[]{BaseColumns._ID}, SongLoader.MUSIC_ONLY_SELECTION, null, null);
        if (c != null) {
            int count = 0;
            if (c.moveToFirst()) {
                count = c.getCount();
            }
            c.close();
            return count;
        }
        return 0;
    }

    public static class PlayListAsynctaskLoader extends WrappedAsyncTaskLoader<List<Playlist>> {
        private final String selection;
        private final String sortOrder;

        /**
         * Constructor of <code>WrappedAsyncTaskLoader</code>
         *
         * @param context The {@link Context} to use.
         */
        public PlayListAsynctaskLoader(Context context, String selection, String sortOrder) {
            super(context);
            this.selection = selection;
            this.sortOrder = sortOrder;
        }

        public PlayListAsynctaskLoader(Context context) {
            this(context, null, null);
        }

        @Override
        public List<Playlist> loadInBackground() {
            return loadPlayLists(makePlaylistCursor(getContext(), selection, sortOrder), getContext(), true);
        }
    }
}
