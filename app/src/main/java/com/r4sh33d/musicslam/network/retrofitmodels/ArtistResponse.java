package com.r4sh33d.musicslam.network.retrofitmodels;

import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;

import static com.r4sh33d.musicslam.network.retrofitmodels.ArtistResponse.ArtistInfo.ImageSize.EXTRA_LARGE;
import static com.r4sh33d.musicslam.network.retrofitmodels.ArtistResponse.ArtistInfo.ImageSize.LARGE;
import static com.r4sh33d.musicslam.network.retrofitmodels.ArtistResponse.ArtistInfo.ImageSize.MEDIUM;
import static com.r4sh33d.musicslam.network.retrofitmodels.ArtistResponse.ArtistInfo.ImageSize.MEGA;
import static com.r4sh33d.musicslam.network.retrofitmodels.ArtistResponse.ArtistInfo.ImageSize.SMALL;

public class ArtistResponse {

    public static class ArtistInfoContainer {
        @SerializedName("artist")
        @Expose
        public ArtistInfo artistInfo;
    }

    public static class Bio {
        @SerializedName("summary")
        @Expose
        public String summary;
        @SerializedName("content")
        @Expose
        public String content;

    }

    public static class Image {

        @SerializedName("#text")
        @Expose
        public String text;
        @SerializedName("size")
        @Expose
        public String size;

        @Override
        public String toString() {
            return "Image{" +
                    "text='" + text + '\'' +
                    ", size='" + size + '\'' +
                    '}';
        }
    }


    public static class ArtistInfo {

        @SerializedName("name")
        @Expose
        public String name;
        @SerializedName("url")
        @Expose
        public String url;
        @SerializedName("image")
        @Expose
        public List<Image> images;

        @SerializedName("bio")
        @Expose
        public Bio bio;


        public @Nullable
        String getLargestImageUrl() {
            if (images != null) {
                HashMap<String, String> imagesSizeMap = new HashMap<>();
                for (Image image : images) {
                    imagesSizeMap.put(image.size, image.text);
                }
                if (imagesSizeMap.containsKey(MEGA)) {
                    return imagesSizeMap.get(MEGA);
                }
                if (imagesSizeMap.containsKey(EXTRA_LARGE)) {
                    return imagesSizeMap.get(EXTRA_LARGE);
                }
                if (imagesSizeMap.containsKey(LARGE)) {
                    return imagesSizeMap.get(LARGE);
                }
                if (imagesSizeMap.containsKey(MEDIUM)) {
                    return imagesSizeMap.get(MEDIUM);
                }
                if (imagesSizeMap.containsKey(SMALL)) {
                    return imagesSizeMap.get(SMALL);
                }
            }
            return null;
        }

        public interface ImageSize {
            String MEGA = "mega";
            String EXTRA_LARGE = "extralarge";
            String LARGE = "large";
            String MEDIUM = "medium";
            String SMALL = "small";
        }

    }
}
