package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in_smt;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.XmppConnectEvent;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.common.UpdateVersionControl;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.system.CoreInfoHandler;
import com.yunbiao.ybsmartcheckin_live_id.utils.FileUtils;
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

import butterknife.BindView;
import skin.support.SkinCompatManager;

public class SMTSystemActivity extends SMTBaseActivity implements View.OnClickListener {
    @BindView(R.id.fl_version_smt_system)
    TextView flVersionLoading;
    @BindView(R.id.tv_version_name_smt_system)
    TextView tvVersionInfo;
    @BindView(R.id.tv_version_info_smt_system)
    TextView tvVersionName;

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
    private TextView tv_bindcode_syetem;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private Button btnVisitorSystem;
    private Button btnSkinSystem;
    private TextView tvCopyRight;

    @Override
    protected int getLayout() {
        return R.layout.activity_smt_system_fake_landscape;
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        ivLogo = (ImageView) findViewById(R.id.iv_system_logo);

        tvCopyRight = findViewById(R.id.tv_copyright_smt_system);

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

        btnSkinSystem.setVisibility(View.INVISIBLE);

        if (Constants.isHT) {
            tvCopyRight.setVisibility(View.GONE);
            appName = getResources().getString(R.string.app_name2);
            ivLogo.setImageResource(R.mipmap.logo_icon_horizontal);
            ImageFileLoader.setDefaultLogoId(R.mipmap.logo_icon_horizontal);
        } else if (Constants.isSK) {
            tvCopyRight.setVisibility(View.GONE);
            appName = getResources().getString(R.string.app_name3);
            ivLogo.setImageResource(R.mipmap.icon_logo3);
            ImageFileLoader.setDefaultLogoId(R.mipmap.icon_logo3);
        } else {
            tvCopyRight.setVisibility(View.VISIBLE);
            appName = getResources().getString(R.string.app_name);
            ivLogo.setImageResource(R.mipmap.yb_logo);
            ImageFileLoader.setDefaultLogoId(R.mipmap.yb_logo);
        }

        Company company = SpUtils.getCompany();
        if (!TextUtils.isEmpty(company.getComlogo())) {
            ImageFileLoader.i().loadAndSave(this, company.getComlogo(), Constants.DATA_PATH, ivLogo);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SpUtils.getIntOrDef(SpUtils.SERVER_MODEL, Constants.serverModel.YUN) == Constants.serverModel.YUN) {
            tv_server_system.setText(getString(R.string.System_cloud_service));
        } else {
            tv_server_system.setText(getString(R.string.System_local_service));
        }
    }

    private String appName;

    @Override
    protected void initData() {
        try {
            PackageInfo packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            appName += " V" + packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        tv_version_system.setText(appName);

        setInfo();

        FileUtils.getDataSize(new FileUtils.OnSizeCallback() {
            @Override
            public void getSize(long size) {
                Log.e("123", "getSize: -----------" + size);
            }
        });

        checkUpgrade(new CheckUpgradeCallback() {
            @Override
            public void onStart() {
                flVersionLoading.setVisibility(View.VISIBLE);
            }

            @Override
            public void noUpgrade(String currVersionName) {
                tvVersionName.setText(getResString(R.string.update_lable_current) + currVersionName);
                tvVersionInfo.setGravity(Gravity.CENTER);
                tvVersionInfo.setText(getResString(R.string.updateManager_dqbbwzxbb));
                tvVersionInfo.setTextColor(Color.GREEN);
            }

            @Override
            public void haveNewVersion(String versionName, String versionInfo) {
                tvVersionName.setText(getResString(R.string.update_lable_new) + versionName);
                tvVersionInfo.setGravity(Gravity.LEFT);
                tvVersionInfo.setText(TextUtils.isEmpty(versionInfo) ? getResString(R.string.update_no_description) : versionInfo);
                tvVersionInfo.setTextColor(Color.WHITE);
            }

            @Override
            public void onError(String currVersionName, String s) {
                tvVersionName.setText(getResString(R.string.update_lable_current) + currVersionName);
                tvVersionInfo.setGravity(Gravity.CENTER);
                tvVersionInfo.setText(getResString(R.string.update_check_failed));
                tvVersionInfo.setTextColor(Color.GRAY);
            }

            @Override
            public void onFinish() {
                flVersionLoading.setVisibility(View.GONE);
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

    public void seeVisitor(View view) {
        startActivity(new Intent(this, SMTVisitorActivity.class));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_depart_system:
                startActivity(new Intent(this, SMTEmployListActivity.class));
                break;
            case R.id.btn_add_system:
                Intent intent = new Intent(this, SMTEditEmployActivity.class);
                intent.putExtra(SMTEditEmployActivity.KEY_TYPE, SMTEditEmployActivity.TYPE_ADD);
                startActivity(intent);
                break;
            case R.id.btn_data_system:
                startActivity(new Intent(this, SMTRecordActivity.class));
                break;
            case R.id.btn_setting_system:
                startActivity(new Intent(this, SMTSettingActivity.class));
                break;
            case R.id.btn_update_system:
                UpdateVersionControl.getInstance().checkUpdate(this);
                break;
            default:
                break;
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

