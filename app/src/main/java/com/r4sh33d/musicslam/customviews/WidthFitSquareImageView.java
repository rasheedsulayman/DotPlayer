package com.r4sh33d.musicslam.customviews;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class WidthFitSquareImageView extends AppCompatImageView {

    public WidthFitSquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
