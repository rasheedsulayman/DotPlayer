package com.r4sh33d.musicslam.playback;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.app.NotificationCompat;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.utils.SlamUtils;
import com.r4sh33d.musicslam.activities.MainActivity;
import com.r4sh33d.musicslam.customglide.audiocover.AudioCoverImage;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.r4sh33d.musicslam.playback.Constants.NEXT_ACTION;
import static com.r4sh33d.musicslam.playback.Constants.PREVIOUS_ACTION;
import static com.r4sh33d.musicslam.playback.Constants.TOGGLEPAUSE_ACTION;

public class NotificationHelper {

    private boolean isNotificationCancelled = false;
    private int mNotifyMode = NOTIFY_MODE_NONE;
    private long mNotificationPostTime = 0;
    private static final int NOTIFY_MODE_NONE = 0;
    private static final int NOTIFY_MODE_FOREGROUND = 1;
    private static final int NOTIFY_MODE_BACKGROUND = 2;
    private SimpleTarget<Bitmap> target;

    private static final String NOTIFICATION_CHANEL_ID = "music_slam_playback_notification";
    private MusicService service;
    private NotificationManager mNotificationManager;

    public NotificationHelper(MusicService service) {
        this.service = service;
        mNotificationManager = (NotificationManager) service.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setUPNotificationChannel();
        }
    }

    public synchronized void updateNotification() {
        isNotificationCancelled = false;
        boolean isPlaying = service.isPlaying();
        int playButtonResId = isPlaying ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp;
        int playButtonTitleResId = isPlaying ? R.string.pause : R.string.play;
        final String albumName = service.getCurrentTrackAlbumName();
        final String artistName = service.getArtistName();
        final String secondLineText = TextUtils.isEmpty(albumName)
                ? artistName : artistName + " - " + albumName;
        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle()
                .setMediaSession(service.getMediasSessionToken())
                .setShowActionsInCompactView(0, 1, 2);
        Intent nowPlayingIntent = new Intent(service, MainActivity.class);
               nowPlayingIntent .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent clickIntent = PendingIntent.getActivity(service, 0, nowPlayingIntent, 0);
        if (mNotificationPostTime == 0) {
            mNotificationPostTime = System.currentTimeMillis();
        }
        android.support.v4.app.NotificationCompat.Builder notificationBuilder =
                new android.support.v4.app.NotificationCompat.Builder(service, NOTIFICATION_CHANEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(clickIntent)
                        .setContentTitle(service.getTrackName())
                        .setContentText(secondLineText)
                        .setCategory(android.support.v4.app.NotificationCompat.CATEGORY_SERVICE)
                        .setWhen(mNotificationPostTime)
                        .setShowWhen(false)
                        .setStyle(style)
                        .setVisibility(android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC)
                        .addAction(R.drawable.ic_skip_previous_24dp,
                                service.getString(R.string.prev),
                                retrievePlaybackAction(PREVIOUS_ACTION))
                        .addAction(playButtonResId, service.getString(playButtonTitleResId),
                                retrievePlaybackAction(TOGGLEPAUSE_ACTION))
                        .addAction(R.drawable.ic_skip_next_24dp,
                                service.getString(R.string.next),
                                retrievePlaybackAction(NEXT_ACTION));
        service.runOnUiThread(() -> {
            if (target != null) {
                Glide.with(service).clear(target);
            }
            int imageSize = SlamUtils.dpToPx(128, service);
            target = Glide.with(service)
                    .asBitmap()
                    .load(new AudioCoverImage(service.getCurrentSongPath()))
                    .into(new SimpleTarget<Bitmap>(imageSize, imageSize) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            updateNotification(resource);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            updateNotification(BitmapFactory.decodeResource(service.getResources(),
                                    R.drawable.default_artwork_dark_small));
                        }

                        void updateNotification(Bitmap bitmap) {
                            if (isNotificationCancelled) {
                                return;
                            }
                            notificationBuilder.setLargeIcon(bitmap);
                            postNotification(notificationBuilder.build());
                        }
                    });
        });
    }

    private PendingIntent retrievePlaybackAction(final String action) {
        final ComponentName serviceName = new ComponentName(service, MusicService.class);
        Intent intent = new Intent(action);
        intent.setComponent(serviceName);
        return PendingIntent.getService(service, 0, intent, 0);
    }


    private void postNotification(Notification notification) {
        final int newNotifyMode;
        if (service.isPlaying()) {
            newNotifyMode = NOTIFY_MODE_FOREGROUND;
        } else if (service.recentlyPlayed()) {
            newNotifyMode = NOTIFY_MODE_BACKGROUND;
        } else {
            newNotifyMode = NOTIFY_MODE_NONE;
        }

        int notificationId = hashCode();
        if (mNotifyMode != newNotifyMode) {
            if (mNotifyMode == NOTIFY_MODE_FOREGROUND) {
                service.stopForeground(newNotifyMode == NOTIFY_MODE_NONE);
            } else if (newNotifyMode == NOTIFY_MODE_NONE) {
                mNotificationManager.cancel(notificationId);
                mNotificationPostTime = 0;
            }
        }

        if (newNotifyMode == NOTIFY_MODE_FOREGROUND) {
            service.startForeground(notificationId, notification);
        } else if (newNotifyMode == NOTIFY_MODE_BACKGROUND) {
            mNotificationManager.notify(notificationId, notification);
        }
        mNotifyMode = newNotifyMode;
    }


    @TargetApi(Build.VERSION_CODES.O)
    private void setUPNotificationChannel() {
        CharSequence name = service.getString(R.string.notification_channel_name);
        String description = service.getString(R.string.notification_channel_description);
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANEL_ID, name,
                NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(description);
        channel.enableVibration(false);
        channel.enableLights(false);
        mNotificationManager.createNotificationChannel(channel);
    }

    public synchronized void cancelNotification() {
        isNotificationCancelled = true;
        service.stopForeground(true);
        mNotificationManager.cancel(hashCode());
        mNotificationPostTime = 0;
        mNotifyMode = NOTIFY_MODE_NONE;
    }
}
