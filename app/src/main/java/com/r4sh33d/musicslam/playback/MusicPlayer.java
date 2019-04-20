package com.r4sh33d.musicslam.playback;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.models.Song;
import com.r4sh33d.musicslam.sleeptimer.SleepTimer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.WeakHashMap;

import static com.r4sh33d.musicslam.playback.Constants.LAST;
import static com.r4sh33d.musicslam.playback.Constants.NEXT;
import static com.r4sh33d.musicslam.playback.Constants.REPEAT_ALL;
import static com.r4sh33d.musicslam.playback.Constants.REPEAT_CURRENT;
import static com.r4sh33d.musicslam.playback.Constants.REPEAT_NONE;
import static com.r4sh33d.musicslam.playback.Constants.SHUFFLE_AUTO;
import static com.r4sh33d.musicslam.playback.Constants.SHUFFLE_NONE;
import static com.r4sh33d.musicslam.playback.Constants.SHUFFLE_NORMAL;

public final class MusicPlayer {

    public static MusicService mService = null;
    private static final WeakHashMap<Context, ServiceBinder> mConnectionMap;

    static {
        mConnectionMap = new WeakHashMap<>();
    }

    public MusicPlayer() {
    }

    public static ServiceToken bindToService(final Context context,
                                             final ServiceConnection callback) {
        Activity realActivity = ((Activity) context).getParent();
        if (realActivity == null) {
            realActivity = (Activity) context;
        }
        final ContextWrapper contextWrapper = new ContextWrapper(realActivity);
        contextWrapper.startService(new Intent(contextWrapper, MusicService.class));
        final ServiceBinder binder = new ServiceBinder(callback);
        if (contextWrapper.bindService(
                new Intent().setClass(contextWrapper, MusicService.class), binder, Context.BIND_AUTO_CREATE)) {
            mConnectionMap.put(contextWrapper, binder);
            return new ServiceToken(contextWrapper);
        }
        return null;
    }

    public static void unbindFromService(final ServiceToken token) {
        if (token == null) {
            return;
        }
        final ContextWrapper mContextWrapper = token.mWrappedContext;
        final ServiceBinder mBinder = mConnectionMap.remove(mContextWrapper);
        if (mBinder == null) {
            return;
        }
        mContextWrapper.unbindService(mBinder);
        if (mConnectionMap.isEmpty()) {
            mService = null;
        }
    }


    public static final class ServiceBinder implements ServiceConnection {
        private final ServiceConnection mCallback;

        public ServiceBinder(final ServiceConnection callback) {
            mCallback = callback;
        }

        @Override
        public void onServiceConnected(final ComponentName className, final IBinder service) {
            MusicService.MusicBinder musicBinder = (MusicService.MusicBinder) service;
            mService = musicBinder.getMusicService();
            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName className) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(className);
            }
            mService = null;
        }
    }

    public static final class ServiceToken {
        public ContextWrapper mWrappedContext;

        public ServiceToken(final ContextWrapper context) {
            mWrappedContext = context;
        }
    }

    public static final boolean isPlaybackServiceConnected() {
        return mService != null;
    }


    public static Song getCurrentSong() {
        if (mService == null) {
            return Song.EMPTY_SONG;
        }
        return mService.getCurrentSong();
    }

    public static void next() {
        try {
            if (mService != null) {
                mService.asyncNext(true);
            }
        } catch (final Exception ignored) {
        }
    }

    public static void previous(final Context context, final boolean force) {
        if (mService != null) {
            mService.asyncPrevious(force);
        }
    }

    public static List<Song> getNowPlayingQueue() {
        if (mService == null) {
            return Collections.EMPTY_LIST;
        }
        return mService.getPlayingQueue();
    }

    public static void playOrPause() {
        if (mService != null) {
            if (mService.isPlaying()) {
                mService.asyncPause();
            } else {
                mService.asyncPlay();
            }
        }
    }

    public static void pause() {
        if (mService != null) {
            mService.asyncPause();
        }
    }

    public static void cycleRepeat() {
        if(mService!=null){
            mService.asyncCycleRepeat();
        }
    }

    public static void cycleShuffle() {
        if (mService != null){
            mService.asyncCycleShuffle();
        }
    }

    public static final boolean isPlaying() {
        if (mService != null) {
            return mService.isPlaying();
        }
        return false;
    }

    public static final int getShuffleMode() {
        if (mService != null) {
            return mService.getShuffleMode();
        }
        return 0;
    }

    public static final int getRepeatMode() {
        if (mService != null) {
            return mService.getRepeatMode();
        }
        return 0;
    }

    public static final int getAudioSessionId() {
        if (mService != null) {
            return mService.getAudioSessionId();
        }
        return -1;
    }


    public static final int getQueuePosition() {
        if (mService != null) {
            return mService.getQueuePosition();
        }
        return 0;
    }

    //TODO Try to run this asynchronously
    public static final int removeTrack(final long id) {
        if (mService != null) {
            return mService.removeTrack(id);
        }
        return 0;
    }

    public static final boolean removeTrackAtPosition(final long trackId, final int position) {
        if (mService != null) {
            return mService.removeTrackAtPosition(trackId, position);
        }
        return false;
    }


    public static void playFile(final Context context, final Uri uri) {
        if (uri == null || mService == null) {
            return;
        }
        // If this is a file:// URI, just use the path directly instead
        // of going through the open-from-filedescriptor codepath.
        String filename;
        String scheme = uri.getScheme();
        if ("file".equals(scheme)) {
            filename = uri.getPath();
        } else {
            filename = uri.toString();
        }

        try {
            mService.stop();
            mService.openFile(filename);
            mService.play();
        } catch (final Exception ignored) {
        }
    }

    public static void playAll(List<Song> list, int position,
                               final boolean forceShuffle) {
        if (mService == null || list == null || list.size() == 0) {
            return;
        }
        int startPosition = forceShuffle ? new Random().nextInt(list.size()) : position;
        if (forceShuffle) {
            mService.setShuffleModeLight(SHUFFLE_NORMAL);
        }else {
            mService.setShuffleModeLight(SHUFFLE_NONE);
        }
        mService.open(list, startPosition , forceShuffle);
    }

    public static void playShuffle(List<Song> list) {
        if (mService == null || list == null || list.size() == 0) {
            return;
        }
        playAll(list, -1, true);
    }

    public static void playSong(Song song) {
        if (mService == null) {
            return;
        }
        ArrayList<Song> list = new ArrayList<>();
        list.add(song);
        playAll(list, 0, false);
    }


    public static void playNext(List<Song> list, Context context) {
        if (mService == null) {
            return;
        }
        mService.asyncEnqueue(list, NEXT);
        String message = context.getResources().
                getQuantityString(R.plurals.n_tracks_were_added_to_queue, list.size(), list.size());
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void playNext(Song song, Context context) {
        if (mService == null) {
            return;
        }
        ArrayList<Song> list = new ArrayList<>(1);
        list.add(song);
        mService.asyncEnqueue(list, NEXT);
        String message = context.getResources().
                getQuantityString(R.plurals.n_tracks_were_added_to_queue, list.size(), list.size());
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void addToQueue(final Context context, Song song) {
        if (mService == null) {
            return;
        }
        ArrayList<Song> list = new ArrayList<>(1);
        list.add(song);
        mService.asyncEnqueue(list, LAST);
        String message = context.getResources().
                getQuantityString(R.plurals.n_tracks_were_added_to_queue, list.size(), list.size());
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void addToQueue(final Context context, List<Song> list) {
        if (mService == null) {
            return;
        }
        mService.asyncEnqueue(list, LAST);
        String message = context.getResources().
                getQuantityString(R.plurals.n_tracks_were_added_to_queue, list.size(), list.size());
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void moveQueueItem(final int from, final int to) {
        if (mService != null) {
            mService.asyncMoveQueueItem(from, to);
        }
    }

    public static void refresh() {
        if (mService != null) {
            mService.asyncRefresh();
        }
    }

    public static void seek(long position) {
        if (mService != null) {
            mService.asyncSeek(position);
        }
    }

    public static final long position() {
        if (mService != null) {
            long position = mService.position();
            // For some reasons media player is returning large negative values when switching tracks
            //TODO investigate this
            return position >= 0 ? position : 0;
        }
        return 0;
    }

    public static final long getCurrentSongDuration() {
        if (mService != null) {
            return mService.getCurrentSongDuration();
        }
        return 0;
    }

    public static void playSongAt(final int position) {
        if (mService != null) {
            mService.asyncPlaySongAt(position);
        }
    }

    public static void playSongsFromUri(ArrayList<Song> songs) {
        if (mService != null) {
            mService.asyncPlaySongsFromUri(songs);
        }
    }

    @Nullable
    public static SleepTimer getSleepTimer() {
        if (mService != null) {
            return mService.getSleepTimer();
        }
        return null;
    }

    public static void clearQueue() {
        if (mService != null) {
            mService.removeTracks(0, Integer.MAX_VALUE);
        }
    }
}
