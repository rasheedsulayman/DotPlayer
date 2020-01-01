/*
 * Copyright (C) 2012 Andrew Neal
 * Copyright (C) 2014 The CyanogenMod Project
 * Copyright (C) 2019 Rasheed Sulayman
 *
 * Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
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
