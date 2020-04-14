package com.yunbiao.ybsmartcheckin_live_id.activity_temper_multiple;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.bean.BlackBody;
import com.intelligence.hardware.temperature.bean.FaceIndexInfo;
import com.intelligence.hardware.temperature.callback.HotImageK6080CallBack;
import com.intelligence.hardware.temperature.callback.UsbPermissionCallBack;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.ButtonClickListener;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AutoCorrectionActivity extends BaseActivity {
    private boolean mThermalMirror;
    private Rect mBlackBodyAreaRect;
    private int mPreValue;
    private ImageView ivHotImage;
    private float mCorrectValue;
    private float mBodyTemper;
    private EditText edtCorrect;
    private Button btnCorrectPlus;
    private Button btnCorrectSub;
    private EditText edtBodyTemper;
    private Button btnBodyTemperPlus;
    private Button btnBodyTemperSub;
    private TextView tvOriginT;
    private Button btnConfirm;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_auto_correction;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_auto_correction;
    }

    @Override
    protected void initView() {
        //矫正值
        edtCorrect = findViewById(R.id.edt_correct_safety_check);
        //矫正加
        btnCorrectPlus = findViewById(R.id.btn_correct_plus_safety_check);
        //矫正减
        btnCorrectSub = findViewById(R.id.btn_correct_sub_safety_check);
        //体温值
        edtBodyTemper = findViewById(R.id.edt_body_temper_safety_check);
        //体温加
        btnBodyTemperPlus = findViewById(R.id.btn_body_temper_plus_safety_check);
        //体温减
        btnBodyTemperSub = findViewById(R.id.btn_body_temper_sub_safety_check);
        //测温值
        tvOriginT = findViewById(R.id.tv_oringinT_safety_check);
        //确定键
        btnConfirm = findViewById(R.id.btn_confirm_correction_safety_check);
        //热成像
        ivHotImage = findViewById(R.id.iv_hot_image);
    }

    @Override
    protected void initData() {
        mPreValue = SpUtils.getIntOrDef(MultiThermalConst.Key.BLACK_BODY_PRE_VALUE, MultiThermalConst.Default.BLACK_BODY_PRE_VALUE);
        mThermalMirror = SpUtils.getBoolean(MultiThermalConst.Key.THERMAL_MIRROR, MultiThermalConst.Default.THERMAL_MIRROR);

        mCorrectValue = SpUtils.getFloat(MultiThermalConst.Key.CORRECT_VALUE, MultiThermalConst.Default.BODY_CORRECT_TEMPER);
        mBodyTemper = SpUtils.getFloat(MultiThermalConst.Key.BODY_TEMPER,MultiThermalConst.Default.BODY_TEMPER);
        d("缓存补正值：" + mCorrectValue);
        d("缓存体温值：" + mBodyTemper);

        int left = SpUtils.getIntOrDef(MultiThermalConst.Key.CORRECT_AREA_LEFT, MultiThermalConst.Default.CORRECT_AREA_LEFT);
        int top = SpUtils.getIntOrDef(MultiThermalConst.Key.CORRECT_AREA_TOP, MultiThermalConst.Default.CORRECT_AREA_TOP);
        int right = SpUtils.getIntOrDef(MultiThermalConst.Key.CORRECT_AREA_RIGHT, MultiThermalConst.Default.CORRECT_AREA_RIGHT);
        int bottom = SpUtils.getIntOrDef(MultiThermalConst.Key.CORRECT_AREA_BOTTOM, MultiThermalConst.Default.CORRECT_AREA_BOTTOM);
        mBlackBodyAreaRect = new Rect(left, top, right, bottom);

        startHotImage();

        initCorrectionArea(null);
    }

    private void startHotImage(){
        int usbPermission = TemperatureModule.getIns().initUsbDevice(this, new UsbPermissionCallBack() {
            @Override
            public void usbPermissionNotice(boolean b) {
                if (!b) {
                    UIUtils.showShort(AutoCorrectionActivity.this, getResources().getString(R.string.main_permission_failed_multi_thermal));
                    return;
                }

                //usb设备初始化成功
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //开启热成像6080模块
                        //isMirror:热成像画面是否左右镜像, isCold:是否为低温补偿模式, hotImageK6080CallBack:数据回调
                        TemperatureModule.getIns().startHotImageK6080(mThermalMirror, false, hotImageK6080CallBack);
                        BlackBody blackBody = new BlackBody(mBlackBodyAreaRect.left, mBlackBodyAreaRect.right, mBlackBodyAreaRect.top, mBlackBodyAreaRect.bottom);
                        blackBody.setFrameColor(Color.WHITE);
                        blackBody.setDrawFrame(false);
                        blackBody.setTempPreValue(mPreValue);
                        TemperatureModule.getIns().setmCorrectionValue(mCorrectValue);
                        TemperatureModule.getIns().startK6080BlackBodyMode(blackBody);
                        TemperatureModule.getIns().closeK6080BlackBodyMode();
                    }
                }, 1000);
            }
        });

        if (usbPermission == -1) {
            UIUtils.showShort(AutoCorrectionActivity.this, getResources().getString(R.string.main_permission_failed_multi_thermal));
        } else if (usbPermission == 0) {
            UIUtils.showShort(AutoCorrectionActivity.this, getResources().getString(R.string.main_not_found_usb_multi_thermal));
        } else if (usbPermission == 1) {
            //usb设备初始化成功
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //开启热成像6080模块
                    //isMirror:热成像画面是否左右镜像, isCold:是否为低温补偿模式, hotImageK6080CallBack:数据回调
                    TemperatureModule.getIns().startHotImageK6080(mThermalMirror, false, hotImageK6080CallBack);
                    BlackBody blackBody = new BlackBody(mBlackBodyAreaRect.left, mBlackBodyAreaRect.right, mBlackBodyAreaRect.top, mBlackBodyAreaRect.bottom);
                    blackBody.setFrameColor(Color.WHITE);
                    blackBody.setDrawFrame(false);
                    blackBody.setTempPreValue(mPreValue);
                    TemperatureModule.getIns().setmCorrectionValue(mCorrectValue);
                    TemperatureModule.getIns().startK6080BlackBodyMode(blackBody);
                    TemperatureModule.getIns().closeK6080BlackBodyMode();
                }
            }, 1000);
        }
    }

    private HotImageK6080CallBack hotImageK6080CallBack = new HotImageK6080CallBack() {
        @Override
        public void newestHotImageData(Bitmap bitmap, float v, float v1, float v2) {
            setFaceIndex();
        }

        @Override
        public void newestHotImageData(Bitmap bitmap, ArrayList<FaceIndexInfo> arrayList) {
            ivHotImage.post(() -> ivHotImage.setImageBitmap(bitmap));

            if (arrayList == null || arrayList.size() <= 0) {
                return;
            }

            FaceIndexInfo faceIndexInfo = arrayList.get(0);
            float originalTempF = faceIndexInfo.getOriginalTempF();

            if (isCorrecting) {
                mCorrectionTemperList.add(originalTempF);
                if (mCorrectionTemperList.size() > 30) {
                    mCorrectionTemperList.remove(0);
                }
                return;
            }
        }
    };

    private ArrayList<FaceIndexInfo> faceIndexInfos = new ArrayList<>();
    private Rect mTemperRect = new Rect(20,20,60,40);
    //设置人脸框
    private void setFaceIndex() {
        if (faceIndexInfos != null && faceIndexInfos.size() > 0) {
            faceIndexInfos.clear();
        }
        //可以支持一次传入多个人脸框信息
        faceIndexInfos.add(new FaceIndexInfo(0, mTemperRect.left, mTemperRect.right, mTemperRect.top, mTemperRect.bottom));

        //设置热成像画面中人脸框的颜色，默认为黑色
        TemperatureModule.getIns().setFaceFrameColor(0xFFFFFFFF);
        //人脸框信息faceIndexInfoList，是否在热成像画面中绘制人脸框(true为绘制)
        TemperatureModule.getIns().setFaceIndexInfoList(faceIndexInfos, true);
    }

    /**
     * ==校准区=======================================================================================
     */
    private boolean isCorrecting = false;//矫正标识
    private float measureT = 0.0f;//测出的温度
    private List<Float> mCorrectionTemperList = new ArrayList<>();//测温集合
    private int time = 3;//测温延时
    private float mCurrCorrectValue = 0.0f;//当前校正值（修改时不会影响缓存值）
    private float mCurrBodyTemper = 36.5f;//当前体温值（修改时不会影响缓存值）

    public void startCorrect(View view){
        btnCorrectPlus.setEnabled(false);
        btnCorrectSub.setEnabled(false);
        btnBodyTemperPlus.setEnabled(false);
        btnBodyTemperSub.setEnabled(false);
        btnConfirm.setEnabled(false);
        mHandler.sendEmptyMessageDelayed(0, 200);
    }

    private void initCorrectionArea(Runnable runnable) {
        d("补正值：" + mCorrectValue);
        d("体温值：" + mBodyTemper);
        //值显示
        mCurrCorrectValue = mCorrectValue;
        mCurrBodyTemper = mBodyTemper;
        tvOriginT.setText(measureT + "℃");
        edtBodyTemper.setText(mCurrBodyTemper + "");
        edtCorrect.setText(mCurrCorrectValue + "");

        //校正值加减
        ButtonClickListener buttonLongClick = new ButtonClickListener() {
            @Override
            public void onShortClick(int viewId) {
                if (viewId == R.id.btn_correct_plus_safety_check) {
                    mCurrCorrectValue += 0.1f;
                    mCurrBodyTemper += 0.1f;
                } else {
                    mCurrCorrectValue -= 0.1f;
                    mCurrBodyTemper -= 0.1f;
                }

                mCurrCorrectValue = formatF(mCurrCorrectValue);
                edtCorrect.setText("" + mCurrCorrectValue);
                TemperatureModule.getIns().setmCorrectionValue(mCurrCorrectValue);

                mCurrBodyTemper = formatF(mCurrBodyTemper);
                edtBodyTemper.setText(mCurrBodyTemper + "");
            }

            @Override
            public void onLongClick(int viewId) {
                if (viewId == R.id.btn_correct_plus_safety_check) {
                    mCurrCorrectValue += 0.1f;
                    mCurrBodyTemper += 0.1f;
                } else {
                    mCurrCorrectValue -= 0.1f;
                    mCurrBodyTemper -= 0.1f;
                }
                mCurrCorrectValue = formatF(mCurrCorrectValue);
                edtCorrect.setText("" + mCurrCorrectValue);
                TemperatureModule.getIns().setmCorrectionValue(mCurrCorrectValue);

                mCurrBodyTemper = formatF(mCurrBodyTemper);
                edtBodyTemper.setText(mCurrBodyTemper + "");
            }

            @Override
            public void onLongClickFinish(int viewId) {
            }
        };
        btnCorrectSub.setOnTouchListener(buttonLongClick);
        btnCorrectPlus.setOnTouchListener(buttonLongClick);

        //体温加减
        ButtonClickListener bodyTemperClick = new ButtonClickListener() {
            @Override
            public void onShortClick(int viewId) {
                if (viewId == R.id.btn_body_temper_plus_safety_check) {
                    mCurrBodyTemper += 0.1f;
                    mCurrCorrectValue += 0.1f;
                } else {
                    mCurrBodyTemper -= 0.1f;
                    mCurrCorrectValue -= 0.1f;
                }

                mCurrBodyTemper = formatF(mCurrBodyTemper);
                edtBodyTemper.setText(mCurrBodyTemper + "");

                mCurrCorrectValue = formatF(mCurrCorrectValue);
                edtCorrect.setText("" + mCurrCorrectValue);
                TemperatureModule.getIns().setmCorrectionValue(mCurrCorrectValue);
            }

            @Override
            public void onLongClick(int viewId) {
                if (viewId == R.id.btn_body_temper_plus_safety_check) {
                    mCurrBodyTemper += 0.1f;
                    mCurrCorrectValue += 0.1f;
                } else {
                    mCurrBodyTemper -= 0.1f;
                    mCurrCorrectValue -= 0.1f;
                }
                mCurrBodyTemper = formatF(mCurrBodyTemper);
                edtBodyTemper.setText(mCurrBodyTemper + "");

                mCurrCorrectValue = formatF(mCurrCorrectValue);
                edtCorrect.setText("" + mCurrCorrectValue);
                TemperatureModule.getIns().setmCorrectionValue(mCurrCorrectValue);
            }

            @Override
            public void onLongClickFinish(int viewId) {
            }
        };
        btnBodyTemperSub.setOnTouchListener(bodyTemperClick);
        btnBodyTemperPlus.setOnTouchListener(bodyTemperClick);

        //取消、确定键
        View.OnClickListener onClickListener = v -> {
            measureT = 0.0f;
            if (runnable != null) {
                runnable.run();
            }
            if (v.getId() == R.id.btn_confirm_correction_safety_check) {
                UIUtils.showShort(AutoCorrectionActivity.this, APP.getContext().getResources().getString(R.string.safety_save_completion));
                mCorrectValue = mCurrCorrectValue;
                mBodyTemper = mCurrBodyTemper;
                d("补正值：" + mCorrectValue);
                d("体温值：" + mBodyTemper);

                SpUtils.saveFloat(MultiThermalConst.Key.CORRECT_VALUE, mCorrectValue);
                SpUtils.saveFloat(MultiThermalConst.Key.BODY_TEMPER, mBodyTemper);
                //点击确定或取消，
                TemperatureModule.getIns().setmCorrectionValue(mCorrectValue);
            }
        };
        btnConfirm.setOnClickListener(onClickListener);
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0://准备开始
                    if (time < 1) {
                        time = 3;
                        TemperatureModule.getIns().setmCorrectionValue(0.0f);
                        sendEmptyMessageDelayed(1, 200);
                        break;
                    }
                    UIUtils.showShort(AutoCorrectionActivity.this,
                            APP.getContext().getResources().getString(R.string.safety_correct_temper_tip) + " " + time);
                    time--;
                    sendEmptyMessageDelayed(0, 1000);
                    break;
                case 1:
                    isCorrecting = true;
                    UIUtils.showShort(AutoCorrectionActivity.this, APP.getContext().getResources().getString(R.string.safety_correct_temper_ing));
                    if (mCorrectionTemperList.size() >= 30) {
                        UIUtils.showShort(AutoCorrectionActivity.this, APP.getContext().getResources().getString(R.string.safety_correct_temper_completion));

                        measureT = Collections.max(mCorrectionTemperList);
                        tvOriginT.setText(measureT + "℃");
                        if (measureT <= mCurrBodyTemper) {
                            tvOriginT.setTextColor(Color.GREEN);
                        } else {
                            tvOriginT.setTextColor(Color.WHITE);
                        }
                        mCurrCorrectValue = mCurrBodyTemper - measureT;
                        mCurrCorrectValue = formatF(mCurrCorrectValue);
                        edtCorrect.setText(mCurrCorrectValue + "");
                        TemperatureModule.getIns().setmCorrectionValue(mCurrCorrectValue);

                        btnCorrectPlus.setEnabled(true);
                        btnCorrectSub.setEnabled(true);
                        btnBodyTemperPlus.setEnabled(true);
                        btnBodyTemperSub.setEnabled(true);
                        btnConfirm.setEnabled(true);

                        isCorrecting = false;
                        mCorrectionTemperList.clear();
                        break;
                    }
                    sendEmptyMessageDelayed(1, 1000);
                    break;
            }
        }
    };

    private float getMean(List<Float> array) {
        float result = 0.0f;
        if (array.size() == 0) {
            return result;
        }
        for (float anArray : array) {
            result += anArray;
        }
        result = result / array.size();
        result = formatF(result);
        return result;
    }

}
