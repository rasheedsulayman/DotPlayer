package com.r4sh33d.musicslam.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.InputType;

import com.afollestad.materialdialogs.MaterialDialog;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.models.Playlist;
import com.r4sh33d.musicslam.utils.PlayListHelper;
/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class RenamePlaylistDialog extends DialogFragment {
    public final static String RENAME_PLAYLIST_FRAG_TAG = "RENAME_PLAYLIST";

    private static final String ARG_PLAYLIST = "playlist";

    public static RenamePlaylistDialog newInstance(Playlist playlist) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_PLAYLIST, playlist);
        RenamePlaylistDialog fragment = new RenamePlaylistDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Playlist playlist = getArguments().getParcelable(ARG_PLAYLIST);
        return new MaterialDialog.Builder(getContext())
                .title(R.string.rename_playlist)
                .negativeText(R.string.cancel)
                .positiveText(R.string.save)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(playlist.name, playlist.name, false, (dialog, input) -> {
                    PlayListHelper.renamePlaylist(input.toString(), playlist.id, getContext());
                }).build();
    }

}
