package com.yunbiao.ybsmartcheckin_live_id.business;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.yunbiao.faceview.CompareResult;
import com.yunbiao.ybsmartcheckin_live_id.OutputLog;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.SignLogTest;
import com.yunbiao.ybsmartcheckin_live_id.activity_temper_multiple.MultiTemperBean;
import com.yunbiao.ybsmartcheckin_live_id.db2.Record5Inch;
import com.yunbiao.ybsmartcheckin_live_id.db2.VertifyRecord;
import com.yunbiao.ybsmartcheckin_live_id.utils.IdCardMsg;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.db2.User;
import com.yunbiao.ybsmartcheckin_live_id.db2.Visitor;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.utils.SdCardUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.StringCallback;

import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import io.reactivex.functions.Consumer;
import okhttp3.Call;
import timber.log.Timber;

/**
 * Created by Administrator on 2019/3/18.
 */

public class SignManager {
    private final String TAG = getClass().getSimpleName();
    private static SignManager instance;
    private String today;
    private long verifyOffsetTime = 0;//验证间隔时间

    private DateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
    private DateFormat vertifySdf = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat visitSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private SimpleDateFormat paramsDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private boolean isDebug = true;
    private boolean isBulu = false;

    private Map<String, Long> passageMap = new HashMap<>();
    private AutoUpload autoUpload = null;

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

        if (autoUpload == null) {
            autoUpload = new AutoUpload();
        }

        if (Constants.DEVICE_TYPE == Constants.DeviceType.MULTIPLE_THERMAL
                || Constants.DEVICE_TYPE == Constants.DeviceType.HT_MULTIPLE_THERMAL
                || Constants.DEVICE_TYPE == Constants.DeviceType.SAFETY_CHECK_DOUBLE_LIGHT
                || Constants.DEVICE_TYPE == Constants.DeviceType.HT_SAFETY_CHECK_DOUBLE_LIGHT) {
            autoUpload.startMultiUploadThread();
        } else if (Constants.DEVICE_TYPE == Constants.DeviceType.CHECK_IN
                || Constants.DEVICE_TYPE == Constants.DeviceType.TEMPERATURE_CHECK_IN
                || Constants.DEVICE_TYPE == Constants.DeviceType.HT_TEMPERATURE_CHECK_IN) {
            autoUpload.startUploadThread();
        }

        AutoClean autoClean = new AutoClean();
        if (Constants.DEVICE_TYPE == Constants.DeviceType.TEMPERATURE_MEASUREMENT_5_INCH) {
            autoUpload.start5InchUploadThread();
            autoClean.startAutoClear5InchRecord();
        } else {
            autoClean.startAutoClear();
        }
    }

    public void uploadSignRecord(Consumer<Boolean> consumer) {
        if (autoUpload != null) {
            autoUpload.uploadSignRecord(consumer);
        } else {
            try {
                consumer.accept(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    /***
     * 普通考勤打卡
     * @param compareResult
     * @param temperature
     * @param isUpload
     * @return
     */
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

                    Timber.d( "访问开始时间：" + currStart);
                    Timber.d( "访问结束时间：" + currEnd);

                    try {
                        Date start = visitSdf.parse(currStart);
                        Date end = visitSdf.parse(currEnd);
                        //在开始时间之前或者在结束时间之后
                        if (currDate.before(start) || currDate.after(end)) {
                            Timber.d( "不在访问期内");
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

                SignLogTest.getInstance().addFaceContent(sign.getTime(),sign.getEmpId() + " --> " + sign.getName());

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
        boolean isPrivacy = SpUtils.getBoolean(Constants.Key.PRIVACY_MODE, Constants.Default.PRIVACY_MODE);
        return checkSignData(compareResult, temperature, !isPrivacy);
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

    /***
     * 双光提交并删除
     * @param sign
     */
    public void uploadTemperatureSignAndDelete(Sign sign) {
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
        Timber.d( "uploadTemperatureSign: 保存头像：" + file.getPath());

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
        Timber.d( "uploadTemperatureSign: 保存热图：" + hotFile.getPath());
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
        Timber.d( "上传温度");
        Timber.d( "地址：" + url);
        Timber.d( "参数: " + params.toString());
        PostFormBuilder builder = OkHttpUtils.post()
                .url(url)
                .params(params);
        builder.addFile("heads", file.getName(), file);
        builder.addFile("reHead", hotFile.getName(), hotFile);

        builder.build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Timber.d( "onError: 上传失败：" + (e == null ? "NULL" : e.getMessage()));
                sign.setUpload(false);
                DaoManager.get().addOrUpdate(sign);
            }

            @Override
            public void onResponse(String response, int id) {
                Timber.d( "onResponse: 上传成功：" + response);
                JSONObject jsonObject = JSONObject.parseObject(response);
                String status = jsonObject.getString("status");
                boolean isSucc = TextUtils.equals("1", status);
                if (isSucc) {
                    if (file != null && file.exists()) {
                        boolean delete = file.delete();
                        Timber.d( "onResponse: 头像删除：" + file.getPath() + " ----- " + delete);
                    }
                    if (hotFile != null && hotFile.exists()) {
                        boolean delete = hotFile.delete();
                        Timber.d( "onResponse: 热图删除：" + hotFile.getPath() + " ----- " + delete);
                    }
                }
            }

            @Override
            public void onAfter(int id) {
            }
        });
    }

    private File createEmptyFile() {
        File file = new File(Constants.LOCAL_ROOT_PATH + File.separator + "0.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /***
     * 上传测温记录
     * @param headBitmap
     * @param hotBitmap
     * @param sign
     * @param isPrivacy
     * @param signConsumer
     */
    public void uploadTemperatureSign(Bitmap headBitmap, Bitmap hotBitmap, final Sign sign, boolean isPrivacy, Consumer<Sign> signConsumer) {
        long start = System.currentTimeMillis();
        String url = ResourceUpdate.UPLOAD_TEMPERETURE_EXCEPTION;

        File file = !isPrivacy && headBitmap != null ? saveBitmap(sign.getTime(), headBitmap) : createEmptyFile();
        sign.setHeadPath(file.getPath());
        if (headBitmap != null && !headBitmap.isRecycled()) {
            headBitmap.recycle();
        }

        File hotFile = !isPrivacy && hotBitmap != null ? saveBitmap("hot_", sign.getTime(), hotBitmap) : createEmptyFile();
        sign.setHotImgPath(hotFile.getPath());
        if (hotBitmap != null && !hotBitmap.isRecycled()) {
            hotBitmap.recycle();
        }

        Timber.d( "uploadTemperatureSign: 存DB耗时:" + "(" + (System.currentTimeMillis() - start) + ") 毫秒");
        if (signConsumer != null) {
            try {
                signConsumer.accept(sign);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        sign.setUpload(false);
        if (!isPrivacy) {
            DaoManager.get().addOrUpdate(sign);
            checkStorageSpace();
        } else {
            return;
        }

        // TODO: 2020/3/18 离线功能
        if (sign.getComid() == Constants.NOT_BIND_COMPANY_ID) {
            return;
        }

        final long time = sign.getTime();

        Map<String, String> params = new HashMap<>();
        params.put("deviceNo", HeartBeatClient.getDeviceNo());
        params.put("comId", SpUtils.getCompany().getComid() + "");
        params.put("temper", sign.getTemperature() + "");
        params.put("signTime", sign.getTime() + "");
        params.put("signTimeFormat", paramsDateFormat.format(sign.getTime()));
        if (sign.getType() != -9) {
            params.put("entryId", sign.getEmpId() + "");
        }
        Timber.d( "上传温度");
        Timber.d( "地址：" + url);
        Timber.d( "参数: " + params.toString());
        PostFormBuilder builder = OkHttpUtils.post()
                .url(url)
                .params(params);
        //如果不为隐私模式并且图片Bitmap不为null，则存照片
        builder.addFile("heads", file.getName(), file);
        builder.addFile("reHead", hotFile.getName(), hotFile);

        OutputLog.getInstance().addLog(sign.getTemperature() + " ----- " + params.toString());

        builder.build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Timber.d( "onError: 上传失败：" + (e == null ? "NULL" : e.getMessage()));
                sign.setUpload(false);
            }

            @Override
            public void onResponse(String response, int id) {
                Timber.d( "onResponse: 上传结果：" + response);

                JSONObject jsonObject = JSONObject.parseObject(response);
                if (jsonObject.getInteger("status") == 1) {
                    Sign sign = DaoManager.get().querySignByTime(time);
                    if (sign != null) {
                        sign.setUpload(true);
                        DaoManager.get().addOrUpdate(sign);
                    }
                } else {
                    Timber.d( "onResponse: 上传失败");
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
        Timber.d( "通知被访人：" + url);
        Timber.d( "地址：" + url);
        Timber.d( "参数：" + params.toString());

        OkHttpUtils.post()
                .url(url)// TODO: 2019/12/7
                .params(params)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Timber.d( "通知被访人：onError: " + (e == null ? "NULL" : e.getMessage()));
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Timber.d( "通知被访人：onResponse: " + response);
                    }
                });
    }

    /***
     * 上传访客记录
     * @param signBean
     */
    private void sendVisitRecord(final Sign signBean) {
        Map<String, String> map = new HashMap<>();
        map.put("deviceId", HeartBeatClient.getDeviceNo());
        map.put("comId", "" + signBean.getComid());
        map.put("visitorId", signBean.getEmpId() + "");
        map.put("signTime", signBean.getTime() + "");
        map.put("signTimeFormat", paramsDateFormat.format(signBean.getTime()));
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

    /***
     * 上传考勤记录
     * @param signBean
     */
    private void sendSignRecord(final Sign signBean) {
        final Map<String, String> map = new HashMap<>();
        map.put("entryid", signBean.getEmpId() + "");
        map.put("signTime", signBean.getTime() + "");
        map.put("signTimeFormat", paramsDateFormat.format(signBean.getTime()));
        map.put("deviceId", HeartBeatClient.getDeviceNo());
        map.put("temper", signBean.getTemperature() + "");
        d("上传考勤记录");
        d("地址：" + ResourceUpdate.SIGNLOG);
        d("参数：" + map.toString());

        OutputLog.getInstance().addLog(signBean.getTemperature() + " ----- " + signBean.getName() + " ----- " + map.toString());

        long l = DaoManager.get().addOrUpdate(signBean);
        SignLogTest.getInstance().addSaveContent(signBean.getTime(),signBean.getEmpId() + " --> " + signBean.getName() + " --> " + l);

        checkStorageSpace();
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
                SignLogTest.getInstance().addUploadContent(time, sign.getEmpId() + " --> " + sign.getName() + " --> " + response);
            }

            @Override
            public void onAfter(int id) {
            }
        });
    }

    /***
     * 根据时间判断是否可以打卡
     * @param signBean
     * @return
     */
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
        Timber.d( "uploadNoIdCardResult: 地址：" + url);

        Map<String, String> params = new HashMap<>();
        params.put("isPass", isPass + "");
        params.put("deviceNo", HeartBeatClient.getDeviceNo());
        params.put("temper", max + "");
        Timber.d( "uploadNoIdCardResult: params：" + params.toString());

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
        Timber.d( "uploadNoIdCardResult: 头像：" + file.getPath());

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
        Timber.d( "uploadNoIdCardResult: 热量图：" + reFile.getPath());

        builder.build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Timber.d( "onError: " + (e == null ? "NULL" : e.getMessage()));
            }

            @Override
            public void onResponse(String response, int id) {
                Timber.d( "onResponse: " + response);
            }
        });


    }

    /***
     * 上传二维码认证记录
     * @param entryId
     * @param isPass
     * @param newHead
     * @param temper
     * @param reHead
     */
    public void uploadCodeVerifyResult(String entryId, boolean isPass, Bitmap newHead, float temper, Bitmap reHead) {
        Map<String, String> params = new HashMap();
        params.put("entryId", entryId);
        params.put("deviceNo", HeartBeatClient.getDeviceNo());
        params.put("isPass", (isPass ? 0 : 1) + "");
        params.put("temper", temper + "");
        Timber.d( "uploadCodeVerifyResult: 参数：" + params.toString());

        PostFormBuilder builder = OkHttpUtils.post().url(ResourceUpdate.UPLOAD_CODE_VERIFY_RESULT).params(params);

        File file = saveBitmap(System.currentTimeMillis(), newHead);
        builder.addFile("newHeads", file.getName(), file);
        Timber.d( "uploadCodeVerifyResult: 截图：" + file.exists() + " --- " + file.getPath());

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
        Timber.d( "uploadCodeVerifyResult: 热量图：" + reFile.exists() + " --- " + reFile.getPath());
        builder.addFile("reHead", reFile.getName(), reFile);

        builder.build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Timber.d( "onError: 上传失败：" + (e == null ? "NULL" : e.getMessage()));
            }

            @Override
            public void onResponse(String response, int id) {
                Timber.d( "onResponse: 上传结果：" + response);
            }
        });
    }

    public VertifyRecord getICCardVerifyRecord(Bitmap faceBitmap, Bitmap reBitmap, Bitmap idCardBitmap, float temper, IdCardMsg msg, int similar, int isPass) {
        long time = System.currentTimeMillis();
        File idCardFile = idCardBitmap != null ? saveBitmap("idCard_", time, idCardBitmap) : createEmptyFile();
        File faceFile = faceBitmap != null ? saveBitmap(time, faceBitmap) : createEmptyFile();
        File reFile = reBitmap != null ? saveBitmap("re_", time, reBitmap) : createEmptyFile();

        VertifyRecord vertifyRecord = new VertifyRecord();
        vertifyRecord.setIdCardHeadPath(idCardFile.getPath());
        vertifyRecord.setPersonHeadPath(faceFile.getPath());
        vertifyRecord.setHotImagePath(reFile.getPath());
        vertifyRecord.setSimilar(similar + "");
        vertifyRecord.setName(msg.name.trim());
        vertifyRecord.setSex(TextUtils.equals(msg.sex, "男") ? "1" : "0");
        vertifyRecord.setNation(msg.nation_str);
        vertifyRecord.setBirthDate(msg.birth_year + "-" + msg.birth_month + "-" + msg.birth_day);
        vertifyRecord.setIdNum(msg.id_num);
        vertifyRecord.setAddress(msg.address);
        vertifyRecord.setTermDate(msg.useful_e_date_year + "-" + msg.useful_e_date_month + "-" + msg.useful_e_date_day);
        vertifyRecord.setIsPass(isPass + "");
        vertifyRecord.setComId(SpUtils.getCompany().getComid() + "");
        vertifyRecord.setTemper(temper + "");
        vertifyRecord.setTime(time);
        vertifyRecord.setDate(vertifySdf.format(new Date(time)));
        vertifyRecord.setUpload(false);
        return vertifyRecord;
    }

    /**
     * 加入验证记录到数据库
     *
     * @param faceBitmap
     * @param reBitmap
     * @param idCardBitmap
     * @param temper
     * @param msg
     * @param similar
     * @param isPass
     */
    public void addICCardVerifyRecordToDB(Bitmap faceBitmap, Bitmap reBitmap, Bitmap idCardBitmap, float temper, IdCardMsg msg, int similar, int isPass) {
        VertifyRecord verifyRecord = getICCardVerifyRecord(faceBitmap, reBitmap, idCardBitmap, temper, msg, similar, isPass);
        long l = DaoManager.get().addOrUpdate(verifyRecord);
    }

    public VertifyRecord getIDCardVerifyRecord(float temper, IdCardMsg msg, int similar, int isPass, Bitmap idCardBitmap, Bitmap faceBitmap, Bitmap reBitmap) {
        long time = System.currentTimeMillis();
        File idCardFile = idCardBitmap != null ? saveBitmap("idCard_", time, idCardBitmap) : createEmptyFile();
        File faceFile = faceBitmap != null ? saveBitmap(time, faceBitmap) : createEmptyFile();
        File reFile = reBitmap != null ? saveBitmap("re_", time, reBitmap) : createEmptyFile();
        VertifyRecord vertifyRecord = new VertifyRecord();
        vertifyRecord.setIdCardHeadPath(idCardFile.getPath());
        vertifyRecord.setPersonHeadPath(faceFile.getPath());
        vertifyRecord.setHotImagePath(reFile.getPath());
        vertifyRecord.setSimilar(similar + "");
        vertifyRecord.setName(msg.name.trim());
        vertifyRecord.setSex(TextUtils.equals(msg.sex, "男") ? "1" : "0");
        vertifyRecord.setNation(msg.nation_str);
        vertifyRecord.setBirthDate(msg.birth_year + "-" + msg.birth_month + "-" + msg.birth_day);
        vertifyRecord.setIdNum(msg.id_num);
        vertifyRecord.setAddress(msg.address);
        vertifyRecord.setTermDate(msg.useful_e_date_year + "-" + msg.useful_e_date_month + "-" + msg.useful_e_date_day);
        vertifyRecord.setIsPass(isPass + "");
        vertifyRecord.setComId(SpUtils.getCompany().getComid() + "");
        vertifyRecord.setTemper(temper + "");
        vertifyRecord.setTime(time);
        vertifyRecord.setDate(vertifySdf.format(new Date(time)));
        vertifyRecord.setUpload(false);
        return vertifyRecord;
    }

    public void uploadIdCardAndReImage(VertifyRecord vertifyRecord) {
        long l = DaoManager.get().addOrUpdate(vertifyRecord);
        vertifyRecord.set_id(l);

        File idCardFile = new File(vertifyRecord.getIdCardHeadPath());
        File faceFile = new File(vertifyRecord.getPersonHeadPath());
        File reFile = new File(vertifyRecord.getHotImagePath());

        Map<String, String> params = new HashMap();
        params.put("similar", vertifyRecord.getSimilar());
        params.put("name", vertifyRecord.getName());
        params.put("sex", vertifyRecord.getSex());
        params.put("nation", vertifyRecord.getNation());
        params.put("birthDate", vertifyRecord.getBirthDate());
        params.put("IdCard", vertifyRecord.getIdNum());
        params.put("address", vertifyRecord.getAddress());
        params.put("termDate", vertifyRecord.getTermDate());
        params.put("isPass", vertifyRecord.getIsPass());
        params.put("deviceNo", HeartBeatClient.getDeviceNo());
        params.put("comId", vertifyRecord.getComId());
        params.put("temper", vertifyRecord.getTemper());
        params.put("signTime", vertifyRecord.getTime() + "");
        params.put("signTimeFormat", paramsDateFormat.format(vertifyRecord.getTime()));

        String phoneNumber = vertifyRecord.getPhoneNumber();
        if (!TextUtils.isEmpty(phoneNumber)) {
            params.put("phone", vertifyRecord.getPhoneNumber());
        }
        String uploadIdcard = ResourceUpdate.UPLOAD_IDCARD;
        Timber.d( "上传身份信息");
        Timber.d( "地址" + uploadIdcard);
        Timber.d( "参数：" + params.toString());
        Timber.d( "身份证图像: " + vertifyRecord.getIdCardHeadPath());
        Timber.d( "人脸图像：" + vertifyRecord.getPersonHeadPath());
        Timber.d( "热成像图像" + vertifyRecord.getHotImagePath());
        OkHttpUtils.post().url(uploadIdcard)
                .params(params)
                .addFile("oldHeads", idCardFile.getName(), idCardFile)
                .addFile("newHeads", faceFile.getName(), faceFile)
                .addFile("reHead", reFile.getName(), reFile)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Timber.d( "onError: " + (e == null ? "NULL" : e.getMessage()));
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Timber.d( "onResponse: " + response);
                        vertifyRecord.setUpload(true);
                        DaoManager.get().update(vertifyRecord);
                    }
                });
    }

    /***
     * 上传人证记录
     * @param temper
     * @param msg
     * @param similar
     * @param isPass
     * @param idCardBitmap
     * @param faceBitmap
     * @param reBitmap
     */
    public void uploadIdCardAndReImage(float temper, IdCardMsg msg, int similar, int isPass, Bitmap idCardBitmap, Bitmap faceBitmap, Bitmap reBitmap) {
        VertifyRecord vertifyRecord = getIDCardVerifyRecord(temper, msg, similar, isPass, idCardBitmap, faceBitmap, reBitmap);
        uploadIdCardAndReImage(vertifyRecord);
    }

    //*************************************************************************************************8
    public boolean isBuluState() {
        return isBulu;
    }

    public void startBulu() {
        isBulu = true;
    }

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

    private void d(@NonNull String msg) {
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }

    /***
     * 大通量加入到库
     * @param multiTemperBean
     */
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

        Timber.d( "addSignToDB: 当前是第：" + add + " --- " + sign.getTime());

        int comid = SpUtils.getCompany().getComid();
        if (comid == Constants.NOT_BIND_COMPANY_ID) {
            return;
        }
    }

    //5寸添加记录到数据库
    public void add5InchRecordToDB(Bitmap bitmap, float temperature) {
        Record5Inch record5Inch = new Record5Inch();
        long time = System.currentTimeMillis();

        File file = saveBitmap(time, bitmap);
        record5Inch.setImgPath(file.getPath());
        record5Inch.setTemperature(temperature);

        int comid = SpUtils.getCompany().getComid();
        record5Inch.setComid(comid);
        record5Inch.setTime(time);
        record5Inch.setDate(dateFormat.format(time));

        record5Inch.setUpload(false);
        long add = DaoManager.get().add(record5Inch);
    }

    public void clearAllData(@NonNull Activity activity){
        List<Sign> signList = DaoManager.get().queryAll(Sign.class);
        if(signList == null || signList.size() == 0){
            UIUtils.showShort(activity,(activity.getString(R.string.clear_no_data) + "0"));
            return;
        }

        int total = 0;
        Iterator<Sign> iterator = signList.iterator();
        while (iterator.hasNext()) {
            Sign next = iterator.next();
            String headPath = next.getHeadPath();
            String hotImgPath = next.getHotImgPath();
            if(next.getType() != 0){
                if(!TextUtils.isEmpty(headPath)){
                    File headFile = new File(headPath);
                    if(headFile.exists()){
                        headFile.delete();
                    }
                }
            }
            if(!TextUtils.isEmpty(hotImgPath)){
                File hotFile = new File(hotImgPath);
                if(hotFile.exists()){
                    hotFile.delete();
                }
            }
            DaoManager.get().deleteSign(next);
            total ++;
        }

        UIUtils.showShort(activity,(activity.getString(R.string.clear_no_data) + total));
    }

    public void checkStorageSpace(){
        SdCardUtils.Capacity capacity = SdCardUtils.getUsedCapacity();
        double remainingSpace = capacity.getAll_mb() - capacity.getUsed_mb();
        if(remainingSpace < 500){
            List<Sign> signList = DaoManager.get().querySignByComidForEarly(SpUtils.getCompany().getComid(), 1);
            if(signList != null){
                Iterator<Sign> iterator = signList.iterator();
                while (iterator.hasNext()) {
                    Sign next = iterator.next();
                    String headPath = next.getHeadPath();
                    File file = new File(headPath);
                    if(file.exists()){
                        file.delete();
                    }
                    String hotImgPath = next.getHotImgPath();
                    file = new File(hotImgPath);
                    if(file.exists()){
                        file.delete();
                    }
                    DaoManager.get().deleteSign(next);
                }
            }
        }
    }
}