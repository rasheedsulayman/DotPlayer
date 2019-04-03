package com.r4sh33d.musicslam.sleeptimer;

import com.r4sh33d.musicslam.playback.MusicService;

import java.util.ArrayList;

public class SleepTimer {

    private static final long COUNTDOWN_INTERVAL =  1000;
    private ArrayList<SleepTimerListener> listeners = new ArrayList<>();
    private MusicService service;
    private long currentTimeUntilFinished = 0;

    public SleepTimer(MusicService service) {
        this.service = service;
    }

    public void startTimer(int countDownDurationMillis) {
        countDownTimer.cancel();
        countDownTimer.setMillisInFuture(countDownDurationMillis);
        countDownTimer.start();
    }

    public void subscribeForSleepTimerUpdates(SleepTimerListener listener) {
        if (listener != null) {
            listeners.add(listener);
            listener.onTick(currentTimeUntilFinished);
        }
    }

    public void unSubscribeFromSleepTimerUpdates(SleepTimerListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public void notifyTimeUpdate() {
        for (SleepTimerListener listener : listeners) {
            listener.onTick(currentTimeUntilFinished);
        }
    }


    public void stopTimer() {
        countDownTimer.cancel();
        currentTimeUntilFinished = 0;
        notifyTimeUpdate();
    }

    public void tearDown() {
        listeners.clear();
    }

    private CountDownTimer countDownTimer = new CountDownTimer(COUNTDOWN_INTERVAL) {

        @Override
        public void onTick(long millisUntilFinished) {
            currentTimeUntilFinished = millisUntilFinished;
            notifyTimeUpdate();
        }

        @Override
        public void onFinish() {
            service.asyncPause();
            currentTimeUntilFinished = 0;
            notifyTimeUpdate();
        }
    };

    public interface SleepTimerListener {

        void onTick(long millisUntilFinished);
    }
}