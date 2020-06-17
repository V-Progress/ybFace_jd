package com.yunbiao.ybsmartcheckin_live_id.activity_certificates;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Html;
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

import com.bumptech.glide.Glide;
import com.yunbiao.faceview.CertificatesView;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.FlavorType;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.DisplayOrientationEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.ResetLogoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateMediaEvent;
import com.yunbiao.ybsmartcheckin_live_id.db2.VertifyRecord;
import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.IdCardMsg;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.utils.IDCardReader;
import com.yunbiao.ybsmartcheckin_live_id.utils.RestartAPPTool;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.views.ImageFileLoader;
import com.yunbiao.ybsmartcheckin_live_id.xmpp.ServiceManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;

public class CertificatesActivity extends BaseCertificatesActivity implements CertificatesViewInterface {

    private static final String TAG = "CertificatesActivity";
    @BindView(R.id.iv_logo)
    ImageView ivLogo;
    @BindView(R.id.iv_face)
    ImageView ivFace;
    @BindView(R.id.iv_idCard)
    ImageView ivIdCard;
    @BindView(R.id.certificates_view)
    CertificatesView certificatesView;
    @BindView(R.id.iv_hot_image)
    ImageView ivHotImage;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_origin)
    TextView tvOrigin;
    @BindView(R.id.tv_tip)
    TextView tvTip;
    @BindView(R.id.tv_alike)
    TextView tvSimilar;
    @BindView(R.id.tv_tem)
    TextView tvTemp;
    @BindView(R.id.tv_ostatus)
    TextView tvOStatus;
    @BindView(R.id.iv_status)
    ImageView ivStatus;
    @BindView(R.id.view_temp_verify)
    View verifyStatusTip;
    @BindView(R.id.iv_verifyInfo)
    TextView tvVerifyInfo;
    @BindView(R.id.iv_verifyStatus)
    ImageView ivVerifyStatus;
    @BindView(R.id.ll_bg_verifyStatus)
    View llBgVerifyStatus;
    @BindView(R.id.tv_origin_t)
    TextView tvOriginT;
    @BindView(R.id.tv_temp_main)
    TextView tvLeftTopTemp;
    @BindView(R.id.btn_no_id_card)
    Button btnNoIdCard;
    @BindView(R.id.tv_bottomTitle)
    View tvBottomTitle;
    @BindView(R.id.tv_net_state_certi)
    TextView tvNetState;
    @BindView(R.id.iv_similar_icon_certi)
    ImageView ivSimilarIcon;
    @BindView(R.id.tv_white_status)
    TextView tvWhiteStatus;
    @BindView(R.id.iv_white_status)
    ImageView ivWhiteStatus;
    @BindView(R.id.tv_white_label)
    TextView tvWhiteLabel;
    @BindView(R.id.ll_white_status)
    View llWhiteStatus;
    @BindView(R.id.tv_device_number_certi)
    TextView tvDeviceNumber;
    @BindView(R.id.tv_bind_code_certi)
    TextView tvBindCode;
    @BindView(R.id.tv_app_version_certi)
    TextView tvAppVersion;

    private ServiceManager serviceManager;
    private boolean collectPhone;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_ai_temp_verify;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_ai_temp_verify_h;
    }

    @Override
    protected void initView() {
        APP.setMainActivity(this);
        EventBus.getDefault().register(this);
        btnNoIdCard.setOnClickListener(v -> noCardToTemper());//无证测温
        ivLogo.setOnClickListener(v -> goSetting());//进入设置

        if (Constants.FLAVOR_TYPE == FlavorType.HT) {
            ImageFileLoader.setDefaultLogoId(R.mipmap.logo_icon_horizontal);
            ivLogo.setImageResource(R.mipmap.logo_icon_horizontal);
            tvBottomTitle.setVisibility(View.GONE);
        } else {
            ImageFileLoader.setDefaultLogoId(R.mipmap.yb_logo);
            ivLogo.setImageResource(R.mipmap.yb_logo);
            tvBottomTitle.setVisibility(View.VISIBLE);
        }

        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            btnNoIdCard.setVisibility(View.GONE);
        }

        setDeviceInfo();
    }

    private void setDeviceInfo() {
        if (tvDeviceNumber != null) {
            String deviceNumber = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
            tvDeviceNumber.setText(deviceNumber);
        }
        if (tvBindCode != null) {
            String bindeCode = SpUtils.getStr(SpUtils.BIND_CODE);
            tvBindCode.setText(bindeCode);
        }
        if (tvAppVersion != null) {
            String appVersion = CommonUtils.getAppVersion(this);
            tvAppVersion.setText(appVersion);
        }
    }

    @Override
    protected void initData() {
        super.initData();

        setFaceCallback(certificatesView);

        setCertificatesView(this);

        startXmpp();
    }

    @Override
    public void onModeChanged(int mode) {
        Log.e(TAG, "onModeChanged: 当前模式：" + mode);
    }

    @Override
    public void onNetStateChanged(boolean isNet) {
        if (isNet) {
            tvNetState.setText("网络正常");
            tvNetState.setTextColor(Color.GREEN);
        } else {
            tvNetState.setText("无网络");
            tvNetState.setTextColor(Color.RED);
        }
    }

    @Override
    public void onFaceViewReady() {
        SyncManager.instance().requestOnlyCompany();
        SyncManager.instance().requestWhiteList(null);
    }

    @Override
    public void updateFaceImage(Bitmap bitmap) {
        ivFace.setImageBitmap(bitmap);
    }

    @Override
    public void updateHotImage(Bitmap hotImage, float temper, boolean mHasFace) {
        ivHotImage.setImageBitmap(hotImage);
        tvLeftTopTemp.setText(mHasFace ? getResString(R.string.act_certificates_someone) : getResString(R.string.act_certificates_noone));
    }

    @Override
    public void updateIdCardInfo(IdCardMsg idCardMsg, Bitmap bitmap, boolean icCardMode) {
        Log.e(TAG, "updateIdCardInfo: " + idCardMsg.name);
        ivIdCard.setImageBitmap(bitmap);
        tvName.setText(idCardMsg.name);
        if (icCardMode) {
            tvOriginT.setText(getResString(R.string.act_certificates_depart));
            tvOrigin.setText(idCardMsg.nation_str);
        } else {
            tvOriginT.setText(getResString(R.string.act_certificates_printer_native_place));
            tvOrigin.setText(IDCardReader.getNativeplace(idCardMsg.id_num));
        }
    }

    @Override
    public void updateResultTip(String resultTip, IdCardMsg idCardMsg, float finalTemper, int similarInt, boolean isAlike, boolean isNormal, boolean isInWhite, boolean icCardMode) {
        tvTemp.setText(finalTemper + "℃");
        tvSimilar.setText(getResString(R.string.act_certificates_similar) + similarInt + "%");
        tvTip.setText(Html.fromHtml(resultTip));
        if (isAlike) {
            tvSimilar.setTextColor(Color.GREEN);
            ivSimilarIcon.setImageResource(R.mipmap.icon_normal);
        } else {
            tvSimilar.setTextColor(Color.RED);
            ivSimilarIcon.setImageResource(R.mipmap.icon_warning);
        }

        if (isNormal) {
            tvOStatus.setText(getResString(R.string.act_certificates_normal));
            tvOStatus.setTextColor(Color.GREEN);
            tvTemp.setTextColor(Color.GREEN);
            ivStatus.setImageResource(R.mipmap.icon_normal);
        } else {
            tvOStatus.setText(getResString(R.string.act_certificates_warning));
            tvOStatus.setTextColor(Color.RED);
            tvTemp.setTextColor(Color.RED);
            ivStatus.setImageResource(R.mipmap.icon_warning);
        }

        if (!verifyStatusTip.isShown()) {
            verifyStatusTip.setVisibility(View.VISIBLE);
        }

        if (isAlike && isNormal) {
            if (isInWhite) {
                tvWhiteStatus.setText(getResString(R.string.act_certificates_printer_white_yes));
                tvWhiteStatus.setTextColor(Color.GREEN);
                ivWhiteStatus.setImageResource(R.mipmap.icon_normal);
                llBgVerifyStatus.setBackgroundResource(R.mipmap.bg_verify_pass);
                ivVerifyStatus.setImageResource(R.mipmap.icon_verify_pass);
                tvVerifyInfo.setText(getResString(R.string.act_certificates_verify_passage_yes));
            } else {
                tvWhiteStatus.setText(getResString(R.string.act_certificates_printer_white_no));
                tvWhiteStatus.setTextColor(Color.RED);
                ivWhiteStatus.setImageResource(R.mipmap.icon_warning);
                llBgVerifyStatus.setBackgroundResource(R.mipmap.bg_verify_ensure);
                ivVerifyStatus.setImageResource(R.mipmap.icon_verify_ensure);
                tvVerifyInfo.setText(getResString(R.string.act_certificates_verify_passage_ensure));
            }
        } else {
            llBgVerifyStatus.setBackgroundResource(R.mipmap.bg_verify_nopass);
            ivVerifyStatus.setImageResource(R.mipmap.icon_verify_nopass);
            tvVerifyInfo.setText(getResString(R.string.act_certificates_verify_passage_no));
        }

        BitmapDrawable bitmapDrawable = (BitmapDrawable) ivFace.getDrawable();
        Bitmap faceImage = bitmapDrawable.getBitmap();
        BitmapDrawable hotImageBitmap = (BitmapDrawable) ivHotImage.getDrawable();
        Bitmap hotImage = hotImageBitmap.getBitmap();
        BitmapDrawable idCardDrawable = (BitmapDrawable) ivIdCard.getDrawable();
        Bitmap idCardImage = idCardDrawable.getBitmap();
        if (icCardMode) {
            SignManager.instance().addICCardVerifyRecordToDB(faceImage, hotImage, idCardImage, finalTemper, idCardMsg, similarInt, (isAlike && isNormal ? 0 : 1));
        } else {
            if(collectPhone && isAlike){
                VertifyRecord idCardVerifyRecord = SignManager.instance().getIDCardVerifyRecord(finalTemper, idCardMsg, similarInt, (isAlike && isNormal ? 0 : 1), idCardImage, faceImage, hotImage);
                Intent intent = new Intent(this, CollectPhoneActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("verifyRecord",idCardVerifyRecord);
                intent.putExtras(bundle);
                startActivity(intent);
            } else {
                SignManager.instance().uploadIdCardAndReImage(finalTemper, idCardMsg, similarInt, (isAlike && isNormal ? 0 : 1), idCardImage, faceImage, hotImage);
            }
        }
    }

    @Override
    public void updateTips(String tip) {
        tvTip.setText(tip);
    }

    @Override
    public void resetAllUI() {
//        ivFace.setImageBitmap(null);
        ivIdCard.setImageBitmap(null);
        tvSimilar.setText("");
        tvTip.setText("");
        tvName.setText("");
        tvOrigin.setText("");
        tvOStatus.setText("");
        tvTemp.setText("");
        ivSimilarIcon.setImageBitmap(null);
        ivStatus.setImageBitmap(null);
        tvWhiteStatus.setText("");
        ivWhiteStatus.setImageBitmap(null);
        if (verifyStatusTip.isShown()) {
            verifyStatusTip.setVisibility(View.GONE);
        }
    }

    @Override
    public void updateRealTimeTemper(float temper, boolean isTempNormal) {
        tvTemp.setText(temper + "℃");
        if (isTempNormal) {
            tvOStatus.setText(getResString(R.string.act_certificates_normal));
            tvOStatus.setTextColor(Color.GREEN);
            tvTemp.setTextColor(Color.GREEN);
            ivStatus.setImageResource(R.mipmap.icon_normal);
        } else {
            tvOStatus.setText(getResString(R.string.act_certificates_warning));
            tvOStatus.setTextColor(Color.RED);
            tvTemp.setTextColor(Color.RED);
            ivStatus.setImageResource(R.mipmap.icon_warning);
        }
    }

    @Override
    public void clearRealTimeTemper() {
        tvOrigin.setText("");
        tvOStatus.setText("");
        tvTemp.setText("");
        ivStatus.setImageBitmap(null);
    }

    private String mEntryId;

    @Override
    public void getUserInfoByCode(UserInfo userInfo) {
        String head = userInfo.head;
        Glide.with(this).load(head).asBitmap().into(ivIdCard);

        tvName.setText(userInfo.name);
        tvOriginT.setText("部门:");
        tvOrigin.setText(userInfo.dept);
        mEntryId = userInfo.entryId;
    }

    @Override
    public void updateCodeTemperResult(float finalTemper, boolean normal) {
        tvTemp.setText(finalTemper + "℃");
        if (normal) {
            tvTip.setText("体温正常");
            tvOStatus.setText("正常");
            tvOStatus.setTextColor(Color.GREEN);
            tvTemp.setTextColor(Color.GREEN);
            ivStatus.setImageResource(R.mipmap.icon_normal);
        } else {
            tvTip.setText("体温异常");
            tvOStatus.setText("异常");
            tvOStatus.setTextColor(Color.RED);
            tvTemp.setTextColor(Color.RED);
            ivStatus.setImageResource(R.mipmap.icon_warning);
        }

        if (!verifyStatusTip.isShown()) {
            verifyStatusTip.setVisibility(View.VISIBLE);
        }

        if (normal) {
            llBgVerifyStatus.setBackgroundResource(R.mipmap.bg_verify_pass);
            ivVerifyStatus.setImageResource(R.mipmap.icon_verify_pass);
            tvVerifyInfo.setText("可以通行");
        } else {
            llBgVerifyStatus.setBackgroundResource(R.mipmap.bg_verify_nopass);
            ivVerifyStatus.setImageResource(R.mipmap.icon_verify_nopass);
            tvVerifyInfo.setText("禁止通行");
        }

        BitmapDrawable bitmapDrawable = (BitmapDrawable) ivFace.getDrawable();
        Bitmap faceImage = bitmapDrawable.getBitmap();
        BitmapDrawable hotImageBitmap = (BitmapDrawable) ivHotImage.getDrawable();
        Bitmap hotImage = hotImageBitmap.getBitmap();
        SignManager.instance().uploadCodeVerifyResult(mEntryId, normal, faceImage, finalTemper, hotImage);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateInfoEvent event) {

        Company company = SpUtils.getCompany();

        EventBus.getDefault().post(new UpdateMediaEvent());

        ImageFileLoader.i().loadAndSave(this, company.getComlogo(), Constants.DATA_PATH, ivLogo);

        setDeviceInfo();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(DisplayOrientationEvent event) {
        Log.e(TAG, "update: 收到摄像头更新事件");
        certificatesView.changeAngle();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(ResetLogoEvent event) {
        Company company = SpUtils.getCompany();
        ImageFileLoader.i().loadAndSave(this, company.getComlogo(), Constants.DATA_PATH, ivLogo);
    }

    //密码弹窗
    private void inputPwd(final Runnable runnable) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_input_pwd);

        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_edt_shake);
        final View rootView = dialog.findViewById(R.id.ll_input_pwd);
        Button btnConfirm = dialog.findViewById(R.id.btn_input_confirm);
        final EditText edtPwd = dialog.findViewById(R.id.edt_input_pwd);
        btnConfirm.setOnClickListener(v -> {
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

    private void goSetting() {
        String pwd = SpUtils.getStr(SpUtils.MENU_PWD);
        if (!TextUtils.isEmpty(pwd)) {
            inputPwd(() -> startActivity(new Intent(CertificatesActivity.this, CertificatesSystemActivity.class)));
            return;
        }
        startActivity(new Intent(CertificatesActivity.this, CertificatesSystemActivity.class));
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
        }, (dialog, which) -> onBackKeyPressed(new Runnable() {
            @Override
            public void run() {
                APP.exit();
            }
        }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        collectPhone = SpUtils.getBoolean(CertificatesConst.Key.COLLECT_PHONE_ENABLED, CertificatesConst.Default.COLLECT_PHONE_ENABLED);
        if(btnNoIdCard != null){
            boolean icCardMode = SpUtils.getBoolean(CertificatesConst.Key.IC_CARD_MODE,CertificatesConst.Default.IC_CARD_MODE);
            if(icCardMode){
                btnNoIdCard.setVisibility(View.GONE);
            } else {
                if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE){
                    btnNoIdCard.setVisibility(View.VISIBLE);
                }
            }
        }

        boolean aBoolean = SpUtils.getBoolean(CertificatesConst.Key.WHITE_LIST, CertificatesConst.Default.WHITE_LIST);
        if (aBoolean) {
            tvWhiteLabel.setVisibility(View.VISIBLE);
            llWhiteStatus.setVisibility(View.VISIBLE);
        } else {
            tvWhiteLabel.setVisibility(View.GONE);
            llWhiteStatus.setVisibility(View.GONE);
        }

        certificatesView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        certificatesView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SyncManager.instance().destory();
        destoryXmpp();
        certificatesView.destory();
        EventBus.getDefault().unregister(this);
    }

}
