package com.r4sh33d.musicslam.customviews;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.utils.PrefsUtils;

public class DividerView extends View {

    public DividerView(Context context) {
        super(context);
        applyColor();
    }

    public DividerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        applyColor();
    }

    public DividerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyColor();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DividerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        applyColor();
    }

    void applyColor() {
        if (PrefsUtils.getInstance(getContext()).isLightTheme()) {
            setBackgroundColor(getResources().getColor(R.color.line_separator_color));
        } else {
            setBackgroundColor(getResources().getColor(R.color.line_separator_color_album_art));
        }
    }
}
