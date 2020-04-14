package com.yunbiao.ybsmartcheckin_live_id.activity_certificates;

import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.intelligence.hardware.temperature.TemperatureModule;
import com.intelligence.hardware.temperature.callback.HotImageK1604CallBack;
import com.intelligence.hardware.temperature.callback.HotImageK3232CallBack;
import com.yunbiao.faceview.CertificatesView;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseGpioActivity;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.utils.IDCardReader;
import com.yunbiao.ybsmartcheckin_live_id.utils.IdCardMsg;
import com.yunbiao.ybsmartcheckin_live_id.utils.NetWorkChangReceiver;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    @Override
    protected void initData() {
        super.initData();
        KDXFSpeechManager.instance().init(this);
        IDCardReader.getInstance().startReaderThread(this, readListener);
        registerNetState();
    }

    protected void setCertificatesView(CertificatesViewInterface viewInterface) {
        this.viewInterface = viewInterface;
        if (this.viewInterface == null) {
            this.viewInterface = new CertificatesViewImpl();
        }
    }

    protected void setFaceCallback(CertificatesView certificatesView) {
        mCertificatesView = certificatesView;
        mCertificatesView.setCallback(this);
    }

    protected void startNoCardToTemper() {

    }

    @Override
    protected void onResume() {
        super.onResume();

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
    }

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
        KDXFSpeechManager.instance().destroy();
    }

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
            handleTemper(bitmap, sensorT, afterT);
        }
    };
    private HotImageK1604CallBack hotImageK1604CallBack = new HotImageK1604CallBack() {
        @Override
        public void newestHotImageData(Bitmap bitmap, final float originalMaxT, final float afterF, final float minT) {
            handleTemper(bitmap, originalMaxT, afterF);
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
            if (mDetectTemperList.size() > 6) {
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

    //证件收集
    private IDCardReader.ReadListener readListener = new IDCardReader.ReadListener() {
        @Override
        public void getCardInfo(IdCardMsg msg) {
            KDXFSpeechManager.instance().playDingDong();
            d("贴卡，清除UI");
            sendClearUIMessage();
            if (msg == null || msg.name == null || msg.ptoto == null) {
                sendTipMessage("证件读取失败，请重新贴卡", true);
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
    private List<FaceFeature> faceFeatureList = new ArrayList<>();//特征缓存集合

    private void startCompareThread() {
        if (compareThread != null && compareThread.isAlive()) {
            d("对比线程正在运行");
        } else {
            sendTipMessage("正在验证，请正视摄像头", true);
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
                    sendClearUIMessage();
                    sendTipMessage("检测超时，请重新贴卡", true);
                    stopCompareThread();
                    break;//检测超时重新贴卡
                }
                continue;//没有人脸，继续检测
            } else {
                waitFaceTime = 0;
            }

            //等待测温
            if (mDetectTemperList.size() < 6) {
                long l = System.currentTimeMillis();
                if (waitTemperTime == 0) {
                    waitFaceTime = l;
                } else if (l - waitTemperTime > 5000) {
                    d("测温超时");
                    waitTemperTime = 0;
                    sendClearUIMessage();
                    sendTipMessage("测温失败，请重新贴卡", true);
                    stopCompareThread();
                    break;//测温超时，重新贴卡
                }
                continue;//测温数据不足，继续检测
            } else {
                waitFaceTime = 0;
            }

            //等待特征
            if (faceFeatureList.size() < 3) {
                FaceFeature faceFeature = mCertificatesView.getFaceFeature();
                if (faceFeature == null) {
                    //如果连续取特征失败超过五次则结束
                    if (waitFaceFeatureTimes >= 5) {
                        d("提取特征失败");
                        sendClearUIMessage();
                        sendTipMessage("特征提取超时，请重新贴卡", true);
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
            byte[] bytes = IDCardReader.getInstance().decodeToBitmap(idCardMsg.ptoto);
            Bitmap cardBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            FaceFeature idCardFeature = mCertificatesView.inputIdCard(cardBitmap);

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
            faceFeatureList.clear();
            int similar = (int) (finalSimilar * 100);
            Float max = Collections.max(mDetectTemperList);
            mDetectTemperList.clear();
            d("对比出结果：" + similar);
            sendResultMessage(similar, max);
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
                    viewInterface.updateHotImage(bitmap, temper);
                }
                break;
            case 1://更新卡片信息
                IdCardMsg idCardMsg = (IdCardMsg) msg.obj;
                if (idCardMsg != null) {
                    byte[] bytes = IDCardReader.getInstance().decodeToBitmap(idCardMsg.ptoto);
                    Bitmap mIdCardBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (viewInterface != null) {
                        viewInterface.updateIdCardInfo(idCardMsg, mIdCardBitmap);
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
                    KDXFSpeechManager.instance().playNormal(tip);
                }
                break;
            case 3://结果提示
                int similarInt = (int) msg.obj;
                float finalTemper = msg.getData().getFloat("temper");
                boolean isAlike = similarInt >= mSimilar;
                boolean isNormal = finalTemper > mMinThreshold && finalTemper < mWarningThreshold;

                StringBuffer resultBuffer = new StringBuffer();
                String speech = "";
                if (isAlike) {
                    resultBuffer.append("<font color='#00ff00'>验证通过</font>");//绿色字体
                    speech += "验证通过";
                } else {
                    resultBuffer.append("<font color='#ff0000'>人证不匹配</font>");//红字
                    speech += "人证不匹配";
                }
                resultBuffer.append("<font color='#ffffff'>，</font>");//白字
                if (isNormal) {
                    resultBuffer.append("<font color='#00ff00'>体温正常</font>");//绿字
                    speech += "，体温正常";
                } else {
                    resultBuffer.append("<font color='#ff0000'>体温异常</font>");//红字
                    speech += "，体温异常";
                }
                resultBuffer.append("<font color='#ffffff'>，</font>");//白字
                if (isAlike && isNormal) {
                    resultBuffer.append("<font color='#00ff00'>请通行</font>");//绿字
                    speech += "，请通行";
                } else {
                    resultBuffer.append("<font color='#ff0000'>禁止通行</font>");//红字
                    speech += "，禁止通行";
                }
                KDXFSpeechManager.instance().stopNormal();
                KDXFSpeechManager.instance().playNormal(speech);
                if (viewInterface != null) {

                    viewInterface.updateResultTip(resultBuffer.toString(), BaseCertificatesActivity.this.idCardMsg, finalTemper, similarInt, isAlike, isNormal);
                }
                if (isAlike && isNormal) {
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
                if (viewInterface != null) {
                    viewInterface.updateRealTimeTemper(temperature, isTempNormal);
                }
                break;
            case 6:
                if (viewInterface != null) {
                    viewInterface.clearRealTimeTemper();
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
    private void sendClearUIMessage() {
        resultHandler.removeMessages(4);
        resultHandler.sendEmptyMessageDelayed(4, 8000);
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
}
