/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.inputmethod.latin;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.android.inputmethod.latin.LocaleUtils.RunInLocale;

import java.util.HashMap;
import java.util.Locale;

public final class Settings implements SharedPreferences.OnSharedPreferenceChangeListener {
    // In the same order as xml/prefs.xml
    public static final String PREF_GENERAL_SETTINGS = "general_settings";
    public static final String PREF_AUTO_CAP = "auto_cap";
    public static final String PREF_VIBRATE_ON = "vibrate_on";
    public static final String PREF_SOUND_ON = "sound_on";
    public static final String PREF_POPUP_ON = "popup_on";
    public static final String PREF_VOICE_MODE = "voice_mode";
    public static final String PREF_CORRECTION_SETTINGS = "correction_settings";
    public static final String PREF_CONFIGURE_DICTIONARIES_KEY = "configure_dictionaries_key";
    public static final String PREF_AUTO_CORRECTION_THRESHOLD = "auto_correction_threshold";
    public static final String PREF_SHOW_SUGGESTIONS_SETTING = "show_suggestions_setting";
    public static final String PREF_MISC_SETTINGS = "misc_settings";
    public static final String PREF_LAST_USER_DICTIONARY_WRITE_TIME =
            "last_user_dictionary_write_time";
    public static final String PREF_ADVANCED_SETTINGS = "pref_advanced_settings";
    public static final String PREF_KEY_USE_CONTACTS_DICT = "pref_key_use_contacts_dict";
    public static final String PREF_KEY_USE_DOUBLE_SPACE_PERIOD =
            "pref_key_use_double_space_period";
    public static final String PREF_SHOW_LANGUAGE_SWITCH_KEY =
            "pref_show_language_switch_key";
    public static final String PREF_INCLUDE_OTHER_IMES_IN_LANGUAGE_SWITCH_LIST =
            "pref_include_other_imes_in_language_switch_list";
    public static final String PREF_CUSTOM_INPUT_STYLES = "custom_input_styles";
    public static final String PREF_KEY_PREVIEW_POPUP_DISMISS_DELAY =
            "pref_key_preview_popup_dismiss_delay";
    public static final String PREF_BIGRAM_PREDICTIONS = "next_word_prediction";
    public static final String PREF_GESTURE_SETTINGS = "gesture_typing_settings";
    public static final String PREF_GESTURE_INPUT = "gesture_input";
    public static final String PREF_KEY_LONGPRESS_TIMEOUT = "pref_key_longpress_timeout";
    public static final String PREF_VIBRATION_DURATION_SETTINGS =
            "pref_vibration_duration_settings";
    public static final String PREF_KEYPRESS_SOUND_VOLUME =
            "pref_keypress_sound_volume";
    public static final String PREF_GESTURE_PREVIEW_TRAIL = "pref_gesture_preview_trail";
    public static final String PREF_GESTURE_FLOATING_PREVIEW_TEXT =
            "pref_gesture_floating_preview_text";

    public static final String PREF_INPUT_LANGUAGE = "input_language";
    public static final String PREF_SELECTED_LANGUAGES = "selected_languages";
    public static final String PREF_DEBUG_SETTINGS = "debug_settings";

    // This preference key is deprecated. Use {@link #PREF_SHOW_LANGUAGE_SWITCH_KEY} instead.
    // This is being used only for the backward compatibility.
    private static final String PREF_SUPPRESS_LANGUAGE_SWITCH_KEY =
            "pref_suppress_language_switch_key";

    private Resources mRes;
    private SharedPreferences mPrefs;
    private Locale mCurrentLocale;
    private SettingsValues mSettingsValues;

    private static final Settings sInstance = new Settings();

    public static Settings getInstance() {
        return sInstance;
    }

    public static void init(final Context context) {
        sInstance.onCreate(context);
    }

    private Settings() {
        // Intentional empty constructor for singleton.
    }

    private void onCreate(final Context context) {
        mRes = context.getResources();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    public void onDestroy() {
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences prefs, final String key) {
        loadSettings(mCurrentLocale, mSettingsValues.mInputAttributes);
    }

    public void loadSettings(final Locale locale, final InputAttributes inputAttributes) {
        mCurrentLocale = locale;
        final SharedPreferences prefs = mPrefs;
        final RunInLocale<SettingsValues> job = new RunInLocale<SettingsValues>() {
            @Override
            protected SettingsValues job(final Resources res) {
                return new SettingsValues(prefs, res, inputAttributes);
            }
        };
        mSettingsValues = job.runInLocale(mRes, locale);
    }

    // TODO: Remove this method and add proxy method to SettingsValues.
    public SettingsValues getCurrent() {
        return mSettingsValues;
    }

    // Accessed from the settings interface, hence public
    public static boolean readKeyPreviewPopupEnabled(final SharedPreferences prefs,
            final Resources res) {
        final boolean showPopupOption = res.getBoolean(
                R.bool.config_enable_show_popup_on_keypress_option);
        if (!showPopupOption) return res.getBoolean(R.bool.config_default_popup_preview);
        return prefs.getBoolean(PREF_POPUP_ON,
                res.getBoolean(R.bool.config_default_popup_preview));
    }

    public static int readKeyPreviewPopupDismissDelay(final SharedPreferences prefs,
            final Resources res) {
        return Integer.parseInt(prefs.getString(PREF_KEY_PREVIEW_POPUP_DISMISS_DELAY,
                Integer.toString(res.getInteger(
                        R.integer.config_key_preview_linger_timeout))));
    }

    public static boolean readShowsLanguageSwitchKey(final SharedPreferences prefs) {
        if (prefs.contains(PREF_SUPPRESS_LANGUAGE_SWITCH_KEY)) {
            final boolean suppressLanguageSwitchKey = prefs.getBoolean(
                    PREF_SUPPRESS_LANGUAGE_SWITCH_KEY, false);
            final SharedPreferences.Editor editor = prefs.edit();
            editor.remove(PREF_SUPPRESS_LANGUAGE_SWITCH_KEY);
            editor.putBoolean(PREF_SHOW_LANGUAGE_SWITCH_KEY, !suppressLanguageSwitchKey);
            editor.apply();
        }
        return prefs.getBoolean(PREF_SHOW_LANGUAGE_SWITCH_KEY, true);
    }

    public static String readPrefAdditionalSubtypes(final SharedPreferences prefs,
            final Resources res) {
        final String predefinedPrefSubtypes = AdditionalSubtype.createPrefSubtypes(
                res.getStringArray(R.array.predefined_subtypes));
        return prefs.getString(PREF_CUSTOM_INPUT_STYLES, predefinedPrefSubtypes);
    }

    public static void writePrefAdditionalSubtypes(final SharedPreferences prefs,
            final String prefSubtypes) {
        prefs.edit().putString(Settings.PREF_CUSTOM_INPUT_STYLES, prefSubtypes).apply();
    }

    public static float readKeypressSoundVolume(final SharedPreferences prefs,
            final Resources res) {
        final float volume = prefs.getFloat(PREF_KEYPRESS_SOUND_VOLUME, -1.0f);
        return (volume >= 0) ? volume : readDefaultKeypressSoundVolume(res);
    }

    public static float readDefaultKeypressSoundVolume(final Resources res) {
        return Float.parseFloat(
                ResourceUtils.getDeviceOverrideValue(res, R.array.keypress_volumes));
    }

    public static int readKeyLongpressTimeout(final SharedPreferences prefs,
            final Resources res) {
        final int ms = prefs.getInt(PREF_KEY_LONGPRESS_TIMEOUT, -1);
        return (ms >= 0) ? ms : readDefaultKeyLongpressTimeout(res);
    }

    public static int readDefaultKeyLongpressTimeout(final Resources res) {
        return res.getInteger(R.integer.config_default_longpress_key_timeout);
    }

    public static int readKeypressVibrationDuration(final SharedPreferences prefs,
            final Resources res) {
        final int ms = prefs.getInt(PREF_VIBRATION_DURATION_SETTINGS, -1);
        return (ms >= 0) ? ms : readDefaultKeypressVibrationDuration(res);
    }

    public static int readDefaultKeypressVibrationDuration(final Resources res) {
        return Integer.parseInt(
                ResourceUtils.getDeviceOverrideValue(res, R.array.keypress_vibration_durations));
    }

    public static boolean readUsabilityStudyMode(final SharedPreferences prefs) {
        return prefs.getBoolean(DebugSettings.PREF_USABILITY_STUDY_MODE, true);
    }

    public static long readLastUserHistoryWriteTime(final SharedPreferences prefs,
            final String locale) {
        final String str = prefs.getString(PREF_LAST_USER_DICTIONARY_WRITE_TIME, "");
        final HashMap<String, Long> map = LocaleUtils.localeAndTimeStrToHashMap(str);
        if (map.containsKey(locale)) {
            return map.get(locale);
        }
        return 0;
    }

    public static void writeLastUserHistoryWriteTime(final SharedPreferences prefs,
            final String locale) {
        final String oldStr = prefs.getString(PREF_LAST_USER_DICTIONARY_WRITE_TIME, "");
        final HashMap<String, Long> map = LocaleUtils.localeAndTimeStrToHashMap(oldStr);
        map.put(locale, System.currentTimeMillis());
        final String newStr = LocaleUtils.localeAndTimeHashMapToStr(map);
        prefs.edit().putString(PREF_LAST_USER_DICTIONARY_WRITE_TIME, newStr).apply();
    }

    public static boolean readUseFullscreenMode(final Resources res) {
        return res.getBoolean(R.bool.config_use_fullscreen_mode);
    }
}
