package com.yunbiao.ybsmartcheckin_live_id.business;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.OutputLog;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateSignDataEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.SignLogTest;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Record5Inch;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;
import okhttp3.Call;
import okhttp3.Request;
import timber.log.Timber;

public class AutoUpload {
    private int INITIAL_TIME = 1;
    private int PERIOD_TIME = 5;
    private static final String TAG = "AutoUpload";
    private ScheduledExecutorService scheduledExecutorService;
    private SimpleDateFormat paramsDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public AutoUpload(){
        scheduledExecutorService = Executors.newScheduledThreadPool(3);
    }

    public AutoUpload(int initialTime,int periodTime){
        INITIAL_TIME = initialTime;
        PERIOD_TIME = periodTime;
        scheduledExecutorService = Executors.newScheduledThreadPool(3);
    }

    public int uploadProgress = -1;
    Map<Integer, Queue<List<Sign>>> uploadMap = new HashMap<>();
    public void startUploadThread(){
        scheduledExecutorService.scheduleAtFixedRate(() ->{
            d("开启自动上传线程");
            //判断上次上传进度
            d("上传进度，uploadProgress = " + uploadProgress);
            if (uploadProgress != -1) {
                return;
            }
            checkAndUploadSignRecord();
        }, INITIAL_TIME, PERIOD_TIME,TimeUnit.MINUTES);
    }

    //检查并上传记录
    public void checkAndUploadSignRecord() {
        uploadProgress = 0;
        int comid = SpUtils.getCompany().getComid();

        if(comid == Constants.NOT_BIND_COMPANY_ID){
            return;
        }

        //查询未上传总数据量
        long unUploadCount = DaoManager.get().querySignByComIdAndUploadCount(comid, false);
        Timber.d("未上传条数 = " + unUploadCount);
        if (unUploadCount == 0) {
            uploadProgress = -1;
            return;
        }

        Queue<List<Sign>> signQueue = new LinkedList<>();
        Queue<List<Sign>> visitorQueue = new LinkedList<>();
        Queue<List<Sign>> temperQueue = new LinkedList<>();

        //查询考勤记录总数 0
        long signCount = DaoManager.get().querySignByComIdUploadTypeCount(comid, false, 0);
        Timber.d("考勤 0 - 未上传条数 = " + signCount);
        if (signCount > 0) {
            //每页条数
            int signPageLength = 30;
            //总页数
            int signPage = (int) (signCount / signPageLength);
            if (signCount % signPageLength != 0) {
                signPage += 1;
            }
            for (int i = 0; i < signPage; i++) {
                List<Sign> signs = DaoManager.get().querySignByComIdUploadTypeWithLimit(comid, false, 0, i, signPageLength);
                signQueue.add(signs);
            }
        }
        Timber.d("考勤 0 - 队列数 = " + signQueue.size());

        //查询访客记录总数 -1
        long visitorCount1 = DaoManager.get().querySignByComIdUploadTypeCount(comid, false, -1);
        Timber.d("访客 -1 - 未上传条数 = " + visitorCount1);
        if (visitorCount1 > 0) {
            //每页条数
            int visitorPageLength1 = 30;
            //总页数
            int visitorPage1 = (int) (visitorCount1 / visitorPageLength1);
            if (visitorCount1 % visitorPageLength1 != 0) {
                visitorPage1 += 1;
            }
            for (int i = 0; i < visitorPage1; i++) {
                List<Sign> visitors1 = DaoManager.get().querySignByComIdUploadTypeWithLimit(comid, false, -1, i, visitorPageLength1);
                visitorQueue.add(visitors1);
            }
        }

        //查询访客记录总数 -2
        long visitorCount2 = DaoManager.get().querySignByComIdUploadTypeCount(comid, false, -2);
        Timber.d("访客 -2 - 未上传条数 = " + visitorCount2);
        if (visitorCount2 > 0) {
            //每页条数
            int visitorPageLength2 = 30;
            //总页数
            int visitorPage2 = (int) (visitorCount2 / visitorPageLength2);
            if (visitorCount2 % visitorPageLength2 != 0) {
                visitorPage2 += 1;
            }
            for (int i = 0; i < visitorPage2; i++) {
                List<Sign> visitors2 = DaoManager.get().querySignByComIdUploadTypeWithLimit(comid, false, -2, i, visitorPageLength2);
                visitorQueue.add(visitors2);
            }
        }
        Timber.d("访客 -1/-2 - 队列数 = " + visitorQueue.size());

        //查询体温记录总数 -9
        long temperCount = DaoManager.get().querySignByComIdUploadTypeCount(comid, false, -9);
        Timber.d("体温 -9 - 未上传条数 = " + temperCount);
        if (temperCount > 0) {
            //每页条数
            int temperPageLength = 30;
            //总页数
            int temperPage = (int) (temperCount / temperPageLength);
            if (temperCount % temperPageLength != 0) {
                temperPage += 1;
            }
            for (int i = 0; i < temperPage; i++) {
                List<Sign> temper = DaoManager.get().querySignByComIdUploadTypeWithLimit(comid, false, -9, i, temperPageLength);
                temperQueue.add(temper);
            }
        }
        Timber.d("体温 -9 - 队列数 = " + temperQueue.size());

        uploadMap.put(1, signQueue);
        uploadMap.put(2, visitorQueue);
        uploadMap.put(3, temperQueue);

        uploadProgress = 1;
        uploadSignRecord();
    }

    private void uploadSignRecord() {
        switch (uploadProgress) {
            case 1:
                uploadSignArray(uploadMap.get(1));
                break;
            case 2:
                uploadTemperArray(null, uploadMap.get(2));
                break;
            case 3:
                uploadVisitorSignArray(uploadMap.get(3));
                break;
            case 4:
                uploadProgress = -1;
                uploadMap.clear();
                EventBus.getDefault().post(new UpdateSignDataEvent());
                break;
        }
    }

    //开始上传记录
//    public void uploadSignRecord(final Consumer<Boolean> callback) {
//        int comid = SpUtils.getCompany().getComid();
//        //只取出30条数据
//        List<Sign> signs = DaoManager.get().querySignByComIdAndUploadLimit(comid, false, 30);
//        if (signs == null || signs.size() <= 0) {
//            try {
//                callback.accept(true);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return;
//        }
//        Timber.d( "run: ------ 本次上传条数：" + signs.size());
//
//        List<Sign> entrySignList = new ArrayList<>();
//        List<Sign> visitorSignList = new ArrayList<>();
//        List<Sign> temperSignList = new ArrayList<>();
//        for (Sign signBean : signs) {
//            // TODO: 2020/3/18 离线功能
//            if (signBean.getComid() == Constants.NOT_BIND_COMPANY_ID) {
//                continue;
//            }
//            if (signBean.getType() == 0) {
//                entrySignList.add(signBean);
//            } else if (signBean.getType() == -9) {
//                temperSignList.add(signBean);
//            } else {
//                visitorSignList.add(signBean);
//            }
//        }
//
//        if(entrySignList.size() <= 0 && visitorSignList.size() <= 0 && temperSignList.size() <= 0){
//            try {
//                callback.accept(true);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return;
//        }
//
//        //开始上传
//        uploadProgress = 0;
//
//        //上传考勤数据
//        uploadSignArray(entrySignList, callback);
//        //上传访客数据
//        uploadVisitorSignArray(visitorSignList, callback);
//        //上传测温记录
//        uploadTemperArray(temperSignList, callback);
//    }

    //上传考勤记录
    private int uploadState1 = 0;
    private void uploadSignArray(Queue<List<Sign>> signQueue) {
        if (signQueue == null || signQueue.size() <= 0) {
            uploadProgress = 2;
            uploadSignRecord();
            return;
        }

        List<Sign> signList = signQueue.poll();
        List<EntrySignBean> signBeans = new ArrayList<>();
        for (Sign sign : signList) {
            signBeans.add(new EntrySignBean(sign.getEmpId(), sign.getTime(), HeartBeatClient.getDeviceNo(), sign.getTemperature(), paramsDateFormat.format(sign.getTime())));
        }
        String jsonStr = new Gson().toJson(signBeans);
        d(uploadProgress + " - 批量上传考勤记录：条数：" + signList.size());
        d("批量上传考勤记录：" + ResourceUpdate.SIGNARRAY);
        d("批量上传考勤记录：参数1：" + jsonStr);
        d("批量上传考勤记录：参数2：" + HeartBeatClient.getDeviceNo());
        uploadState1 = 0;
        OkHttpUtils.post()
                .url(ResourceUpdate.SIGNARRAY)
                .addParams("signstr", jsonStr)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        d("批量上传考勤记录：上送失败--->" + (e != null ? e.getMessage() : "NULL"));
                        e.printStackTrace();

                        SignLogTest.getInstance().addArrayContent(signList,(e != null ? e.getMessage() : "NULL"));
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Timber.d( "批量上传考勤记录：onResponse: 上传结果：" + response);
                        JSONObject jsonObject = JSONObject.parseObject(response);
                        String status = jsonObject.getString("status");
                        boolean isSucc = TextUtils.equals("1", status);

                        if (!isSucc) {
                            return;
                        }
                        uploadState1 = 1;

                        for (Sign sign : signList) {
                            if(sign.getTemperature() == 0){
                                sign.setUpload(true);
                                DaoManager.get().addOrUpdate(sign);
                            }
                        }

                        SignLogTest.getInstance().addArrayContent(signList,response);

                        uploadTemperArray(signList, signQueue);
                    }

                    @Override
                    public void onAfter(int id) {
                        super.onAfter(id);
                        if (uploadState1 != 1) {
                            uploadSignArray(signQueue);
                        }
                    }
                });
    }

    //上传测温记录
    private void uploadTemperArray(final List<Sign> signs, Queue<List<Sign>> signQueue) {
        if(Constants.DEVICE_TYPE == Constants.DeviceType.CHECK_IN) {
            if (uploadProgress == 1) {
                uploadSignArray(signQueue);
            } else if (uploadProgress == 2) {
                uploadProgress = 3;
                uploadSignRecord();
            } else if (uploadProgress == 3) {
                uploadVisitorSignArray(signQueue);
            }
            return;
        }
        List<Sign> signList;
        if (uploadProgress == 1) {
            signList = signs;
        } else if (uploadProgress == 2) {
            if (signQueue == null || signQueue.size() <= 0) {
                uploadProgress = 3;
                uploadSignRecord();
                return;
            } else {
                signList = signQueue.poll();
            }
        } else {
            signList = signs;
        }

        Map<String, File> headMap = new LinkedHashMap<>();
        Map<String, File> hotMap = new LinkedHashMap<>();
        List<TemperSignBean> temperSignBeans = new ArrayList<>();
        for (int i = 0; i < signList.size(); i++) {
            Sign sign = signList.get(i);
            long time = sign.getTime();
            temperSignBeans.add(new TemperSignBean(time, sign.getTemperature(), sign.getEmpId(), paramsDateFormat.format(sign.getTime())));

            //存头像
            File headFile = getFileByPath(sign.getHeadPath());
            headMap.put(time + ".jpg", headFile);

            //存热图
            File hotFile = getFileByPath(sign.getHotImgPath());
            hotMap.put(time + "_hot.jpg", hotFile);
        }
        String json = new Gson().toJson(temperSignBeans);

        String format = paramsDateFormat.format(new Date());
        OutputLog.getInstance().addMultipleLog(format + " ----- " + json);

        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, File> stringFileEntry : headMap.entrySet()) {
            sb.append("Key: ").append(stringFileEntry.getKey()).append(" --- Value: ").append(stringFileEntry.getValue().length());
        }
        OutputLog.getInstance().addMultipleLog("头像 ----- " + sb.toString());
        sb.setLength(0);

        for (Map.Entry<String, File> stringFileEntry : hotMap.entrySet()) {
            sb.append("Key: ").append(stringFileEntry.getKey()).append(" --- Value: ").append(stringFileEntry.getValue().length());
        }
        OutputLog.getInstance().addMultipleLog("热成像 ----- " + sb.toString());

        d(uploadProgress + " - 批量上传测温记录：条数：" + signList.size());
        d("批量上传测温记录：" + ResourceUpdate.UPLOAD_TEMPERETURE_EXCEPTION_ARRAY_NOW);
        d("批量上传测温记录:参数：" + json);
        d("批量上传测温记录:头像文件：" + headMap.toString());
        d("批量上传测温记录:热图文件：" + hotMap.toString());

        int comid = SpUtils.getCompany().getComid();
        OkHttpUtils.post()
                .url(ResourceUpdate.UPLOAD_TEMPERETURE_EXCEPTION_ARRAY_NOW)
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
                        Timber.d( "批量上传测温记录:onResponse: 上传结果：" + response);
                        if (TextUtils.isEmpty(response)) {
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
                        if (uploadProgress == 1) {
                            uploadSignArray(signQueue);
                        } else if (uploadProgress == 2) {
                            uploadTemperArray(null, signQueue);
                        } else if (uploadProgress == 3) {
                            uploadVisitorSignArray(signQueue);
                        }
                    }
                });
    }

    //上传访客记录
    private int uploadState2 = 0;
    private void uploadVisitorSignArray(Queue<List<Sign>> signQueue) {
        if (signQueue == null || signQueue.size() <= 0) {
            uploadProgress = 4;
            uploadSignRecord();
            return;
        }

        List<Sign> signList = signQueue.poll();
        List<VisitorSignBean> visitorSignBeans = new ArrayList<>();
        for (Sign sign : signList) {
            visitorSignBeans.add(new VisitorSignBean(sign.getEmpId(), sign.getTime(), paramsDateFormat.format(sign.getTime())));
        }
        String json = new Gson().toJson(visitorSignBeans);

        Map<String, File> headMap = new HashMap<>();
        for (Sign sign : signList) {
            File fileByPath = getFileByPath(sign.getHeadPath());
            headMap.put(fileByPath.getName(), fileByPath);
        }
        d(uploadProgress + " - 批量上传访客记录：条数：" + signList.size());
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
        uploadState2 = 0;
        OkHttpUtils.post()
                .url(ResourceUpdate.VISITARRAY)
                .addParams("witJson", json)
                .addParams("deviceNo", HeartBeatClient.getDeviceNo())
                .addParams("comId", comid + "")
                .files("heads", headMap)
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
                        Timber.d( "批量上传访客记录：onResponse: 上传结果：" + response);
                        JSONObject jsonObject = JSONObject.parseObject(response);
                        String status = jsonObject.getString("status");
                        boolean isSucc = TextUtils.equals("1", status);
                        if (!isSucc) {
                            return;
                        }
                        uploadState2 = 1;
                        for (Sign sign : signList) {
                            sign.setUpload(true);
                            DaoManager.get().addOrUpdate(sign);
                        }
                        uploadTemperArray(signList, signQueue);
                    }

                    @Override
                    public void onAfter(int id) {
                        super.onAfter(id);
                        if (uploadState2 != 1) {
                            uploadVisitorSignArray(signQueue);
                        }
                    }
                });
    }

    /**
     * 开启大通量上传线程
     */
    public void startMultiUploadThread() {
        d("开启批量上传线程");
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                boolean isPrivacy = SpUtils.getBoolean(Constants.Key.PRIVACY_MODE, Constants.Default.PRIVACY_MODE);
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

                Map<String, File> hotMap = new LinkedHashMap<>();
                Map<String, File> fileMap = new LinkedHashMap<>();
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
                    if (!TextUtils.isEmpty(headPath)) {
                        file = new File(sign.getHeadPath());
                    } else if (isPrivacy) {
                        file = createNullFile("", sign.getTime());
                    } else {
                        file = createNullFile("", sign.getTime());
                    }
                    fileMap.put(sign.getTime() + ".jpg", file);

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
                    hotMap.put(sign.getTime() + "_hot.jpg", hotFile);
                }
                Map<String, String> params = new HashMap<>();
                String jsonStr = new Gson().toJson(witBeanList);
                params.put("witJson", jsonStr + "");
                params.put("deviceNo", HeartBeatClient.getDeviceNo());
                params.put("comId", SpUtils.getCompany().getComid() + "");

                d("地址：" + ResourceUpdate.UPLOAD_TEMPERETURE_EXCEPTION_ARRAY_NOW);
                d("参数：" + params.toString());
                d("头像：" + fileMap.size());
                d("热图：" + hotMap.size());

                OkHttpUtils.post().url(ResourceUpdate.UPLOAD_TEMPERETURE_EXCEPTION_ARRAY_NOW)
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
        }, INITIAL_TIME, PERIOD_TIME, TimeUnit.MINUTES);
    }

    /**
     * 开启5寸上传线程
     */
    public void start5InchUploadThread() {
        d("开启批量上传线程");
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                boolean isPrivacy = SpUtils.getBoolean(Constants.Key.PRIVACY_MODE, Constants.Default.PRIVACY_MODE);
                d("开始批量上传");
                int comid = SpUtils.getCompany().getComid();
                if (comid == Constants.NOT_BIND_COMPANY_ID) {
                    d("公司未绑定，不上传数据");
                    check5InchDaoData(20 * 1000);
                    return;
                }

                final List<Record5Inch> recordList = DaoManager.get().queryRecord5InchByComIdAndUpload(comid, false);
                if (recordList == null || recordList.size() <= 0) {
                    d("暂无批量数据");
                    return;
                }

                Map<String, File> fileMap = new HashMap<>();
                List<WitBean> witBeanList = new ArrayList<>();
                for (int i = 0; i < recordList.size(); i++) {
                    Record5Inch record = recordList.get(i);
                    String headPath1 = record.getImgPath();
                    if (TextUtils.isEmpty(headPath1)) {
                        d("图片不存在：" + headPath1);
                        DaoManager.get().delete(record);
                        continue;
                    }
                    if (!new File(headPath1).exists()) {
                        d("图片不存在：" + headPath1);
                        DaoManager.get().delete(record);
                        continue;
                    }

                    WitBean witBean = new WitBean();
                    witBean.createTime = record.getTime();
                    witBean.temper = record.getTemperature();
                    witBeanList.add(witBean);

                    //添加头像
                    File file;
                    String headPath = record.getImgPath();
                    if (!TextUtils.isEmpty(headPath)) {
                        file = new File(record.getImgPath());
                    } else if (isPrivacy) {
                        file = createNullFile("", record.getTime());
                    } else {
                        file = createNullFile("", record.getTime());
                    }
                    fileMap.put(file.getName(), file);
                }
                Map<String, String> params = new HashMap<>();
                String jsonStr = new Gson().toJson(witBeanList);
                params.put("witJson", jsonStr + "");
                params.put("deviceNo", HeartBeatClient.getDeviceNo());
                params.put("comId", SpUtils.getCompany().getComid() + "");

                d("地址：" + ResourceUpdate.UPLOAD_TEMPERETURE_EXCEPTION_ARRAY_HEAD);
                d("参数：" + params.toString());
                d("图片：" + fileMap.size());

                OkHttpUtils.post().url(ResourceUpdate.UPLOAD_TEMPERETURE_EXCEPTION_ARRAY_HEAD)
                        .params(params)
                        .files("heads", fileMap)
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

                                check5InchDaoData(100);
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
                                for (int i = 0; i < recordList.size(); i++) {
                                    Record5Inch record = recordList.get(i);
                                    record.setUpload(true);
                                    DaoManager.get().addOrUpdate(record);
                                }

                                check5InchDaoData(100);
                            }

                            @Override
                            public void onAfter(int id) {
                                super.onAfter(id);
                            }
                        });
            }
        }, INITIAL_TIME, PERIOD_TIME, TimeUnit.MINUTES);
    }

    // 检查数据，已上传的就删除
    private void checkDaoData(final int mostNum) {
        scheduledExecutorService.execute(() -> {
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
        });
    }

    // 检查数据，已上传的就删除（5寸）
    private void check5InchDaoData(final int mostNum) {
        scheduledExecutorService.execute(() -> {
            int comid = SpUtils.getCompany().getComid();
            List<Record5Inch> recordList = DaoManager.get().queryRecord5InchByComId(comid);
            if (recordList == null) {
                return;
            }
            if (recordList.size() <= mostNum) {
                return;
            }
            int deleteNum = 0;
            Iterator<Record5Inch> iterator = recordList.iterator();
            while (iterator.hasNext()) {
                Record5Inch record = iterator.next();
                if (record.isUpload()) {
                    deleteNum += 1;
                    File file = new File(record.getImgPath());
                    if (file.exists()) {
                        file.delete();
                    }
                    DaoManager.get().delete5InchRecord(record);
                    iterator.remove();

                    if (recordList.size() <= mostNum) {
                        d("run: 数量小于：" + mostNum + "，停止删除");
                        break;
                    }
                }
            }
            d("run: 删除：" + deleteNum + "条");
        });
    }

    static class WitBean {
        long createTime;
        float temper;
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

    private void d(String s){
        Timber.d( s);
    }

    private File getFileByPath(String headPath) {
        File headFile;
        if (TextUtils.isEmpty(headPath)) {
            headFile = new File(Environment.getExternalStorageDirectory(), "1.txt");
            if (!headFile.exists()) {
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


    static class VisitorSignBean {
        long visitorId;
        long createTime;
        String signTimeFormat;

        VisitorSignBean(long visitorId, long createTime, String signTimeFormat) {
            this.visitorId = visitorId;
            this.createTime = createTime;
            this.signTimeFormat = signTimeFormat;
        }
    }

    static class TemperSignBean {
        long createTime;
        float temper;
        long entryId;
        String signTimeFormat;

        TemperSignBean(long createTime, float temper, long entryId, String signTimeFormat) {
            this.createTime = createTime;
            this.temper = temper;
            this.entryId = entryId;
            this.signTimeFormat = signTimeFormat;
        }
    }


    static class EntrySignBean {
        long entryid;
        long signTime;
        String deviceId;
        float temper;
        String signTimeFormat;

        EntrySignBean(long entryid, long signTime, String deviceId, float temper, String signTimeFormat) {
            this.entryid = entryid;
            this.signTime = signTime;
            this.deviceId = deviceId;
            this.temper = temper;
            this.signTimeFormat = signTimeFormat;
        }
    }

}
