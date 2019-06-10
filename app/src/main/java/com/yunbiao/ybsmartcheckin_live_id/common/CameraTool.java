package com.yunbiao.ybsmartcheckin_live_id.common;

import android.hardware.Camera;
import android.os.Build;

/**
 * Created by Administrator on 2017/8/16.
 */

public class CameraTool {
    /**
     * 判断前后摄像头
     */
    private static boolean checkCameraFacing(final int facing) {
        if (getSdkVersion() < Build.VERSION_CODES.GINGERBREAD) {
            return false;
        }
        final int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, info);
            if (facing == info.facing) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasBackFacingCamera() {
        final int CAMERA_FACING_BACK = 0;
        return checkCameraFacing(CAMERA_FACING_BACK);
    }

    private static boolean hasFrontFacingCamera() {
        final int CAMERA_FACING_BACK = 1;
        return checkCameraFacing(CAMERA_FACING_BACK);
    }

    public static int getCamera() {
        if (hasFrontFacingCamera()) {
            return 1;
        } else if (hasBackFacingCamera()) {
            return 0;
        }
        return -1;
    }

    private static int getSdkVersion() {
        return Build.VERSION.SDK_INT;
    }

}
