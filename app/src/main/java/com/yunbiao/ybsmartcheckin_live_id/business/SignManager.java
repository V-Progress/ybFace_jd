package com.yunbiao.ybsmartcheckin_live_id.business;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.arcsoft.face.util.ImageUtils;
import com.google.gson.Gson;
import com.jdjr.risk.face.local.extract.FaceProperty;
import com.yunbiao.faceview.CompareResult;
import com.yunbiao.ybsmartcheckin_live_id.Config;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateSignDataEvent;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.db2.User;
import com.yunbiao.ybsmartcheckin_live_id.db2.Visitor;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFormBuilder;
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
import okhttp3.Request;

/**
 * Created by Administrator on 2019/3/18.
 */

public class SignManager {
    private final String TAG = getClass().getSimpleName();
    private static SignManager instance;
    private final int UPDATE_TIME = 20;
    //    private SignEventListener listener;
    private String today;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
    private final ExecutorService threadPool;
    private final ScheduledExecutorService autoUploadThread;
    private long verifyOffsetTime = 10000;//验证间隔时间

    private SimpleDateFormat visitSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private SimpleDateFormat typeSdf = new SimpleDateFormat("HH:mm");
    private boolean isDebug = true;
    private boolean isBulu = false;
    private boolean isBuluing = false;

    private Map<String, Long> passageMap = new HashMap<>();

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

        threadPool = Executors.newFixedThreadPool(2);

        autoUploadThread = Executors.newSingleThreadScheduledExecutor();
        autoUploadThread.scheduleAtFixedRate(autoUploadRunnable, 10, UPDATE_TIME, TimeUnit.MINUTES);
    }

    public List<Sign> getTodaySignData() {
        int compId = SpUtils.getInt(SpUtils.COMPANYID);
        List<Sign> signs = DaoManager.get().querySignByComIdAndDate(compId, today);
        if (signs == null || signs.size() <= 0) {
            return null;
        }
        Collections.reverse(signs);
        return signs;
    }

    //定时发送签到数据
    private Runnable autoUploadRunnable = new Runnable() {
        @Override
        public void run() {
            String currDate = dateFormat.format(new Date());
            if (!TextUtils.equals(currDate, today)) {
                today = currDate;
                passageMap.clear();
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
        }
    };

    //开始上传记录
    public void uploadSignRecord(final Consumer<Boolean> callback) {
        final List<Sign> signs = DaoManager.get().querySignByUpload(false);
        if (signs == null) {
            return;
        }
        Log.e(TAG, "run: ------ 未上传记录：" + signs.size());

        if (signs.size() <= 0) {
            try {
                callback.accept(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        List<SignDataBean> signDataBeans = new ArrayList<>();
        List<VisitDataBean> visitDataBeans = new ArrayList<>();
        List<StrangerDataBean> strangerDataBeanList = new ArrayList<>();
        for (Sign signBean : signs) {
            if (signBean.getType() == 0) {
                SignDataBean signDataBean = new SignDataBean();
                signDataBean.entryid = signBean.getEmpId() + "";
                signDataBean.signTime = signBean.getTime();
                signDataBean.sign = signBean;
                signDataBeans.add(signDataBean);
            } else if (signBean.getType() == -9) {
                StrangerDataBean bean = new StrangerDataBean();
                bean.createTime = signBean.getTime();
                bean.temper = signBean.getTemperature();
                bean.headPath = signBean.getHeadPath();
                bean.sign = signBean;
                strangerDataBeanList.add(bean);
            } else {
                VisitDataBean bean = new VisitDataBean();
                bean.visitorId = signBean.getEmpId() + "";
                bean.createTime = signBean.getTime();
                bean.headPath = signBean.getHeadPath();
                bean.sign = signBean;
                visitDataBeans.add(bean);
            }
        }

        Log.e(TAG, "uploadSignRecord: 未上传考勤记录：" + signDataBeans.size());
        Log.e(TAG, "uploadSignRecord: 未上传访客记录：" + visitDataBeans.size());
        Log.e(TAG, "uploadSignRecord: 未上传陌生记录：" + strangerDataBeanList.size());

        //上传考勤数据
        uploadSignDataList(signDataBeans, callback);
        //上传访客数据
        uploadVisitDataList(visitDataBeans);
        //上传陌生人访问记录
        uploadStrangerData(strangerDataBeanList);
    }

    //上传陌生人访问记录
    private void uploadStrangerData(final List<StrangerDataBean> strangerDataBeanList) {
        if (strangerDataBeanList.size() > 0) {
            d("准备上传陌生人访问记录");
            int comid = SpUtils.getCompany().getComid();
            String jsonStr = new Gson().toJson(strangerDataBeanList);
            d(ResourceUpdate.UPLOAD_TEMPERETURE_EXCEPTION_ARRAY + " --- " + jsonStr);
            Map<String, File> fileMap = new HashMap<>();
            for (StrangerDataBean bean : strangerDataBeanList) {
                String headPath = bean.headPath;
                File headFile;
                if (TextUtils.isEmpty(headPath)) {
                    headFile = new File(Environment.getExternalStorageDirectory(), "1.txt");
                    if (headFile == null || !headFile.exists()) {
                        try {
                            headFile.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    headFile = new File(headPath);
                }
                fileMap.put(headFile.getName(), headFile);
            }

            OkHttpUtils.post()
                    .url(ResourceUpdate.VISITARRAY)
                    .addParams("witJson", jsonStr)
                    .addParams("deviceNo", HeartBeatClient.getDeviceNo())
                    .addParams("comId", comid + "")
                    .files("heads", fileMap)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onBefore(Request request, int id) {
                            super.onBefore(request, id);
                            d("开始上传----------");
                        }

                        @Override
                        public void onError(Call call, Exception e, int id) {
                            d("上送失败--->" + (e != null ? e.getMessage() : "NULL"));
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.e(TAG, "onResponse: 上传结果：" + response);
                            JSONObject jsonObject = JSONObject.parseObject(response);
                            String status = jsonObject.getString("status");
                            boolean isSucc = TextUtils.equals("1", status);
                            if (!isSucc) {
                                return;
                            }

                            for (StrangerDataBean bean : strangerDataBeanList) {
                                Sign sign = bean.sign;
                                sign.setUpload(true);
                                DaoManager.get().addOrUpdate(sign);
                            }
                        }
                    });
        }
    }

    //上传访客记录列表
    private void uploadVisitDataList(final List<VisitDataBean> visitDataBeans) {
        if (visitDataBeans.size() > 0) {
            d("准备上传访客记录");
            int comid = SpUtils.getCompany().getComid();
            String jsonStr = new Gson().toJson(visitDataBeans);
            d(ResourceUpdate.VISITARRAY + " --- " + jsonStr);
            Map<String, File> fileMap = new HashMap<>();
            for (VisitDataBean visitDataBean : visitDataBeans) {
                String headPath = visitDataBean.headPath;
                File headFile;
                if (TextUtils.isEmpty(headPath)) {
                    headFile = new File(Environment.getExternalStorageDirectory(), "1.txt");
                    if (headFile == null || !headFile.exists()) {
                        try {
                            headFile.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    headFile = new File(headPath);
                }
                fileMap.put(headFile.getName(), headFile);
            }

            OkHttpUtils.post()
                    .url(ResourceUpdate.VISITARRAY)
                    .addParams("witJson", jsonStr)
                    .addParams("deviceNo", HeartBeatClient.getDeviceNo())
                    .addParams("comId", comid + "")
                    .files("heads", fileMap)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onBefore(Request request, int id) {
                            super.onBefore(request, id);
                            d("开始上传----------");
                        }

                        @Override
                        public void onError(Call call, Exception e, int id) {
                            d("上送失败--->" + (e != null ? e.getMessage() : "NULL"));
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.e(TAG, "onResponse: 上传结果：" + response);
                            JSONObject jsonObject = JSONObject.parseObject(response);
                            String status = jsonObject.getString("status");
                            boolean isSucc = TextUtils.equals("1", status);
                            if (!isSucc) {
                                return;
                            }
                            for (VisitDataBean visitDataBean : visitDataBeans) {
                                Sign sign = visitDataBean.sign;
                                sign.setUpload(true);
                                DaoManager.get().addOrUpdate(sign);
                            }
                        }
                    });
        }
    }

    //上传考勤记录列表
    private void uploadSignDataList(final List<SignDataBean> signDataBeans, final Consumer<Boolean> callback) {
        if (signDataBeans.size() > 0) {
            String jsonStr = new Gson().toJson(signDataBeans);
            d(ResourceUpdate.SIGNARRAY + " --- " + jsonStr);
            OkHttpUtils.post()
                    .addParams("signstr", jsonStr)
                    .addParams("deviceId", HeartBeatClient.getDeviceNo())
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
                            for (SignDataBean signDataBean : signDataBeans) {
                                Sign sign = signDataBean.sign;
                                sign.setUpload(true);
                                DaoManager.get().addOrUpdate(sign);
                            }
                        }
                    });
        }
    }

    /**
     * 刷卡打卡
     *
     * @param barCode
     * @return
     */
    public Sign checkSignForCard(String barCode) {
        User user = DaoManager.get().queryUserByCardId(barCode);
        if (user == null) {
            return null;
        }

        final Date currDate = new Date();
        final Sign sign = new Sign();
        sign.setTime(currDate.getTime());
        sign.setFaceId(user.getFaceId());
        sign.setUpload(false);
        sign.setDate(dateFormat.format(currDate.getTime()));

        sign.setEmployNum(user.getNumber());
        sign.setEmpId(user.getId());
        sign.setDepart(user.getDepartName());
        sign.setName(user.getName());
        sign.setAutograph(user.getAutograph());
        sign.setPosition(user.getPosition());
        sign.setSex(user.getSex());
        sign.setComid(user.getCompanyId());
        sign.setType(0);
        sign.setHeadPath(user.getHeadPath());

        sendSignRecord(sign);

        return sign;
    }

    //人脸打卡和访客打卡
    public Sign checkSignData(CompareResult compareResult, float temperature) {
        String userId = compareResult.getUserName();
        final Date currDate = new Date();
        final Sign sign = new Sign();
        sign.setTime(currDate.getTime());
        sign.setFaceId(userId);
        sign.setTemperature(temperature);

        if (canPass(sign)) {//可以打卡
            sign.setUpload(false);
            sign.setDate(dateFormat.format(currDate.getTime()));

            if (userId.startsWith("vi")) {
                Visitor visitor = DaoManager.get().queryVisitorByFaceId(userId);
                if (visitor != null) {
                    sign.setEmpId(visitor.getId());
                    sign.setComid(visitor.getComId());
                    sign.setName(visitor.getName());
                    sign.setDepart("访客");
                    sign.setType(-1);
                    sign.setVisEntryId(visitor.getVisEntryId());
                    sign.setAutograph("访客");
                    sign.setHeadPath(visitor.getHeadPath());

                    String currStart = visitor.getCurrStart();
                    String currEnd = visitor.getCurrEnd();

                    Log.e(TAG, "访问开始时间：" + currStart);
                    Log.e(TAG, "访问结束时间：" + currEnd);

                    try {
                        Date start = visitSdf.parse(currStart);
                        Date end = visitSdf.parse(currEnd);
                        //在开始时间之前或者在结束时间之后
                        if (currDate.before(start) || currDate.after(end)) {
                            Log.e(TAG, "不在访问期内");
                            sign.setType(-2);
                            sign.setAutograph("不在访问期内");
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    notifyInterviewed(visitor.getId(), visitor.getVisEntryId());
                    sendVisitRecord(sign);

                    return sign;
                }
            } else {
                User userBean = DaoManager.get().queryUserByFaceId(userId);
                //如果在员工库中未查到
                if (userBean == null) {
                    return null;
                }

                sign.setEmployNum(userBean.getNumber());
                sign.setEmpId(userBean.getId());
                sign.setDepart(userBean.getDepartName());
                sign.setName(userBean.getName());
                sign.setAutograph(userBean.getAutograph());
                sign.setPosition(userBean.getPosition());
                sign.setSex(userBean.getSex());
                sign.setComid(userBean.getCompanyId());
                sign.setType(0);
                sign.setHeadPath(userBean.getHeadPath());

                sendSignRecord(sign);

                return sign;
            }
        }
        return null;
    }

    public Sign getTemperatureSign(float temperatureValue) {
        Date currDate = new Date();
        final Sign sign = new Sign();
        sign.setType(-9);
        sign.setTime(currDate.getTime());
        sign.setTemperature(temperatureValue);
        sign.setDate(dateFormat.format(currDate));
        sign.setUpload(false);
        return sign;
    }

    /*0：主线程add
1：子线程上传
2：子线程addOrUpdate
3.主线程addOrUpdate*/
    public void uploadTemperatureSign(final Sign sign) {
        String url = ResourceUpdate.UPLOAD_TEMPERETURE_EXCEPTION;

        File file = saveBitmap(sign.getTime(), sign.getImgBitmap());
        sign.setHeadPath(file.getPath());

        File hotFile = null;
        Bitmap hotImageBitmap = sign.getHotImageBitmap();
        if (hotImageBitmap != null) {
            hotFile = saveBitmap("hot_", sign.getTime(), hotImageBitmap);
        }

        Map<String, String> params = new HashMap<>();
        params.put("deviceNo", HeartBeatClient.getDeviceNo());
        params.put("comId", SpUtils.getCompany().getComid() + "");
        params.put("temper", sign.getTemperature() + "");
        if (sign.getType() != -9) {
            params.put("entryId", sign.getEmpId() + "");
        }
        Log.e(TAG, "上传温度");
        Log.e(TAG, "地址：" + url);
        Log.e(TAG, "参数: " + params.toString());
        PostFormBuilder builder = OkHttpUtils.post()
                .url(url)
                .params(params);
        builder.addFile("heads", file.getName(), file);
        if (hotFile != null) {
            builder.addFile("reHead", hotFile.getName(), hotFile);
        }
        builder.build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG, "onError: 上传失败：" + (e == null ? "NULL" : e.getMessage()));
                sign.setUpload(false);
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG, "onResponse: 上传成功：" + response);
                sign.setUpload(true);
            }

            @Override
            public void onAfter(int id) {
                DaoManager.get().addOrUpdate(sign);
            }
        });
    }

    //通知被访人
    private void notifyInterviewed(long visitorId, long visEntryId) {
        String url = ResourceUpdate.SEND_VIS_ENTRY;
        Map<String, String> params = new HashMap<>();
        params.put("visitorId", String.valueOf(visitorId));
        params.put("visEntryId", String.valueOf(visEntryId));
        params.put("deviceNo", HeartBeatClient.getDeviceNo());
        Log.e(TAG, "通知被访人：" + url);
        Log.e(TAG, "地址：" + url);
        Log.e(TAG, "参数：" + params.toString());

        OkHttpUtils.post()
                .url(url)// TODO: 2019/12/7
                .params(params)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "通知被访人：onError: " + (e == null ? "NULL" : e.getMessage()));
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.e(TAG, "通知被访人：onResponse: " + response);
                    }
                });
    }

    //上送单条访客记录
    private void sendVisitRecord(final Sign signBean) {
        Map<String, String> map = new HashMap<>();
        map.put("deviceId", HeartBeatClient.getDeviceNo());
        map.put("comId", "" + signBean.getComid());
        map.put("visitorId", signBean.getEmpId() + "");
        d("上传访客记录");
        d("地址：" + ResourceUpdate.VISITOLOG);
        d("参数：" + map.toString());

//        File file = saveBitmap(signBean.getTime(), signBean.getImgBytes());
//        signBean.setHeadPath(file.getPath());
        File file = new File(signBean.getHeadPath());
        OkHttpUtils.post()
                .url(ResourceUpdate.VISITOLOG).params(map)
                .addFile("heads", file.getName(), file)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        d("上传失败：" + (e == null ? "NULL" : e.getMessage()));
                        signBean.setUpload(false);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        d("上传结果：" + response);
                        JSONObject jsonObject = JSONObject.parseObject(response);
                        signBean.setUpload(jsonObject.getInteger("status") == 1);
                    }

                    @Override
                    public void onAfter(int id) {
                        DaoManager.get().addOrUpdate(signBean);
                    }
                });
    }

    //上送单条考勤记录
    private void sendSignRecord(final Sign signBean) {
        final Map<String, String> map = new HashMap<>();
        map.put("entryid", signBean.getEmpId() + "");
        map.put("signTime", signBean.getTime() + "");
        map.put("deviceId", HeartBeatClient.getDeviceNo());
        map.put("temper", signBean.getTemperature() + "");
        d("上传考勤记录");
        d("地址：" + ResourceUpdate.VISITOLOG);
        d("参数：" + map.toString());

        OkHttpUtils.post().url(ResourceUpdate.SIGNLOG).params(map).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                d("上传失败：" + (e == null ? "NULL" : e.getMessage()));
                signBean.setUpload(false);
            }

            @Override
            public void onResponse(String response, int id) {
                d("上传结果：" + response);
                JSONObject jsonObject = JSONObject.parseObject(response);
                signBean.setUpload(jsonObject.getInteger("status") == 1);
            }

            @Override
            public void onAfter(int id) {
                DaoManager.get().addOrUpdate(signBean);
                /*File imgFile = saveBitmap(signBean.getTime(), signBean.getImgBytes());
                if(imgFile != null){
                    signBean.setHeadPath(imgFile.getPath());
                }*/
            }
        });
    }

    //判断是否可打卡
    private boolean canPass(Sign signBean) {
        String faceId = signBean.getFaceId();
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

    //*************************************************************************************************8
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

                /*if (listener != null) {
                    mAct.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onMakeUped(signBean, true);
                        }
                    });
                }*/

                int companyid = SpUtils.getInt(SpUtils.COMPANYID);
                final Map<String, String> map = new HashMap<>();
                map.put("comId", companyid + "");
                map.put("deviceId", HeartBeatClient.getDeviceNo());
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

    public File saveBitmap(long time, Bitmap bitmap) {
        return saveBitmap("", time, bitmap);
    }

    public File saveBitmap(String preName, long time, Bitmap bitmap) {
        File filePic;
        try {
            //格式化时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String today = sdf.format(time);
            sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String sdfTime = sdf.format(time);

            //添加附加名
            preName = TextUtils.isEmpty(preName) ? "" : (preName + "_");
            filePic = new File(Constants.RECORD_PATH + "/" + today + "/" + preName + sdfTime + ".jpg");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            bitmap.compress(Bitmap.CompressFormat.JPEG, Config.getCompressRatio(), fos);
            fos.flush();
            fos.close();

            return filePic;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 保存bitmap到本地
     *
     * @return
     */
    public File saveBitmap(long time, byte[] mBitmapByteArry) {
        if (mBitmapByteArry == null) {
            return null;
        }
        final BitmapFactory.Options options = new BitmapFactory.Options();
        final Bitmap image = BitmapFactory.decodeByteArray(mBitmapByteArry, 0, mBitmapByteArry.length, options);

        return saveBitmap("", time, image);
    }

    class SignDataBean {
        String entryid;
        long signTime;
        transient Sign sign;
    }

    class VisitDataBean {
        String visitorId;
        long createTime;
        transient String headPath;
        transient Sign sign;
    }

    class StrangerDataBean {
        long createTime;
        float temper;
        transient String headPath;
        transient Sign sign;
    }

    private void d(@NonNull String msg) {
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }

}