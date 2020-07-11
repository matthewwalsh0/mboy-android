package com.matthew.mboy;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class PermissionHelper {

    private static final int REQUEST_CODE = 1234;

    private Activity m_context;
    private List<String> m_permissions;
    private Runnable m_runnable;

    public PermissionHelper(Activity context, List<String> permissions, Runnable runnable) {
        m_context = context;
        m_permissions = permissions;
        m_runnable = runnable;
    }

    public void check() {
        boolean hasPermissions = true;

        for(String permission : m_permissions) {
            boolean hasPermission = ContextCompat.checkSelfPermission(m_context, permission) ==
                    PackageManager.PERMISSION_GRANTED;

            if(!hasPermission) {
                hasPermissions = false;
                break;
            }
        }

        if (!hasPermissions) {
            String[] permissionArray = new String[m_permissions.size()];
            m_permissions.toArray(permissionArray);
            ActivityCompat.requestPermissions(m_context, permissionArray, REQUEST_CODE);
        } else {
            m_runnable.run();
        }
    }

    public void onResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
                boolean granted = true;

                for(int i = 0; i < grantResults.length; i++) {
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        granted = false;
                        break;
                    }
                }

                if(granted) {
                    m_runnable.run();
                } else {
                    check();
                }
            }
        }
    }
}
