package com.yunbiao.ybsmartcheckin_live_id;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.bean.ActiveCodeResponse;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import timber.log.Timber;

public class FaceSDKActive {
    private static String wifiMac = "";
    private static String localMac = "";
    public static final int TYPE_LOCAL = 0;
    public static final int TYPE_REMOTE = 1;
    private static ActiveCallback cb;
    private static String ACTIVE_FILE_NAME = "33C096899E53BE44705CD53F68190A75";

    public static String YB_APPID = "HaViJoN9enHzZjfE9nb6G672dQxgRuJe8mVJS3nuNX7Q";
    public static String YB_SDKKEY = "212ZpN5Yu7H3NKNhsM8mYqzJn6bpwcHPiSvf8AmnMRLs";

    public static String HSD_APPID = "AYuHvcMYBDjwqVXRHkGTvQzyRndUfjgGmrcfKTZHk8bf";
    public static String HSD_SDKKEY = "2foqrfXF7oF8WDj17ZiFyAhoRZbAJn3DsAZCEYpkVXdE";

    public static void activeLocal(String appId,String sdkKey, @NonNull ActiveCallback callback){
        cb = callback;
        wifiMac = CommonUtils.getWifiMac();
        if (!TextUtils.isEmpty(wifiMac)) {
            wifiMac = wifiMac.replace(":", "-");
        } else {
            wifiMac = "";
        }
        localMac = CommonUtils.getLocalMac();
        if (!TextUtils.isEmpty(localMac)) {
            localMac = localMac.replace(":", "-");
        } else {
            localMac = "";
        }

        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
        int code = FaceEngine.getActiveFileInfo(APP.getContext(), activeFileInfo);
        if(code == ErrorInfo.MOK
                && !TextUtils.isEmpty(activeFileInfo.getAppId())
                && !TextUtils.isEmpty(activeFileInfo.getSdkKey())){
            int activeCode = FaceEngine.active(APP.getContext(), activeFileInfo.getAppId(), activeFileInfo.getSdkKey());
            if(activeCode == ErrorInfo.MOK || activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED){
                writeActiveInfo(activeFileInfo.getAppId(),activeFileInfo.getSdkKey());
                cb.onActiveResult(true,"");
                return;
            }
        } else {
            AF af = readActiveFile();
            if(af != null){
                int activeCode = FaceEngine.active(APP.getContext(), af.appId, af.sdkKey);
                if(activeCode == ErrorInfo.MOK || activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED){
                    writeActiveInfo(activeFileInfo.getAppId(),activeFileInfo.getSdkKey());
                    cb.onActiveResult(true,"");
                    return;
                }
            }
        }

        activeSDK(appId, sdkKey);
    }

    public static void active(boolean canGo, @NonNull ActiveCallback callback){
        cb = callback;
        wifiMac = CommonUtils.getWifiMac();
        if (!TextUtils.isEmpty(wifiMac)) {
            wifiMac = wifiMac.replace(":", "-");
        } else {
            wifiMac = "";
        }
        localMac = CommonUtils.getLocalMac();
        if (!TextUtils.isEmpty(localMac)) {
            localMac = localMac.replace(":", "-");
        } else {
            localMac = "";
        }

        if(!canGo){
            Timber.d("不激活，回调结果");
            cb.onActiveResult(true,"");
            return;
        }

        Timber.d("检查虹软激活信息");
        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
        int code = FaceEngine.getActiveFileInfo(APP.getContext(), activeFileInfo);
        if(code == ErrorInfo.MOK
                && !TextUtils.isEmpty(activeFileInfo.getAppId())
                && !TextUtils.isEmpty(activeFileInfo.getSdkKey())){
            int activeCode = FaceEngine.active(APP.getContext(), activeFileInfo.getAppId(), activeFileInfo.getSdkKey());
            Timber.d("激活结果：" + activeCode);
            if(activeCode == ErrorInfo.MOK || activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED){
                writeActiveInfo(activeFileInfo.getAppId(),activeFileInfo.getSdkKey());
                cb.onActiveResult(true,"");
                return;
            }
        } else {
            AF af = readActiveFile();
            Timber.d("检查文件：" + (af == null ? "NULL" : af.toString()));
            if(af != null){
                int activeCode = FaceEngine.active(APP.getContext(), af.appId, af.sdkKey);
                if(activeCode == ErrorInfo.MOK || activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED){
                    Timber.d("激活结果：" + activeCode);
                    writeActiveInfo(activeFileInfo.getAppId(),activeFileInfo.getSdkKey());
                    cb.onActiveResult(true,"");
                    return;
                }
                Timber.d("文件激活失败");
            }
        }

        requestAppIdAndKey();
    }

    private static void requestAppIdAndKey(){
        Timber.d("获取激活信息");
        String url = ResourceUpdate.GET_ACTIVE_CODE;
        Map<String,String> params = new HashMap<>();
        params.put("deviceNo", HeartBeatClient.getDeviceNo());
        params.put("type", "0");
        params.put("wifiMac", wifiMac);
        params.put("localMac", localMac);
        OkHttpUtils.post().url(url).params(params).build()
                .connTimeOut(10000).writeTimeOut(10000).readTimeOut(10000)
                .execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Timber.d("获取失败：" + (e == null ? "NULL" : e.getMessage()));
                cb.onActiveResult(false,(e == null ? "NetWorkException" : e.getMessage()));
            }

            @Override
            public void onResponse(String response, int id) {
                Timber.d("获取结果：" +  response);
                if (TextUtils.isEmpty(response)) {
                    cb.onActiveResult(false,"response is null");
                    return;
                }
                try {
                    ActiveCodeResponse activeCodeResponse = new Gson().fromJson(response, ActiveCodeResponse.class);
                    if (activeCodeResponse.getStatus() != 1) {
                        cb.onActiveResult(false,activeCodeResponse.getMessage());
                        return;
                    }
                    activeSDK(activeCodeResponse.getAppid(),activeCodeResponse.getSdk_key());
                } catch (Exception e) {
                    e.printStackTrace();
                    cb.onActiveResult(false,(e == null ? "JSONException" : e.getMessage()));
                }
            }
        });
    }

    private static void activeSDK(String appId,String sdkKey){
        int code = FaceEngine.active(APP.getContext(), appId, sdkKey);
        if(code == ErrorInfo.MOK || code == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED){
            writeActiveInfo(appId,sdkKey);
        }
        cb.onActiveResult(code == ErrorInfo.MOK || code == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED,String.valueOf(code));
    }

    public interface ActiveCallback{
        void onActiveResult(boolean result,String message);
    }

    private static void writeActiveInfo(String appId, String sdkKey){
        File acDir = new File(Constants._T);
        if(!acDir.exists()){
            acDir.mkdirs();
        }

        File activeFile = new File(acDir,ACTIVE_FILE_NAME);
        if(!activeFile.exists()){
            try {
                activeFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                String jsonStr = new Gson().toJson(new AF(appId, sdkKey));
                try {
                    byte[] decode = Base64.encode(jsonStr.getBytes(), Base64.DEFAULT);
                    String decodeStr = new String(decode, StandardCharsets.UTF_8);
                    FileUtils.writeStringToFile(activeFile,decodeStr,StandardCharsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    private static AF readActiveFile(){
        File acDir = new File(Constants._T);
        if(!acDir.exists()){
            return null;
        }
        File activeFile = new File(acDir,ACTIVE_FILE_NAME);
        if(!activeFile.exists()){
            return null;
        }

        try {
            String string = FileUtils.readFileToString(activeFile, StandardCharsets.UTF_8);
            if(TextUtils.isEmpty(string)){
                return null;
            }
            byte[] decode = Base64.decode(string, Base64.DEFAULT);
            String jsonStr = new String(decode,StandardCharsets.UTF_8);
            try{
                AF af = new Gson().fromJson(jsonStr, AF.class);
                return af;
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class AF{
        String appId;
        String sdkKey;

        public AF(String appId, String sdkKey) {
            this.appId = appId;
            this.sdkKey = sdkKey;
        }

        @Override
        public String toString() {
            return "AF{" +
                    "appId='" + appId + '\'' +
                    ", sdkKey='" + sdkKey + '\'' +
                    '}';
        }
    }
}
