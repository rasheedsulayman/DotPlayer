
package com.r4sh33d.musicslam.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import com.r4sh33d.musicslam.playback.MusicStateListener;
import com.r4sh33d.musicslam.playback.MusicPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import timber.log.Timber;

import static com.r4sh33d.musicslam.playback.Constants.META_CHANGED;
import static com.r4sh33d.musicslam.playback.Constants.PLAYLIST_CHANGED;
import static com.r4sh33d.musicslam.playback.Constants.PLAY_STATE_CHANGED;
import static com.r4sh33d.musicslam.playback.Constants.QUEUE_CHANGED;
import static com.r4sh33d.musicslam.playback.Constants.REFRESH;
import static com.r4sh33d.musicslam.playback.Constants.REPEAT_MODE_CHANGED;
import static com.r4sh33d.musicslam.playback.Constants.SHUFFLE_MODE_CHANGED;
import static com.r4sh33d.musicslam.playback.Constants.TRACK_ERROR;

public abstract class MusicEventsListenerActivity extends BaseActivity implements ServiceConnection,
        MusicStateListener {

    private final ArrayList<MusicStateListener> musicStateListenersList = new ArrayList<>();

    private MusicPlayer.ServiceToken bindingToken;

    private MusicEventsBroadcastReceiver musicEventsBroadcastReceiver;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isPermissionGranted) {
            bindToService();
        }
    }

    @Override
    public void onServiceConnected(final ComponentName name, final IBinder service) {
        this.onServiceConnected();
        for (final MusicStateListener listener : musicStateListenersList) {
            if (listener != null) {
                listener.onServiceConnected();
            }
        }
    }

    @Override
    public void onServiceConnected() {

    }

    void bindToService() {
        Timber.d("Bind to service called");
        bindingToken = MusicPlayer.bindToService(this, this);
        initBroadcastReceiver();
    }

    @Override
    public void onStoragePermissionGranted() {
        bindToService();
        onMediaStoreRefreshed();
    }

    public void activateTransparentStatusBar() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
    }


    public void initBroadcastReceiver() {
        musicEventsBroadcastReceiver = new MusicEventsBroadcastReceiver(this);
        final IntentFilter filter = new IntentFilter();
        filter.addAction(PLAY_STATE_CHANGED);
        filter.addAction(META_CHANGED);
        filter.addAction(REFRESH);
        filter.addAction(PLAYLIST_CHANGED);
        filter.addAction(TRACK_ERROR);
        filter.addAction(SHUFFLE_MODE_CHANGED);
        filter.addAction(REPEAT_MODE_CHANGED);
        filter.addAction(QUEUE_CHANGED);
        registerReceiver(musicEventsBroadcastReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bindingToken != null) {
            MusicPlayer.unbindFromService(bindingToken);
            unregisterReceiver(musicEventsBroadcastReceiver);
        }
        musicStateListenersList.clear();
    }

    private final static class MusicEventsBroadcastReceiver extends BroadcastReceiver {

        private final WeakReference<MusicEventsListenerActivity> mReference;

        public MusicEventsBroadcastReceiver(final MusicEventsListenerActivity activity) {
            mReference = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            MusicEventsListenerActivity musicEventsListenerActivity = mReference.get();
            if (musicEventsListenerActivity != null) {
                //noinspection ConstantConditions
                switch (action) {
                    case QUEUE_CHANGED:
                        musicEventsListenerActivity.onQueueChanged();
                    case META_CHANGED:
                        musicEventsListenerActivity.onMetaChanged();
                        break;
                    case PLAY_STATE_CHANGED:
                        musicEventsListenerActivity.onPlayStateChanged();
                        break;
                    case REFRESH:
                        musicEventsListenerActivity.onMediaStoreRefreshed();
                        break;
                    case PLAYLIST_CHANGED:
                        musicEventsListenerActivity.onPlaylistChanged();
                        break;
                    case SHUFFLE_MODE_CHANGED:
                        musicEventsListenerActivity.onShuffleModeChanged();
                        break;
                    case REPEAT_MODE_CHANGED:
                        musicEventsListenerActivity.onRepeatModeChanged();
                        break;
                    case TRACK_ERROR:
                        Timber.d("Track error");
                        break;
                }
            }
        }
    }

    @Override
    public void onQueueChanged() {
        for (final MusicStateListener listener : musicStateListenersList) {
            if (listener != null) {
                listener.onQueueChanged();
            }
        }
    }

    @Override
    public void onMetaChanged() {
        for (final MusicStateListener listener : musicStateListenersList) {
            if (listener != null) {
                listener.onMetaChanged();
            }
        }
    }

    @Override
    public void onMediaStoreRefreshed() {
        for (final MusicStateListener listener : musicStateListenersList) {
            if (listener != null) {
                listener.onMediaStoreRefreshed();
            }
        }
    }

    @Override
    public void onPlaylistChanged() {
        for (final MusicStateListener listener : musicStateListenersList) {
            if (listener != null) {
                listener.onPlaylistChanged();
            }
        }
    }

    @Override
    public void onRepeatModeChanged() {
        for (final MusicStateListener listener : musicStateListenersList) {
            if (listener != null) {
                listener.onRepeatModeChanged();
            }
        }
    }

    @Override
    public void onPlayStateChanged() {
        for (final MusicStateListener listener : musicStateListenersList) {
            if (listener != null) {
                listener.onPlayStateChanged();
            }
        }
    }

    @Override
    public void onShuffleModeChanged() {
        for (final MusicStateListener listener : musicStateListenersList) {
            if (listener != null) {
                listener.onShuffleModeChanged();
            }
        }
    }

    public void subscribeToMusicEvents(final MusicStateListener listener) {
        if (listener == this) {
            throw new IllegalArgumentException("Override the method, don't add a listener. " +
                    "The events are subscribed for already in the super class");
        }
        if (listener != null) {
            musicStateListenersList.add(listener);
        }
    }

    public void unSubscribeFromMusicEvents(final MusicStateListener listener) {
        if (listener != null) {
            musicStateListenersList.remove(listener);
        }
    }
}
