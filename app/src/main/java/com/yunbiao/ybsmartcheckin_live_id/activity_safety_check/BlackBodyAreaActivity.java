package com.yunbiao.ybsmartcheckin_live_id.activity_safety_check;

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
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.bean.BlackBody;
import com.intelligence.hardware.temperature.bean.FaceIndexInfo;
import com.intelligence.hardware.temperature.callback.HotImageK6080CallBack;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

import java.util.ArrayList;

public class BlackBodyAreaActivity extends BaseActivity {

    private ImageView ivHotImage;
    private SeekBar sbX;
    private SeekBar sbY;

    private Rect mSaveRect = new Rect();
    private Rect mTempRect = new Rect();
    private ImageView ivExample;
    private RadioGroup rgTemper;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_black_body_area;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_black_body_area;
    }

    @Override
    protected void initView() {
        ivHotImage = findViewById(R.id.iv_hot_image);
        sbX = findViewById(R.id.sb_x_safety_check);
        sbY = findViewById(R.id.sb_y_safety_check);
        ivExample = findViewById(R.id.iv_rect);
        rgTemper = findViewById(R.id.rg_temper_area_rect);
        rgTemper.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_too_small) {
                    mTempRect.set(TemperRect.getTooSmall());
                    SpUtils.saveInt(ThermalSafetyCheckConst.Key.TEMPER_AREA_SIZE, ThermalSafetyCheckConst.Size.TOO_SMALL);
                } else if (checkedId == R.id.rb_small) {
                    mTempRect.set(TemperRect.getSmall());
                    SpUtils.saveInt(ThermalSafetyCheckConst.Key.TEMPER_AREA_SIZE, ThermalSafetyCheckConst.Size.SMALL);
                } else if (checkedId == R.id.rb_middle) {
                    mTempRect.set(TemperRect.getMiddle());
                    SpUtils.saveInt(ThermalSafetyCheckConst.Key.TEMPER_AREA_SIZE, ThermalSafetyCheckConst.Size.MIDDLE);
                } else {
                    mTempRect.set(TemperRect.getLarge());
                    SpUtils.saveInt(ThermalSafetyCheckConst.Key.TEMPER_AREA_SIZE, ThermalSafetyCheckConst.Size.LARGE);
                }
            }
        });
        int mCacheSize = SpUtils.getIntOrDef(ThermalSafetyCheckConst.Key.TEMPER_AREA_SIZE, ThermalSafetyCheckConst.Default.TEMPER_AREA_SIZE);
        if (mCacheSize == ThermalSafetyCheckConst.Size.SMALL) {
            rgTemper.check(R.id.rb_small);
        } else if (mCacheSize == ThermalSafetyCheckConst.Size.MIDDLE) {
            rgTemper.check(R.id.rb_middle);
        } else {
            rgTemper.check(R.id.rb_large);
        }

        ivHotImage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ivHotImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                final boolean mThermalMirror = SpUtils.getBoolean(ThermalSafetyCheckConst.Key.THERMAL_MIRROR, ThermalSafetyCheckConst.Default.THERMAL_MIRROR);
                final boolean mLowTempMode = SpUtils.getBoolean(ThermalSafetyCheckConst.Key.LOW_TEMP, ThermalSafetyCheckConst.Default.LOW_TEMP);
                final int mBlackBodyLeft = SpUtils.getIntOrDef(ThermalSafetyCheckConst.Key.BLACK_BODY_LEFT, ThermalSafetyCheckConst.Default.BLACK_BODY_LEFT);
                final int mBlackBodyTop = SpUtils.getIntOrDef(ThermalSafetyCheckConst.Key.BLACK_BODY_TOP, ThermalSafetyCheckConst.Default.BLACK_BODY_TOP);
                final int mBlackBodyRight = SpUtils.getIntOrDef(ThermalSafetyCheckConst.Key.BLACK_BODY_RIGHT, ThermalSafetyCheckConst.Default.BLACK_BODY_RIGHT);
                final int mBlackBodyBottom = SpUtils.getIntOrDef(ThermalSafetyCheckConst.Key.BLACK_BODY_BOTTOM, ThermalSafetyCheckConst.Default.BLACK_BODY_BOTTOM);
                final Rect rect = new Rect(mBlackBodyLeft, mBlackBodyTop, mBlackBodyRight, mBlackBodyBottom);
                startHotImage(mThermalMirror, mLowTempMode, rect);

                int maxWidth = 80;
                int maxHeight = 60;
                maxWidth -= rect.width();
                maxHeight -= rect.height();

                sbX.setMax(maxWidth);
                sbY.setMax(maxHeight);

                sbX.setProgress(rect.left);
                sbY.setProgress(rect.top);

                final Bitmap bitmap = getBitmap(ivExample.getMeasuredWidth(), ivExample.getMeasuredHeight());
                ivExample.setImageBitmap(drawRectangles(bitmap, rect));

                final int widthHieght = 5;
                SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (seekBar.getId() == R.id.sb_x_safety_check) {
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
                sbX.setOnSeekBarChangeListener(onSeekBarChangeListener);
                sbY.setOnSeekBarChangeListener(onSeekBarChangeListener);
            }
        });
    }

    private void startHotImage(final boolean isMirror, final boolean isLowTemp, final Rect rect) {
        //usb设备初始化成功
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //开启热成像6080模块
                //isMirror:热成像画面是否左右镜像, isCold:是否为低温补偿模式, hotImageK6080CallBack:数据回调
                TemperatureModule.getIns().startHotImageK6080(isMirror, isLowTemp, hotImageK6080CallBack);
                BlackBody blackBody = new BlackBody(rect.left, rect.right, rect.top, rect.bottom);
                blackBody.setFrameColor(Color.WHITE);
                blackBody.setTempPreValue(345);
                TemperatureModule.getIns().setmCorrectionValue(0.0f);
                TemperatureModule.getIns().startK6080BlackBodyMode(blackBody);
            }
        }, 1000);
    }

    private HotImageK6080CallBack hotImageK6080CallBack = new HotImageK6080CallBack() {
        @Override
        public void newestHotImageData(final Bitmap bitmap, float v, float v1, float v2) {
            setFaceIndex();
        }

        @Override
        public void newestHotImageData(final Bitmap bitmap, ArrayList<FaceIndexInfo> arrayList) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ivHotImage.setImageBitmap(bitmap);
                }
            });
        }
    };

    private ArrayList<FaceIndexInfo> faceIndexInfos = new ArrayList<>();

    //设置人脸框
    private void setFaceIndex() {
        if (faceIndexInfos != null && faceIndexInfos.size() > 0) {
            faceIndexInfos.clear();
        }
        //可以支持一次传入多个人脸框信息
        faceIndexInfos.add(new FaceIndexInfo(0, mTempRect.left, mTempRect.right, mTempRect.top, mTempRect.bottom));

        //设置热成像画面中人脸框的颜色，默认为黑色
        TemperatureModule.getIns().setFaceFrameColor(0xFFFFFFFF);
        //人脸框信息faceIndexInfoList，是否在热成像画面中绘制人脸框(true为绘制)
        TemperatureModule.getIns().setFaceIndexInfoList(faceIndexInfos, true);
    }


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

    public void saveRect(View view) {
        SpUtils.saveInt(ThermalSafetyCheckConst.Key.BLACK_BODY_LEFT, mSaveRect.left);
        SpUtils.saveInt(ThermalSafetyCheckConst.Key.BLACK_BODY_TOP, mSaveRect.top);
        SpUtils.saveInt(ThermalSafetyCheckConst.Key.BLACK_BODY_RIGHT, mSaveRect.right);
        SpUtils.saveInt(ThermalSafetyCheckConst.Key.BLACK_BODY_BOTTOM, mSaveRect.bottom);

        UIUtils.showShort(this, "保存成功");
    }
}
