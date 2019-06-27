package com.yunbiao.ybsmartcheckin_live_id.business;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.jdjr.risk.face.local.user.FaceUser;
import com.jdjr.risk.face.local.verify.VerifyResult;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.db.SignBean;
import com.yunbiao.ybsmartcheckin_live_id.db.SignDao;
import com.yunbiao.ybsmartcheckin_live_id.db.VIPDetail;
import com.yunbiao.ybsmartcheckin_live_id.db.UserDao;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;

/**
 * Created by Administrator on 2019/3/18.
 */

public class SignManager {
    private final String TAG = getClass().getSimpleName();
    private static SignManager instance;
    private SignDao signDao;
    private final int UPDATE_TIME = 60;
    private SignEventListener listener;
    private String today;
    private Activity mAct;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
    private final ExecutorService threadPool;
    private final ScheduledExecutorService autoUploadThread;
    private Object signLock = new Object();

    private long verifyOffsetTime = 10000;//验证间隔时间
    private final UserDao userDao;

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
        today = dateFormat.format(new Date());
        signDao = APP.getSignDao();
        userDao = APP.getUserDao();

        threadPool = Executors.newFixedThreadPool(2);
        threadPool.execute(initRunnable);

        autoUploadThread = Executors.newSingleThreadScheduledExecutor();
        autoUploadThread.scheduleAtFixedRate(autoUploadRunnable, 10, UPDATE_TIME, TimeUnit.SECONDS);
    }

    private Map<Integer, SignBean> passageMap = new HashMap<>();
    //初始化线程
    private Runnable initRunnable = new Runnable() {
        @Override
        public void run() {
            final List<SignBean> signBeans = signDao.queryByDate(today);
            if (signBeans == null) {
                return;
            }
            for (SignBean signBean : signBeans) {
                long time = signBean.getTime();
                int faceId = signBean.getFaceId();
                if (passageMap.containsKey(faceId)) {
                    long time1 = passageMap.get(faceId).getTime();
                    if (time > time1) {
                        passageMap.put(faceId, signBean);
                    } else {
                        continue;
                    }
                }
            }

            if (listener != null) {
                mAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onPrepared(signBeans);
                    }
                });
            }
        }
    };


    //定时发送签到数据
    private Runnable autoUploadRunnable = new Runnable() {
        @Override
        public void run() {
            String currDate = dateFormat.format(new Date());
            if (!TextUtils.equals(currDate, today)) {
                today = currDate;
                passageMap.clear();
                initRunnable.run();
            }

            final List<SignBean> signList = signDao.queryByIsUpload(false);
            if (signList == null) {
                return;
            }
            Log.e(TAG, "run: ------ 未上传：" + signList.size());

            if (signList.size() <= 0) {
                return;
            }

            List<SignDataBean> signDataBeans = new ArrayList<>();
            for (SignBean signBean : signList) {
                SignDataBean signDataBean = new SignDataBean();
                signDataBean.entryid = signBean.getEmpId() + "";
                signDataBean.signTime = signBean.getTime();
                signDataBeans.add(signDataBean);
            }

            String jsonStr = new Gson().toJson(signDataBeans);
            d(ResourceUpdate.SIGNARRAY + " --- " + jsonStr);
            OkHttpUtils.post()
                    .addParams("signstr", jsonStr)
                    .url(ResourceUpdate.SIGNARRAY)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            d("上送失败--->" + e != null ? e.getMessage() : "NULL");
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.e(TAG, "onResponse: 上传结果：" + response);
                            JSONObject jsonObject = JSONObject.parseObject(response);
                            String status = jsonObject.getString("status");
                            if (!TextUtils.equals("1", status)) {
                                return;
                            }

                            threadPool.execute(new Runnable() {
                                @Override
                                public void run() {
                                    for (SignBean signBean : signList) {
                                        signBean.setUpload(true);
                                        signDao.update(signBean);
                                    }
                                }
                            });
                        }
                    });
        }
    };

    public SignManager init(@NonNull Activity mAct, @NonNull SignEventListener signEventListener) {
        this.mAct = mAct;
        listener = signEventListener;

        return instance();
    }

    /***
     * 签到
     */
    public void sign(final VerifyResult verifyResult) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                checkSign(verifyResult);
            }
        });
    }

    public void checkSign(VerifyResult verifyResult){
        synchronized (signLock){
            byte[] faceImageBytes = verifyResult.getFaceImageBytes();
            FaceUser user = verifyResult.getUser();
            if(user == null){
                return;
            }
            String userId = user.getUserId();
            if (TextUtils.isEmpty(userId)) {
                return;
            }

            List<VIPDetail> vipDetails = userDao.queryByFaceId(Integer.valueOf(userId));
            if (vipDetails == null || vipDetails.size() <= 0) {
                return;
            }

            final long currTime = System.currentTimeMillis();
            File imgFile = saveBitmap(currTime, faceImageBytes);

            VIPDetail vipDetail = vipDetails.get(0);
            final SignBean signBean = new SignBean();
            signBean.setEmployNum(vipDetail.getEmployNum());
            signBean.setEmpId(vipDetail.getEmpId());
            signBean.setFaceId(vipDetail.getFaceId());
            signBean.setImgUrl(imgFile.getPath());
            signBean.setTime(currTime);
            signBean.setDepart(vipDetail.getDepart());
            signBean.setName(vipDetail.getName());
            signBean.setUpload(false);
            signBean.setDate(dateFormat.format(currTime));
            signBean.setSignature(vipDetail.getSignature());
            signBean.setJob(vipDetail.getJob());
            signBean.setBirthday(vipDetail.getBirthday());

            if(!canPass(signBean)){
                return;
            }

            if(listener != null){
                mAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onSigned(signBean,getCurrentTime(currTime));
                    }
                });
            }

            sendSignRecord(signBean);
        }
    }

    private void sendSignRecord(final SignBean signBean){
        final Map<String, String> map = new HashMap<>();
        map.put("entryid", signBean.getEmpId() + "");
        map.put("signTime", signBean.getTime() + "");
        Log.e(TAG, "发送签到：" + ResourceUpdate.SIGNLOG + " --- " + map.toString());
        OkHttpUtils.post().url(ResourceUpdate.SIGNLOG).params(map).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG, "onError: " + e != null ? e.getMessage() : "NULL");
                signBean.setUpload(false);
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG, "onResponse: " + response);
                JSONObject jsonObject = JSONObject.parseObject(response);
                signBean.setUpload(jsonObject.getInteger("status") == 1);

            }

            @Override
            public void onAfter(int id) {
                signDao.insert(signBean);
            }
        });
    }

    private boolean canPass(SignBean signBean) {
        int faceId = signBean.getFaceId();
        if (!passageMap.containsKey(faceId)) {
            passageMap.put(faceId, signBean);
            return true;
        }

        SignBean cacheBean = passageMap.get(faceId);
        long lastTime = cacheBean.getTime();
        long currTime = signBean.getTime();
        boolean isCanPass = (currTime - lastTime) > verifyOffsetTime;
        if (isCanPass) {
            passageMap.put(faceId, signBean);
        }
        return isCanPass;
    }

    class SignDataBean {
        String entryid;
        long signTime;
    }

    SimpleDateFormat typeSdf = new SimpleDateFormat("HH:mm");
    private int getCurrentTime(long currTime) {//得到现在的时间与获取到的上下班时间对比
        try {
            Date hourDate = typeSdf.parse(typeSdf.format(currTime));//现在的时间
            Date dataMorn = typeSdf.parse(SpUtils.getStr(SpUtils.GOTIME, "09:00"));//上班时间
            Date dataNoon = typeSdf.parse(SpUtils.getStr(SpUtils.DOWNTIME, "18:00"));//下班时间

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

    private static boolean canMakeUp = true;

    public static boolean canMakeUp() {
        return canMakeUp;
    }

    public void makeUpSign(byte[] faceImage) {
        canMakeUp = false;
        final File imgFile = saveBitmap(System.currentTimeMillis(),faceImage);

        int companyid = SpUtils.getInt(SpUtils.COMPANYID);
        final Map<String, String> map = new HashMap<>();
        map.put("comId", companyid + "");

        Log.e("HAHA", "makeUpSign: -----" + imgFile.exists() + "-----" + imgFile.length());

        Log.e("HAHA", "makeUpSign: " + ResourceUpdate.BULUSIGN + "-----" + map.toString());
        OkHttpUtils.post()
                .url(ResourceUpdate.BULUSIGN)
                .params(map)
                .addFile("head", imgFile.getName(), imgFile)
                .build()
                .execute(new StringCallback() {
                    boolean makeUpSuccess = false;

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e("HAHA", "补录失败--->" + e != null ? e.getMessage() : "NULL");
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.e("HAHA", "onResponse: " + response);
                        JSONObject jsonObject = JSONObject.parseObject(response);
                        int status = jsonObject.getInteger("status");
                        makeUpSuccess = status == 1;
                    }

                    @Override
                    public void onAfter(int id) {
                        canMakeUp = true;
                        if (listener != null) {
                            listener.onMakeUped(imgFile.getPath(), makeUpSuccess);
                        }
                    }
                });
    }

    /**
     * 保存bitmap到本地
     *
     * @return
     */
    public File saveBitmap(long time, byte[] mBitmapByteArry) {
        File filePic;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            final Bitmap image = BitmapFactory.decodeByteArray(mBitmapByteArry, 0, mBitmapByteArry.length, options);

            //格式化时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String today = sdf.format(time);
            sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String sdfTime = sdf.format(time);
            filePic = new File(Constants.CURRENT_FACE_CACHE_PATH + "/" + today + "/" + sdfTime + ".png");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return filePic;
    }


    public interface SignEventListener {
        void onPrepared(List<SignBean> mList);

        void onSigned(SignBean signBean, int signType);

        void onMakeUped(String imgPath, boolean makeUpSuccess);
    }

}