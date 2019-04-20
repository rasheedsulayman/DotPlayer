package com.r4sh33d.musicslam.fragments.nowplaying;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.fragments.BaseListenerFragment;
import com.r4sh33d.musicslam.playback.MusicPlayer;

import java.lang.reflect.Field;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class ArtworkPagerFragment extends BaseListenerFragment implements ViewPager.OnPageChangeListener {
    @BindView(R.id.viewpager)
    ViewPager viewPager;
    ArtworkPagerAdapter pagerAdapter;

    public ArtworkPagerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_album_art_pager, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pagerAdapter = new ArtworkPagerAdapter(getChildFragmentManager(), MusicPlayer.getNowPlayingQueue());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(this);
        viewPager.setCurrentItem(MusicPlayer.getQueuePosition());
        setUpScroller();
    }

    void setUpScroller(){
        try {
            Field mScroller;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(viewPager.getContext(), new DecelerateInterpolator());
            mScroller.set(viewPager, scroller);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ignored) {
        }
    }

    @Override
    public void onServiceConnected() {
        updatePlayQueue();
    }

    @Override
    public void onQueueChanged() {
        updatePlayQueue();
    }

    private void updatePlayQueue() {
        //Hack to prevent ArtworkFragment not updating
        //TODO Find more efficient way to deal with it, without setting empty adapter
        viewPager.setAdapter(new ArtworkPagerAdapter(getChildFragmentManager(), new ArrayList<>()));
        pagerAdapter = new ArtworkPagerAdapter(getChildFragmentManager(), MusicPlayer.getNowPlayingQueue());
        viewPager.setAdapter(pagerAdapter);
        pagerAdapter.notifyDataSetChanged();
        viewPager.setCurrentItem(MusicPlayer.getQueuePosition());
    }

    @Override
    public void onStop() {
        super.onStop();
        viewPager.setPageTransformer(true, null);
    }

    @Override
    public void onStart() {
        super.onStart();
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
    }

    @Override
    public void onMetaChanged() {
        viewPager.setCurrentItem(MusicPlayer.getQueuePosition());
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (MusicPlayer.getQueuePosition() != position) {
            MusicPlayer.playSongAt(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
