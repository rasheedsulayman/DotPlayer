package com.r4sh33d.musicslam.fragments.pager;


import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.Util;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.fragments.BaseListenerFragment;
import com.r4sh33d.musicslam.fragments.album.AlbumsFragment;
import com.r4sh33d.musicslam.fragments.artist.ArtistsFragment;
import com.r4sh33d.musicslam.fragments.genres.GenresFragment;
import com.r4sh33d.musicslam.fragments.playlist.PlaylistsFragment;
import com.r4sh33d.musicslam.fragments.songlist.SongsFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainViewFragment extends BaseListenerFragment {
    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    String tabTitles[] = new String[]{"SONGS", "ALBUMS", "ARTISTS", "GENRES", "PLAYLISTS"};

    public MainViewFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        if (isAlbumArtTheme) {
            view = inflater.inflate(R.layout.fragment_main_view, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_main_view_two, container, false);
            mActivity.colorStatusBar(Color.TRANSPARENT);
        }
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setToolbarAndViewPager();
        if (isAlbumArtTheme) {
            Util.setOverflowButtonColor(toolbar, Color.WHITE);
            setTablayoutIndicatorColor(getCurrentPaletteColor());
        }
    }

    public void setToolbarAndViewPager() {
        mActivity.setSupportActionBar(toolbar);
        mActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        PagerAdapter pagerAdapter = new PagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(pagerAdapter.getCount() - 1);
        viewPager.setCurrentItem(prefsUtils.getStartPage());
        mTabLayout.setupWithViewPager(viewPager);
    }

    @SuppressLint("CheckResult")
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!isAlbumArtTheme) {
            //noinspection ResultOfMethodCallIgnored
            Aesthetic.get()
                    .colorPrimary()
                    .take(1)
                    .subscribe(color -> Util.setTaskDescriptionColor(getActivity(), color));
        }
    }

    public void setTablayoutIndicatorColor(int color) {
        if (color != 0) {
            mTabLayout.setTabTextColors(Color.WHITE, color);
            mTabLayout.setSelectedTabIndicatorColor(color);
        }
    }

    @Override
    public void onPaletteReady(int color) {
        if (isAlbumArtTheme) {
            setTablayoutIndicatorColor(color);
        }
    }

    class PagerAdapter extends FragmentPagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new SongsFragment();
                case 1:
                    return new AlbumsFragment();
                case 2:
                    return new ArtistsFragment();
                case 3:
                    return new GenresFragment();
                case 4:
                    return new PlaylistsFragment();
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        prefsUtils.putLastViewPagerFragmentId(viewPager.getCurrentItem());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
