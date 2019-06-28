package com.yunbiao.ybsmartcheckin_live_id.common;

import android.app.ProgressDialog;


import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.activity.SystemActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.common.power.PowerOffTool;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.heartbeat.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.business.AdsManager;
import com.yunbiao.ybsmartcheckin_live_id.utils.*;
import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.logutils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * xmpp消息处理
 *
 * @author Administrator
 */
public class CoreInfoHandler {
    private static final String TAG = "CoreInfoHandler";

    private static final int ONLINE_TYPE = 1;// 上线
    private static final int VOICE_TYPE = 3;// 声音
    private static final int CUTSCREN_TYPE = 4;// 截屏
    private static final int RUNSET_TYPE = 5;// 设备开关机设置
    private static final int SHOW_SERNUM = 6;// 显示设备编号
    private static final int SHOW_VERSION = 7;// 显示版本号
    private static final int SHOW_DISK_IFNO = 8;// 获取磁盘容量
    private static final int POWER_RELOAD = 9;// 设备 开机 重启
    private static final int PUSH_TO_UPDATE = 10;//软件升级
    private final static int ADS_PUSH = 23;
    private final static int UPDATE_STAFF = 26;//员工信息更新

    public static boolean isOnline = false;

    public static void messageReceived(String message) {
        LogUtils.e(TAG, "接收消息：" + message);
        try {
            JSONObject mesJson = new JSONObject(message);
            Integer type = mesJson.getInt("type");
            JSONObject contentJson;
            switch (type.intValue()) {
                case ONLINE_TYPE:
                    // 系统登录
                    contentJson = mesJson.getJSONObject("content");
                    if (!contentJson.isNull("serNum")) {
                        MachineDetial.getInstance().upLoadHardWareMessage();

                        String bindCode = CommonUtils.getJsonObj(contentJson, "pwd", "").toString();
                        SpUtils.saveStr(SpUtils.BINDCODE,bindCode);
//                     //第一次系统启动的时候服务器没有设备详细信息，需要向设备传消息
                        String serNum = (String) CommonUtils.getJsonObj(contentJson, "serNum", "");
                        SpUtils.saveStr(SpUtils.DEVICE_NUMBER, serNum);
                        //设备过期时间
                        String expireDate = CommonUtils.getJsonObj(contentJson, "expireDate", "").toString();
                        SpUtils.saveStr(SpUtils.EXP_DATE,expireDate);
                        isOnline = true;

                        EventBus.getDefault().postSticky(new SystemActivity.UpdateEvent());
                    }

                    String runKey = (String) CommonUtils.getJsonObj(contentJson, "runKey", "");
                    SpUtils.saveStr(SpUtils.RUN_KEY,runKey);

                    Integer dtype = (Integer) CommonUtils.getJsonObj(contentJson, "dtype", -1);
                    SpUtils.saveInt(SpUtils.DEVICE_TYPE,dtype);
                    break;

                case VOICE_TYPE:// 声音控制
                    JSONObject jsonObject = mesJson.getJSONObject("content");
                    if (jsonObject != null) {
                        SoundControl.setMusicSound(jsonObject.getDouble("voice"));
                    }
                    break;
                case CUTSCREN_TYPE:
                    final ScreenShotUtil instance = ScreenShotUtil.getInstance();
                    instance.takeScreenshot(APP.getContext(), new ScreenShotUtil.ScreenShotCallback() {
                        @Override
                        public void onShotted(boolean isSucc, String filePath) {
                            String sid = HeartBeatClient.getDeviceNo();
                            instance.sendCutFinish(sid,filePath);
                        }
                    });
                    break;
                case RUNSET_TYPE:
                    ThreadUitls.runInThread(new Runnable() {
                        @Override
                        public void run() {// 开关机时间设置
                            PowerOffTool.getPowerOffTool().getPowerOffTime(HeartBeatClient.getDeviceNo());
                        }
                    });
                    break;
                case SHOW_SERNUM:
                    contentJson = (JSONObject) CommonUtils.getJsonObj(mesJson, "content", null);
                    if (contentJson != null) {
                        Integer showType = (Integer) CommonUtils.getJsonObj(contentJson, "showType", null);
                        if (showType != null && showType == 0) {//状态栏  视美泰主板
                            Integer showValue = (Integer) CommonUtils.getJsonObj(contentJson, "showValue", null);
                            if (showValue == 0) {//显示
                                APP.getSmdt().smdtSetStatusBar(APP.getContext().getApplicationContext(), true);
                            } else if (showValue == 1) {//隐藏
                                APP.getSmdt().smdtSetStatusBar(APP.getContext().getApplicationContext(), false);
                            }
                        } else { // 显示设备编号
                            UIUtils.showTitleTip(SpUtils.getStr(SpUtils.DEVICE_NUMBER));
                        }
                    }
                    break;
                case SHOW_VERSION:// 版本信息
                    ResourceUpdate.uploadAppVersion();
                    break;
                case SHOW_DISK_IFNO:
                    contentJson = mesJson.getJSONObject("content");
                    Integer flag = (Integer) CommonUtils.getJsonObj(contentJson, "flag", null);
                    if (flag != null) {
                        if (flag == 0) { //显示
                            ResourceUpdate.uploadDiskInfo();
                        } else if (flag == 1) {// 清理磁盘
                            ResourceUpdate.uploadDiskInfo();
                        }
                    }
                    break;
                case POWER_RELOAD:// 机器重启
                    contentJson = mesJson.getJSONObject("content");
                    Integer restart = (Integer) CommonUtils.getJsonObj(contentJson, "restart", null);
                    if (restart != null) {
                        if (restart == 0) {
                            ProgressDialog progressDialog = UIUtils.coreInfoShow3sDialog();
                            progressDialog.setTitle("关机");
                            progressDialog.setMessage("3秒后将关闭设备");
                            progressDialog.show();
                            UIUtils.powerShutDown.start();
                        } else if (restart == 1) {
                            ProgressDialog progressDialog = UIUtils.coreInfoShow3sDialog();
                            progressDialog.setTitle("重启");
                            progressDialog.setMessage("3秒后将重启设备");
                            progressDialog.show();
                            UIUtils.restart.start();
                        }
                    }
                    break;
                case PUSH_TO_UPDATE:
                    UIUtils.updatePd();
                    UpdateVersionControl.getInstance().checkUpdate();
                    setOnReceivedProgressRun(new OnReceivedProgressRun() {
                        @Override
                        public void OnProgressRunReceived(int progress) {
                            UIUtils.pd.setProgress(progress);//给进度条设置数值
                            if (progress == 100) {
                                UIUtils.pd.dismiss();
                            }
                        }
                    });
                    break;
                case ADS_PUSH:
                    AdsManager.instance().requestAdsData();
                    break;
                case UPDATE_STAFF:
                    SyncManager.instance().initInfo();
                    break;
                default:
                    break;
            }
        } catch (JSONException /*| UnsupportedEncodingException*/ e) {
            e.printStackTrace();
        }
    }

    public interface OnReceivedProgressRun {
        void OnProgressRunReceived(int progress);
    }

    public static OnReceivedProgressRun onReceivedProgressRun;

    public static void setOnReceivedProgressRun(OnReceivedProgressRun onReceivedProgressRun) {
        CoreInfoHandler.onReceivedProgressRun = onReceivedProgressRun;
    }
}