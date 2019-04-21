package com.r4sh33d.musicslam.playback;

public interface MusicStateListener {

    public void onMediaStoreRefreshed();

    public void onPlaylistChanged();

    public void onMetaChanged();

    public void onRepeatModeChanged();

    public void onPlayStateChanged();

    public void onShuffleModeChanged();

    public void onServiceConnected();

    public void onQueueChanged();

}
