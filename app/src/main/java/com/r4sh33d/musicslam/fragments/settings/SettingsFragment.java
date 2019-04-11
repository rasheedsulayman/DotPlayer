package com.r4sh33d.musicslam.fragments.settings;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.utils.NavigationUtil;
import com.r4sh33d.musicslam.utils.PrefsUtils;

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener, ColorChooserDialog.ColorCallback {
    ColorChooserDialog primaryColorChooserDialog, accentColorChooserDialog;

    public static final String PREF_KEY_THEME = "pref_key_theme";
    public static final String PREF_KEY_COLOR_PRIMARY = "pref_key_color_primary";
    public static final String PREF_KEY_COLOR_ACCENT = "pref_key_color_accent";
    public static final String PREF_KEY_ALBUM_ART_THEME = "pref_key_album_art_theme";
    public static final String PREF_KEY_ENABLE_LOCK_SCREEN_ART = "pref_key_album_art_on_lock_screen";
    public static final String PREF_KEY_AUTO_DONWLOAD_ARTWORK = "pref_key_auto_donwload";
    public static final String PREF_KEY_DOWNLOAD_ON_WIFI_ONLY = "pref_key_download_via_wifi_only";
    public static final String PREF_KEY_START_PAGE = "pref_key_start_page";

    public static final String PREF_KEY_EQUALIZER = "pref_key_equalizer";

    public static final String PREF_THEME_VALUE_WHITE = "light";
    public static final String PREF_THEME_VALUE_DARK = "dark";
    public static final String PREF_THEME_VALUE_BlACK = "black";
    public static final String PREF_THEME_VALUE_BLUE = "blue";

    SwitchPreferenceCompat albumArtPreference;
    ListPreference themesListPreference, startPageListPreference;
    boolean isAlbumArtTheme;

    PrefsUtils prefsUtils;

    public SettingsFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
        albumArtPreference = (SwitchPreferenceCompat) findPreference(PREF_KEY_ALBUM_ART_THEME);
        prefsUtils = PrefsUtils.getInstance(getContext());
        isAlbumArtTheme = prefsUtils.isAlbumArtTheme();
        albumArtPreference.setChecked(isAlbumArtTheme);
        themesListPreference = (ListPreference) findPreference(PREF_KEY_THEME);
       // themesListPreference.setEnabled(!isAlbumArtTheme);
        startPageListPreference = (ListPreference) findPreference(PREF_KEY_START_PAGE);
        startPageListPreference.setSummary(startPageListPreference.getEntry());
        prepareColorPreferenceClickListeners();
    }

    private void prepareColorPreferenceClickListeners() {
        Preference colorPrimaryPreference = findPreference(PREF_KEY_COLOR_PRIMARY);
        colorPrimaryPreference.setOnPreferenceClickListener(preference -> {
            int coloPrimary = Aesthetic.get()
                    .colorPrimary()
                    .take(1)
                    .blockingFirst(R.color.colorPrimary);
            primaryColorChooserDialog = new ColorChooserDialog.Builder(getActivity(), R.string.color_primary)
                    .titleSub(R.string.color_primary)
                    .accentMode(false)
                    .doneButton(R.string.done)
                    .cancelButton(R.string.cancel)
                    .backButton(R.string.back)
                    .preselect(coloPrimary)
                    .dynamicButtonColor(false)
                    .show(getChildFragmentManager());
            return true;
        });
        Preference colorAccentPreference = findPreference(PREF_KEY_COLOR_ACCENT);
        colorAccentPreference.setOnPreferenceClickListener(preference -> {
            int colorAccent = Aesthetic.get()
                    .colorAccent()
                    .take(1)
                    .blockingFirst(R.color.colorAccent);
            accentColorChooserDialog = new ColorChooserDialog.Builder(getActivity(), R.string.color_accent)
                    .titleSub(R.string.color_accent)
                    .accentMode(true)
                    .doneButton(R.string.done)
                    .cancelButton(R.string.cancel)
                    .backButton(R.string.back)
                    .preselect(colorAccent)
                    .dynamicButtonColor(false)
                    .show(getChildFragmentManager());
            return true;
        });

        albumArtPreference.setOnPreferenceClickListener(preference -> {
            if (albumArtPreference.isChecked()) {
                //noinspection ConstantConditions
                new MaterialDialog.Builder(getContext())
                        .title("Enable Album art theme?")
                        .content("This theme is experimental but usable!.\n" +
                                "You need to have correct artworks for greater visual experience")
                        .positiveText("Okay")
                        .negativeText("Cancel")
                        .onPositive((dialog, which) -> {
                            prefsUtils.setCurrentTheme(PrefsUtils.ThemesTypes.AlbumArtTheme);
                            albumArtPreference.setChecked(true);
                            startActivity(NavigationUtil.getAppRestartIntent(getContext()));
                        })
                        .onNegative((dialog, which) -> albumArtPreference.setChecked(false))
                        .dismissListener(dialog -> albumArtPreference.setChecked(false))
                        .show();
            } else {
                prefsUtils.setCurrentTheme(getThemeType(themesListPreference.getValue()));
                albumArtPreference.setChecked(false);
                startActivity(NavigationUtil.getAppRestartIntent(getContext()));
            }
            return true;
        });

        Preference equalizerPreference = findPreference(PREF_KEY_EQUALIZER);
        equalizerPreference.setOnPreferenceClickListener(preference -> {
            NavigationUtil.openEqualizer(getActivity(), NavigationUtil.OPEN_EQUALIZER_REQUEST);
            return true;
        });

        themesListPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if (isAlbumArtTheme) {
                new MaterialDialog.Builder(getContext())
                        .title("Disable Album art theme")
                        .content("You are currently using the album art theme. You need to disable it to continue.")
                        .show();
                return false;
            }
            switch ((String) newValue) {
                case PREF_THEME_VALUE_WHITE:
                    Aesthetic.get()
                            .activityTheme(R.style.AppThemeLight)
                            .isDark(false)
                            .apply();
                    prefsUtils.setCurrentTheme(PrefsUtils.ThemesTypes.LightTheme);
                    break;
                case PREF_THEME_VALUE_DARK:
                    Aesthetic.get()
                            .activityTheme(R.style.AppThemeDark)
                            .isDark(true)
                            .apply();
                    prefsUtils.setCurrentTheme(PrefsUtils.ThemesTypes.DarkTheme);
                    //We just enabled the dark theme
                    break;
                case PREF_THEME_VALUE_BlACK:
                    Aesthetic.get()
                            .activityTheme(R.style.AppThemeBlack)
                            .isDark(true)
                            .apply();
                    prefsUtils.setCurrentTheme(PrefsUtils.ThemesTypes.BlackTheme);
                    break;
                case PREF_THEME_VALUE_BLUE:
                    Aesthetic.get()
                            .activityTheme(R.style.AppThemeBlue)
                            .isDark(true)
                            .apply();
                    prefsUtils.setCurrentTheme(PrefsUtils.ThemesTypes.BlueTheme);
                    break;
            }
            return true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PREF_KEY_START_PAGE:
                startPageListPreference.setSummary(startPageListPreference.getEntry());
                break;
        }
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, int selectedColor) {
        if (dialog == primaryColorChooserDialog) {
            Aesthetic.get()
                    .colorPrimary(selectedColor)
                    .apply();
        } else if (dialog == accentColorChooserDialog) {
            Aesthetic.get()
                    .colorAccent(selectedColor)
                    .apply();
        }
    }

    public static int getThemeType(String themeValue) {
        switch (themeValue) {
            case PREF_THEME_VALUE_WHITE:
                return PrefsUtils.ThemesTypes.LightTheme;
            case PREF_THEME_VALUE_DARK:
                return PrefsUtils.ThemesTypes.DarkTheme;
            case PREF_THEME_VALUE_BlACK:
                return PrefsUtils.ThemesTypes.BlackTheme;
            case PREF_THEME_VALUE_BLUE:
                return PrefsUtils.ThemesTypes.BlueTheme;
        }
        return PrefsUtils.ThemesTypes.LightTheme;
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
    }
}
