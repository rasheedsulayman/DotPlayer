package com.r4sh33d.musicslam.playback;

/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public interface Constants {

    String SCHEME_CONTENT = "content";
    String SCHEME_FILE = "file";
    String AUTHORITY_MEDIA = "media";

    String MUSIC_SLAM_PACKAGE_NAME = "com.r4sh33d.musicslam";
    String MUSIC_PACKAGE_NAME = "com.android.music";

    //Intent actions
    String SHUTDOWN_ACTION = MUSIC_SLAM_PACKAGE_NAME + ".shutdown";
    String TOGGLEPAUSE_ACTION = MUSIC_SLAM_PACKAGE_NAME + ".togglepause";
    String PAUSE_ACTION = MUSIC_SLAM_PACKAGE_NAME + ".pause";
    String STOP_ACTION = MUSIC_SLAM_PACKAGE_NAME + ".stop";
    String PREVIOUS_ACTION = MUSIC_SLAM_PACKAGE_NAME + ".previous";
    String PREVIOUS_FORCE_ACTION = MUSIC_SLAM_PACKAGE_NAME + ".force_previous";
    String NEXT_ACTION = MUSIC_SLAM_PACKAGE_NAME + ".next";
    String REPEAT_ACTION = MUSIC_SLAM_PACKAGE_NAME + ".repeat";
    String SHUFFLE_ACTION = MUSIC_SLAM_PACKAGE_NAME + ".shuffle";

    //Playback activities
    String PLAY_STATE_CHANGED = MUSIC_SLAM_PACKAGE_NAME + ".playstatechanged";
    String POSITION_CHANGED = MUSIC_SLAM_PACKAGE_NAME + ".positionchanged";
    String META_CHANGED = MUSIC_SLAM_PACKAGE_NAME + ".metachanged";
    String QUEUE_CHANGED = MUSIC_SLAM_PACKAGE_NAME + ".queuechanged";
    String PLAYLIST_CHANGED = MUSIC_SLAM_PACKAGE_NAME + ".playlistchanged";
    String REPEAT_MODE_CHANGED = MUSIC_SLAM_PACKAGE_NAME + ".repeatmodechanged";
    String SHUFFLE_MODE_CHANGED = MUSIC_SLAM_PACKAGE_NAME + ".shufflemodechanged";
    String REFRESH = MUSIC_SLAM_PACKAGE_NAME + ".refresh";
    String TRACK_ERROR = MUSIC_SLAM_PACKAGE_NAME + ".trackerror";
    String EXTRA_TRACK_NAME = "trackname";

    //App widgets
    String EXTRA_WIDGET_TYPE = MUSIC_SLAM_PACKAGE_NAME + ".extra_app_widget_type";
    String ACTION_UPDATE_APP_WIDGETS = MUSIC_SLAM_PACKAGE_NAME + ".updateappwidget";

    // Music player handler messages
    int TRACK_ENDED = 1;
    int TRACK_WENT_TO_NEXT = 2;
    int SERVER_DIED = 3;
    int FOCUSCHANGE = 4;
    int FADEDOWN = 5;
    int FADEUP = 6;

    int NEXT = 1;
    int LAST = 2;

    //Shuffle modes
    int SHUFFLE_NONE = 0;
    int SHUFFLE_NORMAL = 1;
    int SHUFFLE_AUTO = 2;

    //repeat modes
    int REPEAT_NONE = 0;
    int REPEAT_CURRENT = 1;
    int REPEAT_ALL = 2;


    // Constants for asynchronous actions
    int GOTO_NEXT_ASYNC = 20;
    int GOTO_PREVIOUS_ASYNC = 21;
    int PLAY_ASYNC = 22;
    int PAUSE_ASYNC = 23;
    int SET_REPEAT_MODE_ASYNC = 24;
    int SET_SHUFFLE_MODE_ASYNC = 25;
    int STOP_ASYNC = 26;
    int OPEN_FILE_ASYNC = 27;
    int OPEN_ASYNC = 28;
    int ENQUEUE_ASYNC = 29;
    int MOVE_QUEUE_ITEM_ASYNC = 30;
    int REFRESH_ASYNC = 31;
    int PLAYLIST_CHANGED_ASYNC = 32;
    int SEEK_ASYNC = 33;
    int SEEK_RELATIVE_ASYNC = 34;
    int PLAY_SONG_AT_ASYNC = 35;
    int TOGGLE_PLAY_PAUSE_ASYNC = 36;
    int PLAY_SONG_FROM_URI_ASYNC = 37;
    int CYCLE_SHUFFLE_ASYNC = 38;
    int CYCLE_REPEAT_ASYNC = 39;
    int SET_SHUFFLE_MODE_LIGHT_ASYNC = 40;
    int PREPARE_AND_PLAY_ASYNC = 41;
    int SAVE_QUEUES = 42;
    int SET_NEXT_TRACK = 45;
}

