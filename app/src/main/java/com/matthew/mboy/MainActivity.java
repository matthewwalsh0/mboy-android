package com.matthew.mboy;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final List<String> PERMISSIONS;

    static {
        PERMISSIONS = new ArrayList<String>();
        PERMISSIONS.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        PERMISSIONS.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private PermissionHelper m_permissionHelper;
    private SettingsHelper m_settingsHelper;
    private Gameboy m_gameboy;
    private Boolean m_paused;
    private String m_pendingRom;

    public MainActivity() {
        m_permissionHelper = new PermissionHelper(this, PERMISSIONS, new Runnable() {
            @Override
            public void run() {
                onPermissionsGranted();
            }
        });

        m_settingsHelper = SettingsHelper.getInstance(this);
        m_paused = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        m_gameboy = new Gameboy(
                (SurfaceView) findViewById(R.id.surfaceView),
                (ImageButton) findViewById(R.id.buttonA),
                (ImageButton) findViewById(R.id.buttonB),
                (ImageButton) findViewById(R.id.buttonStart),
                (ImageButton) findViewById(R.id.buttonSelect),
                (ImageButton) findViewById(R.id.buttonDpad),
                getSupportActionBar(),
                displayMetrics)
                .withOnReady(new Runnable() {
                    @Override
                    public void run() {
                        m_gameboy.setTurbo(m_settingsHelper.getTurbo());
                    }
                });

        final MainActivity context = this;

        m_settingsHelper.withOnChangeListener(SettingsHelper.Setting.ROM, new SettingsHelper.OnChangeListener<String>() {
            @Override
            public void onChange(String value) {
                m_pendingRom = value;
            }
        });

        m_settingsHelper.withOnChangeListener(SettingsHelper.Setting.TURBO, new SettingsHelper.OnChangeListener<Boolean>() {
            @Override
            public void onChange(Boolean value) {
                m_gameboy.setTurbo(value);
            }
        });

        findViewById(R.id.surfaceView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_gameboy.pause();
                m_paused = true;
                Intent intent = new Intent(context, SettingsActivity.class);
                startActivity(intent);
            }
        });

        m_permissionHelper.check();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(m_gameboy != null && m_paused) {
            if(m_pendingRom != null) {
                m_gameboy.changeRom(m_pendingRom);
                m_pendingRom = null;
            } else {
                m_gameboy.continueEmulator();
            }
            m_paused = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        m_permissionHelper.onResult(requestCode, permissions, grantResults);
    }

    public void onPermissionsGranted() {
        String rom = m_settingsHelper.getROM();
        m_gameboy.start(rom);
    }
}
