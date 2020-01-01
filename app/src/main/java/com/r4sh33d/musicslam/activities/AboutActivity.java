package com.r4sh33d.musicslam.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.AestheticActivity;
import com.afollestad.aesthetic.Util;
import com.afollestad.materialdialogs.MaterialDialog;
import com.r4sh33d.musicslam.BuildConfig;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.utils.NavigationUtil;
import com.r4sh33d.musicslam.utils.PrefsUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;

/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class AboutActivity extends AestheticActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.status_bar_view)
    View statusBarView;
    Disposable colorPrimarySubscription;
    @BindView(R.id.version_textview)
    TextView versionNameTextView;
    @BindView(R.id.app_name)
    TextView appNameTextView;
    PrefsUtils prefsUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        activateTransparentStatusBar();
        versionNameTextView.setText(String.format("Version %s", BuildConfig.VERSION_NAME));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_white_24dp);
        toolbar.setNavigationOnClickListener(v -> finish());
        prefsUtils = PrefsUtils.getInstance(this);
        colorPrimarySubscription = Aesthetic.get()
                .colorPrimary()
                .subscribe((Integer color) -> {
                    statusBarView.setBackgroundColor(Util.darkenColor(color));
                    Util.setTaskDescriptionColor(this, color);
                });
    }

    public void activateTransparentStatusBar() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @OnClick(R.id.send_feedback_button)
    public void onClickSendFeedbackButton() {
        startActivity(Intent.createChooser(NavigationUtil.getFeedbackEmailIntent(), getString(R.string.send_feedback)));
    }


    @OnClick(R.id.licences_button)
    public void onClickLicencesButton() {
        WebView view = (WebView) LayoutInflater.from(this).inflate(R.layout.licences_dialog, null);
        view.loadUrl(prefsUtils.isDarkTheme() ? "file:///android_asset/notices_dark.html" : "file:///android_asset/notices.html");
        new MaterialDialog.Builder(this)
                .title("Licences")
                .customView(view, false)
                .positiveText("Okay")
                .show();
    }


    @OnClick(R.id.rate_on_playstore_button)
    public void onClickRateAppButton() {
        NavigationUtil.launchPlayStore(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        colorPrimarySubscription.dispose();
    }
}

