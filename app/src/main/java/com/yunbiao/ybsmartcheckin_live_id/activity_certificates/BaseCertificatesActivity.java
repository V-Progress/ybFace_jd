package com.yunbiao.ybsmartcheckin_live_id.activity_certificates;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.google.gson.Gson;
import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.callback.HotImageK1604CallBack;
import com.intelligence.hardware.temperature.callback.HotImageK3232CallBack;
import com.yunbiao.faceview.CertificatesView;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseGpioActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.CertificatesUser;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.White;
import com.yunbiao.ybsmartcheckin_live_id.printer.PrinterUtils;
import com.yunbiao.ybsmartcheckin_live_id.printer.UsbPrinterStatus;
import com.yunbiao.ybsmartcheckin_live_id.serialport.InfraredTemperatureUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.IDCardReader;
import com.yunbiao.ybsmartcheckin_live_id.utils.IdCardMsg;
import com.yunbiao.ybsmartcheckin_live_id.utils.NetWorkChangReceiver;
import com.yunbiao.ybsmartcheckin_live_id.utils.ScanKeyManager;
import com.yunbiao.ybsmartcheckin_live_id.utils.SoftKeyBoardListener;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;

public abstract class BaseCertificatesActivity extends BaseGpioActivity implements CertificatesView.FaceCallback {

    private boolean mThermalImgMirror;
    private Float mCorrectValue;
    private boolean mLowTemp;
    private int mMode;
    private Float mMinThreshold;
    private Float mWarningThreshold;
    private int mSimilar;

    private CertificatesViewInterface viewInterface;
    private CertificatesView mCertificatesView;
    private boolean mWhiteListEnable;
    private boolean mUsbPrinterEnabled;
    private boolean icCardMode;

    @Override
    protected void initData() {
        super.initData();
        KDXFSpeechManager.instance().init(this);
        IDCardReader.getInstance().startReaderThread(this, readListener);
        registerNetState();
        initUsbPrinter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        icCardMode = SpUtils.getBoolean(CertificatesConst.Key.IC_CARD_MODE, CertificatesConst.Default.IC_CARD_MODE);
        mWhiteListEnable = SpUtils.getBoolean(CertificatesConst.Key.WHITE_LIST, CertificatesConst.Default.WHITE_LIST);
        mThermalImgMirror = SpUtils.getBoolean(CertificatesConst.Key.THERMAL_MIRROR, CertificatesConst.Default.THERMAL_MIRROR);
        mMinThreshold = SpUtils.getFloat(CertificatesConst.Key.MIN_THRESHOLD, CertificatesConst.Default.MIN_THRESHOLD);
        mWarningThreshold = SpUtils.getFloat(CertificatesConst.Key.WARNING_THRESHOLD, CertificatesConst.Default.WARNING_THRESHOLD);
        mCorrectValue = SpUtils.getFloat(CertificatesConst.Key.CORRECT_VALUE, CertificatesConst.Default.CORRECT_VALUE);

        mLowTemp = SpUtils.getBoolean(CertificatesConst.Key.LOW_TEMP, CertificatesConst.Default.LOW_TEMP);
        //相似度
        mSimilar = SpUtils.getIntOrDef(CertificatesConst.Key.SIMILAR, CertificatesConst.Default.SIMILAR);
        int mode = SpUtils.getIntOrDef(CertificatesConst.Key.MODE, CertificatesConst.Default.MODE);
        if (mMode != mode) {
            mMode = mode;
            if (viewInterface != null) {
                viewInterface.onModeChanged(mMode);
            }
        }
        startTemperModule();

        initScanQrCodeReader();

        mUsbPrinterEnabled = SpUtils.getBoolean(CertificatesConst.Key.USB_PRINTER_ENABLED, CertificatesConst.Default.USB_PRINTER_ENABLED);
    }

    private void initUsbPrinter() {
        boolean open = PrinterUtils.getInstance().openDevice(this);
        if (!open) {
            UIUtils.showShort(this, getResString(R.string.act_certificates_no_printer));
            return;
        }

        int printerStatus = PrinterUtils.getInstance().getPrinterStatus();
        String stringStatus = UsbPrinterStatus.getStringStatus(printerStatus);
        if (!TextUtils.isEmpty(stringStatus)) {
            UIUtils.showShort(this, stringStatus);
        }
    }

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private void printMsg(IdCardMsg idCardMsg, float finalTemper, boolean isNormal, boolean isInWhite) {
        if (!mUsbPrinterEnabled) {
            return;
        }
        int printerStatus = PrinterUtils.getInstance().getPrinterStatus();
        if (printerStatus != UsbPrinterStatus.AVAILABLE //可用
                && printerStatus != UsbPrinterStatus.SDK_DONT_MATCH //不匹配
                && printerStatus != UsbPrinterStatus.PRINT_HEAD_OPEN //打印头已打开
                && printerStatus != UsbPrinterStatus.PAPER_LESS //纸较少
        ) {
            UIUtils.showShort(this, getResString(R.string.act_certificates_warning_printer) + UsbPrinterStatus.getStringStatus(printerStatus));
            return;
        }

        String name = idCardMsg.name;
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(getResString(R.string.act_certificates_title)).append("\n");
        stringBuffer.append("==============================").append("\n");
        stringBuffer.append(getResString(R.string.act_certificates_printer_name)).append(name).append("\n").append("\n");
        if(icCardMode){
            stringBuffer.append(getResString(R.string.act_certificates_printer_depart)).append(idCardMsg.nation_str).append("\n").append("\n");
        } else {
            stringBuffer.append(getResString(R.string.act_certificates_printer_native_place)).append(IDCardReader.getNativeplace(idCardMsg.id_num.substring(0, 6))).append("\n").append("\n");
        }
        stringBuffer.append(getResString(R.string.act_certificates_printer_temper)).append(finalTemper).append("℃");
        if (!isNormal) {
            stringBuffer.append(getResString(R.string.act_certificates_printer_error));
        }
        stringBuffer.append("\n").append("\n");

        stringBuffer.append(getResString(R.string.act_certificates_printer_time)).append(dateFormat.format(new Date())).append("\n");
        if (mWhiteListEnable) {
            stringBuffer.append("\n").append(getResString(R.string.act_certificates_printer_white)).append((isInWhite ? getResString(R.string.act_certificates_printer_white_yes) : getResString(R.string.act_certificates_printer_white_no))).append("\n");
        }
        stringBuffer.append("==============================").append("\n");
        stringBuffer.append(getResString(R.string.act_certificates_printer_tips)).append("\n");
        stringBuffer.append("==============================").append("\n");
        PrinterUtils.getInstance().printText(stringBuffer.toString());
    }

    //设置View接口
    protected void setCertificatesView(CertificatesViewInterface viewInterface) {
        this.viewInterface = viewInterface;
        if (this.viewInterface == null) {
            this.viewInterface = new CertificatesViewImpl();
        }
    }

    //设置人脸回调
    protected void setFaceCallback(CertificatesView certificatesView) {
        mCertificatesView = certificatesView;
        mCertificatesView.setCallback(this);
    }

    //无证测温
    protected void noCardToTemper() {
        mUpdateTemperList.clear();
    }

    //开启测温模块
    private void startTemperModule() {
        //横竖屏判断端口号
        String portPath = mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT ? "/dev/ttyS3" : "/dev/ttyS4";

        d("串口号：" + portPath);
        //判断模式
        if (mMode == CertificatesConst.Mode.CERTIFICATES_THERMAL) {
            resultHandler.postDelayed(() -> {
                TemperatureModule.getIns().initSerialPort(this, portPath, 115200);
                resultHandler.postDelayed(() -> TemperatureModule.getIns().startHotImageK3232(mThermalImgMirror, mLowTemp, hotImageK3232CallBack), 2000);
            }, 1000);
        } else if (mMode == CertificatesConst.Mode.CERTIFICATES_THERMAL_16_4) {
            d("当前模式：16——4");
            TemperatureModule.getIns().initSerialPort(this, portPath, 19200);
            resultHandler.postDelayed(() -> TemperatureModule.getIns().startHotImageK1604(mThermalImgMirror, mLowTemp, hotImageK1604CallBack), 2000);
        }

        TemperatureModule.getIns().setmCorrectionValue(mCorrectValue);
    }

    //注册网络状态
    private void registerNetState() {
        NetWorkChangReceiver netWorkChangReceiver = new NetWorkChangReceiver(new NetWorkChangReceiver.NetWorkChangeListener() {
            @Override
            public void connect() {
                if (viewInterface == null) {
                    viewInterface.onNetStateChanged(true);
                }
            }

            @Override
            public void disConnect() {
                if (viewInterface != null) {
                    viewInterface.onNetStateChanged(false);
                }
            }
        });
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkChangReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mMode == CertificatesConst.Mode.CERTIFICATES_THERMAL) {
            TemperatureModule.getIns().closeHotImageK3232();
        } else if (mMode == CertificatesConst.Mode.CERTIFICATES_THERMAL_16_4) {
            TemperatureModule.getIns().closeHotImageK1604();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeCardReader();//关闭读卡器
        InfraredTemperatureUtils.getIns().closeSerialPort();
        KDXFSpeechManager.instance().destroy();
    }

    /*===关键数据====================================================================*/
    /*===关键数据====================================================================*/
    /*===关键数据====================================================================*/
    /*===关键数据====================================================================*/
    private boolean mHasFace = false;
    private IdCardMsg idCardMsg;
    private List<Float> mDetectTemperList = new ArrayList<>();
    //温度收集
    private HotImageK3232CallBack hotImageK3232CallBack = new HotImageK3232CallBack() {
        @Override
        public void newestHotImageData(Bitmap bitmap, float sensorT, float afterT, float v2, float v3, boolean b, int i) {
            dismissAlert();
            handleTemper(bitmap, sensorT, afterT);
        }

        @Override
        public void dataRecoveryFailed() {
//            showRestartAlert(getResString(R.string.temper_error_tips),getResString(R.string.temper_error_btn_restart), () -> PowerOffTool.getPowerOffTool().restart());
        }
    };
    private HotImageK1604CallBack hotImageK1604CallBack = new HotImageK1604CallBack() {
        @Override
        public void newestHotImageData(Bitmap bitmap, final float originalMaxT, final float afterF, final float minT) {
            dismissAlert();
            handleTemper(bitmap, originalMaxT, afterF);
        }

        @Override
        public void dataRecoveryFailed() {
//            showRestartAlert(getResString(R.string.temper_error_tips),getResString(R.string.temper_error_btn_restart), () -> PowerOffTool.getPowerOffTool().restart());
        }
    };

    private List<Float> mUpdateTemperList = new ArrayList<>();

    private void handleTemper(Bitmap bitmap, float sensorT, float afterT) {
        sendUpdateHotImageMessage(bitmap, sensorT);
        if (mHasFace) {
            if (afterT < mMinThreshold) {
                return;
            }
            //更新实时测温
            if (mUpdateTemperList.size() <= 5) {
                mUpdateTemperList.add(afterT);
                if (mUpdateTemperList.size() == 5) {
                    Float max = Collections.max(mUpdateTemperList);
                    sendUpdateRealTimeTemper(max);
                }
            }

            //添加测温结果
            if (mDetectTemperList.size() > 5) {
                mDetectTemperList.remove(0);
            }
            mDetectTemperList.add(afterT);
        } else {
            if (mUpdateTemperList.size() > 0) {
                mUpdateTemperList.clear();
                sendClearRealTimeTemper();
            }
            if (mDetectTemperList.size() > 0) {
                mDetectTemperList.clear();
            }
        }
    }

    //人脸收集
    @Override
    public void onReady() {
        if (viewInterface != null) {
            viewInterface.onFaceViewReady();
        }
    }

    @Override
    public void onFaceDetection(boolean hasFace, FaceInfo faceInfo) {
        mHasFace = hasFace;
        if (mHasFace) {
            onLight();
            sendUpdateFaceImageMessage(faceInfo);
        } else {

        }
    }

    protected IdCardMsg testIdCard() {
        IdCardMsg idCardMsg = new IdCardMsg();
        Bitmap bitmap = BitmapFactory.decodeFile(new File(Environment.getExternalStorageDirectory(), "12345.jpg").getPath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] datas = baos.toByteArray();

        idCardMsg.name = "张威";
        idCardMsg.id_num = "130481199308122419";
        idCardMsg.ptoto = datas;
        idCardMsg.sex = "男";
        idCardMsg.nation_str = "汉族";
        idCardMsg.birth_year = "1993";
        idCardMsg.useful_e_date_year = "1993";
        idCardMsg.birth_month = "08";
        idCardMsg.useful_e_date_month = "08";
        idCardMsg.birth_day = "12";
        idCardMsg.useful_e_date_day = "12";
        idCardMsg.address = "A撒大大撒撒";

//        readListener.getCardInfo(idCardMsg);
        return idCardMsg;
    }

    //证件收集
    private IDCardReader.ReadListener readListener = new IDCardReader.ReadListener() {
        @Override
        public void getCardInfo(IdCardMsg msg) {
            d("贴卡，清除UI");
            sendClearUIMessage(0);
            KDXFSpeechManager.instance().playDingDong();
            if (msg == null || msg.name == null || msg.ptoto == null) {
                sendTipMessage(getResString(R.string.act_certificates_card_read_failed), true);
                return;
            }
            idCardMsg = msg;
            sendCardInfoMessage(msg);
            startCompareThread();
        }
    };
    /*===逻辑处理=============================================================================*/
    /*===逻辑处理=============================================================================*/
    /*===逻辑处理=============================================================================*/
    /*===逻辑处理=============================================================================*/
    /*===逻辑处理=============================================================================*/
    private boolean comparing = false;//对比线程运行
    private Thread compareThread;//对比线程
    private long waitFaceTime = 0;//等待人脸的时间戳
    private int waitFaceFeatureTimes = 0;//提取特征的次数
    private long waitTemperTime = 0;//等待测温的时间戳
    private float mCacheTemperature = 0.0f;
    private List<FaceFeature> faceFeatureList = new ArrayList<>();//特征缓存集合

    private void startCompareThread() {
        if (compareThread != null && compareThread.isAlive()) {
            d("对比线程正在运行");
        } else {
            sendTipMessage(getResString(R.string.act_certificates_verifing), true);
            comparing = true;
            compareThread = new Thread(compareRunnable);
            compareThread.start();
        }
    }

    private Runnable compareRunnable = () -> {
        while (comparing) {
            //等待人脸
            if (!mHasFace) {
                //判断延时，如果超过五秒没有人脸则结束对比并且清除UI
                long currMillis = System.currentTimeMillis();
                if (waitFaceTime == 0) {
                    waitFaceTime = currMillis;
                } else if (currMillis - waitFaceTime > 5000) {
                    d("等待人脸超时");
                    waitFaceTime = 0;
                    sendClearUIMessage(5000);
                    sendTipMessage(getResString(R.string.act_certficates_check_outtime), true);
                    stopCompareThread();
                    break;//检测超时重新贴卡
                }
                continue;//没有人脸，继续检测
            } else {
                waitFaceTime = 0;
            }

            //等待测温
            if (mDetectTemperList.size() < 5) {
                long l = System.currentTimeMillis();
                if (waitTemperTime == 0) {
                    waitFaceTime = l;
                } else if (l - waitTemperTime > 5000) {
                    d("测温超时");
                    waitTemperTime = 0;
                    if (mDetectTemperList.size() > 0) {
                        mDetectTemperList.clear();
                    }
                    sendClearUIMessage(5000);
                    sendTipMessage(getResString(R.string.act_certficates_check_failed), true);
                    stopCompareThread();
                    break;//测温超时，重新贴卡
                }
                continue;//测温数据不足，继续检测
            } else {
                waitFaceTime = 0;
            }
            mCacheTemperature = Collections.max(mDetectTemperList);

            //等待特征
            if (faceFeatureList.size() < 3) {
                FaceFeature faceFeature = mCertificatesView.getFaceFeature();
                if (faceFeature == null) {
                    //如果连续取特征失败超过五次则结束
                    if (waitFaceFeatureTimes >= 5) {
                        if (mDetectTemperList.size() > 0) {
                            mDetectTemperList.clear();
                        }
                        d("提取特征失败");
                        sendClearUIMessage(5000);
                        sendTipMessage(getResString(R.string.act_certficates_feature_outtime), true);
                        stopCompareThread();
                        break;//特征提取超时，重新贴卡
                    }
                    continue;//提取特征为null。继续提取
                } else {
                    waitFaceFeatureTimes = 0;
                }
                faceFeatureList.add(faceFeature);
                continue;
            }
            d("特征提取完成");

            //提取身份证信息
            Bitmap cardBitmap;
            if (icCardMode) {
                cardBitmap = BitmapFactory.decodeByteArray(idCardMsg.ptoto, 0, idCardMsg.ptoto.length);
            } else {
                byte[] bytes = IDCardReader.getInstance().decodeToBitmap(idCardMsg.ptoto);
                cardBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            }

            FaceFeature idCardFeature = mCertificatesView.inputIdCard(cardBitmap);
            if (idCardFeature == null) {
                if (mDetectTemperList.size() > 0) {
                    mDetectTemperList.clear();
                }
                if (faceFeatureList.size() > 0) {
                    faceFeatureList.clear();
                }
                sendTipMessage(getResString(R.string.act_certificates_card_read_failed), true);
                stopCompareThread();
                break;
            }

            //开始对比
            float finalSimilar = 0;
            for (FaceFeature feature : faceFeatureList) {
                FaceSimilar compare = mCertificatesView.compare(feature, idCardFeature);
                float comapareSimilar = compare.getScore();
                d("run: 对比:" + comapareSimilar);
                if (comapareSimilar > finalSimilar) {
                    finalSimilar = comapareSimilar;
                }
            }
            int similar = (int) (finalSimilar * 100);
            float max;
            if (mDetectTemperList.size() <= 0) {
                max = mCacheTemperature;
            } else {
                max = Collections.max(mDetectTemperList);
            }
            d("对比出结果：" + similar + "；" + max);
            sendResultMessage(similar, max);
            sendClearUIMessage(8000);

            mDetectTemperList.clear();
            faceFeatureList.clear();
            stopCompareThread();
        }
    };

    private void stopCompareThread() {
        if (comparing) {
            comparing = false;
        }
    }

    /*===消息交互区=============================================================================*/
    /*===消息交互区=============================================================================*/
    /*===消息交互区=============================================================================*/
    /*===消息交互区=============================================================================*/
    private long mUpdateFaceImageTime = 0;
    private Handler resultHandler = new Handler(msg -> {
        switch (msg.what) {
            case 0://更新热成像
                Bitmap bitmap = (Bitmap) msg.obj;
                float temper = msg.getData().getFloat("temper");
                if (viewInterface != null) {
                    viewInterface.updateHotImage(bitmap, temper, mHasFace);
                }
                break;
            case 1://更新卡片信息
                IdCardMsg idCardMsg = (IdCardMsg) msg.obj;
                if (idCardMsg != null) {
                    Bitmap mIdCardBitmap = null;
                    if (icCardMode) {
                        mIdCardBitmap = BitmapFactory.decodeByteArray(idCardMsg.ptoto, 0, idCardMsg.ptoto.length);
                    } else {
                        byte[] bytes = IDCardReader.getInstance().decodeToBitmap(idCardMsg.ptoto);
                        mIdCardBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    }
                    if (viewInterface != null) {
                        viewInterface.updateIdCardInfo(idCardMsg, mIdCardBitmap, icCardMode);
                    }
                }
                break;
            case 2://文字提示
                String tip = (String) msg.obj;
                if (viewInterface != null) {
                    viewInterface.updateTips(tip);
                }
                boolean isBroad = msg.getData().getBoolean("isBroad", true);
                if (isBroad) {
                    KDXFSpeechManager.instance().stopNormal();
                    KDXFSpeechManager.instance().playNormal(tip);
                }
                break;
            case 3://结果提示
                int similarInt = (int) msg.obj;
                float finalTemper = msg.getData().getFloat("temper");
                boolean isAlike = similarInt >= mSimilar;
                boolean isNormal = finalTemper > mMinThreshold && finalTemper < mWarningThreshold;
                boolean isInWhite = true;//白名单标识，如果不开启白名单，则此值不会变，也不会有相应的提示
                if (mWhiteListEnable) {
                    String id_num = BaseCertificatesActivity.this.idCardMsg.id_num;
                    d("身份证号：" + id_num);
                    String num1 = id_num.substring(0, 2);
                    String num2 = id_num.substring(2, 4);
                    String num3 = id_num.substring(4, 6);
                    d("一二位：" + num1);
                    d("三四位：" + num2);
                    d("五六位：" + num3);

                    List<White> whites = DaoManager.get().queryAll(White.class);
                    for (White white : whites) {
                        d("白名单：" + white.getNum());
                    }

                    White white = DaoManager.get().queryWhiteByTopSixNum(num1, num2, num3);
                    isInWhite = white != null;
                }

                StringBuffer resultBuffer = new StringBuffer();
                String speech = "";

                String v1 = getResString(R.string.act_certificates_verify_pass_yes);
                String v2 = getResString(R.string.act_certificates_verify_pass_no);

                String t1 = getResString(R.string.act_certificates_verify_temper_yes);
                String t2 = getResString(R.string.act_certificates_verify_temper_no);

                String p1 = getResString(R.string.act_certificates_verify_passage_no);
                String p2 = getResString(R.string.act_certificates_verify_passage_yes);
                String p3 = getResString(R.string.act_certificates_verify_passage_ensure);

                if (isAlike) {
                    resultBuffer.append("<font color='#00ff00'>" + v1 + "</font>");//绿色字体
                    speech += v1;
                } else {
                    resultBuffer.append("<font color='#ff0000'>" + v2 + "</font>");//红字
                    speech += v2;
                }
                resultBuffer.append("<font color='#ffffff'>，</font>");//白字
                if (isNormal) {
                    resultBuffer.append("<font color='#00ff00'>" + t1 + "</font>");//绿字
                    speech += "，" + t1;
                } else {
                    resultBuffer.append("<font color='#ff0000'>" + t2 + "</font>");//红字
                    speech += "，" + t2;
                }

                if (!isAlike || !isNormal) {
                    speech += "，" + p1;
                } else {
                    if (isInWhite) {
                        speech += "，" + p2;
                    } else {
                        speech += "，" + p3;
                    }
                }
                KDXFSpeechManager.instance().stopNormal();
                KDXFSpeechManager.instance().playNormal(speech, () -> {
                    if (!isNormal) {
                        KDXFSpeechManager.instance().playWaningRingNoStop();
                    }
                });

                if (isAlike) {
                    printMsg(BaseCertificatesActivity.this.idCardMsg, finalTemper, isNormal, isInWhite);
                }
                if (viewInterface != null) {
                    viewInterface.updateResultTip(resultBuffer.toString(), BaseCertificatesActivity.this.idCardMsg, finalTemper, similarInt, isAlike, isNormal, isInWhite, icCardMode);
                }
                if (isAlike && isNormal && isInWhite) {
                    ledGreen();
                    openDoor();
                } else {
                    ledRed();
                }
                break;
            case 4:
                ledInit();
                d("清除所有UI");
                if (viewInterface != null) {
                    viewInterface.resetAllUI();
                }
                break;
            case 5:
                KDXFSpeechManager.instance().playPassRing();
                float temperature = (float) msg.obj;
                boolean isTempNormal = temperature >= mMinThreshold && temperature < mWarningThreshold;
                KDXFSpeechManager.instance().playNormal(getResString(isTempNormal ? R.string.act_certificates_temper_normal : R.string.act_certificates_temper_warning));
                if (viewInterface != null) {
                    viewInterface.updateRealTimeTemper(temperature, isTempNormal);
                }
                break;
            case 6:
                if (viewInterface != null) {
                    viewInterface.clearRealTimeTemper();
                }
                break;
            case 7:
                float mTemper = (float) msg.obj;
                boolean normal = mTemper >= mMinThreshold && mTemper < mWarningThreshold;
                if (normal) {
                    KDXFSpeechManager.instance().playNormal(getResString(R.string.act_certificates_speech_pass_yes));
                } else {
                    KDXFSpeechManager.instance().playNormal(getResString(R.string.act_certificates_speech_pass_no));
                }
                if (viewInterface != null) {
                    viewInterface.updateCodeTemperResult(mTemper, normal);
                }
                if (normal) {
                    ledGreen();
                    openDoor();
                } else {
                    ledRed();
                }
                break;
        }
        return false;
    });

    //热图更新
    private void sendUpdateHotImageMessage(Bitmap bitmap, float temper) {
        Message message = Message.obtain();
        message.what = 0;
        message.obj = bitmap;
        Bundle bundle = new Bundle();
        bundle.putFloat("temper", temper);
        message.setData(bundle);
        resultHandler.sendMessage(message);
    }

    //显示人脸
    private void sendUpdateFaceImageMessage(FaceInfo faceInfo) {
        long currMillis = System.currentTimeMillis();
        if (mUpdateFaceImageTime == 0 || currMillis - mUpdateFaceImageTime > 1500) {
            mUpdateFaceImageTime = currMillis;
            Bitmap faceBitmap = mCertificatesView.getFaceBitmap(faceInfo);
            if (viewInterface != null) {
                viewInterface.updateFaceImage(faceBitmap);
            }
        }
    }

    //设置卡信息
    private void sendCardInfoMessage(IdCardMsg msg) {
        Message message = Message.obtain();
        message.what = 1;
        message.obj = msg;
        resultHandler.sendMessage(message);
    }

    //发送提示消息
    private void sendTipMessage(String tip, boolean isBroad) {
        Message message = Message.obtain();
        message.what = 2;
        message.obj = tip;
        Bundle bundle = new Bundle();
        bundle.putBoolean("isBroad", isBroad);
        resultHandler.sendMessage(message);
    }

    //结果提示
    private void sendResultMessage(int similar, Float max) {
        Message message = Message.obtain();
        message.what = 3;
        message.obj = similar;
        Bundle bundle = new Bundle();
        bundle.putFloat("temper", max);
        message.setData(bundle);
        resultHandler.sendMessage(message);
    }

    //清除UI显示
    private void sendClearUIMessage(long time) {
        resultHandler.removeMessages(4);
        resultHandler.sendEmptyMessageDelayed(4, time);
    }

    //实时更新体温测量值
    private void sendUpdateRealTimeTemper(float temper) {
        resultHandler.removeMessages(6);
        Message message = Message.obtain();
        message.what = 5;
        message.obj = temper;
        resultHandler.sendMessage(message);
    }

    //清除实时更新体温测量值
    private void sendClearRealTimeTemper() {
        resultHandler.removeMessages(6);
        resultHandler.sendEmptyMessageDelayed(6, 2000);
    }

    //显示扫码获取的用户信息
    private void sendUserInfoByCode(UserInfo userInfo) {
        if (viewInterface != null) {
            viewInterface.getUserInfoByCode(userInfo);
        }
    }

    private void sendCodeTemperResult(float mTemper) {
        Message message = Message.obtain();
        message.what = 7;
        message.obj = mTemper;
        resultHandler.sendMessage(message);
    }


    /*==扫码相关=======================================================================================*/
    /*==扫码相关=======================================================================================*/
    /*==扫码相关=======================================================================================*/
    /*==扫码相关=======================================================================================*/
    /*==扫码相关=======================================================================================*/
    /* *
     * 读卡器初始化
     */
    private ScanKeyManager scanKeyManager;

    private void initScanQrCodeReader() {
        //读卡器声明
        scanKeyManager = new ScanKeyManager(value -> {
            d("onScanValue: 检测到扫码：" + value);
            if (icCardMode) {
                d("检测到IC卡，清除UI");
                sendClearUIMessage(0);
                KDXFSpeechManager.instance().playDingDong();
                if (TextUtils.isEmpty(value)) {
                    sendTipMessage(getResString(R.string.act_certificates_card_read_failed), true);
                    return;
                }
                getUserInfoByICCard(value);
            } else {
                d("贴卡，清除UI");
                sendClearUIMessage(0);
                getUserInfoByCode(value);
            }
        });
        onKeyBoardListener();
    }

    private void getUserInfoByICCard(String cardNum) {
        CertificatesUser user = DaoManager.get().queryCertiUserByCardNum(cardNum);
        if(user == null){
            UIUtils.showShort(this,getResString(R.string.act_certificates_no_this_person));
            return;
        }
        idCardMsg = new IdCardMsg();
        idCardMsg.id_num = user.getNum();
        idCardMsg.name = user.getName();
        idCardMsg.nation_str = user.getDepart();

        Bitmap bitmap = BitmapFactory.decodeFile(user.getHeadPath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        idCardMsg.ptoto = baos.toByteArray();

        sendCardInfoMessage(idCardMsg);
        startCompareThread();
    }

    private void getUserInfoByCode(String code) {
        d("地址:" + ResourceUpdate.GETUSERINFO_BY_CODE);
        d("参数：" + code);
        OkHttpUtils.post()
                .url(ResourceUpdate.GETUSERINFO_BY_CODE)
                .addParams("code", code)
                .build().execute(new StringCallback() {
            @Override
            public void onBefore(Request request, int id) {
                UIUtils.showNetLoading(BaseCertificatesActivity.this);
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                d("onError: " + (e == null ? "NULL" : e.getMessage()));
                UIUtils.showLong(BaseCertificatesActivity.this, getResString(R.string.act_certificates_get_info_failed) + "（ " + (e == null ? "NULL" : e.getMessage()) + "）");

                resultHandler.removeMessages(4);
                resultHandler.sendEmptyMessageDelayed(4, 1000);
            }

            @Override
            public void onResponse(String response, int id) {
                d("onResponse: 请求结果：" + response);
                UserInfo getUserInfo = new Gson().fromJson(response, UserInfo.class);
                if (getUserInfo.status != 1) {
                    UIUtils.showLong(BaseCertificatesActivity.this, getResString(R.string.act_certificates_get_info_failed) + "（ " + getUserInfo.message + "）");
                    return;
                }
                sendUserInfoByCode(getUserInfo);
                startCodeTemperThread();
            }

            @Override
            public void onAfter(int id) {
                UIUtils.dismissNetLoading();
            }
        });
    }

    private Thread codeTemperThread = null;
    private boolean isCode = false;
    private long mCodeWaitFaceTime = 0;
    private long mCodeWaitTemperTime = 0;

    private void startCodeTemperThread() {
        if (codeTemperThread != null && codeTemperThread.isAlive()) {
            d("对比线程正在运行");
        } else {
            isCode = true;
            codeTemperThread = new Thread(codeTemperRunnable);
            codeTemperThread.start();
        }
    }

    private void stopCodeTemperThread() {
        if (isCode) {
            isCode = false;
        }
    }

    private Runnable codeTemperRunnable = () -> {
        while (isCode) {
            d("扫码测温");
            if (!mHasFace) {
                d("无人脸");
                long currMillis = System.currentTimeMillis();
                if (mCodeWaitFaceTime == 0) {
                    mCodeWaitFaceTime = currMillis;
                } else if (currMillis - mCodeWaitFaceTime > 5000) {
                    mCodeWaitFaceTime = 0;
                    sendClearUIMessage(5000);
                    sendTipMessage(getResString(R.string.act_certificates_check_outtime_scan_code), true);
                    stopCodeTemperThread();
                    break;
                }
                continue;
            } else {
                mCodeWaitFaceTime = 0;
            }

            if (mDetectTemperList.size() < 6) {
                d("温度不够");
                long currMillis = System.currentTimeMillis();
                if (mCodeWaitTemperTime == 0) {
                    mCodeWaitTemperTime = currMillis;
                } else if (currMillis - mCodeWaitTemperTime > 5000) {
                    mCodeWaitTemperTime = 0;
                    sendClearUIMessage(5000);
                    sendTipMessage(getResString(R.string.act_certificates_temper_outtime_scan_code), true);
                    stopCodeTemperThread();
                    break;
                }
                continue;
            } else {
                mCodeWaitTemperTime = 0;
            }

            Float finalTemper = getMean(mDetectTemperList);
//            Float finalTemper = Collections.max(mDetectTemperList);
            d("测温完毕：" + finalTemper);

            sendCodeTemperResult(finalTemper);
            sendClearUIMessage(8000);

            mDetectTemperList.clear();
            stopCodeTemperThread();
        }
    };

    class UserInfo {
        int status;
        String entryId;
        String message;
        String color;
        String name;
        String dept;
        String head;
    }

    private void closeCardReader() {
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() != KeyEvent.KEYCODE_BACK && !isInput) {
            scanKeyManager.analysisKeyEvent(event);
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    private boolean isInput = false;

    //监听软件盘是否弹起
    private void onKeyBoardListener() {
        SoftKeyBoardListener.setListener(this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                Log.e("软键盘", "键盘显示 高度" + height);
                isInput = true;
            }

            @Override
            public void keyBoardHide(int height) {
                Log.e("软键盘", "键盘隐藏 高度" + height);
                isInput = false;
            }
        });
    }
}
