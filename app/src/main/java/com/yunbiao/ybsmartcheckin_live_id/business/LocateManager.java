package com.yunbiao.ybsmartcheckin_live_id.business;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.yunbiao.ybsmartcheckin_live_id.utils.MyLocationListenner;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

/**
 * Created by Administrator on 2019/3/16.
 */

public class LocateManager {

    private static LocateManager instance;
    private LocationClient locationClient;

    public static LocateManager instance(){
        if(instance == null){
            synchronized(LocateManager.class){
                if(instance == null){
                    instance = new LocateManager();
                }
            }
        }
        return instance;
    }

    private LocateManager(){}

    public void init(final Context context){
        locationClient = new LocationClient(context);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
//              option.setCoorType("gcj02");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(false);//可选，默认false,设置是否使用gps
        option.setLocationNotify(false);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        locationClient.setLocOption(option);

        locationClient.registerLocationListener(new MyLocationListenner() {
            public void onReceiveLocation(BDLocation location) {
                Log.e("Location", "onReceiveLocation: ");
                if (location == null) {
                    return;
                }
                Log.e("Location", "City---->" + location.getCity());
                if(!TextUtils.isEmpty(location.getCity())){
                    SpUtils.saveStr(SpUtils.CITYNAME, location.getCity());
                }
            }
        });

        if (!locationClient.isStarted()) {
            locationClient.start();
        }
    }

    public void destory(){
        if (locationClient != null && locationClient.isStarted()) {
            locationClient.stop();
            locationClient = null;
        }
    }


}
