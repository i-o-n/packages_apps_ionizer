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
public class LockScreenClock extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable, SwitchBar.OnSwitchChangeListener {

    private static final String LOCKSCREEN_CLOCK_SELECTION = "lockscreen_clock_selection";
    private static final String LOCK_CLOCK_FONTS = "lock_clock_fonts";
    private static final String CLOCK_FONT_SIZE  = "lockclock_font_size";
    private static final String TEXT_CLOCK_ALIGNMENT = "text_clock_alignment";
    private static final String TEXT_CLOCK_PADDING = "text_clock_padding";

    private ListPreference mLockClockSelection;
    private ListPreference mLockClockFonts;
    private ListPreference mTextClockAlign;
    private SystemSettingSeekBarPreference mClockFontSize;
    private SystemSettingSeekBarPreference mTextClockPadding;

    private SwitchBar mSwitchBar;

    @Override
    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);

        final boolean isChecked = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_CLOCK, 1) != 0;
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
                Settings.System.LOCKSCREEN_CLOCK, isChecked ? 1 : 0);
        updatePreferences();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.ion_settings_lockscreen_clock);

        ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        Resources resources = getResources();

        // Lockscreen Clock
        mLockClockSelection = (ListPreference) findPreference(LOCKSCREEN_CLOCK_SELECTION);
        mLockClockSelection.setOnPreferenceChangeListener(this);

        // Lockscren Clock Fonts
        mLockClockFonts = (ListPreference) findPreference(LOCK_CLOCK_FONTS);
        mLockClockFonts.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.LOCK_CLOCK_FONTS, 0)));
        mLockClockFonts.setSummary(mLockClockFonts.getEntry());
        mLockClockFonts.setOnPreferenceChangeListener(this);

        // Lock Clock Size
        mClockFontSize = (SystemSettingSeekBarPreference) findPreference(CLOCK_FONT_SIZE);
        mClockFontSize.setValue(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKCLOCK_FONT_SIZE, 54));
        mClockFontSize.setOnPreferenceChangeListener(this);

        // Text Clock Alignment
        mTextClockAlign = (ListPreference) findPreference(TEXT_CLOCK_ALIGNMENT);
        mTextClockAlign.setOnPreferenceChangeListener(this);

        // Text Clock Padding
        mTextClockPadding = (SystemSettingSeekBarPreference) findPreference(TEXT_CLOCK_PADDING);

        updatePreferences();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mLockClockSelection) {
            boolean isChecked = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_CLOCK, 1) != 0;
            boolean val = Integer.valueOf((String) newValue) == 9
                    || Integer.valueOf((String) newValue) == 10;
            mTextClockAlign.setEnabled(val && isChecked);
            return true;
        } else if (preference == mLockClockFonts) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCK_CLOCK_FONTS,
                    Integer.valueOf((String) newValue));
            mLockClockFonts.setValue(String.valueOf(newValue));
            mLockClockFonts.setSummary(mLockClockFonts.getEntry());
            return true;
        } else if (preference == mClockFontSize) {
            int top = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKCLOCK_FONT_SIZE, top*1);
            return true;
        } else if (preference == mTextClockAlign) {
            boolean isChecked = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_CLOCK, 1) != 0;
            boolean val = Integer.valueOf((String) newValue) == 1;
            mTextClockPadding.setEnabled(!val && isChecked);
            return true;
        }
        return false;
    }

    private void updatePreferences() {
        boolean isChecked = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_CLOCK, 1) != 0;
        boolean mClockSelection = Settings.Secure.getIntForUser(getActivity().getContentResolver(),
                Settings.Secure.LOCKSCREEN_CLOCK_SELECTION, 0, UserHandle.USER_CURRENT) == 9
                || Settings.Secure.getIntForUser(getActivity().getContentResolver(),
                Settings.Secure.LOCKSCREEN_CLOCK_SELECTION, 0, UserHandle.USER_CURRENT) == 10;
        boolean mTextClockAlignx = Settings.System.getIntForUser(getActivity().getContentResolver(),
                Settings.System.TEXT_CLOCK_ALIGNMENT, 0, UserHandle.USER_CURRENT) == 1;

        mLockClockSelection.setEnabled(isChecked);
        mLockClockFonts.setEnabled(isChecked);
        mClockFontSize.setEnabled(isChecked);
        mTextClockAlign.setEnabled(mClockSelection && isChecked);
        mTextClockPadding.setEnabled(!mTextClockAlignx && isChecked);
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
                    sir.xmlResId = R.xml.ion_settings_lockscreen_clock;
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
