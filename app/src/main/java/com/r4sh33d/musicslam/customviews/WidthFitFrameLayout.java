package com.r4sh33d.musicslam.customviews;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class WidthFitFrameLayout extends FrameLayout {
    public WidthFitFrameLayout(@NonNull Context context) {
        super(context);
    }

    public WidthFitFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public WidthFitFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //use width length to determine the height length
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
