package com.r4sh33d.musicslam.playback;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

import timber.log.Timber;

import static com.r4sh33d.musicslam.playback.Constants.SERVER_DIED;
import static com.r4sh33d.musicslam.playback.Constants.TRACK_ENDED;
import static com.r4sh33d.musicslam.playback.Constants.TRACK_WENT_TO_NEXT;

/**
 * @author Andrew Neal, Karim Abou Zeid (kabouzeid), Rasheed Sulayman (r4sh33d)
 */
class MultiPlayer implements MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private static final int ILLEGAL_STATE_ERROR_CODE = -38;

    private final WeakReference<MusicService> mService;

    private MediaPlayer mCurrentMediaPlayer = new MediaPlayer();

    private MediaPlayer mNextMediaPlayer;

    private Handler mHandler;

    private boolean mIsInitialized = false;

    private String mNextMediaPath;

    public MultiPlayer(final MusicService service) {
        mService = new WeakReference<>(service);
    }

    /**
     * @param path The path of the file, or the http/rtsp URL of the stream
     *             you want to play
     */
    public void setDataSource(final String path) {
        mIsInitialized = false;
        mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, path);
        if (mIsInitialized) {
            setNextDataSource(null);
        }
    }

    /**
     * @param player The {@link MediaPlayer} to use
     * @param path   The path of the file, or the http/rtsp URL of the stream
     *               you want to play
     * @return True if the <code>player</code> has been prepared and is
     * ready to play, false otherwise
     */
    private boolean setDataSourceImpl(final MediaPlayer player, final String path) {
        try {
            player.reset();
            player.setOnPreparedListener(null);
            if (path.startsWith("content://")) {
                player.setDataSource(mService.get(), Uri.parse(path));
            } else {
                player.setDataSource(path);
            }
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);

            player.prepare();
        } catch (Exception ignored) {
            return false;
        }
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        return true;
    }

    /**
     * Set the MediaPlayer to start when this MediaPlayer finishes playback.
     *
     * @param path The path of the file, or the http/rtsp URL of the stream
     *             you want to play
     */
    public void setNextDataSource(final String path) {
        mNextMediaPath = null;
        try {
            mCurrentMediaPlayer.setNextMediaPlayer(null);
        } catch (IllegalArgumentException e) {
            Timber.d("Next media player is current one, continuing");
        } catch (IllegalStateException e) {
            Timber.d("Media player not initialized!");
            return;
        }
        if (mNextMediaPlayer != null) {
            mNextMediaPlayer.release();
            mNextMediaPlayer = null;
        }
        if (path == null) {
            return;
        }
        mNextMediaPlayer = new MediaPlayer();
        mNextMediaPlayer.setAudioSessionId(getAudioSessionId());
        if (setDataSourceImpl(mNextMediaPlayer, path)) {
            mNextMediaPath = path;
            mCurrentMediaPlayer.setNextMediaPlayer(mNextMediaPlayer);
        } else {
            if (mNextMediaPlayer != null) {
                mNextMediaPlayer.release();
                mNextMediaPlayer = null;
            }
        }
    }

    /**
     * Sets the handler
     *
     * @param handler The handler to use
     */
    public void setHandler(final Handler handler) {
        mHandler = handler;
    }

    /**
     * @return True if the player is ready to go, false otherwise
     */
    public boolean isInitialized() {
        return mIsInitialized;
    }

    /**
     * Starts or resumes playback.
     */
    public void start() {
        mCurrentMediaPlayer.start();
    }


    public void stop() {
        mCurrentMediaPlayer.reset();
        mIsInitialized = false;
    }

    public void release() {
        mCurrentMediaPlayer.release();
    }

    /**
     * Pauses playback. Call start() to resume.
     */
    public void pause() {
        mCurrentMediaPlayer.pause();
    }

    /**
     * Gets the duration of the file.
     *
     * @return The duration in milliseconds
     */
    public long duration() {
        return mCurrentMediaPlayer.getDuration();
    }

    /**
     * Gets the current playback position.
     *
     * @return The current position in milliseconds
     */
    public long position() {
        try {
            return mCurrentMediaPlayer.getCurrentPosition();
        } catch (IllegalStateException ignored) {
            return -1;
        }
    }

    /**
     * Gets the current playback position.
     *
     * @param whereto The offset in milliseconds from the start to seek to
     * @return The offset in milliseconds from the start to seek to
     */
    public long seek(final long whereto) {
        mCurrentMediaPlayer.seekTo((int) whereto);
        return whereto;
    }

    /**
     * Sets the volume on this player.
     *
     * @param vol Left and right volume scalar
     */
    public void setVolume(final float vol) {
        try {
            mCurrentMediaPlayer.setVolume(vol, vol);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the audio session ID.
     *
     * @return The current audio session ID.
     */
    public int getAudioSessionId() {
        return mCurrentMediaPlayer.getAudioSessionId();
    }

    /**
     * Sets the audio session ID.
     *
     * @param sessionId The audio session ID
     */
    public void setAudioSessionId(final int sessionId) {
        mCurrentMediaPlayer.setAudioSessionId(sessionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onError(final MediaPlayer mp, final int what, final int extra) {
        Log.e("MediaPlayer", "Slam - Error what: " + what);
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                final MusicService service = mService.get();
                final TrackErrorInfo errorInfo = new TrackErrorInfo(service.getAudioId(),
                        service.getTrackName());
                mIsInitialized = false;
                mCurrentMediaPlayer.release();
                mCurrentMediaPlayer = new MediaPlayer();
                Message msg = mHandler.obtainMessage(SERVER_DIED, errorInfo);
                mHandler.sendMessageDelayed(msg, 2000);
                return true;
            case ILLEGAL_STATE_ERROR_CODE:
                //TODO this is a bad hack. We are probably calling a method on the media player in an inappropriate state.
                //TODO find the real cause of this error, instead of "faking" that we handled it.
                return true; //Prevents call to onFinished.
            default:
                break;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCompletion(final MediaPlayer mp) {
        if (mp == mCurrentMediaPlayer && mNextMediaPlayer != null) {
            mIsInitialized = false;
            mCurrentMediaPlayer.release();
            mCurrentMediaPlayer = mNextMediaPlayer;
            mIsInitialized = true;
            mNextMediaPath = null;
            mNextMediaPlayer = null;
            mHandler.sendEmptyMessage(TRACK_WENT_TO_NEXT);
        } else {
            mHandler.sendEmptyMessage(TRACK_ENDED);
        }
    }

    static final class TrackErrorInfo {
        public long mId;
        public String mTrackName;

        public TrackErrorInfo(long id, String trackName) {
            mId = id;
            mTrackName = trackName;
        }
    }
}
