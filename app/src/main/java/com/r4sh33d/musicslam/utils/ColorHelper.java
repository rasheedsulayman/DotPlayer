package com.r4sh33d.musicslam.utils;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.graphics.Palette;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.r4sh33d.musicslam.GlideApp;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.customglide.audiocover.AudioCoverImage;
import com.r4sh33d.musicslam.interfaces.PaletteListener;
import com.r4sh33d.musicslam.playback.MusicPlayer;

import static com.r4sh33d.musicslam.utils.PrefsUtils.ThemesTypes.AlbumArtTheme;
import static com.r4sh33d.musicslam.utils.PrefsUtils.ThemesTypes.BlackTheme;
import static com.r4sh33d.musicslam.utils.PrefsUtils.ThemesTypes.BlueTheme;
import static com.r4sh33d.musicslam.utils.PrefsUtils.ThemesTypes.DarkTheme;
import static com.r4sh33d.musicslam.utils.PrefsUtils.ThemesTypes.LightTheme;

public class ColorHelper {
    public static final String DEFAULT_COLOR_ACCENT = "#FF42A5F5";

    public static int extractColorsFromPalette(Palette palette) {
        int color = Color.parseColor(DEFAULT_COLOR_ACCENT);
        Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
        if (vibrantSwatch != null) {
            color = vibrantSwatch.getRgb();
        } else {
            Palette.Swatch vibrantLightSwatch = palette.getLightVibrantSwatch();
            if (vibrantLightSwatch != null) {
                color = vibrantLightSwatch.getRgb();
            } else {
                Palette.Swatch mutedSwatch = palette.getMutedSwatch();
                if (mutedSwatch != null) {
                    color = mutedSwatch.getRgb();
                }
            }
        }
        return adjustColor(color);
    }

    @ColorInt
    public static int adjustColor(int colorInt) {
        int alpha = Color.alpha(colorInt);
        int red = Color.red(colorInt);
        int green = Color.green(colorInt);
        int blue = Color.blue(colorInt);
        //If the RGB components of the color are too close, increase the Green component to
        //improve readability on whitish backgrounds when used as text color
        if (Math.abs(red - blue) <= 10 &&
                Math.abs(red - green) <= 10 &&
                Math.abs(blue - green) <= 10) {
            green += 20;
            if (green > 255) {
                green = 255;
            }
            return Color.argb(alpha, red, green, blue);
        }
        return colorInt;
    }

    public static Drawable tintDrawable(int color, Drawable drawable) {
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        return drawable;
    }


    public static Drawable getTintedDrawable(int drawableResId, int color, Context context) {
        Drawable drawable = context.getResources().getDrawable(drawableResId).mutate();
        return tintDrawable(color, drawable);
    }

    public static int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    public static int getColorWithAlpha(float alpha, int baseColor) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        int rgb = 0x00ffffff & baseColor;
        return a + rgb;
    }

    public static @ColorInt
    int getWindowBackgroundColor(Context context) {
        int res = R.color.bg_white_shade;
        switch (PrefsUtils.getInstance(context).getCurrentTheme()) {
            case AlbumArtTheme:
                res = R.color.bg_white_shade;
                break;
            case DarkTheme:
                res = R.color.dark_theme_windows_bg;
                break;
            case BlueTheme:
                res = R.color.blue_theme_windows_bg;
                break;
            case BlackTheme:
                res = R.color.black_theme_windows_bg;
                break;
            case LightTheme:
                res = R.color.white_theme_windows_bg;
                break;
        }
        return context.getResources().getColor(res);
    }

    public static @StyleRes
    int getWindowsBackgroundColoredTheme(Context context) {
        int res = R.style.AppThemeDark; //Default
        switch (PrefsUtils.getInstance(context).getCurrentTheme()) {
            case AlbumArtTheme:
                res = R.style.AlbumArtTheme;
                break;
            case DarkTheme:
                res = R.style.AppThemeDark;
                break;
            case BlueTheme:
                res = R.style.AppThemeBlue;
                break;
            case BlackTheme:
                res = R.style.AppThemeBlack;
                break;
            case LightTheme:
                res = R.style.AppThemeLight;
                break;
        }
        return res;
    }

    public static void getPaletteColorFromBitmap(Fragment scope, PaletteListener paletteListener) {
        GlideApp.with(scope)
                .asBitmap()
                .load(new AudioCoverImage(MusicPlayer.getCurrentSong().data))
                .into(new SimpleTarget<Bitmap>(40, 40) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap,
                                                Transition<? super Bitmap> transition) {
                        Palette.from(bitmap).generate(p -> {
                            paletteListener.onPaletteReady(ColorHelper.extractColorsFromPalette(p));

                        });
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        paletteListener.onPaletteReady(Color.parseColor(ColorHelper.DEFAULT_COLOR_ACCENT));
                    }
                });
    }
}
