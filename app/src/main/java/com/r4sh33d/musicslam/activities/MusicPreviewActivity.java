package com.r4sh33d.musicslam.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.r4sh33d.musicslam.GlideApp;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.customglide.audiocover.AudioCoverImage;
import com.r4sh33d.musicslam.customviews.playpause.PlayIconDrawable;
import com.r4sh33d.musicslam.customviews.playpause.PlayIconView;
import com.r4sh33d.musicslam.dataloaders.SongLoader;
import com.r4sh33d.musicslam.fragments.nowplaying.ProgressUpdateHelper;
import com.r4sh33d.musicslam.models.Song;
import com.r4sh33d.musicslam.playback.MusicPlayer;
import com.r4sh33d.musicslam.utils.MusicUtils;
import com.r4sh33d.musicslam.utils.NavigationUtil;
import com.r4sh33d.musicslam.utils.SlamUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.r4sh33d.musicslam.fragments.nowplaying.ProgressUpdateHelper.OnProgressUpdateListener;

public class MusicPreviewActivity extends MusicEventsListenerActivity implements
        OnProgressUpdateListener, View.OnTouchListener {

    @BindView(R.id.song_title_textview)
    TextView songTitleTextView;
    @BindView(R.id.song_artist_textview)
    TextView songArtistTextView;
    @BindView(R.id.album_art_imageview)
    ImageView albumArtImageView;
    @BindView(R.id.progress_seekbar)
    SeekBar progressSeekbar;
    @BindView(R.id.play_pause_iconview)
    PlayIconView playIconView;
    @BindView(R.id.content_linear_layout)
    LinearLayout contentLinearLayout;
    @BindView(R.id.song_elapsed_time_textview)
    TextView elapsedTimeTextView;
    @BindView(R.id.song_duration_textview)
    TextView songDurationTextView;
    @BindView(R.id.container_frame_layout)
    FrameLayout containerFrameLayout;

    ProgressUpdateHelper progressUpdateHelper;

    public boolean playbackAlreadyHandled;
    public boolean isGoingToMusicPlayer;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_preview);
        ButterKnife.bind(this);
        setContentLinearLayoutWidth();
        playIconView.setColor(Color.BLACK);
        progressUpdateHelper = new ProgressUpdateHelper(this);
        progressUpdateHelper.startUpdates();
        songTitleTextView.setSelected(true);
        songArtistTextView.setSelected(true);
        containerFrameLayout.setOnTouchListener(this);
        setSeekBarChangeListener();
        if (savedInstanceState != null) {
            playbackAlreadyHandled = true;
        } else {
            progressSeekbar.setProgress(0);
        }
    }

    void setContentLinearLayoutWidth() {
        Point size = SlamUtils.getDefaultDisplaySize(this);
        contentLinearLayout.getLayoutParams().width =
                Math.min(size.x, size.y) - SlamUtils.dpToPx(40, this);
    }

    @SuppressWarnings("ConstantConditions")
    private void handleIntent(Intent intent) {
        if (intent == null) {
            Timber.d("No intent");
            finish();
            return;
        }
        Uri uri = intent.getData();
        if (uri == null) {
            Timber.d("No uri data");
            finish();
            return;
        }
        ArrayList<Song> songs = SongLoader.getSongFromLocalUri(uri, this);
        if (songs.size() <= 0) {
            finish();
            return;
        }
        MusicPlayer.playSongsFromUri(songs);
        setIntent(new Intent());
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        if (!playbackAlreadyHandled) {
            handleIntent(getIntent());
        } else {
            updateViews();
            contentLinearLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMetaChanged() {
        super.onMetaChanged();
        updateViews();
        contentLinearLayout.setVisibility(View.VISIBLE);
    }

    public void updatePlayPauseButton() {
        playIconView.setIconState(MusicPlayer.isPlaying() ? PlayIconDrawable.IconState.PAUSE :
                PlayIconDrawable.IconState.PLAY);
    }

    public void updateViews() {
        Song song = MusicPlayer.getCurrentSong();
        songTitleTextView.setText(song.title);
        songArtistTextView.setText(song.artistName);
        songDurationTextView.setText(SlamUtils.makeShortTimeString(this,
                MusicPlayer.getCurrentSongDuration() / 1000));
        updatePlayPauseButton();
        progressSeekbar.setMax((int) MusicPlayer.getCurrentSongDuration() / 1000);
        GlideApp.with(this)
                .load(new AudioCoverImage(song.data))
                .transition(DrawableTransitionOptions.withCrossFade(100))
                .error(getDrawable(R.drawable.default_artwork_small))
                .into(albumArtImageView);
    }

    @Override
    public void onPlayStateChanged() {
        super.onPlayStateChanged();
        updatePlayPauseButton();
    }

    @OnClick(R.id.play_pause_wrapper)
    public void onClickPlayPauseWrapper() {
        MusicPlayer.playOrPause();
    }

    @OnClick(R.id.open_music_textview)
    public void onClickOpenMusic() {
        isGoingToMusicPlayer = true;
        startActivity(NavigationUtil.getAppRestartIntent(this));
        finish();
    }

    @Override
    public void onProgressUpdate(int currentTimeInSecs, int totalDurationInSecs) {
        progressSeekbar.setProgress(currentTimeInSecs);
        elapsedTimeTextView.setText(MusicUtils.makeShortTimeString(this, currentTimeInSecs));
    }

    private void setSeekBarChangeListener() {
        progressSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    MusicPlayer.seek(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!(isChangingConfigurations() || isGoingToMusicPlayer)) {
            MusicPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressUpdateHelper.stopUpdates();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        int containerX1 = (int) contentLinearLayout.getX();
        int containerY1 = (int) contentLinearLayout.getY();
        int containerX2 = (int) (contentLinearLayout.getX() + contentLinearLayout.getWidth());
        int containerY2 = (int) (contentLinearLayout.getY() + contentLinearLayout.getHeight());
        Rect r = new Rect();
        r.set(containerX1, containerY1, containerX2, containerY2);
        if (!r.contains(x, y)) {
            finish();
        }
        return false;
    }
}
