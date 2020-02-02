package com.yunbiao.yb_smart_attendance;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.XmppConnectEvent;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.bean.CompanyResponse;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.utils.SkinLoader;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.xmpp.ServiceManager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

import okhttp3.Call;

/**
 * 流程：检查权限——登录服务器——获取公司信息——初始化存储路径——初始化皮肤
 */
public class SplashActivity extends Activity {
    private static final String TAG = "SplashActivity";

    private TextView tvTips;
    private TextView tvBindcode;
    private TextView tvNumber;
    private View pbCheck;
    private View pbLogin;
    private View pbRequest;
    private View pbInit;
    private TextView tvInit;
    private TextView tvRequest;
    private TextView tvLogin;
    private TextView tvCheck;
    private View pbSkin;
    private TextView tvSkin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        setContentView(R.layout.activity_splash2);

        initStep();

        //检查权限
        PermissionUtil.check(this, callback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PermissionUtil.onActivityResult(this, requestCode, callback);
    }

    /***
     * 1.检查权限回调
     */
    private PermissionUtil.PermissionCallback callback = new PermissionUtil.PermissionCallback() {
        @Override
        public void onAllPermissionAllowed(boolean isAllAllowed) {
            d("权限检查：" + isAllAllowed);
            if (isAllAllowed) {
                updateStep(0,true);
                startXmpp();
            } else {
                updateStep(0,false);
                showAlert("请重启并允许所有权限，否则APP将无法正常运行", "重启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        APP.restart();
                    }
                }, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        APP.exit();
                    }
                });
            }
        }
    };

    /***
     * 2.登录XMPP
     */
    private void startXmpp(){
        ServiceManager serviceManager = new ServiceManager(SplashActivity.this);
        serviceManager.startService();
    }

    /***
     * 登录XMPP回调
     * @param connectEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(XmppConnectEvent connectEvent) {
        d("xmpp连接：" + connectEvent.isConnected());
        if (connectEvent.isConnected()) {
            updateStep(1,true);
            tvNumber.setText("设备编号：" + SpUtils.getStr(SpUtils.DEVICE_NUMBER));
            tvBindcode.setText("绑定码：" + SpUtils.getStr(SpUtils.BIND_CODE));
            requestCompany();
        }
    }

    /***
     * 3.请求公司信息
     */
    private void requestCompany() {
        OkHttpUtils.post()
                .url(ResourceUpdate.COMPANYINFO)
                .addParams("deviceNo", HeartBeatClient.getDeviceNo())
                .build()
                .execute(stringCallback);
    }

    /***
     * 获取公司信息回调
     */
    private StringCallback stringCallback = new StringCallback() {
        @Override
        public void onError(Call call, Exception e, int id) {
            d("onError：", e);
            updateStep(2,false);
            retryRequest("请求失败：" + (e == null ? "NULL" : e.getMessage()));
        }

        @Override
        public void onResponse(String response, int id) {
            d("onResponse：" + response);
            if (TextUtils.isEmpty(response)) {
                updateStep(2,false);
                retryRequest("请求失败");
                return;
            }

            CompanyResponse companyResponse = new Gson().fromJson(response, CompanyResponse.class);
            if (companyResponse.getStatus() != 1) {
                updateStep(2,false);
                retryRequest("公司未绑定");
                return;
            }

            updateStep(2,true);

            Company company = companyResponse.getCompany();

            //生成数据库唯一名称
            String dbUniqueId = getDBUniqueId(company.getBindType(), company.getComid(), company.getDepId());

            //初始化数据库y
            DaoManager.get().initDb(dbUniqueId);

            //同步数据库（公司、部门）
            DBSync.getInstance().syncCompanyDB(company);

            //更新通用数据里的Company
            CommonData.updateCompany(company);

            //初始化路径
            PathManager.initPath(dbUniqueId, initCallback);
        }
    };

    /***
     * 4.初始化路径的回调
     */
    private PathManager.PathInitCallback initCallback = new PathManager.PathInitCallback() {
        @Override
        public void onPathInitialized(List<File> errFiles) {
            if (errFiles != null && errFiles.size() > 0) {
                updateStep(3,false);
                String errFilePath = "";
                for (File errFile : errFiles) {
                    errFilePath += errFile.getPath() + "\n";
                }
                setTips("目录创建失败，请检查内存空间是否已满\n" + errFilePath);
                return;
            }

            updateStep(3,true);

            loadSkin();
        }
    };

    /***
     * 5.加载皮肤
     */
    private void loadSkin(){
        int skinId = SpUtils.getIntOrDef(SpUtils.SKIN_ID,-1);
        String skinUrl = SpUtils.getStr(SpUtils.SKIN_URL);
        String skinPath = PathManager.getSkinPath();
        SkinLoader.loadSkin(skinUrl,skinId,skinPath,loadSkinCallback);
    }

    /***
     * 检查皮肤回调
     * */
    private SkinLoader.LoadSkinCallback loadSkinCallback = new SkinLoader.LoadSkinCallback() {
        @Override
        public void loadResult(boolean isSuccess) {
            tvSkin.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }
            },1500);
        }
    };

    /**
     * ==重试逻辑======================================================================================
     * */
    private void retryRequest(String log) {
        setTips(log);
        handler.sendEmptyMessage(0);
    }
    private int retryTime = 10;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (retryTime <= 1) {
                retryTime = 10;
                tvTips.setText("");
                requestCompany();
                return;
            }
            retryTime--;
            setTips(tvTips.getText().toString() + "\n即将重试：" + retryTime);
            handler.sendEmptyMessageDelayed(0, 1000);
        }
    };

    /***
     * ==初始化步骤=========================================================================
     */
    private void initStep(){
        tvNumber = findViewById(R.id.tv_number);
        tvBindcode = findViewById(R.id.tv_bindcode);
        tvTips = findViewById(R.id.tv_tips);

        pbCheck = findViewById(R.id.pb_check);
        pbLogin = findViewById(R.id.pb_login);
        pbRequest = findViewById(R.id.pb_request);
        pbInit = findViewById(R.id.pb_init);
        pbSkin = findViewById(R.id.pb_skin);

        tvCheck = findViewById(R.id.tv_check);
        tvLogin = findViewById(R.id.tv_login);
        tvRequest = findViewById(R.id.tv_request);
        tvInit = findViewById(R.id.tv_init);
        tvSkin = findViewById(R.id.tv_skin);

        pbCheck.setVisibility(View.VISIBLE);
        tvCheck.setTextColor(Color.YELLOW);
    }

    private String getDBUniqueId(int bindType,int comId,int depId){
        //数据库的唯一Id
        String dbUniqueId;
        if (bindType == 1) {
            dbUniqueId = comId + "";
        } else {
            dbUniqueId = comId + "_" + depId;
        }
        return dbUniqueId;
    }

    //更新步骤
    private void updateStep(final int posi,boolean completed){
        switch (posi) {
            case 0:
                if(completed){
                    pbCheck.setVisibility(View.INVISIBLE);
                    tvCheck.setTextColor(Color.GREEN);
                } else {
                    tvCheck.setTextColor(Color.RED);
                    return;
                }
                pbLogin.setVisibility(View.VISIBLE);
                tvLogin.setTextColor(Color.YELLOW);
                break;
            case 1:
                if(completed){
                    pbLogin.setVisibility(View.INVISIBLE);
                    tvLogin.setTextColor(Color.GREEN);
                } else {
                    tvLogin.setTextColor(Color.RED);
                    return;
                }
                pbRequest.setVisibility(View.VISIBLE);
                tvRequest.setTextColor(Color.YELLOW);
                break;
            case 2:
                if(completed){
                    pbRequest.setVisibility(View.INVISIBLE);
                    tvRequest.setTextColor(Color.GREEN);
                } else {
                    tvRequest.setTextColor(Color.RED);
                    return;
                }

                pbInit.setVisibility(View.VISIBLE);
                tvInit.setTextColor(Color.YELLOW);
                break;
            case 3:
                if(completed){
                    pbInit.setVisibility(View.INVISIBLE);
                    tvInit.setTextColor(Color.GREEN);
                } else {
                    tvInit.setTextColor(Color.RED);
                    return;
                }

                pbSkin.setVisibility(View.VISIBLE);
                tvSkin.setTextColor(Color.YELLOW);
                break;
            case 4:
                if(completed){
                    pbSkin.setVisibility(View.INVISIBLE);
                    tvSkin.setTextColor(Color.GREEN);
                }
                break;
        }
    }
    //设置提示
    private void setTips(String log){
        tvTips.setText(log);
    }

    /***
     * ==系统提示=========================================================================
     */
    private void d(String log, Throwable t) {
        d(log + (t == null ? "NULL" : t.getMessage()));
    }

    private void d(String log) {
        Log.d(TAG, log);
    }

    public void showAlert(String msg, String positive, final DialogInterface.OnClickListener positiveListener, String Negative, final DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setCancelable(false);
        final AlertDialog alertDialog = builder.create();

        if (!TextUtils.isEmpty(positive) && positiveListener != null) {
            builder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                    positiveListener.onClick(dialog, which);
                }
            });
        }
        if (!TextUtils.isEmpty(Negative) && negativeListener != null) {
            builder.setNegativeButton(Negative, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                    negativeListener.onClick(dialog, which);
                }
            });
        }
        alertDialog.show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
