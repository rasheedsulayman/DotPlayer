package com.r4sh33d.musicslam.interfaces;

import android.view.View;

import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

public interface SlidingPanelEventsListener {

    void onPanelSlide(View panel, float slideOffset);

    void onPanelStateChanged(View panel, PanelState previousState, PanelState newState);
}
