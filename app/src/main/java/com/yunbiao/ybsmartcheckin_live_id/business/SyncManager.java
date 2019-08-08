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
import com.yunbiao.ybsmartcheckin_live_id.db.CompBean;
import com.yunbiao.ybsmartcheckin_live_id.db.DepartBean;
import com.yunbiao.ybsmartcheckin_live_id.db.DepartDao;
import com.yunbiao.ybsmartcheckin_live_id.db.VIPDetail;
import com.yunbiao.ybsmartcheckin_live_id.db.UserDao;
import com.yunbiao.ybsmartcheckin_live_id.faceview.FaceSDK;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
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
    public static final int TYPE_UPDATE_HEAD = 2;

    private FloatSyncView floatSyncView;
    private ExecutorService executorService;

    private int mUpdateTotal = 0;//更新总数
    private int mCurrIndex = 0;//当前索引

    private long initOffset = 2 * 24 * 60 * 60 *1000;//更新间隔时间7天

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
        File file = new File(Constants.HEAD_PATH);
        if(!file.exists()){
            file.mkdirs();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == ConnectivityManager.CONNECTIVITY_ACTION) {
            long lastInitTime = SpUtils.getLong(SpUtils.LAST_INIT_TIME);
            long currTime = System.currentTimeMillis();
            Log.e(TAG, "onReceive: -----" + lastInitTime + " --- " + currTime );
            if(currTime - lastInitTime >= initOffset){//如果大于间隔则同步
                initInfo();
            } else {//如果小于间隔则获取公司数据
                loadCompany(false);
            }
        }
    }

    public interface LoadListener{
        void onLoaded();

        void onFinish();
    }
    private LoadListener mListener;

    /***
     * 初始化数据
     * @param act
     * @return
     */
    public SyncManager init(@NonNull Activity act,LoadListener listener) {
        mAct = act;
        mListener = listener;
        departDao = APP.getDepartDao();
        userDao = APP.getUserDao();
        executorService = Executors.newFixedThreadPool(2);
        APP.initCompBean();

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

        if(mListener != null){
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mListener.onLoaded();
                }
            });
        }
        return instance;
    }

    /***
     * 全部流程重新初始化
     */
    public void initInfo() {
        OkHttpUtils.getInstance().cancelTag(this);
        cancelTimer();
        mUpdateTotal = 0;
        mCurrIndex = 0;
        showUI();
        loadCompany(true);
    }

    // TODO: 2019/6/27 ComById
//    private void loadCompanyById(){
//        setInfo("获取公司信息");
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
//                        mCompId = companyBean.getCompany().getComid();
//                        String abbname = companyBean.getCompany().getAbbname();
//                        SCREEN_BASE_PATH = Constants.HEAD_PATH + mCompId + "/";//人脸头像存储路径
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

    private void loadCompany(final boolean isInit) {
        setInfo("获取公司信息");
        d("-------------" + ResourceUpdate.COMPANYINFO);
        final Map<String, String> map = new HashMap<>();
        String deviceNo = HeartBeatClient.getDeviceNo();
        Log.e(TAG, "loadCompany: " + deviceNo);
        map.put("deviceNo", deviceNo);
        OkHttpUtils.post().params(map).tag(this).url(ResourceUpdate.COMPANYINFO).build().execute(new MyStringCallback<CompanyBean>(MyStringCallback.STEP_COMPANY) {
            @Override public void onRetryAfter5s() {
                loadCompany(isInit);
            }
            @Override public void onSucc(final String response, final CompanyBean companyBean) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        SpUtils.saveStr(SpUtils.MENU_PWD,companyBean.getCompany().getDevicePwd());

                        int comid = companyBean.getCompany().getComid();
                        int lastComId = APP.getCompanyId();
                        //如果是未初始化，或者两次的comId不想等（表示换了绑定公司），则请求员工信息
                        if (isInit || (comid != lastComId)) {
                            loadStaff(companyBean);

                            SpUtils.saveInt(SpUtils.COMPANYID,comid);
                        }

                        CompBean compBean = APP.getCompDao().queryByCompId(comid);
                        boolean notExits = compBean == null;
                        if(notExits){
                            compBean = new CompBean();
                        }
                        //判断logo是否改变
                        String comlogo = companyBean.getCompany().getComlogo();
                        String iconUrl = compBean.getIconUrl();
                        if(!TextUtils.equals(comlogo,iconUrl)){//如果改变了就重新生成路径
                            //生成路径
                            String name = comlogo.substring(comlogo.lastIndexOf("/") + 1);
                            File logoFile = new File(Constants.DATA_PATH, "logo_" + name);
                            compBean.setIconPath(logoFile.getPath());
                            compBean.setIconUrl(comlogo);
                        }
                        compBean.setQRCodePath(new File(Constants.DATA_PATH,"QRCode.png").getPath());
                        compBean.setComid(companyBean.getCompany().getComid());
                        compBean.setCompName(companyBean.getCompany().getComname());
                        compBean.setAbbName(companyBean.getCompany().getAbbname());
                        compBean.setTopTitle(companyBean.getCompany().getToptitle());
                        compBean.setBottomTitle(companyBean.getCompany().getBottomtitle());
                        compBean.setDevicePwd(companyBean.getCompany().getDevicePwd());
                        compBean.setGotime(companyBean.getCompany().getGotime());
                        compBean.setGotips(companyBean.getCompany().getGotips());
                        compBean.setDowntime(companyBean.getCompany().getDowntime());
                        compBean.setDowntips(companyBean.getCompany().getDowntips());
                        compBean.setNotice(companyBean.getCompany().getNotice());
                        compBean.setSlogan(companyBean.getCompany().getSlogan());
                        String jsonStr = new Gson().toJson(companyBean.getCompany().getLate());
                        compBean.setLate(jsonStr);

                        if(notExits)
                            APP.getCompDao().insert(compBean);
                        else
                            APP.getCompDao().update(compBean);

                        APP.initCompBean();
                        if(mListener != null){
                            mAct.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mListener.onLoaded();
                                }
                            });
                        }
                    }
                });
            }

            @Override public void onFailed() {

            }
        });
    }

    //加载员工信息
    private void loadStaff(final CompanyBean companyBean) {
        setInfo("获取人员信息");
        int comId = companyBean.getCompany().getComid();
        if (comId == 0) {
            setErrInfo("数据异常");
            return;
        }
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
                setInfo("正在同步");
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

        List<VIPDetail> localDataList = new ArrayList<>();

        List<VIPDetail> vipDetails1 = userDao.selectAll();
        if(vipDetails1 != null && vipDetails1.size()>0){
            localDataList.addAll(vipDetails1);
        }
        Log.e(TAG, "云端数据：" + staffMap.size() + "---本地数据：" + localDataList.size());

        for (VIPDetail vipDetail : localDataList) {
            int faceId = vipDetail.getFaceId();
            if (staffMap.containsKey(faceId))
                continue;
            boolean isRemoveSucc = FaceSDK.instance().removeUser(String.valueOf(faceId));
            int delete = userDao.delete(vipDetail);
            Log.e(TAG, "syncUserDao: ----- 删除结果： " +  delete + " --- " + isRemoveSucc + " ----- " + vipDetail.toString());
        }

        Queue<UpdateBean> updateBeanList = new LinkedList<>();
        for (Map.Entry<Integer, StaffInfoBean> integerStaffInfoBeanEntry : staffMap.entrySet()) {
            StaffInfoBean value = integerStaffInfoBeanEntry.getValue();
            StaffBean.DepEntity.EntryEntity entryEntity = value.staffInfo;

            //处理头像地址为本地
            String urlPath = entryEntity.getHead();//头像地址
            String filepath = Constants.HEAD_PATH + urlPath.substring(urlPath.lastIndexOf("/") + 1);

            //生成新的员工信息
            VIPDetail newVIPDetail = new VIPDetail(value.depId,entryEntity.getId(),entryEntity.getFaceId(),entryEntity.getSex()+"",entryEntity.getAge()+""
                    , entryEntity.getName(),value.depName,entryEntity.getPosition(),entryEntity.getNumber(),entryEntity.getBirthday(),entryEntity.getAutograph(),filepath);

            List<VIPDetail> vipDetails = userDao.queryByFaceId(entryEntity.getFaceId());
            if(vipDetails == null || vipDetails.size() <= 0){//添加：直接添加，然后下载头像
                int insert = userDao.insert(newVIPDetail);
                Log.e(TAG, "添加结果：" + insert + " --- " + newVIPDetail.toString());
                updateBeanList.add(new UpdateBean(TYPE_ADD,urlPath,newVIPDetail));
            } else {
                VIPDetail oldVIPDetail = vipDetails.get(0);
                boolean headUpdate = isHeadUpdate(newVIPDetail.getImgUrl(), oldVIPDetail.getImgUrl());
                boolean infoUpdate = isInfoUpdate(newVIPDetail, oldVIPDetail);
                if(infoUpdate){//更新信息，直接更新即可
                    int i = userDao.deleteByFaceId(newVIPDetail.getFaceId());
                    int insert = userDao.insert(newVIPDetail);
                    Log.e(TAG, "更新信息结果：" + i + " --- " + insert + " --- " + newVIPDetail.toString());
                } else if(headUpdate){//更新头像：先更新库，然后下载头像
                    File file = new File(oldVIPDetail.getImgUrl());
                    Log.e(TAG, "需更新头像：" + newVIPDetail.getImgUrl() + " --- " + oldVIPDetail.getImgUrl());
                    boolean delete = file.delete();
                    Log.e(TAG, "删除旧文件：" + delete);
                    oldVIPDetail.setImgUrl(newVIPDetail.getImgUrl());
                    userDao.update(oldVIPDetail);
                    updateBeanList.add(new UpdateBean(TYPE_UPDATE_HEAD,urlPath,oldVIPDetail));
                }
            }
        }

        setInfo("下载头像");
        mUpdateTotal = updateBeanList.size();
        showProgress();
        downloadHead(updateBeanList);
    }

    private void downloadHead(final Queue<UpdateBean> queue){
        if(queue == null || queue.size() <= 0){
            setInfo("同步人脸库");
            mCurrIndex = 0;
            addToFaceDB();
            return;
        }
        mCurrIndex++;
        setTextProgress(mCurrIndex + "/" + mUpdateTotal);

        UpdateBean updateBean = queue.poll();
        int ctrlType = updateBean.ctrlType;
        if(ctrlType == -1){
            downloadHead(queue);
            return;
        }
        String headUrl = updateBean.head;
        final VIPDetail vipDetail = updateBean.vipDetail;
        File file = new File(vipDetail.getImgUrl());
        if(file.exists()){
            downloadHead(queue);
            return;
        }

        Log.e(TAG, "开始下载... " + vipDetail.getName() + " --- " + headUrl);
        MyXutils.getInstance().downLoadFile(headUrl, vipDetail.getImgUrl(), false, new MyXutils.XDownLoadCallBack() {
            boolean isSucc = false;
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                Log.e(TAG, "进度... " + current + " ... " + total);
                setProgress((int) current,(int) total);
            }

            @Override
            public void onSuccess(File result) {
                Log.e(TAG, "下载成功... " + result.getPath());
                isSucc = true;
            }

            @Override
            public void onError(Throwable ex) {
                Log.e(TAG, "下载失败... " + ex != null ? ex.getMessage() : "NULL");
                isSucc = false;
            }

            @Override
            public void onFinished() {
                List<VIPDetail> vipDetails = userDao.queryByFaceId(vipDetail.getFaceId());
                if(vipDetails != null && vipDetails.size() > 0){
                    VIPDetail vipDetail1 = vipDetails.get(0);
                    vipDetail1.setDownloadTag(isSucc);
                    int update = userDao.update(vipDetail1);
                    Log.e(TAG, "更新数据结果... " + update);
                }
                downloadHead(queue);
            }
        });
    }

    private void addToFaceDB(){
        FaceSDK.instance().removeAllUser(new FaceUserManager.FaceUserCallback() {
            @Override
            public void onUserResult(boolean b, int i) {
                Log.e(TAG, "删除全部：" + b + " --- " + i);
            }
        });

        final List<VIPDetail> vipDetails = userDao.selectAll();
        if(vipDetails == null || vipDetails.size() <= 0){
            return;
        }

        for (int i = 0; i < vipDetails.size(); i++) {
            final VIPDetail vipDetail = vipDetails.get(i);
            String imgUrl = vipDetail.getImgUrl();
            File file = new File(imgUrl);
            if(!file.exists()){
                vipDetail.setDownloadTag(false);
                userDao.update(vipDetail);
                continue;
            }

            final int finalI = i + 1;
            FaceSDK.instance().addUser(String.valueOf(vipDetail.getFaceId()), imgUrl, new FaceUserManager.FaceUserCallback() {
                @Override
                public void onUserResult(boolean succ, int resultCode) {
                    Log.e(TAG, "添加结果：" + succ + " --- " + resultCode);
                    if (!succ && resultCode == FaceUserManager.RESULT_FAILURE) {
                        vipDetail.setDownloadTag(false);
                        userDao.update(vipDetail);
                    }
                    setTextProgress(finalI + "/" + vipDetails.size());
                    if(!(finalI < vipDetails.size())){
                        close();
                    }
                }
            });
        }
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

        public UpdateBean(int ctrlType, String head, VIPDetail vipDetail) {
            this.ctrlType = ctrlType;
            this.head = head;
            this.vipDetail = vipDetail;
        }

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
                setErrInfo(err + "，5秒后重试...");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onRetryAfter5s();
                    }
                }, 5 * 1000);
            } else {
                setErrInfo("同步失败，请检查网络连接");
                onFailed();
            }
        }

        @Override
        public void onResponse(String response, int id) {
            d(TAG,response);
            Object o = null;
            if (step == STEP_COMPANY) {
                if(TextUtils.isEmpty(response)){
                    setErrInfo("同步失败，Response is null");
                    onFailed();
                    return;
                }

                if(!isJSONValid(response)){
                    setErrInfo("同步失败，Response is not JSON");
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
                    setErrInfo(err);
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
                    setErrInfo(err);
                    onFailed();
                    return;
                }
                o = staffInfo;
            }
            onSucc(response,(T) o);
        }
    }

    /*======UI显示============================================================================================*/
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
    private boolean isHeadUpdate(String newLocalPath, String oldLocalPath) {//员工头像和本地存储的头像是否冲突一致
        if(!TextUtils.equals(newLocalPath,oldLocalPath)){
            return true;
        }
        File oldFile = new File(oldLocalPath);
        if (!oldFile.exists()) {
            return true;
        }
        return false;
    }

    private boolean isInfoUpdate(VIPDetail newVIPDetail,VIPDetail oldVIPDetail){
        if (!TextUtils.equals(newVIPDetail.getName(),oldVIPDetail.getName())) {
            return true;
        }
        if (newVIPDetail.getEmpId() != oldVIPDetail.getEmpId()) {
            return true;
        }
        if (newVIPDetail.getDepartId() != oldVIPDetail.getDepartId()) {
            return true;
        }
        if (!TextUtils.equals(newVIPDetail.getBirthday(),oldVIPDetail.getBirthday())) {
            return true;
        }
        if (!TextUtils.equals(newVIPDetail.getDepart(),oldVIPDetail.getDepart())) {
            return true;
        }
        if (!TextUtils.equals(newVIPDetail.getJob(),oldVIPDetail.getJob())) {
            return true;
        }
        if (!TextUtils.equals(newVIPDetail.getSignature(),oldVIPDetail.getSignature())) {
            return true;
        }
        if(!TextUtils.equals(newVIPDetail.getSex(),oldVIPDetail.getSex())){
            return true;
        }
        return false;
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


    private void showUI(){
        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(floatSyncView == null){
                    floatSyncView = new FloatSyncView(mAct);
                }
                floatSyncView.show();
                floatSyncView.showProgress(false);
            }
        });
    }

    private void setErrInfo(final String info){
        if(floatSyncView != null){
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    floatSyncView.setErrInfo(info);
                }
            });
        }
    }
    private void showProgress(){
        if(floatSyncView != null){
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    floatSyncView.showProgress(true);
                }
            });
        }
    }
    private void setProgress(final int curr, final int total){
        if(floatSyncView != null){
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    floatSyncView.setDownloadProgress(curr,total);
                }
            });
        }
    }

    private void setInfo(final String info){
        if(floatSyncView != null){
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    floatSyncView.setNormalInfo(info);
                }
            });
        }
    }

    private void setTextProgress(final String progress){
        if(floatSyncView != null){
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    floatSyncView.setTvProgress(progress);
                }
            });
        }
    }

    private void close() {
        SpUtils.saveLong(SpUtils.LAST_INIT_TIME,System.currentTimeMillis());
        EventBus.getDefault().postSticky(new EmployListActivity.EmployUpdate());
        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (floatSyncView != null) {
                    setInfo("同步结束");
                    if(mListener != null){
                        mListener.onFinish();
                    }

                    startTimer(new TimerTask() {
                        @Override
                        public void run() {
                            if(floatSyncView != null){
                                floatSyncView.dismiss();
                            }
                        }
                    }, 3 * 1000);
                }
            }

        });
    }

}
