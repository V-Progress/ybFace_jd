package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.callback.HotImageK3232CallBack;
import com.intelligence.hardware.temperature.callback.InfraredTempCallBack;
import com.yunbiao.faceview.CompareResult;
import com.yunbiao.faceview.FacePreviewInfo;
import com.yunbiao.faceview.FaceView;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseGpioActivity;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

public class TemperatureCorrectActivity extends BaseGpioActivity {
    private static final String TAG = "TemperatureCorrectActiv";
    private boolean thermalMirror;
    private boolean lowTemp;
    private float ambientCorrect;
    private TextView tvThermalTemp;
    private Button btnSubTempCorr;
    private Button btnAddTempCorr;
    private EditText edtValueTemp;
    private ImageView ivThermalCorr;
    private FaceView faceView;
    private Button btnClickTemp;
    private TextView tvCurrentCorr;

    private List<Float> tempList = new ArrayList<>();//温度缓存list
    private boolean isDetecting = false;//
    private float mMeanValue = 0.0f;
    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_temperature_correct;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_temperature_correct_h;
    }

    @Override
    protected void initView() {
        btnClickTemp = findViewById(R.id.btn_click_temp_corr);
        tvThermalTemp = findViewById(R.id.tv_thermal_temp_correct);
        btnSubTempCorr = findViewById(R.id.btn_sub_temp_correct);
        btnAddTempCorr = findViewById(R.id.btn_add_temp_correct);
        edtValueTemp = findViewById(R.id.edt_value_temp_gun);
        ivThermalCorr = findViewById(R.id.iv_thermal_corr);
        faceView = findViewById(R.id.face_view);
        tvCurrentCorr = findViewById(R.id.tv_current_corr);

        btnSubTempCorr.setOnClickListener(onClickListener);
        btnAddTempCorr.setOnClickListener(onClickListener);
        btnClickTemp.setOnClickListener(onClickListener);

        thermalMirror = SpUtils.getBoolean(ThermalConst.Key.THERMAL_MIRROR, ThermalConst.Default.THERMAL_MIRROR);
        lowTemp = SpUtils.getBoolean(ThermalConst.Key.LOW_TEMP_MODE, ThermalConst.Default.LOW_TEMP);
        ambientCorrect = SpUtils.getFloat(ThermalConst.Key.AMBIENT_CORRECT, ThermalConst.Default.AMBIENT_CORRECT);

        float thermalCorr = SpUtils.getFloat(ThermalConst.Key.THERMAL_CORRECT, ThermalConst.Default.THERMAL_CORRECT);
        tvCurrentCorr.setText("当前补正值：" + thermalCorr + "℃");

        edtValueTemp.setText(0.0 + "");
        edtValueTemp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        faceView.setCallback(faceCallback);
    }

    @Override
    protected void initData() {
        int currMode = SpUtils.getIntOrDef(ThermalConst.Key.MODE, ThermalConst.Default.MODE);//当前模式
        if (currMode == ThermalConst.ONLY_INFRARED || currMode == ThermalConst.FACE_INFRARED) {
            TemperatureModule.getIns().setInfraredTempCallBack(infraredTempCallBack);
        } else if (currMode == ThermalConst.ONLY_THERMAL_HM_32_32 || currMode == ThermalConst.FACE_THERMAL_HM_32_32) {
            TemperatureModule.getIns().startHotImageK3232(thermalMirror, lowTemp, hotImageK3232CallBack);
        }
    }

    private HotImageK3232CallBack hotImageK3232CallBack = new HotImageK3232CallBack() {
        @Override
        public void newestHotImageData(final Bitmap imageBmp, final float sensorT, final float maxT, final float minT, final float bodyMaxT, final boolean isBody, final int bodyPercentage) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ivThermalCorr.setImageBitmap(imageBmp);
                }
            });

            if (isDetecting) {
                if (tempList.size() < 5) {
                    tempList.add(sensorT);
                } else {
                    mMeanValue = getMean(tempList);
                    tempList.clear();
                    isDetecting = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UIUtils.dismissNetLoading();
                            tvThermalTemp.setText(mMeanValue + "℃");
                            btnClickTemp.setEnabled(true);
                        }
                    });

                }
            }
        }

        @Override
        public void dataRecoveryFailed() {

        }
    };

    private InfraredTempCallBack infraredTempCallBack = new InfraredTempCallBack() {
        @Override
        public void newestInfraredTemp(float measureF, float afterF, float v2) {
            if (isDetecting) {
                if (tempList.size() < 5) {
                    tempList.add(measureF);
                } else {
                    mMeanValue = getMean(tempList);
                    tempList.clear();
                    isDetecting = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UIUtils.dismissNetLoading();
                            tvThermalTemp.setText(mMeanValue + "℃");
                            btnClickTemp.setEnabled(true);
                        }
                    });

                }
            }
        }
    };

    private FaceView.FaceCallback faceCallback = new FaceView.FaceCallback() {
        @Override
        public void onReady() {

        }

        @Override
        public void onFaceDetection(Boolean hasFace, List<FacePreviewInfo> facePreviewInfoList) {

        }

        @Override
        public boolean onFaceDetection(boolean hasFace, FacePreviewInfo facePreviewInfo) {
            return false;
        }

        @Override
        public void onFaceVerify(CompareResult faceAuth) {

        }
    };

    public void onSave(View view) {
        String s = edtValueTemp.getText().toString();
        if (!TextUtils.isEmpty(s)) {

            float v = Float.parseFloat(s);
            if (v == 0.0f && mMeanValue == 0.0f) {
                UIUtils.showShort(this, "未修改补正值");
            } else {
                float corrValue = 0.0f;
                if (v == mMeanValue) {
                    corrValue = 0.0f;
                } else if (v > mMeanValue) {
                    corrValue = v - mMeanValue;
                } else {
                    corrValue = -(mMeanValue - v);
                }
                corrValue = formatF(corrValue);
                tvCurrentCorr.setText("当前补正值：" + corrValue + "℃");
                Log.e(TAG, "onDestroy: ---------------- " + corrValue);
                SpUtils.saveFloat(ThermalConst.Key.THERMAL_CORRECT, corrValue);
                UIUtils.showShort(this, "修改成功，补正值：" + corrValue);
            }
        } else {
            UIUtils.showShort(this, "未修改补正值");
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_click_temp_corr) {
                v.setEnabled(false);
                UIUtils.showNetLoading(TemperatureCorrectActivity.this);
                isDetecting = true;
                return;
            }
            if (v.getId() == R.id.ib_back) {
                finish();
                return;
            }
            String s = edtValueTemp.getText().toString();
            float v1 = Float.parseFloat(s);
            if (v.getId() == R.id.btn_sub_temp_correct) {
                v1 -= 0.1f;
            } else {
                v1 += 0.1f;
            }
            v1 = formatF(v1);
            edtValueTemp.setText(v1 + "");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TemperatureModule.getIns().closeHotImageK3232();
    }

}
