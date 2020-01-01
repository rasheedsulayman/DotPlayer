package com.r4sh33d.musicslam.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.r4sh33d.musicslam.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Not part of the app... Just using this for testing color palette extraction from images
 */
public class PaletteWorkSpaceFragment extends BaseListenerFragment {
    @BindView(R.id.light_muted)
    TextView lightMuted;

    @BindView(R.id.muted)
    TextView muted;

    @BindView(R.id.dark_muted)
    TextView darkMuted;

    @BindView(R.id.light_vibrant)
    TextView lightVibrant;

    @BindView(R.id.vibrant)
    TextView vibrant;

    @BindView(R.id.dark_vibrant)
    TextView darkVibrant;

    public PaletteWorkSpaceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pallete_work_space, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onMetaChanged() {
        super.onMetaChanged();
        restBackgroundColors(lightMuted, muted, darkMuted, vibrant, lightVibrant, darkVibrant);
        // loadArtworkImage();
    }

  /*  void loadArtworkImage() {
        GlideApp.with(this)
                .asBitmap().
                load(SlamUtils.getAlbumArtUri(MusicPlayer.getCurrentSong().albumId))
                .into(new SimpleTarget<Bitmap>(100, 100) {
                    @Override
                    public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                        Palette.from(bitmap).maximumColorCount(32).generate(p -> setUIFromPalette(p));
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {

                    }
                });
    }*/

    void restBackgroundColors(TextView... textViews) {
        for (TextView textView : textViews) {
            textView.setTextColor(Color.WHITE);
            textView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public void setUIFromPalette(Palette palette) {
        Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
        if (vibrantSwatch != null) {
            vibrant.setBackgroundColor(vibrantSwatch.getRgb());
            vibrant.setTextColor(vibrantSwatch.getBodyTextColor());
        }
        Palette.Swatch vibrantLightSwatch = palette.getLightVibrantSwatch();
        if (vibrantLightSwatch != null) {
            lightVibrant.setBackgroundColor(vibrantLightSwatch.getRgb());
            lightVibrant.setTextColor(vibrantLightSwatch.getTitleTextColor());
        }

        Palette.Swatch mutedSwatch = palette.getMutedSwatch();
        if (mutedSwatch != null) {
            muted.setBackgroundColor(mutedSwatch.getRgb());
            muted.setTextColor(mutedSwatch.getTitleTextColor());
        }

        Palette.Swatch lightMutedSwatch = palette.getLightMutedSwatch();
        if (lightMutedSwatch != null) {
            lightMuted.setBackgroundColor(lightMutedSwatch.getRgb());
            lightMuted.setTextColor(lightMutedSwatch.getBodyTextColor());
        }
        Palette.Swatch darkVibrantSwatch = palette.getDarkVibrantSwatch();
        if (darkVibrantSwatch != null) {
            darkVibrant.setBackgroundColor(darkVibrantSwatch.getRgb());
            darkVibrant.setTextColor(darkVibrantSwatch.getTitleTextColor());
        }

        Palette.Swatch darkMutedSwatch = palette.getDarkMutedSwatch();
        if (darkMutedSwatch != null) {
            darkMuted.setBackgroundColor(darkMutedSwatch.getRgb());
            darkMuted.setTextColor(darkMutedSwatch.getTitleTextColor());
        }
    }
}


