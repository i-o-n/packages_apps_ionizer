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
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.PowerManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.widget.Toast;

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
import com.ion.ionizer.preferences.SystemSettingSeekBarPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class Buttons extends DashboardFragment implements
        OnPreferenceChangeListener, Indexable {
    private static final String TAG = Buttons.class.getSimpleName();

    private static final String KEY_VOLUME_KEY_CURSOR_CONTROL = "volume_key_cursor_control";
    private static final String TORCH_POWER_BUTTON_GESTURE = "torch_power_button_gesture";
    private static final String TORCH_POWER_BUTTON_TIMEOUT = "torch_long_press_power_timeout";

    private ListPreference mVolumeKeyCursorControl;
    private ListPreference mTorchPowerButton;
    private SystemSettingSeekBarPreference mTorchPowerButtonTimeout;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        PreferenceScreen prefSet = getPreferenceScreen();

        final Resources res = getResources();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        // Cursor volume keys
        int cursorControlAction = Settings.System.getInt(resolver,
                Settings.System.VOLUME_KEY_CURSOR_CONTROL, 0);
        mVolumeKeyCursorControl = initActionList(KEY_VOLUME_KEY_CURSOR_CONTROL,
                cursorControlAction);

        // screen off torch
        mTorchPowerButton = (ListPreference) findPreference(TORCH_POWER_BUTTON_GESTURE);
        mTorchPowerButtonTimeout = (SystemSettingSeekBarPreference) findPreference(TORCH_POWER_BUTTON_TIMEOUT);
        int mTorchPowerButtonValue = Settings.Secure.getInt(resolver,
                Settings.Secure.TORCH_POWER_BUTTON_GESTURE, 0);
        if (mTorchPowerButtonValue == 0) {
            mTorchPowerButtonTimeout.setEnabled(false);
        } else {
            mTorchPowerButtonTimeout.setEnabled(true);
        }
        mTorchPowerButton.setValue(Integer.toString(mTorchPowerButtonValue));
        mTorchPowerButton.setSummary(mTorchPowerButton.getEntry());
        mTorchPowerButton.setOnPreferenceChangeListener(this);
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
        boolean DoubleTapPowerGesture = Settings.Secure.getInt(resolver,
                Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED, 0) == 0;
        boolean DoubleTapPowerGestureAvailable = getResources().getBoolean(
                com.android.internal.R.bool.config_cameraDoubleTapPowerGestureEnabled);
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
            if (mTorchPowerButtonValue == 1 && DoubleTapPowerGesture && DoubleTapPowerGestureAvailable) {
                //if doubletap for torch is enabled, switch off double tap for camera
                Settings.Secure.putInt(resolver, Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED,
                        1);
                Toast.makeText(getActivity(),
                    (R.string.torch_power_button_gesture_dt_toast),
                    Toast.LENGTH_SHORT).show();
            }
            if (mTorchPowerButtonValue != 0) {
                mTorchPowerButtonTimeout.setEnabled(true);
            } else {
                mTorchPowerButtonTimeout.setEnabled(false);
            }
            return true;
        }
        return false;
    }

    protected int getPreferenceScreenResId() {
        return R.xml.ion_settings_buttons;
    }

    @Override
    protected String getLogTag() {
        return TAG;
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
                    sir.xmlResId = R.xml.ion_settings_buttons;
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
