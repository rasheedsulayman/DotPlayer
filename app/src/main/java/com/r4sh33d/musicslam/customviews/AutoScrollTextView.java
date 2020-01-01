package com.r4sh33d.musicslam.customviews;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class AutoScrollTextView extends android.support.v7.widget.AppCompatTextView {

    public AutoScrollTextView(Context context) {
        super(context);
    }

    public AutoScrollTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoScrollTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setMaxLines(1);
        setFocusable(true);
        setSelected(true);
        setHorizontallyScrolling(true);
        setFocusableInTouchMode(true);
    }
}
