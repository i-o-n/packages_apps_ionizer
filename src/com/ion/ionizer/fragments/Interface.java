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

import android.app.Fragment;
import android.content.Context;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.display.AccentPickerPreferenceController;
import com.android.settings.display.SystemUiThemePreferenceController;
import com.android.settings.display.ThemePreferenceController;
import com.ion.ionizer.display.QsHeaderStylePreferenceController;
import com.ion.ionizer.display.QsTileStylePreferenceController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

import com.ion.ionizer.R;

import java.util.ArrayList;
import java.util.List;

public class Interface extends DashboardFragment {
    private static final String TAG = "Interface";

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.ION_IONIZER;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.ion_settings_interface;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getLifecycle(), this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle, Fragment fragment) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new AccentPickerPreferenceController(context, lifecycle, fragment));
        controllers.add(new SystemUiThemePreferenceController(context, lifecycle));
        controllers.add(new QsHeaderStylePreferenceController(context));
        controllers.add(new QsTileStylePreferenceController(context));
        return controllers;
    }
}
