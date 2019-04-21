package com.r4sh33d.musicslam.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

public class Song implements Parcelable {

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
    public static Song EMPTY_SONG = new Song(-1, "", -1, "",
            -1, -1, "", -1, "", "", -1);
    public String data;
    public long albumId;
    public String albumName;
    public long artistId;
    public String artistName;
    public long duration;
    public long id;
    public String title;
    public int trackNumber;
    public String mimeType;
    public long fileSize;

    public Song(long albumId, String albumName, long artistId, String artistName, long duration,
                long id, String title, int trackNumber, String data, String mimeType, long fileSize) {
        this.albumId = albumId;
        this.albumName = albumName;
        this.artistId = artistId;
        this.artistName = artistName;
        this.duration = duration;
        this.id = id;
        this.title = title;
        this.trackNumber = trackNumber % 1000; //Converts CD1,CD2...
        this.data = data;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
    }

    protected Song(Parcel in) {
        data = in.readString();
        albumId = in.readLong();
        albumName = in.readString();
        artistId = in.readLong();
        artistName = in.readString();
        duration = in.readLong();
        id = in.readLong();
        title = in.readString();
        trackNumber = in.readInt();
        mimeType = in.readString();
        fileSize = in.readLong();
    }

    public Song() {
    }

    public String getSongSizeLabel() {
        double size = fileSize / (1024.0 * 1024.0);
        return String.format(Locale.getDefault(), "%.2f %s", size, "MB");
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(data);
        dest.writeLong(albumId);
        dest.writeString(albumName);
        dest.writeLong(artistId);
        dest.writeString(artistName);
        dest.writeLong(duration);
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeInt(trackNumber);
        dest.writeString(mimeType);
        dest.writeLong(fileSize);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getTrackNumberString() {
        return trackNumber > 0 ? String.valueOf(trackNumber) : "-";
    }

    @Override
    public String toString() {
        return "Song{" +
                "data='" + data + '\'' +
                ", albumId=" + albumId +
                ", albumName='" + albumName + '\'' +
                ", artistId=" + artistId +
                ", artistName='" + artistName + '\'' +
                ", duration=" + duration +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", trackNumber=" + trackNumber +
                ", mimeType='" + mimeType + '\'' +
                ", fileSize=" + fileSize +
                '}';
    }
}
