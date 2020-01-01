package com.r4sh33d.musicslam.customglide.audiocover;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;

import java.io.InputStream;
/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class AudioCoverModelLoader implements ModelLoader<AudioCoverImage, InputStream> {


    private Context context;

    public AudioCoverModelLoader(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull AudioCoverImage audioCoverImage, int width, int height, @NonNull Options options) {
        return new LoadData<>(new ObjectKey(audioCoverImage.toString()),
                /*fetcher=*/ new AudioCoverDataFetcher(audioCoverImage, context));
    }

    @Override
    public boolean handles(@NonNull AudioCoverImage audioCoverImage) {
        return true;
    }

}
