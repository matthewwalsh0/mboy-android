package com.matthew.mboy;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
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

    private static final int BUTTON_START = 0;
    private static final int BUTTON_SELECT = 1;
    private static final int BUTTON_A = 2;
    private static final int BUTTON_B = 3;
    private static final int BUTTON_UP = 4;
    private static final int BUTTON_RIGHT = 5;
    private static final int BUTTON_DOWN = 6;
    private static final int BUTTON_LEFT = 7;

    private static final int SCREEN_WIDTH = 160;
    private static final int SCREEN_HEIGHT = 144;
    private static final int BUTTON_UP_DELAY = 20;
    private static final float ASPECT_RATIO = SCREEN_HEIGHT / (float) SCREEN_WIDTH;
    private static final List<String> PERMISSIONS;
    private static String ROM_PATH;

    static {
        PERMISSIONS = new ArrayList<String>();
        PERMISSIONS.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        ROM_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                "/pokemon_gold.gbc";

        System.loadLibrary("native-lib");
    }

    public native void startEmulator(Object surface, String path);
    public native int getFPS();
    public native int getRenderFPS();
    public native void setButtonDown(int button, boolean state);

    private PermissionHelper m_permissionHelper;
    private ImageButton m_buttonA;
    private ImageButton m_buttonB;
    private ImageButton m_buttonStart;
    private ImageButton m_buttonSelect;
    private ImageButton m_buttonDpad;

    public MainActivity() {
        m_permissionHelper = new PermissionHelper(this, PERMISSIONS, new Runnable() {
            @Override
            public void run() {
                startEmulator(ROM_PATH);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_buttonA = findViewById(R.id.buttonA);
        m_buttonB = findViewById(R.id.buttonB);
        m_buttonStart = findViewById(R.id.buttonStart);
        m_buttonSelect = findViewById(R.id.buttonSelect);
        m_buttonDpad = findViewById(R.id.buttonDpad);

        m_buttonA.setOnTouchListener(new TouchListener(BUTTON_A));
        m_buttonB.setOnTouchListener(new TouchListener(BUTTON_B));
        m_buttonStart.setOnTouchListener(new TouchListener(BUTTON_START));
        m_buttonSelect.setOnTouchListener(new TouchListener(BUTTON_SELECT));
        m_buttonDpad.setOnTouchListener(new DpadTouchListener());

        prepareSurface();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        m_permissionHelper.onResult(requestCode, permissions, grantResults);
    }

    private void prepareSurface() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        final SurfaceView sv = findViewById(R.id.surfaceView);
        ViewGroup.LayoutParams lp = sv.getLayoutParams();
        lp.height = new Double(screenWidth * ASPECT_RATIO).intValue();

        sv.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                m_permissionHelper.check();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {}
        });
    }

    private void startEmulator(final String path) {
        final SurfaceView sv = findViewById(R.id.surfaceView);
        final Context context = this;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    startEmulator(sv.getHolder().getSurface(), path);
                }
                catch(final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(context)
                                    .setTitle("Unhandled Error")
                                    .setMessage(e.getMessage())
                                    .show();
                        }
                    });

                }
            }
        }).start();

        updateFPS();
    }

    private void updateFPS() {
        new android.os.Handler().postDelayed(
            new Runnable() {
                public void run() {
                    int fps = getFPS();
                    int renderFps = getRenderFPS();
                    getSupportActionBar().setSubtitle("FPS - Game: " + fps + " Screen: " + renderFps);
                    updateFPS();
                }
            },
            1000);
    }

    private class TouchListener implements View.OnTouchListener {

        private int m_button;

        public TouchListener(int button) {
            m_button = button;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch ( event.getAction() ) {
                case MotionEvent.ACTION_DOWN: {
                    setButtonDown(m_button, true);
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setButtonDown(m_button, false);
                        }
                    }, BUTTON_UP_DELAY);
                    break;
                }
            }
            return true;
        }
    }

    private class DpadTouchListener implements View.OnTouchListener {

        private int m_buttonDown;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch ( event.getAction() ) {
                case MotionEvent.ACTION_DOWN: {
                    m_buttonDown = getButton(event.getX(), event.getY());
                    setButtonDown(m_buttonDown, true);
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setButtonDown(m_buttonDown, false);
                        }
                    }, BUTTON_UP_DELAY);
                    break;
                }
            }

            return true;
        }

        private int getButton(float x, float y) {
            float upDistance = y;
            float bottomDistance = m_buttonDpad.getHeight() - y;
            float leftDistance = x;
            float rightDistance = m_buttonDpad.getWidth() - x;

            Integer closest = null;
            float distance = Integer.MAX_VALUE;

            if(upDistance < distance) {
                closest = BUTTON_UP;
                distance = upDistance;
            }

            if(bottomDistance < distance) {
                closest = BUTTON_DOWN;
                distance = bottomDistance;
            }

            if(leftDistance < distance) {
                closest = BUTTON_LEFT;
                distance = leftDistance;
            }

            if(rightDistance < distance) {
                closest = BUTTON_RIGHT;
                distance = rightDistance;
            }

            return closest;
        }
    }
}
