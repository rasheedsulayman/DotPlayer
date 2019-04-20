package com.r4sh33d.musicslam.fragments.album;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.utils.GridSpacingItemDecoration;
import com.r4sh33d.musicslam.utils.SlamUtils;
import com.r4sh33d.musicslam.dataloaders.AlbumLoader;
import com.r4sh33d.musicslam.dataloaders.PagerLoaderIDs;
import com.r4sh33d.musicslam.fragments.pager.PagerFragment;
import com.r4sh33d.musicslam.models.Album;

import java.util.Collections;
import java.util.List;

public class AlbumsFragment extends PagerFragment implements LoaderManager.LoaderCallbacks<List<Album>> {

    private AlbumGridAdapter mAlbumGridAdapter;

    public AlbumsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAlbumGridAdapter = new AlbumGridAdapter(getActivity(), Collections.emptyList());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = getRecyclerView();
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(3,
                SlamUtils.dpToPx(12, getActivity()), true));
        recyclerView.setAdapter(mAlbumGridAdapter);
    }

    @Override
    public String getEmptyDataMessage() {
        return getString(R.string.no_albums);
    }

    @Override
    public RecyclerView.Adapter getAdapter() {
        return mAlbumGridAdapter;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(PagerLoaderIDs.ALBUMS_FRAGMENT, null, this);
    }

    @NonNull
    @Override
    public Loader<List<Album>> onCreateLoader(int id, Bundle args) {
        return new AlbumLoader.AlbumAsyncTaskLoader(getContext());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Album>> loader, List<Album> data) {
        mAlbumGridAdapter.updateData(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Album>> loader) {
    }

    @Override
    public void onMediaStoreRefreshed() {
        getLoaderManager().restartLoader(PagerLoaderIDs.ALBUMS_FRAGMENT, null, this);
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.pager_grid_fragment;
    }
}
