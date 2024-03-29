package com.r4sh33d.musicslam.activities;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.r4sh33d.musicslam.GlideApp;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.blurtransition.BlurImageWorker;
import com.r4sh33d.musicslam.customglide.audiocover.AudioCoverImage;
import com.r4sh33d.musicslam.fragments.playqueue.PlayQueueFragment;
import com.r4sh33d.musicslam.interfaces.PaletteListener;
import com.r4sh33d.musicslam.interfaces.SlidingPanelEventsListener;
import com.r4sh33d.musicslam.playback.MusicPlayer;
import com.r4sh33d.musicslam.utils.ColorHelper;
import com.r4sh33d.musicslam.utils.SlamUtils;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public abstract class ThemedSlidingPanelActivity extends MusicEventsListenerActivity implements
        SlidingUpPanelLayout.PanelSlideListener {

    @BindView(R.id.sliding_layout)
    public SlidingUpPanelLayout mSlidingUpPanelLayout;

    private int currentPaletteColor;
    private ArrayList<SlidingPanelEventsListener> slidingPanelEventsListeners = new ArrayList<>();
    private ArrayList<PaletteListener> paletteListeners = new ArrayList<>();
    private String paletteKey = "no_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        mSlidingUpPanelLayout.addPanelSlideListener(this);
        currentPaletteColor = getResources().getColor(R.color.blueAccent);
    }

    @Override
    public void onMetaChanged() {
        super.onMetaChanged();
        loadAlbumArtAndExtractPalette();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        loadAlbumArtAndExtractPalette();
    }

    //---> Color palette
    private void notifyPaletteListeners(int color) {
        for (final PaletteListener listener : paletteListeners) {
            if (listener != null) {
                listener.onPaletteReady(color);
            }
        }
    }

    public int getCurrentPaletteColor() {
        return currentPaletteColor;
    }

    public void subscribeToPaletteColors(final PaletteListener paletteListener) {
        if (paletteListener != null) {
            paletteListeners.add(paletteListener);
            //still need to notify the newly added listener
            paletteListener.onPaletteReady(currentPaletteColor);
        }
    }

    public void unsubscribeToPaletteColors(final PaletteListener paletteListener) {
        if (paletteListener != null) {
            paletteListeners.remove(paletteListener);
        }
    }

    //---> Sliding Panel
    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        for (final SlidingPanelEventsListener listener : slidingPanelEventsListeners) {
            if (listener != null) {
                listener.onPanelSlide(panel, slideOffset);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        onPanelStateChanged(mSlidingUpPanelLayout, mSlidingUpPanelLayout.getPanelState(),
                mSlidingUpPanelLayout.getPanelState());
    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState,
                                    SlidingUpPanelLayout.PanelState newState) {
        for (final SlidingPanelEventsListener listener : slidingPanelEventsListeners) {
            if (listener != null) {
                listener.onPanelStateChanged(panel, previousState, newState);
            }
        }
        if (newState == SlidingUpPanelLayout.PanelState.ANCHORED) {
            mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }

    public void setPanelEventListenerListener(final SlidingPanelEventsListener panelEventsListener) {
        if (panelEventsListener != null) {
            slidingPanelEventsListeners.add(panelEventsListener);
            //We still want to update newly added listeners immediately
            panelEventsListener.onPanelStateChanged(mSlidingUpPanelLayout, null,
                    mSlidingUpPanelLayout.getPanelState());
        }
    }

    public void removePanelEventListenerListener(final SlidingPanelEventsListener panelEventsListener) {
        if (panelEventsListener != null) {
            slidingPanelEventsListeners.remove(panelEventsListener);
        }
    }

    @Override
    public void onBackPressed() {
        if (mSlidingUpPanelLayout != null &&
                (mSlidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED ||
                        mSlidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            if (getSupportFragmentManager().findFragmentById(R.id.dragView) instanceof PlayQueueFragment) {
                super.onBackPressed(); //pop backstack
                mSlidingUpPanelLayout.setTouchEnabled(true);
            } else {
                mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        } else {
            super.onBackPressed();
        }
    }

    public void colorStatusBar(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        slidingPanelEventsListeners.clear();
        paletteListeners.clear();
        mSlidingUpPanelLayout.removePanelSlideListener(this);
    }

    private void loadAlbumArtAndExtractPalette() {
        String key = BlurImageWorker.getCurrentCacheKey();
        if (key == null || key.equals(paletteKey)) {
            return;
        }
        paletteKey = key;
        GlideApp.with(this)
                .asBitmap()
                .load(new AudioCoverImage(MusicPlayer.getCurrentSong().data))
                .signature(SlamUtils.getMediaStoreSignature(MusicPlayer.getCurrentSong()))
                .into(new SimpleTarget<Bitmap>(40, 40) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap,
                                                Transition<? super Bitmap> transition) {
                        Palette.from(bitmap).generate(p -> {
                            currentPaletteColor = ColorHelper.extractColorsFromPalette(p);
                            notifyPaletteListeners(currentPaletteColor);
                        });
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        notifyPaletteListeners(Color.parseColor(ColorHelper.DEFAULT_COLOR_ACCENT));
                    }
                });
    }
}