package com.r4sh33d.musicslam.customviews;

import android.content.Context;
import android.support.design.widget.CollapsingToolbarLayout;
import android.util.AttributeSet;

import com.r4sh33d.musicslam.utils.PrefsUtils;
import com.r4sh33d.musicslam.utils.SlamUtils;
/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class FixedMinimumHeightCollapsingToolBar extends CollapsingToolbarLayout {

    public FixedMinimumHeightCollapsingToolBar(Context context) {
        super(context);
    }

    public FixedMinimumHeightCollapsingToolBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedMinimumHeightCollapsingToolBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (isInEditMode()) {
            return;
        }
        boolean isAlbumArtTheme = PrefsUtils.getInstance(getContext()).isAlbumArtTheme();
        setMinimumHeight(SlamUtils.dpToPx(isAlbumArtTheme ? 240 : 128, getContext()));
    }
}
