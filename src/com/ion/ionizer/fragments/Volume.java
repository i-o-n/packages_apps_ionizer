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
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.PowerManager;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.ion.ionizer.Utils;

import com.android.internal.logging.nano.MetricsProto;

public class Volume extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {
    private static final String TAG = Volume.class.getSimpleName();


    private static final String KEY_VOLUME_KEY_CURSOR_CONTROL = "volume_key_cursor_control";
    private static final String TORCH_POWER_BUTTON_GESTURE = "torch_power_button_gesture";

    private ListPreference mTorchPowerButton;
    private ListPreference mVolumeKeyCursorControl;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.ion_settings_volume);
        PreferenceScreen prefSet = getPreferenceScreen();

        final Resources res = getResources();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        // Cursor volume keys
        int cursorControlAction = Settings.System.getInt(resolver,
                Settings.System.VOLUME_KEY_CURSOR_CONTROL, 0);
        mVolumeKeyCursorControl = initActionList(KEY_VOLUME_KEY_CURSOR_CONTROL,
                cursorControlAction);

        if (!Utils.deviceSupportsFlashLight(getContext())) {
            Preference powerTorch = prefScreen.findPreference(TORCH_POWER_BUTTON_GESTURE);
            if (powerTorch != null) {
                prefScreen.removePreference(powerTorch);
            }
        } else {
            mTorchPowerButton = (ListPreference) findPreference(TORCH_POWER_BUTTON_GESTURE);
            int mTorchPowerButtonValue = Settings.Secure.getInt(resolver,
                    Settings.Secure.TORCH_POWER_BUTTON_GESTURE, 0);
            mTorchPowerButton.setValue(Integer.toString(mTorchPowerButtonValue));
            mTorchPowerButton.setSummary(mTorchPowerButton.getEntry());
            mTorchPowerButton.setOnPreferenceChangeListener(this);
        }
    }

    private ListPreference initActionList(String key, int value) {
        ListPreference list = (ListPreference) getPreferenceScreen().findPreference(key);
        list.setValue(Integer.toString(value));
        list.setSummary(list.getEntry());
        list.setOnPreferenceChangeListener(this);
        return list;
    }

    private void handleActionListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);

        pref.setSummary(pref.getEntries()[index]);
        Settings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mVolumeKeyCursorControl) {
            handleActionListChange(mVolumeKeyCursorControl, objValue,
                    Settings.System.VOLUME_KEY_CURSOR_CONTROL);
            return true;
        } else if (preference == mTorchPowerButton) {
            int mTorchPowerButtonValue = Integer.valueOf((String) objValue);
            int index = mTorchPowerButton.findIndexOfValue((String) objValue);
            mTorchPowerButton.setSummary(
                    mTorchPowerButton.getEntries()[index]);
            Settings.Secure.putInt(resolver, Settings.Secure.TORCH_POWER_BUTTON_GESTURE,
                    mTorchPowerButtonValue);
            if (mTorchPowerButtonValue == 1) {
                //if doubletap for torch is enabled, switch off double tap for camera
                Settings.Secure.putInt(resolver, Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED, 1);
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
