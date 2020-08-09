package com.yunbiao.ybsmartcheckin_live_id.activity_certificates;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceSimilar;

import timber.log.Timber;

public class Verifier extends AsyncTask<Void,Void, Verifier.CompareResult> {
    private static Verifier verifier;

    //等待人脸的时间
    private final long WAIT_FACE_TIME = 5000;
    private long waitFaceTime = 0;
    //等待温度的时间
    private final long WAIT_TEMPERATURE_TIME = 5000;
    private long waitTemperatureTime = 0;

    //提取特征失败次数
    private final int WAIT_FEATURE_TIME = 5;
    //相似度阈值
    private float SIMILAR = 0.6F;

    private VerifyCallback verifyCallback;

    private Verifier(VerifyCallback verifyCallback) {
        this.verifyCallback = verifyCallback;
        waitFaceTime = 0;
        waitTemperatureTime = 0;
    }

    public static synchronized void startCompare(@NonNull VerifyCallback verifyCallback){
        if(verifier != null && !verifier.isCancelled()){
            Timber.d("准备结束对比流程：" + verifier.hashCode());
            verifier.clearCallback();
            verifier.cancel(true);
            verifier = null;
        }

        verifier = new Verifier(verifyCallback);
        verifier.execute();
        Timber.d("创建新的对比流程：" + verifier.hashCode());
    }

    private void clearCallback(){
        verifyCallback = null;
    }

    @Override
    protected void onPreExecute() {
        if(verifyCallback != null){
            verifyCallback.onStart();
        }
    }

    @Override
    protected CompareResult doInBackground(Void... voids) {
        while (!verifier.isCancelled()){
            boolean hasFace = false;
            if(verifyCallback != null){
                hasFace = verifyCallback.hasFace();
            }
            //人脸检测流程
            if (!hasFace) {
                waitTemperatureTime = 0;
                long currentTimeMillis = System.currentTimeMillis();
                if(waitFaceTime == 0){
                    waitFaceTime = currentTimeMillis;
                } else if(currentTimeMillis - waitFaceTime > WAIT_FACE_TIME){
                    waitFaceTime = 0;
                    return new CompareResult(-1);
                }
                continue;
            }
            waitFaceTime = 0;//在有人脸的时候归零，以下步骤人离开后可以重置判断规则

            //温度检测流程
            float temperature = 0.0f;
            if(verifyCallback != null){
                temperature = verifyCallback.getTemperature();
            }
            if(temperature == 0.0f){
                long currentTimeMillis = System.currentTimeMillis();
                if(waitTemperatureTime == 0){
                    waitTemperatureTime = currentTimeMillis;
                } else if(currentTimeMillis - waitTemperatureTime > WAIT_TEMPERATURE_TIME){
                    waitTemperatureTime = 0;
                    return new CompareResult(-2);
                }
                continue;
            }
            waitTemperatureTime = 0;

            //提取卡片特征
            FaceFeature idCardFeature = null;
            for (int i = 0; i < WAIT_FEATURE_TIME; i++) {
                if(isCancelled()){
                    return null;
                }
                if(verifyCallback != null){
                    idCardFeature = verifyCallback.getIdCardFeature();
                }
                if (idCardFeature != null) {
                    break;
                }
            }
            //如果此时特征依然为空则判定失败
            if(idCardFeature == null){
                return new CompareResult(-3);
            }

            //提取人脸特征+对比，每提取一次就对比，如果当时分数高于阈值，则判定立即通过
            FaceSimilar faceSimilar = null;
            for (int i = 0; i < WAIT_FEATURE_TIME; i++) {
//                if(isCancelled()){
//                    return null;
//                }
                Timber.d("当前是第：" + i);
                FaceFeature faceFeature = null;
                if(verifyCallback != null){
                    faceFeature = verifyCallback.getFaceFeature();
                }
                Timber.d("提取特征完毕");
                if(faceFeature == null){
                    Timber.d("人脸特征为空");
                    continue;
                }

                //如果对比结果不为空且分数高于缓存分数，则赋值
                FaceSimilar compare = null;
                if(verifyCallback != null){
                    compare = verifyCallback.compare(idCardFeature, faceFeature);
                    Timber.d("开始进行对比");
                    if(faceSimilar == null || (compare != null && compare.getScore() > faceSimilar.getScore())){
                        faceSimilar = compare;
                    }
                    Timber.d("对比结果：%s", (faceSimilar == null ? "NULL" : faceSimilar.getScore()));
                }
                //如果此时对比结果不为空且分数大于阈值则立即结束此流程
                if(faceSimilar != null && faceSimilar.getScore() >= SIMILAR){
                    faceSimilar = compare;
                    Timber.d("对比结束：" + faceSimilar.getScore());
                    break;
                }
            }
            //如果此时对比结果依然为空则判定为提取人脸特征失败
            if(faceSimilar == null){
                Timber.d("对比结果为空");
                return new CompareResult(-4);
            }

            //对比结束，生成对比结果
            CompareResult compareResult = new CompareResult(faceSimilar.getScore() >= SIMILAR ? 1 : 0);
            compareResult.setSimilar(faceSimilar.getScore());
            compareResult.setTemperature(temperature);
            return compareResult;
        }

        return new CompareResult(0);
    }

    @Override
    protected void onPostExecute(CompareResult compareResult) {
        if(compareResult == null){
            return;
        }
        if(verifyCallback != null){
            verifyCallback.result(compareResult);
        }
        cancel(true);
    }

    public interface VerifyCallback{
        //开始
        void onStart();

        //判断是否有人脸
        boolean hasFace();

        //提取温度
        float getTemperature();

        //提取卡片特征
        FaceFeature getIdCardFeature();

        //提取人脸特征
        FaceFeature getFaceFeature();

        //对比
        FaceSimilar compare(FaceFeature idCardFeature, FaceFeature faceFeature);

        //结果回调
        void result(CompareResult compareResult);
    }

    public static class CompareResult{
        private int resultCode;//1验证通过，0未验证通过，-1等待人脸超时，-2等待测温超时，-3提取卡片特征超时，-4提取人脸特征失败，-111未知错误
        private float similar;
        private float temperature;

        public CompareResult(int resultCode) {
            this.resultCode = resultCode;
        }

        public int getResultCode() {
            return resultCode;
        }

        public void setResultCode(int resultCode) {
            this.resultCode = resultCode;
        }

        public float getSimilar() {
            return similar;
        }

        public void setSimilar(float similar) {
            this.similar = similar;
        }

        public float getTemperature() {
            return temperature;
        }

        public void setTemperature(float temperature) {
            this.temperature = temperature;
        }

        @Override
        public String toString() {
            return "CompareResult{" +
                    "resultCode=" + resultCode +
                    ", similar=" + similar +
                    ", temperature=" + temperature +
                    '}';
        }
    }
}
