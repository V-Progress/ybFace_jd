package com.yunbiao.ybsmartcheckin_live_id.afinel;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.heartbeat.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.utils.SdCardUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.xutil.MyXutils;

import java.util.HashMap;
import java.util.Map;


public class ResourceUpdate {

    public static String WEB_BASE_URL = Constants.RESOURCE_URL;

    public static void initUrl() {
        WEB_BASE_URL = Constants.RESOURCE_URL;
        QRCODE_ADD = WEB_BASE_URL + "nhb/entryCode.html";
        COMPANYINFO = WEB_BASE_URL + "api/company/getcompany.html";//获取公司信息以及下的部门信息接口
        UPDATSTAFF = WEB_BASE_URL + "api/entry/entryupdate.html";//修改员工信息接口
        ADDSTAFF = WEB_BASE_URL + "api/entry/entryadd.html";//添加员工信息接口
        DELETESTAFF = WEB_BASE_URL + "api/entry/entrydelete.html";//删除员工接口
        GETSTAFF = WEB_BASE_URL + "api/entry/getentry.html";//获取员工信息接口
        SIGNLOG = WEB_BASE_URL + "api/sign/signlog.html";//创建签到信息接口
        BULUSIGN = WEB_BASE_URL + "api/entry/entryecord.html";//补录接口
        SIGNARRAY = WEB_BASE_URL + "api/sign/signlogByarray.html";//定时发送签到列表
        ADDDEPART = WEB_BASE_URL + "api/department/departmentadd.html";//创建部门
        DELETEDEPART = WEB_BASE_URL + "api/department/departmentdelete.html";//删除部门
        GETAD = WEB_BASE_URL + "api/company/getadvert.html";//获取广告
        getWeatherInfo = "http://www.yunbiaowulian.com/weather/city.html";
        RESOURCE_URL = WEB_BASE_URL + "device/service/getresource.html";
        LAYOUT_CHANGE_STATUS = WEB_BASE_URL + "device/service/layoutchangestatus.html";
        DEVICE_ONLINE_STATUS = WEB_BASE_URL + "device/status/getrunstatus.html";
        VERSION_URL = WEB_BASE_URL + "device/service/getversion.html";
        POWER_OFF_URL = WEB_BASE_URL + "device/service/poweroff.html";
        SCREEN_UPLOAD_URL = WEB_BASE_URL + "device/service/uploadScreenImg.html";
        RES_UPLOAD_URL = WEB_BASE_URL + "device/service/rsupdate.html";
        SER_NUMBER = WEB_BASE_URL + "device/status/getHasNumber.html";
        SCAN_TO_CALL = WEB_BASE_URL + "/mobilebusself/mobilebusselfpost/selectbyordersernum.html";
        SETTIME = WEB_BASE_URL + "common/service/getSystemTime.html";
        DEC_NUM = WEB_BASE_URL + "device/status/binduser.html";
        QRCODE = WEB_BASE_URL + "device/renewal/getopenrenewalqrcode.html";
        UPLOADFACE = WEB_BASE_URL + "visitors/saveVisitors.html";
        NETSPEEFURL = WEB_BASE_URL + "device/status/uploadnetspeed.html";
        VOLUME_URL = WEB_BASE_URL + "device/service/getVolume.html";
        UPLOAD_APP_VERSION_URL = WEB_BASE_URL + "device/service/uploadAppVersionNew.html";
        UPLOAD_DISK_URL = WEB_BASE_URL + "device/service/uploadDisk.html";
    }

    public static String HOLIREM = WEB_BASE_URL + "api/holidayrem/getHolirem.html";//节日提醒背景
    public static String JOINREM = WEB_BASE_URL + "api/holidayrem/getJoinrem.html";//生日提醒背景
    public static String QRCODE_ADD = WEB_BASE_URL + "nhb/entryCode.html";
    public static String COMPANYINFO = WEB_BASE_URL + "api/company/getcompany.html";//获取公司信息以及下的部门信息接口
    public static String UPDATSTAFF = WEB_BASE_URL + "api/entry/entryupdate.html";//修改员工信息接口
    public static String ADDSTAFF = WEB_BASE_URL + "api/entry/entryadd.html";//添加员工信息接口
    public static String DELETESTAFF = WEB_BASE_URL + "api/entry/entrydelete.html";//删除员工接口
    public static String GETSTAFF = WEB_BASE_URL + "api/entry/getentry.html";//获取员工信息接口
    public static String SIGNLOG = WEB_BASE_URL + "api/sign/signlog.html";//创建签到信息接口
    public static String BULUSIGN = WEB_BASE_URL + "api/entry/entryecord.html";//补录接口
    public static String SIGNARRAY = WEB_BASE_URL + "api/sign/signlogByarray.html";//定时发送签到列表
    public static String ADDDEPART = WEB_BASE_URL + "api/department/departmentadd.html";//创建部门
    public static String DELETEDEPART = WEB_BASE_URL + "api/department/departmentdelete.html";//删除部门
    public static String GETAD = WEB_BASE_URL + "api/company/getadvert.html";//获取广告
    public static String getWeatherInfo = "http://www.yunbiaowulian.com/weather/city.html";
    /**
     * 资源获取
     **/
    private static String RESOURCE_URL = WEB_BASE_URL + "device/service/getresource.html";

    /**
     * 判断服务器和本地布局是否匹配
     **/
    public static String LAYOUT_CHANGE_STATUS = WEB_BASE_URL + "device/service/layoutchangestatus.html";

    /**
     * 判断服务器中的设备是否在线
     **/
    public static String DEVICE_ONLINE_STATUS = WEB_BASE_URL + "device/status/getrunstatus.html";


    /**
     * 版本检测
     **/
    public static String VERSION_URL = WEB_BASE_URL + "device/service/getversion.html";

    /**
     * 开关机时间获取
     **/
    public static String POWER_OFF_URL = WEB_BASE_URL + "device/service/poweroff.html";

    /**
     * 截图上传
     **/
    public static String SCREEN_UPLOAD_URL = WEB_BASE_URL + "device/service/uploadScreenImg.html";

    /**
     * 上传进度
     **/
    public static String RES_UPLOAD_URL = WEB_BASE_URL + "device/service/rsupdate.html";

    /**
     * SER_NUMBER
     **/
    public static String SER_NUMBER = WEB_BASE_URL + "device/status/getHasNumber.html";

    public static String SCAN_TO_CALL = WEB_BASE_URL + "/mobilebusself/mobilebusselfpost/selectbyordersernum.html";

    public static String SETTIME = WEB_BASE_URL + "common/service/getSystemTime.html";

    /**
     * 绑定设备
     */
    public static String DEC_NUM = WEB_BASE_URL + "device/status/binduser.html";

    /**
     * 获取续费二维码
     */
    public static String QRCODE = WEB_BASE_URL + "device/renewal/getopenrenewalqrcode.html";

    /**
     * 上传人脸识别
     */
    public static String UPLOADFACE = WEB_BASE_URL + "visitors/saveVisitors.html";

    /**
     * 上传下载进度
     */
    public static String NETSPEEFURL = WEB_BASE_URL + "device/status/uploadnetspeed.html";

    /**
     * 音量调节值获取
     * http://tyiyun.com/device/service/getVolume.html?deviceId=ffffffff-
     * be09-eca9-756a-0d8000000000
     */
    private static String VOLUME_URL = WEB_BASE_URL + "device/service/getVolume.html";
    private static String UPLOAD_APP_VERSION_URL = WEB_BASE_URL + "device/service/uploadAppVersionNew.html";
    private static String UPLOAD_DISK_URL = WEB_BASE_URL + "device/service/uploadDisk.html";


    private static String CACHE_BASE_PATH = Constants.LOCAL_ROOT_PATH;
    public static String PROPERTY_CACHE_PATH = CACHE_BASE_PATH + "property/";// 参数缓存存储目录
    public static String SCREEN_CACHE_PATH = CACHE_BASE_PATH + "screen/";//参数缓存存储目录

    public static void initWebConnect() {
        WEB_BASE_URL = Constants.RESOURCE_URL;
        RESOURCE_URL = WEB_BASE_URL + "device/service/getresource.html";
        VERSION_URL = WEB_BASE_URL + "device/service/getversion.html";
        POWER_OFF_URL = WEB_BASE_URL + "device/service/poweroff.html";
        SCREEN_UPLOAD_URL = WEB_BASE_URL + "device/service/uploadScreenImg.html";
        RES_UPLOAD_URL = WEB_BASE_URL + "device/service/rsupdate.html";
        SER_NUMBER = WEB_BASE_URL + "device/status/getHasNumber.html";
        VOLUME_URL = WEB_BASE_URL + "device/service/getVolume.html";
        UPLOAD_APP_VERSION_URL = WEB_BASE_URL + "device/service/uploadAppVersionNew.html";
        UPLOAD_DISK_URL = WEB_BASE_URL + "device/service/uploadDisk.html";
        SETTIME = WEB_BASE_URL + "common/service/getSystemTime.html";
        DEC_NUM = WEB_BASE_URL + "device/status/binduser.html";
        QRCODE = WEB_BASE_URL + "device/renewal/getopenrenewalqrcode.html";
    }

    /**
     * 下载进度完成
     */
    static void finishUpLoad() {
        Map<String, String> netSpeed = new HashMap<String, String>();
        netSpeed.put("deviceNo", HeartBeatClient.getDeviceNo());
        netSpeed.put("speed", "-1");
        MyXutils.getInstance().post(NETSPEEFURL, netSpeed, new MyXutils.XCallBack() {
            @Override
            public void onSuccess(String result) {

            }

            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onFinish() {

            }
        });
    }

    /**
     * 实时下载进度
     * 上传到服务器
     */

    static void upToServer(String speedStr) {
        Map<String, String> netSpeed = new HashMap<String, String>();
        netSpeed.put("deviceNo", HeartBeatClient.getDeviceNo());
        netSpeed.put("speed", speedStr);
        MyXutils.getInstance().post(NETSPEEFURL, netSpeed, new MyXutils.XCallBack() {
            @Override
            public void onSuccess(String result) {

            }

            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onFinish() {

            }
        });
    }

    //    /**
//     * 音量调节
//     * <p/>
//     * param uid
//     */
//    public static void setVolume(String uid) {
//        HashMap<String, String> paramMap = new HashMap<String, String>();
//        paramMap.put("deviceId", uid);
//        String volume = NetTool.sendPost(VOLUME_URL, paramMap);
//        Double volumeD = Double.parseDouble(volume);
//        SoundControl.setMusicSound(volumeD);
//    }
    public static void uploadAppVersion() {
        UIUtils.showTitleTip("版本:" + getVersionName());
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("deviceNo", HeartBeatClient.getDeviceNo());
        paramMap.put("version", getVersionName());
        paramMap.put("type", 1 + "");
        MyXutils.getInstance().post(UPLOAD_APP_VERSION_URL, paramMap, new MyXutils.XCallBack() {
            @Override
            public void onSuccess(String result) {

            }

            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onFinish() {

            }
        });
    }

    /**
     * 获取当前版本号
     *
     * @return
     */
    public static String getVersionName() {
        String version = "";
        try {
            // 获取packagemanager的实例
            PackageManager packageManager = APP.getContext().getApplicationContext().getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(APP.getContext().getApplicationContext().getPackageName(), 0);
            version = packInfo.versionName;
        } catch (Exception e) {
        }
        return version;
    }

    /**
     * 上传磁盘数据
     */
    public static void uploadDiskInfo() {
        String diskInfo = SdCardUtils.getSDDiskCon();
        String ss = "磁盘:" + diskInfo;
        UIUtils.showTitleTip(ss);
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("deviceNo", HeartBeatClient.getDeviceNo());
        paramMap.put("diskInfo", diskInfo);
        MyXutils.getInstance().post(UPLOAD_DISK_URL, paramMap, new MyXutils.XCallBack() {
            @Override
            public void onSuccess(String result) {

            }

            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onFinish() {

            }
        });
    }


}
