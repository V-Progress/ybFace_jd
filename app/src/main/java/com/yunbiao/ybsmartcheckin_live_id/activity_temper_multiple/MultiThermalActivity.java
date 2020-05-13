package com.yunbiao.ybsmartcheckin_live_id.activity_temper_multiple;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.arcsoft.face.FaceInfo;
import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.bean.BlackBody;
import com.intelligence.hardware.temperature.bean.FaceIndexInfo;
import com.intelligence.hardware.temperature.callback.HotImageK6080CallBack;
import com.intelligence.hardware.temperature.callback.UsbPermissionCallBack;
import com.yunbiao.faceview.CompareResult;
import com.yunbiao.faceview.FaceManager;
import com.yunbiao.faceview.FacePreviewInfo;
import com.yunbiao.faceview.FaceView;
import com.yunbiao.faceview.SecondFaceRectView;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.ButtonClickListener;
import com.yunbiao.ybsmartcheckin_live_id.FlavorType;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.DisplayOrientationEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.ResetLogoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateMediaEvent;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.MultiTotal;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.db2.User;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.yunbiao.ybsmartcheckin_live_id.utils.L;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.yunbiao.ybsmartcheckin_live_id.views.ImageFileLoader;
import com.yunbiao.ybsmartcheckin_live_id.xmpp.ServiceManager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import butterknife.BindView;
import okhttp3.Call;

public class MultiThermalActivity extends BaseMultiThermalActivity {

    private static final String TAG = "MultiThermalActivity";

    @BindView(R.id.ll_correction_area)
    View llCorrectionArea;//校准区域
    @BindView(R.id.btn_correction_safety_check)
    Button btnCorrect;//开始校准
    @BindView(R.id.btn_cancel_correction_safety_check)
    Button btnCorrectCancel;//取消校准
    @BindView(R.id.btn_confirm_correction_safety_check)
    Button btnCorrectConfirm;//确定校准
    @BindView(R.id.tv_oringinT_safety_check)
    TextView tvOriginT;//预测值
    @BindView(R.id.btn_correct_sub_safety_check)
    Button btnCorrSub;//补正值-
    @BindView(R.id.edt_correct_safety_check)
    EditText edtCorr;//补正值
    @BindView(R.id.btn_correct_plus_safety_check)
    Button btnCorrPlus;//补正值+
    @BindView(R.id.btn_body_temper_sub_safety_check)
    Button btnBodyTSub;//体温值-
    @BindView(R.id.edt_body_temper_safety_check)
    EditText edtBodyT;//体温值
    @BindView(R.id.btn_body_temper_plus_safety_check)
    Button btnBodyTPlus;//体温值+
    @BindView(R.id.tv_total_num_multi_thermal)
    TextView tvTotalNum;//总数
    @BindView(R.id.tv_normal_num_multi_thermal)
    TextView tvNormalNum;//正常数
    @BindView(R.id.tv_warning_num_multi_normal)
    TextView tvWarningNum;//异常数
    @BindView(R.id.tv_company_num_multi_thermal)
    TextView tvCompanyNum;//员工数
    @BindView(R.id.tv_visitor_num_multi_thermal)
    TextView tvVisitorNum;//访客数
    @BindView(R.id.tv_normal_total_multi_thermal)
    TextView tvNormalTotal;//正常总数
    @BindView(R.id.tv_warning_total_multi_thermal)
    TextView tvWarningTotal;//异常总数
    @BindView(R.id.gtv_title_multi_thermal)
    TextView tvTitle;//标题
    @BindView(R.id.iv_hot_image)
    ImageView ivHotImage;//热图
    @BindView(R.id.rlv_warning_list)
    RecyclerView rlvWarningList;//异常列表
    @BindView(R.id.face_view)
    FaceView faceView;//人脸控件
    @BindView(R.id.iv_logo_multi_thermal)
    ImageView ivLogo;//logo
    @BindView(R.id.face_rect_view_hot_image)
    SecondFaceRectView secondFaceRectView;//第二人脸框
    @BindView(R.id.rlv_normal_list)
    RecyclerView rlvNormalList;//正常列表

    @BindView(R.id.tv_logo_multi_thermal)
    TextView tvLogo;
    @BindView(R.id.ll_correction_box_area)
    View llCorrectionBoxArea;
    @BindView(R.id.btn_box_up)
    Button btnBoxUp;
    @BindView(R.id.btn_box_down)
    Button btnBoxDown;
    @BindView(R.id.btn_cancel_correction_box)
    Button btnCancelCorrectionBox;
    @BindView(R.id.btn_confirm_correction_box)
    Button btnConfirmCorrectionBox;
    @BindView(R.id.btn_box_size_sub)
    Button btnBoxSizeSub;
    @BindView(R.id.btn_box_size_add)
    Button btnBoxSizeAdd;

    private boolean mThermalMirror;
    private MultiThermalRecordAdapter warningAdapter;
    private List<MultiTemperBean> warningList = new ArrayList<>();
    private List<MultiTemperBean> normalList = new ArrayList<>();
    private MultiThermalRecordAdapter normalAdapter;

    private float mWarningTemper = 37.3f;
    private float mNormalTemper = 35.5f;

    private int totalNum = 0;
    private int normalNum = 0;
    private int warningNum = 0;
    private int companyNum = 0;
    private int visitorNum = 0;
    private ServiceManager serviceManager;
    private float mCorrectValue;
    private Rect mBlackBodyAreaRect;
    private boolean mBlackBodyFrame;
    private boolean mMultiTrack;
    private int mPreValue;
    private boolean mBlackBodyEnable;
    private float mBodyTemper;

    private boolean isFirstCorrected = true;
    private float mLastMinT;
    private boolean mTempMode = false;

    @Override
    protected int getLayout() {
        return R.layout.activity_multi_thermal;
    }

    @Override
    protected void initView() {
        super.initView();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        APP.setMainActivity(this);

        //开启多人识别模式
        faceView.enableMutiple(true);
        //关闭多次重试
        faceView.enableMultiRetry(false);
        //设置回调延时
        faceView.setRetryDelayTime(500);

        faceView.setSecondFaceRectView(secondFaceRectView);
        faceView.setCallback(faceCallback);
        startXmpp();

        if (Constants.FLAVOR_TYPE == FlavorType.HT) {
            ivLogo.setImageResource(R.mipmap.logo_icon_horizontal);
            ImageFileLoader.setDefaultLogoId(R.mipmap.logo_icon_horizontal);
        } else if (Constants.FLAVOR_TYPE == FlavorType.SK) {
            ivLogo.setImageResource(R.mipmap.icon_logo3);
            ImageFileLoader.setDefaultLogoId(R.mipmap.icon_logo3);
        } else if (Constants.FLAVOR_TYPE == FlavorType.OSIMLE) {
            ivLogo.setImageResource(R.mipmap.osimle_logo);
            ImageFileLoader.setDefaultLogoId(R.mipmap.osimle_logo);
        } else {
            ivLogo.setVisibility(View.GONE);
//            ivLogo.setImageResource(R.mipmap.logo_hushida);
//            ImageFileLoader.setDefaultLogoId(R.mipmap.logo_hushida);
        }

        initCalibrationBoxBtn();
        btnCorrect.setOnLongClickListener(v -> {
            startCalibrationBox();
            return true;
        });
    }

    @Override
    protected void initData() {
        KDXFSpeechManager.instance().init(this);
        setHorizontalRlv();
        setVerticalRlv();
        addAllItemRecord();
        handleData();
    }

    private void setHorizontalRlv() {
        rlvNormalList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        rlvNormalList.setItemAnimator(new DefaultItemAnimator());
        rlvNormalList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.right = 10;
            }
        });
        normalAdapter = new MultiThermalRecordAdapter(this, normalList, RecyclerView.HORIZONTAL);
        rlvNormalList.setAdapter(normalAdapter);
    }

    private void setVerticalRlv() {
        rlvWarningList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        rlvWarningList.setItemAnimator(new DefaultItemAnimator());
        rlvWarningList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = 10;
            }
        });

        warningAdapter = new MultiThermalRecordAdapter(this, warningList, RecyclerView.VERTICAL);
        rlvWarningList.setAdapter(warningAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        faceView.resume();
        mLastMinT = SpUtils.getFloat(MultiThermalConst.Key.LAST_MIN_T, MultiThermalConst.Default.LAST_MIN_T);
        mBodyTemper = SpUtils.getFloat(MultiThermalConst.Key.BODY_TEMPER, MultiThermalConst.Default.BODY_TEMPER);
        boolean isBlackBodyTemper = SpUtils.getBoolean(MultiThermalConst.Key.BLACK_BODY_ENABLE, MultiThermalConst.Default.BLACK_BODY_ENABLE);
        if(mBlackBodyEnable != isBlackBodyTemper){
            mTempMode = false;
            SpUtils.saveFloat(MultiThermalConst.Key.LAST_MIN_T, MultiThermalConst.Default.LAST_MIN_T);
            SpUtils.saveFloat(MultiThermalConst.Key.CORRECT_VALUE,MultiThermalConst.Default.BODY_CORRECT_TEMPER);
        }
        mBlackBodyEnable = isBlackBodyTemper;

        mPreValue = SpUtils.getIntOrDef(MultiThermalConst.Key.BLACK_BODY_PRE_VALUE, MultiThermalConst.Default.BLACK_BODY_PRE_VALUE);
        mBlackBodyAreaRect = getCacheRect();
        mMultiTrack = SpUtils.getBoolean(MultiThermalConst.Key.MULTI_TRACK, MultiThermalConst.Default.MULTI_TRACK);
        mWarningTemper = SpUtils.getFloat(MultiThermalConst.Key.WARNING_TEMP, MultiThermalConst.Default.WARNING_TEMP);
        mThermalMirror = SpUtils.getBoolean(MultiThermalConst.Key.THERMAL_MIRROR, MultiThermalConst.Default.THERMAL_MIRROR);
        mCorrectValue = SpUtils.getFloat(MultiThermalConst.Key.CORRECT_VALUE, MultiThermalConst.Default.BODY_CORRECT_TEMPER);
        mBlackBodyFrame = SpUtils.getBoolean(MultiThermalConst.Key.BLACK_BODY_FRAME, MultiThermalConst.Default.BLACK_BODY_FRAME);
        boolean isThermalFaceFrame = SpUtils.getBoolean(MultiThermalConst.Key.THERMAL_FACE_FRAME, MultiThermalConst.Default.THERMAL_FACE_FRAME);
        //是否显示热成像人脸框
        faceView.enableThermalFaceFrame(isThermalFaceFrame);
        //开启多次回调
        faceView.enableMultiCallback(mMultiTrack);
        startHotImage();
    }

    private Rect getCacheRect() {
        int left = SpUtils.getIntOrDef(MultiThermalConst.Key.CORRECT_AREA_LEFT, MultiThermalConst.Default.CORRECT_AREA_LEFT);
        int top = SpUtils.getIntOrDef(MultiThermalConst.Key.CORRECT_AREA_TOP, MultiThermalConst.Default.CORRECT_AREA_TOP);
        int right = SpUtils.getIntOrDef(MultiThermalConst.Key.CORRECT_AREA_RIGHT, MultiThermalConst.Default.CORRECT_AREA_RIGHT);
        int bottom = SpUtils.getIntOrDef(MultiThermalConst.Key.CORRECT_AREA_BOTTOM, MultiThermalConst.Default.CORRECT_AREA_BOTTOM);
        return new Rect(left, top, right, bottom);
    }

    private void startHotImage() {
        TemperatureModule.getIns().closeHotImageK6080();
        int usbPermission = TemperatureModule.getIns().initUsbDevice(this, new UsbPermissionCallBack() {
            @Override
            public void usbPermissionNotice(boolean b) {
                if (!b) {
                    UIUtils.showShort(MultiThermalActivity.this, getResources().getString(R.string.main_permission_failed_multi_thermal));
                    return;
                }

                //usb设备初始化成功
                new Handler().postDelayed(() -> {
                    //开启热成像6080模块
                    //isMirror:热成像画面是否左右镜像, isCold:是否为低温补偿模式, hotImageK6080CallBack:数据回调
                    TemperatureModule.getIns().startHotImageK6080(mThermalMirror, mTempMode, hotImageK6080CallBack);
                    if(mCorrectValue == MultiThermalConst.Default.BODY_CORRECT_TEMPER){
                        TemperatureModule.getIns().setmCorrectionValue(0.0f);
                    } else {
                        TemperatureModule.getIns().setmCorrectionValue(mCorrectValue);
                    }

                    if (mBlackBodyEnable) {
                        isFirstCorrected = false;
                        BlackBody blackBody = new BlackBody(mBlackBodyAreaRect.left, mBlackBodyAreaRect.right, mBlackBodyAreaRect.top, mBlackBodyAreaRect.bottom);
                        blackBody.setFrameColor(Color.WHITE);
                        blackBody.setDrawFrame(mBlackBodyFrame);
                        blackBody.setTempPreValue(mPreValue);
                        TemperatureModule.getIns().startK6080BlackBodyMode(blackBody);
                        TemperatureModule.getIns().closeK6080AutoCalibMode();
                    } else {
                        isFirstCorrected = true;
                        TemperatureModule.getIns().closeK6080BlackBodyMode();
                        TemperatureModule.getIns().startK6080AutoCalibMode(2);
                    }

                    if(mCorrectValue != MultiThermalConst.Default.BODY_CORRECT_TEMPER){
                        mTempMode = true;
                        TemperatureModule.getIns().setHotImageColdMode(mTempMode);
                    }
                }, 1000);
            }
        });

        if (usbPermission == -1) {
            UIUtils.showShort(MultiThermalActivity.this, getResources().getString(R.string.main_permission_failed_multi_thermal));
        } else if (usbPermission == 0) {
            UIUtils.showShort(MultiThermalActivity.this, getResources().getString(R.string.main_not_found_usb_multi_thermal));
        } else if (usbPermission == 1) {
            //usb设备初始化成功
            new Handler().postDelayed(() -> {
                //开启热成像6080模块
                //isMirror:热成像画面是否左右镜像, isCold:是否为低温补偿模式, hotImageK6080CallBack:数据回调
                TemperatureModule.getIns().startHotImageK6080(mThermalMirror, mTempMode, hotImageK6080CallBack);
                if(mCorrectValue == MultiThermalConst.Default.BODY_CORRECT_TEMPER){
                    TemperatureModule.getIns().setmCorrectionValue(0.0f);
                } else {
                    TemperatureModule.getIns().setmCorrectionValue(mCorrectValue);
                }
                if (mBlackBodyEnable) {
                    isFirstCorrected = false;
                    BlackBody blackBody = new BlackBody(mBlackBodyAreaRect.left, mBlackBodyAreaRect.right, mBlackBodyAreaRect.top, mBlackBodyAreaRect.bottom);
                    blackBody.setFrameColor(Color.WHITE);
                    blackBody.setDrawFrame(mBlackBodyFrame);
                    blackBody.setTempPreValue(mPreValue);
                    TemperatureModule.getIns().startK6080BlackBodyMode(blackBody);
                    TemperatureModule.getIns().closeK6080AutoCalibMode();
                } else {
                    isFirstCorrected = true;
                    TemperatureModule.getIns().closeK6080BlackBodyMode();
                    TemperatureModule.getIns().startK6080AutoCalibMode(2);
                }

                if(mCorrectValue != MultiThermalConst.Default.BODY_CORRECT_TEMPER){
                    mTempMode = true;
                    TemperatureModule.getIns().setHotImageColdMode(mTempMode);
                }
            }, 1000);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        d("onStop-----------------");
        if (handler != null) {
            handler.removeMessages(1);
        }
        faceView.pause();
        TemperatureModule.getIns().closeHotImageK6080();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        faceView.destory();
        destoryXmpp();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    private HotImageK6080CallBack hotImageK6080CallBack = new HotImageK6080CallBack() {
        @Override
        public void newestHotImageData(final Bitmap bitmap, float v, float v1, float v2) {
            if (isFirstCorrected) {
                float value = TemperatureModule.getIns().getK6080OffsetStandard();
                if (value != -1f) {
                    d("上次最低值：" + mLastMinT);
                    d("当前最低值：" + value);

                    float diffValue = 0.0f;
                    if (mLastMinT != 0.0f) {
                        diffValue = mLastMinT - value;
                    }
                    diffValue = formatF(diffValue);
                    d("前后差异值：" + diffValue);
                    d("上次校准值：" + mCorrectValue);

                    mCorrectValue += diffValue;
                    mCorrectValue = formatF(mCorrectValue);
                    d("最终校准值：" + mCorrectValue);

                    SpUtils.saveFloat(MultiThermalConst.Key.LAST_MIN_T, value);
                    SpUtils.saveFloat(MultiThermalConst.Key.CORRECT_VALUE, mCorrectValue);

                    TemperatureModule.getIns().setmCorrectionValue(mCorrectValue);

                    isFirstCorrected = false;
                }
            } else if (isCalibration) {
                setFaceIndex();
            } else {
                setHotImage(bitmap);
            }
        }

        @Override
        public void newestHotImageData(final Bitmap bitmap, ArrayList<FaceIndexInfo> arrayList) {
            if (isFirstCorrected) {
                setHotImage(bitmap);
            } else if (isCalibration) {
                setHotImage(bitmap);
                if (arrayList == null || arrayList.size() <= 0) {
                    return;
                }
                FaceIndexInfo faceIndexInfo = arrayList.get(0);
                float originalTempF = faceIndexInfo.getOriginalTempF();
                mCorrectionTemperList.add(originalTempF);
                if (mCorrectionTemperList.size() > 30) {
                    mCorrectionTemperList.remove(0);
                }
            } else {
                setHotImage(bitmap);
                faceView.setTemperList(arrayList);
            }
        }
    };

    private FaceView.FaceCallback faceCallback = new FaceView.FaceCallback() {
        @Override
        public void onReady() {
            SignManager.instance();
            FaceManager.getInstance().init(MultiThermalActivity.this);
            SyncManager.instance().requestOnlyCompany();
        }

        @Override
        public void onFaceDetection(Boolean hasFace, List<FacePreviewInfo> facePreviewInfoList) {
            if (!hasFace) {
                if (totalMap.size() > 0) {
                    totalMap.clear();
                }
                return;
            }

            if (isFirstCorrected) {
                return;
            }

            if (isCalibration) {
                return;
            }

            setFaceIndex(facePreviewInfoList);
            if (totalMap.size() > 30) {
                Iterator<Map.Entry<Integer, MultiTemperBean>> iterator = totalMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    iterator.next();
                    iterator.remove();
                    if (totalMap.size() <= 30) {
                        break;
                    }
                }
            }
        }

        @Override
        public boolean onFaceDetection(boolean hasFace, FacePreviewInfo facePreviewInfo) {
            return false;
        }

        @Override
        public void onFaceVerify(CompareResult faceAuth) {
            if (isFirstCorrected) {
                return;
            }

            if (isCalibration) {
                return;
            }

            int trackId = faceAuth.getTrackId();
            float temperByTrackId = faceView.getTemperByTrackId(trackId);
            if (temperByTrackId <= 0.0f) {
                return;
            }
            //截取人脸图
            Bitmap picture = faceView.getPicture(trackId);
            if (picture == null) {
                return;
            }
            //截取热图
            if (ivHotImage == null) {
                return;
            }
            BitmapDrawable bitmapDrawable = (BitmapDrawable) ivHotImage.getDrawable();
            if (bitmapDrawable == null) {
                return;
            }

            temperByTrackId = formatF(temperByTrackId);

            MultiTemperBean multiTemperBean = new MultiTemperBean();
            //trackId
            multiTemperBean.setTrackId(faceAuth.getTrackId());
            //温度
            multiTemperBean.setTemper(temperByTrackId);
            //faceId
            multiTemperBean.setFaceId(faceAuth.getUserName());
            //时间
            multiTemperBean.setTime(System.currentTimeMillis());
            //人脸图
            multiTemperBean.setHeadImage(picture);
            //热图
            multiTemperBean.setHotImage(bitmapDrawable.getBitmap());
            //截取热图
            FaceInfo faceInfo = faceView.getFaceInfo(faceAuth.getTrackId());
            if (faceInfo != null) {
                Rect realRect = faceView.getRealRect(faceInfo.getRect());
                if (realRect != null) {//如果真实rect不为null的时候再抓取
                    Rect hotRect = adjustRect(faceView.getWidth(), faceView.getHeight(), realRect);
                    hotRect.left -= 2;
                    hotRect.bottom += 3;
                    multiTemperBean.setHotRect(hotRect);
                }
            }
            addResultAndNotifyData(multiTemperBean);
        }
    };

    //设置人脸框
    private ArrayList<FaceIndexInfo> faceIndexInfos = new ArrayList<>();

    private void setFaceIndex() {
        if (faceIndexInfos != null && faceIndexInfos.size() > 0) {
            faceIndexInfos.clear();
        }
        //可以支持一次传入多个人脸框信息
        faceIndexInfos.add(new FaceIndexInfo(0, 20, 60, 20, 40));

        //设置热成像画面中人脸框的颜色，默认为黑色
        TemperatureModule.getIns().setFaceFrameColor(0xFFFFFFFF);
        //人脸框信息faceIndexInfoList，是否在热成像画面中绘制人脸框(true为绘制)
        TemperatureModule.getIns().setFaceIndexInfoList(faceIndexInfos, true);
    }

    private void setHotImage(final Bitmap bitmap) {
        ivHotImage.post(() -> ivHotImage.setImageBitmap(bitmap));
    }

    //设置人脸框
    private void setFaceIndex(List<FacePreviewInfo> facePreviewInfos) {
        ArrayList<FaceIndexInfo> faceIndexInfoList = new ArrayList<>();

        for (int i = 0; i < facePreviewInfos.size(); i++) {
            FacePreviewInfo facePreviewInfo = facePreviewInfos.get(i);
            FaceInfo faceInfo = facePreviewInfo.getFaceInfo();
            Rect realRect = faceView.getRealRect(faceInfo.getRect());
            Rect hotRect = adjustRect(faceView.getWidth(), faceView.getHeight(), realRect);//先转换再校准
            //left, right, top, bottom
            FaceIndexInfo faceIndexInfo = new FaceIndexInfo(facePreviewInfo.getTrackId(),  hotRect.left, hotRect.right, hotRect.top, hotRect.bottom);
            //可以支持一次传入多个人脸框信息
            faceIndexInfoList.add(faceIndexInfo);
        }

        //设置热成像画面中人脸框的颜色，默认为黑色
        TemperatureModule.getIns().setFaceFrameColor(0xFFFFFFFF);
        //人脸框信息faceIndexInfoList，是否在热成像画面中绘制人脸框(true为绘制)
        TemperatureModule.getIns().setFaceIndexInfoList(faceIndexInfoList, true);
    }

    /*====数据刷新=======================================================================================*/
    /*====数据刷新=========================================================================================*/
    /*====数据刷新=========================================================================================*/
    private ConcurrentLinkedQueue<MultiTemperBean> resultQueue = new ConcurrentLinkedQueue<>();

    private void addResultAndNotifyData(MultiTemperBean result) {
        if (result == null) {
            return;
        }
        resultQueue.add(result);
        startDataNotify();
    }

    private boolean isDataNotifyRunning = false;

    //开启数据刷新线程
    private void startDataNotify() {
        if (dataNotifyThread != null && isDataNotifyRunning) {
            return;
        }
        isDataNotifyRunning = true;
        dataNotifyThread = new Thread(runnable);
        dataNotifyThread.start();
    }

    //关闭数据刷新线程
    private void stopDataNotify() {
        if (dataNotifyThread != null && isDataNotifyRunning) {
            isDataNotifyRunning = false;
            dataNotifyThread = null;
        }
    }

    //数据刷新线程
    private Thread dataNotifyThread = null;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (isDataNotifyRunning) {
                if (resultQueue.size() > 0) {
                    int comid = SpUtils.getCompany().getComid();
                    //查用户信息，查不到的就是-1访客
                    final MultiTemperBean multiTemperBean = resultQueue.poll();
                    if (multiTemperBean == null) {
                        continue;
                    }
                    multiTemperBean.setCompId(comid);

                    String faceId = multiTemperBean.getFaceId();

                    //如果faceId不为-1则查库
                    User user = null;
                    if (!TextUtils.equals("-1", faceId)) {
                        user = DaoManager.get().queryUserByComIdAndFaceId(comid, faceId);
                    }

                    //设置用户信息
                    if (user == null) {
                        multiTemperBean.setFaceId("-1");
                        multiTemperBean.setName("访客");
                    } else {
                        multiTemperBean.setName(user.getName());
                        multiTemperBean.setEntryId(user.getId());
                    }

                    //裁剪热图
                    Rect hotRect = multiTemperBean.getHotRect();
                    if (hotRect != null) {
                        int x = hotRect.left;
                        int y = hotRect.top;
                        int width = hotRect.width();
                        int height = hotRect.height();

                        Bitmap hotImage = multiTemperBean.getHotImage();
                        if (x + width > hotImage.getWidth()) {
                            width = hotImage.getWidth() - x;
                        }
                        if (y + height > hotImage.getHeight()) {
                            height = hotImage.getHeight() - y;
                        }
                        if (x <= 0) {
                            x = 1;
                        }
                        if (y <= 0) {
                            y = 1;
                        }
                        if (width > 0 && height > 0) {
                            Bitmap bitmap = Bitmap.createBitmap(hotImage, x, y, width, height);
                            multiTemperBean.setHotImage(bitmap);
                        }
                    }
                    //镜像人脸图
                    Bitmap bitmap = horverImage(multiTemperBean.getHeadImage(), true, false);
                    multiTemperBean.setHeadImage(bitmap);

                    //更新记录列表
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addItemRecord(multiTemperBean);
                        }
                    });
                } else {
                    stopDataNotify();
                }

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    };

    private DateFormat dateFormat2 = new SimpleDateFormat("yyyy年MM月dd日");

    private void addAllItemRecord() {
        int comid = SpUtils.getCompany().getComid();
        List<Sign> signs = DaoManager.get().querySignByComIdAndDateWithLimit(comid, dateFormat2.format(new Date()), 100);
        if (signs == null || signs.size() <= 0) {
            return;
        }
        Collections.sort(signs, new Comparator<Sign>() {
            @Override
            public int compare(Sign o1, Sign o2) {
                return (int) (o2.getTime() - o1.getTime());
            }
        });
        for (int i = 0; i < signs.size(); i++) {
            Sign sign = signs.get(i);
            if (sign.getTemperature() < mWarningTemper) {
                if (normalList.size() < 15) {
                    MultiTemperBean multiTemperBean = signToMultiTemperBean(sign);
                    normalList.add(multiTemperBean);
                }
            } else {
                if (warningList.size() < 30) {
                    MultiTemperBean multiTemperBean = signToMultiTemperBean(sign);
                    warningList.add(multiTemperBean);
                }
            }
        }
        if (normalList.size() > 0) {
            normalAdapter.notifyItemRangeInserted(0, normalList.size());
        }
        if (warningList.size() > 0) {
            warningAdapter.notifyItemRangeInserted(0, warningList.size());
        }
    }

    private MultiTemperBean signToMultiTemperBean(Sign sign) {
        MultiTemperBean multiTemperBean = new MultiTemperBean();
        multiTemperBean.setFaceId(sign.getFaceId());
        multiTemperBean.setTime(sign.getTime());
        multiTemperBean.setName(sign.getName());
        multiTemperBean.setTemper(sign.getTemperature());
        String headPath = sign.getHeadPath();
        if (!TextUtils.isEmpty(headPath)) {
            multiTemperBean.setHeadImage(BitmapFactory.decodeFile(headPath));
        }
        String hotImgPath = sign.getHotImgPath();
        if (!TextUtils.isEmpty(hotImgPath)) {
            multiTemperBean.setHotImage(BitmapFactory.decodeFile(hotImgPath));
        }
        return multiTemperBean;
    }

    //添加条目到记录中
    private void updateItemRecord(List<MultiTemperBean> multiTemperBeanList) {
        Log.e(TAG, "addItemRecord: 本次刷新的list的数量" + multiTemperBeanList.size());
        int warningSize = 0;
        int normalSize = 0;
        for (MultiTemperBean multiTemperBean : multiTemperBeanList) {
            float temper = multiTemperBean.getTemper();
            if (temper <= 0f || temper >= 37.3f) {
                warningSize += 1;
                warningList.add(0, multiTemperBean);
                warningAdapter.notifyItemInserted(0);
                if (warningList.size() > 15) {
                    int index = warningList.size() - 1;
                    warningList.remove(warningList.get(index));
                }
            } else {
                normalSize += 1;
                normalList.add(0, multiTemperBean);
                normalAdapter.notifyItemInserted(0);
                if (normalList.size() > 15) {
                    int index = normalList.size() - 1;
                    normalList.remove(normalList.get(index));
                }
            }

            handleData(multiTemperBean);
        }

        if (warningSize > 0) {
            warningAdapter.notifyItemRangeInserted(0, warningSize);
            rlvWarningList.scrollToPosition(0);
        }
        if (normalSize > 0) {
            normalAdapter.notifyItemRangeInserted(0, normalSize);
            rlvNormalList.scrollToPosition(0);
        }
    }

    //添加条目到记录中
    private void addItemRecord(MultiTemperBean multiTemperBean) {
        float temper = multiTemperBean.getTemper();
        if (temper <= 0f || temper >= 37.3f) {
            KDXFSpeechManager.instance().playWaningRingNoStop();
            warningList.add(0, multiTemperBean);
            warningAdapter.notifyItemInserted(0);
            if (warningList.size() > 15) {
                int index = warningList.size() - 1;
                warningList.remove(warningList.get(index));
                warningAdapter.notifyItemRemoved(index);
            }
            rlvWarningList.scrollToPosition(0);

            sendWarningMessage(temper);
        } else {
            normalList.add(0, multiTemperBean);
            normalAdapter.notifyItemInserted(0);
            if (normalList.size() > 15) {
                int index = normalList.size() - 1;
                normalList.remove(normalList.get(index));
                normalAdapter.notifyItemRemoved(index);
            }
            rlvNormalList.scrollToPosition(0);
        }

        int trackId = multiTemperBean.getTrackId();
        //判断是缓存trackId里是否包含这个Id，如果包含代表已经计数过
        if (totalMap.containsKey(trackId)) {
            MultiTemperBean oldBean = totalMap.get(trackId);
            float oldTemper = oldBean.getTemper();
            float newTemper = multiTemperBean.getTemper();
            //判断两次的差值是否超过0.3f，如果是代表波动大，则加入数据库等待上传
            if (newTemper - oldTemper >= 0.3f || oldTemper - newTemper >= 0.3f) {
                totalMap.put(trackId, multiTemperBean);
                SignManager.instance().addSignToDB(multiTemperBean);
            }
            return;
        }
        totalMap.put(trackId, multiTemperBean);

        //如果map里不包含这个id，则直接加入数据库
        SignManager.instance().addSignToDB(multiTemperBean);
        handleData(multiTemperBean);
    }

    private void sendWarningMessage(float temper) {
        int comid = SpUtils.getCompany().getComid();
        if (comid == Constants.NOT_BIND_COMPANY_ID) {
            return;
        }
        String url = ResourceUpdate.SAFETY_SEND_WARNING_MESSAGE;
        Log.e(TAG, "sendWarningMessage: 发送异常短信:" + url);
        OkHttpUtils.post()
                .url(url)
                .addParams("deviceNo", HeartBeatClient.getDeviceNo())
                .addParams("temper", String.valueOf(temper))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "onError: " + (e == null ? "NULL" : e.getMessage()));
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.e(TAG, "onResponse: " + response);
                    }
                });
    }

    private Map<Integer, MultiTemperBean> totalMap = new HashMap<>();

    //加载统计数据
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private MultiTotal multiTotal;

    //初始化统计数据
    private void handleData() {
        int comid = SpUtils.getCompany().getComid();
        multiTotal = DaoManager.get().queryTotalByCompIfAndDate(comid, dateFormat.format(System.currentTimeMillis()));
        if (multiTotal != null) {
            totalNum = multiTotal.getTotalNum();
            warningNum = multiTotal.getWarningNum();
            normalNum = multiTotal.getNormalNum();
            companyNum = multiTotal.getStaffNum();
            visitorNum = multiTotal.getVisitorNum();
        }

        //总数
        tvTotalNum.setText(String.valueOf(totalNum));
        //正常统计
        String wNum = String.valueOf(warningNum);
        tvWarningNum.setText(wNum);
        String warningTotal = "<font color='#00FFFF'>" + getResources().getString(R.string.main_exception_record_multi_thermal) + "</font><font color='#F01854'>（" + wNum + "）</font>";
        tvWarningTotal.setText(Html.fromHtml(warningTotal));
        //异常统计
        String nNum = String.valueOf(normalNum);
        tvNormalNum.setText(nNum);
        String normalTotal = "<font color='#00FFFF'>" + getResources().getString(R.string.main_normal_record_multi_thermal) + "</font><font color='#ffffff'>（" + nNum + "）</font>";
        tvNormalTotal.setText(Html.fromHtml(normalTotal));
        //访客统计
        tvVisitorNum.setText(String.valueOf(visitorNum));
        //员工统计
        tvCompanyNum.setText(String.valueOf(companyNum));
    }

    //更新统计数据
    private void handleData(MultiTemperBean multiTemperBean) {
        float temper = multiTemperBean.getTemper();
        String faceId = multiTemperBean.getFaceId();

        tvTotalNum.setText(String.valueOf(++totalNum));
        if (temper <= 0f || temper >= 37.3f) {
            warningNum += 1;
            String s = String.valueOf(warningNum);
            tvWarningNum.setText(s);

            String warningTotal = "<font color='#00FFFF'>" + getResources().getString(R.string.main_exception_record_multi_thermal) + "</font><font color='#F01854'>（" + s + "）</font>";
            tvWarningTotal.setText(Html.fromHtml(warningTotal));
        } else {
            normalNum += 1;
            String s = String.valueOf(normalNum);
            tvNormalNum.setText(s);

            String normalTotal = "<font color='#00FFFF'>" + getResources().getString(R.string.main_normal_record_multi_thermal) + "</font><font color='#ffffff'>（" + s + "）</font>";
            tvNormalTotal.setText(Html.fromHtml(normalTotal));
        }
        if (TextUtils.equals("-1", faceId)) {
            visitorNum += 1;
            tvVisitorNum.setText(String.valueOf(visitorNum));
        } else {
            companyNum += 1;
            tvCompanyNum.setText(String.valueOf(companyNum));
        }

        if (multiTotal == null) {
            multiTotal = new MultiTotal();
        }
        multiTotal.setTotalNum(totalNum);
        multiTotal.setStaffNum(companyNum);
        multiTotal.setVisitorNum(visitorNum);
        multiTotal.setWarningNum(warningNum);
        multiTotal.setNormalNum(normalNum);
        multiTotal.setCompId(SpUtils.getCompany().getComid());
        multiTotal.setDate(dateFormat.format(System.currentTimeMillis()));
        DaoManager.get().addOrUpdate(multiTotal);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateInfoEvent event) {
        Company company = SpUtils.getCompany();
        if (!TextUtils.isEmpty(company.getAbbname())) {
            tvTitle.setText(company.getAbbname());
        }

        EventBus.getDefault().post(new UpdateMediaEvent());

        ivLogo.setVisibility(View.VISIBLE);
        tvLogo.setVisibility(View.GONE);
        ImageFileLoader.i().loadAndSave(this, company.getComlogo(), Constants.DATA_PATH, ivLogo);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(ResetLogoEvent event) {
        Company company = SpUtils.getCompany();
        ivLogo.setVisibility(View.VISIBLE);
        tvLogo.setVisibility(View.GONE);
        ImageFileLoader.i().loadAndSave(this, company.getComlogo(), Constants.DATA_PATH, ivLogo);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(DisplayOrientationEvent event) {
        Log.e(TAG, "update: 收到摄像头更新事件");
        faceView.changeAngle();
    }

    private void startXmpp() {//开启xmpp
        serviceManager = new ServiceManager(this);
        serviceManager.startService();
    }

    private void destoryXmpp() {
        if (serviceManager != null) {
            serviceManager.stopService();
            serviceManager = null;
        }
    }

    public Bitmap horverImage(Bitmap bitmap, boolean H, boolean V) {
        int bmpWidth = bitmap.getWidth();
        int bmpHeight = bitmap.getHeight();
        Matrix matrix = new Matrix();
        if (H)
            matrix.postScale(-1, 1);   //水平翻转H
        if (V)
            matrix.postScale(1, -1);   //垂直翻转V
        if (H && V)
            matrix.postScale(-1, -1);   //水平&垂直翻转HV
        return Bitmap.createBitmap(bitmap, 0, 0, bmpWidth, bmpHeight, matrix, true);
    }

    public void showSystemInfo(View view) {
        showSystemInfoPopup();
    }

    /***
     * 自动校准
     * ============================================================================================
     * ============================================================================================
     * ============================================================================================
     * ============================================================================================
     */
    public void startCalibration(View view) {
        btnCorrect.setVisibility(View.GONE);
        llCorrectionArea.setVisibility(View.VISIBLE);
        initCalibration();
    }

    private boolean isCalibration = false;
    private float mCurrCorrectValue;
    private float mCurrBodyTemper;
    private List<Float> mCorrectionTemperList = new ArrayList<>();

    private void initCalibration() {
        btnCorrectCancel.setEnabled(false);
        btnCorrectConfirm.setEnabled(false);
        btnBodyTSub.setEnabled(false);
        btnBodyTPlus.setEnabled(false);
        btnCorrPlus.setEnabled(false);
        btnCorrSub.setEnabled(false);

        mCurrCorrectValue = mCorrectValue;
        mCurrBodyTemper = mBodyTemper;

        edtCorr.setText(mCurrCorrectValue + "");
        edtBodyT.setText(mCurrBodyTemper + "");
        tvOriginT.setText(0.0f + "℃");

        //校正值加减
        ButtonClickListener buttonLongClick = new ButtonClickListener() {
            @Override
            public void onShortClick(int viewId) {
                if (viewId == R.id.btn_correct_plus_safety_check) {
                    mCurrCorrectValue += 0.1f;
                    mCurrBodyTemper += 0.1f;
                } else {
                    mCurrCorrectValue -= 0.1f;
                    mCurrBodyTemper -= 0.1f;
                }

                mCurrCorrectValue = formatF(mCurrCorrectValue);
                edtCorr.setText("" + mCurrCorrectValue);
                TemperatureModule.getIns().setmCorrectionValue(mCurrCorrectValue);

                mCurrBodyTemper = formatF(mCurrBodyTemper);
                edtBodyT.setText(mCurrBodyTemper + "");
            }

            @Override
            public void onLongClick(int viewId) {
                if (viewId == R.id.btn_correct_plus_safety_check) {
                    mCurrCorrectValue += 0.1f;
                    mCurrBodyTemper += 0.1f;
                } else {
                    mCurrCorrectValue -= 0.1f;
                    mCurrBodyTemper -= 0.1f;
                }
                mCurrCorrectValue = formatF(mCurrCorrectValue);
                edtCorr.setText("" + mCurrCorrectValue);
                TemperatureModule.getIns().setmCorrectionValue(mCurrCorrectValue);

                mCurrBodyTemper = formatF(mCurrBodyTemper);
                edtBodyT.setText(mCurrBodyTemper + "");
            }

            @Override
            public void onLongClickFinish(int viewId) {
            }
        };
        btnCorrSub.setOnTouchListener(buttonLongClick);
        btnCorrPlus.setOnTouchListener(buttonLongClick);

        //体温加减
        ButtonClickListener bodyTemperClick = new ButtonClickListener() {
            @Override
            public void onShortClick(int viewId) {
                if (viewId == R.id.btn_body_temper_plus_safety_check) {
                    mCurrBodyTemper += 0.1f;
                    mCurrCorrectValue += 0.1f;
                } else {
                    mCurrBodyTemper -= 0.1f;
                    mCurrCorrectValue -= 0.1f;
                }

                mCurrBodyTemper = formatF(mCurrBodyTemper);
                edtBodyT.setText(mCurrBodyTemper + "");

                mCurrCorrectValue = formatF(mCurrCorrectValue);
                edtCorr.setText("" + mCurrCorrectValue);
                TemperatureModule.getIns().setmCorrectionValue(mCurrCorrectValue);
            }

            @Override
            public void onLongClick(int viewId) {
                if (viewId == R.id.btn_body_temper_plus_safety_check) {
                    mCurrBodyTemper += 0.1f;
                    mCurrCorrectValue += 0.1f;
                } else {
                    mCurrBodyTemper -= 0.1f;
                    mCurrCorrectValue -= 0.1f;
                }
                mCurrBodyTemper = formatF(mCurrBodyTemper);
                edtBodyT.setText(mCurrBodyTemper + "");

                mCurrCorrectValue = formatF(mCurrCorrectValue);
                edtCorr.setText("" + mCurrCorrectValue);
                TemperatureModule.getIns().setmCorrectionValue(mCurrCorrectValue);
            }
        };
        btnBodyTPlus.setOnTouchListener(bodyTemperClick);
        btnBodyTSub.setOnTouchListener(bodyTemperClick);

        //取消、确定键
        View.OnClickListener onClickListener = v -> {
            faceView.setDrawSecondFaceRect(true);
            isCalibration = false;
            mCorrectionTemperList.clear();
            if (runnable != null) {
                runnable.run();
            }
            if (v.getId() == R.id.btn_confirm_correction_safety_check) {
                UIUtils.showShort(MultiThermalActivity.this, APP.getContext().getResources().getString(R.string.safety_save_completion));
                mCorrectValue = mCurrCorrectValue;
                mBodyTemper = mCurrBodyTemper;
                SpUtils.saveFloat(MultiThermalConst.Key.CORRECT_VALUE, mCorrectValue);
                SpUtils.saveFloat(MultiThermalConst.Key.BODY_TEMPER, mBodyTemper);
                Log.e(TAG, "最终保存校准值：" + mCorrectValue);
                //点击确定或取消，
                TemperatureModule.getIns().setmCorrectionValue(mCorrectValue);

                if(mCorrectValue != MultiThermalConst.Default.BODY_CORRECT_TEMPER){
                    mTempMode = true;
                    TemperatureModule.getIns().setHotImageColdMode(mTempMode);
                }
            } else {
                UIUtils.showShort(MultiThermalActivity.this, APP.getContext().getResources().getString(R.string.safety_save_cancel));
                TemperatureModule.getIns().setmCorrectionValue(mCorrectValue);
            }

            btnCorrect.setVisibility(View.VISIBLE);
            llCorrectionArea.setVisibility(View.GONE);
        };
        btnCorrectConfirm.setOnClickListener(onClickListener);
        btnCorrectCancel.setOnClickListener(onClickListener);

        TemperatureModule.getIns().setmCorrectionValue(0.0f);
        sendCalibrationTipMessage(200);
    }

    private void sendCalibrationTipMessage(int delay) {
        handler.sendEmptyMessageDelayed(0, delay);
    }

    private void sendCalibrationMessage(int delay) {
        handler.sendEmptyMessageDelayed(1, delay);
    }

    private int time = 3;
    private Handler handler = new Handler(msg -> {
        switch (msg.what) {
            case 0:
                faceView.setDrawSecondFaceRect(false);
                isCalibration = true;
                if (time < 1) {
                    time = 3;
                    sendCalibrationMessage(200);
                    break;
                }
                UIUtils.showShort(MultiThermalActivity.this, getResString(R.string.safety_correct_temper_tip) + " " + time);
                time--;
                sendCalibrationTipMessage(1000);
                break;
            case 1:
                UIUtils.showShort(MultiThermalActivity.this, getResString(R.string.safety_correct_temper_ing));
                if (mCorrectionTemperList.size() >= 30) {
                    UIUtils.showShort(MultiThermalActivity.this, getResString(R.string.safety_correct_temper_completion));

                    Float max = getMean(mCorrectionTemperList);
                    tvOriginT.setText(max + "℃");

                    if (max <= mCurrBodyTemper) {
                        tvOriginT.setTextColor(Color.GREEN);
                    } else {
                        tvOriginT.setTextColor(Color.RED);
                    }

                    d("原始值：" + max);
                    mCurrCorrectValue = mCurrBodyTemper - max;
                    mCurrCorrectValue = formatF(mCurrCorrectValue);
                    d("校准值：" + mCurrCorrectValue);

                    edtCorr.setText(mCurrCorrectValue + "");
                    TemperatureModule.getIns().setmCorrectionValue(mCurrCorrectValue);

                    btnCorrectCancel.setEnabled(true);
                    btnCorrectConfirm.setEnabled(true);
                    btnBodyTSub.setEnabled(true);
                    btnBodyTPlus.setEnabled(true);
                    btnCorrPlus.setEnabled(true);
                    btnCorrSub.setEnabled(true);
                    break;
                }
                sendCalibrationMessage(1000);
                break;
        }

        return false;
    });

    /***
     * 校准人脸框
     * ============================================================================================
     * ============================================================================================
     * ============================================================================================
     * ============================================================================================
     */
    private void startCalibrationBox() {
        btnCorrect.setVisibility(View.GONE);
        llCorrectionBoxArea.setVisibility(View.VISIBLE);
    }

    private BoxUpOrDownThread boxUpOrDownThread;
    private BoxSubOrAddThread boxSubOrAddThread;
    private boolean isUpOrDownLongClick = false;
    private boolean isSubOrAddLongClick = false;

    private void initCalibrationBoxBtn() {
        boxPortraitOffset = SpUtils.getIntOrDef(SpUtils.MULTI_BOX_PORTRAIT_OFFSET, 0);
        boxSizeOffset = SpUtils.getIntOrDef(SpUtils.MULTI_BOX_SIZE_OFFSET, 0);

        View.OnClickListener clickListener = v -> {
            switch (v.getId()) {
                case R.id.btn_cancel_correction_box:
                    boxPortraitOffset = SpUtils.getIntOrDef(SpUtils.MULTI_BOX_PORTRAIT_OFFSET, 0);
                    boxSizeOffset = SpUtils.getIntOrDef(SpUtils.MULTI_BOX_SIZE_OFFSET, 0);
                    llCorrectionBoxArea.setVisibility(View.GONE);
                    btnCorrect.setVisibility(View.VISIBLE);
                    break;
                case R.id.btn_confirm_correction_box:
                    SpUtils.saveInt(SpUtils.MULTI_BOX_PORTRAIT_OFFSET, boxPortraitOffset);
                    SpUtils.saveInt(SpUtils.MULTI_BOX_SIZE_OFFSET, boxSizeOffset);
                    llCorrectionBoxArea.setVisibility(View.GONE);
                    btnCorrect.setVisibility(View.VISIBLE);
                    break;
            }
        };

        View.OnTouchListener upOrDownTouchListener = (v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (v.getId() == R.id.btn_box_up) {
                    boxUpOrDownThread = new BoxUpOrDownThread(true);
                } else {
                    boxUpOrDownThread = new BoxUpOrDownThread(false);
                }
                isUpOrDownLongClick = true;
                boxUpOrDownThread.start();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                isUpOrDownLongClick = false;
                boxUpOrDownThread = null;
            }
            return false;
        };

        View.OnTouchListener subOrAddTouchListener = (v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (v.getId() == R.id.btn_box_size_add) {
                    boxSubOrAddThread = new BoxSubOrAddThread(true);
                } else {
                    boxSubOrAddThread = new BoxSubOrAddThread(false);
                }
                isSubOrAddLongClick = true;
                boxSubOrAddThread.start();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                isSubOrAddLongClick = false;
                boxSubOrAddThread = null;
            }
            return false;
        };

        btnBoxUp.setOnTouchListener(upOrDownTouchListener);
        btnBoxDown.setOnTouchListener(upOrDownTouchListener);
        btnBoxSizeAdd.setOnTouchListener(subOrAddTouchListener);
        btnBoxSizeSub.setOnTouchListener(subOrAddTouchListener);
        btnCancelCorrectionBox.setOnClickListener(clickListener);
        btnConfirmCorrectionBox.setOnClickListener(clickListener);
    }

    private class BoxUpOrDownThread extends Thread {
        private boolean isUp;
        public BoxUpOrDownThread(boolean isUp) {
            this.isUp = isUp;
        }
        @Override
        public void run() {
            while (isUpOrDownLongClick) {
                try {
                    Thread.sleep(200);
                    if (isUp) {
                        boxPortraitOffset -= 1;
                    } else {
                        boxPortraitOffset += 1;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            super.run();
        }
    }

    private class BoxSubOrAddThread extends Thread {
        private boolean isAdd;
        public BoxSubOrAddThread(boolean isAdd) {
            this.isAdd = isAdd;
        }
        @Override
        public void run() {
            while (isSubOrAddLongClick) {
                try {
                    Thread.sleep(200);
                    if (isAdd) {
                        boxSizeOffset += 1;
                    } else {
                        if (boxSizeOffset > 0) {
                            boxSizeOffset -= 1;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            super.run();
        }
    }
}
