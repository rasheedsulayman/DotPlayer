package com.r4sh33d.musicslam.blurtransition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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

public class BlurImageView extends android.support.v7.widget.AppCompatImageView {
    private boolean isUsingDefaultBlur;
    private boolean isCurrentlyOnScreen;
    String albumArtKey = "no_key";

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
        Bitmap blurredBitmap = BitmapFactory.decodeResource(getContext().getResources(),
                R.drawable.default_artwork_blur);
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
        // if we are already showing the default blur and we are transitioning to the default blur
        // then don't do the transition at all
        if (isUsingDefaultBlur) {
            return;
        }

        Drawable drawable = getContext().getDrawable(R.drawable.default_artwork_blur);

        if (animate) {
            drawable = BlurImageWorker.createImageTransitionDrawable(getDrawable(), drawable , BlurImageWorker.FADE_IN_TIME);
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
