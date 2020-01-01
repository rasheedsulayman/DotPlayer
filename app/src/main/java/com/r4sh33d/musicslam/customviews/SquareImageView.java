package com.r4sh33d.musicslam.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class SquareImageView extends AppCompatImageView {

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int mSize = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(mSize, mSize);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void requestLayout() {
        forceLayout();
    }
}
