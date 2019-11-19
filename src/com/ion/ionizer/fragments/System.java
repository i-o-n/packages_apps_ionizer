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
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto; 
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.SettingsPreferenceFragment;

import com.ion.ionizer.R;
import com.ion.ionizer.preferences.SystemSettingMasterSwitchPreference;

public class System extends DashboardFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String TAG = "System";

    private static final String SHOW_CPU_INFO_KEY = "show_cpu_info";
    private static final String KEY_SCREEN_OFF_ANIMATION = "screen_off_animation";
    private static final String SCREEN_STATE_TOGGLES_ENABLE = "screen_state_toggles_enable_key";

    private SwitchPreference mShowCpuInfo;
    private ListPreference mScreenOffAnimation;
    private SystemSettingMasterSwitchPreference mEnableScreenStateToggles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentResolver resolver = getActivity().getContentResolver();

        mShowCpuInfo = (SwitchPreference) findPreference(SHOW_CPU_INFO_KEY);
        mShowCpuInfo.setChecked(Settings.Global.getInt(getActivity().getContentResolver(),
                Settings.Global.SHOW_CPU_OVERLAY, 0) == 1);
        mShowCpuInfo.setOnPreferenceChangeListener(this);

        mScreenOffAnimation = (ListPreference) findPreference(KEY_SCREEN_OFF_ANIMATION);
        int screenOffAnimation = Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_OFF_ANIMATION, 0);
        mScreenOffAnimation.setValue(Integer.toString(screenOffAnimation));
        mScreenOffAnimation.setSummary(mScreenOffAnimation.getEntry());
        mScreenOffAnimation.setOnPreferenceChangeListener(this);

        mEnableScreenStateToggles = (SystemSettingMasterSwitchPreference) findPreference(SCREEN_STATE_TOGGLES_ENABLE);
        int enabled = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.START_SCREEN_STATE_SERVICE, 0, UserHandle.USER_CURRENT);
        mEnableScreenStateToggles.setChecked(enabled != 0);
        mEnableScreenStateToggles.setOnPreferenceChangeListener(this);
    }

    private void writeCpuInfoOptions(boolean value) {
        Settings.Global.putInt(getActivity().getContentResolver(),
                Settings.Global.SHOW_CPU_OVERLAY, value ? 1 : 0);
        Intent service = (new Intent())
                .setClassName("com.android.systemui", "com.android.systemui.CPUInfoService");
        if (value) {
            getActivity().startService(service);
        } else {
            getActivity().stopService(service);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mShowCpuInfo) {
            writeCpuInfoOptions((Boolean) newValue);
            return true;
        } else if (preference == mScreenOffAnimation) {
            int value = Integer.valueOf((String) newValue);
            int index = mScreenOffAnimation.findIndexOfValue((String) newValue);
            mScreenOffAnimation.setSummary(mScreenOffAnimation.getEntries()[index]);
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_ANIMATION, value);
            return true;
        } else if (preference == mEnableScreenStateToggles) {
            boolean value = (Boolean) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.START_SCREEN_STATE_SERVICE, value ? 1 : 0, UserHandle.USER_CURRENT);
            Intent service = (new Intent())
                .setClassName("com.android.systemui", "com.android.systemui.ion.screenstate.ScreenStateService");
            if (value) {
                getActivity().stopService(service);
                getActivity().startService(service);
            } else {
                getActivity().stopService(service);
            }
            return true;
        }
        return false;
    }

    protected int getPreferenceScreenResId() {
        return R.xml.ion_settings_system;
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
