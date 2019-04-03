package com.r4sh33d.musicslam.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.Util;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.activities.SettingsActivity;
import com.r4sh33d.musicslam.utils.ColorHelper;
import com.r4sh33d.musicslam.utils.SlamUtils;

import butterknife.BindView;
import timber.log.Timber;


public abstract class AbsParallaxArtworkDetailsFragment extends BaseListenerFragment
        implements Toolbar.OnMenuItemClickListener {

    boolean isExpanded = true;
    protected int currentColor;

    @BindView(R.id.upper_black_shade)
    public FrameLayout upperBlackShade;
    @BindView(R.id.lower_black_shade)
    public FrameLayout lowerBlackShade;
    @BindView(R.id.parallax_color_view)
    public FrameLayout parallaxColorView;
    @Nullable
    @BindView(R.id.empty_data_textview)
    TextView emptyDataTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!isAlbumArtTheme) {
            getToolbar().setPadding(0, SlamUtils.dpToPx(24, getContext()), 0, 0);
            currentColor = Aesthetic.get().colorPrimary().blockingFirst();
            getAppbarLayout().setBackgroundColor(currentColor);
            mActivity.colorStatusBar(Color.TRANSPARENT);
        }
        if (prefsUtils.isDarkTheme()) {
            getToolbar().setPopupTheme(R.style.BlackOverflowButtonStyle);
        }
        setUpPageHeader();
        addAdapterDataObserver();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void setUpPageHeader() {
        Toolbar toolbar = getToolbar();
        Util.setOverflowButtonColor(toolbar, Color.WHITE);
        toolbar.setNavigationIcon(R.drawable.left_arrow);
        toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        toolbar.inflateMenu(R.menu.menu_parrallax_toolbar_info_info);
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setTitle(getToolbarTitle());
        AppBarLayout barLayout = getAppbarLayout();
        parallaxColorView.setOnClickListener(v -> barLayout.setExpanded(!isExpanded, true));
        barLayout.addOnOffsetChangedListener((AppBarLayout appBarLayout, int verticalOffset) -> {
            int appBarLayoutTotalScrollRange = appBarLayout.getTotalScrollRange();
            isExpanded = Math.abs(verticalOffset) - appBarLayoutTotalScrollRange != 0;
            if (!isAlbumArtTheme) {
                float alpha = Math.abs(verticalOffset) / (float) appBarLayoutTotalScrollRange;
                parallaxColorView.setBackgroundColor(ColorHelper.getColorWithAlpha(alpha, currentColor));
                animateTextGuardViewsWithAlpha(isExpanded ? 1 : 0);
            }
        });
    }

    void animateTextGuardViewsWithAlpha(float alpha) {
        upperBlackShade.animate().alpha(alpha).setDuration(1000).start();
        lowerBlackShade.animate().alpha(alpha).setDuration(1000).start();
    }

    public void fadeInView(View view, long millis) {
        view.setAlpha(0f);
        view.animate().alpha(1).setDuration(millis)
                .setInterpolator(new DecelerateInterpolator()).start();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_shuffle:
                playShuffledSongs();
                return true;
            case R.id.menu_album_play:
                playSongs();
                return true;
            case R.id.menu_album_play_next:
                playNext();
                return true;
            case R.id.menu_add_to_queue:
                addToQueue();
                return true;
            case R.id.menu_add_to_playlist:
                addToPlaylist();
                return true;
            case R.id.settings:
                startActivity(new Intent(getContext(), SettingsActivity.class));
                return true;

        }
        return false;
    }

    public void onArtworkLoaded(Bitmap resource) {
        if (!isAlbumArtTheme) {
            Palette.from(resource).generate(p -> {
                currentColor = ColorHelper.extractColorsFromPalette(p);
                Util.setTaskDescriptionColor(getActivity(), currentColor);
            });
        }
    }

    public void addAdapterDataObserver() {
        if (getAdapter() != null) {
            getAdapter().registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    if (emptyDataTextView != null) {
                        Timber.d("Entered observer");
                        emptyDataTextView.setText(getEmptyDataMessage());
                        emptyDataTextView.setVisibility(getAdapter().getItemCount() == 0 ? View.VISIBLE : View.GONE);
                    }
                }
            });
        }
    }

    public String getEmptyDataMessage() {
        return "No Song";
    }

    @Nullable
    public abstract RecyclerView.Adapter getAdapter();

    public abstract Toolbar getToolbar();

    public abstract ImageView getArtWorkImageView();

    public abstract AppBarLayout getAppbarLayout();

    public abstract String getToolbarTitle();

    public abstract void playShuffledSongs();

    public abstract void playSongs();

    public abstract void playNext();

    public abstract void addToQueue();

    public abstract void addToPlaylist();
}
