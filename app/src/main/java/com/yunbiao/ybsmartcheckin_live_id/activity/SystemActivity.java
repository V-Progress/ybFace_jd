package com.yunbiao.ybsmartcheckin_live_id.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.XmppConnectEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.common.UpdateVersionControl;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.system.CoreInfoHandler;
import com.yunbiao.ybsmartcheckin_live_id.utils.SkinLoader;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.yunbiao.ybsmartcheckin_live_id.views.ImageFileLoader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import skin.support.SkinCompatManager;

public class SystemActivity extends BaseActivity implements View.OnClickListener {

    private Button btn_depart_system;
    private Button btn_add_system;
    private Button btn_data_system;
    private Button btn_setting_system;
    private TextView btn_update_system;
    private TextView tv_company_system;
    private TextView tv_deviceno_system;
    private TextView tv_exp_system;
    private TextView tv_server_system;
    private TextView tv_version_system;
    private TextView tv_online_system;
    private ImageView ivLogo;
    private View ivBack;
    private TextView tv_bindcode_syetem;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private Button btnVisitorSystem;
    private Button btnSkinSystem;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_system;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_system_h;
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        ivLogo = (ImageView) findViewById(R.id.iv_system_logo);

        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_depart_system = findViewById(R.id.btn_depart_system);
        btn_add_system = findViewById(R.id.btn_add_system);
        btn_data_system = findViewById(R.id.btn_data_system);
        btn_setting_system = findViewById(R.id.btn_setting_system);
        btn_update_system = findViewById(R.id.btn_update_system);
        btnVisitorSystem = findViewById(R.id.btn_visitor_system);
        btnSkinSystem = findViewById(R.id.btn_skin_system);

        tv_bindcode_syetem = (TextView) findViewById(R.id.tv_bindcode_syetem);
        tv_company_system = (TextView) findViewById(R.id.tv_company_system);
        tv_deviceno_system = (TextView) findViewById(R.id.tv_deviceno_system);
        tv_exp_system = (TextView) findViewById(R.id.tv_exp_system);
        tv_server_system = (TextView) findViewById(R.id.tv_server_system);
        tv_version_system = (TextView) findViewById(R.id.tv_version_system);
        tv_online_system = (TextView) findViewById(R.id.tv_online_system);

        btn_depart_system.setOnClickListener(this);
        btn_add_system.setOnClickListener(this);
        btn_data_system.setOnClickListener(this);
        btn_setting_system.setOnClickListener(this);
        btn_update_system.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        String appName = getResources().getString(R.string.app_name);
        try {
            PackageInfo packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            appName += " V" + packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        tv_version_system.setText(appName);

        updateServerState();

        setInfo();

        com.yunbiao.ybsmartcheckin_live_id.utils.FileUtils.getDataSize(new com.yunbiao.ybsmartcheckin_live_id.utils.FileUtils.OnSizeCallback() {
            @Override
            public void getSize(long size) {
                Log.e("123", "getSize: -----------" + size);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateInfoEvent updateEvent) {
        setInfo();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(XmppConnectEvent connectEvent) {
        tv_online_system.setText(connectEvent.isConnected() ? getString(R.string.System_online) : getString(R.string.System_offline));
    }

    public void setInfo() {
        Company company = SpUtils.getCompany();
        ImageFileLoader.i().loadAndSave(this, company.getComlogo(), Constants.DATA_PATH, ivLogo);

        tv_company_system.setText(company.getComname());

        String serNum = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
        tv_deviceno_system.setText(serNum);

        String bindCode = SpUtils.getStr(SpUtils.BIND_CODE);
        tv_bindcode_syetem.setText(bindCode);

        String expDate = SpUtils.getStr(SpUtils.EXP_DATE);
        if (TextUtils.isEmpty(expDate)) {
            expDate = getString(R.string.System_validityPeriod);
        } else {
            expDate = dateFormat.format(new Date(Long.parseLong(expDate)));
        }
        tv_exp_system.setText(expDate);

        tv_online_system.setText(CoreInfoHandler.isOnline ? getString(R.string.System_online) : getString(R.string.System_offline));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_depart_system:
                startActivity(new Intent(this, EmployListActivity.class));
                break;
            case R.id.btn_add_system:
                Intent intent = new Intent(this, EditEmployActivity.class);
                intent.putExtra(EditEmployActivity.KEY_TYPE, EditEmployActivity.TYPE_ADD);
                startActivity(intent);
                break;
            case R.id.btn_data_system:
                startActivity(new Intent(this, SignActivity.class));
                break;
            case R.id.btn_setting_system:
                startActivity(new Intent(this, SettingActivity.class));
                break;
            case R.id.btn_update_system:
                UpdateVersionControl.getInstance().checkUpdate(this);
                break;
            default:
                break;
        }
    }

    private void updateServerState() {
        String host = Constants.RESOURCE_URL;
        tv_server_system.setText(getString(R.string.System_cloud_service));
        if (host.contains("192.168.")) {
            tv_server_system.setText(getString(R.string.System_local_service));
        } else {
//            ivQrCode.setVisibility(View.VISIBLE);
//            tvQrLable.setVisibility(View.VISIBLE);
        }
    }

    private void setTextWatchers(final EditText[] editTexts) {
        for (int i = 0; i < editTexts.length; i++) {
            EditText editText = editTexts[i];
            final int finalI = i;
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() >= 3 && (finalI < editTexts.length - 1)) {
                        editTexts[finalI + 1].requestFocus();
                    } else if ((s.length() <= 0) && (finalI != 0)) {
                        editTexts[finalI - 1].requestFocus();
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    class SkinModel {
        String skinName;
        File skinFile;
    }

    public void seeVisitor(View view) {
        startActivity(new Intent(this, VisitorActivity.class));
    }

    public void selectSkin(View view) {
        final File file = new File(Constants.SKIN_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        final File[] files = file.listFiles();

        if (files == null || files.length <= 0) {
            UIUtils.showTitleTip(this, "暂无皮肤");
            return;
        }

        final List<SkinModel> skinList = new ArrayList<>();
        SkinModel skinModel = new SkinModel();
        skinModel.skinName = "默认";
        skinList.add(skinModel);

        for (File file1 : files) {
            if (file1.getName().endsWith(".skin")) {
                SkinModel skin = new SkinModel();
                skin.skinName = file1.getName();
                skin.skinFile = file1;
                skinList.add(skin);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this).setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return skinList.size();
            }

            @Override
            public Object getItem(int position) {
                return skinList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                VH vh;
                if (convertView == null) {
                    vh = new VH();
                    convertView = View.inflate(parent.getContext(), android.R.layout.simple_list_item_1, null);
                    vh.tv = convertView.findViewById(android.R.id.text1);
                    convertView.setTag(vh);
                } else {
                    vh = (VH) convertView.getTag();
                }
                SkinModel skinModel = skinList.get(position);
                vh.tv.setText(skinModel.skinName);
                return convertView;
            }

            class VH {
                TextView tv;
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.e(TAG, "onClick: " + which);
                SkinModel skinModel = skinList.get(which);
                File skinFile = skinModel.skinFile;
                if (which == 0 || skinFile == null) {
                    SkinCompatManager.getInstance().restoreDefaultTheme();
                } else {
                    load(skinFile.getName());
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private static final String TAG = "SystemActivity";

    private void load(final String apkName) {
        Log.e(TAG, "load: 加载：" + " : " + apkName);
        SkinLoader.setSkin(apkName);
    }
}

