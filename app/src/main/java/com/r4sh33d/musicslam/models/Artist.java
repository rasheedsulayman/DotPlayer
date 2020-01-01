package com.r4sh33d.musicslam.models;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class Artist implements Parcelable {

    public static final Creator<Artist> CREATOR = new Creator<Artist>() {
        @Override
        public Artist createFromParcel(Parcel in) {
            return new Artist(in);
        }

        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };
    public int albumCount;
    public long id;
    public String name;
    public int songCount;


    public Artist(int albumCount, long id, String name, int songCount) {
        this.albumCount = albumCount;
        this.id = id;
        this.name = name;
        this.songCount = songCount;
    }

    public Artist() {

    }

    protected Artist(Parcel in) {
        albumCount = in.readInt();
        id = in.readLong();
        name = in.readString();
        songCount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(albumCount);
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeInt(songCount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "Artist{" +
                "albumCount=" + albumCount +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", songCount=" + songCount +
                '}';
    }
}

