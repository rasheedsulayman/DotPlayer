package com.r4sh33d.musicslam.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import com.afollestad.aesthetic.Aesthetic;
import com.r4sh33d.musicslam.activities.ThemedSlidingPanelActivity;
import com.r4sh33d.musicslam.interfaces.PaletteListener;
import com.r4sh33d.musicslam.utils.PrefsUtils;
import com.reddit.indicatorfastscroll.FastScrollerThumbView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.reactivex.disposables.Disposable;

/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class ColoredFastScrollerThumbView extends FastScrollerThumbView implements PaletteListener {

    Disposable subscription;
    boolean isAlbumArtTheme;

    public ColoredFastScrollerThumbView(@NotNull Context context,
                                        @Nullable AttributeSet attrs, @Nullable int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ColoredFastScrollerThumbView(@NotNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ColoredFastScrollerThumbView(@Nullable Context context) {
        super(context);
    }

    public void initColorSubs() {
        isAlbumArtTheme = PrefsUtils.getInstance(getContext()).isAlbumArtTheme();
        if (!isAlbumArtTheme) {
            subscription = Aesthetic.get()
                    .colorAccent()
                    .subscribe(i -> setThumbColor(ColorStateList.valueOf(i)));
        } else {
            ((ThemedSlidingPanelActivity) getContext()).subscribeToPaletteColors(this);
        }
    }

    public void cancelColorSubs() {
        if (!isAlbumArtTheme) {
            subscription.dispose();
        } else {
            ((ThemedSlidingPanelActivity) getContext()).unsubscribeToPaletteColors(this);
        }
    }


    @Override
    public void onPaletteReady(int color) {
        setThumbColor(ColorStateList.valueOf(color));
    }
}
