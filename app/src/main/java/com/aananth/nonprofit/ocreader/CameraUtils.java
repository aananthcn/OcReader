package com.aananth.nonprofit.ocreader;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;

public class CameraUtils {
    final static String TAG = "DBG_" + CameraUtils.class.getName();

    //Check if the device has a camera
    public static boolean deviceHasCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    //Get available camera
    public static Camera getCamera() {

        if (deviceHasCamera(MainActivity.getAppContext())) {
            try {
                return Camera.open();
            } catch (Exception e) {
                Log.e(TAG, "Exception: Cannot getCamera()");
            }
        }
        else {
            Log.d(TAG, "This device has no camera!");
        }

        return null;
    }
}
