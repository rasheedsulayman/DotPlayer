package com.r4sh33d.musicslam.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.r4sh33d.musicslam.utils.PlayListHelper;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.dataloaders.PlaylistLoader;
import com.r4sh33d.musicslam.models.Playlist;

import java.util.ArrayList;

public class AddToPlaylistDialog extends DialogFragment {
    public static final String ADD_TO_PLAYLIST_ARG = "ADD_TO_PLAY_LIST";
    private static final String ARG_SONG_IDS = "song_ids";

    public static AddToPlaylistDialog newInstance(long[] songIds) {
        Bundle args = new Bundle();
        AddToPlaylistDialog fragment = new AddToPlaylistDialog();
        args.putLongArray(ARG_SONG_IDS, songIds);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        long[] songIds = getArguments().getLongArray(ARG_SONG_IDS);
        ArrayList<Playlist> playlists = PlaylistLoader.loadPlayLists(
                PlaylistLoader.makePlaylistCursor(getContext(), null, null),
                getContext(), false);

        String[] playlistTitles = new String[playlists.size() + 1];
        playlistTitles[0] = getString(R.string.new_playlist);
        for (int i = 1; i < playlists.size(); i++) {
            playlistTitles[i] = playlists.get(i - 1).name;
        }
        return new MaterialDialog.Builder(getContext())
                .title(getString(R.string.add_to_playlist))
                .items(playlistTitles)
                .titleColor(Color.BLACK)
                .contentColor(getResources().getColor(R.color.grey_800))
                .itemsCallback((dialog, view, which, text) -> {
                    if (which == 0) {
                        NewPlaylistDialog.newInstance(songIds).show(getActivity().getSupportFragmentManager(),
                                NewPlaylistDialog.NEW_PLAYLIST_FRAG_TAG);
                    } else {
                        PlayListHelper.addToPlaylist(getContext(), songIds, playlists.get(which - 1).id);
                    }
                    dialog.dismiss();
                })
                .build();
    }
}
