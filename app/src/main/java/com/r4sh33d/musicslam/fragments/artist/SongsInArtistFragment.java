package com.r4sh33d.musicslam.fragments.artist;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.dataloaders.SongLoader;
import com.r4sh33d.musicslam.fragments.BaseListenerFragment;
import com.r4sh33d.musicslam.models.Artist;
import com.r4sh33d.musicslam.models.Song;

import java.util.ArrayList;
import java.util.List;
/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class SongsInArtistFragment extends BaseListenerFragment implements
        LoaderManager.LoaderCallbacks<List<Song>> {

    private static final String ARG_ARTIST = "artist";
    Artist mArtist;
    RecyclerView recyclerView;
    private SongsInArtistAdapter mSongsInArtistAdapter;

    public SongsInArtistFragment() {
        // Required empty public constructor
    }


    public static SongsInArtistFragment newInstance(Artist artist) {
        SongsInArtistFragment fragment = new SongsInArtistFragment();
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
        View view = inflater.inflate(R.layout.fragment_songs_in_artist, container, false);
        recyclerView = view.findViewById(R.id.recyclerview);
        mArtist = getArguments().getParcelable(ARG_ARTIST);
        setUpPageDetails();
        return view;
    }

    private void setUpPageDetails() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSongsInArtistAdapter = new SongsInArtistAdapter(new ArrayList<>(), getActivity());
        recyclerView.setAdapter(mSongsInArtistAdapter);
    }

    @Override
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        return new SongLoader.SongsAsyncTaskLoader(getContext(),
                SongLoader.ARTIST_SELECTION,
                new String[]{String.valueOf(mArtist.id)},
                null);
    }

    @Override
    public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
        mSongsInArtistAdapter.updateData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {
    }

    @Override
    public void onMediaStoreRefreshed() {
        getLoaderManager().restartLoader(0, null, this);
    }
}
