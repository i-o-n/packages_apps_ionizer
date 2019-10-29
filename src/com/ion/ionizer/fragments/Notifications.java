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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto; 
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.SettingsPreferenceFragment;

import com.ion.ionizer.preferences.Utils;
import com.ion.ionizer.preferences.GlobalSettingMasterSwitchPreference;
import com.ion.ionizer.preferences.SystemSettingMasterSwitchPreference;
import com.ion.ionizer.preferences.CustomSeekBarPreference;

import com.ion.ionizer.R;

public class Notifications extends DashboardFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String TAG = "Notifications";

    private static final String HEADS_UP_NOTIFICATIONS_ENABLED = "heads_up_notifications_enabled";
    private static final String INCALL_VIB_OPTIONS = "incall_vib_options";
    private static final String BATTERY_LIGHT_ENABLED = "battery_light_enabled";
    private static final String BATTERY_LED = "battery_led_category";
    private static final String KEY_PULSE_BRIGHTNESS = "ambient_pulse_brightness";
    private static final String KEY_DOZE_BRIGHTNESS = "ambient_doze_brightness";

    private GlobalSettingMasterSwitchPreference mHeadsUpEnabled;
    private SystemSettingMasterSwitchPreference mBatteryLightx;
    private CustomSeekBarPreference mPulseBrightness;
    private CustomSeekBarPreference mDozeBrightness;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceScreen prefScreen = getPreferenceScreen();

        PreferenceCategory incallVibCategory = (PreferenceCategory) findPreference(INCALL_VIB_OPTIONS);
        if (!Utils.isVoiceCapable(getActivity())) {
            prefScreen.removePreference(incallVibCategory);
        }

        mHeadsUpEnabled = (GlobalSettingMasterSwitchPreference) findPreference(HEADS_UP_NOTIFICATIONS_ENABLED);
        mHeadsUpEnabled.setOnPreferenceChangeListener(this);
        int headsUpEnabled = Settings.Global.getInt(getContentResolver(),
                HEADS_UP_NOTIFICATIONS_ENABLED, 1);
        mHeadsUpEnabled.setChecked(headsUpEnabled != 0);
        if (headsUpEnabled != 0) {
            mHeadsUpEnabled.setSummary(getActivity().getString(
                        R.string.summary_heads_up_enabled));
        } else {
            mHeadsUpEnabled.setSummary(getActivity().getString(
                        R.string.summary_heads_up_disabled));
        }

        mBatteryLightx = (SystemSettingMasterSwitchPreference) findPreference(BATTERY_LIGHT_ENABLED);
        mBatteryLightx.setOnPreferenceChangeListener(this);
        int batteryLight = Settings.System.getInt(getContentResolver(),
                BATTERY_LIGHT_ENABLED, 1);
        mBatteryLightx.setChecked(batteryLight != 0);

        PreferenceCategory batteryLightxx = (PreferenceCategory) findPreference(BATTERY_LED);
        if (!getResources().getBoolean(
                        com.android.internal.R.bool.config_intrusiveBatteryLed)) {
            prefScreen.removePreference(batteryLightxx);
        }

        int defaultDoze = getResources().getInteger(
                com.android.internal.R.integer.config_screenBrightnessDoze);
        int defaultPulse = getResources().getInteger(
                com.android.internal.R.integer.config_screenBrightnessPulse);
        if (defaultPulse == -1) {
            defaultPulse = defaultDoze;
        }

        mPulseBrightness = (CustomSeekBarPreference) findPreference(KEY_PULSE_BRIGHTNESS);
        int value = Settings.System.getInt(getContentResolver(),
                Settings.System.PULSE_BRIGHTNESS, defaultPulse);
        mPulseBrightness.setValue(value);
        mPulseBrightness.setDefaultValue(defaultPulse, true);
        mPulseBrightness.setOnPreferenceChangeListener(this);

        mDozeBrightness = (CustomSeekBarPreference) findPreference(KEY_DOZE_BRIGHTNESS);
        value = Settings.System.getInt(getContentResolver(),
                Settings.System.DOZE_BRIGHTNESS, defaultDoze);
        mDozeBrightness.setValue(value);
        mDozeBrightness.setDefaultValue(defaultDoze, true);
        mDozeBrightness.setOnPreferenceChangeListener(this);
   }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mHeadsUpEnabled) {
            boolean value = (Boolean) objValue;
            Settings.Global.putInt(getContentResolver(),
		            HEADS_UP_NOTIFICATIONS_ENABLED, value ? 1 : 0);
            if (value) {
                mHeadsUpEnabled.setSummary(getActivity().getString(
                            R.string.summary_heads_up_enabled));
            } else {
                mHeadsUpEnabled.setSummary(getActivity().getString(
                            R.string.summary_heads_up_disabled));
            }
            return true;
        } else if (preference == mBatteryLightx) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(),
		            BATTERY_LIGHT_ENABLED, value ? 1 : 0);
            return true;
        } else if (preference == mPulseBrightness) {
            int value = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PULSE_BRIGHTNESS, value);
            return true;
        } else if (preference == mDozeBrightness) {
            int value = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.DOZE_BRIGHTNESS, value);
            return true;
        }
        return false;
    }

    protected int getPreferenceScreenResId() {
        return R.xml.ion_settings_notifications;
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
