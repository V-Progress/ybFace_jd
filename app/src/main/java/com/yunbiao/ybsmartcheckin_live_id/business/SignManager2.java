package com.yunbiao.ybsmartcheckin_live_id.business;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.db.SignBean;
import com.yunbiao.ybsmartcheckin_live_id.db.SignDao;
import com.yunbiao.ybsmartcheckin_live_id.db.VIPDetail;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.ThreadUitls;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by Administrator on 2019/3/18.
 */

public class SignManager2 {
    private final String TAG = getClass().getSimpleName();
    private static SignManager2 instance;
    private SignDao signDao;
    private String signThreadName = "sign";
    private final int UPDATE_TIME = 10 * 60 * 1000;
    private Handler signHandler;
    private final HandlerThread signThread;
    private List<SignBean> mList;//签到人员list
    private SignEventListener listener;
    private String today;
    private Activity mAct;
    private boolean isInited = false;
    private long SIGN_OFF_TIME = 10 * 1000;
    private DateFormat signTimeFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static SignManager2 instance() {
        if (instance == null) {
            synchronized (SignManager2.class) {
                if (instance == null) {
                    instance = new SignManager2();
                }
            }
        }
        return instance;
    }

    private SignManager2() {
        //初始化当前时间
        initToday();
        signThread = new HandlerThread(signThreadName);
        signThread.start();
        signHandler = new Handler(signThread.getLooper());
    }


    public interface SignEventListener {
        void onPrepared(List<SignBean> mList);
        void onSigned(List<SignBean> mList, SignBean signBean, int currentTime);
    }

    public SignManager2 init(@NonNull Activity mAct, @NonNull SignEventListener signEventListener) {
        this.mAct = mAct;
        signDao = new SignDao(mAct);
        listener = signEventListener;

        //读取列表
        ThreadUitls.runInThread(initRunnable);

        //开始自动更新签到列表的操作
        startAutoUpdate(signDao);

        return instance();
    }

    //初始化线程
    private Runnable initRunnable = new Runnable() {
        @Override
        public void run() {
            mList = new ArrayList<>();
            List<SignBean> mSignList = signDao.queryByDate(today);
            if (mSignList != null) {
                d("今日签到数据..." + mSignList.size()+"条");
            }
            mList.addAll(mSignList);

            d("签到列表: " + mList.toString());

            if (listener != null) {
                mAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onPrepared(mList);
                    }
                });
            }
            isInited = true;
        }
    };

    //判断是否可签到
    private boolean canSign(int faceId,long signTime){
        for (SignBean signBean : mList) {
            int faceId1 = signBean.getFaceId();
            long lastSignTime = signBean.getTime();

            if (faceId1 == faceId) {
                boolean b = signTime - lastSignTime >= SIGN_OFF_TIME;
                d("打卡时间： " + lastSignTime  +" --- "+ signTime + "---" + b);
                return b;
            }
        }
        return true;
    }

    //签到
    public void sign(int faceId,byte[] imgByteArray,long signTime){
        if (!isInited) {
            return;
        }
        if (canSign(faceId,signTime)) {
            List<VIPDetail> vipDetails = APP.getUserDao().queryByFaceId(faceId);
            if(vipDetails == null){
                return;
            }

            VIPDetail vipDetail = vipDetails.get(0);
            final SignBean signBean = new SignBean(vipDetail.getFaceId()
                    ,vipDetail.getEmpId()
                    ,vipDetail.getName()
                    ,vipDetail.getJob()
                    ,vipDetail.getImgUrl()
                    ,signTime
                    ,false
                    ,vipDetail.getSignature());
//            signBean.setCurrentImg(getBitmap(imgByteArray,vipDetail.getImgUrl()));
            mList.add(0,signBean);

            d("打卡成功-------- " + signBean.toString());
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //回调
                    if (listener != null) {
                        listener.onSigned(mList,signBean, getSignType());
                    }
                }
            });

            sendSign(signBean);
        }
    }

    private void sendSign(final SignBean signBean){
        final Map<String, String> map = new HashMap<>();
        map.put("entryid", signBean.getEmpId() + "");
        map.put("signTime", signBean.getTime() + "");
        d("签到： "+ResourceUpdate.SIGNLOG + "-----" + map.toString());
        OkHttpUtils.post().url(ResourceUpdate.SIGNLOG).params(map).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                d("发送失败--------> " + e != null ? e.getMessage() : "NULL");
                signBean.setUpload(false);
            }

            @Override
            public void onResponse(String response, int id) {
                d("发送成功--------> " + response);
                try {
                    JSONObject json = new JSONObject(response);
                    signBean.setUpload(json.getInt("status") == 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAfter(int id) {
                signDao.insert(signBean);
            }
        });
    }

    private Bitmap getBitmap(byte[] imgByteArray,String url){
        if(imgByteArray != null && imgByteArray.length > 0){
            final BitmapFactory.Options options = new BitmapFactory.Options();
            return BitmapFactory.decodeByteArray(imgByteArray, 0, imgByteArray.length, options);
        } else {
            return BitmapFactory.decodeFile(url);
        }
    }

    /***
     * 开始发送签到数据
     * @param signDao
     */
    private void startAutoUpdate(SignDao signDao) {
        this.signDao = signDao;
        if (signHandler != null) {
            signHandler.removeCallbacks(sendSignDataRunnable);
        }
        signHandler.postDelayed(sendSignDataRunnable, 10 * 1000);
    }

    //定时发送签到数据
    private Runnable sendSignDataRunnable = new Runnable() {
        @Override
        public void run() {
            initToday();

            d("定时上送签到数据...");
            if (signDao == null) {
                signHandler.postDelayed(sendSignDataRunnable, UPDATE_TIME);
                return;
            }
            final List<SignBean> signList = signDao.queryByDate(today);
            if (signList == null) {
                signHandler.postDelayed(sendSignDataRunnable, UPDATE_TIME);
                return;
            }

            JSONArray jsonArray = new JSONArray();
            int size = 0;
            for (int i = 0; i < signList.size(); i++) {
                final SignBean bean = signList.get(i);
                if (!bean.isUpload()) {
                    try {
                        size++;
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("entryid", bean.getEmpId() + "");
                        jsonObject.put("signTime", bean.getTime() + "");
                        jsonArray.put(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (size <= 0) {
                signHandler.postDelayed(sendSignDataRunnable, UPDATE_TIME);
                return;
            }

            final Map<String, String> map = new HashMap<String, String>();
            map.put("signstr", jsonArray.toString() + "");
            if (jsonArray.length() > 0) {
                d("准备上送..." + map.toString());
                OkHttpUtils.post().url(ResourceUpdate.SIGNARRAY).params(map).build().execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        d("上送失败--->" + e != null ?e.getMessage():"NULL");
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        d("上送成功---> " + response);
//                        try {
//                            JSONObject json = new JSONObject(response);
//                            if (json.getInt("status") == 1) {
//                                for (int i = 0; i < signList.size(); i++) {
//                                    final SignBean bean = signList.get(i);
//                                    if (bean.isUpload() == false) {
//                                        List<SignBean> signList = signDao.queryByTime(bean.getFaceId());
//                                        if (signList != null && signList.size() > 0) {
//                                            signList.get(0).setUpload(true);
//                                            signDao.update(signList.get(0));
//                                        }
//                                    }
//                                }
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
                    }
                });
            }
            signHandler.postDelayed(sendSignDataRunnable, UPDATE_TIME);
        }
    };

    private void initToday() {
        Calendar calendar = Calendar.getInstance();
        String yearStr = calendar.get(Calendar.YEAR) + "";//获取年份
        String monthStr = calendar.get(Calendar.MONTH) + 1 + "";//获取月份
        String dayStr = calendar.get(Calendar.DAY_OF_MONTH) + "";//获取天
        today = yearStr + "年" + monthStr + "月" + dayStr + "日";
        d("更新日期..."+today);
    }

    private int getSignType() {//得到现在的时间与获取到的上下班时间对比
        Date dateCurrent = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            Date hourDate = sdf.parse(sdf.format(dateCurrent));//现在的时间

            Date dataMorn = sdf.parse(SpUtils.getStr(SpUtils.GOTIME, "09:00"));//上班时间
            Date dataNoon = sdf.parse(SpUtils.getStr(SpUtils.DOWNTIME, "18:00"));//下班时间

            if (hourDate.getTime() < dataMorn.getTime() + 1000 * 60 * 30) {//现在的时间小于上班时间+半个小时，就返回type1
                return 1;
            } else if (hourDate.getTime() >= dataMorn.getTime() + 1000 * 60 * 30 && hourDate.getTime() < dataNoon.getTime()) {//其他时间，就返回type0
                return 0;
            } else if (hourDate.getTime() > dataNoon.getTime()) {//现在的时间大于下班时间，就返回type2
                return 2;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private boolean isDebug = true;

    private void d(@NonNull String msg) {
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }
}