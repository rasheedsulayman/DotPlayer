package com.r4sh33d.musicslam.fragments.playlist;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.dataloaders.PagerLoaderIDs;
import com.r4sh33d.musicslam.dataloaders.PlaylistLoader;
import com.r4sh33d.musicslam.dialogs.NewPlaylistDialog;
import com.r4sh33d.musicslam.fragments.pager.PagerFragment;
import com.r4sh33d.musicslam.models.Playlist;

import java.util.Collections;
import java.util.List;

/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class PlaylistsFragment extends PagerFragment implements LoaderManager.LoaderCallbacks<List<Playlist>> {

    private PlaylistAdapter mPlayListAdapter;

    public PlaylistsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlayListAdapter = new PlaylistAdapter(getActivity(), Collections.emptyList());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = getRecyclerView();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mPlayListAdapter);
    }

    @Override
    public String getEmptyDataMessage() {
        return "No Playlist";
    }

    @Override
    public RecyclerView.Adapter getAdapter() {
        return mPlayListAdapter;
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.pager_list_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        getLoaderManager().initLoader(PagerLoaderIDs.PLAYLIST_FRAGMENT, null, this);
    }

    @NonNull
    @Override
    public Loader<List<Playlist>> onCreateLoader(int id, Bundle args) {
        return new PlaylistLoader.PlayListAsynctaskLoader(getContext());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Playlist>> loader, List<Playlist> data) {
        mPlayListAdapter.updateData(data);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_new_playlist).setVisible(true).setEnabled(true);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_new_playlist:
                NewPlaylistDialog.newInstance(null).show(getActivity().getSupportFragmentManager(),
                        NewPlaylistDialog.NEW_PLAYLIST_FRAG_TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Playlist>> loader) {
    }

    @Override
    public void onMediaStoreRefreshed() {
        getLoaderManager().restartLoader(PagerLoaderIDs.PLAYLIST_FRAGMENT, null, this);
    }

    @Override
    public void onPlaylistChanged() {
        onMediaStoreRefreshed();
    }

}
