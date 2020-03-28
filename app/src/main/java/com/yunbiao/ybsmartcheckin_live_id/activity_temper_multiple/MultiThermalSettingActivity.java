package com.yunbiao.ybsmartcheckin_live_id.activity_temper_multiple;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;

import com.google.gson.Gson;
import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.bean.BlackBody;
import com.intelligence.hardware.temperature.bean.FaceIndexInfo;
import com.intelligence.hardware.temperature.callback.HotImageK6080CallBack;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.PowerOnOffActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

import java.util.ArrayList;

public class MultiThermalSettingActivity extends BaseActivity {

    private ImageView ivHotImage;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_multi_thermal_setting;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_multi_thermal_setting;
    }

    @Override
    protected void initView() {
        super.initView();

        initFaceRectMirrorSetting();

        setLowTemp();

        setWarningTemper();

        setHotImageMirror();

        setBodyCorrect();
    }

    private void setBodyCorrect() {
        Button btnSub = findViewById(R.id.btn_body_correct_sub_setting);
        Button btnAdd = findViewById(R.id.btn_body_correct_add_setting);
        final EditText edtCorrect = findViewById(R.id.edt_body_correct__setting);
        float mBodyCorrect = SpUtils.getFloat(MultiThermalConst.Key.BODY_CORRECT_TEMPER, MultiThermalConst.Default.BODY_CORRECT_TEMPER);
        edtCorrect.setText(mBodyCorrect + "");
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = edtCorrect.getText().toString();
                float v1 = formatF(Float.parseFloat(value));
                if (v.getId() == R.id.btn_body_correct_sub_setting) {
                    v1 -= 0.1f;
                } else {
                    v1 += 0.1f;
                }
                v1 = formatF(v1);
                edtCorrect.setText(v1 + "");
                SpUtils.saveFloat(MultiThermalConst.Key.BODY_CORRECT_TEMPER, v1);
            }
        };
        btnSub.setOnClickListener(onClickListener);
        btnAdd.setOnClickListener(onClickListener);
    }

    private void setLowTemp() {
        Switch swLowTemp = findViewById(R.id.sw_low_temp_model_setting);
        boolean lowTemp = SpUtils.getBoolean(MultiThermalConst.Key.LOW_TEMP, MultiThermalConst.Default.LOW_TEMP);
        swLowTemp.setChecked(lowTemp);
        swLowTemp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(MultiThermalConst.Key.LOW_TEMP, MultiThermalConst.Default.LOW_TEMP);
            }
        });
    }

    private void setWarningTemper() {
        Button btnSub = findViewById(R.id.btn_temp_warning_threshold_sub_setting);
        final EditText edtTemper = findViewById(R.id.edt_temp_warning_threshold_setting);
        Button btnAdd = findViewById(R.id.btn_temp_warning_threshold_add_setting);

        float warningValue = SpUtils.getFloat(MultiThermalConst.Key.WARNING_TEMP, MultiThermalConst.Default.WARNING_TEMP);
        edtTemper.setText(warningValue + "");
        View.OnClickListener warnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = edtTemper.getText().toString();
                float v1 = formatF(Float.parseFloat(value));
                if (v.getId() == R.id.btn_temp_warning_threshold_sub_setting) {
                    v1 -= 0.1;
                } else {
                    v1 += 0.1;
                }
                v1 = formatF(v1);
                edtTemper.setText(v1 + "");
                SpUtils.saveFloat(MultiThermalConst.Key.WARNING_TEMP, v1);
            }
        };
        btnSub.setOnClickListener(warnClickListener);
        btnAdd.setOnClickListener(warnClickListener);
    }


    //初始化人脸框镜像设置
    private void initFaceRectMirrorSetting() {
        CheckBox cbMirror = findViewById(R.id.cb_mirror);
        //人脸框镜像
        final boolean mirror = SpUtils.isMirror();
        cbMirror.setChecked(mirror);
        cbMirror.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.setMirror(isChecked);
            }
        });
    }

    private void setHotImageMirror() {
        Switch swThermalImageMirror = findViewById(R.id.sw_thermal_imag_mirror_setting);
        boolean mirror = SpUtils.getBoolean(MultiThermalConst.Key.THERMAL_MIRROR, MultiThermalConst.Default.THERMAL_MIRROR);
        swThermalImageMirror.setChecked(mirror);
        swThermalImageMirror.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(MultiThermalConst.Key.THERMAL_MIRROR, isChecked);
            }
        });
    }

    public void powerOnOff(View view) {
        startActivity(new Intent(this, PowerOnOffActivity.class));
    }

    public void setBlackBody(View view) {
        ivHotImage = findViewById(R.id.iv_hot_image);
        View rlCorrectArea = findViewById(R.id.rl_correct_area);
        if (rlCorrectArea.isShown()) {
            rlCorrectArea.setVisibility(View.GONE);
            handler.removeCallbacks(runnable);
            TemperatureModule.getIns().closeHotImageK6080();
        } else {
            rlCorrectArea.setVisibility(View.VISIBLE);
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, 1000);

            initCorrectArea();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
    }

    public void saveRect(View view){
        if(mSaveRect.width() == 0 || mSaveRect.height() == 0){
            UIUtils.showShort(this,getResources().getString(R.string.setting_save_failed_multi_thermal));
            return;
        }
        Log.e(TAG, "onDestroy: ---------保存的Rect:"+ mSaveRect.toString());
        String rectJson = new Gson().toJson(mSaveRect);
        boolean b = SpUtils.saveStr(MultiThermalConst.Key.CORRECT_AREA_JSON, rectJson);
        if(b){
            UIUtils.showShort(this,getResources().getString(R.string.setting_save_success_multi_thermal));
        } else {
            UIUtils.showShort(this,getResources().getString(R.string.setting_save_failed_multi_thermal));
        }
    }

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            boolean mThermalMirror = SpUtils.getBoolean(MultiThermalConst.Key.THERMAL_MIRROR, MultiThermalConst.Default.THERMAL_MIRROR);
            boolean mLowTemp = SpUtils.getBoolean(MultiThermalConst.Key.LOW_TEMP, MultiThermalConst.Default.LOW_TEMP);
            float mBodyCorrectTemper = SpUtils.getFloat(MultiThermalConst.Key.BODY_CORRECT_TEMPER, MultiThermalConst.Default.BODY_CORRECT_TEMPER);
            String str = SpUtils.getStr(MultiThermalConst.Key.CORRECT_AREA_JSON, MultiThermalConst.Default.CORRECT_AREA_JSON);
            final Rect rect = new Gson().fromJson(str, Rect.class);
            //开启热成像6080模块
            //isMirror:热成像画面是否左右镜像, isCold:是否为低温补偿模式, hotImageK6080CallBack:数据回调
            TemperatureModule.getIns().startHotImageK6080(mThermalMirror, mLowTemp, new HotImageK6080CallBack() {
                @Override
                public void newestHotImageData(final Bitmap bitmap, float v, float v1, float v2) {
                    if (ivHotImage != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ivHotImage.setImageBitmap(bitmap);
                            }
                        });
                    }
                }

                @Override
                public void newestHotImageData(Bitmap bitmap, ArrayList<FaceIndexInfo> arrayList) {

                }
            });
            TemperatureModule.getIns().setmCorrectionValue(mBodyCorrectTemper);
            BlackBody blackBody = new BlackBody(rect.left, rect.right, rect.top, rect.bottom);
            blackBody.setFrameColor(Color.WHITE);
            blackBody.setTempPreValue(345);
            TemperatureModule.getIns().startK6080BlackBodyMode(blackBody);
        }
    };

    private void initCorrectArea() {
        final SeekBar xSeekBar = findViewById(R.id.sb_x_multi_setting);
        final SeekBar ySeekBar = findViewById(R.id.sb_y_multi_setting);
        final ImageView ivExample = findViewById(R.id.iv_example_multi_setting);
        ivExample.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                String str = SpUtils.getStr(MultiThermalConst.Key.CORRECT_AREA_JSON, MultiThermalConst.Default.CORRECT_AREA_JSON);
                final Rect rect = new Gson().fromJson(str, Rect.class);

                ivExample.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                final Bitmap bitmap = getBitmap(ivExample.getMeasuredWidth(), ivExample.getMeasuredHeight());

                ivExample.setImageBitmap(drawRectangles(bitmap, rect));

                xSeekBar.setProgress(rect.left);
                xSeekBar.setMax(80 - rect.width());

                ySeekBar.setProgress(rect.top);
                ySeekBar.setMax(60 - rect.height());

                final int widthHieght = 5;
                SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (seekBar.getId() == R.id.sb_x_multi_setting) {
                            rect.left = 0 + progress;
                            rect.right = widthHieght + progress;
                            if (rect.right >= 80) {
                                seekBar.setProgress(progress);
                                return;
                            }
                        } else {
                            rect.top = 0 + progress;
                            rect.bottom = widthHieght + progress;
                            if (rect.bottom >= 60) {
                                seekBar.setProgress(progress);
                                return;
                            }
                        }
                        mSaveRect.set(rect);
                        ivExample.setImageBitmap(drawRectangles(bitmap, rect));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                };

                xSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
                ySeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
            }
        });
    }

    private Rect mSaveRect = new Rect();
    private static final String TAG = "MultiThermalSettingActi";

    //生成纯色bitmap
    private Bitmap getBitmap(int measuredWidth, int measuredHeight) {
        Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_4444);
        bitmap.eraseColor(Color.parseColor("#2D000000")); // 填充颜色
        return bitmap;
    }

    //生成带框的bitmap
    private Bitmap drawRectangles(Bitmap imageBitmap, Rect rect) {
        Bitmap mutableBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);//不填充

        int widthOffset = imageBitmap.getWidth() / 80;
        int heightOffset = imageBitmap.getHeight() / 60;
        paint.setStrokeWidth(1 * widthOffset); //线的宽度

        RectF exmpRect = new RectF(rect);
        exmpRect.left *= widthOffset;
        exmpRect.right *= widthOffset;
        exmpRect.top *= heightOffset;
        exmpRect.bottom *= heightOffset;
        canvas.drawRect(exmpRect, paint);
        return mutableBitmap;
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.left_in, R.anim.left_out);

    }
}
