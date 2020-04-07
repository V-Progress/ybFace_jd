package com.yunbiao.ybsmartcheckin_live_id.business;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.yunbiao.faceview.FaceManager;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.VisitorUpdateEvent;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.UserInfo;
import com.yunbiao.ybsmartcheckin_live_id.db2.Visitor;
import com.yunbiao.ybsmartcheckin_live_id.bean.VisitorResponse;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.xutil.MyXutils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;

public class VisitorManager {
    private static final String TAG = "VisitorManager";
    private static VisitorManager visitorManager = new VisitorManager();
    private final ScheduledExecutorService executor;

    public static VisitorManager getInstance() {
        return visitorManager;
    }

    private VisitorManager() {
        executor = Executors.newSingleThreadScheduledExecutor();
    }
    private boolean isRunning = false;

    public void autoSyncVisitor() {
        if(isRunning){
            return;
        }
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                isRunning = true;
                syncVisitor();
            }
        }, 5, 150, TimeUnit.SECONDS);
    }

    public void syncVisitor() {
        String url = ResourceUpdate.VISITORINFO;
        final int comid = SpUtils.getCompany().getComid();
        Map<String, String> params = new HashMap<>();
        params.put("comId", comid + "");
        Log.e(TAG, "请求地址：" + url);
        Log.e(TAG, "请求参数：" + params.toString());
        OkHttpUtils.post().url(url).params(params).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG, "onError: " + (e == null ? "NULL" : e.getMessage()));
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG, "onResponse: " + response);
                if (TextUtils.isEmpty(response)) {
                    return;
                }

                VisitorResponse visitorResponse = new Gson().fromJson(response, VisitorResponse.class);
                if (visitorResponse == null) {
                    return;
                }

                int status = visitorResponse.getStatus();
                String message = visitorResponse.getMessage();
                Log.e(TAG, "onResponse: " + status + " - " + message);

                List<Visitor> visitor = visitorResponse.getVisitor();
                syncDB(comid, visitor);
            }
        });
    }

    private void syncDB(final int comid, final List<Visitor> visitors) {
        if(visitors == null || visitors.size() <= 0){
            return;
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<Visitor> localList = DaoManager.get().queryVisitorsByCompId(comid);
                Map<String, File> visitorList = FaceManager.getInstance().getVisitorList();
                Log.e(TAG, "run: 更新前数据库中访客总数：" + (localList == null ? 0 : localList.size()));
                Log.e(TAG, "run: 更新前人脸库中访客总数：" + (visitorList == null ? 0 : visitorList.size()));
                Log.e(TAG, "run: 远程访客总数：" + (visitors == null ? 0 : visitors.size()));

                Map<String, Visitor> remoteData = new HashMap<>();
                if (visitors != null) {
                    for (Visitor visitor : visitors) {
                        String head = visitor.getHead();
                        visitor.setHeadPath(Constants.HEAD_PATH + head.substring(head.lastIndexOf("/") + 1));
                        visitor.setComId(comid);
                        remoteData.put(visitor.getFaceId(), visitor);
                        Log.e(TAG, "远程：" + visitor.getFaceId() + " --- " + visitor.getCurrStart() + " --- " + visitor.getCurrEnd());
                    }
                }

                //删除不存在数据
                for (Visitor visitor : localList) {
                    String faceId = visitor.getFaceId();
                    //如果远程数据不包含这个则删除
                    if (!remoteData.containsKey(faceId)) {
                        DaoManager.get().delete(visitor);
                    } else {
                        remoteData.get(faceId).setAddTag(visitor.getAddTag());
                    }
                }
                for (Map.Entry<String, File> entry : visitorList.entrySet()) {
                    if (!remoteData.containsKey(entry.getKey())) {
                        entry.getValue().delete();
                    }
                }

                //更新数据库
                for (Map.Entry<String, Visitor> entry : remoteData.entrySet()) {
                    Visitor value = entry.getValue();
                    DaoManager.get().addOrUpdate(value);
                }

                //检查不存在的头像数据
                Queue<Visitor> visitorQueue = new LinkedList<>();
                List<Visitor> localVisitors = DaoManager.get().queryVisitorsByCompId(comid);
                for (Visitor localVisitor : localVisitors) {
                    String headPath = localVisitor.getHeadPath();
                    File file = new File(headPath);
                    if (TextUtils.isEmpty(headPath) || !file.exists()) {
                        visitorQueue.add(localVisitor);
                    }
                }

                downloadHead(visitorQueue, new DownloadCallback() {
                    @Override
                    public void onSingleComplete(Visitor visitor, File result) {
                        visitor.setAddTag(0);
                        visitor.setHeadPath(result.getPath());
                        DaoManager.get().addOrUpdate(visitor);
                    }

                    @Override
                    public void onSingleFailed(Visitor visitor, Throwable ex) {
                        visitor.setAddTag(-1);
                        DaoManager.get().addOrUpdate(visitor);
                    }

                    @Override
                    public void onFinished() {
                        updateFace();
                    }
                });
            }
        });
    }

    private void updateFace() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Company company = SpUtils.getCompany();
                List<Visitor> visitors = DaoManager.get().queryVisitorsByCompId(company.getComid());
                if(visitors == null){
                    return;
                }

                int size = visitors.size();
                for (int i = 0; i < size; i++) {
                    Visitor visitor = visitors.get(i);
                    String faceId = visitor.getFaceId();
                    String headPath = visitor.getHeadPath();
                    int addTag = visitor.getAddTag();
                    //判断照片是否更新
                    boolean isExists = FaceManager.getInstance().checkFace(faceId);
                    Log.e(TAG, "文件检查结果：" + isExists);

                    //人脸文件存在，并且状态是无需更新时则不继续
                    if(addTag != UserInfo.HEAD_HAS_UPDATE && isExists){
                        Log.e(TAG, "照片未更新且文件存在：" + i + " --- " + faceId);
                        continue;
                    }

                    //添加进库
                    boolean addUser = FaceManager.getInstance().addUser(faceId, headPath);

                    //添加人脸库失败，并且addTag为下载图片失败或添加人脸库失败的时候，无需更新数据库
                    if((!addUser) && (addTag == UserInfo.HEAD_DOWNLOAD_FAILED || addTag == UserInfo.ADD_FACE_DB_FAILED)){
                        Log.e(TAG, "添加失败且无需更新：" + i + " --- " + faceId + " --- " + addTag);
                        continue;
                    }

                    addTag = addUser ? UserInfo.HANDLE_SUCCESS : UserInfo.ADD_FACE_DB_FAILED;
                    visitor.setAddTag(addTag);
                    long l = DaoManager.get().update(visitor);
                    Log.e(TAG, "添加人脸库结果：" + addUser + " --- " + i + " --- " + faceId + " --- 更新数据库结果：" + l);
                }

                FaceManager.getInstance().reloadRegisterList();
                int totalSize = FaceManager.getInstance().getTotalSize();
                Log.e(TAG, "onFinished: 完毕后库中数据：" + totalSize);

                EventBus.getDefault().postSticky(new VisitorUpdateEvent());
            }
        });
    }

    private void downloadHead(final Queue<Visitor> queue, final DownloadCallback callback) {
        if (queue == null || queue.size() <= 0) {
            callback.onFinished();
            return;
        }

        final Visitor visitor = queue.poll();
        Log.e(TAG, "下载：" + visitor.getName() + " —— " + visitor.getHead());

        String headUrl = visitor.getHead();
        MyXutils.getInstance().downLoadFile(headUrl, visitor.getHeadPath(), false, new MyXutils.XDownLoadCallBack() {
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                Log.e(TAG, "下载进度... " + current + " —— " + total);
            }

            @Override
            public void onSuccess(File result) {
                Log.e(TAG, "下载成功... " + result.getPath());
                callback.onSingleComplete(visitor,result);
            }

            @Override
            public void onError(Throwable ex) {
                Log.e(TAG, "下载失败... " + (ex != null ? ex.getMessage() : "NULL"));
                callback.onSingleFailed(visitor,ex);
            }

            @Override
            public void onFinished() {
                downloadHead(queue, callback);
            }
        });
    }

    private interface DownloadCallback {
        void onSingleComplete(Visitor visitor, File result);

        void onSingleFailed(Visitor visitor, Throwable ex);

        void onFinished();
    }

}
