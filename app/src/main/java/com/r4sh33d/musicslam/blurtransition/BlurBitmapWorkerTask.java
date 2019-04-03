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
import android.os.AsyncTask;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

import java.lang.ref.WeakReference;

public class BlurBitmapWorkerTask extends AsyncTask<Bitmap, Void, Bitmap> {
    // if the image is too small, the blur will look bad post scale up so we use the min size
    // to scale up before bluring
    private static final int MIN_BITMAP_SIZE = 500;
    private static final int NUM_BLUR_RUNS = 8;
    private static final float BLUR_RADIUS = 25f;

    protected Drawable mFromDrawable;
    private boolean animate;
    Resources mResources;


    private final WeakReference<BlurImageView> mBlurScrimImage;

    protected final RenderScript mRenderScript;


    public BlurBitmapWorkerTask(final BlurImageView blurImageView,
                                final Context context, final RenderScript renderScript, boolean animate) {
        mBlurScrimImage = new WeakReference<>(blurImageView);
        mRenderScript = renderScript;
        mResources = context.getResources();
        // use the existing image as the drawable and if it doesn't exist fallback to transparent
        mFromDrawable = blurImageView.getDrawable();
        this.animate = animate;
        if (mFromDrawable == null) {
            mFromDrawable = new ColorDrawable(Color.TRANSPARENT);
        }
    }


    @Override
    protected Bitmap doInBackground(Bitmap... bitmaps) {
        Bitmap bitmap = bitmaps[0];
        Bitmap output;

        if (bitmap != null) {
            Bitmap input = bitmap;
            output = Bitmap.createBitmap(input.getWidth(), input.getHeight(), input.getConfig());
            // run the blur multiple times
            for (int i = 0; i < NUM_BLUR_RUNS; i++) {
                final Allocation inputAlloc = Allocation.createFromBitmap(mRenderScript, input);
                final Allocation outputAlloc = Allocation.createTyped(mRenderScript,
                        inputAlloc.getType());
                final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(mRenderScript,
                        Element.U8_4(mRenderScript));

                script.setRadius(BLUR_RADIUS);
                script.setInput(inputAlloc);
                script.forEach(outputAlloc);
                outputAlloc.copyTo(output);
                input = output;
            }

            return output;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        BlurImageView blurImageView = mBlurScrimImage.get();
        if (blurImageView != null) {
            if (result == null) {
                // if we have no image, then signal the transition to the default state
                blurImageView.transitionToDefaultState(animate);
            } else {
                if (animate) {
                    blurImageView.setTransitionDrawable(BlurImageWorker.createImageTransitionDrawable(mResources,
                            mFromDrawable, result,
                            BlurImageWorker.FADE_IN_TIME, false, true));
                } else {
                    blurImageView.setTransitionDrawable(new BitmapDrawable(mResources, result));
                }
            }
        }
    }
}