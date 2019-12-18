package com.yunbiao.yb_smart_attendance;

import android.text.TextUtils;

import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import org.greenrobot.eventbus.EventBus;

public class CommonData {
    private static Company company;
    private static String deviceNo;
    private static String bindCode;
    private static String expData;
    private static String deviceUniqueId;
    private static String menuPassword;

    static {
        deviceNo = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
        bindCode = SpUtils.getStr(SpUtils.BIND_CODE);
        expData = SpUtils.getStr(SpUtils.EXP_DATE);
        menuPassword = SpUtils.getStr(SpUtils.MENU_PWD);
    }

    public static void updateCompany(Company comp){
        company = comp;
        if(company != null){
            updatePassword(company.getDevicePwd());
            EventBus.getDefault().post(new CommonDataUpdateEvent(CommonDataUpdateEvent.UPDATE_COMPANY));
        }
    }

    public static void updateDeviceInfo(String no,String code,String date){
        deviceNo = no;
        bindCode = code;
        expData = date;
        if(!TextUtils.isEmpty(deviceNo) || !TextUtils.isEmpty(bindCode)){
            SpUtils.saveStr(SpUtils.DEVICE_NUMBER,no);
            SpUtils.saveStr(SpUtils.BIND_CODE,code);
            SpUtils.saveStr(SpUtils.EXP_DATE,date);
            EventBus.getDefault().post(new CommonDataUpdateEvent(CommonDataUpdateEvent.UPDATE_DEVICE));
        }
    }

    private static void updatePassword(String pwd){
        menuPassword = pwd;
        SpUtils.saveStr(SpUtils.MENU_PWD,pwd);
    }

    /***
     * 获取设备号
     * @return
     */
    public static String getDeviceNo(){
        return deviceNo;
    }

    /***
     * 获取绑定码
     * @return
     */
    public static String getBindCode(){
        return bindCode;
    }

    /***
     * 获取过期日期
     * @return
     */
    public static String getExpData() {
        return expData;
    }

    /***
     * 获取设备管理密码
     * @return
     */
    public static String getPassword(){
        return menuPassword;
    }

    /***
     * 获取公司数据
     * @return
     */
    public synchronized static Company getCompany(){
        return company;
    }

    /***
     * 获取设备号
     * @return
     */
    public synchronized static String getDeviceUniqueId(){
        if(TextUtils.isEmpty(deviceUniqueId)){//如果为空则取缓存
            deviceUniqueId = SpUtils.getStr(SpUtils.DEVICE_UNIQUE_NO);
            if(TextUtils.isEmpty(deviceUniqueId)){//如果缓存为空则读取
                deviceUniqueId = CommonUtils.getMacAddress();
                SpUtils.saveStr(SpUtils.DEVICE_UNIQUE_NO,deviceUniqueId);
            }
        }
        return deviceUniqueId;
    }

    static class CommonDataUpdateEvent {
        public static final int UPDATE_ALL = 0;
        public static final int UPDATE_COMPANY = 1;
        public static final int UPDATE_DEVICE = 1;
        private static int mUpdateTag = UPDATE_ALL;
        public CommonDataUpdateEvent(int tag) {
            mUpdateTag = tag;
        }

        public static int getTag() {
            return mUpdateTag;
        }
    }

}
