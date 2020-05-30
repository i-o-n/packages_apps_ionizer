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
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import com.ion.ionizer.preferences.SecureSettingMasterSwitchPreference;
import com.ion.ionizer.preferences.SystemSettingMasterSwitchPreference;
import com.ion.ionizer.preferences.SystemSettingSeekBarPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class LockScreen extends DashboardFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    public static final String TAG = "LockScreenSettings";
    private static final String LOCKSCREEN_CLOCK = "lockscreen_clock";
    private static final String LOCKSCREEN_INFO = "lockscreen_info";
    private static final String LOCKSCREEN_VISUALIZER_ENABLED = "lockscreen_visualizer_enabled";
    private static final String LOCKSCREEN_EXTRA = "lockscreen_extra";
    private static final String FOD_ICON_PICKER_CATEGORY = "fod_icon_picker_category";
    private static final String FOD_RECOGNIZING_ANIMATION = "fod_recognizing_animation";
    private static final String FOD_ANIM_PICKER = "fod_anim";

    private SystemSettingMasterSwitchPreference mClockEnabled;
    private SystemSettingMasterSwitchPreference mInfoEnabled;
    private SecureSettingMasterSwitchPreference mVisualizerEnabled;
    private PreferenceCategory mLsExtra;
    private Preference mFODIconPicker;
    private SwitchPreference mFODRecognitionAnimation;
    private ListPreference mFODAnimationPicker;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        Resources resources = getResources();

        mClockEnabled = (SystemSettingMasterSwitchPreference) findPreference(LOCKSCREEN_CLOCK);
        mClockEnabled.setOnPreferenceChangeListener(this);

        mInfoEnabled = (SystemSettingMasterSwitchPreference) findPreference(LOCKSCREEN_INFO);
        mInfoEnabled.setOnPreferenceChangeListener(this);

        mVisualizerEnabled = (SecureSettingMasterSwitchPreference) findPreference(LOCKSCREEN_VISUALIZER_ENABLED);
        mVisualizerEnabled.setOnPreferenceChangeListener(this);
        int visualizerEnabled = Settings.Secure.getInt(resolver,
                LOCKSCREEN_VISUALIZER_ENABLED, 0);
        mVisualizerEnabled.setChecked(visualizerEnabled != 0);

        mLsExtra = (PreferenceCategory) findPreference(LOCKSCREEN_EXTRA);
        mFODIconPicker = (Preference) findPreference(FOD_ICON_PICKER_CATEGORY);
        mFODRecognitionAnimation = (SwitchPreference) findPreference(FOD_RECOGNIZING_ANIMATION);
        mFODAnimationPicker = (ListPreference) findPreference(FOD_ANIM_PICKER);
        if (!getResources().getBoolean(com.android.internal.R.bool.config_needCustomFODView)) {
            if (mFODIconPicker != null) mLsExtra.removePreference(mFODIconPicker);
            if (mFODRecognitionAnimation != null) mLsExtra.removePreference(mFODRecognitionAnimation);
            if (mFODAnimationPicker != null) mLsExtra.removePreference(mFODAnimationPicker);
        } else if (!getResources().getBoolean(R.bool.config_showFODAnimationPicker)) {
            if (mFODAnimationPicker != null) mLsExtra.removePreference(mFODAnimationPicker);
        }

        updatePreferences();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mClockEnabled) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
		            LOCKSCREEN_CLOCK, value ? 1 : 0);
            mInfoEnabled.setEnabled(value);
            return true;
        } else if (preference == mInfoEnabled) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
		            LOCKSCREEN_INFO, value ? 1 : 0);
            return true;
        } else if (preference == mVisualizerEnabled) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(getContentResolver(),
		            LOCKSCREEN_VISUALIZER_ENABLED, value ? 1 : 0);
            return true;
        }
        return false;
    }

    private void updatePreferences() {
        ContentResolver resolver = getActivity().getContentResolver();

        int clockEnabled = Settings.System.getInt(resolver,
                LOCKSCREEN_CLOCK, 1);
        mClockEnabled.setChecked(clockEnabled != 0);
        int infoEnabled = Settings.System.getInt(resolver,
                LOCKSCREEN_INFO, 1);
        mInfoEnabled.setChecked(infoEnabled != 0);
        mInfoEnabled.setEnabled(clockEnabled != 0);
    }

    protected int getPreferenceScreenResId() {
        return R.xml.ion_settings_lockscreen;
    }

    @Override
    public void onPause() {
        super.onPause();

        updatePreferences();
    }

    @Override
    public void onResume() {
        super.onResume();

        updatePreferences();
    }

    @Override
    protected String getLogTag() {
        return TAG;
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
                    sir.xmlResId = R.xml.ion_settings_lockscreen;
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
