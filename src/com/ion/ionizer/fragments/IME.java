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

import android.app.AlertDialog; 
import android.app.Dialog; 
import android.content.Context; 
import android.content.DialogInterface; 
import android.content.Intent; 
import android.content.pm.PackageManager; 
import android.content.pm.ResolveInfo; 
import android.content.res.Resources; 
import android.support.v7.preference.Preference; 
import android.support.v14.preference.SwitchPreference; 
import android.graphics.drawable.Drawable; 
import android.os.Bundle; 
import android.app.DialogFragment; 
import android.os.Handler; 
import android.os.UserHandle; 
import android.support.v7.preference.ListPreference; 
import android.support.v14.preference.SwitchPreference; 
import android.support.v7.preference.Preference; 
import android.support.v7.preference.PreferenceCategory; 
import android.support.v7.preference.PreferenceScreen; 
 
import com.android.settings.SettingsPreferenceFragment; 
import com.ion.ionizer.preferences.CustomSeekBarPreference; 
import com.android.settings.R; 
import android.provider.Settings; 
import android.view.Gravity; 
import android.view.LayoutInflater; 
import android.view.View; 
import android.view.ViewGroup; 
import android.widget.AdapterView; 
import android.widget.BaseAdapter; 
import android.widget.ImageView; 
import android.widget.ListView; 
import android.widget.RadioButton; 
import android.widget.TextView; 
import android.widget.Toast; 
 
import java.util.ArrayList; 
import java.util.Collections; 
import java.util.Comparator; 
import java.util.HashMap; 
import java.util.Map; 
 
import com.android.internal.logging.nano.MetricsProto;

public class IME extends SettingsPreferenceFragment 
        implements Preference.OnPreferenceChangeListener { 
    
    public static final String TAG = "IME";
 
    private static final int DLG_KEYBOARD_ROTATION = 0; 
 
    private static final String PREF_DISABLE_FULLSCREEN_KEYBOARD = "disable_fullscreen_keyboard"; 
    private static final String KEYBOARD_ROTATION_TOGGLE = "keyboard_rotation_toggle"; 
    private static final String KEYBOARD_ROTATION_TIMEOUT = "keyboard_rotation_timeout"; 
    private static final String SHOW_ENTER_KEY = "show_enter_key"; 
 
    private static final int KEYBOARD_ROTATION_TIMEOUT_DEFAULT = 5000; // 5s 
 
    private SwitchPreference mDisableFullscreenKeyboard; 
    private SwitchPreference mKeyboardRotationToggle; 
    private ListPreference mKeyboardRotationTimeout; 
    private SwitchPreference mShowEnterKey; 
 
 
    @Override 
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        addPreferencesFromResource(R.xml.ion_settings_ime); 
        mDisableFullscreenKeyboard = 
            (SwitchPreference) findPreference(PREF_DISABLE_FULLSCREEN_KEYBOARD); 
        mDisableFullscreenKeyboard.setChecked(Settings.System.getInt(getContentResolver(), 
                Settings.System.DISABLE_FULLSCREEN_KEYBOARD, 0) == 1); 
        mDisableFullscreenKeyboard.setOnPreferenceChangeListener(this); 
      
        mKeyboardRotationToggle = (SwitchPreference) findPreference(KEYBOARD_ROTATION_TOGGLE); 
        mKeyboardRotationToggle.setChecked(Settings.System.getInt(getContentResolver(), 
                Settings.System.KEYBOARD_ROTATION_TIMEOUT, 0) > 0); 
        mKeyboardRotationToggle.setOnPreferenceChangeListener(this); 
      
        mKeyboardRotationTimeout = (ListPreference) findPreference(KEYBOARD_ROTATION_TIMEOUT); 
        mKeyboardRotationTimeout.setOnPreferenceChangeListener(this); 
        updateRotationTimeout(Settings.System.getInt( 
                getContentResolver(), Settings.System.KEYBOARD_ROTATION_TIMEOUT, 
                KEYBOARD_ROTATION_TIMEOUT_DEFAULT)); 
      
        mShowEnterKey = (SwitchPreference) findPreference(SHOW_ENTER_KEY); 
        mShowEnterKey.setChecked(Settings.System.getInt(getContentResolver(), 
                Settings.System.FORMAL_TEXT_INPUT, 0) == 1); 
        mShowEnterKey.setOnPreferenceChangeListener(this); 

        // Enable or disable mStatusBarImeSwitcher based on boolean: config_show_cmIMESwitcher 
        boolean showCmImeSwitcher = getResources().getBoolean( 
         com.android.internal.R.bool.config_show_cmIMESwitcher); 
         if (!showCmImeSwitcher) { 
             getPreferenceScreen().removePreference( 
                     findPreference(Settings.System.STATUS_BAR_IME_SWITCHER)); 
         } 
    } 
        
     public void updateRotationTimeout(int timeout) { 
               if (timeout == 0) { 
                   timeout = KEYBOARD_ROTATION_TIMEOUT_DEFAULT; 
               } 
               mKeyboardRotationTimeout.setValue(Integer.toString(timeout)); 
               mKeyboardRotationTimeout.setSummary( 
                   getString(R.string.keyboard_rotation_timeout_summary, 
                   mKeyboardRotationTimeout.getEntry())); 
           } 
        
           @Override 
           public void onResume() { 
               super.onResume(); 
    } 
    public boolean onPreferenceChange(Preference preference, Object objValue) { 
        if (preference == mDisableFullscreenKeyboard) { 
            Settings.System.putInt(getContentResolver(), 
                    Settings.System.DISABLE_FULLSCREEN_KEYBOARD,  (Boolean) objValue ? 1 : 0); 
            return true; 
        } else if (preference == mKeyboardRotationToggle) { 
            boolean isAutoRotate = (Settings.System.getIntForUser(getContentResolver(), 
                        Settings.System.ACCELEROMETER_ROTATION, 0, UserHandle.USER_CURRENT) == 1); 
            if (isAutoRotate && (Boolean) objValue) { 
                showDialogInner(DLG_KEYBOARD_ROTATION); 
            } 
            Settings.System.putInt(getContentResolver(), 
                    Settings.System.KEYBOARD_ROTATION_TIMEOUT, 
                    (Boolean) objValue ? KEYBOARD_ROTATION_TIMEOUT_DEFAULT : 0); 
            updateRotationTimeout(KEYBOARD_ROTATION_TIMEOUT_DEFAULT); 
            return true; 
        } else if (preference == mShowEnterKey) { 
            Settings.System.putInt(getContentResolver(), 
                Settings.System.FORMAL_TEXT_INPUT, (Boolean) objValue ? 1 : 0); 
            return true; 
        } else if (preference == mKeyboardRotationTimeout) { 
            int timeout = Integer.parseInt((String) objValue); 
            Settings.System.putInt(getContentResolver(), 
                    Settings.System.KEYBOARD_ROTATION_TIMEOUT, timeout); 
            updateRotationTimeout(timeout); 
            return true; 
        } 
        return false; 
    } 
 
    private void showDialogInner(int id) { 
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id); 
        newFragment.setTargetFragment(this, 0); 
        newFragment.show(getFragmentManager(), "dialog " + id); 
    } 
     
    public static class MyAlertDialogFragment extends DialogFragment { 
 
        public static MyAlertDialogFragment newInstance(int id) { 
            MyAlertDialogFragment frag = new MyAlertDialogFragment(); 
            Bundle args = new Bundle(); 
            args.putInt("id", id); 
            frag.setArguments(args); 
            return frag; 
        } 
 
        IME getOwner() { 
            return (IME) getTargetFragment(); 
        } 
 
        public Dialog onCreateDialog(Bundle savedInstanceState) { 
            int id = getArguments().getInt("id"); 
            switch (id) { 
                case DLG_KEYBOARD_ROTATION: 
                    return new AlertDialog.Builder(getActivity()) 
                    .setTitle(R.string.attention) 
                    .setMessage(R.string.keyboard_rotation_dialog) 
                    .setPositiveButton(R.string.dlg_ok, 
                        new DialogInterface.OnClickListener() { 
                        public void onClick(DialogInterface dialog, int which) { 
 
                        } 
                    }) 
                    .create(); 
            } 
            throw new IllegalArgumentException("unknown id " + id); 
        } 
 
        public void onCancel(DialogInterface dialog) { 
 
        } 
    } 
         
    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ION_IONIZER;
    }
 
} 
