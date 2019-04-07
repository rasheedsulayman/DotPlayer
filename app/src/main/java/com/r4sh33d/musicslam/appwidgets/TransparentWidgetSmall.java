package com.r4sh33d.musicslam.appwidgets;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.r4sh33d.musicslam.GlideApp;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.activities.MainActivity;
import com.r4sh33d.musicslam.customglide.audiocover.AudioCoverImage;
import com.r4sh33d.musicslam.playback.MusicService;

import static com.r4sh33d.musicslam.playback.Constants.NEXT_ACTION;
import static com.r4sh33d.musicslam.playback.Constants.PREVIOUS_ACTION;
import static com.r4sh33d.musicslam.playback.Constants.TOGGLEPAUSE_ACTION;


public class TransparentWidgetSmall extends BaseAppWidget {

    private static TransparentWidgetSmall sInstance;
    private SimpleTarget<Bitmap> artworkTarget;
    public static final String TYPE = "small_widget";
    int artWorkImageSizeDP = 0;

    public static synchronized TransparentWidgetSmall getInstance() {
        if (sInstance == null) {
            sInstance = new TransparentWidgetSmall();
        }
        return sInstance;
    }

    @Override
    public int getWidgetLayoutRes() {
        return R.layout.transparent_widget_small;
    }

    /**
     * Update all active widget instances by pushing changes
     */
    @Override
    public void performUpdate(final MusicService service, final int[] appWidgetIds) {
        final RemoteViews remoteViews = new RemoteViews(service.getPackageName(),
                R.layout.transparent_widget_small);

        final CharSequence trackName = service.getTrackName();
        final CharSequence artistName = service.getArtistName();

        remoteViews.setTextViewText(R.id.song_title, trackName);
        remoteViews.setTextViewText(R.id.song_artist, artistName);
        remoteViews.setImageViewResource(R.id.play_pause, getImageResourceForPlayPause(service));
        linkButtons(service, remoteViews);

        if (artWorkImageSizeDP == 0) {
            artWorkImageSizeDP = service.getResources().getDimensionPixelSize(R.dimen.app_widget_small_image_size);
        }

        Context applicationContext = service.getApplicationContext();
        service.runOnUiThread(() -> {
            if (artworkTarget != null) {
                GlideApp.with(applicationContext).clear(artworkTarget);
            }
            artworkTarget = GlideApp.with(applicationContext)
                    .asBitmap()
                    .load(new AudioCoverImage(service.getCurrentSongPath()))
                    .into(new SimpleTarget<Bitmap>(artWorkImageSizeDP, artWorkImageSizeDP) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            updateWidget(resource);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            updateWidget(null);
                        }

                        void updateWidget(Bitmap bitmap) {
                            if (bitmap != null) {
                                remoteViews.setImageViewBitmap(R.id.album_art, bitmap);
                            } else {
                                remoteViews.setImageViewResource(R.id.album_art, R.drawable.default_artwork);
                            }
                            pushUpdate(service, appWidgetIds, remoteViews);
                        }
                    });
        });
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public void linkButtons(final Context context, final RemoteViews views) {
        Intent action;
        PendingIntent pendingIntent;
        final ComponentName serviceName = new ComponentName(context, MusicService.class);

        // Home
        action = new Intent(context, MainActivity.class);
        action.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pendingIntent = PendingIntent.getActivity(context, 0, action, 0);
        views.setOnClickPendingIntent(R.id.album_art, pendingIntent);

        // Previous track
        pendingIntent = buildPendingIntent(context, PREVIOUS_ACTION, serviceName);
        views.setOnClickPendingIntent(R.id.previous, pendingIntent);

        // Play and pause
        pendingIntent = buildPendingIntent(context, TOGGLEPAUSE_ACTION, serviceName);
        views.setOnClickPendingIntent(R.id.play_pause, pendingIntent);

        // Next track
        pendingIntent = buildPendingIntent(context, NEXT_ACTION, serviceName);
        views.setOnClickPendingIntent(R.id.next, pendingIntent);
    }

    public int getImageResourceForPlayPause(MusicService service) {
        return service.isPlaying() ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp;
    }
}
