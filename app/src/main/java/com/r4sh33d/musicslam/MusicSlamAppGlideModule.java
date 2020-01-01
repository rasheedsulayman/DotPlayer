package com.r4sh33d.musicslam;


import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.r4sh33d.musicslam.customglide.artist.ArtistImage;
import com.r4sh33d.musicslam.customglide.artist.ArtistModelLoaderFactory;
import com.r4sh33d.musicslam.customglide.audiocover.AudioCoverImage;
import com.r4sh33d.musicslam.customglide.audiocover.AudioCoverLoaderFactory;

import java.io.InputStream;
/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
@GlideModule
public final class MusicSlamAppGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.prepend(ArtistImage.class, InputStream.class, new ArtistModelLoaderFactory(context));
        registry.prepend(AudioCoverImage.class, InputStream.class, new AudioCoverLoaderFactory(context));
    }
}