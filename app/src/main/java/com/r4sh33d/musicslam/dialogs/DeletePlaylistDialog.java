package com.r4sh33d.musicslam.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.utils.PlayListHelper;
import com.r4sh33d.musicslam.models.Playlist;

public class DeletePlaylistDialog extends DialogFragment {
    public static final String PLAYLIST_ARGS = "playlist_arg";
    public static final String DELETE_PLAYLIST_FRAG_TAG = "delete_playlist_dialog_tag";

    public static DeletePlaylistDialog newInstance(Playlist playlist) {
        Bundle args = new Bundle();
        args.putParcelable(PLAYLIST_ARGS, playlist);
        DeletePlaylistDialog fragment = new DeletePlaylistDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Playlist playlist = getArguments().getParcelable(PLAYLIST_ARGS);
        return new MaterialDialog.Builder(getContext())
                .title(String.format("%s %s", getString(R.string.delete), playlist.name))
                .content(R.string.this_can_not_be_undone)
                .positiveText(R.string.delete)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) -> PlayListHelper.deletePlayList(playlist.id, getContext()))
                .onNegative((dialog, which) -> {
                    //Nothing
                })
                .build();
    }
}
