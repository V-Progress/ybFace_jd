package com.yunbiao.ybsmartcheckin_live_id.views;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.yunbiao.ybsmartcheckin_live_id.R;

public class SDialog extends Dialog {
    public SDialog(@NonNull Context context) {
        super(context, R.style.sync_dialog);

        init();
    }

    private void init() {
        setContentView(R.layout.layout_load_pop);
        setCanceledOnTouchOutside(false);
        setCancelable(false);

        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; //核心代码是这个属性。
        attributes.gravity = Gravity.RIGHT | Gravity.TOP;
        int orientation = getContext().getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            attributes.y = 200;
        } else {
            attributes.y = 100;
        }
        window.setDimAmount(0f);
        window.setAttributes(attributes);
    }
}
