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
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.hwkeys.ActionUtils;
import com.android.internal.util.ion.IonUtils;
import com.android.settings.gestures.SystemNavigationGestureSettings;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import com.ion.ionizer.R;
import com.ion.ionizer.preferences.SystemSettingListPreference;
import com.ion.ionizer.preferences.SystemSettingSwitchPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class Navigation extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener, Indexable {

    public static final String TAG = "Navigation";

    private static final String PREF_HW_BUTTONS = "hw_buttons";
    private static final String ENABLE_NAV_BAR = "enable_nav_bar";
    private static final String NAV_BAR_LAYOUT = "nav_bar_layout";
    private static final String SYSUI_NAV_BAR = "sysui_nav_bar";
    private static final String NAVBAR_ARROW_KEYS = "navigation_bar_menu_arrow_keys";
    private static final String GESTURE_SETTINGS = "gesture_settings";

    private Preference mGestureSettings;
    private ListPreference mNavBarLayout;
    private SwitchPreference mEnableNavigationBar;
    private SwitchPreference mNavbarArrowKeys;
    private boolean isButtonMode = false;
    private boolean mIsNavSwitchingMode = false;
    private ContentResolver mResolver;
    private Handler mHandler;


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.ion_settings_navigation);
        final PreferenceScreen prefScreen = getPreferenceScreen();
        mFooterPreferenceMixin.createFooterPreference().setTitle(R.string.recents_style_info_title);
        mResolver = getActivity().getContentResolver();

        final boolean needsNavbar = ActionUtils.hasNavbarByDefault(getActivity());
        // bits for hardware keys present on device
        final int deviceKeys = getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
        if (needsNavbar && deviceKeys == 0) {
            getPreferenceScreen().removePreference(findPreference(PREF_HW_BUTTONS));
        }

        // Navigation bar related options
        mEnableNavigationBar = (SwitchPreference) findPreference(ENABLE_NAV_BAR);

        // Only visible on devices that have a navigation bar already
        if (ActionUtils.hasNavbarByDefault(getActivity())) {
            mEnableNavigationBar.setOnPreferenceChangeListener(this);
            mHandler = new Handler();
            updateNavBarOption();
        } else {
            prefScreen.removePreference(mEnableNavigationBar);
        }

        // check if button mode navigation is in use
        isButtonMode = IonUtils.isThemeEnabled("com.android.internal.systemui.navbar.twobutton")
                || IonUtils.isThemeEnabled("com.android.internal.systemui.navbar.threebutton");

        mGestureSettings = (Preference) findPreference(GESTURE_SETTINGS);
        mGestureSettings.setEnabled(!isButtonMode);

        mNavBarLayout = (ListPreference) findPreference(NAV_BAR_LAYOUT);
        mNavBarLayout.setOnPreferenceChangeListener(this);
        String navBarLayoutValue = Settings.Secure.getString(mResolver, SYSUI_NAV_BAR);
        if (navBarLayoutValue != null) {
            mNavBarLayout.setValue(navBarLayoutValue);
        } else {
            mNavBarLayout.setValueIndex(0);
        }
        mNavBarLayout.setEnabled(isButtonMode);

        mNavbarArrowKeys = (SwitchPreference) findPreference(NAVBAR_ARROW_KEYS);
        mNavbarArrowKeys.setOnPreferenceChangeListener(this);
        refreshNavbarArrowKeysOption();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mEnableNavigationBar) {
            if (mIsNavSwitchingMode) {
                return false;
            }
            mIsNavSwitchingMode = true;
            boolean isNavBarChecked = ((Boolean) newValue);
            mEnableNavigationBar.setEnabled(false);
            writeNavBarOption(isNavBarChecked);
            updateNavBarOption();
            mEnableNavigationBar.setEnabled(true);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsNavSwitchingMode = false;
                }
            }, 1000);
            mNavBarLayout.setEnabled(isButtonMode && isNavBarChecked);
            return true;
        } else if (preference == mNavBarLayout) {
            Settings.Secure.putString(mResolver, SYSUI_NAV_BAR, (String) newValue);
            return true;
        } else if (preference == mNavbarArrowKeys) {
            SystemNavigationGestureSettings.updateNavigationBarOverlays(getActivity());
            return true;
        }
        updatePreferences();
        return false;
    }

    private void writeNavBarOption(boolean enabled) {
        Settings.System.putIntForUser(getActivity().getContentResolver(),
                Settings.System.FORCE_SHOW_NAVBAR, enabled ? 1 : 0, UserHandle.USER_CURRENT);
    }

    private void updateNavBarOption() {
        boolean enabled = Settings.System.getIntForUser(getActivity().getContentResolver(),
                Settings.System.FORCE_SHOW_NAVBAR, 1, UserHandle.USER_CURRENT) != 0;
        mEnableNavigationBar.setChecked(enabled);
    }

    private boolean showIMEspace() {
        return Settings.System.getIntForUser(getActivity().getContentResolver(),
                Settings.System.NAVIGATION_BAR_IME_SPACE, 1, UserHandle.USER_CURRENT) != 0;
    }

    private void refreshNavbarArrowKeysOption() {
        mNavbarArrowKeys.setEnabled(showIMEspace() || isButtonMode);
    }

    private void updatePreferences() {
        mGestureSettings.setEnabled(!isButtonMode);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshNavbarArrowKeysOption();
    }

    @Override
    public void onPause() {
        super.onPause();
        refreshNavbarArrowKeysOption();
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
                    sir.xmlResId = R.xml.ion_settings_navigation;
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
