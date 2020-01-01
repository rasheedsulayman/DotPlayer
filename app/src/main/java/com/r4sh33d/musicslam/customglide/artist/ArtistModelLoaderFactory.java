package com.r4sh33d.musicslam.customglide.artist;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.r4sh33d.musicslam.network.LastFmRetrofitClient;
import com.r4sh33d.musicslam.network.LastFmService;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class ArtistModelLoaderFactory implements ModelLoaderFactory<ArtistImage, InputStream> {

    private static final long TIMEOUT = 5000;
    private final OkHttpUrlLoader.Factory okHttpFactory;
    LastFmService lastFmService;
    Context context;

    public ArtistModelLoaderFactory(Context context) {
        okHttpFactory = new OkHttpUrlLoader.Factory(new OkHttpClient.Builder()
                .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .build());
        lastFmService = LastFmRetrofitClient.getLastFmRetrofitService(context, TIMEOUT);
        this.context = context;
    }

    @NonNull
    @Override
    public ModelLoader<ArtistImage, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
        return new ArtistArtModelLoader(lastFmService, okHttpFactory.build(multiFactory), context);
    }

    @Override
    public void teardown() {
        okHttpFactory.teardown();
    }
}
