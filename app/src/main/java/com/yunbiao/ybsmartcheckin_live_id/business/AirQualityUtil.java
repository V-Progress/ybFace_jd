package com.yunbiao.ybsmartcheckin_live_id.business;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.Call;

public class AirQualityUtil {
    private static final String TAG = "AirQualityUtil";
    private static String url = "http://web.juhe.cn:8080/environment/air/cityair";
    private static String KEY = "airQuality";
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public interface AirQualityCallback{
        void onCallback(AirBean.Citynow citynow);
    }

    public static void get(final AirQualityCallback airQualityCallback){
        String str = SpUtils.getStr(KEY);
        if(!TextUtils.isEmpty(str)){
            AirBean.Citynow citynow = new Gson().fromJson(str, AirBean.Citynow.class);
            Log.e(TAG, "SpUtils:  ----- " + citynow.toString());
            String[] s = citynow.date.split(" ");
            String date = s[0];
            String today = dateFormat.format(new Date());
            if(TextUtils.equals(today,date)){
                if(airQualityCallback != null){
                    airQualityCallback.onCallback(citynow);
                }
                return;
            }
        }

        OkHttpUtils.get().url(url)
                .addParams("city","天津")
                .addParams("key","79c010831f45a1402c53f6676e4c512f")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "onError: -------- " + (e == null ? "NULL" : e.getMessage()));
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.e(TAG, "onResponse: -------------- " + response);
                        AirBean airBean = new Gson().fromJson(response, AirBean.class);
                        if (airBean.error_code == 0) {
                            List<AirBean.Result> result = airBean.result;
                            if(result != null){
                                AirBean.Result result1 = result.get(0);
                                AirBean.Citynow citynow = result1.citynow;

                                if(citynow != null){
                                    String jsonStr = new Gson().toJson(citynow);
                                    SpUtils.saveStr(KEY,jsonStr);
                                }
                            }
                        }
                    }

                    @Override
                    public void onAfter(int id) {
                        String str = SpUtils.getStr(KEY);
                        AirBean.Citynow citynow = new Gson().fromJson(str, AirBean.Citynow.class);
                        Log.e(TAG, "onAfter:  ----- " + citynow.toString());
                        if(airQualityCallback != null){
                            airQualityCallback.onCallback(citynow);
                        }
                    }
                });
    }

    public class AirBean{
        int error_code;
        String reason;
        String resultcode;
        List<Result> result;

        class Result{
            Citynow citynow;
        }

        public class Citynow{
            String AQI;
            String city;
            String date;
            String quality;

            public String getAQI() {
                return AQI;
            }

            public String getCity() {
                return city;
            }

            public String getDate() {
                return date;
            }

            public String getQuality() {
                return quality;
            }

            @Override
            public String toString() {
                return "Citynow{" +
                        "AQI='" + AQI + '\'' +
                        ", city='" + city + '\'' +
                        ", date='" + date + '\'' +
                        ", quality='" + quality + '\'' +
                        '}';
            }
        }
    }

}
