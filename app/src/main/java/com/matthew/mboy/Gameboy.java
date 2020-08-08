package com.matthew.mboy;

import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.appcompat.app.ActionBar;

public class Gameboy {

    public static final int SCREEN_WIDTH = 160;
    public static final int SCREEN_HEIGHT = 144;
    public static final float ASPECT_RATIO = SCREEN_HEIGHT / (float) SCREEN_WIDTH;

    private static final int BUTTON_START = 0;
    private static final int BUTTON_SELECT = 1;
    private static final int BUTTON_A = 2;
    private static final int BUTTON_B = 3;
    private static final int BUTTON_UP = 4;
    private static final int BUTTON_RIGHT = 5;
    private static final int BUTTON_DOWN = 6;
    private static final int BUTTON_LEFT = 7;
    private static final int BUTTON_UP_DELAY = 20;
    private static final int FPS_INTERVAL = 1000;

    static {
        System.loadLibrary("native-lib");
    }

    private native void nInit(Object surface);
    private native void nStart(String path);
    private native void nStop();
    private native void nPause();
    private native void nContinue();
    private native void nRender();
    private native int nGetFPS();
    private native int nGetRenderFPS();
    private native void nSetButtonDown(int button, boolean state);
    public native void setTurbo(boolean value);

    private SurfaceView m_surfaceView;
    private ImageButton m_buttonDpad;
    private ActionBar m_actionBar;
    private Boolean m_started;
    private Thread m_runThread;
    private Thread m_renderThread;
    private Runnable m_onReady;

    public Gameboy(final SurfaceView surfaceView, ImageButton buttonA, ImageButton buttonB,
                   ImageButton buttonStart, ImageButton buttonSelect, ImageButton buttonDpad,
                   final ActionBar actionBar, DisplayMetrics displayMetrics) {
        m_surfaceView = surfaceView;
        m_buttonDpad = buttonDpad;
        m_actionBar = actionBar;
        m_started = false;

        buttonA.setOnTouchListener(new TouchListener(BUTTON_A));
        buttonB.setOnTouchListener(new TouchListener(BUTTON_B));
        buttonStart.setOnTouchListener(new TouchListener(BUTTON_START));
        buttonSelect.setOnTouchListener(new TouchListener(BUTTON_SELECT));
        buttonDpad.setOnTouchListener(new DpadTouchListener());

        int screenWidth = displayMetrics.widthPixels;
        int surfaceHeight = new Double(screenWidth * ASPECT_RATIO).intValue();
        ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
        lp.height = surfaceHeight;
    }

    public Gameboy withOnReady(Runnable onReady) {
        m_onReady = onReady;
        return this;
    }

    public void start(final String path) {
        m_surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if(m_started) return;
                nInit(m_surfaceView.getHolder().getSurface());
                if(m_onReady != null) m_onReady.run();
                startThread(path);
                displayFps(m_actionBar);
                m_started = true;
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {}
        });
    }

    public void changeRom(final String path) {
        changeRomThread(path);
    }

    public void pause() {
        if(m_runThread == null) return;

        nPause();

        try {
            m_runThread.join();
            m_renderThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void continueEmulator() {
        if(m_runThread == null) return;
        continueThread();
    }

    private void startThread(final String path) {
        if(path == null) return;
        nStart(path);
        continueThread();
    }

    private void continueThread() {
        m_runThread = new Thread(new Runnable() {
            @Override
            public void run() {
                nContinue();
            }
        });

        m_renderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(1000);
                nRender();
            }
        });

        m_runThread.start();
        m_renderThread.start();
    }

    private void changeRomThread(final String path) {
        if(m_runThread != null) {
            nStop();

            try {
                m_runThread.join();
                m_renderThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        startThread(path);
    }

    private void displayFps(final ActionBar actionBar) {
        new android.os.Handler().postDelayed(
            new Runnable() {
                public void run() {
                    int fps = nGetFPS();
                    int renderFps = nGetRenderFPS();
                    actionBar.setSubtitle("FPS - Game: " + fps + " Screen: " + renderFps);
                    displayFps(actionBar);
                }
            }, FPS_INTERVAL);
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
                    nSetButtonDown(m_button, true);
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            nSetButtonDown(m_button, false);
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
                    nSetButtonDown(m_buttonDown, true);
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            nSetButtonDown(m_buttonDown, false);
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
