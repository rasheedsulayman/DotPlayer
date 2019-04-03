package com.r4sh33d.musicslam.customviews;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.utils.PrefsUtils;


public class BlackAndWhiteImageView extends AppCompatImageView {

    public BlackAndWhiteImageView(Context context) {
        super(context);
        applyColor();
    }

    public BlackAndWhiteImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyColor();
    }

    public BlackAndWhiteImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyColor();
    }

    public void applyColor() {
        if (!PrefsUtils.getInstance(getContext()).isLightTheme()) {
            setColorFilter(getResources().getColor(R.color.overflow_white), PorterDuff.Mode.SRC_IN);
        }
    }
}