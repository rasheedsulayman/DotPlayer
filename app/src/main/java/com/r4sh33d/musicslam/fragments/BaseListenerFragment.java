package com.r4sh33d.musicslam.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.r4sh33d.musicslam.playback.MusicStateListener;
import com.r4sh33d.musicslam.utils.PrefsUtils;
import com.r4sh33d.musicslam.activities.MainActivity;
import com.r4sh33d.musicslam.interfaces.PaletteListener;

public abstract class BaseListenerFragment extends Fragment implements MusicStateListener, PaletteListener {
    protected MainActivity mActivity;
    public boolean isAlbumArtTheme;
    protected PrefsUtils prefsUtils;
    public  boolean isStoragePermissionGranted;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        prefsUtils = PrefsUtils.getInstance(context);
        isAlbumArtTheme = prefsUtils.isAlbumArtTheme();
        if (!(context instanceof MainActivity)) {
            throw new RuntimeException("The host Activity must be an instance of ColorPaletteActivity");
        } else {
            mActivity = ((MainActivity) context);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity.subscribeToMusicEvents(this);
        mActivity.subscribeToPaletteColors(this);
    }

    @Override
    public void onMediaStoreRefreshed() {

    }

    @Override
    public void onPlaylistChanged() {
    }

    @Override
    public void onMetaChanged() {
    }

    @Override
    public void onRepeatModeChanged() {
    }


    @Override
    public void onPlayStateChanged() {
    }

    @Override
    public void onShuffleModeChanged() {
    }

    @Override
    public void onPaletteReady(int color) {
    }

    @Override
    public void onServiceConnected() {

    }

    @Override
    public void onQueueChanged() {

    }

    public int getCurrentPaletteColor() {
        return mActivity.getCurrentPaletteColor();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mActivity != null) {
            mActivity.unSubscribeFromMusicEvents(this);
            mActivity.unsubscribeToPaletteColors(this);
        }
    }
}