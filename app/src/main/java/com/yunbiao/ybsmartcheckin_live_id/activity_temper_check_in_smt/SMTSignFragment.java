package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in_smt;

import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.db2.User;
import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.NetWorkChangReceiver;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.views.ImageFileLoader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SMTSignFragment extends Fragment implements NetWorkChangReceiver.NetWorkChangeListener/* implements SignManager.SignEventListener*/ {

    private TextView tvTotal;
    private static final String TAG = "SignFragment";
    private ImageView ivQRCode;
    private View rootView;
    private TextView tvAlready;
    private int total = 0;
    private int already = 0;
    private TextView tvBindCompany;
    private TextView tvDeviceNo;
    private TextView tvNetState;
    private TextView appVersion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);

        rootView = inflater.inflate(R.layout.fragment_sign_smt, container, false);
        appVersion = rootView.findViewById(R.id.tv_app_version);
        tvNetState = rootView.findViewById(R.id.tv_net_state);
        ivQRCode = rootView.findViewById(R.id.iv_qrcode_sign_list);
        tvTotal = rootView.findViewById(R.id.tv_total);
        tvAlready = rootView.findViewById(R.id.tv_already);
        tvBindCompany = rootView.findViewById(R.id.tv_bind_company);
        tvDeviceNo = rootView.findViewById(R.id.tv_device_no);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        NetWorkChangReceiver netWorkChangReceiver = new NetWorkChangReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(netWorkChangReceiver, filter);

        tvBindCompany.setText(APP.getContext().getResources().getString(R.string.fment_sign_bind_code) + SpUtils.getStr(SpUtils.BIND_CODE));

        String deviceNum = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
        tvDeviceNo.setText(APP.getContext().getResources().getString(R.string.smt_main_device_no) + deviceNum);

        appVersion.setText(APP.getContext().getResources().getString(R.string.smt_main_ver) + CommonUtils.getAppVersion(APP.getContext()));

        int comid = SpUtils.getCompany().getComid();
        List<User> users = DaoManager.get().queryUserByCompId(comid);
        if (users != null) {
            total = users.size();
            tvTotal.setText(total + "");
        }

        List<Sign> todaySignData = SignManager.instance().getTodaySignData();
        if (todaySignData != null) {
            List<Sign> signs = removeDuplicateCase(todaySignData);
            Iterator<Sign> iterator = signs.iterator();
            while (iterator.hasNext()) {
                Sign next = iterator.next();
                if (next.getType() == -9) {
                    iterator.remove();
                }
            }
            already = signs.size();
        }
        tvAlready.setText(already + "");
    }

    @Override
    public void connect() {
        Log.e(TAG, "connect: 网络已连接");
        tvNetState.setText(APP.getContext().getResources().getString(R.string.smt_main_net_normal2));
        tvNetState.setTextColor(Color.GREEN);
    }

    @Override
    public void disConnect() {
        Log.e(TAG, "connect: 网络未连接");
        tvNetState.setText(APP.getContext().getResources().getString(R.string.smt_main_net_no));
        tvNetState.setTextColor(Color.RED);
    }

    public void updateNum(Sign sign) {
        if (sign.getType() == -9) {
            return;
        }
        already++;
        tvAlready.setText(APP.getContext().getResources().getString(R.string.smt_main_already) + already);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(Constants.isHT){
            ivQRCode.setVisibility(View.GONE);
        } else if(Constants.isSK){
            ivQRCode.setVisibility(View.GONE);
        } else {
            boolean qrCodeEnabled = SpUtils.getBoolean(SpUtils.QRCODE_ENABLED, Constants.DEFAULT_QRCODE_ENABLED);
            ivQRCode.setVisibility(qrCodeEnabled ? View.VISIBLE : View.GONE);
        }
    }

    private String mModel = "";

    public void setModelText(String model) {
        if (TextUtils.isEmpty(model)) {
            return;
        }
        mModel = model;
       /* if (tvModel != null) {
            tvModel.setText(mModel);
        }*/
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateInfoEvent event) {
        Company company = SpUtils.getCompany();
        ImageFileLoader.i().loadAndSave(getActivity(), company.getCodeUrl(), Constants.DATA_PATH, ivQRCode);


        if(tvBindCompany != null){
            if(TextUtils.isEmpty(company.getAbbname())){
                tvBindCompany.setText(APP.getContext().getResources().getString(R.string.fment_sign_bind_code) + SpUtils.getStr(SpUtils.BIND_CODE));
            } else {
                tvBindCompany.setText(APP.getContext().getResources().getString(R.string.smt_main_company) + company.getAbbname());
            }
        }

        String deviceNum = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
        tvDeviceNo.setText(APP.getContext().getResources().getString(R.string.smt_main_device_no) + deviceNum);

        appVersion.setText(APP.getContext().getResources().getString(R.string.smt_main_ver) + CommonUtils.getAppVersion(APP.getContext()));
    }


    /**
     * @param
     * @return
     * @description 根据员工ID去重
     * @date 14:39 2018/6/19
     * @author zhenghao
     */
    private List<Sign> removeDuplicateCase(List<Sign> cases) {
        Set<Sign> set = new TreeSet<>(new Comparator<Sign>() {
            @Override
            public int compare(Sign o1, Sign o2) {
                //字符串,则按照asicc码升序排列
                return (o1.getEmpId() + "").compareTo(o2.getEmpId() + "");
            }
        });
        set.addAll(cases);
        return new ArrayList<>(set);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
