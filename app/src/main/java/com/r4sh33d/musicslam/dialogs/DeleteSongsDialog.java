package com.r4sh33d.musicslam.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.utils.MusicUtils;

import java.util.Locale;

public class DeleteSongsDialog extends DialogFragment {
    private static final String ARG_SONG_IDS = "song_ids";
    private static final String ARG_DIALOG_TITILE = "song_title";
    public static final String DELETE_FRAG_TAG = "delete_dialog_frag";

    public static DeleteSongsDialog newInstance(final long[] items, String title) {
        Bundle args = new Bundle();
        args.putLongArray(ARG_SONG_IDS, items);
        args.putString(ARG_DIALOG_TITILE, title);
        DeleteSongsDialog fragment = new DeleteSongsDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        long[] songIds = getArguments().getLongArray(ARG_SONG_IDS);
        int length = songIds.length;
        String title = getArguments().getString(ARG_DIALOG_TITILE, "");
        String content = getResources().getQuantityString(R.plurals.delete_songs_warning, length, length);
        if (songIds.length > 1) {
            title = String.format(Locale.getDefault(),getString(R.string.delete_songs_format), songIds.length);
        } else {
            title = String.format(getString(R.string.delete_question_mark_format), title);
        }
        return new MaterialDialog.Builder(getContext())
                .title(title)
                .content(content)
                .positiveText(R.string.delete)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) -> {
                    MusicUtils.deleteTracks(getActivity(), songIds);
                    dialog.dismiss();
                })
                .onNegative((dialog, which) -> {
                    dialog.dismiss();
                })
                .build();
    }

}
