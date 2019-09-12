package com.yunbiao.ybsmartcheckin_live_id.business;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.jdjr.risk.face.local.user.FaceUser;
import com.jdjr.risk.face.local.user.FaceUserManager;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.activity.EmployListActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.SyncCompleteEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateLogoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateQRCodeEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateUserDBEvent;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.bean.CompanyResponse;
import com.yunbiao.ybsmartcheckin_live_id.bean.StaffResponse;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Depart;
import com.yunbiao.ybsmartcheckin_live_id.db2.User;
import com.yunbiao.ybsmartcheckin_live_id.faceview.face_new.FaceSDK;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.xutil.MyXutils;
import com.yunbiao.ybsmartcheckin_live_id.views.SyncDialog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import timber.log.Timber;

/**
 * Created by Administrator on 2019/5/14.
 */

public class SyncManager {

    private static SyncManager instance;
    private boolean isLocalServ = false;
    private int mUpdateTotal = 0;//更新总数
    private boolean isFirst = true;

    private long initOffset = 2 * 24 * 60 * 60 * 1000;//更新间隔时间7天

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
        if (!file.exists()) {
            file.mkdirs();
        }
        String webBaseUrl = ResourceUpdate.WEB_BASE_URL;
        String[] split = webBaseUrl.split(":");
        for (String s : split) {
            if (s.startsWith("192.168")) {
                isLocalServ = true;
            }
        }

        SyncDialog.instance().init(APP.getActivity());
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

    public void requestCompany() {
        SyncDialog.instance().show();
        SyncDialog.instance().setStep("获取公司信息");
        d("获取公司信息");
        OkHttpUtils.post()
                .url(ResourceUpdate.COMPANYINFO)
                .addParams("deviceNo", HeartBeatClient.getDeviceNo())
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                d(e);
                if (isFirst) {//如果是第一次则加载缓存
                    EventBus.getDefault().post(new UpdateInfoEvent());
                    isFirst = false;
                }
                SyncDialog.instance().setStep("将在5秒后重试");
                retryGetCompany();
            }

            @Override
            public void onResponse(String response, int id) {
                d(response);
                if (TextUtils.isEmpty(response)) {
                    retryGetCompany();
                    return;
                }
                CompanyResponse companyResponse = new Gson().fromJson(response, CompanyResponse.class);
                if (companyResponse == null) {
                    retryGetCompany();
                    return;
                }

                if (companyResponse.getStatus() != 1) {
                    retryGetCompany();
                    return;
                }

                //存公司ID
                SpUtils.saveInt(SpUtils.COMPANYID, companyResponse.getCompany().getComid());
                SpUtils.setCompany(companyResponse.getCompany());
                d("缓存公司信息");

                //初始化存储路径
                Constants.initStorage();
                d("初始化存储路径");

                //发送系统信息更新事件
                EventBus.getDefault().post(new UpdateInfoEvent());
                d("发送系统信息更新事件");

                d("开始同步部门数据");
                //同步部门数据
                Company company = companyResponse.getCompany();
                syncDepart(company);

                //加载logo
                loadLogo(company);

                //加载二维码
                loadQRCode(company);

                syncDB();
            }
        });
    }

    private void loadQRCode(Company company) {
        String codeUrl = company.getCodeUrl();
        File code = new File(codeUrl);
        File codeFile = new File(Constants.DATA_PATH , "qrcode_" + code.getName());
        Log.e(TAG, "loadQRCode: " + codeFile.getPath() + " --- " + codeFile.getTotalSpace());
        if (codeFile.exists()) {
            Log.e(TAG, "loadQRCode: 存在");
            SpUtils.saveStr(SpUtils.COMPANY_QRCODE, codeFile.getPath());
            EventBus.getDefault().post(new UpdateQRCodeEvent(codeFile.getPath()));
        } else {
            Log.e(TAG, "loadQRCode: 不存在");
            MyXutils.getInstance().downLoadFile(codeUrl, codeFile.getPath(), false, new MyXutils.XDownLoadCallBack() {
                String path;

                @Override
                public void onLoading(long total, long current, boolean isDownloading) {

                }

                @Override
                public void onSuccess(File result) {
                    d("下载完毕");
                    path = result.getPath();
                }

                @Override
                public void onError(Throwable ex) {
                    d("下载失败");
                }

                @Override
                public void onFinished() {
                    d("发送logo更新事件");
                    SpUtils.saveStr(SpUtils.COMPANY_QRCODE, path);
                    EventBus.getDefault().post(new UpdateQRCodeEvent(path));
                }
            });
        }
    }

    private void loadLogo(Company company) {
        d("加载部门logo");
        String comlogo = company.getComlogo();
        File logo = new File(comlogo);

        File logoFile = new File(Constants.DATA_PATH, "logo_" + logo.getName());
        if (logoFile.exists()) {
            d("logo存在，发送更新事件");
            SpUtils.saveStr(SpUtils.COMPANY_LOGO, logoFile.getPath());
            EventBus.getDefault().post(new UpdateLogoEvent(logoFile.getPath()));
        } else {
            MyXutils.getInstance().downLoadFile(comlogo, logoFile.getPath(), false, new MyXutils.XDownLoadCallBack() {
                String path;

                @Override
                public void onLoading(long total, long current, boolean isDownloading) {
                }

                @Override
                public void onSuccess(File result) {
                    d("下载完毕");
                    path = result.getPath();
                }

                @Override
                public void onError(Throwable ex) {
                    d("下载失败");
                }

                @Override
                public void onFinished() {
                    d("发送logo更新事件");
                    SpUtils.saveStr(SpUtils.COMPANY_LOGO, path);
                    EventBus.getDefault().post(new UpdateLogoEvent(path));
                }
            });
        }
    }

    private void retryGetCompany() {
        d("重新获取公司信息");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestCompany();
            }
        }, 10 * 1000);
    }

    private void retryGetUser() {
        d("重新获取员工信息");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                syncDB();
            }
        }, 10 * 1000);
    }

    public void syncDB() {
        d("请求员工信息");
        final int comid = SpUtils.getInt(SpUtils.COMPANYID);
        OkHttpUtils.post()
                .url(ResourceUpdate.GETSTAFF)
                .addParams("companyId", comid + "")
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                d(e);
                retryGetUser();
            }

            @Override
            public void onResponse(String response, int id) {
                d(response);
                if (TextUtils.isEmpty(response)) {
                    retryGetUser();
                    return;
                }

                StaffResponse staffResponse = new Gson().fromJson(response, StaffResponse.class);
                if (staffResponse == null) {
                    retryGetUser();
                    return;
                }

                if (staffResponse.getStatus() != 1) {
                    retryGetUser();
                    return;
                }

                List<Depart> dep = staffResponse.getDep();
                syncUser(comid, dep);
            }
        });
    }

    //同步部门数据库
    private void syncDepart(final Company company) {
        SyncDialog.instance().setStep("同步部门");
        if (company == null) {
            return;
        }
        List<Depart> deparray = company.getDeparray();
        if (deparray == null) {
            return;
        }
        for (Depart depart : deparray) {
            depart.setCompId(company.getComid());
            depart.setId(depart.getDepId());
            DaoManager.get().addOrUpdate(depart);
        }
    }

    private void syncUser(int comid, List<Depart> dep) {
        SyncDialog.instance().setStep("同步员工信息");
        d("同步员工信息");
        if (dep == null) {
            SyncDialog.instance().dismiss();
            d("结束，列表为null");
            return;
        }
        //生成远程数据
        Map<Long, User> remoteAllUser = getRemoteAllUser(dep);
        mUpdateTotal = remoteAllUser.size();
        d("远程数据：" + mUpdateTotal);

        //获取本地数据
        List<User> localDatas = DaoManager.get().queryAll(User.class);
        d("本地数据：" + localDatas.size());

        //对比删除
        compareUser(localDatas, remoteAllUser);
        //更新用户库
        updateUser(comid, remoteAllUser);
        //获取头像更新队列
        Queue<User> headUpdateQueue = getHeadUpdateQueue();
        //开始下载
        startDownload(headUpdateQueue, new Runnable() {
            @Override
            public void run() {
                checkFaceDB(new Runnable() {
                    @Override
                    public void run() {
                        SyncDialog.instance().dismiss();
                        EventBus.getDefault().post(new SyncCompleteEvent());
                    }
                });
            }
        });
    }

    private void checkFaceDB(Runnable runnable) {
        d("检查人脸库----------------------------");
        SyncDialog.instance().setStep("同步人脸库");
        //取出数据库中的数据和人脸库中的数据并做对比
        final List<User> userBeans = DaoManager.get().queryAll(User.class);
        Map<String, FaceUser> allUserMap = FaceSDK.instance().getAllFaceData();
        d("人脸库：" + allUserMap.size() + "，数据库：" + userBeans.size());

        if (userBeans == null || userBeans.size() <= 0) {
            FaceSDK.instance().removeAllUser(new FaceUserManager.FaceUserCallback() {
                @Override
                public void onUserResult(boolean b, int i) {
                    d("删除全部员工结果：" + b + " —— " + i);
                }
            });
        } else {
            for (int i = 0; i < userBeans.size(); i++) {
                final User userBean = userBeans.get(i);
                SyncDialog.instance().setProgress(i + 1, userBeans.size());
                final long faceId = userBean.getFaceId();
                String headPath = userBean.getHeadPath();
                if (TextUtils.isEmpty(headPath) || !new File(headPath).exists()) {
                    d("头像不存在，跳过：" + userBean.getName());
                    continue;
                }

                String userId = String.valueOf(faceId);
                if (allUserMap.containsKey(userId)) {
                    FaceUser faceUser = allUserMap.get(userId);
                    String imagePath = faceUser.getImagePath();
                    if (!TextUtils.equals(imagePath, headPath)) {
                        faceUser.setImagePath(headPath);
                        FaceSDK.instance().update(faceUser, new FaceUserManager.FaceUserCallback() {
                            @Override
                            public void onUserResult(boolean b, int i) {
                                d("更新：" + userBean.getName() + ", FaceId：" + faceId + "，结果：" + b + "," + i);
                            }
                        });
                    }
                } else {
                    FaceSDK.instance().addUser(String.valueOf(faceId), headPath, new FaceUserManager.FaceUserCallback() {
                        @Override
                        public void onUserResult(boolean b, int i) {
                            d("添加：" + userBean.getName() + ", FaceId：" + faceId + "，结果：" + b + "," + i);
                        }
                    });
                }
            }
        }

        runnable.run();
    }

    private int mCurrDownloadIndex = 0;//当前索引

    private void startDownload(Queue<User> updateQueue, final Runnable runnable) {
        d("下载头像----------------------------");
        mUpdateTotal = updateQueue.size();
        SyncDialog.instance().setProgress(mCurrDownloadIndex, mUpdateTotal);
        downloadHead(updateQueue, new Runnable() {
            @Override
            public void run() {
                mCurrDownloadIndex = 0;
                runnable.run();
            }
        });
    }

    private void downloadHead(final Queue<User> queue, final Runnable runnable) {
        if (queue == null || queue.size() <= 0) {
            runnable.run();
            return;
        }
        mCurrDownloadIndex++;
        SyncDialog.instance().setProgress(mCurrDownloadIndex, mUpdateTotal);

        final User userBean = queue.poll();
        d("下载：" + userBean.getName() + " —— " + userBean.getHead());
        if (!TextUtils.isEmpty(userBean.getHeadPath()) && new File(userBean.getHeadPath()).exists()) {
            d("头像存在");
            downloadHead(queue, runnable);
            return;
        }

        String headUrl = userBean.getHead();
        MyXutils.getInstance().downLoadFile(headUrl, userBean.getHeadPath(), false, new MyXutils.XDownLoadCallBack() {
            int downloadTag = 0;

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                Log.e(TAG, "下载进度... " + current + " —— " + total);
            }

            @Override
            public void onSuccess(File result) {
                Log.e(TAG, "下载成功... " + result.getPath());
                downloadTag = 0;
                userBean.setHeadPath(result.getPath());
            }

            @Override
            public void onError(Throwable ex) {
                d("下载失败... " + (ex != null ? ex.getMessage() : "NULL"));
                downloadTag = -1;
            }

            @Override
            public void onFinished() {
                userBean.setAddTag(downloadTag);
                long l = DaoManager.get().addOrUpdate(userBean);
                d("更新数据结果... " + l);
                downloadHead(queue, runnable);
            }
        });
    }


    private Map<Long, User> getRemoteAllUser(List<Depart> dep) {
        d("检查所有员工----------------------------");
        //生成云端数据的所有员工
        Map<Long, User> remoteDatas = new HashMap<>();
        for (Depart bean : dep) {
            List<User> entry = bean.getEntry();
            for (User user : entry) {
                user.setDepartId(bean.getDepId());
                user.setDepartName(bean.getDepName());
                String head = user.getHead();
                if (!TextUtils.isEmpty(head)) {
                    String filepath = Constants.HEAD_PATH + head.substring(head.lastIndexOf("/") + 1);
                    user.setHeadPath(filepath);
                }

                d(user.toString());
                remoteDatas.put(user.getId(), user);
            }
        }
        return remoteDatas;
    }

    private void compareUser(List<User> localDatas, Map<Long, User> remoteDatas) {
        d("开始数据对比----------------------------");
        d("本地数据：" + localDatas.size() + ", 远程数据：" + remoteDatas.size());
        //检查已删除
        if (localDatas != null) {
            for (User localData : localDatas) {
                long id = localData.getId();
                //如果远程数据不包含这个id，则本地库删除
                if (!remoteDatas.containsKey(id)) {
                    d("准备删除：" + localData.toString());
                    String headPath = localData.getHeadPath();
                    if (!TextUtils.isEmpty(headPath)) {
                        boolean delete = new File(headPath).delete();
                        d("删除头像：" + delete);
                    }
                    long faceId = localData.getFaceId();
                    boolean b = FaceSDK.instance().removeUser(String.valueOf(faceId));
                    d("删除人脸：" + b);
                    long delete = DaoManager.get().delete(localData);
                    d("删除数据：" + delete);
                }
            }
        }
    }

    private void updateUser(int comid, Map<Long, User> remoteDatas) {
        d("更新员工信息----------------------------");
        //先更新信息
        for (Map.Entry<Long, User> entry : remoteDatas.entrySet()) {
            User remoteBean = entry.getValue();
            remoteBean.setCompanyId(comid);
            long l = DaoManager.get().addOrUpdate(remoteBean);
            d("更新结果：" + l);
        }
        EventBus.getDefault().post(new UpdateUserDBEvent());
    }

    private Queue<User> getHeadUpdateQueue() {
        d("检查头像数据----------------------------");
        Queue<User> updateList = new LinkedList<>();
        //检查头像
        List<User> localDatas = DaoManager.get().queryAll(User.class);
        for (User localData : localDatas) {
            String headPath = localData.getHeadPath();
            File file = new File(headPath);
            if (!file.exists()) {
                updateList.add(localData);
            }
        }
        for (User userBean : updateList) {
            d(userBean.toString());
        }
        return updateList;
    }

    public void destory() {
        OkHttpUtils.getInstance().cancelTag(this);
//        if (floatSyncView != null) {
//            floatSyncView.dismiss();
//            floatSyncView = null;
//        }
        SyncDialog.instance().dismiss();
    }

    /*======UI显示============================================================================================*/
    private Timer timer;

    private void startTimer(TimerTask timerTask, long delay) {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(timerTask, delay);
    }

    private void cancelTimer() {
        if (timer != null) {
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

    public final static boolean isJSONValid(String jsonInString) {
        try {
            new Gson().fromJson(jsonInString, Object.class);
            return true;
        } catch (JsonSyntaxException ex) {
            return false;
        }
    }


//    private void showUI() {
//        mAct.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (floatSyncView == null) {
//                    floatSyncView = new FloatSyncView(mAct);
//                }
//                floatSyncView.show();
//                floatSyncView.showProgress(false);
//            }
//        });
//    }
//
//    private void setErrInfo(final String info) {
//        if (floatSyncView != null) {
//            mAct.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    floatSyncView.setErrInfo(info);
//                }
//            });
//        }
//    }
//
//    private void showProgress() {
//        if (floatSyncView != null) {
//            mAct.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    floatSyncView.showProgress(true);
//                }
//            });
//        }
//    }
//
//    private void setProgress(final int curr, final int total) {
//        if (floatSyncView != null) {
//            mAct.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    floatSyncView.setDownloadProgress(curr, total);
//                }
//            });
//        }
//    }
//
//    private void setInfo(final String info) {
//        if (floatSyncView != null) {
//            mAct.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    floatSyncView.setNormalInfo(info);
//                }
//            });
//        }
//    }
//
//    private void setTextProgress(final String progress) {
//        if (floatSyncView != null) {
//            mAct.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    floatSyncView.setTvProgress(progress);
//                }
//            });
//        }
//    }
//
//    private void close() {
//        SpUtils.saveLong(SpUtils.LAST_INIT_TIME, System.currentTimeMillis());
//        EventBus.getDefault().postSticky(new EmployListActivity.EmployUpdate());
//        mAct.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (floatSyncView != null) {
//                    setInfo("同步结束");
//                    startTimer(new TimerTask() {
//                        @Override
//                        public void run() {
//                            if (floatSyncView != null) {
//                                floatSyncView.dismiss();
//                            }
//                        }
//                    }, 3 * 1000);
//                }
//            }
//
//        });
//    }

    private void d(String log) {
        Timber.tag(this.getClass().getSimpleName());
        Timber.d(log);
    }

    private void d(Throwable t) {
        Timber.tag(this.getClass().getSimpleName());
        Timber.d(t);
    }
}
