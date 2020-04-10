package com.yunbiao.ybsmartcheckin_live_id.activity_temper_multiple;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.text.TextUtils;
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
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.PowerOnOffActivity;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.utils.L;
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

        setThermalFaceFrame();

        setBlackBodyFrame();

        initMultiTrack();

        initBlackBodyPreValue();

        initBlackEnable();
    }

    private void initBlackEnable(){
        final View viewBlackBodyCorrection = findViewById(R.id.tv_black_body_correction_area);
        final boolean blackBodyEnable = SpUtils.getBoolean(MultiThermalConst.Key.BLACK_BODY_ENABLE,MultiThermalConst.Default.BLACK_BODY_ENABLE);
        if(blackBodyEnable){
            viewBlackBodyCorrection.setVisibility(View.VISIBLE);
        } else {
            viewBlackBodyCorrection.setVisibility(View.GONE);
        }
        Switch swBlackBodyEnable = findViewById(R.id.sw_black_body_enable_setting);
        swBlackBodyEnable.setChecked(blackBodyEnable);
        swBlackBodyEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(MultiThermalConst.Key.BLACK_BODY_ENABLE,isChecked);
                if(isChecked){
                    viewBlackBodyCorrection.setVisibility(View.VISIBLE);
                } else {
                    viewBlackBodyCorrection.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initBlackBodyPreValue(){
        final View llPreValue = findViewById(R.id.ll_black_body_pre_value);
        View viewById = findViewById(R.id.setting_title);
        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llPreValue.setVisibility(llPreValue.isShown() ? View.GONE : View.VISIBLE);
            }
        });
        int preValue = SpUtils.getIntOrDef(MultiThermalConst.Key.BLACK_BODY_PRE_VALUE,MultiThermalConst.Default.BLACK_BODY_PRE_VALUE);
        final EditText edtPreValue = findViewById(R.id.edt_black_body_pre_value);
        edtPreValue.setText(preValue + "");

        Button btnSave = findViewById(R.id.btn_black_body_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = edtPreValue.getText().toString();
                int finalValue = MultiThermalConst.Default.BLACK_BODY_PRE_VALUE;
                if(!TextUtils.isEmpty(value)){
                    finalValue = Integer.parseInt(value);
                }
                SpUtils.saveInt(MultiThermalConst.Key.BLACK_BODY_PRE_VALUE,finalValue);
                UIUtils.showShort(MultiThermalSettingActivity.this, APP.getContext().getResources().getString(R.string.setting_save_success));
            }
        });
    }

    private void initMultiTrack() {
        boolean multiTrack = SpUtils.getBoolean(MultiThermalConst.Key.MULTI_TRACK, MultiThermalConst.Default.MULTI_TRACK);
        Switch swMultiTrack = findViewById(R.id.sw_multi_track_setting);
        swMultiTrack.setChecked(multiTrack);
        swMultiTrack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(MultiThermalConst.Key.MULTI_TRACK, isChecked);
            }
        });
    }

    private void setBlackBodyFrame() {
        Switch swBlackBody = findViewById(R.id.sw_black_body_frame_setting);
        boolean blackBodyFrame = SpUtils.getBoolean(MultiThermalConst.Key.BLACK_BODY_FRAME, MultiThermalConst.Default.BLACK_BODY_FRAME);
        swBlackBody.setChecked(blackBodyFrame);
        swBlackBody.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(MultiThermalConst.Key.BLACK_BODY_FRAME, isChecked);
            }
        });
    }

    private void setThermalFaceFrame() {
        Switch swFaceFrame = findViewById(R.id.sw_thermal_face_frame_setting);
        boolean isThermalFaceFrame = SpUtils.getBoolean(MultiThermalConst.Key.THERMAL_FACE_FRAME, MultiThermalConst.Default.THERMAL_FACE_FRAME);
        swFaceFrame.setChecked(isThermalFaceFrame);
        swFaceFrame.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.saveBoolean(MultiThermalConst.Key.THERMAL_FACE_FRAME, isChecked);
            }
        });
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
                SpUtils.saveBoolean(MultiThermalConst.Key.LOW_TEMP, isChecked);
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

    public void saveRect(View view) {
        if (mSaveRect.width() == 0 || mSaveRect.height() == 0) {
            UIUtils.showShort(this, getResources().getString(R.string.setting_save_failed_multi_thermal));
            return;
        }

        int left = mSaveRect.left;
        int top = mSaveRect.top;
        int right = mSaveRect.right;
        int bottom = mSaveRect.bottom;
        SpUtils.saveInt(MultiThermalConst.Key.CORRECT_AREA_LEFT, left);
        SpUtils.saveInt(MultiThermalConst.Key.CORRECT_AREA_TOP, top);
        SpUtils.saveInt(MultiThermalConst.Key.CORRECT_AREA_RIGHT, right);
        SpUtils.saveInt(MultiThermalConst.Key.CORRECT_AREA_BOTTOM, bottom);

        L.e("MultiThermalSettingActivity", "saveRect:保存的数值：" + left + " --- " + top + " --- " + right + " --- " + bottom);

        UIUtils.showShort(this, getResources().getString(R.string.setting_save_success_multi_thermal));
    }

    private Rect getCacheRect() {
        int left = SpUtils.getIntOrDef(MultiThermalConst.Key.CORRECT_AREA_LEFT, MultiThermalConst.Default.CORRECT_AREA_LEFT);
        int top = SpUtils.getIntOrDef(MultiThermalConst.Key.CORRECT_AREA_TOP, MultiThermalConst.Default.CORRECT_AREA_TOP);
        int right = SpUtils.getIntOrDef(MultiThermalConst.Key.CORRECT_AREA_RIGHT, MultiThermalConst.Default.CORRECT_AREA_RIGHT);
        int bottom = SpUtils.getIntOrDef(MultiThermalConst.Key.CORRECT_AREA_BOTTOM, MultiThermalConst.Default.CORRECT_AREA_BOTTOM);
        return new Rect(left, top, right, bottom);
    }

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            boolean mThermalMirror = SpUtils.getBoolean(MultiThermalConst.Key.THERMAL_MIRROR, MultiThermalConst.Default.THERMAL_MIRROR);
            boolean mLowTemp = SpUtils.getBoolean(MultiThermalConst.Key.LOW_TEMP, MultiThermalConst.Default.LOW_TEMP);
            float mBodyCorrectTemper = SpUtils.getFloat(MultiThermalConst.Key.BODY_CORRECT_TEMPER, MultiThermalConst.Default.BODY_CORRECT_TEMPER);

            Rect cacheRect = getCacheRect();

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
            BlackBody blackBody = new BlackBody(cacheRect.left, cacheRect.right, cacheRect.top, cacheRect.bottom);
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

                final Rect rect = getCacheRect();

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
