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
import android.text.TextUtils;

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
import com.ion.ionizer.preferences.SystemSettingSeekBarPreference;
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
    private static final String KEY_SHOW_ONLY_NAVBAR_HANDLE = "show_only_navbar_handle";

    private ContentResolver mResolver;
    private ListPreference mLeftSwipeActions;
    private ListPreference mLeftSwipeActionsHold;
    private ListPreference mRightSwipeActions;
    private ListPreference mRightSwipeActionsHold;
    private Preference mLeftSwipeAppSelection;
    private Preference mLeftSwipeAppSelectionHold;
    private Preference mRightSwipeAppSelection;
    private Preference mRightSwipeAppSelectionHold;
    private PreferenceCategory leftSwipeCategory;
    private PreferenceCategory rightSwipeCategory;
    private SystemSettingSwitchPreference mNavigationIMESpace;
    private SystemSettingSeekBarPreference mSwipeHoldDelay;
    private SystemSettingSwitchPreference mShowOnlyNavbarHandle;

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

        mLeftSwipeAppSelection.setVisible(mLeftSwipeActions.getEntryValues()
                [leftSwipeActions].equals("5"));
        mLeftSwipeAppSelection.setVisible(mRightSwipeActions.getEntryValues()
                [rightSwipeActions].equals("5"));

        mNavigationIMESpace = (SystemSettingSwitchPreference) findPreference(KEY_NAVIGATION_IME_SPACE);
        mNavigationIMESpace.setOnPreferenceChangeListener(this);

        int leftSwipeActionsHold = Settings.System.getIntForUser(mResolver,
                Settings.System.LEFT_HOLD_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);
        mLeftSwipeActionsHold = (ListPreference) findPreference("hold_left_swipe_actions");
        mLeftSwipeActionsHold.setValue(Integer.toString(leftSwipeActionsHold));
        mLeftSwipeActionsHold.setSummary(mLeftSwipeActionsHold.getEntry());
        mLeftSwipeActionsHold.setOnPreferenceChangeListener(this);

        int rightSwipeActionsHold = Settings.System.getIntForUser(mResolver,
                Settings.System.RIGHT_HOLD_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);
        mRightSwipeActionsHold = (ListPreference) findPreference("hold_right_swipe_actions");
        mRightSwipeActionsHold.setValue(Integer.toString(rightSwipeActionsHold));
        mRightSwipeActionsHold.setSummary(mRightSwipeActionsHold.getEntry());
        mRightSwipeActionsHold.setOnPreferenceChangeListener(this);

        mLeftSwipeAppSelectionHold = (Preference) findPreference("hold_left_swipe_app_action");
        isAppSelection = Settings.System.getIntForUser(mResolver,
                Settings.System.LEFT_HOLD_BACK_SWIPE_ACTION, 0, UserHandle.USER_CURRENT) == 5/*action_app_action*/;
        mLeftSwipeAppSelectionHold.setEnabled(isAppSelection);

        mRightSwipeAppSelectionHold = (Preference) findPreference("hold_right_swipe_app_action");
        isAppSelection = Settings.System.getIntForUser(mResolver,
                Settings.System.RIGHT_HOLD_BACK_SWIPE_ACTION, 0, UserHandle.USER_CURRENT) == 5/*action_app_action*/;
        mRightSwipeAppSelectionHold.setEnabled(isAppSelection);

        mLeftSwipeAppSelectionHold.setVisible(mLeftSwipeActionsHold.getEntryValues()
                [leftSwipeActionsHold].equals("5"));
        mLeftSwipeAppSelectionHold.setVisible(mRightSwipeActionsHold.getEntryValues()
                [rightSwipeActionsHold].equals("5"));

        mShowOnlyNavbarHandle = (SystemSettingSwitchPreference) findPreference(KEY_SHOW_ONLY_NAVBAR_HANDLE);
        mShowOnlyNavbarHandle.setOnPreferenceChangeListener(this);

        mSwipeHoldDelay = (SystemSettingSeekBarPreference) findPreference("long_back_swipe_timeout");
        updateSwipeHoldDelayState();
        customAppCheck();
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
        } else if (preference == mNavigationIMESpace) {
            SystemNavigationGestureSettings.updateNavigationBarOverlays(getActivity());
            return true;
        } else if (preference == mLeftSwipeActionsHold) {
            int leftSwipeActionsHold = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.LEFT_HOLD_BACK_SWIPE_ACTION, leftSwipeActionsHold,
                    UserHandle.USER_CURRENT);
            int index = mLeftSwipeActionsHold.findIndexOfValue((String) newValue);
            mLeftSwipeActionsHold.setSummary(
                    mLeftSwipeActionsHold.getEntries()[index]);
            mLeftSwipeAppSelectionHold.setEnabled(leftSwipeActionsHold == 5);
            actionPreferenceReload();
            customAppCheck();
            updateSwipeHoldDelayState();
            return true;
        } else if (preference == mRightSwipeActionsHold) {
            int rightSwipeActionsHold = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.RIGHT_HOLD_BACK_SWIPE_ACTION, rightSwipeActionsHold,
                    UserHandle.USER_CURRENT);
            int index = mRightSwipeActionsHold.findIndexOfValue((String) newValue);
            mRightSwipeActionsHold.setSummary(
                    mRightSwipeActionsHold.getEntries()[index]);
            mRightSwipeAppSelectionHold.setEnabled(rightSwipeActionsHold == 5);
            actionPreferenceReload();
            customAppCheck();
            updateSwipeHoldDelayState();
            return true;
        } else if (preference == mShowOnlyNavbarHandle) {
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
        String mLeftLongSwipeApp = Settings.System.getStringForUser(getActivity().getContentResolver(),
                String.valueOf(Settings.System.LEFT_LONG_BACK_SWIPE_APP_FR_ACTION), UserHandle.USER_CURRENT);
        String mRightLongSwipeApp = Settings.System.getStringForUser(getActivity().getContentResolver(),
                String.valueOf(Settings.System.RIGHT_LONG_BACK_SWIPE_APP_FR_ACTION), UserHandle.USER_CURRENT);
        String mLeftHoldSwipeApp = Settings.System.getStringForUser(getActivity().getContentResolver(),
                String.valueOf(Settings.System.LEFT_HOLD_BACK_SWIPE_APP_FR_ACTION), UserHandle.USER_CURRENT);
        String mRightHoldSwipeApp = Settings.System.getStringForUser(getActivity().getContentResolver(),
                String.valueOf(Settings.System.RIGHT_HOLD_BACK_SWIPE_APP_FR_ACTION), UserHandle.USER_CURRENT);

        mLeftSwipeAppSelection.setSummary(TextUtils.isEmpty(mLeftLongSwipeApp)
                ? getActivity().getString(R.string.back_swipe_app_select_summary) : mLeftLongSwipeApp);
        mRightSwipeAppSelection.setSummary(TextUtils.isEmpty(mRightLongSwipeApp)
                ? getActivity().getString(R.string.back_swipe_app_select_summary) : mRightLongSwipeApp);
        mLeftSwipeAppSelectionHold.setSummary(TextUtils.isEmpty(mLeftHoldSwipeApp)
                ? getActivity().getString(R.string.back_swipe_app_select_summary) : mLeftHoldSwipeApp);
        mRightSwipeAppSelectionHold.setSummary(TextUtils.isEmpty(mRightHoldSwipeApp)
                ? getActivity().getString(R.string.back_swipe_app_select_summary) : mRightHoldSwipeApp);
    }

    private void actionPreferenceReload() {
        int leftSwipeActions = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.LEFT_LONG_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);
        int rightSwipeActions = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.RIGHT_LONG_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);
        int leftSwipeActionsHold = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.LEFT_HOLD_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);
        int rightSwipeActionsHold = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.RIGHT_HOLD_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);

        // Reload the action preferences
        mLeftSwipeActions.setValue(Integer.toString(leftSwipeActions));
        mLeftSwipeActions.setSummary(mLeftSwipeActions.getEntry());

        mRightSwipeActions.setValue(Integer.toString(rightSwipeActions));
        mRightSwipeActions.setSummary(mRightSwipeActions.getEntry());

        mLeftSwipeActionsHold.setValue(Integer.toString(leftSwipeActionsHold));
        mLeftSwipeActionsHold.setSummary(mLeftSwipeActionsHold.getEntry());

        mRightSwipeActionsHold.setValue(Integer.toString(rightSwipeActionsHold));
        mRightSwipeActionsHold.setSummary(mRightSwipeActionsHold.getEntry());

        mLeftSwipeAppSelection.setVisible(mLeftSwipeActions.getEntryValues()
                [leftSwipeActions].equals("5"));
        mRightSwipeAppSelection.setVisible(mRightSwipeActions.getEntryValues()
                [rightSwipeActions].equals("5"));

        mLeftSwipeAppSelectionHold.setVisible(mLeftSwipeActionsHold.getEntryValues()
                [leftSwipeActionsHold].equals("5"));
        mRightSwipeAppSelectionHold.setVisible(mRightSwipeActionsHold.getEntryValues()
                [rightSwipeActionsHold].equals("5"));
    }

    private void updateSwipeHoldDelayState() {
        boolean leftHoldBool = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.LEFT_HOLD_BACK_SWIPE_ACTION, 0, UserHandle.USER_CURRENT) != 0;
        boolean rightHoldBool = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.RIGHT_HOLD_BACK_SWIPE_ACTION, 0, UserHandle.USER_CURRENT) != 0;
        mSwipeHoldDelay.setEnabled(leftHoldBool || rightHoldBool);
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
