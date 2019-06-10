package com.yunbiao.ybsmartcheckin_live_id.faceview;

/**
 * Created by michael on 19-5-6.
 */

public class FrameHelper {
    public static byte[] getFrameRotate(byte[] frame, int width, int height) {
        final int rotation = CameraManager.getOrientation();
        final byte[] frameRotate = FrameHelper.rotateFrame(rotation, frame, width, height);
        if (rotation == 90 || rotation == 180) {
            CameraManager.setCameraSize(height,width);
        }
        return frameRotate;
    }

    private static byte[] rotateFrame(int rotation, byte[] frame, int width, int height) {
        byte[] frameRotate = frame;
        if (rotation == 0) {
            return frame;
        } else if (rotation == 90) {
            frameRotate = NV21Util.NV21_rotate_to_90(frame, width, height);
        } else if (rotation == 180) {
            frameRotate = NV21Util.NV21_rotate_to_180(frame, width, height);
        } else if (rotation == 270) {
            frameRotate = NV21Util.NV21_rotate_to_270(frame, width, height);
        } else {
            // do nothing
        }
        return frameRotate;
    }
}
