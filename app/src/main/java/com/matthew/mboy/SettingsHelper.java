package com.matthew.mboy;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

public class SettingsHelper {

    private static SettingsHelper m_instance;

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

    public String getROM() {
        return getPreferences().getString("rom", null);
    }

    public Boolean getTurbo() {
        return getPreferences().getBoolean("turbo", false);
    }

    public void setROM(String rom) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putString("rom", rom);
        editor.commit();

        notifySettingChange(Setting.ROM, rom);
    }

    public void notifySettingChange(Setting setting, Object value) {
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
        TURBO
    }
}
