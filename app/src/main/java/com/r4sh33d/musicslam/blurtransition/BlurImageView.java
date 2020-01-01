/*
 * Copyright (C) 2014 The CyanogenMod Project
 * Copyright (C) 2019 Rasheed Sulayman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.r4sh33d.musicslam.blurtransition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.r4sh33d.musicslam.GlideApp;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.customglide.audiocover.AudioCoverImage;
import com.r4sh33d.musicslam.playback.MusicPlayer;
import com.r4sh33d.musicslam.utils.SlamUtils;

public class BlurImageView extends android.support.v7.widget.AppCompatImageView {
    String albumArtKey = "no_key";
    private boolean isUsingDefaultBlur;
    private boolean isCurrentlyOnScreen;

    public BlurImageView(Context context) {
        super(context);
        doInitSetup();
    }

    public BlurImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        doInitSetup();
    }

    public BlurImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        doInitSetup();
    }

    void doInitSetup() {
        isCurrentlyOnScreen = true;
        isUsingDefaultBlur = true;
    }

    public void onStart() {
        isCurrentlyOnScreen = true;
    }

    public void onStop() {
        isCurrentlyOnScreen = false;
    }

    public void onHiddenStatusChanged(boolean isHidden) {
        isCurrentlyOnScreen = !isHidden;
    }


    public void transitionToDefaultState(boolean animate) {
        if (isUsingDefaultBlur) {
            return;
        }

        Drawable drawable = getContext().getDrawable(R.drawable.default_artwork_blur);

        if (animate) {
            drawable = BlurImageWorker.createImageTransitionDrawable(getDrawable(), drawable, BlurImageWorker.FADE_IN_TIME);
        }

        setTransitionDrawable(drawable);
        isUsingDefaultBlur = true;
    }

    public void setTransitionDrawable(Drawable drawable) {
        setImageDrawable(drawable);
        isUsingDefaultBlur = false;
    }

    public void loadBlurImage() {
        String key = BlurImageWorker.getCurrentCacheKey();
        if (key == null || key.equals(albumArtKey)) {
            return;
        }
        albumArtKey = key;
        GlideApp.with(getContext().getApplicationContext()).
                asBitmap().
                centerCrop().
                load(new AudioCoverImage(MusicPlayer.getCurrentSong().data))
                .signature(SlamUtils.getMediaStoreSignature(MusicPlayer.getCurrentSong()))
                .into(new SimpleTarget<Bitmap>(500, 500) {

                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, Transition<? super Bitmap> transition) {
                        BlurImageWorker.loadBlurImage(BlurImageView.this,
                                getContext(), bitmap, isCurrentlyOnScreen);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        transitionToDefaultState(isCurrentlyOnScreen);
                    }
                });
    }
}
