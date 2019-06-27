package com.yunbiao.ybsmartcheckin_live_id.business;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.jdjr.risk.face.local.user.FaceUserManager;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.activity.EmployListActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.bean.CompanyBean;
import com.yunbiao.ybsmartcheckin_live_id.bean.StaffBean;
import com.yunbiao.ybsmartcheckin_live_id.db.DepartBean;
import com.yunbiao.ybsmartcheckin_live_id.db.DepartDao;
import com.yunbiao.ybsmartcheckin_live_id.db.VIPDetail;
import com.yunbiao.ybsmartcheckin_live_id.db.UserDao;
import com.yunbiao.ybsmartcheckin_live_id.faceview.FaceSDK;
import com.yunbiao.ybsmartcheckin_live_id.heartbeat.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.xutil.MyXutils;
import com.yunbiao.ybsmartcheckin_live_id.views.FloatSyncView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by Administrator on 2019/5/14.
 */

public class SyncManager extends BroadcastReceiver {

    private static SyncManager instance;
    private Activity mAct;
    private boolean isLocalServ = false;
    private DepartDao departDao;
    private UserDao userDao;

    public static final int TYPE_ADD = 0;
    public static final int TYPE_UPDATE_INFO = 1;
    public static final int TYPE_UPDATE_HEAD = 2;

    private static int COMPANY_ID = 000000;
    public static String SCREEN_BASE_PATH = Constants.HEAD_PATH + COMPANY_ID + "/";//人脸头像存储路径

    private FloatSyncView floatSyncView;
    private ExecutorService executorService;

    private int remoteCount = 0;
    private int localCount = 0;

    public static SyncManager instance() {
        if (instance == null) {
            synchronized (SyncManager.class) {
                if (instance == null) {
                    instance = new SyncManager();
                }
            }
        }
        return instance;
    }

    private SyncManager() {
        File file = new File(SCREEN_BASE_PATH);
        if(!file.exists()){
            file.mkdirs();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == ConnectivityManager.CONNECTIVITY_ACTION) {
            /*判断当前网络时候可用以及网络类型*/
            boolean networkConnected = isNetworkConnected(context);
            if (networkConnected) {
                initInfo();
            } else {
                setFailed("失败", "网络不可用，请检查网络");
            }
        }
    }

    public interface LoadListener{
        void onLoaded(CompanyBean companyBean);

        void onFinish();
    }
    private LoadListener mListener;
    public void setListener(LoadListener listener){
        mListener = listener;
    }

    /***
     * 初始化数据
     * @param act
     * @return
     */
    public SyncManager init(@NonNull Activity act) {
        mAct = act;
        departDao = APP.getDepartDao();
        userDao = APP.getUserDao();
        executorService = Executors.newFixedThreadPool(2);
        String webBaseUrl = ResourceUpdate.WEB_BASE_URL;
        String[] split = webBaseUrl.split(":");
        for (String s : split) {
            if (s.startsWith("192.168")) {
                isLocalServ = true;
            }
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mAct.registerReceiver(this, filter);
        return instance;
    }

    /***
     * 全部流程重新初始化
     */
    public void initInfo() {
        OkHttpUtils.getInstance().cancelTag(this);
        cancelTimer();
        remoteCount = 0;
        localCount = 0;
        show();
        loadCompany();
    }

    public UserDao getUserDao(){
        return userDao;
    }

    // TODO: 2019/6/27 ComById
//    private void loadCompanyById(){
//        setStep(1,null);
//        int companyId = SpUtils.getCompanyId();
//        d(ResourceUpdate.COMPANYINFO_ID + " --- " + companyId);
//        Map<String,String> params = new HashMap<>();
//        params.put("comId","" + companyId);
//        OkHttpUtils.post()
//                .url(ResourceUpdate.COMPANYINFO_ID)
//                .params(params).build()
//                .execute(new MyStringCallback<CompanyBean>(MyStringCallback.STEP_COMPANY){
//                    @Override public void onRetryAfter5s() { }
//                    @Override public void onFailed() { }
//                    @Override public void onSucc(String response, final CompanyBean companyBean) {
//                        COMPANY_ID = companyBean.getCompany().getComid();
//                        String abbname = companyBean.getCompany().getAbbname();
//                        SCREEN_BASE_PATH = Constants.HEAD_PATH + COMPANY_ID + "/";//人脸头像存储路径
//
//                        SpUtils.saveStr(SpUtils.COMPANY_INFO, response);
//                        //保存公司信息
//                        SpUtils.saveInt(SpUtils.COMPANYID, companyBean.getCompany().getComid());
//                        SpUtils.saveStr(SpUtils.GOTIME, companyBean.getCompany().getGotime());
//                        SpUtils.saveStr(SpUtils.GOTIPS, companyBean.getCompany().getGotips());
//                        SpUtils.saveStr(SpUtils.DOWNTIME, companyBean.getCompany().getDowntime());
//                        SpUtils.saveStr(SpUtils.DOWNTIPS, companyBean.getCompany().getDowntips());
//                        SpUtils.saveStr(SpUtils.COMPANY_NAME, abbname);
//
//                        if(mListener != null){
//                            mAct.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    mListener.onLoaded(companyBean);
//                                }
//                            });
//                        }
//                        loadStaff(companyBean);
//                    }
//                });
//    }

    private void loadCompany() {
        setStep(1,null);
        d("-------------" + ResourceUpdate.COMPANYINFO);
        final Map<String, String> map = new HashMap<>();
        String deviceNo = HeartBeatClient.getDeviceNo();
        Log.e(TAG, "loadCompany: " + deviceNo);
        map.put("deviceNo", deviceNo);
        OkHttpUtils.post().params(map).tag(this).url(ResourceUpdate.COMPANYINFO).build().execute(new MyStringCallback<CompanyBean>(MyStringCallback.STEP_COMPANY) {
            @Override public void onRetryAfter5s() {
                loadCompany();
            }
            @Override public void onFailed() {}
            @Override public void onSucc(final String response, final CompanyBean companyBean) {
                COMPANY_ID = companyBean.getCompany().getComid();
                String abbname = companyBean.getCompany().getAbbname();
                SCREEN_BASE_PATH = Constants.HEAD_PATH + COMPANY_ID + "/";//人脸头像存储路径

                SpUtils.saveStr(SpUtils.COMPANY_INFO, response);
                //保存公司信息
                SpUtils.saveInt(SpUtils.COMPANYID, companyBean.getCompany().getComid());
                SpUtils.saveStr(SpUtils.GOTIME, companyBean.getCompany().getGotime());
                SpUtils.saveStr(SpUtils.GOTIPS, companyBean.getCompany().getGotips());
                SpUtils.saveStr(SpUtils.DOWNTIME, companyBean.getCompany().getDowntime());
                SpUtils.saveStr(SpUtils.DOWNTIPS, companyBean.getCompany().getDowntips());
                SpUtils.saveStr(SpUtils.COMPANY_NAME, abbname);

                if(mListener != null){
                    mAct.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onLoaded(companyBean);
                        }
                    });
                }
                loadStaff(companyBean);
            }
        });
    }

    //加载员工信息
    private void loadStaff(final CompanyBean companyBean) {
        setStep(2,null);
        int comId = companyBean.getCompany().getComid();
        if (comId == 0) {
            setFailed("失败", "数据异常");
            return;
        }
        d("请求员工信息");
        final HashMap<String, String> map = new HashMap<>();
        map.put("companyId", comId + "");
        OkHttpUtils.post().params(map).tag(this).url(ResourceUpdate.GETSTAFF).build().execute(new MyStringCallback<StaffBean>(MyStringCallback.STEP_STAFF) {
            @Override
            public void onRetryAfter5s() {
                loadStaff(companyBean);
            }

            @Override
            public void onFailed() {
            }

            @Override
            public void onSucc(String response, StaffBean staffBean) {
                syncDao(companyBean, staffBean);
            }
        });
    }

    //同步数据库
    private void syncDao(final CompanyBean companyBean, final StaffBean staffBean) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                syncDepartDao(companyBean);
                syncUserDao(staffBean);
            }
        });
    }

    //同步部门数据库
    private void syncDepartDao(final CompanyBean companyBean) {
        if (companyBean == null) {
            return;
        }
        setStep(3,null);
        List<CompanyBean.CompanyEntity.DeparrayEntity> departList = companyBean.getCompany().getDeparray();
        if(departList == null){
            return;
        }
        for (CompanyBean.CompanyEntity.DeparrayEntity deparrayEntity : departList) {
            String depName = deparrayEntity.getDepName();
            int depId = deparrayEntity.getDepId();
            List<DepartBean> list = departDao.queryByName(depName);
            if (list == null || list.size() <= 0) {
                departDao.insert(new DepartBean(depName, depId));
            }
        }
    }

    private void syncUserDao(final StaffBean staffBean) {
        if (staffBean == null) {
            return;
        }
        setStep(4,null);
        Queue<UpdateBean> updateQueue = new LinkedList<>();
        Map<Integer, StaffInfoBean> staffMap = new HashMap<>();
        List<StaffBean.DepEntity> dep = staffBean.getDep();
        for (StaffBean.DepEntity depEntity : dep) {
            List<StaffBean.DepEntity.EntryEntity> entryList = depEntity.getEntry();
            if (entryList != null && entryList.size() > 0) {
                for (StaffBean.DepEntity.EntryEntity entryEntity : entryList) {
                    StaffInfoBean staffInfoBean = new StaffInfoBean();
                    staffInfoBean.depId = depEntity.getDepId();
                    staffInfoBean.depName = depEntity.getDepName();
                    staffInfoBean.staffInfo = entryEntity;
                    staffMap.put(entryEntity.getFaceId(), staffInfoBean);
                }
            }
        }

        for (UpdateBean updateBean : updateQueue) {
            d(updateBean.toString());
        }

        for (Map.Entry<Integer, StaffInfoBean> integerStaffInfoBeanEntry : staffMap.entrySet()) {
            d(integerStaffInfoBeanEntry.toString());
        }

        List<VIPDetail> localDataList = new ArrayList<>();
        localDataList.addAll(userDao.selectAll());
        localCount = localDataList.size();

        if(staffMap != null){
            remoteCount = staffMap.size();
        }
        d("本地数据共有：" + localCount);
        d("服务器数据共有：" + remoteCount);

        for (VIPDetail vipDetail : localDataList) {
            int faceId = vipDetail.getFaceId();
            if (staffMap.containsKey(faceId))
                continue;
            boolean isRemoveSucc = FaceSDK.instance().removeUser(String.valueOf(faceId));
            userDao.delete(vipDetail);
            Log.e(TAG, "syncUserDao: ----- 删除结果： " + isRemoveSucc + " ----- " + vipDetail.toString());
        }

        for (Map.Entry<Integer, StaffInfoBean> integerStaffInfoBeanEntry : staffMap.entrySet()) {
            StaffInfoBean value = integerStaffInfoBeanEntry.getValue();
            int departId = value.depId;//部门ID
            String departName = value.depName;//部门
            StaffBean.DepEntity.EntryEntity entryEntity = value.staffInfo;
            int faceId = entryEntity.getFaceId();//人脸ID
            String xingbie = entryEntity.getSex()== 1 ? "男" : "女";//性别
            int empId = entryEntity.getId();//员工ID
            int age = entryEntity.getAge();//年龄
            String name = entryEntity.getName();//名字
            String job = entryEntity.getPosition();//职位
            String birthday = entryEntity.getBirthday();//生日
            String employNum = entryEntity.getNumber();//员工编号
            String signature = entryEntity.getAutograph();//签名
            String urlPath = entryEntity.getHead();//头像地址
            int index = urlPath.lastIndexOf("/");
            String str = urlPath.substring(index + 1, urlPath.length());
            String filepath = SCREEN_BASE_PATH + str;

            List<VIPDetail> vipDetails = userDao.queryByFaceId(faceId);
            if (vipDetails == null || vipDetails.size() <= 0) {//添加
                UpdateBean addStaffBean = new UpdateBean();
                addStaffBean.ctrlType = TYPE_ADD;
                addStaffBean.head = entryEntity.getHead();
                addStaffBean.vipDetail = new VIPDetail(departId, empId, faceId, xingbie, age + "", name, departName, job, employNum, birthday, signature, filepath);
                updateQueue.offer(addStaffBean);
            } else {//更新
                VIPDetail vipDetail = vipDetails.get(0);
                boolean isHeadUpdate = isHeadUpdate(filepath, vipDetail.getImgUrl());
                boolean isInfoUpdate = isUpdate(empId, name, departName, job, employNum, signature, vipDetail);
                if (isHeadUpdate) {//头像更新
                    UpdateBean addStaffBean = new UpdateBean();
                    addStaffBean.ctrlType = TYPE_UPDATE_HEAD;
                    addStaffBean.head = entryEntity.getHead();
                    addStaffBean.vipDetail = new VIPDetail(departId, empId, faceId, xingbie, age + "", name, departName, job, employNum, birthday, signature, filepath);
                    updateQueue.offer(addStaffBean);
                }

                if (isInfoUpdate) {//信息更新
                    UpdateBean addStaffBean = new UpdateBean();
                    addStaffBean.ctrlType = TYPE_UPDATE_INFO;
                    addStaffBean.head = entryEntity.getHead();
                    addStaffBean.vipDetail = new VIPDetail(departId, empId, faceId, xingbie, age + "", name, departName, job, employNum, birthday, signature, vipDetail.getImgUrl());
                    updateQueue.offer(addStaffBean);
                }
            }
            setStep(0,name);
        }

        downloadHead(updateQueue);
    }

    private void downloadHead(final Queue<UpdateBean> updateQueue){
        setStep(5,"下载头像");
        if (updateQueue == null || updateQueue.size() <= 0) {
            close();
            return;
        }
        UpdateBean poll = updateQueue.poll();
        final int ctrlType = poll.ctrlType;
        if (ctrlType == -1) {
            downloadHead(updateQueue);
            return;
        }
        if (ctrlType == TYPE_ADD || ctrlType == TYPE_UPDATE_HEAD) {
            final VIPDetail vipDetail = poll.vipDetail;
            String headUrl = poll.head;
            int index = headUrl.lastIndexOf("/");
            String str = headUrl.substring(index + 1, headUrl.length());
            final String filepath = SCREEN_BASE_PATH + str;
            setStep(5, vipDetail.getName());
            Log.e(TAG, "下载: --------" + vipDetail.getName());
            final String finalHeadUrl = headUrl;
            MyXutils.getInstance().downLoadFile(headUrl, filepath, false, new MyXutils.XDownLoadCallBack() {
                @Override public void onLoading(long total, long current, boolean isDownloading) {}

                @Override
                public void onSuccess(File result) {
                    Log.d(TAG, "下载头像成功..." + Thread.currentThread().getName());
                    vipDetail.setDownloadTag(true);
                    updateOnThread(ctrlType, filepath, vipDetail, new Runnable() {
                        @Override
                        public void run() {
                            downloadHead(updateQueue);
                        }
                    });
                }

                @Override
                public void onError(Throwable ex) {
                    Log.d(TAG, "下载头像失败..." + finalHeadUrl + "---------" + ex.getMessage());
                    vipDetail.setDownloadTag(false);
//                    vipDetail.setMark("头像下载失败");
                    List<VIPDetail> vipDetails = userDao.queryByFaceId(vipDetail.getFaceId());
                    if (vipDetails == null || vipDetails.size() <= 0) {
                        userDao.insert(vipDetail);
                    } else {
                        userDao.deleteByFaceId(vipDetail.getFaceId());
                        userDao.insert(vipDetail);
                    }
                    downloadHead(updateQueue);
                }
            });
        }else if (ctrlType == TYPE_UPDATE_INFO) {
            VIPDetail vipDetail = poll.vipDetail;
            updateOnThread(ctrlType, null, vipDetail, new Runnable() {
                @Override
                public void run() {
                    downloadHead(updateQueue);
                }
            });
        }
    }

    private void updateOnThread(final int ctrlType, final String filePath, final VIPDetail vipDetail, final Runnable r) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                String threadName = Thread.currentThread().getName();
                Log.e("checkThread","run运行线程："+threadName);
                if (ctrlType == TYPE_ADD) {
                    userDao.insert(vipDetail);

                    // TODO: 2019/6/4
                    FaceSDK.instance().addUser(String.valueOf(vipDetail.getFaceId()), filePath, new FaceUserManager.FaceUserCallback() {
                        @Override
                        public void onUserResult(boolean b, int resultCode) {
                            if(!b){
                                if (resultCode == FaceUserManager.USER_EXIST) {
                                    Log.e(TAG, "onUserResult: 用户已经注册...");
                                } else {
                                    Log.e(TAG, "onUserResult: 用户注册失败!!!");
                                    vipDetail.setDownloadTag(false);
//                                  vipDetail.setMark("添加人脸库失败");
                                }
                            }
                        }
                    });

                } else if (ctrlType == TYPE_UPDATE_HEAD) {

                    boolean b = FaceSDK.instance().removeUser(String.valueOf(vipDetail.getFaceId()));
                    if(!b){
                        vipDetail.setDownloadTag(false);
                        //vipDetail.setMark("更新头像失败：删除旧头像失败");
                    }

                    userDao.deleteByFaceId(vipDetail.getFaceId());
                    userDao.insert(vipDetail);
                } else if (ctrlType == TYPE_UPDATE_INFO) {
                    userDao.deleteByFaceId(vipDetail.getFaceId());
                    userDao.insert(vipDetail);
                    Log.e(TAG, "TYPE_UPDATE_INFO: --------" + vipDetail.getName());
                }
                r.run();
            }
        });
    }

    public void destory() {
        OkHttpUtils.getInstance().cancelTag(this);
        if(floatSyncView != null){
            floatSyncView.dismiss();
            floatSyncView = null;
        }
        try{
            mAct.unregisterReceiver(this);
        }catch (Exception e){
            Log.d(TAG,TAG+"广播未注册");
        }
    }

    //员工详情
    class StaffInfoBean {
        int depId;
        String depName;
        StaffBean.DepEntity.EntryEntity staffInfo;

        @Override
        public String toString() {
            return "StaffInfoBean{" +
                    "depId=" + depId +
                    ", depName='" + depName + '\'' +
                    ", staffInfo=" + staffInfo +
                    '}';
        }
    }

    //更新bean
    class UpdateBean {
        int ctrlType = -1;
        String head;
        VIPDetail vipDetail;

        @Override
        public String toString() {
            return "UpdateBean{" +
                    "ctrlType=" + ctrlType +
                    ", head='" + head + '\'' +
                    ", vipDetail=" + vipDetail +
                    '}';
        }
    }

    /***
     * 带UI更新的请求回调
     * @param <T>
     */
    abstract class MyStringCallback<T> extends StringCallback {
        private String title;
        private int step;
        public static final int STEP_COMPANY = 1;
        public static final int STEP_STAFF = 3;
        public static final String TITLE_COMPANY = "公司信息";
        public static final String TITLE_STAFF = "员工信息";
        private Handler handler = new Handler(Looper.getMainLooper());

        public MyStringCallback(int s) {
            step = s;
            switch (step) {
                case STEP_COMPANY:
                    title = TITLE_COMPANY;
                    break;
                case STEP_STAFF:
                    title = TITLE_STAFF;
                    break;
            }
        }

        public abstract void onRetryAfter5s();

        public abstract void onFailed();

        public abstract void onSucc(String response, T t);

        @Override
        public void onBefore(Request request, int id) {
            super.onBefore(request, id);
        }

        @Override
        public void onError(Call call, Exception e, int id) {
            e.printStackTrace();
            String err = "请求失败";
            if(e != null && (!TextUtils.isEmpty(e.getMessage()))){
                if(e.getMessage().contains("404")){
                    err = "服务器异常";
                } else if(e.getMessage().contains("500")){
                    err = "服务器异常";
                }
            }
            if (isLocalServ || isNetworkConnected(APP.getContext())) {
                setErr("失败",err + "，5秒后重试...");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onRetryAfter5s();
                    }
                }, 5 * 1000);
            } else {
                setFailed("失败", "同步失败，请检查网络连接");
                onFailed();
            }
        }

        @Override
        public void onResponse(String response, int id) {
            d(TAG,response);
            Object o = null;
            if (step == STEP_COMPANY) {
                if(TextUtils.isEmpty(response)){
                    setFailed("失败", "Response is null");
                    onFailed();
                    return;
                }

                if(!isJSONValid(response)){
                    setFailed("失败", "Response is not JSON");
                    onFailed();
                    return;
                }

                CompanyBean bean = new Gson().fromJson(response, CompanyBean.class);

                if (bean.getStatus() != 1) {
                    String err = "同步失败，请检查网络或重启设备";
                    switch (bean.getStatus()) {
                        case 3://设备不存在（参数错误）
                            err = "设备不存在";
                            break;
                        case 4://设备未绑定
                            err = "请先绑定设备";
                            break;
                        case 5://未设置主题
                            err = "该设备未设置主题";
                            break;
                        default://获取失败
                            err = "同步失败，错误码：" + bean.getStatus();
                            break;
                    }
                    setFailed("失败", err);
                    onFailed();
                    return;
                }
                o = bean;
            } else {//员工信息
                StaffBean staffInfo = new Gson().fromJson(response, StaffBean.class);
                if (staffInfo.getStatus() != 1) {
                    String err = "同步失败，请检查网络或重启设备";
                    switch (staffInfo.getStatus()) {
                        case 3://公司不存在
                            err = "公司不存在";
                            break;
                        case 4://公司未设置部门
                            err = "该公司未设置部门";
                            break;
                        default://参数错误
                            err = "数据异常，错误码：" + staffInfo.getStatus();
                            break;
                    }
                    setFailed("失败", err);
                    onFailed();
                    return;
                }
                o = staffInfo;
            }
            onSucc(response,(T) o);
        }
    }

    /*======UI显示============================================================================================*/
    //显示同步UI
    private void show() {
        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (floatSyncView == null) {
                    floatSyncView = new FloatSyncView(APP.getContext());
                }
                floatSyncView.initUIState();
                floatSyncView.show();
            }
        });
    }

    private Timer timer;
    private void startTimer(TimerTask timerTask,long delay){
        if(timer != null){
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(timerTask,delay);
    }

    private void cancelTimer(){
        if(timer != null){
            timer.cancel();
        }
    }

    //关闭同步
    private void close() {
        List<VIPDetail> vipDetails = userDao.selectAll();
        localCount = vipDetails.size();
        EventBus.getDefault().postSticky(new EmployListActivity.EmployUpdate());

        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (floatSyncView != null) {
                    setStep(0,"同步完成");
                    if(mListener != null){
                        mListener.onFinish();
                    }

                    floatSyncView.hideLoadingView();
                    floatSyncView.showDownloadView(false);
                    floatSyncView.showCount(localCount, remoteCount);

                    startTimer(new TimerTask() {
                        @Override
                        public void run() {
                            if(floatSyncView != null){
                                floatSyncView.dismiss();
                            }
                        }
                    }, 8 * 1000);
                }
            }

        });
    }

    //错误显示
    private void setErr(final String info, final String err){
        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(floatSyncView != null){
                    if(!TextUtils.isEmpty(info)){
                        floatSyncView.setErr(err,true);
                    }
                    if(!TextUtils.isEmpty(err)){
                        floatSyncView.setInfo(info);
                    }
                }
            }
        });
    }

    //失败显示
    private void setFailed(final String info, final String errStr) {
        if (floatSyncView != null) {
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    floatSyncView.hideLoadingView();
                }
            });
        }
        setErr(info,errStr);
    }

    //下载进度
    private void setDownloadP(final int max, final int p){
        if(floatSyncView != null){
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    floatSyncView.setP(max,p);
                }
            });
        }
    }

    private void setStep(final int max, final int p, final String info){
        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (floatSyncView != null) {
                    floatSyncView.showDownloadView(true);
                    floatSyncView.setStep(p + "/" + max);

                    if (!TextUtils.isEmpty(info)) {
                        floatSyncView.setInfo(info);
                    }
                }
            }
        });
    }

    //步骤
    private void setStep(int i,String info){
        String step = null;
        if(i>=1 && i <=5){
            step = i + "/5";
        }
        boolean showDownload = false;
        switch (i) {
            case 1:
                info = "查询公司信息";
                step += info;
                info = null;
                break;
            case 2:
                info = "查询员工信息";
                step += info;
                info = null;
                break;
            case 3:
                info = "删除无效数据";
                step += info;
                info = null;
                break;
            case 4:
                info = "同步员工信息";
                step += info;
                info = null;
                break;
            case 5:
                step += "下载头像";
                showDownload = true;
                break;
            default:break;
        }

        final String finalInfo = info;
        final String finalStep = step;
        final boolean finalShowDownload = showDownload;
        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (floatSyncView != null) {
                    floatSyncView.showDownloadView(finalShowDownload);

                    if(!TextUtils.isEmpty(finalStep)){
                        floatSyncView.setStep(finalStep);
                    }
                    if (!TextUtils.isEmpty(finalInfo)) {
                        floatSyncView.setInfo(finalInfo);
                    }
                }
            }
        });
    }

    /*===========判断方法=====================================================================================*/
    //判断网络连接
    private boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    private static final String TAG = "SyncManager";

    private void d(String log) {
        if (true) {
            Log.d(TAG, log);
        }
    }

    //判断头像是否有更新
    private boolean isHeadUpdate(String filePath, String imgUrl) {//员工头像和本地存储的头像是否冲突一致
        File fileLoc = new File(imgUrl);
        boolean b = !imgUrl.equals(filePath);
        Log.e(TAG, "isHeadUpdate: -----" + filePath + "-----" + imgUrl + "-----" +fileLoc.exists() + "-----" + b);
        if (!fileLoc.exists()) {
            return true;
        }
        return b;
    }

    //员工信息是否有更新
    private boolean isUpdate(String depName,StaffBean.DepEntity.EntryEntity staffInfo, VIPDetail vipLoc) {
        if (staffInfo.getId() != vipLoc.getEmpId()) {
            return true;
        }
        if (!staffInfo.getName().equals(vipLoc.getName())) {
            return true;
        }
        if (!depName.equals(vipLoc.getDepart())) {
            return true;
        }
        if (staffInfo.getNumber() != null && !staffInfo.getNumber().equals(vipLoc.getEmployNum())) {
            return true;
        }
        if (staffInfo.getPosition() != null && !staffInfo.getPosition().equals(vipLoc.getJob())) {
            return true;
        }
        return staffInfo.getAutograph() != null && !staffInfo.getAutograph().equals(vipLoc.getSignature());

    }

    //员工信息是否有更新
    private boolean isUpdate(int empId, String name, String depart, String job, String employNum, String signature, VIPDetail vipLoc) {

        if (empId != vipLoc.getEmpId()) {
            return true;
        }
        if (!name.equals(vipLoc.getName())) {
            return true;
        }
        if (!depart.equals(vipLoc.getDepart())) {
            return true;
        }
        if (employNum != null && !employNum.equals(vipLoc.getEmployNum())) {
            return true;
        }
        if (job != null && !job.equals(vipLoc.getJob())) {
            return true;
        }
        return signature != null && !signature.equals(vipLoc.getSignature());

    }

    private VIPDetail getVipDetail(StaffInfoBean value){
        VIPDetail vipDetail = new VIPDetail();
        StaffBean.DepEntity.EntryEntity staffInfo = value.staffInfo;
        vipDetail.setDepartId(value.depId);
        vipDetail.setEmpId(staffInfo.getId());
        vipDetail.setFaceId(staffInfo.getFaceId());
        vipDetail.setSex(staffInfo.getSex()== 1 ? "男" : "女");
        vipDetail.setAge(staffInfo.getAge()+"");
        vipDetail.setName(staffInfo.getName());
        vipDetail.setDepart(value.depName);
        vipDetail.setJob(staffInfo.getPosition());
        vipDetail.setEmployNum(staffInfo.getNumber());
        vipDetail.setBirthday(staffInfo.getBirthday());
        vipDetail.setSignature(staffInfo.getAutograph());
        vipDetail.setImgUrl(staffInfo.getHead());
        vipDetail.setDownloadTag(true);
        return vipDetail;
    }

    private String getImgPath(String imgUrl,int empId){
        if(TextUtils.isEmpty(imgUrl)){
            return "";
        }
        //生成文件名
        int index = imgUrl.lastIndexOf("/");
        String str = imgUrl.substring(index + 1, imgUrl.length());
        String filepath = SCREEN_BASE_PATH + empId + "_" +str;
        return filepath;
    }
    //日志打印不全
    public static void d(String tag, String msg) {  //信息太长,分段打印

        //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，

        //  把4*1024的MAX字节打印长度改为2001字符数

        int max_str_length = 2001 - tag.length();

        //大于4000时

        while (msg.length() > max_str_length) {

            Log.i(tag, msg.substring(0, max_str_length));

            msg = msg.substring(max_str_length);

        }

        //剩余部分

        Log.d(tag, msg);

    }

    public final static boolean isJSONValid(String jsonInString) {
        try {
            new Gson().fromJson(jsonInString, Object.class);
            return true;
        } catch(JsonSyntaxException ex) {
            return false;
        }
    }
}
