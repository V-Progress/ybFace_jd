package com.yunbiao.ybsmartcheckin_live_id.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Created by LiuShao on 2016/3/7.
 */
public class RotateScreen {
    private static RotateScreen rotateScreen;
    public static RotateScreen getInstance(){
        if(rotateScreen==null){
            rotateScreen = new RotateScreen();
        }
        return rotateScreen;
    }

    public void rotateScreen(String value){
        Process process = null;
        DataOutputStream os = null;
        DataInputStream is = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("setprop persist.sys.hwrotation "+value+" \n");
            os.writeBytes("reboot \n");
            os.writeBytes("exit\n");
            os.flush();
            int aa = process.waitFor();
            is = new DataInputStream(process.getInputStream());
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            String out = new String(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
    }


}
