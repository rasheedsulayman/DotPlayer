package com.r4sh33d.musicslam.fragments.artist;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.dataloaders.ArtistLoader;
import com.r4sh33d.musicslam.dataloaders.PagerLoaderIDs;
import com.r4sh33d.musicslam.fragments.pager.PagerFragment;
import com.r4sh33d.musicslam.models.Artist;
import com.r4sh33d.musicslam.utils.GridSpacingItemDecoration;
import com.r4sh33d.musicslam.utils.SlamUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistsFragment extends PagerFragment implements LoaderManager.LoaderCallbacks<List<Artist>> {

    ArtistGridAdapter mArtistGridAdapter;

    public ArtistsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mArtistGridAdapter = new ArtistGridAdapter(getContext(), new ArrayList<>());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = getRecyclerView();
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(3,
                SlamUtils.dpToPx(12, getActivity()), true));
        recyclerView.setAdapter(mArtistGridAdapter);
    }

    @Override
    public String getEmptyDataMessage() {
        return getString(R.string.no_artists);
    }

    @Override
    public RecyclerView.Adapter getAdapter() {
        return mArtistGridAdapter;
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.pager_grid_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(PagerLoaderIDs.ARTIST_FRAGMENTS, null, this);
    }


    @NonNull
    @Override
    public Loader<List<Artist>> onCreateLoader(int id, Bundle args) {
        return new ArtistLoader.ArtistAsynctaskLoader(getContext());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Artist>> loader, List<Artist> data) {
        mArtistGridAdapter.updateData(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Artist>> loader) {
    }


    @Override
    public void onMediaStoreRefreshed() {
        getLoaderManager().restartLoader(PagerLoaderIDs.ARTIST_FRAGMENTS, null, this);
    }

}
