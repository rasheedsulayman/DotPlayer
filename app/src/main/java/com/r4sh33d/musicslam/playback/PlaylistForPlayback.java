package com.r4sh33d.musicslam.playback;

import com.r4sh33d.musicslam.models.Song;

import java.util.List;

public class PlaylistForPlayback {
    public List<Song> list;
    public int position; // Specifies  position to start
    public int action; //Used for transmitting action to take for enqueueing playlist, Play Next of Last
    public boolean forceShuffle;

    public PlaylistForPlayback(List<Song> list, int position, int action, boolean forceShuffle) {
        this.list = list;
        this.position = position;
        this.action = action;
        this.forceShuffle = forceShuffle;
    }
}
