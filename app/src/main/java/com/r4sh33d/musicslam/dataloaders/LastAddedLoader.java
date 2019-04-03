package com.r4sh33d.musicslam.dataloaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;

import com.r4sh33d.musicslam.dataloaders.SongLoader;
import com.r4sh33d.musicslam.models.Song;

import java.util.ArrayList;

public class LastAddedLoader {

    public static final int CUT_OFF = 6 * 4 * 7 * 24 * 60 * 60; // 6 months

    public static ArrayList<Song> getLastAddedSongs(Context context) {
        return SongLoader.getSongsListFromCursor(makeLastAddedCursor(context));
    }

    public static final Cursor makeLastAddedCursor(final Context context) {
        // 6 months ago
        //TODO: export the cuttoff to settings so that users can choose.
        long cuttoff = (System.currentTimeMillis() / 1000) - (CUT_OFF);

        String selection = (AudioColumns.IS_MUSIC + "=1") +
                " AND " + AudioColumns.TITLE + " != ''" +
                " AND " + MediaStore.Audio.Media.DATE_ADDED + ">" +
                cuttoff;
        return SongLoader.makeSongsCursor(selection, null,
                        MediaStore.Audio.Media.DATE_ADDED + " DESC", context );
    }
}
