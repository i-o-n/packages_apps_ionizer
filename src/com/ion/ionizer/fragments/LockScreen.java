/*
 * Copyright (C) 2019 ion-OS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ion.ionizer.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import com.ion.ionizer.preferences.SystemSettingListPreference;
import com.ion.ionizer.preferences.SystemSettingSeekBarPreference;
import com.ion.ionizer.Utils;

import android.provider.Settings;
import com.android.internal.util.custom.weather.WeatherClient;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class LockScreen extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String FINGERPRINT_VIB = "fingerprint_success_vib";

    private static final String KEY_AUTOCOLOR = "lockscreen_visualizer_autocolor";
    private static final String KEY_LAVALAMP = "lockscreen_lavalamp_enabled";

    private static final String CLOCK_FONT_SIZE  = "lockclock_font_size";
    private static final String LOCK_CLOCK_FONTS = "lock_clock_fonts";
    private static final String DATE_FONT_SIZE  = "lockdate_font_size";
    private static final String LOCK_DATE_FONTS = "lock_date_fonts";
    private static final String LOCKSCREEN_CLOCK_SELECTION  = "lockscreen_clock_selection";

    private static final String WEATHER_LS_CAT = "weather_lockscreen_key_two";

    private SwitchPreference mAutoColor;
    private SwitchPreference mLavaLamp;
    private FingerprintManager mFingerprintManager;
    private SwitchPreference mFingerprintVib;
    private SystemSettingSeekBarPreference mClockFontSize;
    private SystemSettingSeekBarPreference mDateFontSize;
    ListPreference mLockClockFonts;
    ListPreference mLockDateFonts;
    SystemSettingListPreference mLockClockStyle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.ion_settings_lockscreen);

        ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        Resources resources = getResources();

        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mFingerprintVib = (SwitchPreference) findPreference(FINGERPRINT_VIB);
        if (mFingerprintManager == null){
            prefScreen.removePreference(mFingerprintVib);
        } else {
            mFingerprintVib.setChecked((Settings.System.getInt(getContentResolver(),
                    Settings.System.FINGERPRINT_SUCCESS_VIB, 1) == 1));
            mFingerprintVib.setOnPreferenceChangeListener(this);
        }

        boolean mMediaArtEnabled = Settings.System.getIntForUser(resolver,
                Settings.System.LOCKSCREEN_MEDIA_METADATA, 1,
                UserHandle.USER_CURRENT) != 0;
        boolean mLavaLampEnabled = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.LOCKSCREEN_LAVALAMP_ENABLED, 1,
                UserHandle.USER_CURRENT) != 0;

        mAutoColor = (SwitchPreference) findPreference(KEY_AUTOCOLOR);
        mAutoColor.setEnabled(mMediaArtEnabled && !mLavaLampEnabled);

        if (!mMediaArtEnabled) {
            mAutoColor.setSummary(getActivity().getString(
                    R.string.lockscreen_autocolor_mediametadata));
        } else if (mLavaLampEnabled) {
            mAutoColor.setSummary(getActivity().getString(
                    R.string.lockscreen_autocolor_lavalamp));
        } else {
            mAutoColor.setSummary(getActivity().getString(
                    R.string.lockscreen_autocolor_summary));
        }

        mLavaLamp = (SwitchPreference) findPreference(KEY_LAVALAMP);
        mLavaLamp.setOnPreferenceChangeListener(this);

        // Lockscren Clock Fonts
        mLockClockFonts = (ListPreference) findPreference(LOCK_CLOCK_FONTS);
        mLockClockFonts.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.LOCK_CLOCK_FONTS, 17)));
        mLockClockFonts.setSummary(mLockClockFonts.getEntry());
        mLockClockFonts.setOnPreferenceChangeListener(this);

        mLockClockStyle = (SystemSettingListPreference) findPreference(LOCKSCREEN_CLOCK_SELECTION);
        mLockClockStyle.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.LOCKSCREEN_CLOCK_SELECTION, 0)));
        mLockClockStyle.setOnPreferenceChangeListener(this);

        // Lockscren Date Fonts
        mLockDateFonts = (ListPreference) findPreference(LOCK_DATE_FONTS);
        mLockDateFonts.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.LOCK_DATE_FONTS, 0)));
        mLockDateFonts.setSummary(mLockDateFonts.getEntry());
        mLockDateFonts.setOnPreferenceChangeListener(this);

        // Lock Clock Size
        mClockFontSize = (SystemSettingSeekBarPreference) findPreference(CLOCK_FONT_SIZE);
        mClockFontSize.setValue(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKCLOCK_FONT_SIZE, 64));
        mClockFontSize.setOnPreferenceChangeListener(this);

         // Lock Date Size
        mDateFontSize = (SystemSettingSeekBarPreference) findPreference(DATE_FONT_SIZE);
        mDateFontSize.setValue(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKDATE_FONT_SIZE,16));
        mDateFontSize.setOnPreferenceChangeListener(this);

        final PreferenceCategory weatherCategory = (PreferenceCategory) prefScreen
                .findPreference(WEATHER_LS_CAT);

        if (!Utils.isPackageInstalled(getContext(), "com.android.providers.weather")) {
            prefScreen.removePreference(weatherCategory);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mFingerprintVib) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.FINGERPRINT_SUCCESS_VIB, value ? 1 : 0);
            return true;
        } else if (preference == mClockFontSize) {
            int top = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKCLOCK_FONT_SIZE, top*1);
            return true;
        } else if (preference == mDateFontSize) {
            int top = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKDATE_FONT_SIZE, top*1);
            return true;
        } else if (preference == mLockClockFonts) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCK_CLOCK_FONTS,
                    Integer.valueOf((String) newValue));
            mLockClockFonts.setValue(String.valueOf(newValue));
            mLockClockFonts.setSummary(mLockClockFonts.getEntry());
        } else if (preference == mLockDateFonts) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCK_DATE_FONTS,
                    Integer.valueOf((String) newValue));
            mLockDateFonts.setValue(String.valueOf(newValue));
            mLockDateFonts.setSummary(mLockDateFonts.getEntry());
            return true;
        } else if (preference == mLockClockStyle) {
            int val = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_CLOCK_SELECTION, val);
            return true;
        } else if (preference == mLavaLamp) {
            boolean mLavaLampEnabled = (Boolean) newValue;
            boolean mMediaArtEnabled = Settings.System.getIntForUser(resolver,
                Settings.System.LOCKSCREEN_MEDIA_METADATA, 1,
                UserHandle.USER_CURRENT) != 0;

            mAutoColor.setEnabled(mMediaArtEnabled && !mLavaLampEnabled);

            if (!mMediaArtEnabled) {
                mAutoColor.setSummary(getActivity().getString(
                        R.string.lockscreen_autocolor_mediametadata));
            } else if (mLavaLampEnabled) {
                mAutoColor.setSummary(getActivity().getString(
                        R.string.lockscreen_autocolor_lavalamp));
            } else {
                mAutoColor.setSummary(getActivity().getString(
                        R.string.lockscreen_autocolor_summary));
            }
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ION_IONIZER;
    }
}
