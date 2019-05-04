package com.r4sh33d.musicslam.fragments.songlist;


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
import com.r4sh33d.musicslam.dataloaders.SongLoader;
import com.r4sh33d.musicslam.fragments.pager.PagerFragment;
import com.r4sh33d.musicslam.models.Song;
import com.r4sh33d.musicslam.playback.MusicPlayer;

import java.util.Collections;
import java.util.List;

public class SongsFragment extends PagerFragment implements LoaderManager.LoaderCallbacks<List<Song>> {

    private SongsListAdapter mSongListAdapter;

    public SongsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSongListAdapter = new SongsListAdapter(getContext(), Collections.emptyList());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = getRecyclerView();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mSongListAdapter);
    }

    @Override
    public String getEmptyDataMessage() {
        return getString(R.string.no_songs);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_shuffle_all).setVisible(true).setEnabled(true);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_shuffle_all:
                MusicPlayer.playShuffle(mSongListAdapter.getData());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public RecyclerView.Adapter getAdapter() {
        return mSongListAdapter;
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.pager_list_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        getLoaderManager().initLoader(PagerLoaderIDs.SONGS_FRAGMENT, null, this);
    }

    @NonNull
    @Override
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        return new SongLoader.SongsAsyncTaskLoader(getContext());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Song>> loader, List<Song> data) {
        mSongListAdapter.updateData(data);
    }

    @Override
    public void onMediaStoreRefreshed() {
        getLoaderManager().restartLoader(PagerLoaderIDs.SONGS_FRAGMENT, null, this);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Song>> loader) {
    }
}
