package com.r4sh33d.musicslam.fragments.playlist;


import android.animation.Animator;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.r4sh33d.musicslam.GlideApp;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.utils.AnimationUtils;
import com.r4sh33d.musicslam.utils.MusicUtils;
import com.r4sh33d.musicslam.utils.SlamUtils;
import com.r4sh33d.musicslam.fragments.AbsParallaxArtworkDetailsFragment;
import com.r4sh33d.musicslam.customglide.audiocover.AudioCoverImage;
import com.r4sh33d.musicslam.dataloaders.SongIdsLoader;
import com.r4sh33d.musicslam.dataloaders.SongLoader;
import com.r4sh33d.musicslam.dialogs.AddToPlaylistDialog;
import com.r4sh33d.musicslam.playback.MusicPlayer;
import com.r4sh33d.musicslam.models.Playlist;
import com.r4sh33d.musicslam.models.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class PlaylistDetailsFragment extends AbsParallaxArtworkDetailsFragment
        implements LoaderManager.LoaderCallbacks<List<Song>> {

    @BindView(R.id.backdrop)
    ImageView albumArt;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.appbar)
    AppBarLayout appBarLayout;
    @BindView(R.id.main_content)
    CoordinatorLayout rootView;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.song_count)
    TextView songCountTextView;

    @BindView(R.id.duration)
    TextView durationTextView;

    @BindView(R.id.playlist_details_background)
    LinearLayout playlistDetailContainer;

    Random random = new Random();

    private static final String ARG_PLAYLIST = "playlist";
    Playlist mPlaylist;
    private SongsInPlaylistAdapter mSongsInPlayListAdapter;

    public PlaylistDetailsFragment() {
        // Required empty public constructor
    }

    public static PlaylistDetailsFragment newInstance(Playlist playlist) {
        PlaylistDetailsFragment fragment = new PlaylistDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PLAYLIST, playlist);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlaylist = getArguments().getParcelable(ARG_PLAYLIST);
        mSongsInPlayListAdapter = new SongsInPlaylistAdapter(new ArrayList<>(), getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play_list_info, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mSongsInPlayListAdapter);
    }

    void fadeInViews() {
        fadeInView(upperBlackShade, 750);
        fadeInView(lowerBlackShade, 750);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Nullable
    @Override
    public RecyclerView.Adapter getAdapter() {
        return mSongsInPlayListAdapter;
    }

    @NonNull
    @Override
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        return new SongLoader.PlayListSongAsysnctaskLoader(getContext(), mPlaylist.id);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Song>> loader, List<Song> data) {
        mSongsInPlayListAdapter.updateData(data);
        songCountTextView.setText(String.valueOf(data.size()));
        durationTextView.setText(MusicUtils.makeShortTimeString(getContext(),
                (MusicUtils.getSongsDuration(data) / 1000)));
        if (data.size() > 0) {
            // We want to use a random song in the playlist to generate the album art
            Song song = data.get(random.nextInt(data.size()));
            loadAlbumArt(song);
            playlistDetailContainer.setVisibility(View.VISIBLE);
        } else {
            SlamUtils.hideViews(playlistDetailContainer);
        }
        getLoaderManager().destroyLoader(0);
    }

    public void loadAlbumArt(Song song) {
        GlideApp.with(this).asBitmap()
                .load(new AudioCoverImage(song.data))
                .error(R.drawable.album_holdertest)
                .into(new BitmapImageViewTarget(albumArt) {
                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        AnimationUtils.revealAnimation(albumArt, animation -> fadeInViews());
                    }

                    @Override
                    public void onResourceReady(Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        super.onResourceReady(resource, transition);
                        AnimationUtils.revealAnimation(albumArt, animation -> fadeInViews());
                        onArtworkLoaded(resource);
                    }
                });
    }


    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {
    }

    @Override
    public void onMediaStoreRefreshed() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onPlaylistChanged() {
        onMediaStoreRefreshed();
    }

    @Override
    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public ImageView getArtWorkImageView() {
        return albumArt;
    }


    @Override
    public AppBarLayout getAppbarLayout() {
        return appBarLayout;
    }

    @Override
    public String getToolbarTitle() {
        return mPlaylist.name;
    }

    @Override
    public void playShuffledSongs() {
        MusicPlayer.playShuffle(mSongsInPlayListAdapter.getData());
    }

    @Override
    public void playSongs() {
        MusicPlayer.playAll(
                mSongsInPlayListAdapter.getData(),
                0,
                false);
    }

    @Override
    public void playNext() {
        MusicPlayer.playNext(mSongsInPlayListAdapter.getData(), getContext());
    }

    @Override
    public void addToQueue() {
        MusicPlayer.addToQueue(getContext(), mSongsInPlayListAdapter.getData());
    }

    @Override
    public void addToPlaylist() {
        AddToPlaylistDialog.newInstance(SongIdsLoader.getSongIdsListForPlaylist(getContext(), mPlaylist))
                .show(getFragmentManager(), AddToPlaylistDialog.ADD_TO_PLAYLIST_ARG);
    }
}
