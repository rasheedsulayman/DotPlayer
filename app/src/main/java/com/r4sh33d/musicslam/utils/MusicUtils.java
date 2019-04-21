package com.r4sh33d.musicslam.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.db.RecentStore;
import com.r4sh33d.musicslam.db.SongPlayCount;
import com.r4sh33d.musicslam.models.Song;
import com.r4sh33d.musicslam.playback.MusicPlayer;

import java.io.File;
import java.util.List;

import timber.log.Timber;

public class MusicUtils {

    public static void setRingtone(final Context context, final long id) {
        if (!checkSystemWritePermission(context)) {
            showWriteSettingsPermissionDialog(context,
                    context.getString(R.string.app_needs_settings_permission));
            return;
        }

        final ContentResolver resolver = context.getContentResolver();
        final Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        try {
            final ContentValues values = new ContentValues(2);
            values.put(MediaStore.Audio.AudioColumns.IS_RINGTONE, "1");
            values.put(MediaStore.Audio.AudioColumns.IS_ALARM, "1");
            resolver.update(uri, values, null, null);
        } catch (final UnsupportedOperationException ingored) {
            return;
        }

        final String[] projection = new String[]{
                BaseColumns._ID, MediaStore.MediaColumns.TITLE
        };

        final String selection = BaseColumns._ID + "=" + id;
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
                selection, null, null);
        try {
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                Settings.System.putString(resolver, Settings.System.RINGTONE, uri.toString());
                final String toastMessageToshow = String.format(context.getString(R.string.song_set_as_your_ringtone_format),
                        cursor.getString(1));
                Toast.makeText(context, toastMessageToshow, Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            Timber.e(e); //Ignoring this
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public static boolean checkSystemWritePermission(Context context) {
        boolean retVal = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            retVal = Settings.System.canWrite(context);
        }
        return retVal;
    }

    @SuppressLint("NewApi")
    public static void showWriteSettingsPermissionDialog(Context context, String reason) {
        new MaterialDialog.Builder(context)
                .title(R.string.grant_settings_permission)
                .content(reason)
                .positiveText(R.string.settings)
                .onPositive((dialog, which) -> {
                    openAndroidPermissionsMenu(context);
                }).negativeText(R.string.cancel)
                .onNegative((dialog, which) -> {
                    dialog.dismiss();
                }).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void openAndroidPermissionsMenu(Context context) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }

    public static void deleteTracks(final Context context, final long[] list) {
        final String[] projection = {BaseColumns._ID, MediaStore.MediaColumns.DATA, MediaStore.Audio.AudioColumns.ALBUM_ID};
        final StringBuilder selection = new StringBuilder();
        selection.append(BaseColumns._ID + " IN (");
        for (int i = 0; i < list.length; i++) {
            selection.append(list[i]);
            if (i < list.length - 1) {
                selection.append(",");
            }
        }
        selection.append(")");
        final Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection.toString(),
                null, null);
        if (c != null) {
            // Step 1: Remove selected tracks from the current playlist, as well
            // as from the album art cache
            c.moveToFirst();
            while (!c.isAfterLast()) {
                // Remove from current playlist
                final long id = c.getLong(0);
                MusicPlayer.removeTrack(id);
                // Remove the track from the play count
                SongPlayCount.getInstance(context).removeItem(id);
                // Remove any items in the recents database
                RecentStore.getInstance(context).removeItem(id);
                c.moveToNext();
            }

            // Step 2: Remove selected tracks from the database
            context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    selection.toString(), null);

            // Step 3: Remove files from card
            c.moveToFirst();
            while (!c.isAfterLast()) {
                final String name = c.getString(1);
                final File f = new File(name);
                try { // File.delete can throw a security exception
                    if (!f.delete()) {
                        // I'm not sure if we'd ever get here (deletion would
                        // have to fail, but no exception thrown)
                        Timber.d("Failed to delete file " + name);
                    }
                    c.moveToNext();
                } catch (final SecurityException ex) {
                    c.moveToNext();
                }
            }
            c.close();
        }
        String message = context.getResources().getQuantityString(R.plurals.n_tracks_were_deleted, list.length, list.length);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        // We deleted a number of tracks, which could affect any number o things
        // in the media content domain, so update everything.
        context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
        // Notify the lists to update
        MusicPlayer.refresh();
    }

    public static final String makeShortTimeString(final Context context, long secs) {
        long hours, mins;

        hours = secs / 3600;
        secs %= 3600;
        mins = secs / 60;
        secs %= 60;

        final String durationFormat = context.getResources().getString(
                hours == 0 ? R.string.durationformatshort : R.string.durationformatlong);
        return String.format(durationFormat, hours, mins, secs);
    }

    public static final String makeLabel(final Context context, final int pluralInt,
                                         final int number) {
        return context.getResources().getQuantityString(pluralInt, number, number);
    }

    public static long[] getSongIdsFromSongsList(List<Song> songs) {
        long[] ret = new long[songs.size()];
        for (int i = 0; i < songs.size(); i++) {
            ret[i] = songs.get(i).id;
        }
        return ret;
    }

    public static int getSongsDuration(List<Song> songs) {
        int totalDuration = 0;
        for (Song song : songs) {
            totalDuration += song.duration;
        }
        return totalDuration;
    }
}
