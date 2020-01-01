package com.r4sh33d.musicslam.customglide.audiocover;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.io.InputStream;

/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class AudioCoverLoaderFactory implements ModelLoaderFactory<AudioCoverImage, InputStream> {

    private Context context;

    public AudioCoverLoaderFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ModelLoader<AudioCoverImage, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
        return new AudioCoverModelLoader(context);
    }

    @Override
    public void teardown() {
    }
}
