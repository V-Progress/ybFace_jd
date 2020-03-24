package com.yunbiao.ybsmartcheckin_live_id.temp_multi;

import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseGpioActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.utils.NetWorkChangReceiver;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

public abstract class BaseMultiThermalActivity extends BaseGpioActivity implements NetWorkChangReceiver.NetWorkChangeListener {

    private TextView tvNetState;

    @Override
    protected int getPortraitLayout() {
        return getLayout();
    }

    @Override
    protected int getLandscapeLayout() {
        return getLayout();
    }

    protected abstract int getLayout();

    @Override
    protected void initView() {
        super.initView();
        NetWorkChangReceiver netWorkChangReceiver = new NetWorkChangReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkChangReceiver, filter);
    }

    protected Rect adjustRect(int width, int height, Rect ftRect) {
        int previewWidth = width;
        int previewHeight = height;
        int canvasWidth = 60;
        int canvasHeight = 80;

        if (ftRect == null) {
            return null;
        }

        Rect rect = new Rect(ftRect);
        float horizontalRatio;
        float verticalRatio;
       /* if (cameraDisplayOrientation % 180 == 0) {
            horizontalRatio = (float) canvasWidth / (float) previewWidth;
            verticalRatio = (float) canvasHeight / (float) previewHeight;
        } else {
            horizontalRatio = (float) canvasHeight / (float) previewWidth;
            verticalRatio = (float) canvasWidth / (float) previewHeight;
        }*/
        horizontalRatio = (float) canvasHeight / (float) previewWidth;
        verticalRatio = (float) canvasWidth / (float) previewHeight;
        rect.left *= horizontalRatio;
        rect.right *= horizontalRatio;
        rect.top *= verticalRatio;
        rect.bottom *= verticalRatio;

        Rect newRect = new Rect();
        newRect.left = rect.left;
        newRect.right = rect.right;
        newRect.top = rect.top;
        newRect.bottom = rect.bottom;
        return newRect;
    }


    private PopupWindow systemInfoPopup;
    private CountDownTimer countDownTimer;

    protected void showSystemInfoPopup(View parentView) {
        if (systemInfoPopup != null && systemInfoPopup.isShowing()) {
            dissmissSystemInfo();
            return;
        }
        if (systemInfoPopup == null) {
            systemInfoPopup = new PopupWindow(this);
            systemInfoPopup.setContentView(View.inflate(this, R.layout.popup_layout_multi_thermal, null));
            systemInfoPopup.setWidth(400);
            systemInfoPopup.setHeight(500);
        }

        //设备编号
        final View rootView = systemInfoPopup.getContentView();
        final String deviceNo = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
        final TextView tvDeviceNo = rootView.findViewById(R.id.tv_device_no_info_multi_thermal);
        tvDeviceNo.setText(deviceNo);
        //绑定码
        final String bindCode = SpUtils.getStr(SpUtils.BIND_CODE);
        final TextView tvBindCode = rootView.findViewById(R.id.tv_bindcode_info_multi_thermal);
        tvBindCode.setText(bindCode);
        //公司
        TextView tvCompany = rootView.findViewById(R.id.tv_company_info_multi_thermal);
        int comid = SpUtils.getCompany().getComid();
        String compName = SpUtils.getCompany().getComname();
        if (comid == Constants.NOT_BIND_COMPANY_ID) {
            tvCompany.setText("未绑定");
        } else {
            tvCompany.setText(compName);
        }
        //网络状态
        tvNetState = rootView.findViewById(R.id.tv_net_state_info_multi_thermal);
        if(isNetConnected){
            tvNetState.setText("正常");
            tvNetState.setTextColor(Color.GREEN);
        } else {
            tvNetState.setText("无网络");
            tvNetState.setTextColor(Color.RED);
        }

        //倒计时
        final TextView tvTimeInfo = rootView.findViewById(R.id.tv_time_info_multi_thermal);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimeInfo.setText((millisUntilFinished / 1000) + "");
                String deviceNo2 = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
                if (TextUtils.isEmpty(deviceNo) && !TextUtils.isEmpty(deviceNo2)) {
                    tvDeviceNo.setText(deviceNo2);
                }
                String bindCode2 = SpUtils.getStr(SpUtils.BIND_CODE);
                if (TextUtils.isEmpty(bindCode) && !TextUtils.isEmpty(bindCode2)) {
                    tvBindCode.setText(bindCode2);
                }
            }

            @Override
            public void onFinish() {
                dissmissSystemInfo();
            }
        };
        countDownTimer.start();
        //隐藏
        View ivHidden = rootView.findViewById(R.id.iv_hidden_info_multi_thermal);
        ivHidden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dissmissSystemInfo();
            }
        });

        systemInfoPopup.showAsDropDown(parentView);
    }

    private void dissmissSystemInfo() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        if (systemInfoPopup != null && systemInfoPopup.isShowing()) {
            systemInfoPopup.dismiss();
        }
    }

    private boolean isNetConnected = false;

    @Override
    public void connect() {
        isNetConnected = true;
        if(tvNetState != null){
            tvNetState.setText("正常");
            tvNetState.setTextColor(Color.GREEN);
        }
    }

    @Override
    public void disConnect() {
        isNetConnected = false;
        if(tvNetState != null){
            tvNetState.setText("无网络");
            tvNetState.setTextColor(Color.RED);
        }
    }
}
