package com.r4sh33d.musicslam.customglide.artist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.r4sh33d.musicslam.network.LastFmService;
import com.r4sh33d.musicslam.utils.SlamUtils;

import java.io.InputStream;

/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class ArtistArtDataFetcher implements DataFetcher<InputStream> {

    private final int height;
    private final int width;
    private final Options options;
    private ArtistImage artistImage;
    private LastFmService lastFmService;
    private ModelLoader<GlideUrl, InputStream> okHttpUrlLoader;
    private Context context;
    private boolean isCancelled = false;
    private DataFetcher<InputStream> okHttpUrlFetcher;

    public ArtistArtDataFetcher(ArtistImage artistImage, LastFmService lastFmService,
                                ModelLoader<GlideUrl, InputStream> okHttpUrlLoader, int height, int width, Options options
            , Context context) {
        this.artistImage = artistImage;
        this.lastFmService = lastFmService;
        //We want to delegate our url -> InputStream loading to the stock
        //OkHttpUrlLoader. Since it handles GlideUrl to InputStream loading efficiently.
        this.okHttpUrlLoader = okHttpUrlLoader;
        this.height = height;
        this.width = width;
        this.options = options;
        this.context = context;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        if (SlamUtils.canAutoDownloadArtworks(context)) {
            ArtistImageUtil.fetchArtistImageUrl(lastFmService, artistImage.artistName, url -> {
                if (isCancelled || TextUtils.isEmpty(url)) {
                    callback.onDataReady(null);
                    return;
                }
                GlideUrl glideUrl = new GlideUrl(url);
                //noinspection ConstantConditions
                okHttpUrlFetcher = okHttpUrlLoader.buildLoadData(glideUrl, width, height, options).fetcher;
                okHttpUrlFetcher.loadData(priority, new DataCallback<InputStream>() {
                    @Override
                    public void onDataReady(@Nullable InputStream data) {
                        callback.onDataReady(data);
                    }

                    @Override
                    public void onLoadFailed(@NonNull Exception e) {
                        callback.onLoadFailed(e);
                    }
                });
            });
        } else {
            callback.onDataReady(null);
        }
    }

    @Override
    public void cleanup() {
        if (okHttpUrlFetcher != null) {
            okHttpUrlFetcher.cleanup();
        }
    }

    @Override
    public void cancel() {
        isCancelled = true;
        if (okHttpUrlFetcher != null) {
            okHttpUrlFetcher.cancel();
        }
    }

    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.REMOTE;
    }
}
