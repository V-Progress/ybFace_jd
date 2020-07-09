package com.yunbiao.ybsmartcheckin_live_id.business;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateSignDataEvent;
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

    public void startUploadThread(){
        scheduledExecutorService.scheduleAtFixedRate(() ->{
            d("开启自动上传线程");
            uploadSignRecord(aBoolean -> {
                if (aBoolean) {
                    EventBus.getDefault().post(new UpdateSignDataEvent());
                }
            });
        }, INITIAL_TIME, PERIOD_TIME,TimeUnit.MINUTES);
    }

    //开始上传记录
    public void uploadSignRecord(final Consumer<Boolean> callback) {
        int comid = SpUtils.getCompany().getComid();
        List<Sign> signs = DaoManager.get().querySignByComIdAndUpload(comid, false);
        if (signs == null || signs.size() <= 0) {
            try {
                callback.accept(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        Log.e(TAG, "run: ------ 未上传记录：" + signs.size());

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

        if(entrySignList.size() <= 0 && visitorSignList.size() <= 0 && temperSignList.size() <= 0){
            try {
                callback.accept(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        //上传考勤数据
        uploadSignArray(entrySignList, callback);
        //上传访客数据
        uploadVisitorSignArray(visitorSignList, callback);
        //上传测温记录
        uploadTemperArray(temperSignList, callback);
    }
    //上传考勤记录
    private void uploadSignArray(final List<Sign> signList, final Consumer<Boolean> callback) {
        if (signList == null || signList.size() <= 0) {
            return;
        }

        List<EntrySignBean> signBeans = new ArrayList<>();
        for (Sign sign : signList) {
            signBeans.add(new EntrySignBean(sign.getEmpId(), sign.getTime(), HeartBeatClient.getDeviceNo(),sign.getTemperature(),paramsDateFormat.format(sign.getTime())));
        }
        String jsonStr = new Gson().toJson(signBeans);
        d("批量上传考勤记录：" + ResourceUpdate.SIGNARRAY);
        d("批量上传考勤记录：参数1：" + jsonStr);
        d("批量上传考勤记录：参数2：" + HeartBeatClient.getDeviceNo());
        OkHttpUtils.post()
                .url(ResourceUpdate.SIGNARRAY)
                .addParams("signstr", jsonStr)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        d("批量上传考勤记录：上送失败--->" + (e != null ? e.getMessage() : "NULL"));
                        e.printStackTrace();
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

                        uploadTemperArray(signList, callback);
                    }

                    @Override
                    public void onAfter(int id) {
                        super.onAfter(id);
                        if (callback != null) {
                            try {
                                callback.accept(true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    //上传测温记录
    private void uploadTemperArray(final List<Sign> signList, final Consumer<Boolean> callback) {
        if (signList == null || signList.size() <= 0) {
            return;
        }
        List<TemperSignBean> temperSignBeans = new ArrayList<>();
        for (Sign sign : signList) {
            temperSignBeans.add(new TemperSignBean(sign.getTime(), sign.getTemperature(),sign.getEmpId(),paramsDateFormat.format(sign.getTime())));
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
                        if (callback != null) {
                            try {
                                callback.accept(true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    //上传访客记录
    private void uploadVisitorSignArray(final List<Sign> signList, final Consumer<Boolean> callback) {
        if (signList == null || signList.size() <= 0) {
            return;
        }
        List<VisitorSignBean> visitorSignBeans = new ArrayList<>();
        for (Sign sign : signList) {
            visitorSignBeans.add(new VisitorSignBean(sign.getEmpId(), sign.getTime(),paramsDateFormat.format(sign.getTime())));
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
                        uploadTemperArray(signList, callback);
                    }

                    @Override
                    public void onAfter(int id) {
                        super.onAfter(id);
                        if (callback != null) {
                            try {
                                callback.accept(true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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
                    if (!TextUtils.isEmpty(headPath)) {
                        file = new File(sign.getHeadPath());
                    } else if (isPrivacy) {
                        file = createNullFile("", sign.getTime());
                    } else {
                        file = createNullFile("", sign.getTime());
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

    class WitBean {
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
        Log.e(TAG, s);
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


    class VisitorSignBean {
        long visitorId;
        long createTime;
        String signTimeFormat;

        public VisitorSignBean(long visitorId, long createTime,String signTimeFormat) {
            this.visitorId = visitorId;
            this.createTime = createTime;
            this.signTimeFormat = signTimeFormat;
        }
    }

    class TemperSignBean {
        long createTime;
        float temper;
        long entryId;
        String signTimeFormat;

        public TemperSignBean(long createTime, float temper,long entryId,String signTimeFormat) {
            this.createTime = createTime;
            this.temper = temper;
            this.entryId = entryId;
            this.signTimeFormat = signTimeFormat;
        }
    }


    class EntrySignBean {
        long entryid;
        long signTime;
        String deviceId;
        float temper;
        String signTimeFormat;

        public EntrySignBean(long entryid, long signTime, String deviceId, float temper,String signTimeFormat) {
            this.entryid = entryid;
            this.signTime = signTime;
            this.deviceId = deviceId;
            this.temper = temper;
            this.signTimeFormat = signTimeFormat;
        }
    }

}
