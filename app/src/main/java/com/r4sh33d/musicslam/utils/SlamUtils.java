package com.r4sh33d.musicslam.utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.models.Song;

import java.io.File;

import timber.log.Timber;

public class SlamUtils {

    public static Uri getAlbumArtUri(long albumId) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
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

    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static Point getDefaultDisplaySize(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point(); // size that'll be used to copy the size.
        display.getSize(size);
        return size;
    }


    public static int dpToPx(int dp, Context context) {
        Resources r = context.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public static void shareSong(Song song, Context context) {
        if (song.id == -1) {
            return;
        }
        try {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.setType("audio/*");
            Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName(), new File(song.data));
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            context.startActivity(Intent.createChooser(shareIntent, "Share Music"));
        } catch (IllegalArgumentException e) {
            Timber.d(e);
        }
    }


    public static boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            boolean isConnected = ni != null && ni.getType() == ConnectivityManager.TYPE_WIFI;
            Timber.d("Is Wifi connected: " + isConnected);
            return isConnected;
        }
        return false;
    }

    public static boolean canAutoDownloadArtworks(Context context) {
        PrefsUtils prefsUtils = PrefsUtils.getInstance(context);
        if (prefsUtils.isArtworkAutoDownloadEnabled()) {
            return !prefsUtils.downloadOnWiFiOnly() || isWifiConnected(context);
        }
        return false;
    }


    public static void hideViews(View... views) {
        for (View view : views) {
            view.setVisibility(View.GONE);
        }
    }

    public static void showViews(View... views) {
        for (View view : views) {
            view.setVisibility(View.VISIBLE);
        }
    }

    public static Bitmap copyBitmap(Bitmap bitmap) {
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.RGB_565;
        }
        try {
            return bitmap.copy(config, false);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
}
