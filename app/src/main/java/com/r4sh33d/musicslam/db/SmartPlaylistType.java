package com.r4sh33d.musicslam.db;

import com.r4sh33d.musicslam.R;

public enum SmartPlaylistType {
    LastAdded(-1, R.string.playlist_last_added),
    RecentlyPlayed(-2, R.string.playlist_recently_played),
    TopTracks(-3, R.string.playlist_top_tracks);

    public long mId;
    public int mTitleId;

    SmartPlaylistType(long id, int titleId) {
        mId = id;
        mTitleId = titleId;
    }

    public static SmartPlaylistType getTypeById(long id) {
        for (SmartPlaylistType type : SmartPlaylistType.values()) {
            if (type.mId == id) {
                return type;
            }
        }
        return null;
    }
}
