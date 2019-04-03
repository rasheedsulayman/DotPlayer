package com.r4sh33d.musicslam.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.AutoSwitchMode;
import com.afollestad.aesthetic.Util;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.blurtransition.BlurImageView;
import com.r4sh33d.musicslam.fragments.nowplaying.NowplayingFragment;
import com.r4sh33d.musicslam.fragments.pager.MainViewFragment;
import com.r4sh33d.musicslam.sleeptimer.SleepTimerDialog;
import com.r4sh33d.musicslam.utils.NavigationUtil;
import com.r4sh33d.musicslam.utils.PrefsUtils;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;


public class MainActivity extends ThemedSlidingPanelActivity implements
        NowplayingFragment.NowPlayingControlsCallback {
    @BindView(R.id.blurImage)
    BlurImageView blurImageView;
    @BindView(R.id.bg_black_shade)
    ImageView albumArtBlackShade;
    @BindView(R.id.dragView)
    FrameLayout dragView;

    Disposable colorPrimarySubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isAlbumArtTheme = PrefsUtils.getInstance(this).isAlbumArtTheme();
        if (isAlbumArtTheme) {
            setTheme(R.style.AlbumArtTheme);
            setContentView(R.layout.actvity_main_slidingup);
            super.onCreate(savedInstanceState);
        } else {
            Aesthetic.attach(this);
            if (Aesthetic.isFirstTime()) {
                Aesthetic.get()
                        .colorPrimary(getResources().getColor(R.color.colorPrimary))
                        .colorAccent(getResources().getColor(R.color.colorAccent))
                        .colorStatusBar(getResources().getColor(R.color.transparent))
                        .lightStatusBarMode(AutoSwitchMode.OFF)
                        .apply();
            }
            setContentView(R.layout.actvity_main_slidingup);
            activateTransparentStatusBar();
            super.onCreate(savedInstanceState);
            blurImageView.setVisibility(View.GONE);
            albumArtBlackShade.setVisibility(View.GONE);
        }

        ButterKnife.bind(this);
        dragView.setEnabled(false);
        subscribeToColorPrimaryChanges();
        initMainFragment();
        initSlidingFragment();
    }


    void subscribeToColorPrimaryChanges() {
        if (!isAlbumArtTheme) {
            colorPrimarySubscription = Aesthetic.get()
                    .colorPrimary()
                    .subscribe(color -> Util.setTaskDescriptionColor(this, color));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        blurImageView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        blurImageView.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    private void initMainFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mainViewContainer, new MainViewFragment());
        transaction.commit();
    }

    void initSlidingFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.dragView, new NowplayingFragment());
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activty_main, menu);
        return true;
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        loadBlurredArtwork();
    }

    @Override
    public void onMetaChanged() {
        super.onMetaChanged();
        loadBlurredArtwork();
    }

    void loadBlurredArtwork() {
        if (isAlbumArtTheme) {
            blurImageView.loadBlurImage();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_search:
                NavigationUtil.moveToSearchPage(this);
                return true;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_sleep_timer:
                new SleepTimerDialog()
                        .show(getSupportFragmentManager(), SleepTimerDialog.SLEEP_DIALOG_ARG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public SlidingUpPanelLayout.PanelState getPanelState() {
        return mSlidingUpPanelLayout.getPanelState();
    }

    @Override
    public void setPanelState(SlidingUpPanelLayout.PanelState panelState) {
        mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    @Override
    public void setTouchSlidingPanelEnabled(boolean isEnabled) {
        mSlidingUpPanelLayout.setTouchEnabled(isEnabled);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isAlbumArtTheme) {
            Aesthetic.resume(this);
        }
    }

    @Override
    protected void onPause() {
        if (!isAlbumArtTheme) {
            Aesthetic.pause(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (!isAlbumArtTheme) {
            colorPrimarySubscription.dispose();
        }
        super.onDestroy();
    }
}