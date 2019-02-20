/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.ion.ionizer.display;

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.pm.PackageManager.NameNotFoundException;

import android.os.Bundle;
import android.text.TextUtils;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settings.core.PreferenceControllerMixin;
import com.ion.ionizer.preferences.SystemSettingSeekBarPreference;

import libcore.util.Objects;
import java.util.ArrayList;
import java.util.List;


public class RoundedCornersPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String SYSUI_ROUNDED_SIZE = "sysui_rounded_size";
    private SystemSettingSeekBarPreference mCornerRadius;

    public RoundedCornersPreferenceController(Context context) {
        super(context);
    }

    @Override
    public String getPreferenceKey() {
        return SYSUI_ROUNDED_SIZE;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mCornerRadius = (SystemSettingSeekBarPreference) screen.findPreference(SYSUI_ROUNDED_SIZE);
        Resources res = null;
        try {
            res = mContext.getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        int resourceId = res.getIdentifier("com.android.systemui:dimen/rounded_corner_radius", null, null);
        int cornerRadius = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.SYSUI_ROUNDED_SIZE,
                    res.getDimensionPixelSize(resourceId));
                mCornerRadius.setValue(cornerRadius / 1);
                mCornerRadius.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mCornerRadius) {
            int value = (Integer) newValue;
            Settings.Secure.putInt(mContext.getContentResolver(),
                    Settings.Secure.SYSUI_ROUNDED_SIZE, value * 1);
        }
        return true;
    }
}
