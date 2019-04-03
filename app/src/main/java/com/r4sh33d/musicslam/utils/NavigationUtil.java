package com.r4sh33d.musicslam.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.audiofx.AudioEffect;
import android.os.Build;
import android.support.transition.Transition;
import android.support.transition.TransitionInflater;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.activities.MainActivity;
import com.r4sh33d.musicslam.playback.MusicPlayer;
import com.r4sh33d.musicslam.fragments.playqueue.PlayQueueFragment;
import com.r4sh33d.musicslam.fragments.album.AlbumDetailsFragment;
import com.r4sh33d.musicslam.fragments.artist.ArtistsDetailsFragment;
import com.r4sh33d.musicslam.fragments.genres.GenresDetailsFragment;
import com.r4sh33d.musicslam.fragments.playlist.PlaylistDetailsFragment;
import com.r4sh33d.musicslam.fragments.search.SearchFragment;
import com.r4sh33d.musicslam.models.Album;
import com.r4sh33d.musicslam.models.Artist;
import com.r4sh33d.musicslam.models.Genres;
import com.r4sh33d.musicslam.models.Playlist;

public class NavigationUtil {

    public static final int OPEN_EQUALIZER_REQUEST = 100;

    private static Intent createEffectsIntent() {
        final Intent effects = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
        effects.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, MusicPlayer.getAudioSessionId());
        return effects;
    }

    public static void openEqualizer(final Activity context, final int requestCode) {
        try {
            context.startActivityForResult(createEffectsIntent(), requestCode);
        } catch (final ActivityNotFoundException notFound) {
            Toast.makeText(context, "No Equalizer found on your phone",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static void moveToAlbum(Context context, Album album) {
        FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
        AlbumDetailsFragment albumDetailsFragment = AlbumDetailsFragment.newInstance(album);
        transaction.hide(((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.mainViewContainer));
        transaction.add(R.id.mainViewContainer, albumDetailsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void moveToAlbum(Context context, Album album, Pair<View, String> pair) {
        FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
        AlbumDetailsFragment albumDetailsFragment = AlbumDetailsFragment.newInstance(album);
        Transition moveTransition = TransitionInflater.from(context).inflateTransition(R.transition.change_image_trans);
        albumDetailsFragment.setSharedElementEnterTransition(moveTransition);
        albumDetailsFragment.setSharedElementReturnTransition(moveTransition);

        transaction.addSharedElement(pair.first, pair.second);
        transaction.setReorderingAllowed(true);
        albumDetailsFragment.getArguments().putString("transition", pair.second);
        transaction.hide(((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.mainViewContainer));
        transaction.replace(R.id.mainViewContainer, albumDetailsFragment);
        transaction.add(R.id.mainViewContainer, albumDetailsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public static void moveToArtist(Context context, Artist artist) {
        FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
        ArtistsDetailsFragment artistsDetailsFragment = ArtistsDetailsFragment.newInstance(artist);
        transaction.hide(((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.mainViewContainer));
        transaction.add(R.id.mainViewContainer, artistsDetailsFragment);
        transaction.addToBackStack(null).commit();
    }

    public static void moveToArtist(Context context, Artist artist, Pair<View, String> pair) {
        FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
        ArtistsDetailsFragment artistsDetailsFragment = ArtistsDetailsFragment.newInstance(artist);

        Transition moveTransition = TransitionInflater.from(context).inflateTransition(R.transition.transition_s);
        artistsDetailsFragment.setSharedElementEnterTransition(moveTransition);
        artistsDetailsFragment.setSharedElementReturnTransition(moveTransition);

        transaction.addSharedElement(pair.first, pair.second);
        transaction.setReorderingAllowed(true);
        artistsDetailsFragment.getArguments().putString("transition", pair.second);
        transaction.hide(((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.mainViewContainer));
        transaction.add(R.id.mainViewContainer, artistsDetailsFragment);
        transaction.addToBackStack(null).commit();
    }

    public static void moveToPlaylist(Playlist playlist, Context context) {
        FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
        PlaylistDetailsFragment playlistDetailsFragment = PlaylistDetailsFragment.newInstance(playlist);
        transaction.hide(((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.mainViewContainer));
        transaction.add(R.id.mainViewContainer, playlistDetailsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public static void moveToGenres(Genres genres, Context context) {
        FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
        GenresDetailsFragment genresDetailsFragment = GenresDetailsFragment.newInstance(genres);
        transaction.hide(((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.mainViewContainer));
        transaction.add(R.id.mainViewContainer, genresDetailsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public static void navigateTONowPlaying(Context context) {
        PlayQueueFragment fragment = new PlayQueueFragment();
        FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
        transaction.hide(((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.dragView));
        transaction.add(R.id.dragView, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public static void moveToSearchPage(Context context) {
        FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
        transaction.hide(((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.mainViewContainer));
        transaction.add(R.id.mainViewContainer, new SearchFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }


    public  static Intent getAppRestartIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }
}
