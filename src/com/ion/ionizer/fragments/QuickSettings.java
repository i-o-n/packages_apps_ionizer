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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.SettingsPreferenceFragment;

import com.ion.ionizer.R;
import com.ion.ionizer.preferences.SystemSettingEditTextPreference;
import com.ion.ionizer.preferences.SystemSettingSeekBarPreference;

public class QuickSettings extends DashboardFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String TAG = "QuickSettings";

    private static final String PREF_ROWS_PORTRAIT = "qs_rows_portrait";
    private static final String PREF_ROWS_LANDSCAPE = "qs_rows_landscape";
    private static final String PREF_COLUMNS_PORTRAIT = "qs_columns_portrait";
    private static final String PREF_COLUMNS_LANDSCAPE = "qs_columns_landscape";
    private static final String PREF_COLUMNS_QUICKBAR = "qs_columns_quickbar";
    private static final String ION_FOOTER_TEXT_STRING = "ion_footer_text_string";
    private static final String QS_FOOTER = "quick_footer";
    private static final String QS_USER_TOGGLE = "qs_user_toggle";

    private SystemSettingSeekBarPreference mRowsPortrait;
    private SystemSettingSeekBarPreference mRowsLandscape;
    private SystemSettingSeekBarPreference mQsColumnsPortrait;
    private SystemSettingSeekBarPreference mQsColumnsLandscape;
    private SystemSettingSeekBarPreference mQsColumnsQuickbar;
    private SystemSettingEditTextPreference mFooterString;
    private PreferenceCategory mQsFooter;
    private SwitchPreference mUserIcon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentResolver resolver = getActivity().getContentResolver();

        mQsColumnsQuickbar = (SystemSettingSeekBarPreference) findPreference(PREF_COLUMNS_QUICKBAR);
        int columnsQuickbar = Settings.System.getInt(resolver,
                Settings.System.OMNI_QS_QUICKBAR_COLUMNS, 6);
        mQsColumnsQuickbar.setValue(columnsQuickbar);
        mQsColumnsQuickbar.setOnPreferenceChangeListener(this);

        mRowsPortrait = (SystemSettingSeekBarPreference) findPreference(PREF_ROWS_PORTRAIT);
        int rowsPortrait = Settings.System.getIntForUser(resolver,
                Settings.System.OMNI_QS_LAYOUT_ROWS, 3, UserHandle.USER_CURRENT);
        mRowsPortrait.setValue(rowsPortrait);
        mRowsPortrait.setOnPreferenceChangeListener(this);

        mRowsLandscape = (SystemSettingSeekBarPreference) findPreference(PREF_ROWS_LANDSCAPE);
        int rowsLandscape = Settings.System.getIntForUser(resolver,
                Settings.System.OMNI_QS_LAYOUT_ROWS_LANDSCAPE, 2, UserHandle.USER_CURRENT);
        mRowsLandscape.setValue(rowsLandscape);
        mRowsLandscape.setOnPreferenceChangeListener(this);

        mQsColumnsPortrait = (SystemSettingSeekBarPreference) findPreference(PREF_COLUMNS_PORTRAIT);
        int columnsPortrait = Settings.System.getIntForUser(resolver,
                Settings.System.OMNI_QS_LAYOUT_COLUMNS, 3, UserHandle.USER_CURRENT);
        mQsColumnsPortrait.setValue(columnsPortrait);
        mQsColumnsPortrait.setOnPreferenceChangeListener(this);

        mQsColumnsLandscape = (SystemSettingSeekBarPreference) findPreference(PREF_COLUMNS_LANDSCAPE);
        int columnsLandscape = Settings.System.getIntForUser(resolver,
                Settings.System.OMNI_QS_LAYOUT_COLUMNS_LANDSCAPE, 4, UserHandle.USER_CURRENT);
        mQsColumnsLandscape.setValue(columnsLandscape);
        mQsColumnsLandscape.setOnPreferenceChangeListener(this);

        mFooterString = (SystemSettingEditTextPreference) findPreference(ION_FOOTER_TEXT_STRING);
        mFooterString.setOnPreferenceChangeListener(this);
        String footerString = Settings.System.getString(getContentResolver(),
                ION_FOOTER_TEXT_STRING);
        if (TextUtils.isEmpty(footerString) || footerString == null) {
            mFooterString.setText("#ion");
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.ION_FOOTER_TEXT_STRING, "#ion");
        } else {
            mFooterString.setText(footerString);
        }

        mQsFooter = (PreferenceCategory) findPreference(QS_FOOTER);
        mUserIcon = (SwitchPreference) findPreference(QS_USER_TOGGLE);
        boolean userIcon = Settings.Global.getInt(getContentResolver(),
                Settings.Global.USER_SWITCHER_ENABLED, 0) != 0;
        if (!userIcon) {
            mQsFooter.removePreference(mUserIcon);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mQsColumnsQuickbar) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.OMNI_QS_QUICKBAR_COLUMNS, value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mRowsPortrait) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(resolver,
                    Settings.System.OMNI_QS_LAYOUT_ROWS, value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mRowsLandscape) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(resolver,
                    Settings.System.OMNI_QS_LAYOUT_ROWS_LANDSCAPE, value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mQsColumnsPortrait) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(resolver,
                    Settings.System.OMNI_QS_LAYOUT_COLUMNS, value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mQsColumnsLandscape) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(resolver,
                    Settings.System.OMNI_QS_LAYOUT_COLUMNS_LANDSCAPE, value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mFooterString) {
            String value = (String) newValue;
            if (TextUtils.isEmpty(value) || value == null) {
                mFooterString.setText("#ion");
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.ION_FOOTER_TEXT_STRING, "#ion");
            } else {
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.ION_FOOTER_TEXT_STRING, value);
            }
            return true;
        }
        return false;
    }

    protected int getPreferenceScreenResId() {
        return R.xml.ion_settings_quicksettings;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ION_IONIZER;
    }
}
