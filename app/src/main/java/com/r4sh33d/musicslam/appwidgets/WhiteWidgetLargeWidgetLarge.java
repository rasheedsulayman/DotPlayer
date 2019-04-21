package com.r4sh33d.musicslam.appwidgets;

import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.playback.MusicService;

public class WhiteWidgetLargeWidgetLarge extends TransparentWidgetLarge {
    public static final String TYPE = "white_widget_large_info";
    private static WhiteWidgetLargeWidgetLarge sInstance;

    public static synchronized WhiteWidgetLargeWidgetLarge getInstance() {
        if (sInstance == null) {
            sInstance = new WhiteWidgetLargeWidgetLarge();
        }
        return sInstance;
    }

    @Override
    public int getWidgetLayoutRes() {
        return R.layout.white_widget_large;
    }

    @Override
    public int getImageResourceForPlayPause(MusicService service) {
        return service.isPlaying() ? R.drawable.ic_pause_black_24dp : R.drawable.ic_play_arrow_black_24dp;
    }

    @Override
    public String getType() {
        return TYPE;
    }
}

