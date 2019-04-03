package com.r4sh33d.musicslam.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.r4sh33d.musicslam.utils.PlayListHelper;

public class NewPlaylistDialog extends DialogFragment {
    public final static String NEW_PLAYLIST_FRAG_TAG = "NEW_PLAY_LIST";

    private static final String ARG_SONG_IDS = "song_ids";

    public static NewPlaylistDialog newInstance(long[] songIds) {
        Bundle args = new Bundle();
        args.putLongArray(ARG_SONG_IDS, songIds);
        NewPlaylistDialog fragment = new NewPlaylistDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getContext())
                .title("New Playlist")
                .negativeText("Cancel")
                .positiveText("Save")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("Enter playlist name", "", false, (dialog, input) -> {
                    long[] sondIds = getArguments().getLongArray(ARG_SONG_IDS);
                    if (!TextUtils.isEmpty(input)) {
                        final long playlistId = PlayListHelper.getIdForPlaylist(getActivity(),
                                input.toString());
                        if (playlistId >= 0) {
                            //We have playlist
                            Toast.makeText(getContext(), "Playlist Exists", Toast.LENGTH_SHORT).show();
                        } else {
                            long newId = PlayListHelper.createPlaylist(getActivity(), input.toString());
                            if (newId == -1) {
                                Toast.makeText(getContext(), "Unable to Create Playlist", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Created playlist " + input, Toast.LENGTH_SHORT).show();
                                if (sondIds != null && sondIds.length > 0) {
                                    PlayListHelper.addToPlaylist(getActivity(), sondIds, newId);
                                }
                            }
                        }
                    }
                }).build();
    }
}
