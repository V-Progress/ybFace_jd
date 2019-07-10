package com.yunbiao.ybsmartcheckin_live_id.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.jdjr.risk.face.local.verify.VerifyResult;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.adapter.VisitorAdapter;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.bean.AddQRCodeBean;
import com.yunbiao.ybsmartcheckin_live_id.bean.CompanyBean;
import com.yunbiao.ybsmartcheckin_live_id.business.AdsManager;
import com.yunbiao.ybsmartcheckin_live_id.business.AirQualityUtil;
import com.yunbiao.ybsmartcheckin_live_id.business.ApiManager;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.business.LocateManager;
import com.yunbiao.ybsmartcheckin_live_id.business.ResourceCleanManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.business.VipDialogManager;
import com.yunbiao.ybsmartcheckin_live_id.business.WeatherManager;
import com.yunbiao.ybsmartcheckin_live_id.db.SignBean;
import com.yunbiao.ybsmartcheckin_live_id.faceview.FaceView;
import com.yunbiao.ybsmartcheckin_live_id.heartbeat.BaseGateActivity;
import com.yunbiao.ybsmartcheckin_live_id.serialport.plcgate.GateCommands;
import com.yunbiao.ybsmartcheckin_live_id.utils.RestartAPPTool;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.ThreadUitls;
import com.yunbiao.ybsmartcheckin_live_id.xmpp.ServiceManager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

import static com.yunbiao.ybsmartcheckin_live_id.APP.getContext;

/**
 * Created by Administrator on 2018/11/26.
 */

public class WelComeActivity extends BaseGateActivity {

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

    private ImageView ivQrCodeAdd;

    private BaseAdapter mVisitorAdapter;//签到人员adapter
    private PieChart pieChart_h;//横屏饼图

    // xmpp推送服务
    private ServiceManager serviceManager;

    private String today = "";//获取今天时间
    private boolean isBulu = false;//获取补录头像
    private String yuyin = " 您好 %s";

    private boolean isForeground = false;//是否前台显示

    //摄像头分辨率
    private FaceView faceView;
    private List<SignBean> mSignList = new ArrayList<>();

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_welcome;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_welcome_h;
    }

    @Override
    protected void initView() {
        if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE){
            initPieChart();
        }
        faceView = findViewById(R.id.face_view);
        iv_logo = findViewById(R.id.iv_logo);
        tv_title = findViewById(R.id.tv_title);
        ll_load_container = findViewById(R.id.ll_load_container);
        tv_load_error = findViewById(R.id.tv_load_error);
        gridview = findViewById(R.id.gridview);
        aiv_bulu = findViewById(R.id.aiv_bulu);
        tv_tem = findViewById(R.id.tv_tem);
        iv_wea = findViewById(R.id.iv_wea);
        tv_deviceNo = findViewById(R.id.tv_deviceNo);
        tv_checkInNum = findViewById(R.id.tv_checkInNum);
        imageView = findViewById(R.id.imageView_logo);
        iv_record = findViewById(R.id.iv_record);
        tv_comName = findViewById(R.id.tv_comName);
        tv_notice = findViewById(R.id.tv_notice);
        tv_topTitle = findViewById(R.id.tv_topTitle);
        tv_bottomTitle = findViewById(R.id.tv_bottomTitle);
        ivQrCodeAdd = findViewById(R.id.iv_qrCode_add);

        faceView.setCallback(faceCallback);
        mVisitorAdapter = new VisitorAdapter(WelComeActivity.this, mSignList, mCurrentOrientation);
        gridview.setAdapter(mVisitorAdapter);
    }

    @Override
    protected void initData() {
        //设置设备编号
        String deviceSernum = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
        if (!TextUtils.isEmpty(deviceSernum) && tv_deviceNo != null) {
            tv_deviceNo.setText(deviceSernum);
        }

        //开启Xmpp
        startXmpp();

        //初始化语音系统//todo 7.0以上无法加载讯飞语音库
        KDXFSpeechManager.instance().init(this).welcome();

        //初始化定位工具
        LocateManager.instance().init(this);

        //开始获取天气
        WeatherManager.instance().start(WelComeActivity.this,resultListener);

        //开始定时更新签到列表
        SignManager.instance().init(this,signEventListener);

        //初始化广告
        AdsManager.instance().init(WelComeActivity.this, null);

        //自动清理服务
        ResourceCleanManager.instance().startAutoCleanService();

        if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE){
            AirQualityUtil.get(new AirQualityUtil.AirQualityCallback() {
                @Override
                public void onCallback(AirQualityUtil.AirBean.Citynow citynow) {
                    TextView tvAir = findViewById(R.id.tv_air);
                    if(tvAir != null){
                        tvAir.setText(citynow.getCity() + "，空气质量："+citynow.getQuality());
                    }
                }
            });
        }
    }

    /*人脸识别回调，由上到下执行*/
    private FaceView.FaceCallback faceCallback = new FaceView.FaceCallback() {
        @Override
        public void onReady() {
            syncData();
        }
        @Override
        public void onFaceDetection() {
            ApiManager.instance().onLignt();
            //如果广告可见，收起广告
            if (AdsManager.instance().isAdsShowing()) {
                AdsManager.instance().openAds();
            } else {
                //重置倒计时
                AdsManager.instance().startTimer();
            }
        }

        @Override
        public void onFaceVerify(VerifyResult verifyResult) {
            if(verifyResult == null){
                return;
            }
            int result = verifyResult.getResult();
            if(isBulu ){
                if(!SignManager.canMakeUp()){
                    return;
                }
                SignManager.instance().makeUpSign(verifyResult.getFaceImageBytes());
                return;
            }
            if(result != VerifyResult.UNKNOWN_FACE){
                return;
            }
            SignManager.instance().checkSign(verifyResult);
        }
    };

    /*签到事件监听*/
    private SignManager.SignEventListener signEventListener = new SignManager.SignEventListener() {
        @Override
        public void onPrepared(List<SignBean> mList) {
            if (mList != null) {
                mSignList.addAll(mList);
                mVisitorAdapter.notifyDataSetChanged();
            }

            updateNumber();
            tv_load_error.setVisibility(View.GONE);
            ll_load_container.setVisibility(View.GONE);
            gridview.setVisibility(View.VISIBLE);
        }

        @Override
        public void onSigned(SignBean signBean, int signType) {
            mSignList.add(0,signBean);
            mVisitorAdapter.notifyDataSetChanged();
            updateNumber();

            ApiManager.instance().onGate();
            if (mGateIsAlive) {
                mGateConnection.writeCom(GateCommands.GATE_OPEN_DOOR);
            }

            speak(signType, signBean.getName());

            if (!AdsManager.instance().isAdsShowing()) {
//                MultipleSignDialog.instance().sign(signBean);
                VipDialogManager.showVipDialog(WelComeActivity.this, today, signBean);
            }
        }

        @Override
        public void onMakeUped(String imgPath, boolean makeUpSuccess) {
            isBulu = false;

            VipDialogManager.showBuluDialog(WelComeActivity.this, imgPath,makeUpSuccess);
            KDXFSpeechManager.instance().playText(makeUpSuccess ?"补录成功！":"补录失败！");
            aiv_bulu.setVisibility(View.GONE);
            iv_record.setVisibility(View.VISIBLE);
        }
    };

    /*天气请求结果监听*/
    private WeatherManager.ResultListener resultListener = new WeatherManager.ResultListener() {
        @Override
        public void updateWeather(int id, String weatherInfo) {
            iv_wea.setImageResource(R.mipmap.icon_snow);
            tv_tem.setText(weatherInfo);
        }
    };

    /*补录*/
    public void goMakeUp(View view){
        isBulu = true;
        aiv_bulu.setVisibility(View.VISIBLE);
        iv_record.setVisibility(View.GONE);
    }

    /*加载二维码*/
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

    /*同步数据*/
    private void syncData(){
        SyncManager.instance()
                .init(WelComeActivity.this)
                .setListener(new SyncManager.LoadListener() {
                    @Override
                    public void onLoaded(CompanyBean bean) {
                        tv_comName.setText(bean.getCompany().getAbbname());
                        tv_title.setText(bean.getCompany().getAbbname());
                        if(tv_notice != null){
                            tv_notice.setText(bean.getCompany().getNotice());
                        }
                        if(tv_topTitle != null){
                            tv_topTitle.setText(bean.getCompany().getToptitle());
                        }
                        if(tv_bottomTitle != null){
                            tv_bottomTitle.setText(bean.getCompany().getBottomtitle());
                        }
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

    private void startXmpp() {//开启xmpp
        serviceManager = new ServiceManager(this);
        serviceManager.startService();
    }

    private void destoryXmpp(){
        if(serviceManager != null){
            serviceManager.stopService();
            serviceManager = null;
        }
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
    private void inputPwd(final Runnable runnable) {
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
                String spPwd = SpUtils.getStr(SpUtils.MENU_PWD);
                if (!TextUtils.equals(pwd, spPwd)) {
                    edtPwd.setError("密码错了，重新输入吧");
                    rootView.startAnimation(animation);
                    return;
                }
                if(runnable != null){
                    runnable.run();
                }
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
    private void updateNumber() {
        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
                int male = 0;
                for (SignBean signBean : mSignList) {
                    boolean empty = TextUtils.isEmpty(signBean.getSex());
                    if (empty || signBean.getSex().equals("1") || signBean.getSex().equals("男")) {
                        male = male + 1;
                    }
                }

                final int total = mSignList.size();
                final int female = total - male;
                final int maleNum = male;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                            String signTips = "已签到   <font color='#fff600'>" + total + "</font>   人 （男 <font color='#fff600'>" + maleNum + "</font>   女  <font color='#fff600'>" + female + "</font> ） ";
                            tv_checkInNum.setText(Html.fromHtml(signTips));
                        } else {
                            tv_checkInNum.setText("" + total);
                            ((TextView) findViewById(R.id.tv_sign_number_male)).setText("男: " + maleNum + "人");
                            ((TextView) findViewById(R.id.tv_sign_number_female)).setText("女: " + female + "人");

                            //设置饼图数据
                            List<PieEntry> dataEntry = new ArrayList<>();
                            List<Integer> dataColors = new ArrayList<>();

                            if (maleNum == 0 && female == 0) {
                                dataEntry.add(new PieEntry(100, ""));
                                dataColors.add(getResources().getColor(R.color.white));
                            } else {
                                dataEntry.add(new PieEntry(maleNum, "男"));
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
                });
            }
        });
    }

    /*=======摄像头检测=============================================================================*/
    //语音播报
    private void speak(int signType, String signerName) {
        String speakStr = " 您好 %s ，欢迎光临";
        switch (signType) {
            case 0:
                speakStr = String.format(yuyin, signerName);
                break;
            case 1:
                String goTips = SpUtils.getStr(SpUtils.GOTIPS, "上班签到成功");
                speakStr = String.format(" %s " + goTips, signerName);
                break;
            case 2:
                String downTips = SpUtils.getStr(SpUtils.DOWNTIPS, "下班签到成功");
                speakStr = String.format(" %s " + downTips, signerName);
                break;
        }
        KDXFSpeechManager.instance().playText(speakStr);
    }

    private void goSetting(){
        String pwd = SpUtils.getStr(SpUtils.MENU_PWD);
        if (!TextUtils.isEmpty(pwd)) {
            inputPwd(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(WelComeActivity.this, SystemActivity.class));
                }
            });
            return ;
        }
        startActivity(new Intent(WelComeActivity.this, SystemActivity.class));
    }

    //跳转设置界面
    public void goSetting(View view){
        goSetting();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            goSetting();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        RestartAPPTool.showExitDialog(this,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputPwd(new Runnable() {
                    @Override
                    public void run() {
                        moveTaskToBack(true);
                    }
                });
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputPwd(new Runnable() {
                    @Override
                    public void run() {
                        finishAll();
                        APP.exit();
                    }
                });
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
        destoryXmpp();

        SyncManager.instance().destory();
        AdsManager.instance().destory();
        KDXFSpeechManager.instance().destroy();
        LocateManager.instance().destory();
    }

}



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
////                updateNumber(mList);
//
//                tv_load_error.setVisibility(View.GONE);
//                ll_load_container.setVisibility(View.GONE);
//                gridview.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onSigned(List<SignBean> mList, SignBean signBean, int signType) {
////                updateNumber(mList);
//                speak(signType,signBean.getName());
//                MultipleSignDialog.instance().sign(signBean);
//
//                if (mGateIsAlive) {
//                    mGateConnection.writeCom(GateCommands.GATE_OPEN_DOOR);
//                }
//            }
//        });
