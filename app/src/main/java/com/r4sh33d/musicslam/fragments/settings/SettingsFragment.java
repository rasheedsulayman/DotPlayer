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
import com.r4sh33d.musicslam.dialogs.ClearCacheDialog;
import com.r4sh33d.musicslam.utils.NavigationUtil;
import com.r4sh33d.musicslam.utils.PrefsUtils;

import static com.r4sh33d.musicslam.utils.PrefsUtils.ThemesTypes;
import static com.r4sh33d.musicslam.utils.PrefsUtils.getInstance;
/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener, ColorChooserDialog.ColorCallback {
    public static final String PREF_KEY_THEME = "pref_key_theme";
    public static final String PREF_KEY_COLOR_PRIMARY = "pref_key_color_primary";
    public static final String PREF_KEY_COLOR_ACCENT = "pref_key_color_accent";
    public static final String PREF_KEY_ALBUM_ART_THEME = "pref_key_album_art_theme";
    public static final String PREF_KEY_ENABLE_LOCK_SCREEN_ART = "pref_key_album_art_on_lock_screen";
    public static final String PREF_KEY_AUTO_DONWLOAD_ARTWORK = "pref_key_auto_donwload";
    public static final String PREF_KEY_DOWNLOAD_ON_WIFI_ONLY = "pref_key_download_via_wifi_only";
    public static final String PREF_KEY_START_PAGE = "pref_key_start_page";
    public static final String PREF_KEY_APP_INFO = "pref_key_app_info";
    public static final String PREF_KEY_EQUALIZER = "pref_key_equalizer";
    public static final String PREF_KEY_DELETE_CACHED_CONTENTS = "pref_key_delete_cached_contents";

    public static final String PREF_THEME_VALUE_WHITE = "light";
    public static final String PREF_THEME_VALUE_DARK = "dark";
    public static final String PREF_THEME_VALUE_BlACK = "black";
    public static final String PREF_THEME_VALUE_BLUE = "blue";
    ColorChooserDialog primaryColorChooserDialog, accentColorChooserDialog;
    SwitchPreferenceCompat albumArtPreference;
    ListPreference themesListPreference, startPageListPreference;
    boolean isAlbumArtTheme;

    PrefsUtils prefsUtils;

    public SettingsFragment() {
    }

    public static int getThemeType(String themeValue) {
        switch (themeValue) {
            case PREF_THEME_VALUE_WHITE:
                return ThemesTypes.LightTheme;
            case PREF_THEME_VALUE_DARK:
                return ThemesTypes.DarkTheme;
            case PREF_THEME_VALUE_BlACK:
                return ThemesTypes.BlackTheme;
            case PREF_THEME_VALUE_BLUE:
                return ThemesTypes.BlueTheme;
        }
        return ThemesTypes.LightTheme;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
        albumArtPreference = (SwitchPreferenceCompat) findPreference(PREF_KEY_ALBUM_ART_THEME);
        prefsUtils = getInstance(getContext());
        isAlbumArtTheme = prefsUtils.isAlbumArtTheme();
        albumArtPreference.setChecked(isAlbumArtTheme);
        themesListPreference = (ListPreference) findPreference(PREF_KEY_THEME);
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
                        .title(R.string.enable_album_art_theme)
                        .content(R.string.enable_album_art_theme_disclaimer_message)
                        .positiveText(R.string.okay)
                        .negativeText(R.string.cancel)
                        .onPositive((dialog, which) -> {
                            prefsUtils.setCurrentTheme(ThemesTypes.AlbumArtTheme);
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
                        .title(R.string.disable_album_art_theme)
                        .content(R.string.disable_album_art_theme_warning_message)
                        .positiveText(R.string.okay)
                        .show();
                return false;
            }
            switch ((String) newValue) {
                case PREF_THEME_VALUE_WHITE:
                    Aesthetic.get()
                            .activityTheme(R.style.AppThemeLight)
                            .isDark(false)
                            .apply();
                    prefsUtils.setCurrentTheme(ThemesTypes.LightTheme);
                    break;
                case PREF_THEME_VALUE_DARK:
                    Aesthetic.get()
                            .activityTheme(R.style.AppThemeDark)
                            .isDark(true)
                            .apply();
                    prefsUtils.setCurrentTheme(ThemesTypes.DarkTheme);
                    //We just enabled the dark theme
                    break;
                case PREF_THEME_VALUE_BlACK:
                    Aesthetic.get()
                            .activityTheme(R.style.AppThemeBlack)
                            .isDark(true)
                            .apply();
                    prefsUtils.setCurrentTheme(ThemesTypes.BlackTheme);
                    break;
                case PREF_THEME_VALUE_BLUE:
                    Aesthetic.get()
                            .activityTheme(R.style.AppThemeBlue)
                            .isDark(true)
                            .apply();
                    prefsUtils.setCurrentTheme(ThemesTypes.BlueTheme);
                    break;
            }
            return true;
        });

        Preference appInfoPreference = findPreference(PREF_KEY_APP_INFO);
        appInfoPreference.setOnPreferenceClickListener(preference -> {
            NavigationUtil.navigateToAboutPage(getContext());
            return true;
        });

        Preference deleteCachedContentsPreference = findPreference(PREF_KEY_DELETE_CACHED_CONTENTS);
        deleteCachedContentsPreference.setOnPreferenceClickListener(preference -> {
            new ClearCacheDialog().show(getFragmentManager(), ClearCacheDialog.CLEAR_CACHE_FRAG_TAG);
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

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {

    }
}
