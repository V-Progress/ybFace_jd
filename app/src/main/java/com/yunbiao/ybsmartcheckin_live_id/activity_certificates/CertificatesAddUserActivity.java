package com.yunbiao.ybsmartcheckin_live_id.activity_certificates;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.wang.avi.AVLoadingIndicatorView;
import com.yunbiao.faceview.CompareResult;
import com.yunbiao.faceview.FacePreviewInfo;
import com.yunbiao.faceview.FaceView;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.CertificatesUser;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.utils.ScanKeyManager;
import com.yunbiao.ybsmartcheckin_live_id.utils.SoftKeyBoardListener;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CertificatesAddUserActivity extends BaseActivity implements FaceView.FaceCallback {

    @BindView(R.id.face_view)
    FaceView faceView;
    @BindView(R.id.btn_TakePhoto)
    Button btnTakePhoto;
    @BindView(R.id.alv_take_photo)
    AVLoadingIndicatorView alvTakePhoto;
    @BindView(R.id.et_depart)
    EditText etDepart;
    @BindView(R.id.et_num)
    EditText etNum;
    @BindView(R.id.et_name)
    EditText etName;
    @BindView(R.id.iv_capture)
    ImageView ivCapture;
    private int mHasFace;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_certificates_add_user;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_certificates_add_user;
    }

    @Override
    protected void initView() {
        faceView.setCallback(this);
    }

    @Override
    protected void initData() {
        initScanQrCodeReader();
    }

    @Override
    public void onReady() {

    }

    @Override
    public void onFaceDetection(Boolean hasFace, List<FacePreviewInfo> facePreviewInfoList) {

    }

    @Override
    public boolean onFaceDetection(boolean hasFace, FacePreviewInfo facePreviewInfo) {
        if (hasFace) {
            mHasFace = facePreviewInfo != null ? 1 : -1;
        } else {
            mHasFace = -1;
        }
        return false;
    }

    @Override
    public void onFaceVerify(CompareResult faceAuth) {

    }

    @OnClick(R.id.btn_TakePhoto)
    public void onViewClicked() {
        alvTakePhoto.setVisibility(View.VISIBLE);
        btnTakePhoto.setVisibility(View.GONE);
        handler.sendEmptyMessage(0);
    }

    private String mCurrPhotoPath;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (mHasFace == -1) {
                UIUtils.showTitleTip(CertificatesAddUserActivity.this, APP.getContext().getResources().getString(R.string.act_editEmploy_no_face));
                alvTakePhoto.setVisibility(View.GONE);
                btnTakePhoto.setVisibility(View.VISIBLE);
            } else if (mHasFace == -2) {
                UIUtils.showTitleTip(CertificatesAddUserActivity.this, APP.getContext().getResources().getString(R.string.act_editEmploy_please_face_only_one));
                alvTakePhoto.setVisibility(View.GONE);
                btnTakePhoto.setVisibility(View.VISIBLE);
            } else {
                Bitmap bitmap = faceView.takePicture();
                if (bitmap != null) {
                    mCurrPhotoPath = saveBitmap(bitmap);
                    Glide.with(CertificatesAddUserActivity.this).load(mCurrPhotoPath).asBitmap().override(100, 100).into(ivCapture);
                } else {
                    UIUtils.showTitleTip(CertificatesAddUserActivity.this, APP.getContext().getResources().getString(R.string.act_editEmploy_photo_failed_try_again));
                }
                alvTakePhoto.setVisibility(View.GONE);
                btnTakePhoto.setVisibility(View.VISIBLE);
            }
            return true;
        }
    });

    /***
     * 保存图片到本地
     * @param mBitmap
     * @return
     */
    public static String saveBitmap(Bitmap mBitmap) {
        File filePic;
        try {
            //格式化时间
            long time = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String sdfTime = sdf.format(time);
            filePic = new File(Constants.HEAD_PATH + sdfTime + "_m.jpg");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return filePic.getAbsolutePath();
    }

    @OnClick({R.id.btn_submit, R.id.btn_cancle})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_submit:
                if(TextUtils.isEmpty(mCurrPhotoPath)){
                    UIUtils.showShort(this,getResString(R.string.act_editEmploy_tip_qxpz));
                    return;
                }

                String name = etName.getText().toString();
                if(TextUtils.isEmpty(name)){
                    UIUtils.showShort(this,getResString(R.string.employ_list_please_input_name));
                    return;
                }

                String depart = etDepart.getText().toString();
                if(TextUtils.isEmpty(depart)){
                    UIUtils.showShort(this,getResString(R.string.act_departList_tip_qsrbmmc));
                    return;
                }

                String num = etNum.getText().toString();
                if(TextUtils.isEmpty(num)){
                    UIUtils.showShort(this,"please input the card number");
                    return;
                }

                CertificatesUser user = new CertificatesUser();
                user.setName(name);
                user.setNum(num);
                user.setDepart(depart);
                user.setHeadPath(mCurrPhotoPath);
                long l = DaoManager.get().addOrUpdate(user);
                if(l >= 0){
                    UIUtils.showShort(this,getResString(R.string.act_editEmploy_tip_add_success));
                } else {
                    UIUtils.showShort(this,getResString(R.string.act_editEmploy_add_face_failed));
                }
                break;
            case R.id.btn_cancle:
                finish();
                break;
        }
    }

    /*==扫码相关=======================================================================================*/
    /*==扫码相关=======================================================================================*/
    /*==扫码相关=======================================================================================*/
    /*==扫码相关=======================================================================================*/
    /*==扫码相关=======================================================================================*/
    /* *
     * 读卡器初始化
     */
    private ScanKeyManager scanKeyManager;

    private void initScanQrCodeReader() {
        //读卡器声明
        scanKeyManager = new ScanKeyManager(value -> {
            etNum.setText(value);
        });
        onKeyBoardListener();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() != KeyEvent.KEYCODE_BACK && !isInput) {
            scanKeyManager.analysisKeyEvent(event);
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    private boolean isInput = false;

    //监听软件盘是否弹起
    private void onKeyBoardListener() {
        SoftKeyBoardListener.setListener(this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                Log.e("软键盘", "键盘显示 高度" + height);
                isInput = true;
            }

            @Override
            public void keyBoardHide(int height) {
                Log.e("软键盘", "键盘隐藏 高度" + height);
                isInput = false;
            }
        });
    }
}
