package com.r4sh33d.musicslam.fragments.album;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
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

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.r4sh33d.musicslam.GlideApp;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.customglide.audiocover.AudioCoverImage;
import com.r4sh33d.musicslam.dataloaders.SongIdsLoader;
import com.r4sh33d.musicslam.dataloaders.SongLoader;
import com.r4sh33d.musicslam.dialogs.AddToPlaylistDialog;
import com.r4sh33d.musicslam.fragments.AbsParallaxArtworkDetailsFragment;
import com.r4sh33d.musicslam.models.Album;
import com.r4sh33d.musicslam.models.Song;
import com.r4sh33d.musicslam.playback.MusicPlayer;
import com.r4sh33d.musicslam.utils.MusicUtils;
import com.r4sh33d.musicslam.utils.SlamUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class AlbumDetailsFragment extends AbsParallaxArtworkDetailsFragment
        implements LoaderManager.LoaderCallbacks<List<Song>> {

    private static final String ARG_ALBUM = "album";
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
    @BindView(R.id.year)
    TextView yearTextView;
    @BindView(R.id.duration)
    TextView durationTextView;
    @BindView(R.id.album_details_background)
    LinearLayout albumDetailContainer;
    @BindView(R.id.first_album_detail_separator)
    TextView firstAlbumDetailSeparatorTextView;
    @BindView(R.id.calender_imageview)
    ImageView calenderImageView;
    Album mAlbum;
    SongsInAlbumAdapter mSongsInAlbumAdapter;

    public AlbumDetailsFragment() {
    }

    public static AlbumDetailsFragment newInstance(Album album) {
        AlbumDetailsFragment fragment = new AlbumDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ALBUM, album);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAlbum = getArguments().getParcelable(ARG_ALBUM);
        mSongsInAlbumAdapter = new SongsInAlbumAdapter(new ArrayList<>(), getActivity(), mAlbum);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_info, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadAlbumArt();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mSongsInAlbumAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Nullable
    @Override
    public RecyclerView.Adapter getAdapter() {
        return mSongsInAlbumAdapter;
    }

    @NonNull
    @Override
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        return new SongLoader.SongsAsyncTaskLoader(getContext(),
                SongLoader.ALBUM_SELECTION,
                new String[]{String.valueOf(mAlbum.id)},
                SongLoader.DEFAULT_ALBUM_SORT_ORDER);
    }


    @Override
    public void onLoadFinished(@NonNull Loader<List<Song>> loader, List<Song> data) {
        mSongsInAlbumAdapter.updateData(data);
        if (mAlbum.year > 0) {
            yearTextView.setText(String.valueOf(mAlbum.year));
        } else {
            SlamUtils.hideViews(calenderImageView, firstAlbumDetailSeparatorTextView, yearTextView);
        }
        songCountTextView.setText(String.valueOf(data.size()));
        durationTextView.setText(MusicUtils.makeShortTimeString(getContext(),
                (MusicUtils.getSongsDuration(data) / 1000)));
        albumDetailContainer.setVisibility(View.VISIBLE);
    }

    void fadeInViews() {
        fadeInView(upperBlackShade, 600);
        fadeInView(lowerBlackShade, 600);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Song>> loader) {
    }

    @Override
    public void onMediaStoreRefreshed() {
        getLoaderManager().restartLoader(0, null, this);
    }


    public void loadAlbumArt() {
        GlideApp.with(this).asBitmap()
                .load(new AudioCoverImage(mAlbum.firstSong.data))
                .transition(BitmapTransitionOptions.withCrossFade(150))
                .signature(SlamUtils.getMediaStoreSignature(mAlbum.firstSong))
                .into(new BitmapImageViewTarget(albumArt) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        super.onResourceReady(resource, transition);
                        fadeInViews();
                        onArtworkLoaded(resource);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        albumArt.setScaleType(ImageView.ScaleType.CENTER);
                        albumArt.setImageResource(R.drawable.ic_music_note_24dp);
                        fadeInViews();
                    }
                });
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
        return mAlbum.title;
    }

    @Override
    public void playShuffledSongs() {
        MusicPlayer.playShuffle(mSongsInAlbumAdapter.getData());
    }

    @Override
    public void playSongs() {
        MusicPlayer.playAll(mSongsInAlbumAdapter.getData(), 0, false);
    }

    @Override
    public void playNext() {
        MusicPlayer.playNext(mSongsInAlbumAdapter.getData(), getContext());
    }

    @Override
    public void addToQueue() {
        MusicPlayer.addToQueue(getContext(), mSongsInAlbumAdapter.getData());
    }

    @Override
    public void addToPlaylist() {
        AddToPlaylistDialog.newInstance(SongIdsLoader.getSongIdsListForAlbum(getContext(), mAlbum.id))
                .show(getFragmentManager(), AddToPlaylistDialog.ADD_TO_PLAYLIST_ARG);
    }
}