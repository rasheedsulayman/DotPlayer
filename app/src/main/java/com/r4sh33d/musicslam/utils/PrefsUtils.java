package com.r4sh33d.musicslam.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.r4sh33d.musicslam.fragments.settings.SettingsFragment;

public class PrefsUtils {

    private static final String LAST_PAGER_PAGE_KEY = "pager_start_page";
    private static final String CURRENT_THEME_KEY = "current_theme_key";
    private static PrefsUtils sPrefsUtilsInstance;
    private SharedPreferences sharedPref;


    private PrefsUtils(Context context) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PrefsUtils getInstance(Context context) {
        if (sPrefsUtilsInstance == null) {
            sPrefsUtilsInstance = new PrefsUtils(context);
        }
        return sPrefsUtilsInstance;
    }

    public void putString(String key, String value) {
        sharedPref.edit().putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return sharedPref.getString(key, defaultValue);
    }

    public boolean contains(String key) {
        return sharedPref.contains(key);
    }


    public void putInt(String key, int value) {
        sharedPref.edit().putInt(key, value).apply();
    }

    public void putLong(String key, long value) {
        sharedPref.edit().putLong(key, value).apply();
    }

    public int getInt(String key, int defaultValue) {
        return sharedPref.getInt(key, defaultValue);
    }

    public long getLong(String key, long defaultValue) {
        return sharedPref.getLong(key, defaultValue);
    }


    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPref.getBoolean(key, defaultValue);
    }

    public void putLastViewPagerFragmentId(int id) {
        putInt(LAST_PAGER_PAGE_KEY, id);
    }

    public int getLastViewPagerPage() {
        return getInt(LAST_PAGER_PAGE_KEY, 0);
    }

    public boolean enableLockScreenArtWork() {
        return getBoolean(SettingsFragment.PREF_KEY_ENABLE_LOCK_SCREEN_ART, false);
    }

    public boolean isArtworkAutoDownloadEnabled() {
        return getBoolean(SettingsFragment.PREF_KEY_AUTO_DONWLOAD_ARTWORK, true);
    }

    public boolean downloadOnWiFiOnly() {
        return getBoolean(SettingsFragment.PREF_KEY_DOWNLOAD_ON_WIFI_ONLY, false);
    }

    public boolean isAlbumArtTheme() {
        return getCurrentTheme() == ThemesTypes.AlbumArtTheme;
    }

    public boolean isDarkTheme() {
        int currentTheme = getCurrentTheme();
        return currentTheme == ThemesTypes.DarkTheme
                || currentTheme == ThemesTypes.BlackTheme
                || currentTheme == ThemesTypes.BlueTheme;
    }

    public boolean isLightTheme() {
        return getCurrentTheme() == ThemesTypes.LightTheme;
    }

    public int getStartPage() {
        int lastPage = Integer.parseInt(getString(SettingsFragment.PREF_KEY_START_PAGE, "-1"));
        if (lastPage == -1) {
            lastPage = getLastViewPagerPage();
        }
        return lastPage;
    }

    public int getCurrentTheme() {
        return getInt(CURRENT_THEME_KEY, ThemesTypes.LightTheme);
    }

    @SuppressLint("ApplySharedPref")
    public void setCurrentTheme(int theme) {
        sharedPref.edit().putInt(CURRENT_THEME_KEY, theme).commit();
    }

    public interface ThemesTypes {
        int AlbumArtTheme = 1;
        int LightTheme = 2;
        int DarkTheme = 3;
        int BlackTheme = 4;
        int BlueTheme = 5;
    }
}
