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
package com.ion.ionizer.fragments.ui;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;

import com.android.settings.SettingsPreferenceFragment;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.ion.AwesomeAnimationHelper;

import com.ion.ionizer.preferences.SystemSettingSeekBarPreference;
import com.ion.ionizer.R;

import java.util.Arrays;

public class AnimationControls extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    // private static final String ANIMATION_DURATION = "animation_controls_duration";
    private static final String ACTIVITY_OPEN = "activity_open";
    private static final String ACTIVITY_CLOSE = "activity_close";
    private static final String TASK_OPEN = "task_open";
    private static final String TASK_CLOSE = "task_close";
    private static final String TASK_MOVE_TO_FRONT = "task_move_to_front";
    private static final String TASK_MOVE_TO_BACK = "task_move_to_back";
    private static final String WALLPAPER_OPEN = "wallpaper_open";
    private static final String WALLPAPER_CLOSE = "wallpaper_close";
    private static final String WALLPAPER_INTRA_OPEN = "wallpaper_intra_open";
    private static final String WALLPAPER_INTRA_CLOSE = "wallpaper_intra_close";
    private static final String TASK_OPEN_BEHIND = "task_open_behind";

    // private SystemSettingSeekBarPreference mAnimDuration;
    ListPreference mActivityOpenPref;
    ListPreference mActivityClosePref;
    ListPreference mTaskOpenPref;
    ListPreference mTaskClosePref;
    ListPreference mTaskMoveToFrontPref;
    ListPreference mTaskMoveToBackPref;
    ListPreference mWallpaperOpen;
    ListPreference mWallpaperClose;
    ListPreference mWallpaperIntraOpen;
    ListPreference mWallpaperIntraClose;
    ListPreference mTaskOpenBehind;

    private int[] mAnimations;
    private String[] mAnimationsStrings;
    private String[] mAnimationsNum;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.system_animation_controls);
        final ContentResolver resolver = getActivity().getContentResolver();

        // mAnimDuration = (SystemSettingSeekBarPreference) findPreference(ANIMATION_DURATION);
        // int animdef = Settings.System.getIntForUser(resolver,
        //         Settings.System.ANIMATION_CONTROLS_DURATION, 0, UserHandle.USER_CURRENT);
        // mAnimDuration.setValue(animdef);
        // if (animdef == 0) {
        //     mAnimDuration.setSummary(getResources().getString(R.string.animation_duration_default));
        // } else {
        //     mAnimDuration.setSummary(getResources().getString(R.string.animation_duration_custom));
        // }
        // mAnimDuration.setOnPreferenceChangeListener(this);

        mAnimations = AwesomeAnimationHelper.getAnimationsList();
        int animqty = mAnimations.length;
        mAnimationsStrings = new String[animqty];
        mAnimationsNum = new String[animqty];
        for (int i = 0; i < animqty; i++) {
            mAnimationsStrings[i] = AwesomeAnimationHelper.getProperName(getActivity().getApplicationContext(), mAnimations[i]);
            mAnimationsNum[i] = String.valueOf(mAnimations[i]);
        }

        mActivityOpenPref = (ListPreference) findPreference(ACTIVITY_OPEN);
        mActivityOpenPref.setSummary(getProperSummary(mActivityOpenPref));
        mActivityOpenPref.setEntries(mAnimationsStrings);
        mActivityOpenPref.setEntryValues(mAnimationsNum);
        mActivityOpenPref.setOnPreferenceChangeListener(this);

        mActivityClosePref = (ListPreference) findPreference(ACTIVITY_CLOSE);
        mActivityClosePref.setSummary(getProperSummary(mActivityClosePref));
        mActivityClosePref.setEntries(mAnimationsStrings);
        mActivityClosePref.setEntryValues(mAnimationsNum);
        mActivityClosePref.setOnPreferenceChangeListener(this);

        mTaskOpenPref = (ListPreference) findPreference(TASK_OPEN);
        mTaskOpenPref.setSummary(getProperSummary(mTaskOpenPref));
        mTaskOpenPref.setEntries(mAnimationsStrings);
        mTaskOpenPref.setEntryValues(mAnimationsNum);
        mTaskOpenPref.setOnPreferenceChangeListener(this);

        mTaskClosePref = (ListPreference) findPreference(TASK_CLOSE);
        mTaskClosePref.setSummary(getProperSummary(mTaskClosePref));
        mTaskClosePref.setEntries(mAnimationsStrings);
        mTaskClosePref.setEntryValues(mAnimationsNum);
        mTaskClosePref.setOnPreferenceChangeListener(this);

        mTaskMoveToFrontPref = (ListPreference) findPreference(TASK_MOVE_TO_FRONT);
        mTaskMoveToFrontPref.setSummary(getProperSummary(mTaskMoveToFrontPref));
        mTaskMoveToFrontPref.setEntries(mAnimationsStrings);
        mTaskMoveToFrontPref.setEntryValues(mAnimationsNum);
        mTaskMoveToFrontPref.setOnPreferenceChangeListener(this);

        mTaskMoveToBackPref = (ListPreference) findPreference(TASK_MOVE_TO_BACK);
        mTaskMoveToBackPref.setSummary(getProperSummary(mTaskMoveToBackPref));
        mTaskMoveToBackPref.setEntries(mAnimationsStrings);
        mTaskMoveToBackPref.setEntryValues(mAnimationsNum);
        mTaskMoveToBackPref.setOnPreferenceChangeListener(this);

        mWallpaperOpen = (ListPreference) findPreference(WALLPAPER_OPEN);
        mWallpaperOpen.setSummary(getProperSummary(mWallpaperOpen));
        mWallpaperOpen.setEntries(mAnimationsStrings);
        mWallpaperOpen.setEntryValues(mAnimationsNum);
        mWallpaperOpen.setOnPreferenceChangeListener(this);

        mWallpaperClose = (ListPreference) findPreference(WALLPAPER_CLOSE);
        mWallpaperClose.setSummary(getProperSummary(mWallpaperClose));
        mWallpaperClose.setEntries(mAnimationsStrings);
        mWallpaperClose.setEntryValues(mAnimationsNum);
        mWallpaperClose.setOnPreferenceChangeListener(this);

        mWallpaperIntraOpen = (ListPreference) findPreference(WALLPAPER_INTRA_OPEN);
        mWallpaperIntraOpen.setSummary(getProperSummary(mWallpaperIntraOpen));
        mWallpaperIntraOpen.setEntries(mAnimationsStrings);
        mWallpaperIntraOpen.setEntryValues(mAnimationsNum);
        mWallpaperIntraOpen.setOnPreferenceChangeListener(this);

        mWallpaperIntraClose = (ListPreference) findPreference(WALLPAPER_INTRA_CLOSE);
        mWallpaperIntraClose.setSummary(getProperSummary(mWallpaperIntraClose));
        mWallpaperIntraClose.setEntries(mAnimationsStrings);
        mWallpaperIntraClose.setEntryValues(mAnimationsNum);
        mWallpaperIntraClose.setOnPreferenceChangeListener(this);

        mTaskOpenBehind = (ListPreference) findPreference(TASK_OPEN_BEHIND);
        mTaskOpenBehind.setSummary(getProperSummary(mTaskOpenBehind));
        mTaskOpenBehind.setEntries(mAnimationsStrings);
        mTaskOpenBehind.setEntryValues(mAnimationsNum);
        mTaskOpenBehind.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        // if (preference == mAnimDuration) {
        //     int value = (Integer) newValue;
        //     Settings.System.putIntForUser(resolver,
        //             Settings.System.ANIMATION_CONTROLS_DURATION, value, UserHandle.USER_CURRENT);
        //     if (value == 0) {
        //         preference.setSummary(getResources().getString(R.string.animation_duration_default));
        //     } else {
        //         preference.setSummary(getResources().getString(R.string.animation_duration_custom));
        //     }
        //     return true;
        // } else 
        if (preference == mActivityOpenPref) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.ACTIVITY_ANIMATION_CONTROLS[0], val, UserHandle.USER_CURRENT);
            preference.setSummary(getProperSummary(preference));
            return true;
        } else if (preference == mActivityClosePref) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.ACTIVITY_ANIMATION_CONTROLS[1], val, UserHandle.USER_CURRENT);
            preference.setSummary(getProperSummary(preference));
            return true;
        } else if (preference == mTaskOpenPref) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.ACTIVITY_ANIMATION_CONTROLS[2], val, UserHandle.USER_CURRENT);
            preference.setSummary(getProperSummary(preference));
            return true;
        } else if (preference == mTaskClosePref) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.ACTIVITY_ANIMATION_CONTROLS[3], val, UserHandle.USER_CURRENT);
            preference.setSummary(getProperSummary(preference));
            return true;
        } else if (preference == mTaskMoveToFrontPref) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.ACTIVITY_ANIMATION_CONTROLS[4], val, UserHandle.USER_CURRENT);
            preference.setSummary(getProperSummary(preference));
            return true;
        } else if (preference == mTaskMoveToBackPref) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.ACTIVITY_ANIMATION_CONTROLS[5], val, UserHandle.USER_CURRENT);
            preference.setSummary(getProperSummary(preference));
            return true;
        } else if (preference == mWallpaperOpen) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.ACTIVITY_ANIMATION_CONTROLS[6], val, UserHandle.USER_CURRENT);
            preference.setSummary(getProperSummary(preference));
            return true;
        } else if (preference == mWallpaperClose) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.ACTIVITY_ANIMATION_CONTROLS[7], val, UserHandle.USER_CURRENT);
            preference.setSummary(getProperSummary(preference));
            return true;
        } else if (preference == mWallpaperIntraOpen) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.ACTIVITY_ANIMATION_CONTROLS[8], val, UserHandle.USER_CURRENT);
            preference.setSummary(getProperSummary(preference));
            return true;
        } else if (preference == mWallpaperIntraClose) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.ACTIVITY_ANIMATION_CONTROLS[9], val, UserHandle.USER_CURRENT);
            preference.setSummary(getProperSummary(preference));
            return true;
        } else if (preference == mTaskOpenBehind) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.ACTIVITY_ANIMATION_CONTROLS[10], val, UserHandle.USER_CURRENT);
            preference.setSummary(getProperSummary(preference));
            return true;
        }
        return false;
    }

    private String getProperSummary(Preference preference) {
        String mString = "";
        if (preference == mActivityOpenPref) {
            mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[0];
        } else if (preference == mActivityClosePref) {
            mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[1];
        } else if (preference == mTaskOpenPref) {
            mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[2];
        } else if (preference == mTaskClosePref) {
            mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[3];
        } else if (preference == mTaskMoveToFrontPref) {
            mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[4];
        } else if (preference == mTaskMoveToBackPref) {
            mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[5];
        } else if (preference == mWallpaperOpen) {
            mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[6];
        } else if (preference == mWallpaperClose) {
            mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[7];
        } else if (preference == mWallpaperIntraOpen) {
            mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[8];
        } else if (preference == mWallpaperIntraClose) {
            mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[9];
        } else if (preference == mTaskOpenBehind) {
            mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[10];
        }

        int mNum = Settings.System.getIntForUser(getActivity().getContentResolver(),
                    mString, 0, UserHandle.USER_CURRENT);
        return mAnimationsStrings[mNum];
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.System.putInt(resolver,
                Settings.System.DISABLE_TRANSITION_ANIMATIONS, 0);
        Settings.System.putIntForUser(resolver,
                Settings.System.ANIMATION_CONTROLS_DURATION, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.ACTIVITY_ANIMATION_CONTROLS[0], 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.ACTIVITY_ANIMATION_CONTROLS[1], 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.ACTIVITY_ANIMATION_CONTROLS[2], 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.ACTIVITY_ANIMATION_CONTROLS[3], 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.ACTIVITY_ANIMATION_CONTROLS[4], 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.ACTIVITY_ANIMATION_CONTROLS[5], 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.ACTIVITY_ANIMATION_CONTROLS[6], 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.ACTIVITY_ANIMATION_CONTROLS[7], 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.ACTIVITY_ANIMATION_CONTROLS[8], 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.ACTIVITY_ANIMATION_CONTROLS[9], 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.ACTIVITY_ANIMATION_CONTROLS[10], 0, UserHandle.USER_CURRENT);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ION_IONIZER;
    }
}