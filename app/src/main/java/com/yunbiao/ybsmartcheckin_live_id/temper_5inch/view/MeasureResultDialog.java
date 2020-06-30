package com.yunbiao.ybsmartcheckin_live_id.temper_5inch.view;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.databinding.DialogMeasureResultBinding;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.activity.Main5InchActivity;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.utils.ResourceUtils;
import com.yunbiao.ybsmartcheckin_live_id.temper_5inch.utils.TemperatureUnitUtils;

import androidx.databinding.DataBindingUtil;

public class MeasureResultDialog {

    private static volatile MeasureResultDialog instance;
    private Dialog resultDialog;
    private DialogMeasureResultBinding dialogMeasureResultBinding;

    public static MeasureResultDialog getInstance() {
        if (instance == null) {
            synchronized (MeasureResultDialog.class) {
                if (instance == null) {
                    instance = new MeasureResultDialog();
                }
            }
        }
        return instance;
    }

    public void init() {
        if (resultDialog != null && resultDialog.isShowing()) {
            resultDialog.dismiss();
            resultDialog = null;
        }

        resultDialog = new Dialog(APP.getContext());
        //去掉标题线
        resultDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //背景透明
        resultDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Window window = resultDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER; // 居中位置
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0
            lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        } else {
            lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        window.setGravity(Gravity.CENTER);
        window.setWindowAnimations(R.style.mystyle);  //添加动画
        window.setAttributes(lp);

        dialogMeasureResultBinding = DataBindingUtil.inflate(LayoutInflater.from(APP.getContext()), R.layout.dialog_measure_result, null, false);
        resultDialog.setContentView(dialogMeasureResultBinding.getRoot());
    }

    public void showResultDialog(Activity activity, Bitmap bitmap, final float temperature, boolean isNormal) {
        activity.runOnUiThread(() -> {
            dismissResultDialog();

            SignManager.instance().add5InchRecordToDB(bitmap, temperature);

            dialogMeasureResultBinding.ivMeasurePhoto.setImageBitmap(bitmap);
            String tempStr = "";
            if (Main5InchActivity.temperatureUnit == 1) {
                tempStr = temperature + "℃";
            } else {
                tempStr = TemperatureUnitUtils.c2f(temperature) + "℉";
            }
            dialogMeasureResultBinding.tvMeasureTemperature.setText(tempStr);
            if (isNormal) {
                dialogMeasureResultBinding.tvMeasureTemperature.setTextColor(ResourceUtils.getColorResource(R.color.temperature_normal_text));
            } else {
                dialogMeasureResultBinding.tvMeasureTemperature.setTextColor(ResourceUtils.getColorResource(R.color.temperature_abnormal_text));
            }

            if (!activity.isFinishing()) {
                resultDialog.show();
            }

            handler.removeMessages(0);
            handler.sendEmptyMessageDelayed(0, 3500);
        });
    }

    private void dismissResultDialog() {
        if (resultDialog != null && resultDialog.isShowing()) {
            resultDialog.dismiss();
        }
    }

    public boolean isShowing() {
        if (resultDialog != null && resultDialog.isShowing()) {
            return true;
        }
        return false;
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            dismissResultDialog();
        }
    };

}
