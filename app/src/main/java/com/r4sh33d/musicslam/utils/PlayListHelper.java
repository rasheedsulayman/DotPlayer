/*
 * Copyright (C) 2012 Andrew Neal
 * Copyright (C) 2014 The CyanogenMod Project
 * Copyright (C) 2019 Rasheed Sulayman
 *
 * Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.r4sh33d.musicslam.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.widget.Toast;

import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.playback.MusicPlayer;

import static com.r4sh33d.musicslam.playback.MusicPlayer.mService;


public class PlayListHelper {
    public final static String SLAM_FAVOURITE_PLAYLIST_NAME = "Slam Favourites";
    private static ContentValues[] mContentValuesCache = null;

    /**
     * @param context    The {@link Context} to use.
     * @param ids        The id of the song(s) to add.
     * @param playlistid The id of the playlist being added to.
     */
    public static void addToPlaylist(final Context context, final long[] ids, final long playlistid) {
        final int size = ids.length;
        final ContentResolver resolver = context.getContentResolver();
        final String[] projection = new String[]{
                "max(" + MediaStore.Audio.Playlists.Members.PLAY_ORDER + ")",
        };
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistid);
        Cursor cursor = null;
        int base = 0;

        try {
            cursor = resolver.query(uri, projection, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                base = cursor.getInt(0) + 1;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        int numinserted = 0;
        for (int offSet = 0; offSet < size; offSet += 1000) {
            makeInsertItems(ids, offSet, 1000, base);
            numinserted += resolver.bulkInsert(uri, mContentValuesCache);
        }
        String message = context.getResources().
                getQuantityString(R.plurals.n_tracks_were_added_to_playlist, numinserted, numinserted);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        playlistChanged();
    }

    /**
     * Removes a single track from a given playlist
     *
     * @param context    The {@link Context} to use.
     * @param id         The id of the song to remove.
     * @param playlistId The id of the playlist being removed from.
     */
    public static void removeFromPlaylist(final Context context, final long id,
                                          final long playlistId) {
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        final ContentResolver resolver = context.getContentResolver();
        resolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID + " = ? ", new String[]{
                Long.toString(id)
        });
        final String message = context.getString(R.string.playlist_successfully_removed);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        playlistChanged();
    }

    public static void makeInsertItems(final long[] ids, final int offset, int len, final int base) {
        if (offset + len > ids.length) {
            len = ids.length - offset;
        }

        if (mContentValuesCache == null || mContentValuesCache.length != len) {
            mContentValuesCache = new ContentValues[len];
        }
        for (int i = 0; i < len; i++) {
            if (mContentValuesCache[i] == null) {
                mContentValuesCache[i] = new ContentValues();
            }
            mContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + offset + i);
            mContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, ids[offset + i]);
        }
    }

    /**
     * @param context The {@link Context} to use.
     * @param name    The name of the new playlist.
     * @return A new playlist ID.
     */
    public static final long createPlaylist(final Context context, final String name) {
        if (name != null && name.length() > 0) {
            final ContentResolver resolver = context.getContentResolver();
            final String[] projection = new String[]{
                    MediaStore.Audio.PlaylistsColumns.NAME
            };
            final String selection = MediaStore.Audio.PlaylistsColumns.NAME + " = '" + name + "'";
            Cursor cursor = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    projection, selection, null, null);
            if (cursor.getCount() <= 0) {
                final ContentValues values = new ContentValues(1);
                values.put(MediaStore.Audio.PlaylistsColumns.NAME, name);
                final Uri uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
                return Long.parseLong(uri.getLastPathSegment());
            }
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
            return -1;
        }
        return -1;
    }

    /**
     * Returns The ID for a playlist.
     *
     * @param context The {@link Context} to use.
     * @param name    The name of the playlist.
     * @return The ID for a playlist.
     */
    public static final long getIdForPlaylist(final Context context, final String name) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, new String[]{
                        BaseColumns._ID
                }, MediaStore.Audio.PlaylistsColumns.NAME + "=?", new String[]{
                        name
                }, MediaStore.Audio.PlaylistsColumns.NAME);
        int id = -1;
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                id = cursor.getInt(0);
            }
            cursor.close();
            cursor = null;
        }
        return id;
    }


    /**
     * @param context    The {@link Context} to use.
     * @param playlistId The playlist ID.
     */
    public static void clearPlaylist(final Context context, final int playlistId) {
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        context.getContentResolver().delete(uri, null, null);
        return;
    }

    /**
     * Called when one of playlists have changed
     */
    public static void playlistChanged() {
        try {
            if (mService != null) {
                mService.playlistChanged();
            }
        } catch (final Exception ignored) {
        }
    }

    public static void deletePlayList(long playlistId, Context context) {
        final Uri mUri = ContentUris.withAppendedId(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                playlistId);
        context.getContentResolver().delete(mUri, null, null);
        MusicPlayer.refresh();
    }

    public static boolean doesPlaylistContainSong(Context context, String playlistName, long songId) {
        long playlistId = getIdForPlaylist(context, playlistName);
        if (playlistId == -1) {
            //It is possible to reach this place without having favourites created
            //Favourite does not exist yet
            playlistId = PlayListHelper.createPlaylist(context, PlayListHelper.SLAM_FAVOURITE_PLAYLIST_NAME);
        }
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        String selection = MediaStore.Audio.Playlists.Members.AUDIO_ID + " = " + songId;
        Cursor cursor = context.getContentResolver()
                .query(uri, new String[]{MediaStore.Audio.Playlists.Members.AUDIO_ID},
                        selection, null, null);
        boolean isPresent = false;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                // the song is present
                isPresent = true;
            }
            cursor.close();
        }
        return isPresent;
    }

    public static void renamePlaylist(String updatedName, long playlistId, Context context) {
        final ContentResolver resolver = context.getContentResolver();
        final ContentValues values = new ContentValues(1);
        values.put(MediaStore.Audio.Playlists.NAME, updatedName);
        resolver.update(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values,
                MediaStore.Audio.Playlists._ID + "=?", new String[]{
                        String.valueOf(playlistId)
                });
        MusicPlayer.refresh();
    }

    public static void toggleAddFavourite(long songId, Context context) {
        long playlistId = getIdForPlaylist(context, SLAM_FAVOURITE_PLAYLIST_NAME);
        if (playlistId == -1) {
            //Favourite does not exist yet
            playlistId = createPlaylist(context, SLAM_FAVOURITE_PLAYLIST_NAME);
        }
        if (isSongAFovouriteSong(songId, context)) {
            removeFromPlaylist(context, songId, playlistId);
        } else {
            addToPlaylist(context, new long[]{songId}, playlistId);
        }
    }

    public static boolean isSongAFovouriteSong(long songId, Context context) {
        return doesPlaylistContainSong(context,
                SLAM_FAVOURITE_PLAYLIST_NAME, songId);
    }
}
