/*
 * Copyright (C) 2019-2020 ion-OS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ion.ionizer.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.widget.Switch;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.widget.SwitchBar;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import com.ion.ionizer.colorpicker.ColorPickerPreference;
import com.ion.ionizer.preferences.SecureSettingSwitchPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class Visualizer extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener, Indexable, SwitchBar.OnSwitchChangeListener {

    private static final String KEY_AUTOCOLOR = "lockscreen_visualizer_autocolor";
    private static final String KEY_LAVALAMP = "lockscreen_lavalamp_enabled";
    private static final String KEY_COLOR = "lockscreen_visualizer_color";
    private static final String LINES_CATEGORY = "lockscreen_solid_lines_category";
    private static final String AMBIENT_VISUALIZER = "ambient_visualizer";

    private static final int DEFAULT_COLOR = 0xffffffff;

    private SwitchPreference mAutoColor;
    private SwitchPreference mLavaLamp;
    private ColorPickerPreference mColor;
    private PreferenceCategory mSolidLines;
    private SecureSettingSwitchPreference mAmbientVisualizer;

    private SwitchBar mSwitchBar;

    @Override
    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);

        final boolean isChecked = Settings.Secure.getInt(getActivity().getContentResolver(),
                Settings.Secure.LOCKSCREEN_VISUALIZER_ENABLED, 0) != 0;
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
        Settings.Secure.putInt(getActivity().getContentResolver(),
                Settings.Secure.LOCKSCREEN_VISUALIZER_ENABLED, isChecked ? 1 : 0);
        updatePreferences();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.visualizer_settings);

        ContentResolver resolver = getActivity().getContentResolver();

        mAutoColor = (SwitchPreference) findPreference(KEY_AUTOCOLOR);
        mLavaLamp = (SwitchPreference) findPreference(KEY_LAVALAMP);
        mLavaLamp.setOnPreferenceChangeListener(this);

        mColor = (ColorPickerPreference) findPreference(KEY_COLOR);
        mColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.LOCKSCREEN_VISUALIZER_COLOR, DEFAULT_COLOR);
        String hexColor = String.format("#%08x", (DEFAULT_COLOR & intColor));
        mColor.setSummary(hexColor);
        mColor.setNewPreviewColor(intColor);

        mSolidLines = (PreferenceCategory) findPreference(LINES_CATEGORY);
        mAmbientVisualizer = (SecureSettingSwitchPreference) findPreference(AMBIENT_VISUALIZER);

        updatePreferences();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mLavaLamp) {
            boolean isChecked = Settings.Secure.getInt(getActivity().getContentResolver(),
                    Settings.Secure.LOCKSCREEN_VISUALIZER_ENABLED, 0) != 0;
            boolean mLavaLampEnabled = (Boolean) newValue;
            if (mLavaLampEnabled) {
                mAutoColor.setSummary(getActivity().getString(
                        R.string.lockscreen_autocolor_lavalamp));
            } else {
                mAutoColor.setSummary(getActivity().getString(
                        R.string.lockscreen_autocolor_summary));
            }
            mAutoColor.setEnabled(!mLavaLampEnabled && isChecked);
            return true;
        } else if (preference == mColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.Secure.putInt(resolver,
                    Settings.Secure.LOCKSCREEN_VISUALIZER_COLOR, intHex);
            return true;
        }
        return false;
    }

    private void updatePreferences() {
        boolean isChecked = Settings.Secure.getInt(getActivity().getContentResolver(),
                Settings.Secure.LOCKSCREEN_VISUALIZER_ENABLED, 0) != 0;
        boolean mLavaLampEnabled = Settings.Secure.getIntForUser(getActivity().getContentResolver(),
                Settings.Secure.LOCKSCREEN_LAVALAMP_ENABLED, 1,
                UserHandle.USER_CURRENT) != 0;

        mAutoColor.setEnabled(!mLavaLampEnabled && isChecked);
        if (mLavaLampEnabled) {
            mAutoColor.setSummary(getActivity().getString(
                    R.string.lockscreen_autocolor_lavalamp));
        } else {
            mAutoColor.setSummary(getActivity().getString(
                    R.string.lockscreen_autocolor_summary));
        }
        mLavaLamp.setEnabled(isChecked);
        mSolidLines.setEnabled(isChecked);
        mAmbientVisualizer.setEnabled(isChecked);
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
                    sir.xmlResId = R.xml.visualizer_settings;
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
