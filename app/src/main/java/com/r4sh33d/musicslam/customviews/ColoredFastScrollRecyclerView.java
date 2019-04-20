package com.r4sh33d.musicslam.customviews;

import android.content.Context;
import android.util.AttributeSet;

import com.afollestad.aesthetic.Aesthetic;
import com.r4sh33d.musicslam.utils.PrefsUtils;
import com.r4sh33d.musicslam.activities.ThemedSlidingPanelActivity;
import com.r4sh33d.musicslam.interfaces.PaletteListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import io.reactivex.disposables.Disposable;

public class ColoredFastScrollRecyclerView extends FastScrollRecyclerView implements PaletteListener {
    Disposable subscription;
    boolean isAlbumArtTheme;

    public ColoredFastScrollRecyclerView(Context context) {
        super(context);
    }

    public ColoredFastScrollRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColoredFastScrollRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
                    .colorAccent()
                    .subscribe(color -> {
                        setPopupBgColor(color);
                        setThumbColor(color);
                    });
        }else {
            ((ThemedSlidingPanelActivity) getContext()).subscribeToPaletteColors(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isAlbumArtTheme) {
            subscription.dispose();
        } else {
            ((ThemedSlidingPanelActivity) getContext()).unsubscribeToPaletteColors(this);
        }
    }

    @Override
    public void onPaletteReady(int color) {
        setPopupBgColor(color);
        setThumbColor(color);
    }
}
