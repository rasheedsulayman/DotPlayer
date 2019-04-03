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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;

import com.r4sh33d.musicslam.utils.RenderScriptHelper;
import com.r4sh33d.musicslam.playback.MusicPlayer;

import java.util.concurrent.RejectedExecutionException;

public class BlurImageWorker {

    public static final int FADE_IN_TIME = 300;
    public static final int FADE_IN_TIME_SLOW = 1000;


    /**
     * Creates a transition drawable to Bitmap with params
     *
     * @param resources    Android Resources!
     * @param fromDrawable the drawable to transition from
     * @param bitmap       the bitmap to transition to
     * @param fadeTime     the fade time in MS to fade in
     * @param dither       setting
     * @param force        force create a transition even if bitmap == null (fade to transparent)
     * @return the drawable if created, null otherwise
     */
    public static TransitionDrawable createImageTransitionDrawable(final Resources resources,
                                                                   final Drawable fromDrawable,
                                                                   final Bitmap bitmap, final int fadeTime,
                                                                   final boolean dither, final boolean force) {
        if (bitmap != null || force) {
            final Drawable[] arrayDrawable = new Drawable[2];
            arrayDrawable[0] = getTopDrawable(fromDrawable);

            // Add the transition to drawable
            Drawable layerTwo;
            if (bitmap != null) {
                layerTwo = new BitmapDrawable(resources, bitmap);
                //  layerTwo.setFilterBitmap(false);
                // layerTwo.setDither(dither);
            } else {
                // if no bitmap (forced) then transition to transparent
                layerTwo = new ColorDrawable(Color.TRANSPARENT);
            }

            arrayDrawable[1] = layerTwo;

            // Finally, return the image
            final TransitionDrawable result = new TransitionDrawable(arrayDrawable);
            //result.setCrossFadeEnabled(true);
            result.startTransition(fadeTime);
            return result;
        }
        return null;
    }


    public static Drawable getTopDrawable(final Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Drawable retDrawable = drawable;
        while (retDrawable instanceof TransitionDrawable) {
            TransitionDrawable transition = (TransitionDrawable) retDrawable;
            retDrawable = transition.getDrawable(transition.getNumberOfLayers() - 1);
        }

        return retDrawable;
    }

    protected static void loadBlurImage(final BlurImageView blurImageView,
                                        Context context, Bitmap toBitmap, boolean animate) {
        if (blurImageView == null) {
            return;
        }
        final BlurBitmapWorkerTask blurWorkerTask = new BlurBitmapWorkerTask(blurImageView,
                context, RenderScriptHelper.getRenderScript(context), animate);
        try {
            blurWorkerTask.execute(toBitmap);
        } catch (RejectedExecutionException e) {
            blurImageView.transitionToDefaultState(animate);
        }
    }

    public static String generateAlbumCacheKey(final String albumName, final String artistName) {
        if (albumName == null || artistName == null) {
            return null;
        }
        return albumName + "_" + artistName + "_" + "album";
    }

    public static String getCurrentCacheKey() {
        return generateAlbumCacheKey(MusicPlayer.getCurrentSong().artistName,
                MusicPlayer.getCurrentSong().albumName);
    }
}
