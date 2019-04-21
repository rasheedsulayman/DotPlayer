package com.r4sh33d.musicslam.customglide.audiocover;

import android.support.annotation.Nullable;
import android.text.TextUtils;

public class AudioCoverImage {
    String filePath;

    public AudioCoverImage(@Nullable String filePath) {
        this.filePath = !TextUtils.isEmpty(filePath) ? filePath : "";
    }

    @Override
    public String toString() {
        return filePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudioCoverImage audioCoverImage = (AudioCoverImage) o;
        return audioCoverImage.toString().equals(toString());
    }

    @Override
    public int hashCode() {
        //We are probably wasting lot of cache memory by verifying this with just filepath
        //TODO: - Verify with album ID. or album name
        return filePath.hashCode();
    }
}
