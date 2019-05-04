package com.r4sh33d.musicslam.customglide.artist;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.r4sh33d.musicslam.network.LastFmService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.r4sh33d.musicslam.network.retrofitmodels.ArtistResponse.ArtistInfoContainer;

public class ArtistImageUtil {

    public static void fetchArtistImageUrl(LastFmService lastFmService,
                                           String artistName,
                                           OnArtistImageUrlReadyListener listener) {
        lastFmService.getArtistInfo(LastFmService.ARTIST_METHOD, artistName).enqueue(new Callback<ArtistInfoContainer>() {
            @Override
            public void onResponse(@NonNull Call<ArtistInfoContainer> call, @NonNull Response<ArtistInfoContainer> response) {
                ArtistInfoContainer container = response.body();
                if (container != null && container.artistInfo != null) {
                    listener.onArtistImageReady(container.artistInfo.getLargestImageUrl());
                } else {
                    listener.onArtistImageReady(null);
                }
            }

            @Override
            public void onFailure(Call<ArtistInfoContainer> call, Throwable t) {
                listener.onArtistImageReady(null);
            }
        });
    }

    public static String getFirstWord(String text) {
        int index = text.indexOf(' ');
        if (index > -1) {
            return text.substring(0, index);
        } else {
            return text;
        }
    }

    interface OnArtistImageUrlReadyListener {
        void onArtistImageReady(@Nullable String url);
    }
}
