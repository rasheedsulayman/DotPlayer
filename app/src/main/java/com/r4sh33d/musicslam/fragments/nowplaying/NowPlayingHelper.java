package com.r4sh33d.musicslam.fragments.nowplaying;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.ColorInt;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.utils.PrefsUtils;

import static com.r4sh33d.musicslam.utils.PrefsUtils.ThemesTypes.AlbumArtTheme;
import static com.r4sh33d.musicslam.utils.PrefsUtils.ThemesTypes.BlackTheme;
import static com.r4sh33d.musicslam.utils.PrefsUtils.ThemesTypes.BlueTheme;
import static com.r4sh33d.musicslam.utils.PrefsUtils.ThemesTypes.DarkTheme;
import static com.r4sh33d.musicslam.utils.PrefsUtils.ThemesTypes.LightTheme;

public class NowPlayingHelper {

    public static  void changeSeekBarColor(int color, SeekBar seekBar) {
        LayerDrawable layerDrawable = (LayerDrawable) seekBar.getProgressDrawable();
        ClipDrawable drawable = (ClipDrawable) layerDrawable.findDrawableByLayerId(android.R.id.progress);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        seekBar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    public static void changeProgressBarColor(int color, ProgressBar progressBar) {
        //bottom controller
        LayerDrawable proressBarLayerDrawable =
                (LayerDrawable) progressBar.getProgressDrawable().mutate();
        ClipDrawable seekbarClipDrawable = (ClipDrawable) proressBarLayerDrawable.
                findDrawableByLayerId(android.R.id.progress);
        seekbarClipDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }
}
