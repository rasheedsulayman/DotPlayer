package com.r4sh33d.musicslam.fragments.artist;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.r4sh33d.musicslam.fragments.album.AlbumGridAdapter;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.utils.GridSpacingItemDecoration;
import com.r4sh33d.musicslam.utils.SlamUtils;
import com.r4sh33d.musicslam.dataloaders.AlbumLoader;
import com.r4sh33d.musicslam.fragments.BaseListenerFragment;
import com.r4sh33d.musicslam.models.Album;
import com.r4sh33d.musicslam.models.Artist;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumInArtistFragment extends BaseListenerFragment
        implements LoaderManager.LoaderCallbacks<List<Album>> {

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;

    private static final String ARG_ARTIST = "artist";
    private Artist mArtist;
    private AlbumGridAdapter mAlbumsInArtistAdapter;

    public AlbumInArtistFragment() {
        // Required empty public constructor
    }

    public static AlbumInArtistFragment newInstance(Artist artist) {
        AlbumInArtistFragment fragment = new AlbumInArtistFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ARTIST, artist);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_album_in_artist, container, false);
        ButterKnife.bind(this, view);
        mArtist = getArguments().getParcelable(ARG_ARTIST);
        setPageDetails();
        return view;
    }

    void setPageDetails() {
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(3,
                SlamUtils.dpToPx(12, getActivity()), true));
        mAlbumsInArtistAdapter = new AlbumGridAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(mAlbumsInArtistAdapter);
    }


    @Override
    public Loader<List<Album>> onCreateLoader(int id, Bundle args) {
        return new AlbumLoader.AlbumAsyncTaskLoader(getContext(), mArtist.id);
    }

    @Override
    public void onLoadFinished(Loader<List<Album>> loader, List<Album> data) {
        mAlbumsInArtistAdapter.updateData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Album>> loader) {
    }

    @Override
    public void onMediaStoreRefreshed() {
        getLoaderManager().restartLoader(0, null, this);
    }
}
