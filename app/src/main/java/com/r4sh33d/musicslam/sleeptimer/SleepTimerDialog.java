package com.r4sh33d.musicslam.sleeptimer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.playback.MusicPlayer;
import com.r4sh33d.musicslam.utils.PrefsUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class SleepTimerDialog extends DialogFragment implements SleepTimer.SleepTimerListener {
    public static final String SLEEP_DIALOG_ARG = "ADD_TO_PLAY_LIST";
    @BindView(R.id.hours_editText)
    EditText hoursEditText;
    @BindView(R.id.minutes_editText)
    EditText minutesEditText;
    @BindView(R.id.seconds_editText)
    EditText secondsEditText;
    @BindView(R.id.reset_button)
    Button resetButton;
    @BindView(R.id.start_button)
    Button startButton;
    boolean isViewsActivated = true;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        if (PrefsUtils.getInstance(getContext()).isAlbumArtTheme()) {
            builder = new AlertDialog
                    .Builder(new ContextThemeWrapper(getContext(), R.style.AlbumArtWhiteDialog) );
        }
        View view = LayoutInflater.from(builder.getContext()).inflate(R.layout.layout_sleep_timer_dialog, null);
        ButterKnife.bind(this, view);
        if (MusicPlayer.getSleepTimer() != null) {
            MusicPlayer.getSleepTimer().subscribeForSleepTimerUpdates(this);
        }
        return builder.setView(view).create();
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if (millisUntilFinished == 0) {
            setViewStates(true);
        } else {
            setViewStates(false);
        }
        updateEdiText(millisUntilFinished / 1000);
    }

    @OnClick(R.id.reset_button)
    public void onRestButtonClicked() {
        SleepTimer sleepTimer = MusicPlayer.getSleepTimer();
        if (sleepTimer != null) {
            sleepTimer.stopTimer();
        }
    }

    @OnClick(R.id.start_button)
    public void onStartButtonClicked() {
        if (!isViewsActivated) {
            Toast.makeText(getContext(), R.string.reset_ongoing_timer, Toast.LENGTH_SHORT).show();
            return;
        }
        long hours = 0, minutes = 0, seconds = 0;
        try {
            hours = Long.parseLong(hoursEditText.getText().toString()) * 60 * 60;
            minutes = Long.parseLong(minutesEditText.getText().toString()) * 60;
            seconds = Long.parseLong(secondsEditText.getText().toString());
        } catch (NumberFormatException ignored) {
        }
        long totalDurationMillis = (hours + minutes + seconds) * 1000;
        if (totalDurationMillis <= 0) {
            Toast.makeText(getContext(), R.string.set_a_valid_duration, Toast.LENGTH_SHORT).show();
            return;
        }
        if (MusicPlayer.getSleepTimer() != null) {
            MusicPlayer.getSleepTimer().startTimer((int) totalDurationMillis);
            Toast.makeText(getContext(), R.string.sleep_timer_started, Toast.LENGTH_SHORT).show();
            getDialog().dismiss();
        }
    }

    private void setViewStates(boolean isActive) {
        if (isActive == isViewsActivated) {
            return;
        }
        isViewsActivated = isActive;
        hoursEditText.setEnabled(isActive);
        minutesEditText.setEnabled(isActive);
        secondsEditText.setEnabled(isActive);
    }

    private void updateEdiText(long seconds) {
        long hours, minutes;
        hours = seconds / 3600;
        seconds %= 3600;
        minutes = seconds / 60;
        seconds %= 60;
        hoursEditText.setText(String.valueOf(hours));
        minutesEditText.setText(String.valueOf(minutes));
        secondsEditText.setText(String.valueOf(seconds));
    }

    @Override
    public void onDestroy() {
        if (MusicPlayer.getSleepTimer() != null) {
            MusicPlayer.getSleepTimer().unSubscribeFromSleepTimerUpdates(this);
        }
        super.onDestroy();
    }
}
