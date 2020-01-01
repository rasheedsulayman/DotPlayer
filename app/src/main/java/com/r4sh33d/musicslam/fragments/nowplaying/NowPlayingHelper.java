package com.r4sh33d.musicslam.fragments.nowplaying;

import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition;
import com.bumptech.glide.request.transition.Transition;
import com.r4sh33d.musicslam.GlideApp;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.customglide.audiocover.AudioCoverImage;
import com.r4sh33d.musicslam.playback.MusicPlayer;
import com.r4sh33d.musicslam.utils.SlamUtils;
/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class NowPlayingHelper {

    private final SeekBar seekBar;
    private final ProgressBar progressBar;
    private final ImageView bcAlbumArtImageview; //for the small albumart shown in bottom controller
    private AudioCoverImage previousAudioCoverImage;

    public NowPlayingHelper(SeekBar seekBar, ProgressBar progressBar, ImageView albumartImageView) {
        this.seekBar = seekBar;
        this.progressBar = progressBar;
        this.bcAlbumArtImageview = albumartImageView;
        previousAudioCoverImage = new AudioCoverImage(MusicPlayer.getCurrentSong().data);
    }

    public void changeSeekBarColor(int color) {
        LayerDrawable layerDrawable = (LayerDrawable) seekBar.getProgressDrawable();
        ClipDrawable drawable = (ClipDrawable) layerDrawable.findDrawableByLayerId(android.R.id.progress);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        seekBar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    public void updateBottomControllerArt(NowPlayingFragment scope) {
        AudioCoverImage newAudioCoverImage = new AudioCoverImage(MusicPlayer.getCurrentSong().data);
        GlideApp.with(scope)
                .load(newAudioCoverImage)
                .fitCenter()
                .thumbnail(GlideApp.with(scope).load(previousAudioCoverImage).fitCenter())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target,
                                                boolean isFirstResource) {
                        bcAlbumArtImageview.setImageResource(R.drawable.default_artwork_small);
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                                   DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .fitCenter()
                .into(bcAlbumArtImageview);
        previousAudioCoverImage = newAudioCoverImage;
    }

    public void changeProgressBarColor(int color) {
        //bottom controller
        LayerDrawable progressBarLayerDrawable =
                (LayerDrawable) progressBar.getProgressDrawable().mutate();
        ClipDrawable seekbarClipDrawable = (ClipDrawable) progressBarLayerDrawable.
                findDrawableByLayerId(android.R.id.progress);
        seekbarClipDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }
}