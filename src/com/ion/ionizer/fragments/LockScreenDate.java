/*
 * Copyright (C) 2019-2020 ion-OS
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

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.widget.Switch;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.widget.SwitchBar;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import com.ion.ionizer.preferences.SecureSettingMasterSwitchPreference;
import com.ion.ionizer.preferences.SystemSettingSeekBarPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class LockScreenDate extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable, SwitchBar.OnSwitchChangeListener {

    private static final String LOCK_DATE_STYLE = "lockscreen_date_selection";
    private static final String LOCK_DATE_FONTS = "lock_date_fonts";
    private static final String DATE_FONT_SIZE  = "lockdate_font_size";
    private static final String LOCK_OWNERINFO_FONTS = "lock_ownerinfo_fonts";
    private static final String LOCKOWNER_FONT_SIZE  = "lockowner_font_size";

    private ListPreference mLockDateStyle;
    private ListPreference mLockDateFonts;
    private SystemSettingSeekBarPreference mDateFontSize;
    private ListPreference mLockOwnerInfoFonts;
    private SystemSettingSeekBarPreference mOwnerInfoFontSize;

    private SwitchBar mSwitchBar;

    @Override
    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);

        final boolean isChecked = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_INFO, 1) != 0;
        mSwitchBar = ((SettingsActivity) getActivity()).getSwitchBar();
        mSwitchBar.addOnSwitchChangeListener(this);
        mSwitchBar.setChecked(isChecked);
        mSwitchBar.show();
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        if (switchView != mSwitchBar.getSwitch()) {
            return;
        }
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_INFO, isChecked ? 1 : 0);
        updatePreferences();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.ion_settings_lockscreen_date);

        ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        Resources resources = getResources();

        mLockDateStyle = (ListPreference) findPreference(LOCK_DATE_STYLE);

        // Lockscren Date Fonts
        mLockDateFonts = (ListPreference) findPreference(LOCK_DATE_FONTS);
        mLockDateFonts.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.LOCK_DATE_FONTS, 0)));
        mLockDateFonts.setSummary(mLockDateFonts.getEntry());
        mLockDateFonts.setOnPreferenceChangeListener(this);

        // Lock Date Size
        mDateFontSize = (SystemSettingSeekBarPreference) findPreference(DATE_FONT_SIZE);
        mDateFontSize.setValue(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKDATE_FONT_SIZE, 18));
        mDateFontSize.setOnPreferenceChangeListener(this);

        // Lockscreen Owner Info Fonts
        mLockOwnerInfoFonts = (ListPreference) findPreference(LOCK_OWNERINFO_FONTS);
        mLockOwnerInfoFonts.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.LOCK_OWNERINFO_FONTS, 0)));
        mLockOwnerInfoFonts.setSummary(mLockOwnerInfoFonts.getEntry());
        mLockOwnerInfoFonts.setOnPreferenceChangeListener(this);

        // Lockscreen Owner Info Size
        mOwnerInfoFontSize = (SystemSettingSeekBarPreference) findPreference(LOCKOWNER_FONT_SIZE);
        mOwnerInfoFontSize.setValue(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKOWNER_FONT_SIZE, 18));
        mOwnerInfoFontSize.setOnPreferenceChangeListener(this);

        updatePreferences();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mLockDateFonts) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCK_DATE_FONTS,
                    Integer.valueOf((String) newValue));
            mLockDateFonts.setValue(String.valueOf(newValue));
            mLockDateFonts.setSummary(mLockDateFonts.getEntry());
            return true;
        } else if (preference == mDateFontSize) {
            int top = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKDATE_FONT_SIZE, top*1);
            return true;
        } else if (preference == mLockOwnerInfoFonts) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCK_OWNERINFO_FONTS,
                    Integer.valueOf((String) newValue));
            mLockOwnerInfoFonts.setValue(String.valueOf(newValue));
            mLockOwnerInfoFonts.setSummary(mLockOwnerInfoFonts.getEntry());
            return true;
        } else if (preference == mOwnerInfoFontSize) {
            int top = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKOWNER_FONT_SIZE, top*1);
            return true;
        }
        return false;
    }

    private void updatePreferences() {
        boolean isChecked = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_INFO, 1) != 0;

        mLockDateStyle.setEnabled(isChecked);
        mLockDateFonts.setEnabled(isChecked);
        mDateFontSize.setEnabled(isChecked);
        mLockOwnerInfoFonts.setEnabled(isChecked);
        mOwnerInfoFontSize.setEnabled(isChecked);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ION_IONIZER;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.ion_settings_lockscreen_date;
                    result.add(sir);
                    return result;
                }
                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    return result;
                }
    };
}
