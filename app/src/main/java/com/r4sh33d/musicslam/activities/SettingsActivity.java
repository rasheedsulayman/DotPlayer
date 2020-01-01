package com.r4sh33d.musicslam.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.AestheticActivity;
import com.afollestad.aesthetic.Util;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.fragments.settings.SettingsFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;

/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class SettingsActivity extends AestheticActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.status_bar_view)
    View statusBarView;
    Disposable colorPrimarySubscription;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_activty);
        ButterKnife.bind(this);
        activateTransparentStatusBar();
        toolbar.setNavigationIcon(R.drawable.ic_arrow_white_24dp);
        toolbar.setNavigationOnClickListener(v -> finish());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content, new SettingsFragment())
                .commit();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        colorPrimarySubscription.dispose();
    }
}
