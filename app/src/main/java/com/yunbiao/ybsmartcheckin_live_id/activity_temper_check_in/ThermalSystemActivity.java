package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yunbiao.ybsmartcheckin_live_id.FlavorType;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.XmppConnectEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.common.UpdateVersionControl;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
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

import butterknife.BindView;
import skin.support.SkinCompatManager;
import timber.log.Timber;

public class ThermalSystemActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.tv_version_name_thermal_system)
    TextView tvVersionName;
    @BindView(R.id.tv_version_info_thermal_system)
    TextView tvVersionInfo;
    @BindView(R.id.fl_version_thermal_system)
    View flVersionLoading;

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
    private TextView tvAbbName;
    private View llExpiry;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_thermal_system;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_thermal_system_h;
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        ivLogo = (ImageView) findViewById(R.id.iv_system_logo);

        btn_depart_system = findViewById(R.id.btn_depart_system);
        btn_add_system = findViewById(R.id.btn_add_system);
        btn_data_system = findViewById(R.id.btn_data_system);
        btn_setting_system = findViewById(R.id.btn_setting_system);
        btn_update_system = findViewById(R.id.btn_update_system);
        btnVisitorSystem = findViewById(R.id.btn_visitor_system);
        btnSkinSystem = findViewById(R.id.btn_skin_system);

        tvAbbName = findViewById(R.id.tv_abb_name_system_thermal);
        tv_bindcode_syetem = (TextView) findViewById(R.id.tv_bindcode_syetem);
        tv_company_system = (TextView) findViewById(R.id.tv_company_system);
        tv_deviceno_system = (TextView) findViewById(R.id.tv_deviceno_system);
        tv_exp_system = (TextView) findViewById(R.id.tv_exp_system);
        tv_server_system = (TextView) findViewById(R.id.tv_server_system);
        tv_version_system = (TextView) findViewById(R.id.tv_version_system);
        tv_online_system = (TextView) findViewById(R.id.tv_online_system);
        llExpiry = findViewById(R.id.ll_expiry);

        btn_depart_system.setOnClickListener(this);
        btn_add_system.setOnClickListener(this);
        btn_data_system.setOnClickListener(this);
        btn_setting_system.setOnClickListener(this);
        btn_update_system.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SpUtils.getIntOrDef(Constants.Key.SERVER_MODEL, Constants.Default.SERVER_MODEL) == Constants.serverModel.YUN) {
            tv_server_system.setText(getString(R.string.System_cloud_service));
        } else {
            tv_server_system.setText(getString(R.string.System_local_service));
        }

        boolean titleEnabled = SpUtils.getBoolean(ThermalConst.Key.TITLE_ENABLED,ThermalConst.Default.TITLE_ENABLED);
        boolean showMainLogo = SpUtils.getBoolean(ThermalConst.Key.SHOW_MAIN_LOGO,ThermalConst.Default.SHOW_MAIN_LOGO);
        ivLogo.setVisibility(showMainLogo ? View.VISIBLE : View.GONE);
        tvAbbName.setVisibility(titleEnabled ? View.VISIBLE : View.GONE);

        if(showMainLogo){
            //onResume中加载该加载的东西
            setLogo(ivLogo,tvAbbName);
        }
    }

    private void setLogo(ImageView logoView,TextView tvName) {
        boolean localPriority = SpUtils.getBoolean(ThermalConst.Key.LOCAL_PRIORITY, ThermalConst.Default.LOCAL_PRIORITY);
        if (localPriority) {
            String logoPath = SpUtils.getStr(ThermalConst.Key.MAIN_LOGO_IMG, ThermalConst.Default.MAIN_LOGO_IMG);
            if (TextUtils.isEmpty(logoPath)) {
                logoView.setImageResource(ThermalConst.Default.DEFAULT_LOGO_ID);
            } else {
                logoView.setImageBitmap(BitmapFactory.decodeFile(logoPath));
            }
            tvName.setText(SpUtils.getStr(ThermalConst.Key.MAIN_LOGO_TEXT, ThermalConst.Default.MAIN_LOGO_TEXT));
        } else {
            Company company = SpUtils.getCompany();
            String comlogo = company.getComlogo();
            String abbname = company.getAbbname();
            if(company.getComid() == Constants.NOT_BIND_COMPANY_ID || TextUtils.isEmpty(comlogo)){
                logoView.setVisibility(View.GONE);
                logoView.setImageBitmap(null);
            } else {
                Glide.with(this).load(comlogo).asBitmap().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        logoView.setImageBitmap(resource);
                    }
                });
            }
            tvName.setText(TextUtils.isEmpty(abbname) ? "" : abbname);
        }
    }
    private String appName = "";

    @Override
    protected void initData() {
        try {
            PackageInfo packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            appName += " V" + packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String additional = "";
        if(Constants.FLAVOR_TYPE == FlavorType.XENON){
            additional = "Xenon Fever Defence ";
        }
        tv_version_system.setText(additional + appName);

        setInfo();

        com.yunbiao.ybsmartcheckin_live_id.utils.FileUtils.getDataSize(size -> Log.e("123", "getSize: -----------" + size));

        checkUpgrade(new CheckUpgradeCallback() {
            @Override
            public void onStart() {
                flVersionLoading.setVisibility(View.VISIBLE);
            }

            @Override
            public void noUpgrade(String currVersionName) {
                btn_update_system.setEnabled(false);
                tvVersionName.setText(getResString(R.string.update_lable_current) + currVersionName);
                tvVersionInfo.setGravity(Gravity.CENTER);
                tvVersionInfo.setText(getResString(R.string.updateManager_dqbbwzxbb));
                tvVersionInfo.setTextColor(Color.GREEN);
            }

            @Override
            public void haveNewVersion(String versionName, String versionInfo) {
                btn_update_system.setEnabled(true);
                tvVersionName.setText(getResString(R.string.update_lable_new) + versionName);
                tvVersionInfo.setGravity(Gravity.LEFT);
                tvVersionInfo.setText(TextUtils.isEmpty(versionInfo) ? getResString(R.string.update_no_description) : versionInfo);
                tvVersionInfo.setTextColor(Color.WHITE);
            }

            @Override
            public void onError(String currVersionName, String s) {
                btn_update_system.setEnabled(false);
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
        //获取到公司信息时加载该加载的东西
        setLogo(ivLogo,tvAbbName);

        tv_company_system.setText(company.getComname());

        String serNum = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
        tv_deviceno_system.setText(serNum);

        String bindCode = SpUtils.getStr(SpUtils.BIND_CODE);
        tv_bindcode_syetem.setText(bindCode);

        String expDate = SpUtils.getStr(SpUtils.EXP_DATE);
        if (TextUtils.isEmpty(expDate)) {
            expDate = getString(R.string.System_validityPeriod);
            llExpiry.setVisibility(View.GONE);
        } else {
            llExpiry.setVisibility(View.VISIBLE);
            expDate = dateFormat.format(new Date(Long.parseLong(expDate)));
            tv_exp_system.setText(expDate);
        }

        tv_online_system.setText(CoreInfoHandler.isOnline ? getString(R.string.System_online) : getString(R.string.System_offline));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_depart_system:
                startActivity(new Intent(this, ThermalEmployListActivity.class));
                break;
            case R.id.btn_add_system:
                Intent intent = new Intent(this, ThermalEditEmployActivity.class);
                intent.putExtra(ThermalEditEmployActivity.KEY_TYPE, ThermalEditEmployActivity.TYPE_ADD);
                startActivity(intent);
                break;
            case R.id.btn_data_system:
                startActivity(new Intent(this, ThermalSignActivity.class));
                break;
            case R.id.btn_setting_system:
                startActivity(new Intent(this, ThermalSettingActivity.class));
                break;
            case R.id.btn_update_system:
                UpdateVersionControl.getInstance().checkUpdate(this);
                break;
            default:
                break;
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
        startActivity(new Intent(this, ThermalVisitorActivity.class));
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

