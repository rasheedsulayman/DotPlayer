package com.r4sh33d.musicslam.customviews;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.Util;
import com.r4sh33d.musicslam.utils.PrefsUtils;

import io.reactivex.disposables.Disposable;
/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class ColoredStatusBarView extends View {
    Disposable subscription;
    boolean isAlbumArtTheme;

    public ColoredStatusBarView(Context context) {
        super(context);
    }

    public ColoredStatusBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ColoredStatusBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ColoredStatusBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) {
            return;
        }
        isAlbumArtTheme = PrefsUtils.getInstance(getContext()).isAlbumArtTheme();
        if (!isAlbumArtTheme) {
            subscription = Aesthetic.get()
                    .colorPrimary()
                    .subscribe((Integer color) -> setBackgroundColor(Util.darkenColor(color)));
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isAlbumArtTheme) {
            subscription.dispose();
        }
    }
}
