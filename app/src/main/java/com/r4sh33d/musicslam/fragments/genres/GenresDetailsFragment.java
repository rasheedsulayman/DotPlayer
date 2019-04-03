package com.r4sh33d.musicslam.fragments.genres;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.customviews.ColoredFastScrollRecyclerView;
import com.r4sh33d.musicslam.customviews.ColoredStatusBarView;
import com.r4sh33d.musicslam.dataloaders.SongLoader;
import com.r4sh33d.musicslam.fragments.BaseListenerFragment;
import com.r4sh33d.musicslam.playback.MusicPlayer;
import com.r4sh33d.musicslam.models.Genres;
import com.r4sh33d.musicslam.models.Song;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class GenresDetailsFragment extends BaseListenerFragment
        implements LoaderManager.LoaderCallbacks<List<Song>>, Toolbar.OnMenuItemClickListener {
    @BindView(R.id.recyclerview)
    ColoredFastScrollRecyclerView recyclerView;

    private static final String ARG_GENRES = "genres";
    private Genres genres;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.status_bar_view)
    ColoredStatusBarView statusBarView;

    private SongsInGenresAdapter mSongsInGenresAdapter;

    public GenresDetailsFragment() {
        // Required empty public constructor
    }


    public static GenresDetailsFragment newInstance(Genres genres) {
        GenresDetailsFragment fragment = new GenresDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_GENRES, genres);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_genres_info, container, false);
        ButterKnife.bind(this, view);
        genres = getArguments().getParcelable(ARG_GENRES);
        if (isAlbumArtTheme) {
            statusBarView.setVisibility(View.GONE);
        }
        setUpPageDetails();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    private void setUpPageDetails() {
        if (isAlbumArtTheme) {
            statusBarView.setVisibility(View.GONE);
        }
        mSongsInGenresAdapter = new SongsInGenresAdapter(new ArrayList<>(), getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mSongsInGenresAdapter);

        toolbar.setOnMenuItemClickListener(this);
        toolbar.inflateMenu(R.menu.menu_genres_info);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_white_24dp);
        toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        toolbar.setTitle(genres.name);

    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_shuffle_all:
                MusicPlayer.playShuffle(SongLoader.getSongsForGenres(genres.id, getContext()));
                return true;
        }
        return false;
    }

    @Override
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        return new SongLoader.GenresSongAsyncTaskLoader(getContext(), genres.id);
    }

    @Override
    public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
        mSongsInGenresAdapter.updateData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {
    }

    @Override
    public void onMediaStoreRefreshed() {
        getLoaderManager().restartLoader(0, null, this);
    }
}
