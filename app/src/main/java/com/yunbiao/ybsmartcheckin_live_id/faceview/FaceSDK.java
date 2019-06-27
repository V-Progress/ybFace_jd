package com.yunbiao.ybsmartcheckin_live_id.faceview;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.jdjr.risk.face.local.active.DeviceActiveManager;
import com.jdjr.risk.face.local.frame.FaceFrameManager;
import com.jdjr.risk.face.local.service.FaceLocalService;
import com.jdjr.risk.face.local.settings.FaceSettings;
import com.jdjr.risk.face.local.user.FaceUser;
import com.jdjr.risk.face.local.user.FaceUserManager;
import com.yunbiao.ybsmartcheckin_live_id.APP;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FaceSDK {
    public static final String USER_INFO = "QjFDOTM2MkFDMkU1MzNGQzI2MkVBOTdDOTZDRjJGMjhCOUY2MEZCNzNCOTMwRTZBQjBEOTU5QjBFNkRCNzg2NXxNSUlCSURBTkJna3Foa2lHOXcwQkFRRUZBQU9DQVEwQU1JSUJDQUtDQVFFQTQ0ZElEQnVLclQ4U1JpaDMvT3hLVjhQdE1LUTBsNlU3YnpBTGNYeDBPQldSVWJLWS9IVkVOVUxFUVhBTGs4Wm5sdFFpWk9nSFdHSjRnRUVJME0xQ1N0Z2RRWFdLM0xjY3B1UHVlR00vTGsrdXVHb2JGanUyVlUwckZTeEh4cmxaZHpuSW9nZ1VNSjdmWTRlc2VKbXJZWWJoQmtib0J2b0RIbm5QWTFOZ3pqVVVSUkpITjUxdFRmNWRhNnZ5TTJFVU1iTUQ5bE8vSXdoOWxzb2oxelRGYk42V1ppZmdaK043aHprUHVuV2tuTmZwVmJCamdvRndZQk85aGhOMHZZakVOYzVnM1VoZU1QTFVFL21xbE9OYlNRSmxTdE8zZ2ozUy9oTUs1V1ByaTRQNkxYQkZPSkdZZnJwYzJ3ckwvd0hjNkc2cjFHdTh3enpuc3JSeFdBTTZGd0lCQXc9PXxNSUlCTXpDQjdBWUhLb1pJemowQ0FUQ0I0QUlCQVRBc0JnY3Foa2pPUFFFQkFpRUEvLy8vL3YvLy8vLy8vLy8vLy8vLy8vLy8vLzhBQUFBQS8vLy8vLy8vLy84d1JBUWcvLy8vL3YvLy8vLy8vLy8vLy8vLy8vLy8vLzhBQUFBQS8vLy8vLy8vLy93RUlDanArcDZkbjE0MFRWcWVTODlsQ2Fmemw0bjFGYXVQa3QyOHZVRk5sQTZUQkVFRU1zU3VMQjhaZ1JsZm1RUkdham5KbEkvakM3L3laZ3ZoY1ZwRmlUTk1kTWU4TnphaTlQWjNuRm05enVOcmFTRlQwS21IZk1ZcVIwQUMzekxsSVRud29BSWhBUC8vLy83Ly8vLy8vLy8vLy8vLy8vOXlBOTlySWNZRksxTzc5QWs1MVVFakFnRUJBMElBQlBmT0FVOEp1UHVyOW1VWCtkSnVHWTJJRkpqNE1hTGpoTXNoUXUwYlI2Sld0TXJoSlpOcXhWYXRPZDN4ZGhBNGd1YStReEZxejM0VFhwdC9jbWluT1E0PQ==";
    public static final String activeCode = "45e438f2402a4aefaaf2a2f84904ba70";

    private static final String TAG = "FaceSDK";
    private static FaceSDK instance = new FaceSDK();
    private Context mContext;
    private String captureDir;
    public static FaceSDK instance(){
        return instance;
    }

    public static final int STATE_ACTIVE_FAILED = -1;//激活失败(结束)
    public static final int STATE_SERVER_FAILED = -2;//服务启动失败(结束)
    public static final int STATE_NOT_INIT = 0;//未初始化
    public static final int STATE_ACTIVED = 1;//已激活
    public static final int STATE_COMPLETE = 2;//已启动(结束)

    private static int STATE_SDK = STATE_NOT_INIT;

    private Map<String,FaceUser> allUserMap = new HashMap<>();

    public static int getState(){
        return STATE_SDK;
    }

    private FaceSDK(){
        mContext = APP.getContext();
        captureDir = mContext.getDir("CaptureImageDir", Context.MODE_PRIVATE).getAbsolutePath();
    }

    private void listen(int state){
        if(mListener != null){
            mListener.onStateChanged(state);
        }
    }

    public void init(){
        if (checkActiveState()) {
            STATE_SDK = STATE_ACTIVED;
            listen(STATE_SDK);
            configSDK();
        } else {
            active(new Runnable() {
                @Override
                public void run() {
                    configSDK();
                }
            });
        }
    }

    private boolean checkActiveState(){
        FaceLocalService.getInstance().registerUserOnce(mContext,USER_INFO);
        return FaceLocalService.getInstance().isDeviceActive(mContext, USER_INFO);
    }

    public void configSDK(){
        FaceSettings.setVerifyFace(true);//人脸识别
        FaceSettings.setMultipleFace(true);//多人识别
        FaceSettings.setMinFaceArea(0);//最小识别区域
        FaceSettings.setExtractProperty(true);//抽取属性
        FaceSettings.setOnlyEmotion(false);//只抽取表情(开启以后将无法检测到性别和年龄)
        FaceSettings.setCaptureFace(false);//保存人脸
        FaceSettings.setCaptureDir(null);
        FaceSettings.setPullAuto(false);
        FaceSettings.setRecordVerify(false);
//        FaceSettings.setCheckType(FaceSettings.CHECK_NONE);//关闭防伪
        FaceSettings.setCheckType(FaceSettings.CHECK_RGB);//RGB防伪（只彩色防伪，防伪效果差）
        //FaceSettings.setCheckType(FaceSettings.CHECK_NIR);//NIR防伪（防伪效果好）

        startFaceService();
    }

    private void active(final Runnable runnable){
        Log.e(TAG, "-----" + USER_INFO);
        Log.e(TAG, "-----" + activeCode);
        DeviceActiveManager.getInstance().activeDevice(mContext, USER_INFO, activeCode, new DeviceActiveManager.ActiveCallback() {
            @Override
            public void onActiveResult(boolean enable) {
                Log.d(TAG, ".......... 激活结果 = " + enable);
                if (enable) {
                    STATE_SDK = STATE_ACTIVED;
                    listen(STATE_SDK);

                    if(runnable != null){
                        runnable.run();
                    }
                } else {
                    STATE_SDK = STATE_ACTIVE_FAILED;
                    listen(STATE_SDK);
                    Log.d(TAG, ".......... 激活失败" );
                }
            }
        });
    }

    private void startFaceService(){
        FaceLocalService.getInstance().initSDK(mContext, USER_INFO, new FaceLocalService.InitCallback() {
            @Override
            public void onInitResult(boolean b, int i, String s) {
                if(b){
                    Log.d(TAG, ".......... 启动人脸服务成功");
                    if (FaceSettings.isPullAuto()) {
                        FaceLocalService.getInstance().startPullService();
                    }

                    STATE_SDK = STATE_COMPLETE;
                    listen(STATE_SDK);
                    startDetect();
                    getAllUser();
                } else {
                    STATE_SDK = STATE_SERVER_FAILED;
                    listen(STATE_SDK);
                    Log.d(TAG, ".......... 启动人脸服务失败!!!!!!!!!!");
                }
            }
        });
    }

    private void startDetect(){
        FaceFrameManager.setFaceType(FaceFrameManager.TYPE_FACE_PROCESS);
        FaceFrameManager.startDetectFace();
    }

    private void getAllUser(){
        List<FaceUser> faceUserList = FaceUserManager.getAllUsersSync(mContext);
        for (FaceUser faceUser : faceUserList) {
            allUserMap.put(faceUser.getUserId(),faceUser);
        }
        Log.e(TAG, "getAllUser: " + allUserMap.toString());
    }

    public void setCallback(FaceFrameManager.BasePropertyCallback basePropertyCallback
            , FaceFrameManager.FacePropertyCallback facePropertyCallback
            , FaceFrameManager.VerifyResultCallback verifyResultCallback){
        FaceFrameManager.setBasePropertyCallback(basePropertyCallback);
        FaceFrameManager.setFacePropertyCallback(facePropertyCallback);
        FaceFrameManager.setVerifyResultCallback(verifyResultCallback);
    }

    private SDKStateListener mListener;
    public void setActiveListener(SDKStateListener activeListener){
        mListener = activeListener;
    }

    public interface SDKStateListener{
        void onStateChanged(int state);
    }

    public void addUser(String userId, String imgPath, FaceUserManager.FaceUserCallback callback){
        if(TextUtils.isEmpty(userId) || TextUtils.isEmpty(imgPath)){
            Log.e(TAG, "addUser: 添加失败，userId或imgPath不可为空！！！");
            if(callback != null){
                callback.onUserResult(false, FaceUserManager.RESULT_FAILURE);
            }
            return;
        }
        FaceUserManager.getInstance().registerImageAsync(mContext, userId, imgPath, null, callback);
    }

    public void updateUser(String userId,String imgPath,FaceUserManager.FaceUserCallback callback){
        if(TextUtils.isEmpty(userId) || TextUtils.isEmpty(imgPath)){
            if(callback != null){
                callback.onUserResult(false, FaceUserManager.RESULT_FAILURE);
            }
            return;
        }
        if(allUserMap.containsKey(userId)){
            FaceUser faceUser = allUserMap.get(userId);
            faceUser.setImagePath(imgPath);
            FaceUserManager.getInstance().updateUserAsync(mContext,faceUser,callback);
        } else {
            if(callback != null){
                callback.onUserResult(false, FaceUserManager.RESULT_FAILURE);
            }
        }
    }

    public boolean removeUser(String userId){
        if(!TextUtils.isEmpty(userId)){
            if(allUserMap.containsKey(userId)){
                FaceUser faceUser = allUserMap.get(userId);
                return FaceUserManager.getInstance().removeUserSync(mContext,faceUser);
            }
        }
        return false;
    }

    public void removeAllUser(FaceUserManager.FaceUserCallback callback){
        FaceUserManager.getInstance().removeAllUsersAsync(mContext,callback);
    }
}
