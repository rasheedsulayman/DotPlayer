package com.r4sh33d.musicslam.fragments.artist;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.r4sh33d.musicslam.GlideApp;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.customglide.artist.ArtistImage;
import com.r4sh33d.musicslam.dataloaders.SongIdsLoader;
import com.r4sh33d.musicslam.dataloaders.SongLoader;
import com.r4sh33d.musicslam.dialogs.AddToPlaylistDialog;
import com.r4sh33d.musicslam.fragments.AbsParallaxArtworkDetailsFragment;
import com.r4sh33d.musicslam.models.Artist;
import com.r4sh33d.musicslam.models.Song;
import com.r4sh33d.musicslam.playback.MusicPlayer;
import com.r4sh33d.musicslam.utils.SlamUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class ArtistsDetailsFragment extends AbsParallaxArtworkDetailsFragment {

    private static final String ARG_ARTIST = "artist";
    @BindView(R.id.detail_tabs)
    TabLayout tabLayout;
    @BindView(R.id.backdrop)
    ImageView artistArt;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.appbar)
    AppBarLayout appBarLayout;
    @BindView(R.id.viewpager)
    ViewPager viewPager;
    Artist mArtist;
    String[] tabTitles;

    public ArtistsDetailsFragment() {
        // Required empty public constructor
    }

    public static ArtistsDetailsFragment newInstance(Artist artist) {
        ArtistsDetailsFragment fragment = new ArtistsDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ARTIST, artist);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mArtist = getArguments().getParcelable(ARG_ARTIST);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_artists_info, container, false);
        ButterKnife.bind(this, view);
        //loadArtistArt();
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabTitles = getResources().getStringArray(R.array.artist_details_tab_titles);
        PagerAdapter pagerAdapter = new PagerAdapter(getChildFragmentManager(), getContext());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);
        addCustomViewsToTabLayout(pagerAdapter);
    }

    @Nullable
    @Override
    public RecyclerView.Adapter getAdapter() {
        return null;
    }

    void fadeInViews() {
        fadeInView(upperBlackShade, 1000);
        fadeInView(lowerBlackShade, 1000);
    }

    //This is temporarily disabled because of the last fm API Artist image shutdown.
    //The layout is now temporarily changed to a fixed on (from a scrollable one used in album and playlist details screens)
    //TODO find and integrate another API to fetch artists arts
    public void loadArtistArt() {
        GlideApp.with(this).asBitmap()
                .load(new ArtistImage(mArtist.name))
                .transition(BitmapTransitionOptions.withCrossFade(150))
                .fitCenter()
                .into(new BitmapImageViewTarget(artistArt) {
                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        artistArt.setScaleType(ImageView.ScaleType.CENTER);
                        artistArt.setImageResource(R.drawable.ic_music_note_24dp);
                        fadeInViews();
                    }

                    @Override
                    public void onResourceReady(Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        super.onResourceReady(resource, transition);
                        fadeInViews();
                        onArtworkLoaded(resource);
                    }
                });
    }


    @Override
    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public ImageView getArtWorkImageView() {
        return artistArt;
    }

    @Override
    public AppBarLayout getAppbarLayout() {
        return appBarLayout;
    }

    @Override
    public String getToolbarTitle() {
        return mArtist.name;
    }

    private List<Song> getSongsForArtist() {
        return SongLoader.getSongsForArtist(mArtist.id, getContext());
    }


    @Override
    public void playShuffledSongs() {
        MusicPlayer.playShuffle(getSongsForArtist());
    }

    @Override
    public void playSongs() {
        MusicPlayer.playAll(
                getSongsForArtist(),
                0,
                false);
    }

    @Override
    public void playNext() {
        MusicPlayer.playNext(getSongsForArtist(), getContext());
    }

    @Override
    public void addToQueue() {
        MusicPlayer.addToQueue(getContext(),
                getSongsForArtist());

    }

    @Override
    public void addToPlaylist() {
        AddToPlaylistDialog.newInstance(SongIdsLoader.getSongIdsListForArtist(getContext(), mArtist.id))
                .show(getFragmentManager(), AddToPlaylistDialog.ADD_TO_PLAYLIST_ARG);
    }

    @Override
    public void onPaletteReady(int color) {
        tabLayout.setSelectedTabIndicatorColor(color);
    }

    private void addCustomViewsToTabLayout(PagerAdapter pagerAdapter) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(pagerAdapter.getTabView(i));
        }
    }

    class PagerAdapter extends FragmentPagerAdapter {
        Context context;

        PagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return SongsInArtistFragment.newInstance(mArtist);
                case 1:
                    return AlbumInArtistFragment.newInstance(mArtist);
                case 2:
                    return ArtistBioFragment.newInstance(mArtist.name);
            }
            return null;
        }

        public View getTabView(int position) {
            View v = LayoutInflater.from(context).inflate(R.layout.custom_tab, null);
            TextView tv = v.findViewById(R.id.textView);
            tv.setText(tabTitles[position]);
            return v;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }
    }
}
