package com.matthew.mboy;

import android.os.SystemClock;
import android.view.SurfaceView;

public class Gameboy {

    public static final int BUTTON_START = 0;
    public static final int BUTTON_SELECT = 1;
    public static final int BUTTON_A = 2;
    public static final int BUTTON_B = 3;
    public static final int BUTTON_UP = 4;
    public static final int BUTTON_RIGHT = 5;
    public static final int BUTTON_DOWN = 6;
    public static final int BUTTON_LEFT = 7;

    static {
        System.loadLibrary("native-lib");
    }

    private native void nInit();
    private native void nSetSurface(Object surface);
    private native void nStart(String path);
    private native void nStop();
    private native void nPause();
    private native void nContinue();
    private native void nRender();
    private native void nSetButtonDown(int button, boolean state);

    public native int getFPS();
    public native int getRenderFPS();
    public native void setTurbo(boolean value);
    public native void setBackgroundEnabled(boolean value);
    public native void setWindowEnabled(boolean value);
    public native void setSpritesEnabled(boolean value);
    public native void setAudioEnabled(boolean value);
    public native void setSquare1Enabled(boolean value);
    public native void setSquare2Enabled(boolean value);
    public native void setWaveEnabled(boolean value);

    private Boolean m_started;
    private Boolean m_paused;
    private Thread m_runThread;
    private Thread m_renderThread;
    private Runnable m_onReady;

    public Gameboy() {
        m_started = false;
        m_paused = false;
    }

    public Gameboy withOnReady(Runnable onReady) {
        m_onReady = onReady;
        return this;
    }

    public boolean isStarted() {
        return m_started;
    }

    public boolean isPaused() {
        return m_paused;
    }

    public void start(final String path) {
        if(m_started) return;
        nInit();
        if(m_onReady != null) m_onReady.run();
        startThread(path);
        m_started = true;
        m_paused = false;
    }

    public void setSurface(SurfaceView view) {
        nSetSurface(view.getHolder().getSurface());
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

        m_paused = true;
    }

    public void continueEmulator() {
        if(m_runThread == null) return;
        continueThread();
        m_paused = false;
    }

    public void setButtonDown(int button, boolean down) {
        nSetButtonDown(button, down);
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
}
