package com.r4sh33d.musicslam.fragments.nowplaying;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.r4sh33d.musicslam.GlideApp;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.customglide.audiocover.AudioCoverImage;
import com.r4sh33d.musicslam.customviews.SquareImageView;
import com.r4sh33d.musicslam.models.Song;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class ArtworkPagerAdapter extends FragmentStatePagerAdapter {
    private List<Song> nowPlayingQueue;

    public ArtworkPagerAdapter(FragmentManager fm, List<Song> nowPlayingQueue) {
        super(fm);
        this.nowPlayingQueue = nowPlayingQueue;
    }

    public void updateData(List<Song> nowPlayingQueue) {
        this.nowPlayingQueue = nowPlayingQueue;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        return ArtworkFragment.newInstance(nowPlayingQueue.get(position));
    }

    @Override
    public int getCount() {
        return nowPlayingQueue.size();
    }


    public static class ArtworkFragment extends Fragment {
        private static final String KEY_SONG = "song";

        @BindView(R.id.album_art)
        SquareImageView albumArtImageView;

        public ArtworkFragment() {
            // Required empty public constructor
        }

        public static ArtworkFragment newInstance(Song song) {
            Bundle args = new Bundle();
            args.putParcelable(KEY_SONG, song);
            ArtworkFragment fragment = new ArtworkFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_artwork, container, false);
            ButterKnife.bind(this, view);
            return view;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            albumArtImageView.setClipToOutline(true);
            Song song = getArguments().getParcelable(KEY_SONG);
            Timber.d("ArtworkFragement created for: " + song.title);
            loadAlbumArt(song);
        }

        public void loadAlbumArt(Song currentSong) {
            GlideApp.with(getContext().getApplicationContext())
                    .load(new AudioCoverImage(currentSong.data))
                    .placeholder(getContext().getDrawable(R.drawable.default_artwork_dark))
                    .into(albumArtImageView);
        }
    }
}
