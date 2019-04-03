package com.r4sh33d.musicslam.customglide.artist;

import io.reactivex.annotations.NonNull;

public class ArtistImage {
    String artistName;

    public ArtistImage(@NonNull String artistName) {
        this.artistName = artistName;
    }

    @Override
    public String toString() {
        return artistName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtistImage artistImage = (ArtistImage) o;
        return artistImage.toString().equals(toString());
    }

    @Override
    public int hashCode() {
        return artistName.hashCode();
    }
}
