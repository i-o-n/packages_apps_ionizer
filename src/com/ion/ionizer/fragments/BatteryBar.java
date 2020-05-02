/*
 * Copyright (C) 2020 ion-OS
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

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import com.ion.ionizer.colorpicker.ColorPickerPreference;
import com.ion.ionizer.R;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class BatteryBar extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener, Indexable {

    private static final String PREF_BATTERY_BAR_LOCATION = "battery_bar_list";
    private static final String PREF_BATTERY_BAR_STYLE = "battery_bar_style";
    private static final String PREF_BATTERY_BAR_COLOR = "battery_bar_color";
    private static final String PREF_BATTERY_BAR_CHARGING_COLOR = "battery_bar_charging_color";
    private static final String PREF_BATTERY_BAR_LOW_COLOR_WARNING = "battery_bar_battery_low_color_warning";
    private static final String PREF_BATTERY_BAR_LOW_COLOR = "battery_bar_low_color";
    private static final String PREF_BATTERY_BAR_HIGH_COLOR = "battery_bar_high_color";

    private ListPreference mBatteryBarLocation;
    private ListPreference mBatteryBarStyle;
    private ColorPickerPreference mBatteryBarColor;
    private ColorPickerPreference mBatteryBarChargingColor;
    private ColorPickerPreference mBatteryBarLowColorWarning;
    private ColorPickerPreference mBatteryBarLowColor;
    private ColorPickerPreference mBatteryBarHighColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.battery_bar);

        ContentResolver resolver = getActivity().getContentResolver();

        mBatteryBarLocation = (ListPreference) findPreference(PREF_BATTERY_BAR_LOCATION);
        mBatteryBarLocation.setOnPreferenceChangeListener(this);
        mBatteryBarLocation.setValue((Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_LOCATION, 0)) + "");
        mBatteryBarLocation.setSummary(mBatteryBarLocation.getEntry());

        mBatteryBarStyle = (ListPreference) findPreference(PREF_BATTERY_BAR_STYLE);
        mBatteryBarStyle.setOnPreferenceChangeListener(this);
        mBatteryBarStyle.setValue((Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_STYLE, 0)) + "");
        mBatteryBarStyle.setSummary(mBatteryBarStyle.getEntry());

        mBatteryBarColor = (ColorPickerPreference) findPreference(PREF_BATTERY_BAR_COLOR);
        mBatteryBarColor.setOnPreferenceChangeListener(this);
        int batteryBarColor = Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_COLOR, 0xFFFFFFFF);
        String batteryBarColorHex = String.format("#%08x", (0xFFFFFFFF & batteryBarColor));
        if (batteryBarColorHex.equals("#ffffffff"))
            mBatteryBarColor.setSummary(R.string.default_string);
        else
            mBatteryBarColor.setSummary(batteryBarColorHex);
        mBatteryBarColor.setNewPreviewColor(batteryBarColor);

        mBatteryBarChargingColor = (ColorPickerPreference) findPreference(PREF_BATTERY_BAR_CHARGING_COLOR);
        mBatteryBarChargingColor.setOnPreferenceChangeListener(this);
        int batteryBarChargingColor = Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_CHARGING_COLOR, 0xFF00FF00);
        String batteryBarChargingColorHex = String.format("#%08x", (0xFF00FF00 & batteryBarChargingColor));
        if (batteryBarChargingColorHex.equals("#ff00ff00"))
            mBatteryBarChargingColor.setSummary(R.string.default_string);
        else
            mBatteryBarChargingColor.setSummary(batteryBarChargingColorHex);
        mBatteryBarChargingColor.setNewPreviewColor(batteryBarChargingColor);

        mBatteryBarLowColorWarning = (ColorPickerPreference) findPreference(PREF_BATTERY_BAR_LOW_COLOR_WARNING);
        mBatteryBarLowColorWarning.setOnPreferenceChangeListener(this);
        int batteryBarLowColorWarning = Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_BATTERY_LOW_COLOR_WARNING, 0xFFFF6600);
        String batteryBarLowColorWarningHex = String.format("#%08x", (0xFFFF6600 & batteryBarLowColorWarning));
        if (batteryBarLowColorWarningHex.equals("#ffff6600"))
            mBatteryBarLowColorWarning.setSummary(R.string.default_string);
        else
            mBatteryBarLowColorWarning.setSummary(batteryBarLowColorWarningHex);
        mBatteryBarLowColorWarning.setNewPreviewColor(batteryBarLowColorWarning);

        mBatteryBarLowColor = (ColorPickerPreference) findPreference(PREF_BATTERY_BAR_LOW_COLOR);
        mBatteryBarLowColor.setOnPreferenceChangeListener(this);
        int batteryBarLowColor = Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_LOW_COLOR, 0xFFFF4400);
        String batteryBarLowColorHex = String.format("#%08x", (0xFFFF4400 & batteryBarLowColor));
        if (batteryBarLowColorHex.equals("#ffff4400"))
            mBatteryBarLowColor.setSummary(R.string.default_string);
        else
            mBatteryBarLowColor.setSummary(batteryBarLowColorHex);
        mBatteryBarLowColor.setNewPreviewColor(batteryBarLowColor);

        mBatteryBarHighColor = (ColorPickerPreference) findPreference(PREF_BATTERY_BAR_HIGH_COLOR);
        mBatteryBarHighColor.setOnPreferenceChangeListener(this);
        int batteryBarHighColor = Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_HIGH_COLOR, 0xFF99CC00);
        String batteryBarHighColorHex = String.format("#%08x", (0xFF99CC00 & batteryBarHighColor));
        if (batteryBarHighColorHex.equals("#ff99cc00"))
            mBatteryBarHighColor.setSummary(R.string.default_string);
        else
            mBatteryBarHighColor.setSummary(batteryBarHighColorHex);
        mBatteryBarHighColor.setNewPreviewColor(batteryBarHighColor);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mBatteryBarLocation) {
            int val = Integer.valueOf((String) newValue);
            int index = mBatteryBarLocation.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_LOCATION, val);
            mBatteryBarLocation.setSummary(mBatteryBarLocation.getEntries()[index]);
            return true;
        } else if (preference == mBatteryBarStyle) {
            int val = Integer.valueOf((String) newValue);
            int index = mBatteryBarStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_STYLE, val);
            mBatteryBarStyle.setSummary(mBatteryBarStyle.getEntries()[index]);
            return true;
        } else if (preference == mBatteryBarColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#ffffffff"))
                preference.setSummary(R.string.default_string);
            else
                preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_COLOR, intHex);
            return true;
        } else if (preference == mBatteryBarChargingColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#ff00ff00"))
                preference.setSummary(R.string.default_string);
            else
                preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_CHARGING_COLOR, intHex);
            return true;
        } else if (preference == mBatteryBarLowColorWarning) {
            String hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#ffff6600"))
                preference.setSummary(R.string.default_string);
            else
                preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_BATTERY_LOW_COLOR_WARNING, intHex);
            return true;
        } else if (preference == mBatteryBarLowColor) {
            String hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#ffff4400"))
                preference.setSummary(R.string.default_string);
            else
                preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_LOW_COLOR, intHex);
            return true;
        } else if (preference == mBatteryBarHighColor) {
            String hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#ff99cc00"))
                preference.setSummary(R.string.default_string);
            else
                preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_HIGH_COLOR, intHex);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ION_IONIZER;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.battery_bar;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
    };
}