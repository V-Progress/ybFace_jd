package com.yunbiao.ybsmartcheckin_live_id.afinel;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.utils.SdCardUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.xutil.MyXutils;

import java.util.HashMap;


public class ResourceUpdate {

    public static String WEB_BASE_URL = Constants.RESOURCE_URL;

    public static String COMPANYINFO_ID = WEB_BASE_URL + "api/company/getcompanyById.html";//获取公司信息以及下的部门信息接口
    public static String UPDATE_PWD = WEB_BASE_URL + "api/device/updatePassWord.html";
    public static String HOLIREM = WEB_BASE_URL + "api/holidayrem/getHolirem.html";//节日提醒背景
    public static String JOINREM = WEB_BASE_URL + "api/holidayrem/getJoinrem.html";//生日提醒背景
    public static String QRCODE_ADD = WEB_BASE_URL + "nhb/entryCode.html";
    public static String COMPANYINFO = WEB_BASE_URL + "api/company/getcompany.html";//获取公司信息以及下的部门信息接口
    public static String VISITORINFO = WEB_BASE_URL + "api/visitor/getAllByComDep.html";//获取公司信息以及下的部门信息接口
    public static String UPDATSTAFF = WEB_BASE_URL + "api/entry/entryupdate.html";//修改员工信息接口
    public static String ADDSTAFF = WEB_BASE_URL + "api/entry/entryadd.html";//添加员工信息接口
    public static String DELETESTAFF = WEB_BASE_URL + "api/entry/entrydelete.html";//删除员工接口
    public static String GETSTAFF = WEB_BASE_URL + "api/entry/getentry.html";//获取员工信息接口
    public static String SEND_VIS_ENTRY = WEB_BASE_URL + "api/visitor/sendvisEntry.html";//获取员工信息接口
    public static String SIGNLOG = WEB_BASE_URL + "api/sign/signlog.html";//创建签到信息接口
    public static String VISITOLOG = WEB_BASE_URL + "api/visitor/signUpload.html";//访客记录
    public static String BULUSIGN = WEB_BASE_URL + "api/entry/entryecord.html";//补录接口
    public static String SIGNARRAY = WEB_BASE_URL + "api/sign/signlogByarray.html";//定时发送签到列表
    public static String VISITARRAY = WEB_BASE_URL + "api/visitor/signUploadByarray.html";//定时发送访客列表
    public static String ADDDEPART = WEB_BASE_URL + "api/department/departmentadd.html";//创建部门
    public static String DELETEDEPART = WEB_BASE_URL + "api/department/departmentdelete.html";//删除部门
    public static String GETAD = WEB_BASE_URL + "api/company/getadvert.html";//获取广告
    public static String getCompInfo = WEB_BASE_URL + "api/propa/getAll.html";//公司介绍信息
    public static String getWeatherInfo = "http://www.yunbiao.tv/weather/city.html";
    public static String UPDATE_DEVICE_TYPE = WEB_BASE_URL + "api/device/updateDeviceType.html";//更新设备类型

    public static String GETUSERINFO_BY_CODE = WEB_BASE_URL + "/api/witness/checkcode.html";//获取扫码人信息

    public static String UPLOAD_CODE_VERIFY_RESULT = WEB_BASE_URL + "/api/witness/checkCodeUpload.html";//扫码上传

    public static String UPLOAD_TEMPERETURE_EXCEPTION = WEB_BASE_URL + "api/facewitness/checkTemper.html";//单条测温记录
    public static String UPLOAD_TEMPERETURE_EXCEPTION_ARRAY = WEB_BASE_URL + "api/facewitness/checkByarrayTemper.html";//多条测温记录

    public static String UPLOAD_IDCARD = WEB_BASE_URL + "/api/witness/checktemper.html";//人证测温记录
    public static String UPLOAD_NO_IDCARD = WEB_BASE_URL + "/api/witness/nocardUpload.html";//无证测温

    /**
     * 判断服务器中的设备是否在线
     **/
    public static String DEVICE_ONLINE_STATUS = WEB_BASE_URL + "device/status/getrunstatus.html";

    /**
     * 版本检测
     **/
    public static String VERSION_URL = WEB_BASE_URL + "device/service/getversion.html";

    /***
     * 设备异常上传
     */
    public static String DEVICE_EXCEPTION_UPLOAD = WEB_BASE_URL + "api/device/exceptionupload.html";

    /**
     * 开关机时间获取
     **/
    public static String POWER_OFF_URL = WEB_BASE_URL + "device/service/poweroff.html";

    /**
     * 截图上传
     **/
    public static String SCREEN_UPLOAD_URL = WEB_BASE_URL + "device/service/uploadScreenImg.html";

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

    public static void uploadAppVersion() {
        UIUtils.showTitleTip(APP.getContext(),"版本:" + getVersionName());
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
        UIUtils.showTitleTip(APP.getContext(),ss);
        HashMap<String, String> paramMap = new HashMap<>();
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
