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
import android.view.View;
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
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.DisplayOrientationEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.ResetLogoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateMediaEvent;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.MultiTotal;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.db2.User;
import com.yunbiao.ybsmartcheckin_live_id.utils.L;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.yunbiao.ybsmartcheckin_live_id.views.ImageFileLoader;
import com.yunbiao.ybsmartcheckin_live_id.xmpp.ServiceManager;

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

public class MultiThermalActivity extends BaseMultiThermalActivity {

    private static final String TAG = "MultiThermalActivity";
    private ImageView ivHotImage;
    private FaceView faceView;
    private boolean mThermalMirror;
    private boolean mLowTemp;
    private RecyclerView rlvWarningList;
    private MultiThermalRecordAdapter warningAdapter;
    private List<MultiTemperBean> warningList = new ArrayList<>();
    private List<MultiTemperBean> normalList = new ArrayList<>();
    private RecyclerView rlvNormalList;
    private MultiThermalRecordAdapter normalAdapter;

    private float mWarningTemper = 37.3f;
    private float mNormalTemper = 35.5f;

    private int totalNum = 0;
    private int normalNum = 0;
    private int warningNum = 0;
    private int companyNum = 0;
    private int visitorNum = 0;
    private TextView tvTotalNum;
    private TextView tvNormalNum;
    private TextView tvWarningNum;
    private TextView tvCompanyNum;
    private TextView tvVisitorNum;
    private TextView tvNormalTotal;
    private TextView tvWarningTotal;
    private ServiceManager serviceManager;
    private TextView tvTitle;
    private ImageView ivLogo;
    private float mBodyCorrectTemper;
    private Rect mBlackBodyAreaRect;

    @Override
    protected int getLayout() {
        return R.layout.activity_multi_thermal;
    }

    @Override
    protected void initView() {
        super.initView();
        APP.setMainActivity(this);
        tvTotalNum = findViewById(R.id.tv_total_num_multi_thermal);
        tvNormalNum = findViewById(R.id.tv_normal_num_multi_thermal);
        tvWarningNum = findViewById(R.id.tv_warning_num_multi_normal);
        tvCompanyNum = findViewById(R.id.tv_company_num_multi_thermal);
        tvVisitorNum = findViewById(R.id.tv_visitor_num_multi_thermal);
        tvNormalTotal = findViewById(R.id.tv_normal_total_multi_thermal);
        tvWarningTotal = findViewById(R.id.tv_warning_total_multi_thermal);
        tvTitle = findViewById(R.id.gtv_title_multi_thermal);
        ivLogo = findViewById(R.id.iv_logo_multi_thermal);

        faceView = findViewById(R.id.face_view);
        ivHotImage = findViewById(R.id.iv_hot_image);
        setHorizontalRlv();
        setVerticalRlv();
        //开启多人识别模式
        faceView.enableMutiple(true);
        //关闭多次重试
        faceView.enableMultiRetry(false);
        //开启多次回调
        faceView.enableMultiCallback(true);
        //设置回调延时
        faceView.setRetryDelayTime(500);
        SecondFaceRectView secondFaceRectView = findViewById(R.id.face_rect_view_hot_image);
        faceView.setSecondFaceRectView(secondFaceRectView);
        faceView.setCallback(faceCallback);
        startXmpp();

        if (Constants.isHT) {
            ivLogo.setImageResource(R.mipmap.logo_icon_horizontal);
            ImageFileLoader.setDefaultLogoId(R.mipmap.logo_icon_horizontal);
        }
    }

    @Override
    protected void initData() {
        addAllItemRecord();
        handleData();
    }

    private void setHorizontalRlv() {
        rlvNormalList = findViewById(R.id.rlv_normal_list);
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
        rlvWarningList = findViewById(R.id.rlv_warning_list);
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

        mBlackBodyAreaRect = getCacheRect();
        mWarningTemper = SpUtils.getFloat(MultiThermalConst.Key.WARNING_TEMP, MultiThermalConst.Default.WARNING_TEMP);
        mThermalMirror = SpUtils.getBoolean(MultiThermalConst.Key.THERMAL_MIRROR, MultiThermalConst.Default.THERMAL_MIRROR);
        mLowTemp = SpUtils.getBoolean(MultiThermalConst.Key.LOW_TEMP, MultiThermalConst.Default.LOW_TEMP);
        mBodyCorrectTemper = SpUtils.getFloat(MultiThermalConst.Key.BODY_CORRECT_TEMPER, MultiThermalConst.Default.BODY_CORRECT_TEMPER);

        startHotImage();
    }

    private Rect getCacheRect(){
        int left = SpUtils.getIntOrDef(MultiThermalConst.Key.CORRECT_AREA_LEFT, MultiThermalConst.Default.CORRECT_AREA_LEFT);
        int top = SpUtils.getIntOrDef(MultiThermalConst.Key.CORRECT_AREA_TOP, MultiThermalConst.Default.CORRECT_AREA_TOP);
        int right = SpUtils.getIntOrDef(MultiThermalConst.Key.CORRECT_AREA_RIGHT, MultiThermalConst.Default.CORRECT_AREA_RIGHT);
        int bottom = SpUtils.getIntOrDef(MultiThermalConst.Key.CORRECT_AREA_BOTTOM, MultiThermalConst.Default.CORRECT_AREA_BOTTOM);
        L.e("MultiThermalActivity","onResume:取出的数值：" + left + " --- " + top + " --- " + right + " --- " + bottom);
        return new Rect(left,top,right,bottom);
    }

    private void startHotImage() {
        int usbPermission = TemperatureModule.getIns().initUsbDevice(this, new UsbPermissionCallBack() {
            @Override
            public void usbPermissionNotice(boolean b) {
                if (!b) {
                    UIUtils.showShort(MultiThermalActivity.this, getResources().getString(R.string.main_permission_failed_multi_thermal));
                    return;
                }

                //usb设备初始化成功
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //开启热成像6080模块
                        //isMirror:热成像画面是否左右镜像, isCold:是否为低温补偿模式, hotImageK6080CallBack:数据回调
                        TemperatureModule.getIns().startHotImageK6080(mThermalMirror, mLowTemp, hotImageK6080CallBack);
                        BlackBody blackBody = new BlackBody(mBlackBodyAreaRect.left, mBlackBodyAreaRect.right, mBlackBodyAreaRect.top, mBlackBodyAreaRect.bottom);
                        blackBody.setFrameColor(Color.WHITE);
                        blackBody.setTempPreValue(345);
                        TemperatureModule.getIns().setmCorrectionValue(mBodyCorrectTemper);
                        TemperatureModule.getIns().startK6080BlackBodyMode(blackBody);
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
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //开启热成像6080模块
                    //isMirror:热成像画面是否左右镜像, isCold:是否为低温补偿模式, hotImageK6080CallBack:数据回调
                    TemperatureModule.getIns().startHotImageK6080(mThermalMirror, mLowTemp, hotImageK6080CallBack);
                    BlackBody blackBody = new BlackBody(mBlackBodyAreaRect.left, mBlackBodyAreaRect.right, mBlackBodyAreaRect.top, mBlackBodyAreaRect.bottom);
                    blackBody.setFrameColor(Color.WHITE);
                    blackBody.setTempPreValue(345);
                    TemperatureModule.getIns().setmCorrectionValue(mBodyCorrectTemper);
                    TemperatureModule.getIns().startK6080BlackBodyMode(blackBody);
                }
            }, 1000);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        faceView.pause();
        TemperatureModule.getIns().closeHotImageK6080();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        faceView.destory();
        destoryXmpp();
    }

    private boolean mHasFace = false;
    private FaceView.FaceCallback faceCallback = new FaceView.FaceCallback() {
        @Override
        public void onReady() {
            SignManager.instance();
            FaceManager.getInstance().init(MultiThermalActivity.this);
            SyncManager.instance().requestOnlyCompany();
        }

        @Override
        public void onFaceDetection(Boolean hasFace, List<FacePreviewInfo> facePreviewInfoList) {
            mHasFace = hasFace;
            if (hasFace) {
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
            } else {
                if (totalMap.size() > 0) {
                    totalMap.clear();
                }
            }
        }

        @Override
        public boolean onFaceDetection(boolean hasFace, FacePreviewInfo facePreviewInfo) {
            return false;
        }

        @Override
        public void onFaceVerify(CompareResult faceAuth) {
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
                    hotRect.left -= 3;
                    if (hotRect.left <= 0) {
                        hotRect.left = 1;
                    }
                    hotRect.top -= 5;
                    if (hotRect.top <= 0) {
                        hotRect.top = 1;
                    }
                    multiTemperBean.setHotRect(hotRect);
                }
            }
            addResultAndNotifyData(multiTemperBean);
        }
    };

    private Bitmap mCurrHotImage = null;
    private HotImageK6080CallBack hotImageK6080CallBack = new HotImageK6080CallBack() {
        @Override
        public void newestHotImageData(final Bitmap bitmap, float v, float v1, float v2) {
            /*if (!mHasFace) {
                setHotImage(bitmap);
            }*/
            setHotImage(bitmap);
        }

        @Override
        public void newestHotImageData(final Bitmap bitmap, ArrayList<FaceIndexInfo> arrayList) {
            setHotImage(bitmap);
            /*if (mHasFace) {
                setHotImage(bitmap);
            }*/
            faceView.setTemperList(arrayList);
        }
    };

    private void setHotImage(final Bitmap bitmap) {
        mCurrHotImage = bitmap;
        ivHotImage.post(new Runnable() {
            @Override
            public void run() {
                ivHotImage.setImageBitmap(bitmap);
            }
        });
    }

    //设置人脸框
    private void setFaceIndex(List<FacePreviewInfo> facePreviewInfos) {
        ArrayList<FaceIndexInfo> faceIndexInfoList = new ArrayList<>();

        for (int i = 0; i < facePreviewInfos.size(); i++) {
            FacePreviewInfo facePreviewInfo = facePreviewInfos.get(i);
            FaceInfo faceInfo = facePreviewInfo.getFaceInfo();
            Rect realRect = faceView.getRealRect(faceInfo.getRect());
            Rect hotRect = adjustRect(faceView.getWidth(), faceView.getHeight(), realRect);
            //6080热成像画面以左上角为原点，0 <= left <= right <= 80，0 <= top <= bottom <= 60
            //left, right, top, bottom
            FaceIndexInfo faceIndexInfo = new FaceIndexInfo(facePreviewInfo.getTrackId(), hotRect.left - 3, hotRect.right, hotRect.top - 5, hotRect.bottom);
            //可以支持一次传入多个人脸框信息
            faceIndexInfoList.add(faceIndexInfo);
        }

        //设置热成像画面中人脸框的颜色，默认为黑色
        TemperatureModule.getIns().setFaceFrameColor(0xFFFFFFFF);
        //人脸框信息faceIndexInfoList，是否在热成像画面中绘制人脸框(true为绘制)
        TemperatureModule.getIns().setFaceIndexInfoList(faceIndexInfoList, false);
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

    private void saveMultiTemperBeanToDB(MultiTemperBean multiTemperBean) {
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
    }

    //添加条目到记录中
    private void addItemRecord(MultiTemperBean multiTemperBean) {
        float temper = multiTemperBean.getTemper();
        if (temper <= 0f || temper >= 37.3f) {
            warningList.add(0, multiTemperBean);
            warningAdapter.notifyItemInserted(0);
            if (warningList.size() > 15) {
                int index = warningList.size() - 1;
                warningList.remove(warningList.get(index));
                warningAdapter.notifyItemRemoved(index);
            }
            rlvWarningList.scrollToPosition(0);
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

    private Map<Integer, MultiTemperBean> totalMap = new HashMap<>();

    //加载统计数据
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private MultiTotal multiTotal;

    //初始化统计数据
    private void handleData() {
        int comid = SpUtils.getCompany().getComid();
        Log.e(TAG, "handleData: ===== " + comid);
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
        if (tvTitle != null) tvTitle.setText(company.getAbbname());

        EventBus.getDefault().post(new UpdateMediaEvent());

        ImageFileLoader.i().loadAndSave(this, company.getComlogo(), Constants.DATA_PATH, ivLogo);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(ResetLogoEvent event) {
        Company company = SpUtils.getCompany();
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
}
