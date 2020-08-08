package com.matthew.mboy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private static final String KEY_ROM = "rom";
    private static final String KEY_TURBO = "turbo";
    private static final String ROM_PATH = "%s/MBoy/Roms";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private Activity m_context;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Preference filePicker = findPreference(KEY_ROM);
            m_context = this.getActivity();

            filePicker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");

                    startActivityForResult(intent, 123);
                    return true;
                }
            });

            findPreference(KEY_TURBO).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SettingsHelper.getInstance(m_context).notifySettingChange(SettingsHelper.Setting.TURBO, newValue);
                    return true;
                }
            });

            updateROMSummary();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if(data == null || requestCode != 123) return;

            try {
                String savePath = String.format(ROM_PATH, Environment.getExternalStorageDirectory().getAbsolutePath());
                File saveDirectory = new File(savePath);
                if (!saveDirectory.exists()) saveDirectory.mkdirs();

                String baseName = new File(data.getData().getPath()).getName();
                File file = new File(saveDirectory, baseName);

                if (!file.exists()) file.createNewFile();

                InputStream input = m_context.getContentResolver().openInputStream(data.getData());
                inputStreamToFile(file.getAbsolutePath(), input);
                String value = file.getCanonicalPath();

                SettingsHelper.getInstance(m_context).setROM(value);
                updateROMSummary();
            }
            catch(Exception e) {
                new AlertDialog.Builder(m_context)
                        .setTitle("Unhandled Error")
                        .setMessage(e.getMessage())
                        .show();

            }
        }

        private void updateROMSummary() {
            Preference preference = findPreference(KEY_ROM);
            String value = getPreferenceManager().getSharedPreferences().getString(KEY_ROM, null);
            if(value == null) return;
            String summary = new File(value).getName();
            preference.setSummary(summary);
        }

        private static void inputStreamToFile(String path, InputStream input)
                throws FileNotFoundException, IOException {
            File file = new File(path);
            OutputStream output = new FileOutputStream(file);
            byte[] buffer = new byte[4 * 1024];
            int read;

            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }

            output.flush();
            input.close();
        }
    }
}