package com.r4sh33d.musicslam.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.models.Song;
import com.r4sh33d.musicslam.utils.MusicUtils;
import com.r4sh33d.musicslam.utils.PrefsUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class SongDetailsDialog extends DialogFragment {
    public static final String SONG_DETAILS_DIALOG = "SONG_DETAILS_DIALOG";
    private static final String SONG_KEY = "SONG_KEY";
    @BindView(R.id.title_textview)
    TextView titleTextView;
    @BindView(R.id.file_path_textview)
    TextView filePathTextView;
    @BindView(R.id.artist_textview)
    TextView artistTextView;
    @BindView(R.id.album_textview)
    TextView albumTextView;
    @BindView(R.id.length_textview)
    TextView durationTextView;
    @BindView(R.id.file_size_textview)
    TextView fileSizeTextView;
    @BindView(R.id.mime_type_textview)
    TextView mimeTypeTextView;
    @BindView(R.id.track_no_textview)
    TextView trackNoTextView;
    Song song;

    public static SongDetailsDialog newInstance(Song song) {
        Bundle args = new Bundle();
        args.putParcelable(SONG_KEY, song);
        SongDetailsDialog fragment = new SongDetailsDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        if (PrefsUtils.getInstance(getContext()).isAlbumArtTheme()) {
            builder = new AlertDialog
                    .Builder(new ContextThemeWrapper(getContext(), R.style.AlbumArtWhiteDialog));
        }
        View view = LayoutInflater.from(builder.getContext()).inflate(R.layout.layout_song_details_dialog, null);
        ButterKnife.bind(this, view);
        song = getArguments().getParcelable(SONG_KEY);
        populateFields();
        return builder
                .setTitle(R.string.song_details)
                .setView(view)
                .setPositiveButton(R.string.okay, (dialog, which) -> {
                    dismiss();
                })
                .create();
    }

    private void populateFields() {
        titleTextView.setText(song.title);
        filePathTextView.setText(song.data);
        artistTextView.setText(song.artistName);
        albumTextView.setText(song.albumName);
        durationTextView.setText(MusicUtils.makeShortTimeString(getContext(), song.duration / 1000));
        fileSizeTextView.setText(song.getSongSizeLabel());
        mimeTypeTextView.setText(song.mimeType);
        trackNoTextView.setText(String.valueOf(song.trackNumber));
    }
}
