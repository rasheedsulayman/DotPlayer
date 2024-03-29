package com.r4sh33d.musicslam.models;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class Album implements Parcelable {
    //public final long artistId;
    public String artistName;
    public long id;
    public int songCount;
    public String title;
    public int year;
    public Song firstSong = Song.EMPTY_SONG;


    public Album() {
    }

    public Album(String artistName, long id, int songCount, String title, int year) {
        this.artistName = artistName;
        this.id = id;
        this.songCount = songCount;
        this.title = title;
        this.year = year;
    }

    protected Album(Parcel in) {
        artistName = in.readString();
        id = in.readLong();
        songCount = in.readInt();
        title = in.readString();
        year = in.readInt();
        firstSong = in.readParcelable(Song.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artistName);
        dest.writeLong(id);
        dest.writeInt(songCount);
        dest.writeString(title);
        dest.writeInt(year);
        dest.writeParcelable(firstSong, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
}
