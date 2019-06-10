package com.yunbiao.ybsmartcheckin_live_id.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.adapter.VisitorAdapter;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.bean.AddQRCodeBean;
import com.yunbiao.ybsmartcheckin_live_id.bean.CompanyBean;
import com.yunbiao.ybsmartcheckin_live_id.business.AdsManager;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.business.LocateManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.business.VipDialogManager;
import com.yunbiao.ybsmartcheckin_live_id.business.WeatherManager;
import com.yunbiao.ybsmartcheckin_live_id.business.sign.MultipleSignDialog;
import com.yunbiao.ybsmartcheckin_live_id.db.VIPDetail;
import com.yunbiao.ybsmartcheckin_live_id.exception.CameraError;
import com.yunbiao.ybsmartcheckin_live_id.faceview.FaceView;
import com.yunbiao.ybsmartcheckin_live_id.heartbeat.BaseGateActivity;
import com.yunbiao.ybsmartcheckin_live_id.serialport.plcgate.GateCommands;
import com.yunbiao.ybsmartcheckin_live_id.utils.RestartAPPTool;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.xutil.MyCallBack;
import com.yunbiao.ybsmartcheckin_live_id.xmpp.ServiceManager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

import static com.yunbiao.ybsmartcheckin_live_id.APP.getContext;

/**
 * Created by Administrator on 2018/11/26.
 */

public class WelComeActivity extends BaseGateActivity/* implements ServiceConnection*/ {

    private static final String TAG = "WelComeActivity";
    private ImageView iv_logo;//首页头部logo
    private TextView tv_title;//首页头部公司名称

    private TextView tv_tem;//首页温度
    private ImageView iv_wea;//首页天气
    private TextView tv_deviceNo;//首页设备号

    private TextView tv_checkInNum;//签到页的签到人数统计
    private GridView gridview;//签到页头像列表

    private ImageView imageView;//公司logo
    private ImageView iv_record;//补录
    private TextView tv_comName;//公司名
    private TextView tv_notice;//公司提醒
    private TextView tv_topTitle;//标题
    private TextView tv_bottomTitle;//底部标题

    private View ll_load_container;//加载条
    private TextView tv_load_error;//加载提示
    private View aiv_bulu;//补录加载条

    private View layout_head;
    private View layout_wel;
    private View iv_yuan;
    private View iv_line;

    private TextView tvCameraError;
    private ImageView ivQrCodeAdd;

    private Intent mIntent;
    private IntentFilter intentFilter;

    private static String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String SCREEN_BASE_PATH = sdPath + "/mnt/sdcard/photo/";//人脸头像存储路径
    public static String SCREEN_BASE_PATH_IMGS = sdPath + "/mnt/sdcard/videos/";//广告视频图片存储路径

    private BaseAdapter mVisitorAdapter;//签到人员adapter
    private PieChart pieChart_h;//横屏饼图

    // xmpp推送服务
    private ServiceManager serviceManager;

    private String today = "";//获取今天时间
    private boolean isBulu = false;//获取补录头像
    private String yuyin = " 您好 %s";

    private boolean isForeground = false;//是否前台显示
    private int mCurrentOrientation = 1;//横竖屏状态

    //摄像头分辨率
    private int cameraState;
    private FaceView faceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCurrentOrientation = getResources().getConfiguration().orientation;
        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {//竖屏
            setContentView(R.layout.activity_welcome);
        } else {//横屏
            setContentView(R.layout.activity_welcome_h);
            initPieChart();
        }
        //注册EvntBus
        EventBus.getDefault().register(this);

        initViews();

        initData();

        //检查摄像头
        cameraState = checkCamera();

        //开启Xmpp
        startXmpp();

        //初始化定位工具
        LocateManager.instance().init(this);

        WeatherManager.instance().start(WelComeActivity.this, new WeatherManager.ResultListener() {
            @Override
            public void updateWeather(int id, String weatherInfo) {
                iv_wea.setImageResource(R.mipmap.icon_snow);
                tv_tem.setText(weatherInfo);
            }
        });

        //开始初始化动画
        startInitAnim();

        //初始化语音系统
        KDXFSpeechManager.instance().init(this).welcome(new KDXFSpeechManager.VoicePlayListener() {
            @Override
            public void playComplete(String uttId) {
                closeInitView(null);
            }
        });

        // TODO: 2019/6/10 多人
//        MultipleSignDialog.instance().init(this);
//        SignManager2.instance().init(WelComeActivity.this, new SignManager2.SignEventListener() {
//            @Override
//            public void onPrepared(List<SignBean> mList) {
//                if (mList == null) {
//                    return;
//                }
//
////                mVisitorAdapter = new VisitorAdapter(WelComeActivity.this, mList, mCurrentOrientation);
////                gridview.setAdapter(mVisitorAdapter);
////
////                updateList(mList);
//
//                tv_load_error.setVisibility(View.GONE);
//                ll_load_container.setVisibility(View.GONE);
//                gridview.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onSigned(List<SignBean> mList, SignBean signBean, int signType) {
////                updateList(mList);
//                speak(signType,signBean.getName());
//                MultipleSignDialog.instance().sign(signBean);
//
//                if (mGateIsAlive) {
//                    mGateConnection.writeCom(GateCommands.GATE_OPEN_DOOR);
//                }
//            }
//        });


        //开始定时更新签到列表
        SignManager.instance().init(WelComeActivity.this, new SignManager.SignEventListener() {
            @Override
            public void onPrepared(List<VIPDetail> mList) {
                if (mList == null) {
                    return;
                }

                mVisitorAdapter = new VisitorAdapter(WelComeActivity.this, mList, mCurrentOrientation);
                gridview.setAdapter(mVisitorAdapter);

                updateList(mList);

                tv_load_error.setVisibility(View.GONE);
                ll_load_container.setVisibility(View.GONE);
                gridview.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSigned(List<VIPDetail> mList, String signerName, int signType) {
                updateList(mList);

                if (mGateIsAlive) {
                    mGateConnection.writeCom(GateCommands.GATE_OPEN_DOOR);
                }

                speak(signType, signerName);

                if (!AdsManager.instance().isAdsShowing()) {
                    VipDialogManager.showVipDialog(WelComeActivity.this, today, mList.get(0));
                }
            }
        });

        faceView.setCallback(new FaceView.FaceCallback() {
            @Override
            public void onReady() {
                Log.e(TAG, "onReady: 11111111111111111111111");
                syncData();
            }
            @Override
            public void onFaceDetection() {
                if (!AdsManager.instance().isAniming()) {
                    //如果广告可见，收起广告
                    if (AdsManager.instance().isAdsShowing()) {
                        AdsManager.instance().openAds();
                    } else {
                        //重置倒计时
                        AdsManager.instance().startTimer();
                    }
                }
            }

            @Override
            public void onFaceVerify(FaceView.FaceVerifyResult verifyResult) {
                if(verifyResult == null){
                    return;
                }
                String userId = verifyResult.getUserId();
                if(TextUtils.isEmpty(userId)){
                    return;
                }

                Log.e(TAG, "onFaceVerify: -----------" + userId);

                // TODO: 2019/6/10 多人
//                long time = System.currentTimeMillis();
//                SignManager2.instance().sign(Integer.valueOf(userId),faceView.getFaceImage(),time);

                //正常签到
                try {
                    List<VIPDetail> mainList = new ArrayList<>();
                    final List<VIPDetail> mlist = SyncManager.instance().getUserDao().queryByFaceId(Integer.valueOf(userId));
                    if (mlist != null && mlist.size() > 0) {
                        long time = System.currentTimeMillis();
                        VIPDetail vip = mlist.get(0);
                        byte[] faceImage = faceView.getFaceImage();
                        if(faceImage != null && faceImage.length > 0){
                            final BitmapFactory.Options options = new BitmapFactory.Options();
                            final Bitmap image = BitmapFactory.decodeByteArray(faceImage, 0, faceImage.length, options);
                            vip.setBitmap(image);
                        }

                        vip.setTime(time);
                        mainList.add(vip);
                    }

                    SignManager.instance().sign(mainList);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void initViews() {
        faceView = findViewById(R.id.face_view);
        iv_logo = (ImageView) findViewById(R.id.iv_logo);
        tv_title = (TextView) findViewById(R.id.tv_title);
        ll_load_container = findViewById(R.id.ll_load_container);
        tv_load_error = (TextView) findViewById(R.id.tv_load_error);
        gridview = (GridView) findViewById(R.id.gridview);
        aiv_bulu = findViewById(R.id.aiv_bulu);
        tv_tem = (TextView) findViewById(R.id.tv_tem);
        iv_wea = (ImageView) findViewById(R.id.iv_wea);
        tv_deviceNo = (TextView) findViewById(R.id.tv_deviceNo);
        tv_checkInNum = (TextView) findViewById(R.id.tv_checkInNum);
        imageView = (ImageView) findViewById(R.id.imageView_logo);
        iv_record = (ImageView) findViewById(R.id.iv_record);
        tv_comName = (TextView) findViewById(R.id.tv_comName);
        tv_notice = (TextView) findViewById(R.id.tv_notice);
        tv_topTitle = (TextView) findViewById(R.id.tv_topTitle);
        tv_bottomTitle = (TextView) findViewById(R.id.tv_bottomTitle);
        tvCameraError = (TextView) findViewById(R.id.tv_camera_error);
        ivQrCodeAdd = (ImageView) findViewById(R.id.iv_qrCode_add);

        layout_head = findViewById(R.id.layout_head);
        layout_wel = findViewById(R.id.layout_wel);
        iv_yuan = findViewById(R.id.iv_yuan);
        iv_line = findViewById(R.id.iv_line);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String pwd = SpUtils.getString(WelComeActivity.this, SpUtils.MENU_PWD, "");
                String pwd = SpUtils.getStr(SpUtils.MENU_PWD);
                if (!TextUtils.isEmpty(pwd)) {
                    inputPwd();
                    return;
                }
                startActivity(new Intent(WelComeActivity.this, SystemActivity.class));
            }
        });
        iv_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBulu = true;
                aiv_bulu.setVisibility(View.VISIBLE);
                iv_record.setVisibility(View.GONE);
            }
        });
    }

    //初始化变量
    private void initData() {
        //注册摄像头监听广播
        intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(cameraReceiver, intentFilter);

        //设置设备编号
        String deviceSernum = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
        if (!TextUtils.isEmpty(deviceSernum)) {
            tv_deviceNo.setText(deviceSernum);
        }
    }

    private void loadQrCode(CompanyBean bean) {
        Map<String, String> params = new HashMap();
        params.put("comId", bean.getCompany().getComid() + "");
        OkHttpUtils.post().url(ResourceUpdate.QRCODE_ADD).params(params).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
            }

            @Override
            public void onResponse(String response, int id) {
                if (TextUtils.isEmpty(response)) {
                    return;
                }
                AddQRCodeBean addQRCodeBean = new Gson().fromJson(response, AddQRCodeBean.class);
                if (!TextUtils.equals(addQRCodeBean.status, "1")) {
                    return;
                }
                if (addQRCodeBean == null || TextUtils.isEmpty(addQRCodeBean.codeurl)) {
                    return;
                }
                if (Util.isOnMainThread()) {
                    ivQrCodeAdd.setVisibility(View.VISIBLE);
                    Glide.with(getContext()).load(addQRCodeBean.codeurl).override(150, 150).crossFade(1600).into(ivQrCodeAdd);
                }
            }
        });
    }

    private void syncData(){
        SyncManager.instance()
                .init(WelComeActivity.this)
                .setListener(new SyncManager.LoadListener() {
                    @Override
                    public void onLoaded(CompanyBean bean) {
                        tv_comName.setText(bean.getCompany().getAbbname());
                        tv_title.setText(bean.getCompany().getAbbname());
                        tv_notice.setText(bean.getCompany().getNotice());
                        tv_topTitle.setText(bean.getCompany().getToptitle());
                        tv_bottomTitle.setText(bean.getCompany().getBottomtitle());
                        Glide.with(WelComeActivity.this)
                                .load(bean.getCompany().getComlogo())
                                .skipMemoryCache(true)
                                .crossFade(500)
                                .into(iv_logo);
                        Glide.with(WelComeActivity.this)
                                .load(bean.getCompany().getComlogo())
                                .skipMemoryCache(true)
                                .crossFade(500)
                                .into(imageView);

                        loadQrCode(bean);

                        EventBus.getDefault().postSticky(new SystemActivity.UpdateEvent());
                    }

                    @Override
                    public void onFinish() {
                        AdsManager.instance().init(WelComeActivity.this, null);
                    }
                });
    }

    private void showTips(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UIUtils.showTitleTip(msg);
            }
        });
    }
    
//    /***
//     * 人脸检测Handler
//     */
//    private class CheckFaceHandler extends Handler implements MipsFaceService.PoseCallBack {
//        private mipsFaceInfoTrackLiveness[] faceInfo;
//        private boolean isCheckThreadRunning = false;
//        private static final int MAX_CHECK_LIVENESS_TIME = 3;//活体检测最大时间（单位：秒）
//        private static final int MAX_CHECK_FACE_TIME = 3;//人脸检测最大时间（单位：秒）
//        private static final int MAX_HANDLER_RUNNING_TIME = 10;//Handler运行时长（单位：秒）
//        private int checkHandlerRunningTime = MAX_HANDLER_RUNNING_TIME * 4;//检测handler运行时长
//        private int checkLivenessOutTime = MAX_CHECK_LIVENESS_TIME * 4;//检测超时时长
//        private int checkFaceOutTime = MAX_CHECK_FACE_TIME * 4;//检测超时时长
//
//        @Override
//        public void onPosedetected(final String flag, int curFaceCnt, int cntFaceDB, final mipsFaceInfoTrackLiveness[] faceInfo) {
//            this.faceInfo = faceInfo;
//            if (!isCheckThreadRunning) {
//                sendEmptyMessage(0);
//            }
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            isCheckThreadRunning = true;
//            if (!isForeground) {
//                isCheckThreadRunning = false;
//                return;
//            }
//
//            if (hasFace()) {
//                checkHandlerRunningTime = MAX_HANDLER_RUNNING_TIME * 4;
//                //如果动画正在播放，不响应检测
//                if (!AdsManager.instance().isAniming()) {
//                    //如果广告可见，收起广告
//                    if (AdsManager.instance().isAdsShowing()) {
//                        AdsManager.instance().openAds();
//                    } else {
//                        //重置倒计时
//                        AdsManager.instance().startTimer();
//                        List<VIPDetail> mainList = new ArrayList<>();
//                        for (int i = 0; i < faceInfo.length; i++) {
//                            if (faceInfo[i] != null) {//活体已开启并且是活体
//                                if (faceInfo[i].mfaceFeature == null) {
//                                    continue;
//                                }
//                                if (faceInfo[i].mfaceFeature.mBitmapFace == null) {
//                                    continue;
//                                }
//
//                                Log.e(TAG, "handleMessage: ---------" + faceInfo[i].FaceIdxDB + "--------" +checkFaceOutTime);
//                                if(faceInfo[i].flgSetVIP <= 0){
//                                    checkFaceOutTime--;
//                                    if(checkFaceOutTime <= 0){
//                                        checkFaceOutTime = MAX_CHECK_FACE_TIME * 4;
//                                        tvCameraError.setVisibility(View.VISIBLE);
//                                        tvCameraError.setText("检测失败\n请检查员工照片\n或将面部移出画面后重试");
//                                    }
//                                    continue;
//                                }
//
//                                if (APP.isLiveness && faceInfo[i].flgLiveness != 1) {
//                                    checkLivenessOutTime--;
//                                    if(checkLivenessOutTime <= 0){
//                                        Log.e(TAG, "handleMessage: 检测活体超时");
//                                        checkLivenessOutTime = MAX_CHECK_LIVENESS_TIME * 4;
//                                        tvCameraError.setVisibility(View.VISIBLE);
//                                        tvCameraError.setText("面部认证失败\n请将面部移出画面后再重试");
//                                    }
//                                    continue;
//                                }
//                                checkFaceOutTime = MAX_CHECK_FACE_TIME * 4;
//                                checkLivenessOutTime = MAX_CHECK_LIVENESS_TIME * 4;
//                                tvCameraError.setVisibility(View.GONE);
//                                //正常签到
//                                try {
//                                    final List<VIPDetail> mlist = SyncManager.instance().getUserDao().queryByFaceId(faceInfo[i].FaceIdxDB);
//                                    if (mlist != null && mlist.size() > 0) {
//                                        long time = System.currentTimeMillis();
//                                        VIPDetail vip = mlist.get(0);
//                                        if (faceInfo[i].mfaceFeature.mBitmapFace != null) {
//                                            Bitmap bmp = faceInfo[i].mfaceFeature.mBitmapFace;
//                                            vip.setBitmap(bmp);
//                                        }
//
//                                        vip.setTime(time);
//                                        mainList.add(vip);
//                                    }
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//
//                                //补录
//                                if (faceInfo[i] != null && faceInfo[i].flgSetVIP == 1) {
//                                    if (faceInfo[i].mfaceFeature.mBitmapFace != null) {
//                                        if (isBulu) {
//                                            goToBuLu(faceInfo[i].mfaceFeature.mBitmapFace);
//                                            isBulu = false;
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                        SignManager.instance().sign(mainList);
//                    }
//                }
//            } else {
//                tvCameraError.setVisibility(View.GONE);
//                checkHandlerRunningTime--;
//                if (checkHandlerRunningTime <= 0) {
//                    Log.e(TAG, "handleMessage: 到达十秒");
//                    checkHandlerRunningTime = MAX_HANDLER_RUNNING_TIME;
//                    isCheckThreadRunning = false;
//                    removeMessages(0);
//                    return;
//                }
//            }
//            sendEmptyMessageDelayed(0, 250);
//        }
//
//        //检查人脸
//        private boolean hasFace() {
//            boolean is = false;
//            if (faceInfo != null) {
//                for (int i = 0; i < faceInfo.length; i++) {
//                    if (faceInfo[i] != null) {
//                        is = true;
//                    }
//                }
//            }
//            return is;
//        }
//    }

    private void goToBuLu(final Bitmap bitmap) {
//        int companyid = SpUtils.getInt(WelComeActivity.this, SpUtils.COMPANYID, 0);
        int companyid = SpUtils.getInt(SpUtils.COMPANYID);
        final Map<String, String> map = new HashMap<>();
        map.put("comId", companyid + "");
        String strFileAdd = saveBitmap(bitmap);
        map.put("head", strFileAdd);
        File imgFile = new File(strFileAdd);
        if (imgFile != null && imgFile.exists()) {
            RequestParams params = new RequestParams(ResourceUpdate.BULUSIGN);
            params.addBodyParameter("comId", companyid + "");
            params.addBodyParameter("head", imgFile);

            x.http().post(params, new MyCallBack<String>() {
                @Override
                public void onSuccess(String result) {
                    super.onSuccess(result);
                    Log.d(TAG, "补录成功--->" + result);
                    VipDialogManager.showBuluDialog(WelComeActivity.this, bitmap);
                    KDXFSpeechManager.instance().playText("补录成功！");
                    aiv_bulu.setVisibility(View.GONE);
                    iv_record.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    super.onError(ex, isOnCallback);
                    Log.d(TAG, "补录失败--->" + ex.getMessage());
                    goToBuLu(bitmap);
                    ex.printStackTrace();
                }

                @Override
                public void onFinished() {
                    super.onFinished();
                }
            });
        }
    }

    /**
     * 保存bitmap到本地
     *
     * @param mBitmap
     * @return
     */
    public String saveBitmap(Bitmap mBitmap) {
        File filePic;
        try {
            //格式化时间
            long time = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String sdfTime = sdf.format(time);
            filePic = new File(SCREEN_BASE_PATH + today + "/" + sdfTime + ".jpg");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return filePic.getAbsolutePath();
    }

    private void startXmpp() {//开启xmpp
        serviceManager = new ServiceManager(this);
        serviceManager.startService();
    }

    //设置饼图属性
    private void initPieChart() {
        pieChart_h = (PieChart) findViewById(R.id.pie_chart);
        Description description = pieChart_h.getDescription();
        description.setText("");//关闭饼图描述
        pieChart_h.setDrawEntryLabels(false);//关闭环中显示的lable
        Legend legend = pieChart_h.getLegend();
        legend.setEnabled(false);//关闭色块指示
        pieChart_h.setHoleColor(Color.TRANSPARENT);//中心圆透明
        pieChart_h.setHoleRadius(70f);//中心圆半径
        pieChart_h.setHighlightPerTapEnabled(false);//点击高亮
        pieChart_h.animateY(1400, Easing.EasingOption.EaseInOutQuad);// 设置pieChart图表展示动画效果
        pieChart_h.setNoDataText("");//不显示无数据提醒
    }

    //密码弹窗
    private void inputPwd() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_input_pwd);

        final Animation animation = AnimationUtils.loadAnimation(WelComeActivity.this, R.anim.anim_edt_shake);
        final View rootView = dialog.findViewById(R.id.ll_input_pwd);
        Button btnConfirm = (Button) dialog.findViewById(R.id.btn_input_confirm);
        final EditText edtPwd = (EditText) dialog.findViewById(R.id.edt_input_pwd);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = edtPwd.getText().toString();
                if (TextUtils.isEmpty(pwd)) {
                    edtPwd.setError("不要忘记输入密码哦");
                    rootView.startAnimation(animation);
                    return;
                }
//                String spPwd = SpUtils.getString(WelComeActivity.this, SpUtils.MENU_PWD, "");
                String spPwd = SpUtils.getStr(SpUtils.MENU_PWD);
                if (!TextUtils.equals(pwd, spPwd)) {
                    edtPwd.setError("密码错了，重新输入吧");
                    rootView.startAnimation(animation);
                    return;
                }
                startActivity(new Intent(WelComeActivity.this, SystemActivity.class));
                dialog.dismiss();
            }
        });

        dialog.show();
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int width = dm.widthPixels;
        final Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.mystyle);  //添加动画
        window.setLayout(width / 2, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawableResource(android.R.color.transparent);
    }

    //更新签到列表
    private void updateList(List<VIPDetail> mList) {
        int male = 0;
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i) != null && mList.get(i).getSex() != null && mList.get(i).getSex().equals("男")) {
                male = male + 1;
            }
        }
        int total = mList.size();
        int female = total - male;

        mVisitorAdapter.notifyDataSetChanged();

        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            String signTips = "已签到   <font color='#fff600'>" + total + "</font>   人 （男 <font color='#fff600'>" + male + "</font>   女  <font color='#fff600'>" + female + "</font> ） ";
            tv_checkInNum.setText(Html.fromHtml(signTips));

        } else {
            tv_checkInNum.setText("" + total);
            ((TextView) findViewById(R.id.tv_sign_number_male)).setText("男: " + male + "人");
            ((TextView) findViewById(R.id.tv_sign_number_female)).setText("女: " + female + "人");

            //设置饼图数据
            List<PieEntry> dataEntry = new ArrayList<>();
            List<Integer> dataColors = new ArrayList<>();

            if (male == 0 && female == 0) {
                dataEntry.add(new PieEntry(100, ""));
                dataColors.add(getResources().getColor(R.color.white));
            } else {
                dataEntry.add(new PieEntry(male, "男"));
                dataEntry.add(new PieEntry(female, "女"));
                dataColors.add(getResources().getColor(R.color.horizontal_chart_male));
                dataColors.add(getResources().getColor(R.color.horizontal_chart_female));
            }

            pieChart_h.clear();
            PieDataSet pieDataSet = new PieDataSet(dataEntry, null);
            pieDataSet.setColors(dataColors);
            PieData pieData = new PieData(pieDataSet);
            pieData.setDrawValues(false);//环中value显示
            pieChart_h.setData(pieData);
            pieChart_h.notifyDataSetChanged();
            pieChart_h.invalidate();
        }
    }

    /*=======摄像头检测=============================================================================*/
    //摄像头错误监听
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(CameraError cameraError) {
        tvCameraError.setVisibility(View.VISIBLE);
        tvCameraError.setText("Error:" + cameraError.errCode + "\n设备异常，请检查摄像头");
        cameraState = checkCamera();

        int cameraError1 = SpUtils.getInt("CameraError");
        Log.e(TAG, "update: ------------" + cameraError1 );
        cameraError1 += 1;
        SpUtils.saveInt("CameraError",cameraError1);

        Log.e(TAG, "update: 摄像头错误："+cameraError.errCode);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                RestartAPPTool.restartAPP(WelComeActivity.this);
            }
        },1 * 1000);
    }

    //USB摄像头监听
    private BroadcastReceiver cameraReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
//                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    int ci = checkCamera();
                    if (ci != cameraState) {
                        RestartAPPTool.restartAPP(WelComeActivity.this);
                    }
                    break;
            }
        }
    };

    private int checkCamera() {
        int cameraResult = 0;
        int numberOfCameras = Camera.getNumberOfCameras();
        if (numberOfCameras <= 0) {
            cameraResult = 0;
        }
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            boolean isBack = info.facing == Camera.CameraInfo.CAMERA_FACING_BACK;
            cameraResult = isBack ? 1 : 0;
        }
        return cameraResult;
    }

    //语音播报
    private void speak(int signType, String signerName) {
        String speakStr = " 您好 %s ，欢迎光临";
        switch (signType) {
            case 0:
                speakStr = String.format(yuyin, signerName);
                break;
            case 1:
//                String goTips = SpUtils.getString(WelComeActivity.this, SpUtils.GOTIPS, "上班签到成功");
                String goTips = SpUtils.getStr(SpUtils.GOTIPS, "上班签到成功");
                speakStr = String.format(" %s " + goTips, signerName);
                break;
            case 2:
//                String downTips = SpUtils.getString(WelComeActivity.this, SpUtils.DOWNTIPS, "下班签到成功");
                String downTips = SpUtils.getStr(SpUtils.DOWNTIPS, "下班签到成功");
                speakStr = String.format(" %s " + downTips, signerName);
                break;
        }
        KDXFSpeechManager.instance().playText(speakStr);
    }

    private void startInitAnim() {
        RotateAnimation rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        LinearInterpolator lin = new LinearInterpolator();
        rotate.setInterpolator(lin);
        rotate.setDuration(4000);//设置动画持续周期
        rotate.setRepeatCount(-1);//无限重复
        rotate.setStartOffset(10);//执行前的等待时间
        iv_yuan.startAnimation(rotate);

        AnimationSet animationSet = new AnimationSet(true);//共用动画补间
        TranslateAnimation ta = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0f,
                Animation.RELATIVE_TO_PARENT, 0f,
                Animation.RELATIVE_TO_PARENT, -0.2f,
                Animation.RELATIVE_TO_PARENT, 0.35f);
        ScaleAnimation bigToSmallAnim = new ScaleAnimation(1, 0.6f, 1, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);//x轴0倍，x轴1倍，y轴0倍，y轴1倍

        animationSet.setDuration(4000);
        animationSet.setRepeatCount(-1);
        animationSet.setRepeatMode(Animation.REVERSE);
        animationSet.addAnimation(ta);
        animationSet.addAnimation(bigToSmallAnim);
        // 动画是作用到某一个控件上
        iv_line.startAnimation(animationSet);
    }

    public void closeInitView(final Runnable runnable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TranslateAnimation ta1 = new TranslateAnimation(0, 0, 0, -layout_head.getHeight());
                ta1.setInterpolator(new LinearInterpolator());
                ta1.setDuration(800);
                layout_head.startAnimation(ta1);

                AnimationSet as = new AnimationSet(true);
                as.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        layout_head.setVisibility(View.GONE);
                        layout_wel.setVisibility(View.GONE);
                        if(runnable != null){
                            runnable.run();
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                TranslateAnimation ta2 = new TranslateAnimation(0, 0, 0, layout_wel.getHeight());
                AlphaAnimation a = new AlphaAnimation(1f, 0.5f);
                as.setInterpolator(new LinearInterpolator());
                as.setFillAfter(true);
                as.setDuration(800);
                as.addAnimation(ta2);
                as.addAnimation(a);
                layout_wel.startAnimation(as);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
//            String pwd = SpUtils.getString(WelComeActivity.this, SpUtils.MENU_PWD, "");
            String pwd = SpUtils.getStr(SpUtils.MENU_PWD);
            if (!TextUtils.isEmpty(pwd)) {
                inputPwd();
                return true;
            }
            startActivity(new Intent(WelComeActivity.this, SystemActivity.class));
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        RestartAPPTool.showExitDialog(this,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                moveTaskToBack(true);
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAll();
                APP.exit();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        faceView.resume();
        tagHandler.removeMessages(0);
        tagHandler.sendEmptyMessageDelayed(0, 500);
        AdsManager.instance().resume();
    }

    //延时修改标签，避免返回后立刻弹出签到框的情况
    private Handler tagHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            isForeground = true;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        faceView.pause();
        isForeground = false;
        tagHandler.removeMessages(0);
        AdsManager.instance().pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        faceView.destory();
        if (serviceManager != null) {
            serviceManager.stopService();
            serviceManager = null;
        }
        stopService(mIntent);
        unregisterReceiver(cameraReceiver);

        SyncManager.instance().destory();
        AdsManager.instance().destory();
        KDXFSpeechManager.instance().destroy();
        LocateManager.instance().destory();
        EventBus.getDefault().unregister(this);
    }

}
