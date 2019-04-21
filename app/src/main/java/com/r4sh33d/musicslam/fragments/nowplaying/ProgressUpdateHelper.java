package com.r4sh33d.musicslam.fragments.nowplaying;

import android.os.Handler;
import android.os.Message;

import com.r4sh33d.musicslam.playback.MusicPlayer;

import java.lang.ref.WeakReference;

public class ProgressUpdateHelper extends Handler {
    private static final int UPDATE_PROGRESS_COMMAND = 1;
    private static final long PAUSED_UPDATE_FREQUENCY = 500;
    private static final long PLAYING_UPDATE_FREQUENCY = 1000;
    private static final long MINIMUM_UPDATE_FREQUENCY = 20;

    private WeakReference<OnProgressUpdateListener> onProgressUpdateListener;

    public ProgressUpdateHelper(OnProgressUpdateListener onProgressUpdateListener) {
        this.onProgressUpdateListener = new WeakReference<>(onProgressUpdateListener);
    }

    @Override
    public void handleMessage(final Message msg) {
        switch (msg.what) {
            case UPDATE_PROGRESS_COMMAND:
                final long next = refreshCurrentTime();
                queueNextRefresh(next);
                break;
        }
    }

    private void queueNextRefresh(final long delay) {
        final Message message = obtainMessage(UPDATE_PROGRESS_COMMAND);
        removeMessages(UPDATE_PROGRESS_COMMAND);
        sendMessageDelayed(message, delay);
    }


    private long refreshCurrentTime() {
        long currentTime = MusicPlayer.position();
        long totalDuration = MusicPlayer.getCurrentSongDuration();
        OnProgressUpdateListener listener = onProgressUpdateListener.get();
        if (listener != null) {
            listener.onProgressUpdate((int) currentTime / 1000,
                    (int) totalDuration / 1000);
        }

        if (MusicPlayer.isPlaying()) {
            return Math.max(MINIMUM_UPDATE_FREQUENCY,
                    PLAYING_UPDATE_FREQUENCY - currentTime % PLAYING_UPDATE_FREQUENCY
                    /* number of milliseconds until the next full second*/
            );
        }
        return PAUSED_UPDATE_FREQUENCY;
    }

    public void startUpdates() {
        queueNextRefresh(1);
    }

    public void stopUpdates() {
        removeMessages(UPDATE_PROGRESS_COMMAND);
    }

    public interface OnProgressUpdateListener {
        void onProgressUpdate(int currentTimeInSecs, int totalDurationInSecs);
    }
}
