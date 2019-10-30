package com.yunbiao.ybsmartcheckin_live_id.business;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.jdjr.risk.face.local.extract.FaceProperty;
import com.jdjr.risk.face.local.user.FaceUser;
import com.jdjr.risk.face.local.verify.VerifyResult;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.Config;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateSignDataEvent;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.db.SignDao;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.db2.User;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;
import okhttp3.Call;

/**
 * Created by Administrator on 2019/3/18.
 */

public class SignManager {
    private final String TAG = getClass().getSimpleName();
    private static SignManager instance;
    private SignDao signDao;
    private final int UPDATE_TIME = 20;
    private SignEventListener listener;
    private String today;
    private Activity mAct;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
    private final ExecutorService threadPool;
    private final ScheduledExecutorService autoUploadThread;
    private long verifyOffsetTime = 10000;//验证间隔时间

    private SimpleDateFormat typeSdf = new SimpleDateFormat("HH:mm");
    private boolean isDebug = true;
    private boolean isBulu = false;
    private boolean isBuluing = false;

    private Map<Long, Long> passageMap = new HashMap<>();

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

        threadPool = Executors.newFixedThreadPool(2);
        threadPool.execute(initRunnable);

        autoUploadThread = Executors.newSingleThreadScheduledExecutor();
        autoUploadThread.scheduleAtFixedRate(autoUploadRunnable, 10, UPDATE_TIME, TimeUnit.MINUTES);
    }

    //初始化线程
    private Runnable initRunnable = new Runnable() {
        @Override
        public void run() {
            int compId = SpUtils.getInt(SpUtils.COMPANYID);
            final List<Sign> signs = DaoManager.get().querySignByComIdAndDate(compId, today);
            if (signs != null) {
                for (Sign signBean : signs) {
                    long time = signBean.getTime();
                    long faceId = signBean.getFaceId();
                    if (passageMap.containsKey(faceId)) {
                        long time1 = passageMap.get(faceId);
                        if (time > time1) {
                            passageMap.put(faceId, time);
                        } else {
                            continue;
                        }
                    }
                }
            }

            Collections.reverse(signs);

            if (listener != null) {
                mAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onPrepared(signs);
                    }
                });
            }

            clearJDVerifyRecord();
        }
    };

    public void uploadSignRecord(final Consumer<Boolean> callback) {
        final List<Sign> signs = DaoManager.get().querySignByUpload(false);
        if (signs == null) {
            return;
        }
        Log.e(TAG, "run: ------ 未上传：" + signs.size());

        if (signs.size() <= 0) {
            try {
                callback.accept(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        List<SignDataBean> signDataBeans = new ArrayList<>();
        for (Sign signBean : signs) {
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
                        try {
                            callback.accept(false);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.e(TAG, "onResponse: 上传结果：" + response);
                        JSONObject jsonObject = JSONObject.parseObject(response);
                        String status = jsonObject.getString("status");
                        boolean isSucc = TextUtils.equals("1", status);
                        try {
                            callback.accept(isSucc);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (!isSucc) {
                            return;
                        }
                        for (Sign signBean : signs) {
                            Log.e(TAG, "run: ---" + signBean.getSex());
                            signBean.setUpload(true);
                            DaoManager.get().addOrUpdate(signBean);
                        }
                    }
                });
    }

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

            uploadSignRecord(new Consumer<Boolean>() {
                @Override
                public void accept(Boolean aBoolean) throws Exception {
                    if (aBoolean) {
                        EventBus.getDefault().post(new UpdateSignDataEvent());
                    }
                    d("处理情况：" + aBoolean);
                }
            });

            clearJDVerifyRecord();
        }
    };

    public SignManager init(@NonNull Activity mAct, @NonNull SignEventListener signEventListener) {
        this.mAct = mAct;
        listener = signEventListener;

        return instance();
    }

    public void checkSign(VerifyResult verifyResult) {
        if (isBulu) {
            makeUpSign(verifyResult.getFaceImageBytes());
            return;
        }
        if (verifyResult.getResult() != VerifyResult.UNKNOWN_FACE) {
            return;
        }

        final byte[] faceImageBytes = verifyResult.getFaceImageBytes();
        FaceUser user = verifyResult.getUser();
        if (user == null) {
            return;
        }
        String userId = user.getUserId();
        if (TextUtils.isEmpty(userId)) {
            return;
        }

        final long currTime = System.currentTimeMillis();
        final Sign sign = new Sign();
        sign.setTime(currTime);
        sign.setFaceId(Long.parseLong(userId));
        if (!canPass(sign)) {
            return;
        }

        User userBean = DaoManager.get().queryUserByFaceId(Long.parseLong(userId));
        if (userBean == null) {
            return;
        }

        sign.setEmployNum(userBean.getNumber());
        sign.setEmpId(userBean.getId());
        sign.setImgBytes(faceImageBytes);
        sign.setDepart(userBean.getDepartName());
        sign.setName(userBean.getName());
        sign.setUpload(false);
        sign.setDate(dateFormat.format(currTime));
        sign.setAutograph(userBean.getAutograph());
        sign.setPosition(userBean.getPosition());
        sign.setSex(userBean.getSex());
        sign.setComid(userBean.getCompanyId());

        if (listener != null) {
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onSigned(sign, getCurrentTime(currTime));
                }
            });
        }

        sendSignRecord(sign);
    }

    public void checkSign(VerifyResult verifyResult, FaceProperty faceProperty) {
        if (isBulu) {
            makeUpSign(verifyResult.getFaceImageBytes(), faceProperty);
            return;
        }

        if (verifyResult.getResult() != VerifyResult.UNKNOWN_FACE) {
            return;
        }

        byte[] faceImageBytes = verifyResult.getFaceImageBytes();
        FaceUser user = verifyResult.getUser();
        if (user == null) {
            return;
        }
        String userId = user.getUserId();
        if (TextUtils.isEmpty(userId)) {
            return;
        }

        final long currTime = System.currentTimeMillis();
        final Sign sign = new Sign();
        sign.setTime(currTime);
        sign.setFaceId(Long.parseLong(userId));
        if (!canPass(sign)) {
            return;
        }

        User userBean = DaoManager.get().queryUserByFaceId(Long.parseLong(userId));
        if (userBean == null) {
            return;
        }

        File imgFile = saveBitmap(currTime, faceImageBytes);
        sign.setEmployNum(userBean.getNumber());
        sign.setEmpId(userBean.getId());
        sign.setHeadPath(imgFile.getPath());
        sign.setDepart(userBean.getDepartName());
        sign.setName(userBean.getName());
        sign.setUpload(false);
        sign.setDate(dateFormat.format(currTime));
        sign.setAutograph(userBean.getAutograph());
        sign.setPosition(userBean.getPosition());
        sign.setSex(userBean.getSex());
        sign.setComid(userBean.getCompanyId());

        if (listener != null) {
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onSigned(sign, getCurrentTime(currTime));
                }
            });
        }

        sendSignRecord(sign);
    }

    private void sendSignRecord(final Sign signBean) {
        final Map<String, String> map = new HashMap<>();
        map.put("entryid", signBean.getEmpId() + "");
        map.put("signTime", signBean.getTime() + "");
        OkHttpUtils.post().url(ResourceUpdate.SIGNLOG).params(map).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                signBean.setUpload(false);
            }

            @Override
            public void onResponse(String response, int id) {
                JSONObject jsonObject = JSONObject.parseObject(response);
                signBean.setUpload(jsonObject.getInteger("status") == 1);
            }

            @Override
            public void onAfter(int id) {
                File imgFile = saveBitmap(signBean.getTime(), signBean.getImgBytes());
                signBean.setHeadPath(imgFile.getPath());
                DaoManager.get().addOrUpdate(signBean);
            }
        });
    }

    private boolean canPass(Sign signBean) {
        long faceId = signBean.getFaceId();
        if (!passageMap.containsKey(faceId)) {
            passageMap.put(faceId, signBean.getTime());
            return true;
        }

        long lastTime = passageMap.get(faceId);
        long currTime = signBean.getTime();
        boolean isCanPass = (currTime - lastTime) > verifyOffsetTime;
        if (isCanPass) {
            passageMap.put(faceId, currTime);
        }
        return isCanPass;
    }

    class SignDataBean {
        String entryid;
        long signTime;
    }

    private int getCurrentTime(long currTime) {//得到现在的时间与获取到的上下班时间对比
        Company company = SpUtils.getCompany();
        String gotime = "09:00";
        String downTime = "18:00";
        if (company != null) {
            if (!TextUtils.isEmpty(company.getGotime()))
                gotime = company.getGotime();
            if (!TextUtils.isEmpty(company.getDowntime()))
                downTime = company.getDowntime();
        }

        try {
            Date hourDate = typeSdf.parse(typeSdf.format(currTime));//现在的时间
            Date dataMorn = typeSdf.parse(gotime);//上班时间
            Date dataNoon = typeSdf.parse(downTime);//下班时间

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

    private void d(@NonNull String msg) {
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }

    public boolean isBuluState() {
        return isBulu;
    }

    public void startBulu() {
        isBulu = true;
    }

    public void makeUpSign(final byte[] faceImage) {
        FaceProperty faceProperty = new FaceProperty(0, 0, 0, null);
        makeUpSign(faceImage, faceProperty);
    }

    public void makeUpSign(final byte[] faceImage, final FaceProperty faceProperty) {
        if (isBuluing) {
            return;
        }
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                isBuluing = true;
                long currTime = System.currentTimeMillis();
                final File imgFile = saveBitmap(currTime, faceImage);

                Log.e(TAG, "run: -------------- 补录成功");

                isBuluing = false;
                isBulu = false;

                final Sign signBean = new Sign();
                signBean.setTime(currTime);
                signBean.setUpload(false);
                signBean.setDate(dateFormat.format(currTime));
                signBean.setName("员工补录");
                signBean.setHeadPath(imgFile.getPath());
                signBean.setSex(faceProperty.getGender());
                signBean.setComid(SpUtils.getInt(SpUtils.COMPANYID));
                Log.e(TAG, "run: -------------- " + signBean.toString());

                if (listener != null) {
                    mAct.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onMakeUped(signBean, true);
                        }
                    });
                }

                int companyid = SpUtils.getInt(SpUtils.COMPANYID);
                final Map<String, String> map = new HashMap<>();
                map.put("comId", companyid + "");
                OkHttpUtils.post()
                        .url(ResourceUpdate.BULUSIGN)
                        .params(map)
                        .addFile("head", imgFile.getName(), imgFile)
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                Log.e(TAG, "补录失败--->" + e != null ? e.getMessage() : "NULL");
                                e.printStackTrace();
                                signBean.setUpload(false);
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                Log.e(TAG, "onResponse: " + response);
                                JSONObject jsonObject = JSONObject.parseObject(response);
                                int status = jsonObject.getInteger("status");
                                signBean.setUpload(status == 1);
                            }

                            @Override
                            public void onAfter(int id) {
                                long l = DaoManager.get().addOrUpdate(signBean);
                                Log.e(TAG, "入库结果: " + l);

                                List<Sign> signs = DaoManager.get().queryAll(Sign.class);
                                for (Sign sign : signs) {
                                    Log.e(TAG, "onAfter: " + sign.toString());
                                }
                            }
                        });
            }
        });
    }

    /**
     * 保存bitmap到本地
     *
     * @return
     */
    public File saveBitmap(long time, byte[] mBitmapByteArry) {
        long start = System.currentTimeMillis();
        File filePic;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            final Bitmap image = BitmapFactory.decodeByteArray(mBitmapByteArry, 0, mBitmapByteArry.length, options);

            //格式化时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String today = sdf.format(time);
            sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String sdfTime = sdf.format(time);
            filePic = new File(Constants.RECORD_PATH + "/" + today + "/" + sdfTime + ".jpg");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            image.compress(Bitmap.CompressFormat.JPEG, Config.getCompressRatio(), fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        long end = System.currentTimeMillis();
        Log.e("Compress", "saveBitmap: 压缩耗时----- " + (end - start));
        return filePic;
    }

    /*定时清除京东SDK验证记录*/
    private void clearJDVerifyRecord() {
        int count = 0;
        int failed = 0;
        File dirFile = new File(APP.getContext().getDir("VerifyRecord", Context.MODE_PRIVATE).getAbsolutePath());
        File[] files = dirFile.listFiles();
        for (File file : files) {
            if (file != null) {
                if (file.delete()) {
                    count++;
                } else {
                    failed++;
                }
            } else {
                failed++;
            }
        }
        Log.e(TAG, "总共清除记录：" + count + "条" + "，失败：" + failed + "条");
    }

    public interface SignEventListener {
        void onPrepared(List<Sign> mList);

        void onSigned(Sign sign, int signType);

        void onMakeUped(Sign sign, boolean makeUpSuccess);
    }

}