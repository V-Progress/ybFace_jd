package com.yunbiao.ybsmartcheckin_live_id.temp_check_in_smt;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.intelligence.hardware.temperature.TemperatureModule;
import com.yunbiao.faceview.CompareResult;
import com.yunbiao.faceview.FacePreviewInfo;
import com.yunbiao.faceview.FaceView;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.ReadCardUtils;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.DisplayOrientationEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.ResetLogoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateMediaEvent;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.business.LocateManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.business.VipDialogManager;
import com.yunbiao.ybsmartcheckin_live_id.common.UpdateVersionControl;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.temp_check_in.ThermalImageActivity;
import com.yunbiao.ybsmartcheckin_live_id.utils.RestartAPPTool;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.views.ImageFileLoader;
import com.yunbiao.ybsmartcheckin_live_id.xmpp.ServiceManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class SMTMainActivity extends SMTTempBaseActivity {
    private static final String TAG = "SMTMainActivity";
    private ImageView ivMainLogo;//公司logo

    // xmpp推送服务
    private ServiceManager serviceManager;

    //摄像头分辨率
    public static FaceView faceView;
    private SMTSignFragment smtSignFragment;
    private TextView tvTemperature;
    private TextView tvTempTips;
    private View flTempArea;
    private View distanceFaceView;
    private TextView tvDistanceTip;

    @Override
    protected int getLayout() {
        return R.layout.activity_smt_device;
    }

    @Override
    protected void initView() {
        APP.setMainActivity(this);
        EventBus.getDefault().register(this);
        faceView = findViewById(R.id.face_view);
        faceView.setCallback(faceCallback);
        ivMainLogo = findViewById(R.id.iv_main_logo);
        distanceFaceView = findViewById(R.id.view_face_distance);
        tvDistanceTip = findViewById(R.id.tv_distance_tips_main);

        //测温点
        flTempArea = findViewById(R.id.fl_temp_area);

        tvTemperature = findViewById(R.id.tv_temperature_main);//实时检测温度
        tvTempTips = findViewById(R.id.tv_temp_tips_main);//温度提示

        //加载签到列表Fragment
        smtSignFragment = new SMTSignFragment();
        replaceFragment(R.id.ll_list_container, smtSignFragment);
    }

    @Override
    protected void initData() {
        super.initData();

        KDXFSpeechManager.instance().init(this).welcome();

        //开启Xmpp
        startXmpp();
        //初始化定位工具
        LocateManager.instance().init(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        faceView.resume();
        //设置活体开关
        boolean livenessEnabled = SpUtils.getBoolean(SpUtils.LIVENESS_ENABLED, false);
        faceView.setLiveness(livenessEnabled);
    }

    @Override
    protected void onModeChanged(int mode) {
        if (smtSignFragment != null) {
            smtSignFragment.setModelText(SMTModelConst.models[mode]);
        }

        if (mode == SMTModelConst.SMT_FACE_ONLY) {
            flTempArea.setVisibility(View.GONE);
            tvTempTips.setVisibility(View.GONE);
        } else {
            flTempArea.setVisibility(View.VISIBLE);
            tvTemperature.setVisibility(View.VISIBLE);
        }
    }

//    @Override
//    protected boolean isStableTipsShown() {
//        return tvTempTips.isShown() && TextUtils.equals(tvTempTips.getText().toString(), "请靠近点");
//    }

    @Override
    protected void updateHotImageAndTemper(Bitmap bitmap, float temper, float maxT, float cacheT) {

    }

    @Override
    protected Bitmap getCurrCameraFrame() {
        Bitmap bitmap = faceView.takePicture();
        if(bitmap == null){
            bitmap = faceView.getCurrCameraFrame();
        }
        return bitmap;
    }

    @Override
    protected boolean showStableTips(String tip, int stableTipsId) {
        if (!tvDistanceTip.isShown()) {
            tvDistanceTip.setVisibility(View.VISIBLE);
        }
        tvDistanceTip.setText(tip);
        tvDistanceTip.setBackgroundResource(stableTipsId);
        return true;
    }

    @Override
    protected void clearStableTips(){
        if (tvDistanceTip.isShown()) {
            tvDistanceTip.setVisibility(View.GONE);
        }
    }

    @Override
    protected void showUIResult(String tip, int id) {
        if (!tvTempTips.isShown()) {
            tvTempTips.setVisibility(View.VISIBLE);
        }
        tvDistanceTip.setVisibility(View.GONE);
        tvTempTips.setBackgroundResource(id);
        tvTempTips.setText(tip);
    }

    @Override
    protected void clearUI() {
        if (tvTempTips.isShown()) {
            tvTempTips.setVisibility(View.GONE);
        }
    }

    private FaceView.FaceCallback faceCallback = new FaceView.FaceCallback() {
        @Override
        public void onReady() {
            SyncManager.instance().requestCompany();
        }

        @Override
        public void onFaceDetection(Boolean hasFace, List<FacePreviewInfo> facePreviewInfoList) {

        }

        private boolean isBroaded = false;

        @Override
        public boolean onFaceDetection(boolean hasFace, FacePreviewInfo facePreviewInfo) {
            updateHasFace(hasFace);

            if (!hasFace) {
                isBroaded = false;
                return false;
            }

            onLight();

            //收起海报界面
//            if (adsFragment != null) {
//                adsFragment.detectFace();
//            }

            if (isOnlyFace()) {
                return true;
            }

            Rect rect = facePreviewInfo.getFaceInfo().getRect();
            int distance = distanceFaceView.getMeasuredWidth();

//            boolean isSoFar = faceView.checkFaceToFar(rect, distance / 2);
//            updateSoFar(isSoFar);

            //人脸较远
            boolean isFar = faceView.checkFaceToFar(rect, distance);
            updateFaceState(isFar);
            if (isFar) {
                return false;
            }

            //仅测温
            if (isOnlyTemp()) {
                return false;
            }

            //没有最终温度
            if (!hasFinalTemp()) {
                return false;
            }

            if (isBroaded) {
                return false;
            }
            return true;
        }

        @Override
        public void onFaceVerify(CompareResult compareResult) {
            isBroaded = true;
            if (isOnlyFace()) {
                //======以下是普通识别流程====================================
                if (compareResult == null || compareResult.getSimilar() == -1) {
                    return;
                }
                Sign sign = SignManager.instance().checkSignData(compareResult, 0f);
                if (sign == null) {
                    return;
                }
//                if (signListFragment != null) {
//                    smtSignFragment.addSignData(sign);
//                }

                if (SpUtils.getBoolean(SpUtils.FACE_DIALOG, false)) {
                    VipDialogManager.showVipDialog(SMTMainActivity.this, sign);
                }

                KDXFSpeechManager.instance().playText(sign.getName());

                if (sign.getType() == -2) {
                    return;
                }

                openDoor();
            } else {
                Bitmap bitmap = faceView.takePicture();
                if (bitmap == null) {
                    bitmap = faceView.getCurrCameraFrame();
                }
                sendFaceTempMessage(bitmap, compareResult);
            }
        }
    };

    @Override
    protected boolean isTempTipsShown(){
        return tvTempTips.isShown();
    }

    @Override
    protected void clearTempTips(){
        tvTempTips.setVisibility(View.GONE);
    }

    @Override
    protected void updateSignList(Sign sign) {
        if (sign.getType() != -9) {
            KDXFSpeechManager.instance().playNormal(sign.getName());
        }
    }


/*

    @Override
    protected void updateTemp(float f) {
        tvTemperature.setText(f + "℃");
    }

    @Override
    protected Bitmap getFaceViewBitmap() {
        return faceView.getCurrCameraFrame();
    }

    @Override
    protected void clearUI() {
        if (tvTempTips.isShown()) {
            tvTempTips.setVisibility(View.GONE);
        }
    }

    @Override
    protected void setResult(String tip, int id, Sign sign) {
        if (!tvTempTips.isShown()) {
            tvTempTips.setVisibility(View.VISIBLE);
        }
        tvTempTips.setText(tip);
        tvTempTips.setBackgroundResource(id);
        if (smtSignFragment != null) {
            smtSignFragment.updateNum(sign);
        }
    }

    */
    /*****识别相关回调******************************************************************************************//*

    private FaceView.FaceCallback faceCallback = new FaceView.FaceCallback() {
        @Override
        public void onReady() {
            SyncManager.instance().requestCompany();
        }

        @Override
        public void onFaceDetection(Boolean hasFace, List<FacePreviewInfo> facePreviewInfoList) {

        }

        private float mLastTemp = 0.0f;
        private boolean isBroaded = false;

        @Override
        public boolean onFaceDetection(boolean hasFace, FacePreviewInfo facePreviewInfo) {
            if (!hasFace) {//如果没有人脸
                isBroaded = false;
                return false;
            }

            if (isOnlyTemp()) {
                return false;
            }

            //检测到人后开灯
            onLight();

            if (isFaceAndTemp()) {
                if (getCurrTemp() < getMinThread()) {
                    return false;
                }

                if (isBroaded) {
                    return false;
                }
                isBroaded = true;
                mLastTemp = getCurrTemp();
            }
            return true;
        }

        @Override
        public void onFaceVerify(CompareResult compareResult) {
            if (isFaceAndTemp()) {

                float finalTemp = mLastTemp;
                boolean isWarning = finalTemp > getWarningThread();
                Sign sign = null;
                if (compareResult.getSimilar() == -1) {
                    //直接上报温度
                    sign = SignManager.instance().getTemperatureSign(finalTemp);
                } else {
                    sign = SignManager.instance().checkSignData(compareResult, finalTemp);
                    if (sign == null) {
                        return;
                    }
                }
                sign.setImgBitmap(faceView.takePicture());
                sign.setHotImageBitmap(null);

                sendTempResultMessage(isWarning,sign);

                SignManager.instance().uploadTemperatureSign(sign);
                //如果是过期则结束，陌生人则判断是否体温正常
                if (sign.getType() == -2 || sign.getType() == -9 || isWarning) {
                    return;
                }
                openDoor();
                return;
            }

            //======以下是普通识别流程====================================
            if (compareResult == null || compareResult.getSimilar() == -1) {
                return;
            }
            Sign sign = SignManager.instance().checkSignData(compareResult, 0f);
            if (sign == null) {
                return;
            }
            if (smtSignFragment != null) {
                smtSignFragment.updateNum(sign);
            }

            if (SpUtils.getBoolean(SpUtils.FACE_DIALOG, false)) {
                VipDialogManager.showVipDialog(SMTMainActivity.this, sign);
            }

            KDXFSpeechManager.instance().playText(sign.getName());

            if (sign.getType() == -2) {
                return;
            }

            openDoor();
        }
    };
    //========================================================
*/

    private ReadCardUtils.OnReadSuccessListener readCardListener = new ReadCardUtils.OnReadSuccessListener() {
        @Override
        public void onScanSuccess(String barcode) {
            Log.e(TAG, "barcode: " + barcode);
            Sign sign = SignManager.instance().checkSignForCard(barcode);

            if (smtSignFragment != null) {
                smtSignFragment.updateNum(sign);
            }

            if (SpUtils.getBoolean(SpUtils.FACE_DIALOG, false)) {
                VipDialogManager.showVipDialog(SMTMainActivity.this, sign);
            }

            KDXFSpeechManager.instance().playText(sign.getName());

            if (sign.getType() == -2) {
                return;
            }

            openDoor();
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateInfoEvent event) {
        Company company = SpUtils.getCompany();

        EventBus.getDefault().post(new UpdateMediaEvent());

        ImageFileLoader.i().loadAndSave(this, company.getComlogo(), Constants.DATA_PATH, ivMainLogo);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(ResetLogoEvent event) {
        Company company = SpUtils.getCompany();
        ImageFileLoader.i().loadAndSave(this, company.getComlogo(), Constants.DATA_PATH, ivMainLogo);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(DisplayOrientationEvent event) {
        Log.e(TAG, "update: 收到摄像头更新事件");
        faceView.changeAngle();
    }

    private void startXmpp() {//开启xmpp
        serviceManager = new ServiceManager(this);
        serviceManager.startService();
    }

    private void destoryXmpp() {
        if (serviceManager != null) {
            serviceManager.stopService();
            serviceManager = null;
        }
    }

    /**
     * 读卡器初始化==========================================================================================
     * 读卡器初始化==========================================================================================
     * 读卡器初始化==========================================================================================
     */
    //U口读卡器,类似于外接键盘
    private ReadCardUtils readCardUtils;

    private void initCardReader() {
        //读卡器声明
        readCardUtils = new ReadCardUtils();
        readCardUtils.setReadSuccessListener(readCardListener);
    }

    private void closeCardReader() {
        if (readCardUtils != null) {
            readCardUtils.removeScanSuccessListener();
            readCardUtils = null;
        }
    }

    private void handleKeyEvent(KeyEvent event) {
        if (ReadCardUtils.isInputFromReader(this, event)) {
            if (readCardUtils != null) {
                readCardUtils.resolveKeyEvent(event);
            }
        }
    }

    //密码弹窗
    private void inputPwd(final Runnable runnable) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_input_pwd);

        final Animation animation = AnimationUtils.loadAnimation(SMTMainActivity.this, R.anim.anim_edt_shake);
        final View rootView = dialog.findViewById(R.id.ll_input_pwd);
        Button btnConfirm = (Button) dialog.findViewById(R.id.btn_input_confirm);
        final EditText edtPwd = (EditText) dialog.findViewById(R.id.edt_input_pwd);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = edtPwd.getText().toString();
                if (TextUtils.isEmpty(pwd)) {
                    edtPwd.setError(getString(R.string.act_wel_error_bywjsrmmo));
                    rootView.startAnimation(animation);
                    return;
                }
                String spPwd = SpUtils.getStr(SpUtils.MENU_PWD);
                if (!TextUtils.equals(pwd, spPwd)) {
                    edtPwd.setError(getString(R.string.act_wel_error_mmclqcxsrb));
                    rootView.startAnimation(animation);
                    return;
                }
                if (runnable != null) {
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

    public void setFaceViewSimilar() {
        Log.e(TAG, "setFaceViewSimilar: 设置人脸识别阈值");
        faceView.setSimilarThreshold();
    }

    private void goSetting() {
        String pwd = SpUtils.getStr(SpUtils.MENU_PWD);
        if (!TextUtils.isEmpty(pwd)) {
            inputPwd(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SMTMainActivity.this, SMTSystemActivity.class));
                }
            });
            return;
        }
        startActivity(new Intent(SMTMainActivity.this, SMTSystemActivity.class));
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        handleKeyEvent(event);
        return super.dispatchKeyEvent(event);
    }

    private void onBackKeyPressed(Runnable runnable) {
        String pwd = SpUtils.getStr(SpUtils.MENU_PWD);
        if (!TextUtils.isEmpty(pwd)) {
            inputPwd(runnable);
            return;
        }
        if (runnable != null) {
            runnable.run();
        }
    }

    //跳转设置界面
    public void goSetting(View view) {
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
        RestartAPPTool.showExitDialog(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onBackKeyPressed(new Runnable() {
                    @Override
                    public void run() {
                        moveTaskToBack(true);
                    }
                });
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onBackKeyPressed(new Runnable() {
                    @Override
                    public void run() {
                        APP.exit();
                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        faceView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        TemperatureModule.getIns().closeSerialPort();
        faceView.destory();
        destoryXmpp();

        EventBus.getDefault().unregister(this);
        SyncManager.instance().destory();
        KDXFSpeechManager.instance().destroy();
        LocateManager.instance().destory();
    }
}
