package com.yunbiao.ybsmartcheckin_live_id.business;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.yunbiao.faceview.CompareResult;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateSignDataEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_multiple.MultiTemperBean;
import com.yunbiao.ybsmartcheckin_live_id.utils.IdCardMsg;
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
import com.zhy.http.okhttp.request.RequestCall;

import org.apache.commons.io.IOUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.BufferedOutputStream;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    //    private final ExecutorService threadPool;
    private final ScheduledExecutorService autoUploadThread;
    private long verifyOffsetTime = 0;//验证间隔时间

    private SimpleDateFormat visitSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private boolean isDebug = true;
    private boolean isBulu = false;
    private boolean isBuluing = false;

    private Map<String, Long> passageMap = new HashMap<>();
    private final ScheduledExecutorService threadPool;

    public void setVerifyDelay(long delayTime) {
        verifyOffsetTime = delayTime;
    }

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

        autoUploadThread = Executors.newSingleThreadScheduledExecutor();
        threadPool = Executors.newScheduledThreadPool(5);

        if (Constants.DEVICE_TYPE != Constants.DeviceType.MULTIPLE_THERMAL && Constants.DEVICE_TYPE != Constants.DeviceType.HT_MULTIPLE_THERMAL) {
            autoUploadThread.scheduleAtFixedRate(autoUploadRunnable, 1, 1, TimeUnit.MINUTES);
        } else {
            startMultiUploadThread();
        }
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

        List<Sign> entrySignList = new ArrayList<>();
        List<Sign> visitorSignList = new ArrayList<>();
        List<Sign> temperSignList = new ArrayList<>();
        for (Sign signBean : signs) {
            // TODO: 2020/3/18 离线功能
            if (signBean.getComid() == Constants.NOT_BIND_COMPANY_ID) {
                continue;
            }
            if (signBean.getType() == 0) {
                entrySignList.add(signBean);
            } else if (signBean.getType() == -9) {
                temperSignList.add(signBean);
            } else {
                visitorSignList.add(signBean);
            }
        }

        //上传考勤数据
        uploadSignArray(entrySignList, callback);
        //上传访客数据
        uploadVisitorSignArray(visitorSignList, callback);
        //上传测温记录
        uploadTemperArray(temperSignList,callback);
    }

    private void uploadSignArray(final List<Sign> signList, final Consumer<Boolean> callback) {
        if (signList == null || signList.size() <= 0) {
            return;
        }

        List<EntrySignBean> signBeans = new ArrayList<>();
        for (Sign sign : signList) {
            signBeans.add(new EntrySignBean(sign.getEmpId(), sign.getTime()));
        }
        String jsonStr = new Gson().toJson(signBeans);
        d("批量上传考勤记录：" + ResourceUpdate.SIGNARRAY);
        d("批量上传考勤记录：参数1：" + jsonStr);
        d("批量上传考勤记录：参数2：" + HeartBeatClient.getDeviceNo());
        OkHttpUtils.post()
                .addParams("signstr", jsonStr)
                .addParams("deviceId", HeartBeatClient.getDeviceNo())
                .url(ResourceUpdate.SIGNARRAY)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        d("批量上传考勤记录：上送失败--->" + (e != null ? e.getMessage() : "NULL"));
                        e.printStackTrace();
                        try {
                            callback.accept(false);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.e(TAG, "批量上传考勤记录：onResponse: 上传结果：" + response);
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

                        for (Sign sign : signList) {
                            sign.setUpload(true);
                            DaoManager.get().addOrUpdate(sign);
                        }

                        uploadTemperArray(signList,callback);
                    }
                });
    }

    private void uploadTemperArray(final List<Sign> signList, final Consumer<Boolean> callback) {
        if(signList == null || signList.size() <= 0){
            return;
        }
        List<TemperSignBean> temperSignBeans = new ArrayList<>();
        for (Sign sign : signList) {
            temperSignBeans.add(new TemperSignBean(sign.getTime(), sign.getTemperature()));
        }
        String json = new Gson().toJson(temperSignBeans);

        Map<String, File> headMap = new HashMap<>();
        Map<String, File> hotMap = new HashMap<>();
        for (Sign sign : signList) {
            //存头像
            File headFile = getFileByPath(sign.getHeadPath());
            headMap.put(headFile.getName(), headFile);

            //存热图
            File hotFile = getFileByPath(sign.getHotImgPath());
            hotMap.put(hotFile.getName(), hotFile);
        }

        d("批量上传测温记录：" + ResourceUpdate.UPLOAD_TEMPERETURE_EXCEPTION_ARRAY);
        d("批量上传测温记录:参数：" + json);
        d("批量上传测温记录:头像文件：" + headMap.toString());
        d("批量上传测温记录:热图文件：" + hotMap.toString());

        int comid = SpUtils.getCompany().getComid();
        OkHttpUtils.post()
                .url(ResourceUpdate.UPLOAD_TEMPERETURE_EXCEPTION_ARRAY)
                .addParams("witJson", json)
                .addParams("deviceNo", HeartBeatClient.getDeviceNo())
                .addParams("comId", comid + "")
                .files("heads", headMap)
                .files("reHead", hotMap)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        d("批量上传测温记录:开始上传----------");
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        d("批量上传测温记录:上送失败--->" + (e != null ? e.getMessage() : "NULL"));
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.e(TAG, "批量上传测温记录:onResponse: 上传结果：" + response);
                        if(TextUtils.isEmpty(response)){
                            return;
                        }
                        JSONObject jsonObject = JSONObject.parseObject(response);
                        String status = jsonObject.getString("status");
                        boolean isSucc = TextUtils.equals("1", status);
                        if (!isSucc) {
                            return;
                        }

                        for (Sign sign : signList) {
                            if (sign.isUpload()) {
                                continue;
                            }
                            sign.setUpload(true);
                            DaoManager.get().addOrUpdate(sign);
                        }
                    }

                    @Override
                    public void onAfter(int id) {
                        super.onAfter(id);
                        if(callback != null){
                            try {
                                callback.accept(true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void uploadVisitorSignArray(final List<Sign> signList, final Consumer<Boolean> callback) {
        if (signList == null || signList.size() <= 0) {
            return;
        }
        List<VisitorSignBean> visitorSignBeans = new ArrayList<>();
        for (Sign sign : signList) {
            visitorSignBeans.add(new VisitorSignBean(sign.getEmpId(), sign.getTime()));
        }
        String json = new Gson().toJson(visitorSignBeans);

        Map<String, File> headMap = new HashMap<>();
        for (Sign sign : signList) {
            File fileByPath = getFileByPath(sign.getHeadPath());
            headMap.put(fileByPath.getName(), fileByPath);
        }
        d("批量上传访客记录：" + ResourceUpdate.VISITARRAY);
        d("批量上传访客记录：参数：" + json);
        StringBuffer stringBuffer = new StringBuffer();
        Iterator<Map.Entry<String, File>> iterator = headMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, File> next = iterator.next();
            stringBuffer.append(next.getKey()).append("\n");
        }
        d("批量上传访客记录：头像文件：" + stringBuffer.toString());

        int comid = SpUtils.getCompany().getComid();
        OkHttpUtils.post()
                .url(ResourceUpdate.VISITARRAY)
                .addParams("witJson", json)
                .addParams("deviceNo", HeartBeatClient.getDeviceNo())
                .addParams("comId", comid + "")
                .files("heads",headMap)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        d("批量上传访客记录：开始上传----------");
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        d("批量上传访客记录：上送失败--->" + (e != null ? e.getMessage() : "NULL"));
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.e(TAG, "批量上传访客记录：onResponse: 上传结果：" + response);
                        JSONObject jsonObject = JSONObject.parseObject(response);
                        String status = jsonObject.getString("status");
                        boolean isSucc = TextUtils.equals("1", status);
                        if (!isSucc) {
                            return;
                        }
                        for (Sign sign : signList) {
                            sign.setUpload(true);
                            DaoManager.get().addOrUpdate(sign);
                        }
                        uploadTemperArray(signList,callback);
                    }
                });
    }

    private File getFileByPath(String headPath) {
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
        return headFile;
    }

    class EntrySignBean {
        long entryid;
        long signTime;

        public EntrySignBean(long entryid, long signTime) {
            this.entryid = entryid;
            this.signTime = signTime;
        }
    }

    class TemperSignBean {
        long createTime;
        float temper;

        public TemperSignBean(long createTime, float temper) {
            this.createTime = createTime;
            this.temper = temper;
        }
    }

    class VisitorSignBean {
        long visitorId;
        long createTime;

        public VisitorSignBean(long visitorId, long createTime) {
            this.visitorId = visitorId;
            this.createTime = createTime;
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

    public Sign checkSignData(CompareResult compareResult, float temperature, boolean isUpload) {
        int comid = SpUtils.getCompany().getComid();
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
                // TODO: 2020/3/18 离线功能
                Visitor visitor = DaoManager.get().queryVisitorByComIdAndFaceId(comid, userId);
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

                    if (isUpload) {
                        notifyInterviewed(visitor.getId(), visitor.getVisEntryId());
                        sendVisitRecord(sign);
                    }

                    return sign;
                }
            } else {
                // TODO: 2020/3/18 离线功能
                User userBean = DaoManager.get().queryUserByComIdAndFaceId(comid, userId);
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

                if (isUpload) {
                    sendSignRecord(sign);
                }

                return sign;
            }
        }
        return null;
    }

    //人脸打卡和访客打卡
    public Sign checkSignData(CompareResult compareResult, float temperature) {
        return checkSignData(compareResult, temperature, true);
    }

    public Sign getTemperatureSign(float temperatureValue) {
        Date currDate = new Date();
        final Sign sign = new Sign();
        sign.setType(-9);
        sign.setTime(currDate.getTime());
        sign.setTemperature(temperatureValue);
        sign.setDate(dateFormat.format(currDate.getTime()));
        sign.setComid(SpUtils.getCompany().getComid());
        sign.setUpload(false);
        return sign;
    }

    public void uploadTemperatureSignAndDelete(Sign sign){
        String url = ResourceUpdate.UPLOAD_TEMPERETURE_EXCEPTION;

        File file;
        if (sign.getImgBitmap() != null) {
            file = saveBitmap(sign.getTime(), sign.getImgBitmap());
        } else {
            file = new File(Constants.LOCAL_ROOT_PATH + File.separator + "0.txt");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        sign.setHeadPath(file.getPath());
        Log.e(TAG, "uploadTemperatureSign: 保存头像：" + file.getPath());

        File hotFile;
        Bitmap hotImageBitmap = sign.getHotImageBitmap();
        if (hotImageBitmap != null) {
            hotFile = saveBitmap("hot_", sign.getTime(), hotImageBitmap);
        } else {
            hotFile = new File(Constants.LOCAL_ROOT_PATH + File.separator + "0.txt");
            if (!hotFile.exists()) {
                try {
                    hotFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.e(TAG, "uploadTemperatureSign: 保存热图：" + hotFile.getPath());
        sign.setHotImgPath(hotFile.getPath());

        // TODO: 2020/3/18 离线功能
        if (sign.getComid() == Constants.NOT_BIND_COMPANY_ID) {
            return;
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
        builder.addFile("reHead", hotFile.getName(), hotFile);

        builder.build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG, "onError: 上传失败：" + (e == null ? "NULL" : e.getMessage()));
                sign.setUpload(false);
                DaoManager.get().addOrUpdate(sign);
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG, "onResponse: 上传成功：" + response);
                JSONObject jsonObject = JSONObject.parseObject(response);
                String status = jsonObject.getString("status");
                boolean isSucc = TextUtils.equals("1", status);
                if(isSucc){
                    if(file != null && file.exists()){
                        boolean delete = file.delete();
                        Log.e(TAG, "onResponse: 头像删除：" + file.getPath() + " ----- " + delete);
                    }
                    if(hotFile != null && hotFile.exists()){
                        boolean delete = hotFile.delete();
                        Log.e(TAG, "onResponse: 热图删除：" + hotFile.getPath() + " ----- " + delete);
                    }
                }
            }

            @Override
            public void onAfter(int id) {
            }
        });
    }

    public void uploadTemperatureSign(final Sign sign) {
        String url = ResourceUpdate.UPLOAD_TEMPERETURE_EXCEPTION;

        File file;
        if (sign.getImgBitmap() != null) {
            file = saveBitmap(sign.getTime(), sign.getImgBitmap());
        } else {
            file = new File(Constants.LOCAL_ROOT_PATH + File.separator + "0.txt");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        sign.setHeadPath(file.getPath());
        Log.e(TAG, "uploadTemperatureSign: 保存头像：" + file.getPath());

        File hotFile = null;
        Bitmap hotImageBitmap = sign.getHotImageBitmap();
        if (hotImageBitmap != null) {
            hotFile = saveBitmap("hot_", sign.getTime(), hotImageBitmap);
        } else {
            hotFile = new File(Constants.LOCAL_ROOT_PATH + File.separator + "0.txt");
            if (!hotFile.exists()) {
                try {
                    hotFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.e(TAG, "uploadTemperatureSign: 保存热图：" + hotFile.getPath());
        sign.setHotImgPath(hotFile.getPath());

        sign.setUpload(false);
        DaoManager.get().addOrUpdate(sign);

        // TODO: 2020/3/18 离线功能
        if (sign.getComid() == Constants.NOT_BIND_COMPANY_ID) {
            return;
        }

        final long time = sign.getTime();

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
        builder.addFile("reHead", hotFile.getName(), hotFile);

        builder.build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG, "onError: 上传失败：" + (e == null ? "NULL" : e.getMessage()));
                sign.setUpload(false);
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG, "onResponse: 上传成功：" + response);
                Sign sign = DaoManager.get().querySignByTime(time);
                if (sign != null) {
                    sign.setUpload(true);
                    DaoManager.get().addOrUpdate(sign);
                }
            }

            @Override
            public void onAfter(int id) {

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
        DaoManager.get().addOrUpdate(signBean);

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
                        Sign sign = DaoManager.get().querySignByTime(signBean.getTime());
                        signBean.setUpload(jsonObject.getInteger("status") == 1);
                        DaoManager.get().addOrUpdate(sign);
                    }

                    @Override
                    public void onAfter(int id) {

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
        d("地址：" + ResourceUpdate.SIGNLOG);
        d("参数：" + map.toString());

        DaoManager.get().addOrUpdate(signBean);
        final long time = signBean.getTime();

        // TODO: 2020/3/18 离线功能
        if (signBean.getComid() == Constants.NOT_BIND_COMPANY_ID) {
            return;
        }
        OkHttpUtils.post().url(ResourceUpdate.SIGNLOG).params(map).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                d("上传失败：" + (e == null ? "NULL" : e.getMessage()));
            }

            @Override
            public void onResponse(String response, int id) {
                d("上传结果：" + response);
                JSONObject jsonObject = JSONObject.parseObject(response);
                Integer status = jsonObject.getInteger("status");
                Sign sign = DaoManager.get().querySignByTime(time);
                if (sign != null) {
                    sign.setUpload(status == 1 || status == 12);
                    DaoManager.get().addOrUpdate(sign);
                }
            }

            @Override
            public void onAfter(int id) {
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

    public void uploadNoIdCardResult(int isPass, Bitmap currCameraFrame, Float max, Bitmap mCacheHotImage) {
        String url = ResourceUpdate.UPLOAD_NO_IDCARD;
        Log.e(TAG, "uploadNoIdCardResult: 地址：" + url);

        Map<String, String> params = new HashMap<>();
        params.put("isPass", isPass + "");
        params.put("deviceNo", HeartBeatClient.getDeviceNo());
        params.put("temper", max + "");
        Log.e(TAG, "uploadNoIdCardResult: params：" + params.toString());

        PostFormBuilder builder = OkHttpUtils.post().url(url).params(params);

        File file;
        if (currCameraFrame != null) {
            file = saveBitmap(System.currentTimeMillis(), currCameraFrame);
        } else {
            file = new File(Constants.LOCAL_ROOT_PATH, "0.txt");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        builder.addFile("newHeads", file.getName(), file);
        Log.e(TAG, "uploadNoIdCardResult: 头像：" + file.getPath());

        File reFile;
        if (mCacheHotImage != null) {
            reFile = saveBitmap(System.currentTimeMillis(), mCacheHotImage);
        } else {
            reFile = new File(Constants.LOCAL_ROOT_PATH, "0.txt");
            if (!reFile.exists()) {
                try {
                    reFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        builder.addFile("reHead", reFile.getName(), reFile);
        Log.e(TAG, "uploadNoIdCardResult: 热量图：" + reFile.getPath());

        builder.build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG, "onError: " + (e == null ? "NULL" : e.getMessage()));
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG, "onResponse: " + response);
            }
        });


    }

    public void uploadCodeVerifyResult(String entryId, boolean isPass, Bitmap newHead, float temper, Bitmap reHead) {
        Map<String, String> params = new HashMap();
        params.put("entryId", entryId);
        params.put("deviceNo", HeartBeatClient.getDeviceNo());
        params.put("isPass", (isPass ? 0 : 1) + "");
        params.put("temper", temper + "");
        Log.e(TAG, "uploadCodeVerifyResult: 参数：" + params.toString());

        PostFormBuilder builder = OkHttpUtils.post().url(ResourceUpdate.UPLOAD_CODE_VERIFY_RESULT).params(params);

        File file = saveBitmap(System.currentTimeMillis(), newHead);
        builder.addFile("newHeads", file.getName(), file);
        Log.e(TAG, "uploadCodeVerifyResult: 截图：" + file.exists() + " --- " + file.getPath());

        File reFile;
        if (reHead != null) {
            reFile = saveBitmap("hot_", System.currentTimeMillis(), reHead);
        } else {
            reFile = new File(Constants.LOCAL_ROOT_PATH, "0.txt");
            if (!reFile.exists()) {
                try {
                    reFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.e(TAG, "uploadCodeVerifyResult: 热量图：" + reFile.exists() + " --- " + reFile.getPath());
        builder.addFile("reHead", reFile.getName(), reFile);

        builder.build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG, "onError: 上传失败：" + (e == null ? "NULL" : e.getMessage()));
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG, "onResponse: 上传结果：" + response);
            }
        });
    }

    public void uploadIdCardAndReImage(float temper, IdCardMsg msg, int similar, int isPass, Bitmap idCardBitmap, Bitmap faceBitmap, Bitmap reBitmap) {
        Log.e(TAG, "上传身份信息");
        String uploadIdcard = ResourceUpdate.UPLOAD_IDCARD;
        Log.e(TAG, "地址" + uploadIdcard);
        Log.e(TAG, "身份证图：" + (idCardBitmap == null ? (0 + "---" + 0) : (idCardBitmap.getWidth() + "---" + idCardBitmap.getHeight())));
        Log.e(TAG, "人脸图：" + (faceBitmap == null ? (0 + "---" + 0) : (faceBitmap.getWidth() + "---" + faceBitmap.getHeight())));
        Log.e(TAG, "热量图：" + (reBitmap == null ? (0 + "---" + 0) : (reBitmap.getWidth() + "---" + reBitmap.getHeight())));

        Map<String, String> params = new HashMap();
        params.put("similar", similar + "");
        params.put("name", msg.name.trim());
        params.put("sex", TextUtils.equals(msg.sex, "男") ? "1" : "0");
        params.put("nation", msg.nation_str);
        params.put("birthDate", msg.birth_year + "-" + msg.birth_month + "-" + msg.birth_day);
        params.put("IdCard", msg.id_num);
        params.put("address", msg.address);
        params.put("termDate", msg.useful_e_date_year + "-" + msg.useful_e_date_month + "-" + msg.useful_e_date_day);
        params.put("isPass", isPass + "");
        params.put("deviceNo", HeartBeatClient.getDeviceNo());
        params.put("comId", SpUtils.getCompany().getComid() + "");
        params.put("temper", temper + "");
        Log.e(TAG, "uploadIdCardAndReImage: " + params.toString());

        PostFormBuilder builder = OkHttpUtils.post().url(uploadIdcard).params(params);
        //存身份证图
        long l = System.currentTimeMillis();
        File idCardFile;
        if (idCardBitmap != null) {
            idCardFile = saveBitmap("idCard_", l, idCardBitmap);
            Log.e(TAG, "存身份证图：" + idCardFile.getPath());
        } else {
            idCardFile = new File(Constants.LOCAL_ROOT_PATH, "0.txt");
            if (!idCardFile.exists()) {
                try {
                    idCardFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        builder.addFile("oldHeads", idCardFile.getName(), idCardFile);
        //存人脸图
        File faceFile = null;
        if (faceBitmap != null) {
            faceFile = saveBitmap(l, faceBitmap);
            Log.e(TAG, "存人脸图：" + faceFile.getPath());
        } else {
            faceFile = new File(Constants.LOCAL_ROOT_PATH, "0.txt");
            if (!faceFile.exists()) {
                try {
                    faceFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        builder.addFile("newHeads", faceFile.getName(), faceFile);
        //存热量图
        File reFile = null;
        if (reBitmap != null) {
            reFile = saveBitmap("re_", l, reBitmap);
            Log.e(TAG, "存热量图：" + reFile.getPath());
        } else {
            reFile = new File(Constants.LOCAL_ROOT_PATH, "0.txt");
            if (!reFile.exists()) {
                try {
                    reFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        builder.addFile("reHead", reFile.getName(), reFile);

        //创建请求
        RequestCall build = builder.build();
        build.execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG, "onError: " + (e == null ? "NULL" : e.getMessage()));
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG, "onResponse: " + response);
            }
        });
    }

    //*************************************************************************************************8
    public boolean isBuluState() {
        return isBulu;
    }

    public void startBulu() {
        isBulu = true;
    }

   /* public void makeUpSign(final byte[] faceImage) {
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

                *//*if (listener != null) {
                    mAct.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onMakeUped(signBean, true);
                        }
                    });
                }*//*

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
    }*/

    public File saveBitmap(long time, Bitmap bitmap) {
        return saveBitmap("", time, bitmap);
    }

    public File saveBitmap(String preName, long time, Bitmap bitmap) {
        BufferedOutputStream buffer = null;
        try {
            //格式化时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String today = sdf.format(time);
            sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String sdfTime = sdf.format(time);

            //添加附加名
            preName = TextUtils.isEmpty(preName) ? "" : (preName + "_");
            File filePic = new File(Constants.RECORD_PATH + "/" + today + "/" + preName + sdfTime + ".jpg");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            buffer = IOUtils.buffer(new FileOutputStream(filePic));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, buffer);
            buffer.flush();
            return filePic;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (buffer != null) {
                try {
                    buffer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
    }

    private void d(@NonNull String msg) {
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }

    public void addSignToDB(MultiTemperBean multiTemperBean) {
        Sign sign = new Sign();
        //人脸图
        File file = saveBitmap(multiTemperBean.getTime(), multiTemperBean.getHeadImage());
        sign.setHeadPath(file.getPath());
        //热图
        File hotFile = saveBitmap("hot_", multiTemperBean.getTime(), multiTemperBean.getHotImage());
        sign.setHotImgPath(hotFile.getPath());
        //温度
        sign.setTemperature(multiTemperBean.getTemper());
        //公司Id
        sign.setComid(multiTemperBean.getCompId());
        //员工Id
        sign.setEmpId(multiTemperBean.getEntryId());
        //FaceId
        String faceId = multiTemperBean.getFaceId();
        sign.setFaceId(faceId);
        //类型
        if (TextUtils.equals("-1", faceId)) {
            sign.setType(-9);
        } else if (faceId.startsWith("vi")) {
            sign.setType(-1);
        } else {
            sign.setType(0);
        }
        //时间
        sign.setTime(multiTemperBean.getTime());
        //日期
        sign.setDate(dateFormat.format(multiTemperBean.getTime()));
        //上传标识
        sign.setUpload(false);
        long add = DaoManager.get().add(sign);

        Log.e(TAG, "addSignToDB: 当前是第：" + add + " --- " + sign.getTime());

        int comid = SpUtils.getCompany().getComid();
        if (comid == Constants.NOT_BIND_COMPANY_ID) {
            return;
        }
        /*if (sign.getType() == 0) {
            sendSignRecord(sign);
        } else if (sign.getType() == -1) {
            sendVisitRecord(sign);
        }*/
    }

    private void startMultiUploadThread() {
        d("开启批量上传线程");
        threadPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                d("开始批量上传");
                int comid = SpUtils.getCompany().getComid();
                if (comid == Constants.NOT_BIND_COMPANY_ID) {
                    d("公司未绑定，不上传数据");
                    checkDaoData(20 * 1000);
                    return;
                }

                final List<Sign> signs = DaoManager.get().querySignByComIdAndUpload(comid, false);
                if (signs == null || signs.size() <= 0) {
                    d("暂无批量数据");
                    return;
                }

                Map<String, File> hotMap = new HashMap<>();
                Map<String, File> fileMap = new HashMap<>();
                List<WitBean> witBeanList = new ArrayList<>();
                for (int i = 0; i < signs.size(); i++) {
                    Sign sign = signs.get(i);
                    String headPath1 = sign.getHeadPath();
                    String hotImgPath1 = sign.getHotImgPath();
                    if (TextUtils.isEmpty(headPath1) || TextUtils.isEmpty(hotImgPath1)) {
                        d("头像或热图不存在：" + headPath1 + "\n" + hotImgPath1);
                        DaoManager.get().delete(sign);
                        continue;
                    }
                    if (!new File(headPath1).exists() || !new File(hotImgPath1).exists()) {
                        d("头像或热图不存在：" + headPath1 + "\n" + hotImgPath1);
                        DaoManager.get().delete(sign);
                        continue;
                    }

                    WitBean witBean = new WitBean();
                    witBean.createTime = sign.getTime();
                    witBean.temper = sign.getTemperature();
                    witBeanList.add(witBean);

                    //添加头像
                    File file;
                    String headPath = sign.getHeadPath();
                    if (TextUtils.isEmpty(headPath)) {
                        file = createNullFile("", sign.getTime());
                    } else {
                        file = new File(sign.getHeadPath());
                        if (!file.exists()) {
                            d("头像不存在:" + headPath);
                            file = createNullFile("", sign.getTime());
                        }
                    }
                    fileMap.put(file.getName(), file);

                    //添加热图
                    File hotFile;
                    String hotImgPath = sign.getHotImgPath();
                    if (TextUtils.isEmpty(hotImgPath)) {
                        hotFile = createNullFile("hot_", sign.getTime());
                    } else {
                        hotFile = new File(sign.getHotImgPath());
                        if (!hotFile.exists()) {
                            d("热图不存在:" + hotImgPath);
                            hotFile = createNullFile("hot_", sign.getTime());
                        }
                    }
                    hotMap.put(hotFile.getName(), hotFile);
                }
                Map<String, String> params = new HashMap<>();
                String jsonStr = new Gson().toJson(witBeanList);
                params.put("witJson", jsonStr + "");
                params.put("deviceNo", HeartBeatClient.getDeviceNo());
                params.put("comId", SpUtils.getCompany().getComid() + "");

                d("地址：" + ResourceUpdate.UPLOAD_TEMPERETURE_EXCEPTION_ARRAY);
                d("参数：" + params.toString());
                d("头像：" + fileMap.size());
                d("热图：" + hotMap.size());

                OkHttpUtils.post().url(ResourceUpdate.UPLOAD_TEMPERETURE_EXCEPTION_ARRAY)
                        .params(params)
                        .files("heads", fileMap)
                        .files("reHead", hotMap)
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onBefore(Request request, int id) {
                                super.onBefore(request, id);
                                d("onBefore: 开始上传");
                            }

                            @Override
                            public void onError(Call call, Exception e, int id) {
                                d("上送失败--->" + (e != null ? e.getMessage() : "NULL"));

                                checkDaoData(100);
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                d("onResponse: 上传结果：" + response);
                                JSONObject jsonObject = JSONObject.parseObject(response);
                                String status = jsonObject.getString("status");
                                boolean isSucc = TextUtils.equals("1", status);
                                if (!isSucc) {
                                    return;
                                }
                                for (int i = 0; i < signs.size(); i++) {
                                    Sign sign = signs.get(i);
                                    sign.setUpload(true);
                                    DaoManager.get().addOrUpdate(sign);
                                }

                                checkDaoData(100);
                            }

                            @Override
                            public void onAfter(int id) {
                                super.onAfter(id);
                            }
                        });
            }
        }, 1, 5, TimeUnit.MINUTES);
    }

    private void checkDaoData(final int mostNum) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                int comid = SpUtils.getCompany().getComid();
                List<Sign> signs = DaoManager.get().querySignByComId(comid);
                if (signs == null) {
                    return;
                }
                if (signs.size() <= mostNum) {
                    return;
                }
                int deleteNum = 0;
                Iterator<Sign> iterator = signs.iterator();
                while (iterator.hasNext()) {
                    Sign sign = iterator.next();
                    if (sign.isUpload()) {
                        deleteNum += 1;
                        File file = new File(sign.getHeadPath());
                        if (file.exists()) {
                            file.delete();
                        }

                        File hotFile = new File(sign.getHotImgPath());
                        if (hotFile.exists()) {
                            hotFile.delete();
                        }
                        DaoManager.get().deleteSign(sign);
                        iterator.remove();

                        if (signs.size() <= mostNum) {
                            d("run: 数量小于：" + mostNum + "，停止删除");
                            break;
                        }
                    }
                }
                d("run: 删除：" + deleteNum + "条");
            }
        });
    }

    private File createNullFile(String name, long time) {
        File file = new File(Constants.LOCAL_ROOT_PATH, name + time + ".jpg");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    class WitBean {
        long createTime;
        float temper;
    }
}