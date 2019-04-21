package com.r4sh33d.musicslam.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Genres implements Parcelable {

    public static final Creator<Genres> CREATOR = new Creator<Genres>() {
        @Override
        public Genres createFromParcel(Parcel in) {
            return new Genres(in);
        }

        @Override
        public Genres[] newArray(int size) {
            return new Genres[size];
        }
    };
    public String name;
    public long id;
    public int songCount;

    public Genres(String name, long id, int songCount) {
        this.name = name;
        this.id = id;
        this.songCount = songCount;
    }

    protected Genres(Parcel in) {
        name = in.readString();
        id = in.readLong();
        songCount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeLong(id);
        dest.writeInt(songCount);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
