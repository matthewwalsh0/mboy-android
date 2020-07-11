package com.matthew.mboy;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int SCREEN_WIDTH = 160;
    private static final int SCREEN_HEIGHT = 144;
    private static final float ASPECT_RATIO = SCREEN_HEIGHT / (float) SCREEN_WIDTH;
    private static final List<String> PERMISSIONS;
    private static String ROM_PATH;

    static {
        PERMISSIONS = new ArrayList<String>();
        PERMISSIONS.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        ROM_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                "/Tetris (World) (Rev A).gb";

        System.loadLibrary("native-lib");
    }

    public native void startEmulator(Object surface, String path);
    public native int getFPS();
    public native int getRenderFPS();

    private PermissionHelper m_permissionHelper;

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
        AsyncTask.execute(new Runnable() {
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
        });

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
}
