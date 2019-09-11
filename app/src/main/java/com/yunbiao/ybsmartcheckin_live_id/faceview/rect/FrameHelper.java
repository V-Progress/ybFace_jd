package com.yunbiao.ybsmartcheckin_live_id.faceview.rect;

import com.yunbiao.ybsmartcheckin_live_id.faceview.camera.CameraSettings;

/**
 * Created by michael on 19-5-6.
 */

public class FrameHelper {
    public static byte[] getFrameRotate(byte[] frame, int width, int height) {
        
        final int rotation = CameraSettings.getCameraImageRotation();
        
        final byte[] frameRotate = rotateFrame(rotation, frame, width, height);
        if (rotation == CameraSettings.ROTATION_90 || rotation == CameraSettings.ROTATION_270) {
            CameraSettings.setCameraWidth(height);
            CameraSettings.setCameraHeight(width);
        }
        
        
        return frameRotate;
    }
    
    
    private static byte[] rotateFrame(int rotation, byte[] frame, int width, int height) {
        byte[] frameRotate = frame;
        
        if (rotation == CameraSettings.ROTATION_0) {
            return frame;
        }
        else if (rotation == CameraSettings.ROTATION_90) {
            frameRotate = NV21Util.NV21_rotate_to_90(frame, width, height);
        }
        else if (rotation == CameraSettings.ROTATION_180) {
            frameRotate = NV21Util.NV21_rotate_to_180(frame, width, height);
        }
        else if (rotation == CameraSettings.ROTATION_270) {
            frameRotate = NV21Util.NV21_rotate_to_270(frame, width, height);
        }
        else {
            // do nothing
        }
        
        return frameRotate;
        
        
    }
    


}
