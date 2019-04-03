package com.r4sh33d.musicslam.fragments.playqueue;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.customviews.ColoredStatusBarView;
import com.r4sh33d.musicslam.fragments.BaseListenerFragment;
import com.r4sh33d.musicslam.playback.MusicPlayer;

import butterknife.BindView;
import butterknife.ButterKnife;


public class PlayQueueFragment extends BaseListenerFragment implements SongsInPlayQueueAdapter.OnStartDragListener {

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    SongsInPlayQueueAdapter songsInPlayQueueAdapter;
    @BindView(R.id.status_bar_view)
    ColoredStatusBarView statusBarView;

    ItemTouchHelper itemTouchHelper;

    public PlayQueueFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play_queue, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isAlbumArtTheme) {
            statusBarView.setVisibility(View.GONE);
        }

        setUpToolBar();

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        songsInPlayQueueAdapter = new SongsInPlayQueueAdapter(MusicPlayer.getNowPlayingQueue(), getActivity(),
                this);
        recyclerView.scrollToPosition(MusicPlayer.getQueuePosition());
        recyclerView.setAdapter(songsInPlayQueueAdapter);

        ItemViewTouchHelperCallback callback = new ItemViewTouchHelperCallback(songsInPlayQueueAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }


    void setUpToolBar(){
        toolbar.setTitle("Play Queue");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_white_24dp);
        toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
    }

    @Override
    public void onQueueChanged() {
        songsInPlayQueueAdapter.updateData(MusicPlayer.getNowPlayingQueue());
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }
}
