package com.yunbiao.ybsmartcheckin_live_id.utils;


import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;

/**
 * 定位获得的数据
 */
public class MyLocationListenner implements BDLocationListener {
    private static final String TAG = "MyLocationListenner";
    @Override
    public void onReceiveLocation(BDLocation location) {
//        if (null != location && location.getLocType() != BDLocation.TypeServerError) {
//            LocationBean locationBean = new LocationBean();
//            locationBean.setCity(location.getCity() + "");
//            Log.e(TAG, "City---->"+ location.getCity());
//            locationBean.setAltitude(String.valueOf(location.getLatitude()));
//            locationBean.setLongitude(String.valueOf(location.getLongitude()));
//            locationBean.setAdressHeight(location.getAltitude() + "");
//            locationBean.setAdress(location.getAddrStr());
//            MachineDetial.getInstance().setLocation(locationBean);
//            MachineDetial.getInstance().getLocation();
//        }
    }
}
