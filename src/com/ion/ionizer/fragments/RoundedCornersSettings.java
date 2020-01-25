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

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.ion.IonUtils;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import com.ion.ionizer.R;
import com.ion.ionizer.preferences.AppMultiSelectListPreference;
import com.ion.ionizer.preferences.CustomSeekBarPreference;
import com.ion.ionizer.preferences.ScrollAppsViewPreference;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class RoundedCornersSettings extends SettingsPreferenceFragment implements
         OnPreferenceChangeListener {

    private static final String SYSUI_ROUNDED_SIZE = "sysui_rounded_size";
    private static final String SYSUI_ROUNDED_CONTENT_PADDING = "sysui_rounded_content_padding";
    private static final String SYSUI_STATUS_BAR_PADDING = "sysui_status_bar_padding";
    private static final String SYSUI_ROUNDED_FWVALS = "sysui_rounded_fwvals";
    private static final String KEY_FORCE_FULLSCREEN = "display_cutout_force_fullscreen_settings";
    private static final String KEY_ASPECT_RATIO_APPS_ENABLED = "aspect_ratio_apps_enabled";
    private static final String KEY_ASPECT_RATIO_APPS_LIST = "aspect_ratio_apps_list";
    private static final String KEY_ASPECT_RATIO_CATEGORY = "aspect_ratio_category";
    private static final String KEY_ASPECT_RATIO_APPS_LIST_SCROLLER = "aspect_ratio_apps_list_scroller";

    private CustomSeekBarPreference mCornerRadius;
    private CustomSeekBarPreference mContentPadding;
    private CustomSeekBarPreference mSBPadding;
    private SwitchPreference mRoundedFwvals;
    private AppMultiSelectListPreference mAspectRatioAppsSelect;
    private ScrollAppsViewPreference mAspectRatioApps;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.rounded_corners);

        Resources res = null;
        Context ctx = getContext();
        float density = Resources.getSystem().getDisplayMetrics().density;

        try {
            res = ctx.getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        // Rounded Corner Radius
        mCornerRadius = (CustomSeekBarPreference) findPreference(SYSUI_ROUNDED_SIZE);
        int resourceIdRadius = (int) ctx.getResources().getDimension(com.android.internal.R.dimen.rounded_corner_radius);
        int cornerRadius = Settings.Secure.getIntForUser(ctx.getContentResolver(), Settings.Secure.SYSUI_ROUNDED_SIZE,
                ((int) (resourceIdRadius / density)), UserHandle.USER_CURRENT);
        mCornerRadius.setValue(cornerRadius);
        mCornerRadius.setOnPreferenceChangeListener(this);

        // Rounded Content Padding
        mContentPadding = (CustomSeekBarPreference) findPreference(SYSUI_ROUNDED_CONTENT_PADDING);
        int resourceIdPadding = res.getIdentifier("com.android.systemui:dimen/rounded_corner_content_padding", null,
                null);
        int contentPadding = Settings.Secure.getIntForUser(ctx.getContentResolver(),
                Settings.Secure.SYSUI_ROUNDED_CONTENT_PADDING,
                (int) (res.getDimension(resourceIdPadding) / density), UserHandle.USER_CURRENT);
        mContentPadding.setValue(contentPadding);
        mContentPadding.setOnPreferenceChangeListener(this);

        // Status Bar Content Padding
        mSBPadding = (CustomSeekBarPreference) findPreference(SYSUI_STATUS_BAR_PADDING);
        int resourceIdSBPadding = res.getIdentifier("com.android.systemui:dimen/status_bar_extra_padding", null,
                null);
        int sbPadding = Settings.Secure.getIntForUser(ctx.getContentResolver(),
                Settings.Secure.SYSUI_STATUS_BAR_PADDING,
                (int) (res.getDimension(resourceIdSBPadding) / density), UserHandle.USER_CURRENT);
        mSBPadding.setValue(sbPadding);
        mSBPadding.setOnPreferenceChangeListener(this);

        // Rounded use Framework Values
        mRoundedFwvals = (SwitchPreference) findPreference(SYSUI_ROUNDED_FWVALS);
        mRoundedFwvals.setOnPreferenceChangeListener(this);

        final Preference forceFullscreen = (Preference) getPreferenceScreen().findPreference(KEY_FORCE_FULLSCREEN);
        if (!IonUtils.hasNotch(getContext())) {
            getPreferenceScreen().removePreference(forceFullscreen);
        }

        final PreferenceCategory aspectRatioCategory =
                (PreferenceCategory) getPreferenceScreen().findPreference(KEY_ASPECT_RATIO_CATEGORY);
        final boolean supportMaxAspectRatio =
                getResources().getBoolean(com.android.internal.R.bool.config_haveHigherAspectRatioScreen);
        if (!supportMaxAspectRatio) {
                getPreferenceScreen().removePreference(aspectRatioCategory);
        } else {
        mAspectRatioAppsSelect =
                (AppMultiSelectListPreference) findPreference(KEY_ASPECT_RATIO_APPS_LIST);
        mAspectRatioApps =
                (ScrollAppsViewPreference) findPreference(KEY_ASPECT_RATIO_APPS_LIST_SCROLLER);
        final String valuesString = Settings.System.getString(getContentResolver(),
                Settings.System.ASPECT_RATIO_APPS_LIST);
        List<String> valuesList = new ArrayList<String>();
        if (!TextUtils.isEmpty(valuesString)) {
            valuesList.addAll(Arrays.asList(valuesString.split(":")));
            mAspectRatioApps.setVisible(true);
            mAspectRatioApps.setValues(valuesList);
        } else {
            mAspectRatioApps.setVisible(false);
        }
        mAspectRatioAppsSelect.setValues(valuesList);
        mAspectRatioAppsSelect.setOnPreferenceChangeListener(this);
        }
    }

    private void restoreCorners() {
        Resources res = null;
        float density = Resources.getSystem().getDisplayMetrics().density;
        Context ctx = getContext();

        try {
            res = ctx.getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        int resourceIdRadius = (int) ctx.getResources().getDimension(com.android.internal.R.dimen.rounded_corner_radius);
        int resourceIdPadding = res.getIdentifier("com.android.systemui:dimen/rounded_corner_content_padding", null,
                null);
        int resourceIdSBPadding = res.getIdentifier("com.android.systemui:dimen/status_bar_extra_padding", null,
                null);
        mCornerRadius.setValue((int) (resourceIdRadius / density));
        mContentPadding.setValue((int) (res.getDimension(resourceIdPadding) / density));
        mSBPadding.setValue((int) (res.getDimension(resourceIdSBPadding) / density));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mCornerRadius) {
            Settings.Secure.putIntForUser(getContext().getContentResolver(), Settings.Secure.SYSUI_ROUNDED_SIZE,
                    (int) newValue, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mContentPadding) {
            Settings.Secure.putIntForUser(getContext().getContentResolver(), Settings.Secure.SYSUI_ROUNDED_CONTENT_PADDING,
                    (int) newValue, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mSBPadding) {
            Settings.Secure.putIntForUser(getContext().getContentResolver(), Settings.Secure.SYSUI_STATUS_BAR_PADDING,
                    (int) newValue, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mRoundedFwvals) {
            restoreCorners();
            return true;
        } else if (preference == mAspectRatioAppsSelect) {
            Collection<String> valueList = (Collection<String>) newValue;
            mAspectRatioApps.setVisible(false);
            if (valueList != null) {
                Settings.System.putString(getContentResolver(),
                        Settings.System.ASPECT_RATIO_APPS_LIST, TextUtils.join(":", valueList));
                mAspectRatioApps.setVisible(true);
                mAspectRatioApps.setValues(valueList);
            } else {
                Settings.System.putString(getContentResolver(),
                Settings.System.ASPECT_RATIO_APPS_LIST, "");
            }
            return true;
        }
        return false;
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.SYSUI_ROUNDED_FWVALS, 1, UserHandle.USER_CURRENT);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ION_IONIZER;
    }
}
