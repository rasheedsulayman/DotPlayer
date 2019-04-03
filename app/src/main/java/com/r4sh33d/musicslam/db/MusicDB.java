/*
 * Copyright (C) 2014 The CyanogenMod Project
 * Copyright (C) 2019 Rasheed Sulayman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.r4sh33d.musicslam.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MusicDB extends SQLiteOpenHelper {

    /* Version constant to increment when the database should be rebuilt */
    private static final int VERSION = 1;

    /* Name of database file */
    public static final String DATABASENAME = "musicdb.db";

    private static MusicDB sInstance = null;

    private final Context mContext;

    /**
     * @param context The {@link Context} to use
     * @return A new instance of this class.
     */
    public static final synchronized MusicDB getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new MusicDB(context.getApplicationContext());
        }
        return sInstance;
    }

    public MusicDB(final Context context) {
        super(context, DATABASENAME, null, VERSION);

        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        RecentStore.getInstance(mContext).onCreate(db);
        SongPlayCount.getInstance(mContext).onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        RecentStore.getInstance(mContext).onUpgrade(db, oldVersion, newVersion);
        SongPlayCount.getInstance(mContext).onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MusicDB.class.getSimpleName(),
                "Downgrading from: " + oldVersion + " to " + newVersion + ". Dropping tables");
        RecentStore.getInstance(mContext).onDowngrade(db, oldVersion, newVersion);
        SongPlayCount.getInstance(mContext).onDowngrade(db, oldVersion, newVersion);
    }
}
