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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;

import com.r4sh33d.musicslam.playback.MusicPlayer;
import com.r4sh33d.musicslam.utils.RenderScriptHelper;

import timber.log.Timber;

public class BlurImageWorker {

    public static final int FADE_IN_TIME = 300;
    public static final int FADE_IN_TIME_SLOW = 1000;


    /**
     * Creates a transition drawable to Bitmap with params
     *
     * @param resources    Android Resources!
     * @param fromDrawable the drawable to transition from
     * @param toBitmap     the bitmap to transition to
     * @param fadeTime     the fade time in MS to fade in
     * @return the drawable if created, null otherwise
     */
    public static TransitionDrawable createImageTransitionDrawable(Resources resources,
                                                                   Drawable fromDrawable,
                                                                   Bitmap toBitmap, int fadeTime) {
        if (toBitmap != null) {
            final Drawable[] arrayDrawable = new Drawable[2];
            arrayDrawable[0] = getTopDrawable(fromDrawable);

            // Add the transition to drawable
            Drawable layerTwo;

            layerTwo = new BitmapDrawable(resources, toBitmap);
            arrayDrawable[1] = layerTwo;

            // Finally, return the image
            final TransitionDrawable result = new TransitionDrawable(arrayDrawable);
            //result.setCrossFadeEnabled(true);
            result.startTransition(fadeTime);
            return result;
        }
        return null;
    }

    public static TransitionDrawable createImageTransitionDrawable(Drawable fromDrawable,
                                                                   Drawable toDrawable, int fadeTime) {
        if (toDrawable != null) {
            final Drawable[] arrayDrawable = new Drawable[2];
            arrayDrawable[0] = getTopDrawable(fromDrawable);

            // Add the transition to drawable
            arrayDrawable[1] = toDrawable;

            // Finally, return the image
            final TransitionDrawable result = new TransitionDrawable(arrayDrawable);
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
        cancelWork(blurImageView);
        final BlurBitmapWorkerTask blurWorkerTask = new BlurBitmapWorkerTask(blurImageView,
                context, RenderScriptHelper.getRenderScript(context), animate);
        blurImageView.setTag(blurWorkerTask);
        blurWorkerTask.execute(toBitmap);
    }

    public static void cancelWork(final View image) {
        Object tag = image.getTag();
        if (tag instanceof BlurBitmapWorkerTask) {
            BlurBitmapWorkerTask bitmapWorkerTask = (BlurBitmapWorkerTask) tag;
            bitmapWorkerTask.cancel(true);
            Timber.d("Canceled succesfully");
            // clear out the tag
            image.setTag(null);
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
