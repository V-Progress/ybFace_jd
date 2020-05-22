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

    public static String COMPANYINFO_ID = Constants.RESOURCE_URL + "api/company/getcompanyById.html";//获取公司信息以及下的部门信息接口
    public static String UPDATE_PWD = Constants.RESOURCE_URL + "api/device/updatePassWord.html";
    public static String HOLIREM = Constants.RESOURCE_URL + "api/holidayrem/getHolirem.html";//节日提醒背景
    public static String JOINREM = Constants.RESOURCE_URL + "api/holidayrem/getJoinrem.html";//生日提醒背景
    public static String QRCODE_ADD = Constants.RESOURCE_URL + "nhb/entryCode.html";
    public static String COMPANYINFO = Constants.RESOURCE_URL + "api/company/getcompany.html";//获取公司信息以及下的部门信息接口
    public static String VISITORINFO = Constants.RESOURCE_URL + "api/visitor/getAllByComDep.html";//获取公司信息以及下的部门信息接口
    public static String UPDATSTAFF = Constants.RESOURCE_URL + "api/entry/entryupdate.html";//修改员工信息接口
    public static String ADDSTAFF = Constants.RESOURCE_URL + "api/entry/entryadd.html";//添加员工信息接口
    public static String DELETESTAFF = Constants.RESOURCE_URL + "api/entry/entrydelete.html";//删除员工接口
    public static String GETSTAFF = Constants.RESOURCE_URL + "api/entry/getentry.html";//获取员工信息接口
    public static String SEND_VIS_ENTRY = Constants.RESOURCE_URL + "api/visitor/sendvisEntry.html";//获取员工信息接口
    public static String SIGNLOG = Constants.RESOURCE_URL + "api/sign/signlog.html";//创建签到信息接口
    public static String VISITOLOG = Constants.RESOURCE_URL + "api/visitor/signUpload.html";//访客记录
    public static String BULUSIGN = Constants.RESOURCE_URL + "api/entry/entryecord.html";//补录接口
    public static String SIGNARRAY = Constants.RESOURCE_URL + "api/sign/signlogByarray.html";//定时发送签到列表
    public static String VISITARRAY = Constants.RESOURCE_URL + "api/visitor/signUploadByarray.html";//定时发送访客列表
    public static String ADDDEPART = Constants.RESOURCE_URL + "api/department/departmentadd.html";//创建部门
    public static String DELETEDEPART = Constants.RESOURCE_URL + "api/department/departmentdelete.html";//删除部门
    public static String GETAD = Constants.RESOURCE_URL + "api/company/getadvert.html";//获取广告
    public static String getCompInfo = Constants.RESOURCE_URL + "api/propa/getAll.html";//公司介绍信息
    public static String getWeatherInfo = "http://www.yunbiao.tv/weather/city.html";
    public static String UPDATE_DEVICE_TYPE = Constants.RESOURCE_URL + "api/device/updateDeviceType.html";//更新设备类型
    public static String GETUSERINFO_BY_CODE = Constants.RESOURCE_URL + "/api/witness/checkcode.html";//获取扫码人信息
    public static String UPLOAD_CODE_VERIFY_RESULT = Constants.RESOURCE_URL + "/api/witness/checkCodeUpload.html";//扫码上传
    public static String UPLOAD_TEMPERETURE_EXCEPTION = Constants.RESOURCE_URL + "api/facewitness/checkTemper.html";//单条测温记录
    public static String UPLOAD_TEMPERETURE_EXCEPTION_ARRAY = Constants.RESOURCE_URL + "api/facewitness/checkByarrayTemper.html";//多条测温记录
    public static String UPLOAD_IDCARD = Constants.RESOURCE_URL + "/api/witness/checktemper.html";//人证测温记录
    public static String UPLOAD_NO_IDCARD = Constants.RESOURCE_URL + "/api/witness/nocardUpload.html";//无证测温
    public static String SAFETY_SEND_WARNING_MESSAGE = Constants.RESOURCE_URL + "/api/device/temperexp.html";//安检门发信息
    public static String CERTIFICATES_WHITE_LIST = Constants.RESOURCE_URL + "/api/witness/getWhitelist.html";//人证白名单
    public static String GET_VERSION_INFO = Constants.RESOURCE_URL + "/device/service/getnewversion.html";//获取版本信息
    public static String DEVICE_ONLINE_STATUS = Constants.RESOURCE_URL + "device/status/getrunstatus.html";//判断服务器中的设备是否在线
    public static String VERSION_URL = Constants.RESOURCE_URL + "device/service/getversion.html";//版本检测
    public static String DEVICE_EXCEPTION_UPLOAD = Constants.RESOURCE_URL + "api/device/exceptionupload.html";//设备异常上传
    public static String POWER_OFF_URL = Constants.RESOURCE_URL + "device/service/poweroff.html";//开关机时间获取
    public static String SCREEN_UPLOAD_URL = Constants.RESOURCE_URL + "device/service/uploadScreenImg.html";//截图上传
    public static String UPLOAD_MACHINE_INFO = Constants.RESOURCE_URL + "device/service/updateDeviceHardwareInfo.html";//上传设备信息
    public static String VOLUME_URL = Constants.RESOURCE_URL + "device/service/getVolume.html";//音量调节值获取
    public static String UPLOAD_APP_VERSION_URL = Constants.RESOURCE_URL + "device/service/uploadAppVersionNew.html";
    public static String UPLOAD_DISK_URL = Constants.RESOURCE_URL + "device/service/uploadDisk.html";

    public static String GET_ACTIVE_CODE = Constants.RESOURCE_URL + "api/activate/getCode.html";//获取激活码
    public static String RETURN_ACTIVE_STATUS = Constants.RESOURCE_URL + "api/activate/getCodeReturn.html";//返回激活码

    public static void refreshAddress(){
        COMPANYINFO_ID = Constants.RESOURCE_URL + "api/company/getcompanyById.html";//获取公司信息以及下的部门信息接口
        UPDATE_PWD = Constants.RESOURCE_URL + "api/device/updatePassWord.html";
        HOLIREM = Constants.RESOURCE_URL + "api/holidayrem/getHolirem.html";//节日提醒背景
        JOINREM = Constants.RESOURCE_URL + "api/holidayrem/getJoinrem.html";//生日提醒背景
        QRCODE_ADD = Constants.RESOURCE_URL + "nhb/entryCode.html";
        COMPANYINFO = Constants.RESOURCE_URL + "api/company/getcompany.html";//获取公司信息以及下的部门信息接口
        VISITORINFO = Constants.RESOURCE_URL + "api/visitor/getAllByComDep.html";//获取公司信息以及下的部门信息接口
        UPDATSTAFF = Constants.RESOURCE_URL + "api/entry/entryupdate.html";//修改员工信息接口
        ADDSTAFF = Constants.RESOURCE_URL + "api/entry/entryadd.html";//添加员工信息接口
        DELETESTAFF = Constants.RESOURCE_URL + "api/entry/entrydelete.html";//删除员工接口
        GETSTAFF = Constants.RESOURCE_URL + "api/entry/getentry.html";//获取员工信息接口
        SEND_VIS_ENTRY = Constants.RESOURCE_URL + "api/visitor/sendvisEntry.html";//获取员工信息接口
        SIGNLOG = Constants.RESOURCE_URL + "api/sign/signlog.html";//创建签到信息接口
        VISITOLOG = Constants.RESOURCE_URL + "api/visitor/signUpload.html";//访客记录
        BULUSIGN = Constants.RESOURCE_URL + "api/entry/entryecord.html";//补录接口
        SIGNARRAY = Constants.RESOURCE_URL + "api/sign/signlogByarray.html";//定时发送签到列表
        VISITARRAY = Constants.RESOURCE_URL + "api/visitor/signUploadByarray.html";//定时发送访客列表
        ADDDEPART = Constants.RESOURCE_URL + "api/department/departmentadd.html";//创建部门
        DELETEDEPART = Constants.RESOURCE_URL + "api/department/departmentdelete.html";//删除部门
        GETAD = Constants.RESOURCE_URL + "api/company/getadvert.html";//获取广告
        getCompInfo = Constants.RESOURCE_URL + "api/propa/getAll.html";//公司介绍信息
        getWeatherInfo = "http://www.yunbiao.tv/weather/city.html";
        UPDATE_DEVICE_TYPE = Constants.RESOURCE_URL + "api/device/updateDeviceType.html";//更新设备类型
        GETUSERINFO_BY_CODE = Constants.RESOURCE_URL + "/api/witness/checkcode.html";//获取扫码人信息
        UPLOAD_CODE_VERIFY_RESULT = Constants.RESOURCE_URL + "/api/witness/checkCodeUpload.html";//扫码上传
        UPLOAD_TEMPERETURE_EXCEPTION = Constants.RESOURCE_URL + "api/facewitness/checkTemper.html";//单条测温记录
        UPLOAD_TEMPERETURE_EXCEPTION_ARRAY = Constants.RESOURCE_URL + "api/facewitness/checkByarrayTemper.html";//多条测温记录
        UPLOAD_IDCARD = Constants.RESOURCE_URL + "/api/witness/checktemper.html";//人证测温记录
        UPLOAD_NO_IDCARD = Constants.RESOURCE_URL + "/api/witness/nocardUpload.html";//无证测温
        SAFETY_SEND_WARNING_MESSAGE = Constants.RESOURCE_URL + "/api/device/temperexp.html";//安检门发信息
        CERTIFICATES_WHITE_LIST = Constants.RESOURCE_URL + "/api/witness/getWhitelist.html";//人证白名单
        GET_VERSION_INFO = Constants.RESOURCE_URL + "/device/service/getnewversion.html";//获取版本信息
        DEVICE_ONLINE_STATUS = Constants.RESOURCE_URL + "device/status/getrunstatus.html";//判断服务器中的设备是否在线
        VERSION_URL = Constants.RESOURCE_URL + "device/service/getversion.html";//版本检测
        DEVICE_EXCEPTION_UPLOAD = Constants.RESOURCE_URL + "api/device/exceptionupload.html";//设备异常上传
        POWER_OFF_URL = Constants.RESOURCE_URL + "device/service/poweroff.html";//开关机时间获取
        SCREEN_UPLOAD_URL = Constants.RESOURCE_URL + "device/service/uploadScreenImg.html";//截图上传
        UPLOAD_MACHINE_INFO = Constants.RESOURCE_URL + "device/service/updateDeviceHardwareInfo.html";//上传设备信息
        VOLUME_URL = Constants.RESOURCE_URL + "device/service/getVolume.html";//音量调节值获取
        UPLOAD_APP_VERSION_URL = Constants.RESOURCE_URL + "device/service/uploadAppVersionNew.html";
        UPLOAD_DISK_URL = Constants.RESOURCE_URL + "device/service/uploadDisk.html";
        GET_ACTIVE_CODE = Constants.RESOURCE_URL + "api/activate/getCode.html";//获取激活码
        RETURN_ACTIVE_STATUS = Constants.RESOURCE_URL + "api/activate/getCodeReturn.html";//返回激活码
    }

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
