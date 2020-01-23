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

import android.content.Context;
import android.provider.SearchIndexableResource;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.display.OverlayCategoryPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;

import com.ion.ionizer.R;
import com.ion.ionizer.display.AccentColorPreferenceController;
import com.ion.ionizer.display.QsAlphaPreferenceController;
import com.ion.ionizer.display.QsbgColorPreferenceController;
import com.ion.ionizer.display.QsColorPreferenceController;
import com.ion.ionizer.display.QsCustomHeaderPreferenceController;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class Interface extends DashboardFragment implements
        Indexable {

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
        return buildPreferenceControllers(context, getSettingsLifecycle(), this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle, Fragment fragment) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.font"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.adaptive_icon_shape"));
        controllers.add(new AccentColorPreferenceController(context));
        controllers.add(new QsAlphaPreferenceController(context));
        controllers.add(new QsbgColorPreferenceController(context));
        controllers.add(new QsColorPreferenceController(context));
        controllers.add(new QsCustomHeaderPreferenceController(context));
        return controllers;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.ion_settings_interface;
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
