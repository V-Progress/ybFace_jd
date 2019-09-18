package com.yunbiao.ybsmartcheckin_live_id.business;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.ThreadUitls;
import com.yunbiao.ybsmartcheckin_live_id.utils.xutil.MyXutils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2019/3/16.
 */

public class WeatherManager {
    private String TAG = getClass().getSimpleName();
    private static WeatherManager instance;
    private ResultListener mListener;

    private final int GET_WEATHER = 1;
    private final int UPDATE_WEAHTHER_INTERVAL = 4 * 60 * 60 * 1000;

    public static WeatherManager instance(){
        if(instance == null){
            synchronized(WeatherManager.class){
                if(instance == null){
                    instance = new WeatherManager();
                }
            }
        }
        return instance;
    }

    private WeatherManager(){}

    public void start(final ResultListener resultListener){
        mListener = resultListener;

        weatherHandler.sendEmptyMessageDelayed(GET_WEATHER,1000);
    }

    private Handler weatherHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            initWeather();
            sendEmptyMessageDelayed(GET_WEATHER,UPDATE_WEAHTHER_INTERVAL);
        }
    };

    private void initWeather() {//获取天气
        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
                final Map<String, String> map = new HashMap<>();
                String city = SpUtils.getStr(SpUtils.CITYNAME);
                map.put("city", city);
                map.put("stores", HeartBeatClient.getDeviceNo());
                MyXutils.getInstance().post(ResourceUpdate.getWeatherInfo, map, new MyXutils.XCallBack() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e(TAG, "onSuccess: " + result);
                        try {
                            if(TextUtils.isEmpty(result) || TextUtils.equals("\"\"",result)){
                                return;
                            }

                            int id;
                            if (result.contains("阴")) {
                                id = R.mipmap.icon_yintian;
                            } else if (result.contains("雨")) {
                                id = R.mipmap.icon_rain;
                            } else if (result.contains("雪")) {
                                id = R.mipmap.icon_snow;
                            } else if (result.contains("多云")) {
                                id = R.mipmap.icon_duoyun;
                            } else {
                                id = R.mipmap.icon_sun;
                            }

                            int indStart = result.indexOf('|');
                            int indEnd = result.lastIndexOf('|');
                            String info = result.substring(indStart + 1, indEnd);

                            if(mListener != null){
                                mListener.updateWeather(id,info);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable ex) {
                        if(ex != null){
                            Log.e(TAG, "ex----------->" + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onFinish() {

                    }
                });
            }
        });
    }

    public interface ResultListener{
        void updateWeather(int id,String weatherInfo);
    }

}
