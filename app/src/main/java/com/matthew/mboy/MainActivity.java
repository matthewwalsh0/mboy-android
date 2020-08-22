package com.matthew.mboy;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.matthew.mboy.SettingsHelper.Setting.*;
public class MainActivity extends AppCompatActivity {

    private static final int FPS_INTERVAL = 1000;
    private static final String FPS_FORMAT_PORTRAIT = "FPS - Game: %d Screen: %d";
    private static final String FPS_FORMAT_LANDSCAPE = "FPS\nGame: %d\nScreen: %d";
    private static final List<String> PERMISSIONS;

    static {
        PERMISSIONS = new ArrayList<String>();
        PERMISSIONS.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        PERMISSIONS.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private PermissionHelper m_permissionHelper;
    private SettingsHelper m_settingsHelper;
    private Gameboy m_gameboy;
    private String m_pendingRom;

    public MainActivity() {
        m_permissionHelper = new PermissionHelper(this, PERMISSIONS, new Runnable() {
            @Override
            public void run() {
                onPermissionsGranted();
            }
        });

        m_settingsHelper = SettingsHelper.getInstance(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_gameboy = Application.getInstance().getGameboy();

        if(m_gameboy == null) {
            m_gameboy = Application.getInstance().createGameboy()
                .withOnReady(new Runnable() {
                    @Override
                    public void run() {
                        m_settingsHelper.forceBooleanListeners(Arrays.asList(
                                TURBO, BACKGROUND, WINDOW, SPRITES, AUDIO, SQUARE1, SQUARE2, WAVE));
                    }
                });
        }

        final MainActivity context = this;

        initControls();
        initSettingListeners();

        GameboyView gameboyView = findViewById(R.id.surfaceView);
        gameboyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_gameboy.pause();
                Intent intent = new Intent(context, SettingsActivity.class);
                startActivity(intent);
            }
        });

        TextView fpsText = findViewById(R.id.fps);
        displayFps(fpsText);

        m_permissionHelper.check();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(m_gameboy != null && m_gameboy.isPaused()) {
            if(m_pendingRom != null) {
                m_gameboy.changeRom(m_pendingRom);
                m_pendingRom = null;
            } else {
                m_gameboy.continueEmulator();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        m_permissionHelper.onResult(requestCode, permissions, grantResults);
    }

    public void onPermissionsGranted() {
        if(!m_gameboy.isStarted()) {
            String rom = m_settingsHelper.getROM();
            m_gameboy.start(rom);
        }

        m_gameboy.setSurface((SurfaceView) findViewById(R.id.surfaceView));
    }

    private void initControls() {
        DpadView dpadView = findViewById(R.id.buttonDpad);
        DelayedButtonView buttonA = findViewById(R.id.buttonA);
        DelayedButtonView buttonB = findViewById(R.id.buttonB);
        DelayedButtonView buttonStart = findViewById(R.id.buttonStart);
        DelayedButtonView buttonSelect = findViewById(R.id.buttonSelect);

        dpadView.setOnUpListener(new DpadView.DirectionListener() {
            @Override
            public void onStateChange(boolean down) {
                m_gameboy.setButtonDown(Gameboy.BUTTON_UP, down);
            }
        });

        dpadView.setOnDownListener(new DpadView.DirectionListener() {
            @Override
            public void onStateChange(boolean down) {
                m_gameboy.setButtonDown(Gameboy.BUTTON_DOWN, down);
            }
        });

        dpadView.setOnLeftListener(new DpadView.DirectionListener() {
            @Override
            public void onStateChange(boolean down) {
                m_gameboy.setButtonDown(Gameboy.BUTTON_LEFT, down);
            }
        });

        dpadView.setOnRightListener(new DpadView.DirectionListener() {
            @Override
            public void onStateChange(boolean down) {
                m_gameboy.setButtonDown(Gameboy.BUTTON_RIGHT, down);
            }
        });

        buttonA.setListener(new DelayedButtonView.TouchListener() {
            @Override
            public void onStateChange(boolean down) {
                m_gameboy.setButtonDown(Gameboy.BUTTON_A, down);
            }
        });

        buttonB.setListener(new DelayedButtonView.TouchListener() {
            @Override
            public void onStateChange(boolean down) {
                m_gameboy.setButtonDown(Gameboy.BUTTON_B, down);
            }
        });

        buttonStart.setListener(new DelayedButtonView.TouchListener() {
            @Override
            public void onStateChange(boolean down) {
                m_gameboy.setButtonDown(Gameboy.BUTTON_START, down);
            }
        });

        buttonSelect.setListener(new DelayedButtonView.TouchListener() {
            @Override
            public void onStateChange(boolean down) {
                m_gameboy.setButtonDown(Gameboy.BUTTON_SELECT, down);
            }
        });
    }

    private void initSettingListeners() {
        m_settingsHelper.withOnChangeListener(SettingsHelper.Setting.ROM, new SettingsHelper.OnChangeListener<String>() {
            @Override
            public void onChange(String value) {
                m_pendingRom = value;
            }
        });

        m_settingsHelper.withOnChangeListener(TURBO, new SettingsHelper.OnChangeListener<Boolean>() {
            @Override
            public void onChange(Boolean value) {
                m_gameboy.setTurbo(value);
            }
        });

        m_settingsHelper.withOnChangeListener(SettingsHelper.Setting.BACKGROUND, new SettingsHelper.OnChangeListener<Boolean>() {
            @Override
            public void onChange(Boolean value) {
                m_gameboy.setBackgroundEnabled(value);
            }
        });

        m_settingsHelper.withOnChangeListener(WINDOW, new SettingsHelper.OnChangeListener<Boolean>() {
            @Override
            public void onChange(Boolean value) {
                m_gameboy.setWindowEnabled(value);
            }
        });

        m_settingsHelper.withOnChangeListener(SettingsHelper.Setting.SPRITES, new SettingsHelper.OnChangeListener<Boolean>() {
            @Override
            public void onChange(Boolean value) {
                m_gameboy.setSpritesEnabled(value);
            }
        });

        m_settingsHelper.withOnChangeListener(SettingsHelper.Setting.AUDIO, new SettingsHelper.OnChangeListener<Boolean>() {
            @Override
            public void onChange(Boolean value) {
                m_gameboy.setAudioEnabled(value);
            }
        });

        m_settingsHelper.withOnChangeListener(SettingsHelper.Setting.SQUARE1, new SettingsHelper.OnChangeListener<Boolean>() {
            @Override
            public void onChange(Boolean value) {
                m_gameboy.setSquare1Enabled(value);
            }
        });

        m_settingsHelper.withOnChangeListener(SettingsHelper.Setting.SQUARE2, new SettingsHelper.OnChangeListener<Boolean>() {
            @Override
            public void onChange(Boolean value) {
                m_gameboy.setSquare2Enabled(value);
            }
        });

        m_settingsHelper.withOnChangeListener(SettingsHelper.Setting.WAVE, new SettingsHelper.OnChangeListener<Boolean>() {
            @Override
            public void onChange(Boolean value) {
                m_gameboy.setWaveEnabled(value);
            }
        });
    }

    private void displayFps(final TextView textView) {
        new Handler().postDelayed(
            new Runnable() {
                public void run() {
                    if(!m_settingsHelper.getShowFPS()) {
                        textView.setText("");
                        textView.setVisibility(View.GONE);
                    } else {
                        int fps = m_gameboy.getFPS();
                        int renderFps = m_gameboy.getRenderFPS();
                        boolean landscape = isLandscape();
                        String format = landscape ? FPS_FORMAT_LANDSCAPE : FPS_FORMAT_PORTRAIT;
                        String text = String.format(format, fps, renderFps);
                        textView.setText(text);
                        textView.setVisibility(View.VISIBLE);
                    }
                    displayFps(textView);
                }
            }, FPS_INTERVAL);
    }

    private boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }
}
