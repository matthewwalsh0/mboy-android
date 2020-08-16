package com.matthew.mboy;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsHelper {

    private static final Map<String, Setting> SETTINGS_BY_KEY = new HashMap<>();

    private static SettingsHelper m_instance;

    static {
        SETTINGS_BY_KEY.put("turbo", Setting.TURBO);
        SETTINGS_BY_KEY.put("background", Setting.BACKGROUND);
        SETTINGS_BY_KEY.put("window", Setting.WINDOW);
        SETTINGS_BY_KEY.put("sprites", Setting.SPRITES);
        SETTINGS_BY_KEY.put("audio", Setting.AUDIO);
        SETTINGS_BY_KEY.put("square1", Setting.SQUARE1);
        SETTINGS_BY_KEY.put("square2", Setting.SQUARE2);
        SETTINGS_BY_KEY.put("wave", Setting.WAVE);
        SETTINGS_BY_KEY.put("showFPS", Setting.SHOWFPS);
    }

    private Activity m_activity;
    private Map<Setting, OnChangeListener> m_changeListeners;

    private SettingsHelper(Activity activity) {
        m_activity = activity;
        m_changeListeners = new HashMap<>();
    }

    public SettingsHelper withOnChangeListener(Setting setting, OnChangeListener listener) {
        m_changeListeners.put(setting, listener);
        return this;
    }

    public SettingsHelper registerPreferences(PreferenceFragmentCompat fragment) {
        final SettingsHelper instance = this;

        for(String key : SETTINGS_BY_KEY.keySet()) {
            final Setting setting = SETTINGS_BY_KEY.get(key);

            fragment.findPreference(key).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    instance.notifySettingChange(setting, newValue);
                    return true;
                }
            });
        }

        return this;
    }

    public String getROM() {
        return getPreferences().getString("rom", null);
    }

    public void setROM(String rom) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putString("rom", rom);
        editor.commit();

        notifySettingChange(Setting.ROM, rom);
    }
    
    public Boolean getShowFPS() {
        return getPreferences().getBoolean("showFPS", false);
    }

    public void forceBooleanListeners(List<Setting> settings) {
        for(String key : SETTINGS_BY_KEY.keySet()) {
            Setting setting = SETTINGS_BY_KEY.get(key);
            if(!settings.contains(setting)) continue;
            Boolean value = getPreferences().getBoolean(key, false);
            notifySettingChange(setting, value);
        }
    }

    private void notifySettingChange(Setting setting, Object value) {
        for(Setting currentSetting : m_changeListeners.keySet()) {
            if(currentSetting == setting) {
                OnChangeListener listener = m_changeListeners.get(setting);
                listener.onChange(value);
            }
        }
    }

    public static SettingsHelper getInstance(Activity activity) {
        if(m_instance == null) {
            m_instance = new SettingsHelper(activity);
        }

        return m_instance;
    }

    private SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(m_activity);
    }

    public interface OnChangeListener<T> {
        void onChange(T value);
    }

    public enum Setting {
        ROM,
        TURBO,
        BACKGROUND,
        WINDOW,
        SPRITES,
        AUDIO,
        SQUARE1,
        SQUARE2,
        WAVE,
        SHOWFPS
    }
}
