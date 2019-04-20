package com.r4sh33d.musicslam.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.utils.PrefsUtils;

public abstract class BaseActivity extends AppCompatActivity {
    private static final int PERMISSION_CODE = 500;
    private static final int REQUEST_APP_SETTINGS = 600;
    public boolean isAlbumArtTheme;
    public boolean isPermissionGranted;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isAlbumArtTheme = PrefsUtils.getInstance(this).isAlbumArtTheme();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        checkPermissions();
    }


    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = false;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_CODE);
        } else {
            isPermissionGranted = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onStoragePermissionGranted();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        Snackbar.make(getWindow().getDecorView().getRootView(),
                                R.string.external_storage_permission_is_needed,
                                Snackbar.LENGTH_INDEFINITE)
                                .setAction(R.string.grant, v -> checkPermissions()).show();
                    } else {
                        new MaterialDialog.Builder(this)
                                .title(R.string.enable_permission)
                                .content(R.string.manual_permission_grant_instruction)
                                .positiveText(R.string.settings)
                                .onPositive((dialog, which) -> {
                                    openAppSettings();
                                    finish();
                                }).negativeText(R.string.cancel)
                                .onNegative((dialog, which) -> {
                                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                                    finish();
                                }).show();
                    }
                }
            }
        }
    }

    public void openAppSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(myAppSettings, REQUEST_APP_SETTINGS);
    }

    public abstract void onStoragePermissionGranted();

    public void activateTransparentStatusBar() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}
