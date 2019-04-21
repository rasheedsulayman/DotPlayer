package com.r4sh33d.musicslam.customglide.artist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;
import com.r4sh33d.musicslam.network.LastFmService;

import java.io.InputStream;

public class ArtistArtModelLoader implements ModelLoader<ArtistImage, InputStream> {


    private final LastFmService lastFmService;
    private final ModelLoader<GlideUrl, InputStream> okHttpUrlLoader;
    private Context context;

    public ArtistArtModelLoader(LastFmService lastFmService, ModelLoader<GlideUrl, InputStream> okHttpUrlLoader, Context context) {
        this.lastFmService = lastFmService;
        this.okHttpUrlLoader = okHttpUrlLoader;
        this.context = context;
    }

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull ArtistImage artistImage, int width, int height, @NonNull Options options) {
        return new LoadData<>(new ObjectKey(artistImage.toString()),
                /*fetcher=*/ new ArtistArtDataFetcher(artistImage, lastFmService, okHttpUrlLoader, height, width, options, context));
    }

    @Override
    public boolean handles(@NonNull ArtistImage artistImage) {
        return true;
    }

}
