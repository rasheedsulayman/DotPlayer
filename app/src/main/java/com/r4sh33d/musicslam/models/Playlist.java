package com.r4sh33d.musicslam.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Playlist implements Parcelable {
    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };
    public final long id;
    public final String name;
    public final int songCount;
    public long dateAdded;
    public long dateModifed;

    public Playlist(long id, String name, int songCount) {
        this.id = id;
        this.name = name;
        this.songCount = songCount;
    }

    protected Playlist(Parcel in) {
        id = in.readLong();
        name = in.readString();
        songCount = in.readInt();
        dateAdded = in.readLong();
        dateModifed = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeInt(songCount);
        dest.writeLong(dateAdded);
        dest.writeLong(dateModifed);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}