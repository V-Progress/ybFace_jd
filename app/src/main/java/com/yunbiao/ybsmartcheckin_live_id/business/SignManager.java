package com.yunbiao.ybsmartcheckin_live_id.business;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.activity.WelComeActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.db.SignBean;
import com.yunbiao.ybsmartcheckin_live_id.db.SignDao;
import com.yunbiao.ybsmartcheckin_live_id.db.VIPDetail;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.ThreadUitls;
import com.yunbiao.ybsmartcheckin_live_id.utils.xutil.MyCallBack;
import com.yunbiao.ybsmartcheckin_live_id.utils.xutil.MyXutils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

public class SignManager {
    private final String TAG = getClass().getSimpleName();
    private static SignManager instance;
    private SignDao signDao;
    private String signThreadName = "sign";
    private final int UPDATE_TIME = 10 * 60 * 1000;
//    private final int UPDATE_TIME = 10 * 1000;
//    private final long UPDATE_DATE_TIME = 60 * 60 * 1000;
    private Handler signHandler;
    private final HandlerThread signThread;
    private List<VIPDetail> mList;//签到人员list
    private SignEventListener listener;
    private String today;
    private Activity mAct;
    private boolean isInited = false;
    private DateFormat signTimeFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static SignManager instance() {
        if (instance == null) {
            synchronized (SignManager.class) {
                if (instance == null) {
                    instance = new SignManager();
                }
            }
        }
        return instance;
    }

    private SignManager() {
        //初始化当前时间
        initToday();
        signThread = new HandlerThread(signThreadName);
        signThread.start();
        signHandler = new Handler(signThread.getLooper());
    }


    public interface SignEventListener {
        void onPrepared(List<VIPDetail> mList);

        void onSigned(List<VIPDetail> mList, String vipDetail, int signType);

        void onMakeUped(Bitmap bitmap, boolean makeUpSuccess);
    }

    public SignManager init(@NonNull Activity mAct, @NonNull SignEventListener signEventListener) {
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
            if (mSignList != null && mSignList.size() > 0) {
                d("今日签到数据..." + mSignList.size()+"条");
                for (int i = 0; i < mSignList.size(); i++) {
                    SignBean bean = mSignList.get(i);
                    VIPDetail vip = new VIPDetail(bean.getFaceId(),bean.getName(), bean.getSex(), bean.getAge(), bean.getJob(), bean.getImgUrl(), bean.getTime(), bean.getDepart(), bean.getSignature());
                    mList.add(vip);
                }
            }

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

    private void initToday() {
        Calendar calendar = Calendar.getInstance();
        String yearStr = calendar.get(Calendar.YEAR) + "";//获取年份
        String monthStr = calendar.get(Calendar.MONTH) + 1 + "";//获取月份
        String dayStr = calendar.get(Calendar.DAY_OF_MONTH) + "";//获取天
        String date = yearStr + "年" + monthStr + "月" + dayStr + "日";
        if(!TextUtils.equals(date,today)){
            today = date;
            d("更新日期..."+today);
        }
    }

    /***
     * 签到
     * @param signList
     */
    public void sign(final List<VIPDetail> signList) {
        if (!isInited) {
            return;
        }

        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
                int tempSize = mList.size();
                for (int i = 0; i < signList.size(); i++) {
                    final VIPDetail vipBean = signList.get(i);
                    boolean isADD = isBaohan(mList, vipBean.getName(), vipBean.getTime());//检测主list中是否包含
                    if (!isADD) {//不包含则添加并发送
                        mList.add(0, vipBean);

                        final Map<String, String> map = new HashMap<>();
                        map.put("entryid", vipBean.getEmpId() + "");
                        map.put("signTime", vipBean.getTime() + "");
                        MyXutils.getInstance().post(ResourceUpdate.SIGNLOG, map, new MyXutils.XCallBack() {
                            @Override
                            public void onSuccess(String result) {
                                d("签到成功--------> " + result);
                                try {
                                    JSONObject json = new JSONObject(result);
                                    signDao.insert(new SignBean(vipBean.getEmpId()
                                            , vipBean.getName(), vipBean.getJob()
                                            , vipBean.getImgUrl(), vipBean.getTime()
                                            , vipBean.getDepart(), today
                                            , vipBean.getSex(), json.getInt("status") == 1));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(Throwable ex) {
                                d("签到失败--->" + ex.getMessage());//失败
                                signDao.insert(new SignBean(vipBean.getEmpId(), vipBean.getName(), vipBean.getJob(), vipBean.getImgUrl(), vipBean.getTime(), vipBean.getDepart(), today, vipBean.getSex(), false));
                                ex.printStackTrace();
                            }

                            @Override
                            public void onFinish() {
                            }
                        });
                    }
                }
                //判断是否有变化，有变化再回调
                if (mList.size() > tempSize) {
                    //获取当前签到类型
                    final int signType = getCurrentTime();

                    //签到者的名字
                    String callName = "";
                    long time = new Date().getTime();
                    for (int i = 0; i < signList.size(); i++) {
                        callName = callName + signList.get(i).getName();
                        time = signList.get(i).getTime();
                    }
                    d("签到---> " + signTimeFormate.format(time) + " --- " + callName);

                    //回调
                    if (listener != null) {
                        final String finalCallName = callName;
                        mAct.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onSigned(mList, finalCallName, signType);
                            }
                        });
                    }
                }
            }
        });
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

    class SignDataBean{
        String entryid;
        long signTime;
    }

    class SignDataResponse{
        int status;
    }

    //定时发送签到数据
    private Runnable sendSignDataRunnable = new Runnable() {
        @Override
        public void run() {
            initToday();

            d("定时上送... " + Thread.currentThread().getName());
            if (signDao == null) {
                d("签到库...NULL");
                prepareToSend();
                return;
            }

            List<SignBean> signList = signDao.selectAll();
            if(signList == null){
                d("签到库...NULL");
                prepareToSend();
                return;
            }

            final List<SignDataBean> list = new ArrayList<>();
            for (SignBean signBean : signList) {
                if(signBean.isUpload())
                    continue;

                SignDataBean signDataBean = new SignDataBean();
                signDataBean.entryid = signBean.getEmpId()+"";
                signDataBean.signTime = signBean.getTime();
                list.add(signDataBean);
            }

            d("上送数量... " + list.size());
            if(list.size() <= 0){
                prepareToSend();
                return;
            }

            String jsonStr = new Gson().toJson(list);
            final Map<String, String> map = new HashMap<String, String>();
            map.put("signstr", jsonStr);
            d("参数... "+map.toString());
            OkHttpUtils.post()
                    .params(map)
                    .url(ResourceUpdate.SIGNARRAY)
                    .build().execute(new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    d("上送失败--->" + e != null ? e.getMessage() : "NULL");
                    e.printStackTrace();
                }

                @Override
                public void onResponse(String response, int id) {
                    SignDataResponse signDataResponse = new Gson().fromJson(response, SignDataResponse.class);
                    if (signDataResponse.status == 1) {
                        d("上送成功---> " + response);
                        for (SignDataBean signDataBean : list) {
                            List<SignBean> signBeans = signDao.queryByTime(signDataBean.signTime);
                            if(signBeans != null && signBeans.size()>0){
                                SignBean signBean = signBeans.get(0);
                                signBean.setUpload(true);
                                signDao.update(signBean);
                            }
                        }

                    } else {
                        d("上送失败---> " + response);
                    }
                }
            });
            prepareToSend();
        }
    };
    private void prepareToSend(){
        signHandler.postDelayed(sendSignDataRunnable, UPDATE_TIME);
    }


    private boolean isBaohan(List<VIPDetail> mList, String name, long offtime) {
        for (int i = 0; i < mList.size(); i++) {
            return TextUtils.equals(name, mList.get(i).getName()) && offtime - (mList.get(i).getTime()) < 10000;
        }
        return false;
    }

    private int getCurrentTime() {//得到现在的时间与获取到的上下班时间对比
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

    private void tsetSign() {
        List<SignBean> mSignList = signDao.queryByDate(today);
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

        Log.e(TAG, "SignList.size----------------------> " + mSignList.size());
        for (int i = 0; i < mSignList.size(); i++) {

            if (!mSignList.get(i).isUpload()) {
                Log.e(TAG, "name:----------------->" + mSignList.get(i).getName());
                Log.e(TAG, "isupload:----------------->" + mSignList.get(i).isUpload());
                Log.e(TAG, "time:----------------->" + df.format(mSignList.get(i).getTime()));
            }
        }
    }

    private boolean isDebug = true;

    private void d(@NonNull String msg) {
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }

    private static boolean canMakeUp = true;
    public static boolean canMakeUp(){
        return canMakeUp;
    }
    public void makeUpSign(byte[] faceImage){
        canMakeUp = false;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        final Bitmap image = BitmapFactory.decodeByteArray(faceImage, 0, faceImage.length, options);
        String strFileAdd = saveBitmap(image);

        int companyid = SpUtils.getInt(SpUtils.COMPANYID);
        final Map<String, String> map = new HashMap<>();
        map.put("comId", companyid + "");
        File imgFile = new File(strFileAdd);

        Log.e("HAHA", "makeUpSign: -----" + imgFile.exists() +"-----" +imgFile.length());

        Log.e("HAHA", "makeUpSign: " + ResourceUpdate.BULUSIGN + "-----" + map.toString());
        OkHttpUtils.post()
                .url(ResourceUpdate.BULUSIGN)
                .params(map)
                .addFile("head",imgFile.getName(),imgFile)
                .build()
                .execute(new StringCallback() {
            boolean makeUpSuccess = false;
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e("HAHA", "补录失败--->" + e != null? e.getMessage() :"NULL");
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e("HAHA", "onResponse: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int status = jsonObject.getInt("status");
                    makeUpSuccess = status == 1;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAfter(int id) {
                canMakeUp = true;
                if(listener != null){
                    listener.onMakeUped(image,makeUpSuccess);
                }
            }
        });
    }
    /**
     * 保存bitmap到本地
     *
     * @param mBitmap
     * @return
     */
    public String saveBitmap(Bitmap mBitmap) {
        File filePic;
        try {
            //格式化时间
            long time = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String sdfTime = sdf.format(time);
            filePic = new File(SCREEN_BASE_PATH + today + "/" + sdfTime + ".jpg");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return filePic.getAbsolutePath();
    }
    private static String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String SCREEN_BASE_PATH = sdPath + "/mnt/sdcard/photo/";//人脸头像存储路径
}