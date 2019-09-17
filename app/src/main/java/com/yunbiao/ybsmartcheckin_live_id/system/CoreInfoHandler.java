package com.yunbiao.ybsmartcheckin_live_id.system;

import android.app.ProgressDialog;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.AdsUpdateEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.WelComeActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.fragment.InformationFragment;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.bean.DeviceInfoBean;
import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.common.MachineDetial;
import com.yunbiao.ybsmartcheckin_live_id.common.SoundControl;
import com.yunbiao.ybsmartcheckin_live_id.common.UpdateVersionControl;
import com.yunbiao.ybsmartcheckin_live_id.common.power.PowerOffTool;
import com.yunbiao.ybsmartcheckin_live_id.utils.ScreenShotUtil;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.ThreadUitls;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.logutils.LogUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;

import okhttp3.Call;

/**
 * xmpp消息处理
 *
 * @author Administrator
 */
public class CoreInfoHandler {
    private static final String TAG = "CoreInfoHandler";

    public static String deviceNum = "";

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
    private final static int UPDATE_INFO = 33; //修改了设备宣传目录.

    public static boolean isOnline = false;

    public static void messageReceived(String message) {
        LogUtils.e(TAG, "接收消息：" + message);
        try {
            DeviceInfoBean deviceInfoBean = new Gson().fromJson(message, DeviceInfoBean.class);
            DeviceInfoBean.Content content = deviceInfoBean.getContent();

            switch (deviceInfoBean.getType()) {
                case ONLINE_TYPE:
                    MachineDetial.getInstance().upLoadHardWareMessage();
                    // 系统登录
                    if(content != null){
                        SpUtils.saveStr(SpUtils.BINDCODE,content.getPwd());
                        SpUtils.saveStr(SpUtils.DEVICE_NUMBER,content.getSerNum());
                        SpUtils.saveStr(SpUtils.EXP_DATE,content.getExpireDate());
                        SpUtils.saveStr(SpUtils.RUN_KEY,content.getRunKey());
                        SpUtils.saveInt(SpUtils.DEVICE_TYPE,content.getDtype());
                    }

                    updateDeviceType();

                    isOnline = true;
                    EventBus.getDefault().post(new UpdateInfoEvent());
                    break;
                case VOICE_TYPE:// 声音控制
                    if(content != null){
                        SoundControl.setMusicSound(content.getVoice());
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
                    if (content != null) {
                        UIUtils.showTitleTip(APP.getContext(), SpUtils.getStr(SpUtils.DEVICE_NUMBER));
                    }
                    break;
                case SHOW_VERSION:// 版本信息
                    ResourceUpdate.uploadAppVersion();
                    break;
                case SHOW_DISK_IFNO:
                    if(content == null){
                        break;
                    }
                    if (content.getFlag() == 0) { //显示
                        ResourceUpdate.uploadDiskInfo();
                    } else if (content.getFlag() == 1) {// 清理磁盘
                        ResourceUpdate.uploadDiskInfo();
                    }
                    break;
                case POWER_RELOAD:// 机器重启
                    if(content == null){
                        break;
                    }

                    if (content.getRestart() == 0) {
                        ProgressDialog progressDialog = UIUtils.coreInfoShow3sDialog(BaseActivity.getActivity());
                        progressDialog.setTitle("关机");
                        progressDialog.setMessage("3秒后将关闭设备");
                        progressDialog.show();
                        UIUtils.powerShutDown.start();
                    } else if (content.getRestart() == 1) {
                        ProgressDialog progressDialog = UIUtils.coreInfoShow3sDialog(BaseActivity.getActivity());
                        progressDialog.setTitle("重启");
                        progressDialog.setMessage("3秒后将重启设备");
                        progressDialog.show();
                        UIUtils.restart.start();
                    }
                    break;
                case PUSH_TO_UPDATE:
                    WelComeActivity activity = APP.getActivity();
                    UpdateVersionControl.getInstance().checkUpdate(activity);
                    break;
                case ADS_PUSH:
                    EventBus.getDefault().postSticky(new AdsUpdateEvent());
                    break;
                case UPDATE_STAFF:
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SyncManager.instance().syncDB();
                        }
                    },2000);
                    break;
                case UPDATE_INFO:
                    EventBus.getDefault().postSticky(new InformationFragment.InformationUpdateEvent());
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void updateDeviceType() {
        OkHttpUtils.post()
                .url(ResourceUpdate.UPDATE_DEVICE_TYPE)
                .addParams("deviceNo", HeartBeatClient.getDeviceNo())
                .addParams("type", "1 ")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "onError: " + (e == null ? "NULL" : e.getMessage()));
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.e(TAG, "onResponse: " + response);
                    }
                });
    }
}