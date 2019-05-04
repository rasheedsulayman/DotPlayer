package com.r4sh33d.musicslam.fragments.artist;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.network.retrofitmodels.ArtistResponse;
import com.r4sh33d.musicslam.network.LastFmRetrofitClient;
import com.r4sh33d.musicslam.network.LastFmService;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.r4sh33d.musicslam.network.LastFmService.ARTIST_METHOD;

public class ArtistBioFragment extends Fragment {
    private static final String ARG_ARTIST_NAME = "artistName";
    @BindView(R.id.artist_bio)
    TextView artistBioTextView;
    @BindView(R.id.empty_data_textview)
    TextView emptyDataTextView;

    public ArtistBioFragment() {
        // Required empty public constructor
    }

    public static ArtistBioFragment newInstance(String artistName) {
        ArtistBioFragment fragment = new ArtistBioFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ARTIST_NAME, artistName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_artist_bio, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public void setEmptyState() {
        emptyDataTextView.setText(R.string.artist_bio_not_available);
        emptyDataTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String artistName = getArguments().getString(ARG_ARTIST_NAME, "");
        LastFmService lastFmService = LastFmRetrofitClient.getLastFmRetrofitService(getContext(), 5000);
        lastFmService.getArtistInfo(ARTIST_METHOD, artistName).enqueue(new Callback<ArtistResponse.ArtistInfoContainer>() {
            @Override
            public void onResponse(@NonNull Call<ArtistResponse.ArtistInfoContainer> call,
                                   @NonNull Response<ArtistResponse.ArtistInfoContainer> response) {
                if (artistBioTextView != null) {
                    if (response.isSuccessful() && response.body() != null &&
                            response.body().artistInfo != null) {
                        String content = response.body().artistInfo.bio.content;
                        if (!TextUtils.isEmpty(content)) {
                            artistBioTextView.setText(Html.fromHtml(content));
                        } else {
                            setEmptyState();
                        }
                    } else {
                        setEmptyState();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ArtistResponse.ArtistInfoContainer> call, @NonNull Throwable t) {
                if (artistBioTextView != null) {
                    setEmptyState();
                }
            }
        });
    }

}
