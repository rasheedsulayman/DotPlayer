package com.r4sh33d.musicslam.blurtransition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.r4sh33d.musicslam.GlideApp;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.customglide.audiocover.AudioCoverImage;
import com.r4sh33d.musicslam.playback.MusicPlayer;

public class BlurImageView extends android.support.v7.widget.AppCompatImageView {
    String albumArtKey = "no_key";
    private boolean isUsingDefaultBlur;
    private boolean isCurrentlyOnScreen;
    private SimpleTarget<Bitmap> target;

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
        // if we are already showing the default blur and we are transitioning to the default blur
        // then don't do the transition at all
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
        if (target != null) {
            Glide.with(getContext().getApplicationContext()).clear(target);
        }
        target = GlideApp.with(getContext().getApplicationContext()).
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
