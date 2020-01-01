package com.r4sh33d.musicslam.playback;

public interface MusicStateListener {

    void onMediaStoreRefreshed();

    void onPlaylistChanged();

    void onMetaChanged();

    void onRepeatModeChanged();

    void onPlayStateChanged();

    void onShuffleModeChanged();

    void onServiceConnected();

    void onQueueChanged();

}
