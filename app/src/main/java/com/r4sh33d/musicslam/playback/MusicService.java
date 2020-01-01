package com.r4sh33d.musicslam.playback;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.r4sh33d.musicslam.appwidgets.TransparentWidgetLarge;
import com.r4sh33d.musicslam.appwidgets.TransparentWidgetSmall;
import com.r4sh33d.musicslam.appwidgets.WhiteWidgetLargeWidgetLarge;
import com.r4sh33d.musicslam.customglide.audiocover.AudioCoverImage;
import com.r4sh33d.musicslam.db.MusicPlaybackQueueStore;
import com.r4sh33d.musicslam.db.RecentStore;
import com.r4sh33d.musicslam.db.SongPlayCount;
import com.r4sh33d.musicslam.models.Song;
import com.r4sh33d.musicslam.sleeptimer.SleepTimer;
import com.r4sh33d.musicslam.utils.PrefsUtils;
import com.r4sh33d.musicslam.utils.SlamUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

/**
 * @author Rasheed Sulayman (r4sh33d), Andrew Neal, Karim Abou Zeid (kabouzeid)
 */
public class MusicService extends Service {

    private static final String TAG = "MusicPlaybackService";
    private static final int IDLE_DELAY = 5 * 60 * 1000;
    private static final long REWIND_INSTEAD_PREVIOUS_THRESHOLD = 3000;
    private final IBinder mBinder = new MusicBinder();
    private final TransparentWidgetLarge mTransparentAppWidget = TransparentWidgetLarge.getInstance();
    private final WhiteWidgetLargeWidgetLarge mWhiteAppWidget = WhiteWidgetLargeWidgetLarge.getInstance();
    private final TransparentWidgetSmall mTransparentWidgetSmall = TransparentWidgetSmall.getInstance();
    List<Song> playingQueue = new ArrayList<>();
    List<Song> playingQueueBackup = new ArrayList<>();
    NotificationHelper notificationHelper;
    IntentFilter audioBecomingNoisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    long playBackStateActions = PlaybackStateCompat.ACTION_PLAY |
            PlaybackStateCompat.ACTION_PLAY_PAUSE |
            PlaybackStateCompat.ACTION_PAUSE |
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
            PlaybackStateCompat.ACTION_STOP;
    private MediaSessionCompat mediaSessionCompat;
    private AudioManager mAudioManager;
    private AlarmManager mAlarmManager;
    private PendingIntent mShutdownIntent;
    private boolean mShutdownScheduled;
    private MultiPlayer mPlayer;
    private String mFileToPlay;
    private int mCardId;
    private boolean mQueueIsSaveable = true;
    private PrefsUtils prefsUtils;
    private int mMediaMountedCount = 0;
    private boolean mReadGranted = false;
    private RecentStore mRecentsCache;
    private SongPlayCount mSongPlayCountCache;
    private MusicPlaybackQueueStore mPlaybackStateStore;
    private boolean mServiceInUse = false;
    private boolean mIsSupposedToBePlaying = false;
    private long mLastPlayedTime;
    private boolean mPausedByTransientLossOfFocus = false;
    private int mPlayPos = -1;
    private int mNextPlayPos = -1;
    private int mOpenFailedCounter = 0;
    private int mShuffleMode = Constants.SHUFFLE_NONE;
    private int mRepeatMode = Constants.REPEAT_NONE;
    private int mServiceStartId = -1;
    private MusicPlayerHandler mPlayerHandler;
    private final OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(final int focusChange) {
            mPlayerHandler.obtainMessage(Constants.FOCUSCHANGE, focusChange, 0).sendToTarget();
        }
    };
    private HandlerThread mHandlerThread;
    private Handler uiHandler;
    private PlayingQueueSaverHandler playingQueueSaverHandler;
    private HandlerThread playingQueueSaverHandlerThread;
    private SleepTimer sleepTimer;
    private Point displaySize;
    private BroadcastReceiver mUnmountReceiver = null;
    private BroadcastReceiver audioBecomingNoisyBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            asyncPause();
            mPausedByTransientLossOfFocus = false;
        }
    };
    private boolean audioBecomingNoisyReceiverRegistered;
    private BroadcastReceiver appwidgetUpdatesBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String command = intent.getStringExtra(Constants.EXTRA_WIDGET_TYPE);
            switch (command) {
                case TransparentWidgetLarge.TYPE:
                    final int[] transparentWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                    mTransparentAppWidget.performUpdate(MusicService.this, transparentWidgetIds);
                    break;
                case WhiteWidgetLargeWidgetLarge.TYPE:
                    final int[] wightLargeWidgetIds = intent
                            .getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                    mWhiteAppWidget.performUpdate(MusicService.this, wightLargeWidgetIds);
                    break;
                case TransparentWidgetSmall.TYPE:
                    final int[] smallWidgetIds = intent
                            .getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                    mTransparentWidgetSmall.performUpdate(MusicService.this, smallWidgetIds);
                    break;
            }
        }
    };
    private String PREF_KEY_PLAY_POSITION = "PREF_KEY_PLAY_POSITION";
    private String PREF_KEY_SEEK_POSITION = "PREF_KEY_SEEK_POSITION";
    private ContentObserver mMediaStoreObserver;

    @Override
    public IBinder onBind(final Intent intent) {
        cancelShutdown();
        mServiceInUse = true;
        return mBinder;
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        mServiceInUse = false;
        saveQueue(true);
        if (mReadGranted) {
            if (mIsSupposedToBePlaying || mPausedByTransientLossOfFocus) {
                return true;
            } else if (playingQueue.size() > 0 || mPlayerHandler.hasMessages(Constants.TRACK_ENDED)) {
                scheduleDelayedShutdown();
                return true;
            }
        }
        stopSelf(mServiceStartId);
        return true;
    }

    @Override
    public void onRebind(final Intent intent) {
        cancelShutdown();
        mServiceInUse = true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("OnCreate");
        if (ContextCompat.checkSelfPermission(this, permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        } else {
            mReadGranted = true;
        }

        //Database stores
        mRecentsCache = RecentStore.getInstance(this);
        mSongPlayCountCache = SongPlayCount.getInstance(this);
        mPlaybackStateStore = MusicPlaybackQueueStore.getInstance(this);

        //Threads and Handlers
        mHandlerThread = new HandlerThread("MusicPlayerHandler", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        mPlayerHandler = new MusicPlayerHandler(this, mHandlerThread.getLooper());
        uiHandler = new Handler(Looper.getMainLooper());

        playingQueueSaverHandlerThread = new HandlerThread("PlayingQueueSaveHandler", Process.THREAD_PRIORITY_BACKGROUND);
        playingQueueSaverHandlerThread.start();
        playingQueueSaverHandler = new PlayingQueueSaverHandler(this, playingQueueSaverHandlerThread.getLooper());

        //Initialize the Media player
        mPlayer = new MultiPlayer(this);
        mPlayer.setHandler(mPlayerHandler);

        //Misc
        displaySize = SlamUtils.getDefaultDisplaySize(this);

        //External storage and mediastrore observers
        registerExternalStorageListener();
        mMediaStoreObserver = new MediaStoreObserver(mPlayerHandler);
        getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.INTERNAL_CONTENT_URI, true, mMediaStoreObserver);
        getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, mMediaStoreObserver);

        //MediaSession, Notification and App widgets
        setUpMediaSession();
        notificationHelper = new NotificationHelper(this);
        registerReceiver(appwidgetUpdatesBroadcastReceiver, new IntentFilter(Constants.ACTION_UPDATE_APP_WIDGETS));
        sleepTimer = new SleepTimer(this);

        //Shared prefs
        prefsUtils = PrefsUtils.getInstance(this);
        mCardId = getCardId();

        //System services
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        //Shutdown intent
        final Intent shutdownIntent = new Intent(this, MusicService.class);
        shutdownIntent.setAction(Constants.SHUTDOWN_ACTION);
        mShutdownIntent = PendingIntent.getService(this, 0, shutdownIntent, 0);
        scheduleDelayedShutdown();

        //Restore queue
        reloadQueue();
        notifyChange(Constants.QUEUE_CHANGED);
        notifyChange(Constants.META_CHANGED);
    }

    @SuppressLint("NewApi")
    @Override
    public void onDestroy() {
        if (!mReadGranted) {
            return;
        }
        Timber.d("onDestroy called");
        super.onDestroy();

        if (audioBecomingNoisyReceiverRegistered) {
            unregisterReceiver(audioBecomingNoisyBroadcastReceiver);
            audioBecomingNoisyReceiverRegistered = false;
        }

        final Intent audioEffectsIntent = new Intent(
                AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(audioEffectsIntent);

        mAlarmManager.cancel(mShutdownIntent);

        mPlayerHandler.removeCallbacksAndMessages(null);
        mHandlerThread.quitSafely();
        playingQueueSaverHandler.removeCallbacksAndMessages(null);
        playingQueueSaverHandlerThread.quitSafely();

        mPlayer.release();
        mPlayer = null;

        sleepTimer.tearDown();
        mAudioManager.abandonAudioFocus(mAudioFocusListener);
        mediaSessionCompat.release();

        getContentResolver().unregisterContentObserver(mMediaStoreObserver);
        unregisterReceiver(appwidgetUpdatesBroadcastReceiver);

        if (mUnmountReceiver != null) {
            unregisterReceiver(mUnmountReceiver);
            mUnmountReceiver = null;
        }
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        mServiceStartId = startId;

        if (intent != null) {
            final String action = intent.getAction();

            if (Constants.SHUTDOWN_ACTION.equals(action)) {
                mShutdownScheduled = false;
                releaseServiceUiAndStop();
                //we want to shutdown anyway, don't try to restart the service again if killed by system.
                return START_NOT_STICKY;
            }
            handleCommandIntent(intent);
        }

        scheduleDelayedShutdown();
        return START_STICKY; //try to restart if we are killed by system.
    }

    private void releaseServiceUiAndStop() {
        if (isPlaying() || mPausedByTransientLossOfFocus || mPlayerHandler.hasMessages(Constants.TRACK_ENDED)) {
            return;
        }
        notificationHelper.cancelNotification();
        mAudioManager.abandonAudioFocus(mAudioFocusListener);
        mediaSessionCompat.setActive(false);
        if (!mServiceInUse) {
            saveQueue(true);
            stopSelf(mServiceStartId);
        }
    }

    private void handleCommandIntent(Intent intent) {
        final String action = intent.getAction();
        if (Constants.NEXT_ACTION.equals(action)) {
            asyncNext(true);
        } else if (Constants.PREVIOUS_ACTION.equals(action)
                || Constants.PREVIOUS_FORCE_ACTION.equals(action)) {
            asyncPrevious(Constants.PREVIOUS_FORCE_ACTION.equals(action));
        } else if (Constants.TOGGLEPAUSE_ACTION.equals(action)) {
            asyncTogglePlayPause();
        } else if (Constants.PAUSE_ACTION.equals(action)) {
            asyncPause();
            mPausedByTransientLossOfFocus = false;
        } else if (Constants.STOP_ACTION.equals(action)) {
            asyncPause();
            mPausedByTransientLossOfFocus = false;
            asyncSeek(0);
            releaseServiceUiAndStop();
        } else if (Constants.REPEAT_ACTION.equals(action)) {
            cycleRepeat();
        } else if (Constants.SHUFFLE_ACTION.equals(action)) {
            cycleShuffle();
        }
        MediaButtonReceiver.handleIntent(mediaSessionCompat, intent);
    }

    private void notifyChange(final String what) {
        Timber.d("notifyChange: what = %s", what);
        //First, Handle the change internally
        switch (what) {
            case Constants.PLAY_STATE_CHANGED:
                notificationHelper.updateNotification();
                break;
            case Constants.META_CHANGED:
                // Add the track to the recently played list.
                mRecentsCache.addSongId(getAudioId());
                mSongPlayCountCache.bumpSongCount(getAudioId());
                notificationHelper.updateNotification();
                saveQueue(false);
                break;
            case Constants.QUEUE_CHANGED:
                asyncSaveQueues();
                asyncSetNextTrack();
                break;
        }

        //Then Send the change to other components
        //irrespective of the type of change, we want to update these
        sendExternalBroadcast(what);
        sendInternalBroadcast(what);
        updateMediaSession(what);
        mTransparentAppWidget.notifyChange(this, what);
        mTransparentWidgetSmall.notifyChange(this, what);
        mWhiteAppWidget.notifyChange(this, what);
    }

    private void sendExternalBroadcast(String what) {
        //Wanna fake this broadcast as if it is coming from the stock music player
        String action = what.replace(Constants.MUSIC_SLAM_PACKAGE_NAME, Constants.MUSIC_PACKAGE_NAME);
        final Intent intent = new Intent(action);
        intent.putExtras(getPublicIntentExtra());
        sendStickyBroadcast(intent);
    }

    private void sendInternalBroadcast(String what) {
        Intent intent = new Intent(what);
        sendBroadcast(intent);
    }

    private Bundle getPublicIntentExtra() {
        Bundle bundle = new Bundle();
        bundle.putLong("id", getAudioId());
        bundle.putString("artist", getArtistName());
        bundle.putString("album", getCurrentTrackAlbumName());
        bundle.putString("track", getTrackName());
        bundle.putBoolean("playing", isPlaying());
        return bundle;
    }

    private void setUpMediaSession() {
        mediaSessionCompat = new MediaSessionCompat(this, "MusicSlam");
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                asyncPlay();
            }

            @Override
            public void onPause() {
                asyncPause();
                mPausedByTransientLossOfFocus = false;
            }

            @Override
            public void onSkipToNext() {
                asyncNext(true);
            }

            @Override
            public void onSkipToPrevious() {
                asyncPrevious(false);
            }

            @Override
            public void onStop() {
                asyncPause();
                mPausedByTransientLossOfFocus = false;
                asyncSeek(0);
                releaseServiceUiAndStop();
            }

            @Override
            public void onSeekTo(long pos) {
                asyncSeek(pos);
            }
        });
    }

    private int getCardId() {
        final ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(Uri.parse("content://media/external/fs_id"),
                null, null, null, null);
        int mCardId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            mCardId = cursor.getInt(0);
            cursor.close();
            cursor = null;
        }
        return mCardId;
    }

    public void runOnUiThread(Runnable runnable) {
        uiHandler.post(runnable);
    }

    public void closeExternalStorageFiles(final String storagePath) {
        stop(true);
        notifyChange(Constants.QUEUE_CHANGED);
        notifyChange(Constants.META_CHANGED);
    }

    public void registerExternalStorageListener() {
        if (mUnmountReceiver == null) {
            mUnmountReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(final Context context, final Intent intent) {
                    final String action = intent.getAction();
                    if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                        saveQueue(true);
                        mQueueIsSaveable = false;
                        closeExternalStorageFiles(intent.getData().getPath()); //stops playback
                    } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                        mMediaMountedCount++;
                        mCardId = getCardId();
                        reloadQueue();
                        mQueueIsSaveable = true;
                        notifyChange(Constants.QUEUE_CHANGED);
                        notifyChange(Constants.META_CHANGED);
                    }
                }
            };
            final IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_MEDIA_EJECT);
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            filter.addDataScheme("file");
            registerReceiver(mUnmountReceiver, filter);
        }
    }

    private void scheduleDelayedShutdown() {
        if (!mReadGranted) {
            return;
        }
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + IDLE_DELAY, mShutdownIntent);
        mShutdownScheduled = true;
    }

    private void cancelShutdown() {
        if (mShutdownScheduled) {
            mAlarmManager.cancel(mShutdownIntent);
            mShutdownScheduled = false;
        }
    }

    private void stop(final boolean goToIdle) {
        if (mPlayer.isInitialized()) {
            mPlayer.stop();
        }
        mFileToPlay = null;
        if (goToIdle) {
            setIsSupposedToBePlaying(false, false);
        } else {
            stopForeground(false);
        }
    }

    /**
     * @param first The first file to be removed
     * @param last  The last file to be removed
     * @return the number of tracks deleted
     */
    private int removeTracksInternal(int first, int last) {
        synchronized (this) {
            if (last < first) {
                return 0;
            } else if (first < 0) {
                first = 0;
            } else if (last >= playingQueue.size()) {
                last = playingQueue.size() - 1;
            }

            boolean goToNext = false;

            if (first <= mPlayPos && mPlayPos <= last) { //if play position is inside the interval to delete
                mPlayPos = first;
                goToNext = true;
            } else if (mPlayPos > last) {
                mPlayPos -= last - first + 1; //move the position back to the correct index
            }

            final int numToRemove = last - first + 1;

            if (first == 0 && last == playingQueue.size() - 1) {//remove all from the playlist
                mPlayPos = -1;
                mNextPlayPos = -1;
                playingQueueBackup.clear();
                playingQueue.clear();
            } else {
                for (int i = 0; i < numToRemove; i++) {
                    playingQueueBackup.remove(playingQueue.remove(first));
                }
            }

            if (goToNext) {
                if (playingQueue.size() == 0) {
                    stop(true);
                    mPlayPos = -1;
                } else {
                    if (mShuffleMode != Constants.SHUFFLE_NONE) {
                        mPlayPos = getNextPosition(true);
                    } else if (mPlayPos >= playingQueue.size()) {
                        mPlayPos = 0;
                    }
                    final boolean wasPlaying = isPlaying();
                    stop(false);
                    openCurrentAndNext();
                    if (wasPlaying) {
                        play();
                    }
                }
                notifyChange(Constants.META_CHANGED);
            }
            return numToRemove;
        }
    }

    public List<Song> getPlayingQueue() {
        return playingQueue;
    }

    public synchronized Song getSongAt(int index) {
        synchronized (this) {
            if (index >= 0 && index < playingQueue.size()) {
                return getPlayingQueue().get(index);
            }
            return Song.EMPTY_SONG;
        }
    }

    public Song getCurrentSong() {
        return getSongAt(mPlayPos);
    }

    private void openCurrentAndNext() {
        openCurrentAndMaybeNext(true);
    }

    private void openCurrentAndMaybeNext(final boolean openNext) {
        synchronized (this) {
            if (playingQueue.size() == 0) {
                return;
            }
            boolean shutdown = false;
            while (true) {
                if (openFile(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/"
                        + getSongAt(mPlayPos).id)) {
                    break;
                }
                // closeCursor();
                if (mOpenFailedCounter++ < 10 && playingQueue.size() > 1) {
                    final int pos = getNextPosition(false);
                    if (pos < 0) {
                        shutdown = true;
                        break;
                    }
                    mPlayPos = pos;
                    stop(false);
                    mPlayPos = pos;
                } else {
                    mOpenFailedCounter = 0;
                    Log.w(TAG, "Failed to open file for playback");
                    shutdown = true;
                    break;
                }
            }

            if (shutdown) {
                scheduleDelayedShutdown();
                if (mIsSupposedToBePlaying) {
                    mIsSupposedToBePlaying = false;
                    notifyChange(Constants.PLAY_STATE_CHANGED);
                }
            } else if (openNext) {
                setNextTrack();
            }
        }
    }

    private void sendErrorMessage(final String trackName) {
        final Intent i = new Intent(Constants.TRACK_ERROR);
        i.putExtra(Constants.EXTRA_TRACK_NAME, trackName);
        sendBroadcast(i);
    }

    private int getNextPosition(final boolean force) {
        int currentPosition = getQueuePosition();
        switch (getRepeatMode()) {
            case Constants.REPEAT_ALL:
                if (isLastTrack()) {
                    return 0; //Wanna start again from the top
                }
                break;
            case Constants.REPEAT_CURRENT:
                if (!force) {
                    return currentPosition;
                } else {
                    if (isLastTrack()) {
                        return 0;
                    }
                }
                break;
            case Constants.REPEAT_NONE:
                if (isLastTrack()) {
                    return currentPosition;
                }
                break;
        }
        return currentPosition + 1;
    }

    private boolean isLastTrack() {
        return getQueuePosition() == getPlayingQueue().size() - 1;
    }

    private void setNextTrack() {
        setNextTrack(getNextPosition(false));
    }

    private void setNextTrack(int position) {
        mNextPlayPos = position;
        if (mNextPlayPos >= 0 && playingQueue != null && mNextPlayPos < playingQueue.size()) {
            final long id = playingQueue.get(mNextPlayPos).id;
            mPlayer.setNextDataSource(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + id);
        } else {
            mPlayer.setNextDataSource(null);
        }
    }

    private void updateMediaSession(final String what) {
        switch (what) {
            case Constants.PLAY_STATE_CHANGED:
                int playState = mIsSupposedToBePlaying
                        ? PlaybackStateCompat.STATE_PLAYING
                        : PlaybackStateCompat.STATE_PAUSED;
                mediaSessionCompat.setPlaybackState(new PlaybackStateCompat.Builder()
                        .setActions(playBackStateActions)
                        .setState(playState, position(), 1.0f).build());
                break;
            case Constants.META_CHANGED:
            case Constants.QUEUE_CHANGED:
                MediaMetadataCompat.Builder metadataBuilder =
                        new MediaMetadataCompat.Builder()
                                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, getCurrentTrackAlbumName())
                                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, getTrackName())
                                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, getQueuePosition() + 1)
                                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, getQueue().size())
                                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, getArtistName())
                                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, getArtistName())
                                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, getCurrentSongDuration());
                if (prefsUtils.enableLockScreenArtWork()) {
                    //TODO come back and clear this target, Investigate the OOM
                    runOnUiThread(() -> Glide.with(this).asBitmap().
                            load(new AudioCoverImage(getCurrentSongPath()))
                            .into(new SimpleTarget<Bitmap>(displaySize.x / 4, displaySize.y / 4) {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource,
                                                            @Nullable Transition<? super Bitmap> transition) {
                                    try {
                                        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                                                SlamUtils.copyBitmap(resource));
                                        mediaSessionCompat.setMetadata(metadataBuilder.build());
                                    } catch (OutOfMemoryError error) {
                                        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, null);
                                        mediaSessionCompat.setMetadata(metadataBuilder.build());
                                    }
                                }

                                @Override
                                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                    super.onLoadFailed(errorDrawable);
                                    //We give up on the artwork
                                    mediaSessionCompat.setMetadata(metadataBuilder.build());
                                }
                            }));
                } else {
                    mediaSessionCompat.setMetadata(metadataBuilder.build());
                }
                break;
        }
    }

    public List<Song> getQueue() {
        return playingQueue;
    }

    public synchronized MediaSessionCompat.Token getMediasSessionToken() {
        if (mediaSessionCompat != null) {
            return mediaSessionCompat.getSessionToken();
        }
        return null;
    }

    private void saveQueue(final boolean full) {
        if (!mQueueIsSaveable || prefsUtils == null) {
            return;
        }

        if (full) {
            mPlaybackStateStore.saveQueues(playingQueue, playingQueueBackup);
            prefsUtils.putInt("cardid", mCardId);
        }
        prefsUtils.putInt(PREF_KEY_PLAY_POSITION, mPlayPos);
        if (mPlayer.isInitialized()) {
            prefsUtils.putLong(PREF_KEY_SEEK_POSITION, mPlayer.position());
        }
        prefsUtils.putInt("repeatmode", mRepeatMode);
        prefsUtils.putInt("shufflemode", mShuffleMode);
    }

    private void reloadQueue() {
        int id = mCardId;
        if (prefsUtils.contains("cardid")) {
            id = prefsUtils.getInt("cardid", ~mCardId);
        }
        if (id != mCardId) {
            return;
        }
        ArrayList<Song> savedPlayingQueue = mPlaybackStateStore.getSavedPlayingQueue();
        ArrayList<Song> savedOriginalPlayingQueue = mPlaybackStateStore.getSavedOriginalPlayingQueue();
        final int playPosition = prefsUtils.getInt(PREF_KEY_PLAY_POSITION, -1);
        final long seekPosition = prefsUtils.getLong(PREF_KEY_SEEK_POSITION, 0);
        if (savedPlayingQueue.size() > 0 && savedPlayingQueue.size() == savedOriginalPlayingQueue.size()
                && playPosition != -1) {
            playingQueue = savedPlayingQueue;
            playingQueueBackup = savedOriginalPlayingQueue;
            mPlayPos = playPosition;
            openCurrentAndNext();
            seek(seekPosition >= 0 && seekPosition < getCurrentSongDuration() ? seekPosition : 0);
        }
    }

    public boolean openFile(final String path) {
        synchronized (this) {
            if (path == null) {
                return false;
            }
            mFileToPlay = path;
            mPlayer.setDataSource(mFileToPlay);
            if (mPlayer.isInitialized()) {
                mOpenFailedCounter = 0;
                return true;
            }
            String trackName = getTrackName();
            if (TextUtils.isEmpty(trackName)) {
                trackName = path;
            }
            sendErrorMessage(trackName);
            stop(true);
            return false;
        }
    }

    public int getAudioSessionId() {
        synchronized (this) {
            return mPlayer.getAudioSessionId();
        }
    }

    public int getMediaMountedCount() {
        return mMediaMountedCount;
    }

    public int getShuffleMode() {
        return mShuffleMode;
    }

    public void setShuffleMode(final int shuffleMode) {
        switch (shuffleMode) {
            case Constants.SHUFFLE_NORMAL:
                this.mShuffleMode = shuffleMode;
                makeShuffleList(this.getPlayingQueue(), getQueuePosition());
                mPlayPos = 0;
                break;
            case Constants.SHUFFLE_NONE:
                this.mShuffleMode = shuffleMode;
                long currentSongId = getCurrentSong().id;
                playingQueue = new ArrayList<>(playingQueueBackup);
                int newPosition = 0;
                for (Song song : getPlayingQueue()) {
                    if (song.id == currentSongId) {
                        newPosition = getPlayingQueue().indexOf(song);
                    }
                }
                mPlayPos = newPosition;
                break;
        }
        notifyChange(Constants.SHUFFLE_MODE_CHANGED);
        notifyChange(Constants.QUEUE_CHANGED);
    }

    public SleepTimer getSleepTimer() {
        return sleepTimer;
    }

    public int getRepeatMode() {
        return mRepeatMode;
    }

    public void setRepeatMode(final int repeatmode) {
        synchronized (this) {
            mRepeatMode = repeatmode;
            setNextTrack();
            saveQueue(false);
            notifyChange(Constants.REPEAT_MODE_CHANGED);
        }
    }

    /**
     * Removes all instances of the track with the given ID from the playing queue.
     *
     * @param id The id to be removed
     * @return how many instances of the track were removed
     */
    public int removeTrack(final long id) {
        int numremoved = 0;
        synchronized (this) {
            for (int i = 0; i < playingQueue.size(); i++) {
                if (playingQueue.get(i).id == id) {
                    numremoved += removeTracksInternal(i, i);
                    i--;
                }
            }
        }
        if (numremoved > 0) {
            notifyChange(Constants.QUEUE_CHANGED);
        }
        return numremoved;
    }

    /**
     * Removes a song from the play queue at the specified position.
     *
     * @param id       The song id to be removed
     * @param position The position of the song in the playlist
     * @return true if successful
     */
    public boolean removeTrackAtPosition(final long id, final int position) {
        synchronized (this) {
            if (position >= 0 &&
                    position < playingQueue.size() &&
                    playingQueue.get(position).id == id) {
                return removeTracks(position, position) > 0;
            }
        }
        return false;
    }

    /**
     * Removes the range of tracks specified from the play list. If a file
     * within the range is the file currently being played, playback will move
     * to the next file after the range.
     *
     * @param first The first file to be removed
     * @param last  The last file to be removed
     * @return the number of tracks deleted
     */
    public int removeTracks(final int first, final int last) {
        final int numremoved = removeTracksInternal(first, last);
        if (numremoved > 0) {
            notifyChange(Constants.QUEUE_CHANGED);
        }
        return numremoved;
    }

    public int getQueuePosition() {
        synchronized (this) {
            return mPlayPos;
        }
    }

    public String getCurrentSongPath() {
        synchronized (this) {
            return getCurrentSong().data;
        }
    }

    public String getCurrentTrackAlbumName() {
        synchronized (this) {
            return getCurrentSong().albumName;
        }
    }

    public String getTrackName() {
        synchronized (this) {
            return getCurrentSong().title;
        }
    }

    public String getArtistName() {
        synchronized (this) {
            return getCurrentSong().artistName;
        }
    }

    public long getAlbumId() {
        synchronized (this) {
            return getCurrentSong().albumId;
        }
    }

    public long getArtistId() {
        synchronized (this) {
            return getCurrentSong().artistId;
        }
    }

    public long getAudioId() {
        synchronized (this) {
            Song song = getCurrentSong();
            if (song != null) {
                return song.id;
            }
            return -1;
        }
    }

    public long seek(long position) {
        if (mPlayer.isInitialized()) {
            if (position < 0) {
                position = 0;
            } else if (position > mPlayer.duration()) {
                position = mPlayer.duration();
            }
            long result = mPlayer.seek(position);
            return result;
        }
        return -1;
    }

    /**
     * Seeks the current track to a position relative to its current position
     * If the relative position is after or before the track, it will also automatically
     * jump to the previous or next track respectively
     *
     * @param deltaInMs The delta time to seek to in milliseconds
     */
    public void seekRelative(long deltaInMs) {
        synchronized (this) {
            if (mPlayer.isInitialized()) {
                final long newPos = position() + deltaInMs;
                final long duration = getCurrentSongDuration();
                if (newPos < 0) {
                    prev(true);
                    // seek to the new duration + the leftover position
                    seek(getCurrentSongDuration() + newPos);
                } else if (newPos >= duration) {
                    gotoNext(true);
                    // seek to the leftover duration
                    seek(newPos - duration);
                } else {
                    seek(newPos);
                }
            }
        }
    }

    public long position() {
        if (mPlayer.isInitialized()) {
            return mPlayer.position();
        }
        return -1;
    }

    public long getCurrentSongDuration() {
        if (mPlayer.isInitialized()) {
            return mPlayer.duration();
        }
        return -1;
    }

    public int getQueueSize() {
        synchronized (this) {
            return playingQueue.size();
        }
    }

    public boolean isPlaying() {
        return mIsSupposedToBePlaying;
    }

    private void setIsSupposedToBePlaying(boolean value, boolean notify) {
        if (mIsSupposedToBePlaying != value) {
            mIsSupposedToBePlaying = value;
            // Update mLastPlayed time first and notify afterwards, as
            // the notification listener method needs the up-to-date value
            // for the recentlyPlayed() method to work
            if (!mIsSupposedToBePlaying) {
                scheduleDelayedShutdown();
                mLastPlayedTime = System.currentTimeMillis();
            }

            if (notify) {
                notifyChange(Constants.PLAY_STATE_CHANGED);
            }
        }
    }

    public boolean recentlyPlayed() {
        return isPlaying() || System.currentTimeMillis() - mLastPlayedTime < IDLE_DELAY;
    }

    public void open(List<Song> list, final int position, boolean forceShuffle) {
        synchronized (this) {
            playingQueueBackup = new ArrayList<>(list);
            playingQueue = new ArrayList<>(list);
            mPlayPos = position;
            if (mShuffleMode == Constants.SHUFFLE_NORMAL) {
                makeShuffleList(playingQueue, mPlayPos);
                mPlayPos = 0;
            }
            asyncPrepareAndPlay();
            notifyChange(Constants.QUEUE_CHANGED);
        }
    }

    private void prepareAndPlay() {
        openCurrentAndNext();
        notifyChange(Constants.META_CHANGED);
        play();
    }

    public void stop() {
        stop(true);
    }

    public void play() {
        play(true);
    }

    /**
     * Resumes or starts playback.
     *
     * @param createNewNextTrack True if you want to figure out the next track, false
     *                           if you want to re-use the existing next track (used for going back)
     */
    public void play(boolean createNewNextTrack) {
        int status = mAudioManager.requestAudioFocus(mAudioFocusListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (status != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return;
        }

        final Intent intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(intent);

        mediaSessionCompat.setActive(true);

        if (createNewNextTrack) {
            setNextTrack();
        } else {
            setNextTrack(mNextPlayPos);
        }

        if (mPlayer.isInitialized()) {
            mPlayer.start();
            if (!audioBecomingNoisyReceiverRegistered) {
                registerReceiver(audioBecomingNoisyBroadcastReceiver, audioBecomingNoisyIntentFilter);
                audioBecomingNoisyReceiverRegistered = true;
            }
            mPlayerHandler.removeMessages(Constants.FADEDOWN);
            mPlayerHandler.sendEmptyMessage(Constants.FADEUP);
            setIsSupposedToBePlaying(true, true);
            cancelShutdown();
        }
    }

    private void togglePlayPause() {
        if (isPlaying()) {
            pause();
            mPausedByTransientLossOfFocus = false;
        } else {
            play();
        }
    }

    public void pause() {
        if (mPlayerHandler == null) return;
        synchronized (this) {
            mPlayerHandler.removeMessages(Constants.FADEUP);
            if (mIsSupposedToBePlaying) {
                final Intent intent = new Intent(
                        AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
                intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
                intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
                sendBroadcast(intent);
                mPlayer.pause();
                setIsSupposedToBePlaying(false, true);
            }
        }
    }

    private void makeShuffleList(@NonNull List<Song> list, int current) {
        if (list.isEmpty()) return;
        if (current >= 0) {
            Song currentSong = list.remove(current);
            Collections.shuffle(list);
            list.add(0, currentSong);
        } else {
            Collections.shuffle(list);
        }
    }

    public void setAndRecordPlayPos(int nextPos) {
        synchronized (this) {
            mPlayPos = nextPos;
        }
    }

    public void gotoNext(final boolean force) {
        synchronized (this) {
            if (playingQueue.size() <= 0) {
                scheduleDelayedShutdown();
                return;
            }
            int pos = mNextPlayPos;
            if (pos < 0) {
                pos = getNextPosition(force); //attempt to get next position
            }

            if (pos < 0) {
                setIsSupposedToBePlaying(false, true);
                return;
            }
            // stop(false);
            setAndRecordPlayPos(pos);
            openCurrentAndNext();
            notifyChange(Constants.META_CHANGED);
            play();
        }
    }

    public void prev(boolean forcePrevious) {
        synchronized (this) {
            // if we aren't repeating 1, and we are either early in the song
            // or we want to force go back, then go to the prevous track
            boolean goPrevious = (getRepeatMode() != Constants.REPEAT_CURRENT) &&
                    (position() < REWIND_INSTEAD_PREVIOUS_THRESHOLD || forcePrevious);

            if (goPrevious) {
                int pos = getPreviousPlayPosition(true);
                // if we have no more previous tracks, quit
                if (pos < 0) {
                    return;
                }
                mNextPlayPos = mPlayPos;
                mPlayPos = pos;
                stop(false);// closes cursor too.
                openCurrent();
                play(false);
                notifyChange(Constants.META_CHANGED);
            } else {
                seek(0);
                play(false);
            }
        }
    }

    public int getPreviousPlayPosition(boolean force) {
        int newPosition = getQueuePosition() - 1;
        switch (mRepeatMode) {
            case Constants.REPEAT_ALL:
                if (newPosition < 0) {
                    newPosition = getPlayingQueue().size() - 1;
                }
                break;
            case Constants.REPEAT_CURRENT:
                if (force) {
                    if (newPosition < 0) {
                        newPosition = getPlayingQueue().size() - 1;
                    }
                } else {
                    newPosition = getQueuePosition();
                }
                break;
            default:
            case Constants.REPEAT_NONE:
                if (newPosition < 0) {
                    newPosition = 0;
                }
                break;
        }
        return newPosition;
    }

    private void openCurrent() {
        openCurrentAndMaybeNext(false);
    }

    public void moveQueueItem(int from, int to) {
        if (from == to) return;
        final int currentPosition = getQueuePosition();
        Song songToMove = playingQueue.remove(from);
        playingQueue.add(to, songToMove);
        if (getShuffleMode() == Constants.SHUFFLE_NONE) {
            Song tmpSong = playingQueueBackup.remove(from);
            playingQueueBackup.add(to, tmpSong);
        }
        if (from > currentPosition && to <= currentPosition) {
            mPlayPos = currentPosition + 1;
        } else if (from < currentPosition && to >= currentPosition) {
            mPlayPos = currentPosition - 1;
        } else if (from == currentPosition) {
            mPlayPos = to;
        }
        notifyChange(Constants.QUEUE_CHANGED);
    }

    public void setShuffleModeLight(int shuffleMode) {
        this.mShuffleMode = shuffleMode;
        notifyChange(Constants.SHUFFLE_MODE_CHANGED);
    }

    public void playSongAt(int index) {
        synchronized (this) {
            stop(false);
            mPlayPos = index;
            openCurrentAndNext();
            notifyChange(Constants.META_CHANGED);
            play();
        }
    }

    public void playSongsFromUri(ArrayList<Song> songs) {
        synchronized (this) {
            if (mPlayPos < 0) {
                mPlayPos = 0;
            }
            addToPlayList(songs, mPlayPos);
            notifyChange(Constants.QUEUE_CHANGED);
            stop(false);
            openCurrentAndNext();
            play();
            notifyChange(Constants.META_CHANGED);
        }
    }

    public void enqueue(List<Song> list, final int action) {
        synchronized (this) {
            if (action == Constants.NEXT && mPlayPos + 1 < playingQueue.size()) {
                addToPlayList(list, mPlayPos + 1);
                mNextPlayPos = mPlayPos + 1;
                notifyChange(Constants.QUEUE_CHANGED);
            } else {
                addToPlayList(list, Integer.MAX_VALUE);
                notifyChange(Constants.QUEUE_CHANGED);
            }
        }
    }

    private void addToPlayList(final List<Song> list, int position) {
        synchronized (this) {
            if (position < 0) {
                playingQueue.clear();
                playingQueueBackup.clear();
                position = 0;
            }

            if (position > playingQueue.size()) {
                position = playingQueue.size();
            }

            playingQueue.addAll(position, list);
            playingQueueBackup.addAll(position, list);

            if (playingQueue.size() == 0) {
                notifyChange(Constants.META_CHANGED);
            }
        }
    }

    private void cycleRepeat() {
        if (mRepeatMode == Constants.REPEAT_NONE) {
            setRepeatMode(Constants.REPEAT_ALL);
        } else if (mRepeatMode == Constants.REPEAT_ALL) {
            setRepeatMode(Constants.REPEAT_CURRENT);
            if (mShuffleMode != Constants.SHUFFLE_NONE) {
                setShuffleMode(Constants.SHUFFLE_NONE);
            }
        } else {
            setRepeatMode(Constants.REPEAT_NONE);
        }
    }

    private void cycleShuffle() {
        if (mShuffleMode == Constants.SHUFFLE_NONE) {
            setShuffleMode(Constants.SHUFFLE_NORMAL);
            if (mRepeatMode == Constants.REPEAT_CURRENT) {
                setRepeatMode(Constants.REPEAT_ALL);
            }
        } else if (mShuffleMode == Constants.SHUFFLE_NORMAL || mShuffleMode == Constants.SHUFFLE_AUTO) {
            setShuffleMode(Constants.SHUFFLE_NONE);
        }
    }

    public void refresh() {
        notifyChange(Constants.REFRESH);
    }

    public void playlistChanged() {
        notifyChange(Constants.PLAYLIST_CHANGED);
    }

    public void asyncTogglePlayPause() {
        mPlayerHandler.obtainMessage(Constants.TOGGLE_PLAY_PAUSE_ASYNC).sendToTarget();
    }

    public void asyncNext(boolean force) {
        mPlayerHandler.removeMessages(Constants.GOTO_NEXT_ASYNC, force);
        mPlayerHandler.obtainMessage(Constants.GOTO_NEXT_ASYNC, force).sendToTarget();
    }

    public void asyncPrevious(boolean force) {
        mPlayerHandler.obtainMessage(Constants.GOTO_PREVIOUS_ASYNC, force).sendToTarget();
    }

    public void asyncPlay() {
        mPlayerHandler.removeMessages(Constants.PLAY_ASYNC);
        mPlayerHandler.obtainMessage(Constants.PLAY_ASYNC).sendToTarget();
    }

    public void asyncPause() {
        mPlayerHandler.obtainMessage(Constants.PAUSE_ASYNC).sendToTarget();
    }

    public void asyncSetRepeatMode(final int repeatmode) {
        mPlayerHandler.obtainMessage(Constants.SET_REPEAT_MODE_ASYNC, repeatmode, 0).sendToTarget();
    }

    public void asyncSetShuffleMode(final int shufflemode) {
        mPlayerHandler.obtainMessage(Constants.SET_SHUFFLE_MODE_ASYNC, shufflemode, 0).sendToTarget();
    }

    public void asyncStop(final boolean goToIdle) {
        mPlayerHandler.obtainMessage(Constants.STOP_ASYNC).sendToTarget();
    }

    public void asyncOpenFile(String filename) {
        mPlayerHandler.obtainMessage(Constants.SET_SHUFFLE_MODE_ASYNC, filename).sendToTarget();
    }

    public void asyncOpen(List<Song> list, int position, boolean forceShuffle) {
        mPlayerHandler.removeMessages(Constants.OPEN_ASYNC);
        PlaylistForPlayback playListForPlayback = new PlaylistForPlayback(
                list, position, 0, forceShuffle);
        mPlayerHandler.obtainMessage(Constants.OPEN_ASYNC, playListForPlayback).sendToTarget();
    }

    public void asyncEnqueue(List<Song> list, int action) {
        PlaylistForPlayback playListForPlayback = new PlaylistForPlayback(list, 0, action, false);
        mPlayerHandler.obtainMessage(Constants.ENQUEUE_ASYNC, playListForPlayback).sendToTarget();
    }

    public void asyncMoveQueueItem(int index1, int index2) {
        mPlayerHandler.obtainMessage(Constants.MOVE_QUEUE_ITEM_ASYNC, index1, index2).sendToTarget();
    }

    public void asyncRefresh() {
        mPlayerHandler.obtainMessage(Constants.REFRESH_ASYNC).sendToTarget();
    }

    public void asyncPlaylistChanged() {
        mPlayerHandler.obtainMessage(Constants.PLAYLIST_CHANGED_ASYNC).sendToTarget();
    }

    public void asyncSeek(long position) {
        mPlayerHandler.obtainMessage(Constants.SEEK_ASYNC, position).sendToTarget();
    }

    public void asyncSeekRelative(long deltaInMs) {
        mPlayerHandler.obtainMessage(Constants.SEEK_RELATIVE_ASYNC, deltaInMs).sendToTarget();
    }

    public void asyncPlaySongAt(int position) {
        mPlayerHandler.obtainMessage(Constants.PLAY_SONG_AT_ASYNC, position, 0).sendToTarget();
    }

    public void asyncPlaySongsFromUri(ArrayList<Song> songs) {
        mPlayerHandler.obtainMessage(Constants.PLAY_SONG_FROM_URI_ASYNC, songs).sendToTarget();
    }

    public void asyncCycleShuffle() {
        mPlayerHandler.obtainMessage(Constants.CYCLE_SHUFFLE_ASYNC).sendToTarget();
    }

    public void asyncCycleRepeat() {
        mPlayerHandler.obtainMessage(Constants.CYCLE_REPEAT_ASYNC).sendToTarget();
    }

    public void asyncSetShuffleModeLight(int shuffleMode) {
        mPlayerHandler.obtainMessage(Constants.SET_SHUFFLE_MODE_LIGHT_ASYNC, shuffleMode,
                0).sendToTarget();
    }

    public void asyncPrepareAndPlay() {
        mPlayerHandler.removeMessages(Constants.PREPARE_AND_PLAY_ASYNC);
        mPlayerHandler.obtainMessage(Constants.PREPARE_AND_PLAY_ASYNC).sendToTarget();
    }

    public void asyncSaveQueues() {
        playingQueueSaverHandler.removeMessages(Constants.SAVE_QUEUES);
        playingQueueSaverHandler.obtainMessage(Constants.SAVE_QUEUES).sendToTarget();
    }

    public void asyncSetNextTrack() {
        mPlayerHandler.removeMessages(Constants.SET_NEXT_TRACK);
        mPlayerHandler.obtainMessage(Constants.SET_NEXT_TRACK).sendToTarget();
    }

    private static final class MusicPlayerHandler extends Handler {
        private final WeakReference<MusicService> mService;
        private float mCurrentVolume = 1.0f;

        public MusicPlayerHandler(final MusicService service, final Looper looper) {
            super(looper);
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(final Message msg) {
            final MusicService service = mService.get();
            if (service == null) {
                return;
            }

            synchronized (service) {
                switch (msg.what) {
                    case Constants.FADEDOWN:
                        mCurrentVolume -= .05f;
                        if (mCurrentVolume > .2f) {
                            sendEmptyMessageDelayed(Constants.FADEDOWN, 10);
                        } else {
                            mCurrentVolume = .2f;
                        }
                        service.mPlayer.setVolume(mCurrentVolume);
                        break;
                    case Constants.FADEUP:
                        mCurrentVolume += .01f;
                        if (mCurrentVolume < 1.0f) {
                            sendEmptyMessageDelayed(Constants.FADEUP, 10);
                        } else {
                            mCurrentVolume = 1.0f;
                        }
                        service.mPlayer.setVolume(mCurrentVolume);
                        break;
                    case Constants.SERVER_DIED:
                        if (service.isPlaying()) {
                            final MultiPlayer.TrackErrorInfo info = (MultiPlayer.TrackErrorInfo) msg.obj;
                            service.sendErrorMessage(info.mTrackName);
                            // since the service isPlaying(), we only need to remove the offending
                            // audio track, and the code will automatically play the next track
                            service.removeTrack(info.mId);
                        } else {
                            service.openCurrentAndNext();
                        }
                        break;
                    case Constants.TRACK_WENT_TO_NEXT:
                        service.mPlayPos = service.mNextPlayPos;
                        service.notifyChange(Constants.META_CHANGED); // notifiy  that meta has changed.
                        service.setNextTrack(); //set new Next track
                        break;
                    case Constants.TRACK_ENDED:
                        if (service.mRepeatMode == Constants.REPEAT_CURRENT) {
                            service.seek(0);
                            service.play();
                        } else {
                            service.gotoNext(false);
                        }
                        break;
                    case Constants.FOCUSCHANGE:
                        switch (msg.arg1) {
                            case AudioManager.AUDIOFOCUS_LOSS:
                            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                                if (service.isPlaying()) {
                                    service.mPausedByTransientLossOfFocus =
                                            msg.arg1 == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
                                }
                                service.pause();
                                break;
                            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                                removeMessages(Constants.FADEUP);
                                sendEmptyMessage(Constants.FADEDOWN);
                                break;
                            case AudioManager.AUDIOFOCUS_GAIN:
                                if (!service.isPlaying()
                                        && service.mPausedByTransientLossOfFocus) {
                                    service.mPausedByTransientLossOfFocus = false;
                                    mCurrentVolume = 0f;
                                    service.mPlayer.setVolume(mCurrentVolume);
                                    service.play();
                                } else {
                                    removeMessages(Constants.FADEDOWN);
                                    sendEmptyMessage(Constants.FADEUP);
                                }
                                break;
                            default:
                        }
                        break;

                    //Playback
                    case Constants.SET_NEXT_TRACK:
                        service.setNextTrack();
                        break;
                    case Constants.GOTO_NEXT_ASYNC:
                        service.gotoNext((Boolean) msg.obj);
                        break;
                    case Constants.GOTO_PREVIOUS_ASYNC:
                        service.prev((Boolean) msg.obj);
                        break;
                    case Constants.PLAY_ASYNC:
                        service.play();
                        break;
                    case Constants.PAUSE_ASYNC:
                        service.pause();
                        break;
                    case Constants.SET_REPEAT_MODE_ASYNC:
                        service.setRepeatMode(msg.arg1);
                        break;
                    case Constants.SET_SHUFFLE_MODE_ASYNC:
                        service.setShuffleMode(msg.arg1);
                        break;
                    case Constants.STOP_ASYNC:
                        service.stop();
                        break;
                    case Constants.OPEN_FILE_ASYNC:
                        service.openFile((String) msg.obj);
                        break;
                    case Constants.OPEN_ASYNC:
                        PlaylistForPlayback playback = (PlaylistForPlayback) msg.obj;
                        service.open(playback.list, playback.position, playback.forceShuffle);
                        break;
                    case Constants.ENQUEUE_ASYNC:
                        PlaylistForPlayback playbackListForEnqueue = (PlaylistForPlayback) msg.obj;
                        service.enqueue(playbackListForEnqueue.list, playbackListForEnqueue.action);
                        break;
                    case Constants.MOVE_QUEUE_ITEM_ASYNC:
                        service.moveQueueItem(msg.arg1, msg.arg2);
                        break;
                    case Constants.REFRESH_ASYNC:
                        service.refresh();
                        break;
                    case Constants.PLAYLIST_CHANGED_ASYNC:
                        service.playlistChanged();
                        break;
                    case Constants.SEEK_ASYNC:
                        service.seek((long) msg.obj);
                        break;
                    case Constants.SEEK_RELATIVE_ASYNC:
                        service.seekRelative((long) msg.obj);
                        break;
                    case Constants.PLAY_SONG_AT_ASYNC:
                        service.playSongAt(msg.arg1);
                        break;
                    case Constants.TOGGLE_PLAY_PAUSE_ASYNC:
                        service.togglePlayPause();
                        break;
                    case Constants.PLAY_SONG_FROM_URI_ASYNC:
                        //noinspection unchecked
                        service.playSongsFromUri((ArrayList<Song>) msg.obj);
                        break;
                    case Constants.CYCLE_SHUFFLE_ASYNC:
                        service.cycleShuffle();
                        break;
                    case Constants.CYCLE_REPEAT_ASYNC:
                        service.cycleRepeat();
                        break;
                    case Constants.SET_SHUFFLE_MODE_LIGHT_ASYNC:
                        service.setShuffleModeLight(msg.arg1);
                        break;
                    case Constants.PREPARE_AND_PLAY_ASYNC:
                        service.prepareAndPlay();
                    default:
                        break;
                }
            }
        }
    }

    private static final class PlayingQueueSaverHandler extends Handler {
        @NonNull
        private final WeakReference<MusicService> mService;

        public PlayingQueueSaverHandler(final MusicService service, @NonNull final Looper looper) {
            super(looper);
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            final MusicService service = mService.get();
            if (msg.what == Constants.SAVE_QUEUES) {
                service.saveQueue(true);
            }
        }
    }

    private class MediaStoreObserver extends ContentObserver implements Runnable {
        private static final long REFRESH_DELAY = 500;
        private Handler mHandler;

        public MediaStoreObserver(Handler handler) {
            super(handler);
            mHandler = handler;
        }

        @Override
        public void onChange(boolean selfChange) {
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, REFRESH_DELAY);
        }

        @Override
        public void run() {
            refresh();
        }
    }

    public class MusicBinder extends Binder {
        public MusicService getMusicService() {
            return MusicService.this;
        }
    }
}
