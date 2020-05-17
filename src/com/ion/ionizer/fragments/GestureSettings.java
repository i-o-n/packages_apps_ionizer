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
public class GestureSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener, Indexable {

    public static final String TAG = "GesturesSettings";

    private static final String KEY_CATEGORY_LEFT_SWIPE    = "left_swipe";
    private static final String KEY_CATEGORY_RIGHT_SWIPE   = "right_swipe";
    private static final String KEY_NAVIGATION_IME_SPACE = "navigation_bar_ime_space";

    private ContentResolver mResolver;
    private ListPreference mLeftSwipeActions;
    private ListPreference mRightSwipeActions;
    private Preference mLeftSwipeAppSelection;
    private Preference mRightSwipeAppSelection;
    private PreferenceCategory leftSwipeCategory;
    private PreferenceCategory rightSwipeCategory;
    private SystemSettingListPreference mTimeout;
    private SystemSettingListPreference mBackSwipeType;
    private SystemSettingSwitchPreference mNavigationIMESpace;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.ion_settings_gesture);
        final PreferenceScreen prefScreen = getPreferenceScreen();
        mFooterPreferenceMixin.createFooterPreference().setTitle(R.string.gesture_nav_tweaks_footer_info);
        mResolver = getActivity().getContentResolver();

        leftSwipeCategory = (PreferenceCategory) findPreference(KEY_CATEGORY_LEFT_SWIPE);
        rightSwipeCategory = (PreferenceCategory) findPreference(KEY_CATEGORY_RIGHT_SWIPE);

        int leftSwipeActions = Settings.System.getIntForUser(mResolver,
                Settings.System.LEFT_LONG_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);
        mLeftSwipeActions = (ListPreference) findPreference("left_swipe_actions");
        mLeftSwipeActions.setValue(Integer.toString(leftSwipeActions));
        mLeftSwipeActions.setSummary(mLeftSwipeActions.getEntry());
        mLeftSwipeActions.setOnPreferenceChangeListener(this);

        int rightSwipeActions = Settings.System.getIntForUser(mResolver,
                Settings.System.RIGHT_LONG_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);
        mRightSwipeActions = (ListPreference) findPreference("right_swipe_actions");
        mRightSwipeActions.setValue(Integer.toString(rightSwipeActions));
        mRightSwipeActions.setSummary(mRightSwipeActions.getEntry());
        mRightSwipeActions.setOnPreferenceChangeListener(this);

        mLeftSwipeAppSelection = (Preference) findPreference("left_swipe_app_action");
        boolean isAppSelection = Settings.System.getIntForUser(mResolver,
                Settings.System.LEFT_LONG_BACK_SWIPE_ACTION, 0, UserHandle.USER_CURRENT) == 5/*action_app_action*/;
        mLeftSwipeAppSelection.setEnabled(isAppSelection);

        mRightSwipeAppSelection = (Preference) findPreference("right_swipe_app_action");
        isAppSelection = Settings.System.getIntForUser(mResolver,
                Settings.System.RIGHT_LONG_BACK_SWIPE_ACTION, 0, UserHandle.USER_CURRENT) == 5/*action_app_action*/;
        mRightSwipeAppSelection.setEnabled(isAppSelection);
        customAppCheck();

        mTimeout = (SystemSettingListPreference) findPreference("long_back_swipe_timeout");

        mBackSwipeType = (SystemSettingListPreference) findPreference("back_swipe_type");
        int swipeType = Settings.System.getIntForUser(mResolver,
                Settings.System.BACK_SWIPE_TYPE, 0, UserHandle.USER_CURRENT);
        mBackSwipeType.setValue(String.valueOf(swipeType));
        mBackSwipeType.setSummary(mBackSwipeType.getEntry());
        mBackSwipeType.setOnPreferenceChangeListener(this);
        mTimeout.setEnabled(swipeType == 0);

        mLeftSwipeAppSelection.setVisible(mLeftSwipeActions.getEntryValues()
                [leftSwipeActions].equals("5"));
        mLeftSwipeAppSelection.setVisible(mRightSwipeActions.getEntryValues()
                [rightSwipeActions].equals("5"));

        mNavigationIMESpace = (SystemSettingSwitchPreference) findPreference(KEY_NAVIGATION_IME_SPACE);
        mNavigationIMESpace.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mLeftSwipeActions) {
            int leftSwipeActions = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.LEFT_LONG_BACK_SWIPE_ACTION, leftSwipeActions,
                    UserHandle.USER_CURRENT);
            int index = mLeftSwipeActions.findIndexOfValue((String) newValue);
            mLeftSwipeActions.setSummary(
                    mLeftSwipeActions.getEntries()[index]);
            mLeftSwipeAppSelection.setEnabled(leftSwipeActions == 5);
            actionPreferenceReload();
            customAppCheck();
            return true;
        } else if (preference == mRightSwipeActions) {
            int rightSwipeActions = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.RIGHT_LONG_BACK_SWIPE_ACTION, rightSwipeActions,
                    UserHandle.USER_CURRENT);
            int index = mRightSwipeActions.findIndexOfValue((String) newValue);
            mRightSwipeActions.setSummary(
                    mRightSwipeActions.getEntries()[index]);
            mRightSwipeAppSelection.setEnabled(rightSwipeActions == 5);
            actionPreferenceReload();
            customAppCheck();
            return true;
        } else if (preference == mBackSwipeType) {
            int swipeType = Integer.parseInt((String) newValue);
            int index = mBackSwipeType.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.BACK_SWIPE_TYPE, swipeType);
            mBackSwipeType.setSummary(mBackSwipeType.getEntries()[index]);
            mTimeout.setEnabled(swipeType == 0);
            return true;
        } else if (preference == mNavigationIMESpace) {
            SystemNavigationGestureSettings.updateNavigationBarOverlays(getActivity());
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        customAppCheck();
        actionPreferenceReload();
    }

    @Override
    public void onPause() {
        super.onPause();
        customAppCheck();
        actionPreferenceReload();
    }

    private void customAppCheck() {
        mLeftSwipeAppSelection.setSummary(Settings.System.getStringForUser(getActivity().getContentResolver(),
                String.valueOf(Settings.System.LEFT_LONG_BACK_SWIPE_APP_FR_ACTION), UserHandle.USER_CURRENT));
        mRightSwipeAppSelection.setSummary(Settings.System.getStringForUser(getActivity().getContentResolver(),
                String.valueOf(Settings.System.RIGHT_LONG_BACK_SWIPE_APP_FR_ACTION), UserHandle.USER_CURRENT));
    }

    private void actionPreferenceReload() {
        int leftSwipeActions = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.LEFT_LONG_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);
        int rightSwipeActions = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.RIGHT_LONG_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);

        // Reload the action preferences
        mLeftSwipeActions.setValue(Integer.toString(leftSwipeActions));
        mLeftSwipeActions.setSummary(mLeftSwipeActions.getEntry());

        mRightSwipeActions.setValue(Integer.toString(rightSwipeActions));
        mRightSwipeActions.setSummary(mRightSwipeActions.getEntry());

        mLeftSwipeAppSelection.setVisible(mLeftSwipeActions.getEntryValues()
                [leftSwipeActions].equals("5"));
        mRightSwipeAppSelection.setVisible(mRightSwipeActions.getEntryValues()
                [rightSwipeActions].equals("5"));
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
                    sir.xmlResId = R.xml.ion_settings_gesture;
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
