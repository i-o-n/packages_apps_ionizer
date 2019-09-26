/*
 *  Copyright (C) 2019 ion-OS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.ion.ionizer;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Surface;
import android.preference.Preference;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.deviceinfo.ion.IonInfoPreferenceController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.ion.ionizer.preferences.Utils;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.SettingsPreferenceFragment;

import java.util.ArrayList;
import java.util.List;

public class IonIonizer extends DashboardFragment {

    private static final String TAG = "ionizer";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        final String KEY_DEVICE_PART = "device_part";
        final String KEY_DEVICE_PART_PACKAGE_NAME = "com.ion.ionizer.device";

        addPreferencesFromResource(R.xml.ion_settings);

        //Device ionizer
        if (!Utils.isPackageInstalled(getActivity(), KEY_DEVICE_PART_PACKAGE_NAME)) {
            getPreferenceScreen().removePreference(findPreference(KEY_DEVICE_PART));
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ION_IONIZER;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.ion_settings_;
    }

    public static void lockCurrentOrientation(Activity activity) {
        int currentRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int orientation = activity.getResources().getConfiguration().orientation;
        int frozenRotation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        switch (currentRotation) {
            case Surface.ROTATION_0:
                frozenRotation = orientation == Configuration.ORIENTATION_LANDSCAPE
                        ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                break;
            case Surface.ROTATION_90:
                frozenRotation = orientation == Configuration.ORIENTATION_PORTRAIT
                        ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                        : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                break;
            case Surface.ROTATION_180:
                frozenRotation = orientation == Configuration.ORIENTATION_LANDSCAPE
                        ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                        : ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                break;
            case Surface.ROTATION_270:
                frozenRotation = orientation == Configuration.ORIENTATION_PORTRAIT
                        ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        : ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                break;
        }
        activity.setRequestedOrientation(frozenRotation);
    }

    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle(), this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle, Fragment fragment) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new IonInfoPreferenceController(context, fragment));
        return controllers;
    }
}
