package com.r4sh33d.musicslam.fragments.nowplaying;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.Util;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.r4sh33d.musicslam.GlideApp;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.activities.SettingsActivity;
import com.r4sh33d.musicslam.blurtransition.BlurImageView;
import com.r4sh33d.musicslam.customglide.audiocover.AudioCoverImage;
import com.r4sh33d.musicslam.customviews.playpause.PlayIconDrawable;
import com.r4sh33d.musicslam.customviews.playpause.PlayIconView;
import com.r4sh33d.musicslam.dialogs.AddToPlaylistDialog;
import com.r4sh33d.musicslam.dialogs.DeleteSongsDialog;
import com.r4sh33d.musicslam.dialogs.NewPlaylistDialog;
import com.r4sh33d.musicslam.dialogs.SongDetailsDialog;
import com.r4sh33d.musicslam.fragments.BaseListenerFragment;
import com.r4sh33d.musicslam.interfaces.PaletteListener;
import com.r4sh33d.musicslam.interfaces.SlidingPanelEventsListener;
import com.r4sh33d.musicslam.models.Song;
import com.r4sh33d.musicslam.playback.Constants;
import com.r4sh33d.musicslam.playback.MusicPlayer;
import com.r4sh33d.musicslam.sleeptimer.SleepTimerDialog;
import com.r4sh33d.musicslam.utils.ColorHelper;
import com.r4sh33d.musicslam.utils.MusicUtils;
import com.r4sh33d.musicslam.utils.NavigationUtil;
import com.r4sh33d.musicslam.utils.PlayListHelper;
import com.r4sh33d.musicslam.utils.SlamUtils;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.disposables.Disposable;


public class NowplayingFragment extends BaseListenerFragment implements
        Toolbar.OnMenuItemClickListener, SlidingPanelEventsListener, PaletteListener,
        ProgressUpdateHelper.OnProgressUpdateListener {

    @BindView(R.id.bg_shade_black)
    ImageView albumartShadeBlack;

    @BindView(R.id.blurImage)
    BlurImageView blurImageView;

    @BindView(R.id.song_artist)
    TextView songartist;

    @BindView(R.id.song_duration)
    TextView songduration;

    @BindView(R.id.song_elapsed_time)
    TextView elapsedtime;

    @BindView(R.id.song_progress)
    SeekBar mSeekBar;

    @BindView(R.id.song_title)
    TextView songtitle;

    @BindView(R.id.play_pause_view)
    PlayIconView playPauseView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.shuffle)
    ImageView shuffle;

    @BindView(R.id.repeat)
    ImageView repeat;

    @BindView(R.id.buttom_controller_container)
    LinearLayout bcControllerContainer;

    @BindView(R.id.toggle_favourite)
    ImageView toggleFavourite;


    @BindView(R.id.open_equalizer)
    ImageView menuEqualizer;

    //---------------------------------------------------------------------------------

    @BindView(R.id.npc_album_art)
    ImageView bcAlbumart;

    @BindView(R.id.npc_song_artist)
    TextView bcSongArtist;

    @BindView(R.id.npc_song_title)
    TextView bcSongTitle;

    @BindView(R.id.npc_play_pause)
    PlayIconView bcPlayPause;

    @BindView(R.id.npc_song_progressbar)
    ProgressBar bcProgressbar;

    private Disposable colorAccentSubscription;

    private Unbinder unbinder;
    NowPlayingControlsCallback mActivityCallback;
    int currentPaletteColor;

    private ProgressUpdateHelper progressUpdateHelper;


    @OnClick(R.id.buttom_controller_container)
    public void buttomContainerControllerClicked() {
        mActivityCallback.setPanelState(PanelState.EXPANDED);
    }

    public void subscribeToColorAccentUpdate() {
        colorAccentSubscription = Aesthetic.get()
                .colorAccent()
                .subscribe(color -> {
                    bcPlayPause.setColor(color);
                    NowPlayingHelper.changeProgressBarColor(color, bcProgressbar);
                });
    }


    public NowplayingFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mActivityCallback = (NowPlayingControlsCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement NowPlayingControlsCallback");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_now_playing3, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivityCallback.setPanelEventListenerListener(this);
        currentPaletteColor = getResources().getColor(R.color.transparent);
        progressUpdateHelper = new ProgressUpdateHelper(this);
        setUpToolbar();
        songtitle.setSelected(true);
        songartist.setSelected(true);
        if (isAlbumArtTheme) {
            blurImageView.setVisibility(View.GONE);
            albumartShadeBlack.setVisibility(View.GONE);
        } else {
            subscribeToColorAccentUpdate();
            toolbar.setPadding(0, SlamUtils.dpToPx(24, getContext()), 0, 0);
        }
        setSeekBarChangeListener();
    }


    void setUpToolbar() {
        if (prefsUtils.isDarkTheme()) {
            toolbar.setPopupTheme(R.style.BlackOverflowButtonStyle);
        }
        Util.setOverflowButtonColor(toolbar, Color.WHITE);
        toolbar.inflateMenu(R.menu.menu_fragment_nowplaying);
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setNavigationIcon(R.drawable.ic_big_down_arrow);
        toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
    }

    private void setSeekBarChangeListener() {
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        mSeekBar.setOnClickListener(v -> {
        });
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_playing_queue:
                mActivityCallback.setTouchSlidingPanelEnabled(false);
                NavigationUtil.navigateTONowPlaying(getContext());
                return true;
            case R.id.menu_add_to_playlist:
                AddToPlaylistDialog.newInstance(new long[]{MusicPlayer.getCurrentSong().id}).show(getFragmentManager(),
                        AddToPlaylistDialog.ADD_TO_PLAYLIST_ARG);
                return true;
            case R.id.menu_save_queue:
                NewPlaylistDialog.newInstance(MusicUtils.getSongIdsFromSongsList(MusicPlayer.getNowPlayingQueue()))
                        .show(getFragmentManager(), AddToPlaylistDialog.ADD_TO_PLAYLIST_ARG);
                return true;
            case R.id.menu_clear_queue:
                MusicPlayer.clearQueue();
                return true;
            case R.id.menu_song_share:
                SlamUtils.shareSong(MusicPlayer.getCurrentSong(), getContext());
                return true;
            case R.id.menu_song_info:
                SongDetailsDialog.newInstance(MusicPlayer.getCurrentSong())
                        .show(getFragmentManager(), SongDetailsDialog.SONG_DETAILS_DIALOG);
                return true;
            case R.id.menu_song_delete:
                DeleteSongsDialog.newInstance(new long[]{MusicPlayer.getCurrentSong().id},
                        MusicPlayer.getCurrentSong().title).show(getFragmentManager(),
                        DeleteSongsDialog.DELETE_FRAG_TAG);
                return true;
            case R.id.menu_sleep_timer:
                new SleepTimerDialog()
                        .show(getFragmentManager(), SleepTimerDialog.SLEEP_DIALOG_ARG);
                return true;
            case R.id.settings:
                startActivity(new Intent(getContext(), SettingsActivity.class));
                return true;
        }
        return false;
    }


    void loadBlurredArtWork() {
        blurImageView.loadBlurImage();
    }

    @Override
    public void onStart() {
        super.onStart();
        progressUpdateHelper.startUpdates();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (blurImageView != null) {
            blurImageView.onHiddenStatusChanged(hidden);
        }
    }

    public void updateViews() {
        if (!isAlbumArtTheme) {
            loadBlurredArtWork();
        }
        updateRepeatState();
        updateShuffleState();
        updatePlayPauseButton();
        updateAddFavourite();
        updateImage();
        songduration.setText(SlamUtils.makeShortTimeString(getContext(),
                MusicPlayer.getCurrentSongDuration() / 1000));
        Song song = MusicPlayer.getCurrentSong();
        songtitle.setText(song.title);
        bcSongTitle.setText(song.title);
        songartist.setText(song.artistName);
        bcSongArtist.setText(song.artistName);
        mSeekBar.setMax((int) MusicPlayer.getCurrentSongDuration() / 1000);
        bcProgressbar.setMax((int) MusicPlayer.getCurrentSongDuration() / 1000);
    }

    private void updateRepeatState() {
        switch (MusicPlayer.getRepeatMode()) {
            case Constants.REPEAT_ALL:
                repeat.setImageResource(R.drawable.ic_repeat_white_24dp);
                repeat.setColorFilter(currentPaletteColor);
                break;
            case Constants.REPEAT_CURRENT:
                repeat.setImageResource(R.drawable.ic_repeat_one_white_24dp);
                repeat.setColorFilter(currentPaletteColor);
                break;
            case Constants.REPEAT_NONE:
                repeat.setImageResource(R.drawable.ic_repeat_white_24dp);
                repeat.setColorFilter(ContextCompat.getColor(getContext(), R.color.material_white));
                break;
        }
    }

    void updateImage() {
        AudioCoverImage audioCoverImage = new AudioCoverImage(MusicPlayer.getCurrentSong().data);
        GlideApp.with(this)
                .asBitmap()
                .load(audioCoverImage)
                .transition(BitmapTransitionOptions.withCrossFade())
                .placeholder(getContext().getDrawable(R.drawable.def))
                .into(bcAlbumart);
    }

    public void updateShuffleState() {
        switch (MusicPlayer.getShuffleMode()) {
            case Constants.SHUFFLE_NORMAL:
                shuffle.setColorFilter(currentPaletteColor);
                break;
            case Constants.SHUFFLE_NONE:
                shuffle.setColorFilter(ContextCompat.getColor(getContext(), R.color.material_white));
                break;
        }
    }

    public void updateAddFavourite() {
        if (MusicPlayer.getCurrentSong().id != -1 &&
                PlayListHelper.isSongAFovouriteSong(MusicPlayer.getCurrentSong().id, getContext())) {
            toggleFavourite.setImageDrawable(ColorHelper.getTintedDrawable(R.drawable.ic_favorite_white_48dp,
                    currentPaletteColor, getContext()));
        } else {
            toggleFavourite.setImageResource(R.drawable.ic_favorite_border_white_48dp);
        }
    }

    public void updatePlayPauseButton() {
        if (MusicPlayer.isPlaying()) {
            playPauseView.animateToState(PlayIconDrawable.IconState.PAUSE);
            bcPlayPause.animateToState(PlayIconDrawable.IconState.PAUSE);
        } else {
            playPauseView.animateToState(PlayIconDrawable.IconState.PLAY);
            bcPlayPause.animateToState(PlayIconDrawable.IconState.PLAY);
        }
    }

    @OnClick(R.id.shuffle_mode_wrapper)
    public void onClickShuffle() {
        MusicPlayer.cycleShuffle();
    }

    @OnClick(R.id.favourite_wrapper)
    public void toggleFavouriteClicked() {
        PlayListHelper.toggleAddFavourite(MusicPlayer.getCurrentSong().id, getContext());
        updateAddFavourite();
    }

    @OnClick(R.id.equalizer_wrapper)
    public void onClickOpenEqualizer() {
        NavigationUtil.openEqualizer(mActivity, NavigationUtil.OPEN_EQUALIZER_REQUEST);
    }

    @OnClick(R.id.repeat_wrapper)
    public void onClickRepeat() {
        MusicPlayer.cycleRepeat();
    }

    @OnClick(R.id.next_wrapper)
    public void onClickNext() {
        MusicPlayer.next();
    }

    @OnClick(R.id.previous_wrapper)
    public void onClickPrevious() {
        MusicPlayer.previous(getActivity(), false);
    }

    @OnClick(R.id.play_pause_wrapper)
    public void onClickPlayPause() {
        MusicPlayer.playOrPause();
    }

    @OnClick(R.id.bc_play_pause_wrapper)
    public void onClickBcPlayPauseView() {
        MusicPlayer.playOrPause();
    }

    @Override
    public void onMetaChanged() {
        updateViews();
    }

    @Override
    public void onRepeatModeChanged() {
        updateRepeatState();
    }

    @Override
    public void onPlayStateChanged() {
        updatePlayPauseButton();
    }

    @Override
    public void onShuffleModeChanged() {
        updateShuffleState();
    }

    @Override
    public void onServiceConnected() {
        updateViews();
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        adjustViewsOnPanePanelSlide(slideOffset);
    }

    @Override
    public void onPanelStateChanged(@Nullable View panel, @Nullable PanelState previousState,
                                    PanelState newState) {
        switch (newState) {
            case COLLAPSED:
                adjustViewsToCollapsedMode();
                break;
            case EXPANDED:
                adjustViewsToExpandedMode();
        }
    }

    void adjustViewsOnPanePanelSlide(float slideOffset) {
        toolbar.setVisibility(View.VISIBLE);
        bcControllerContainer.setVisibility(View.VISIBLE);
        toolbar.setAlpha(slideOffset);
        bcControllerContainer.setAlpha(1 - slideOffset);
    }

    void adjustViewsToExpandedMode() {
        toolbar.setVisibility(View.VISIBLE);
        bcControllerContainer.setVisibility(View.INVISIBLE);
    }

    void adjustViewsToCollapsedMode() {
        toolbar.setVisibility(View.INVISIBLE);
        bcControllerContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPaletteReady(int color) {
        currentPaletteColor = color;
        NowPlayingHelper.changeSeekBarColor(color, mSeekBar);
        updateAddFavourite();
        updateShuffleState();
        updateRepeatState();
        if (isAlbumArtTheme) {
            NowPlayingHelper.changeProgressBarColor(color, bcProgressbar);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        progressUpdateHelper.stopUpdates();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!isAlbumArtTheme) {
            colorAccentSubscription.dispose();
        }
        mActivityCallback.removePanelEventListenerListener(this);
        unbinder.unbind();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivityCallback = null;
    }

    @Override
    public void onProgressUpdate(int currentTimeInSecs, int totalDurationInSecs) {
        mSeekBar.setProgress(currentTimeInSecs);
        bcProgressbar.setProgress(currentTimeInSecs);
        elapsedtime.setText(MusicUtils.makeShortTimeString(getContext(), currentTimeInSecs));
    }

    public interface NowPlayingControlsCallback {

        int getCurrentPaletteColor();

        SlidingUpPanelLayout.PanelState getPanelState();

        void setPanelState(PanelState panelState);

        void setTouchSlidingPanelEnabled(boolean isEnabled);

        void setPanelEventListenerListener(SlidingPanelEventsListener panelEventsListener);

        void removePanelEventListenerListener(SlidingPanelEventsListener panelEventsListener);
    }
}