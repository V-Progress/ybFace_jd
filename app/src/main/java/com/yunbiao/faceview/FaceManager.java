package com.yunbiao.faceview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.arcsoft.imageutil.ArcSoftRotateDegree;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FaceManager {
    private static final String TAG = "FaceManager";
    public static final String IMG_SUFFIX = ".jpg";
    private static FaceManager faceManager = new FaceManager();
    private FaceEngine compareEngin;
    private List<FaceRegisterInfo> faceRegisterInfos = new ArrayList<>();
    private int MAX_FACE_NUM = 1000;

    private String FEATURES_PATH;

    public static FaceManager getInstance() {
        return faceManager;
    }

    private FaceManager() {

    }

    public void init(Context context) {
        FEATURES_PATH = Constants.FEATURE_PATH;

        compareEngin = new FaceEngine();
        int compareInitCode = compareEngin.init(context, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY, 16, 1, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT);
//        isInited = compareInitCode == ErrorInfo.MOK;
        reloadRegisterList();
    }

    public Map<String, File> getVisitorList() {
        File file = new File(FEATURES_PATH);
        File[] files = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("v");
            }
        });
        Map<String, File> fileMap = new HashMap<>();
        for (File file1 : files) {
            fileMap.put(file1.getName(), file1);
        }
        return fileMap;
    }

    public Map<String, File> getAllFaceMap() {
        File file = new File(FEATURES_PATH);
        File[] files = file.listFiles();
        Map<String, File> fileMap = new HashMap<>();
        for (File file1 : files) {
            fileMap.put(file1.getName(), file1);
        }
        return fileMap;
    }

    public int getTotalSize() {
        return faceRegisterInfos.size();
    }

    public int getMAX_FACE_NUM() {
        return MAX_FACE_NUM;
    }

    public void setMAX_FACE_NUM(int MAX_FACE_NUM) {
        this.MAX_FACE_NUM = MAX_FACE_NUM;
    }

    public void reloadRegisterList() {
        long start = System.currentTimeMillis();

        faceRegisterInfos.clear();

        File featuresDir = new File(FEATURES_PATH);
        if (featuresDir == null || !featuresDir.exists()) {
            featuresDir.mkdirs();
        }

        File[] featureFiles = featuresDir.listFiles();
        if (featureFiles == null || featureFiles.length == 0) {
            return;
        }

        List<File> fileList = Arrays.asList(featureFiles);
        if (fileList.size() >= MAX_FACE_NUM) {
            //清除逻辑(先把数据翻转，然后挨个删除，直到小鱼当前设置的最大值)
            Collections.reverse(fileList);
            //如果特征文件数量大于最大数量
            Iterator<File> iterator = fileList.iterator();
            while (fileList.size() >= MAX_FACE_NUM) {
                iterator.remove();
            }
        }

        int allFeatures = fileList.size();
        for (int i = 0; i < allFeatures; i++) {
            FileInputStream fis = null;
            try {
                File featureFile = fileList.get(i);
                fis = new FileInputStream(featureFile);
                byte[] feature = new byte[FaceFeature.FEATURE_SIZE];
                fis.read(feature);
                faceRegisterInfos.add(new FaceRegisterInfo(feature, featureFile.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (Exception e) {

                    }
                }
            }
        }
        long end = System.currentTimeMillis();

        Log.e(TAG, "reloadRegisterList: 加载特征库耗时：" + (end - start));
        Log.e(TAG, "reloadRegisterList: 特征库数据总数：" + faceRegisterInfos.size());
    }

    public void removeAllFace() {
        File file = new File(FEATURES_PATH);
        if (file.exists()) {
            for (File listFile : file.listFiles()) {
                listFile.delete();
            }
        }
    }

    public boolean checkFace(String faceId) {
        File featureFile = new File(FEATURES_PATH + faceId);
        return featureFile != null && featureFile.exists();
    }

    public boolean addUser(final String userId, String imageFile) {
        if (faceRegisterInfos.size() >= MAX_FACE_NUM) {
            return false;
        }

        try {
            File file = new File(imageFile);
            if (file == null || !file.exists()) {
                return false;
            }
            //转换成bitmap
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile);
            if(bitmap == null){
                return false;
            }
            //裁剪图片为合适的尺寸
            bitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);
            if(bitmap == null){
                return false;
            }
            //创建等同于bitmap大小的byte[]
            byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
            //为byte[]赋值
            int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
            //释放bitmap
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
/*            if (!bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null;
            }*/
            if (transformCode == ArcSoftImageUtilError.CODE_SUCCESS) {
                return registerBgr24(userId, bgr24, width, height);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean removeUser(String userId) {
        boolean deleFeature = false;
        File featureFile = new File(FEATURES_PATH, userId);
        if (featureFile != null) {
            if (featureFile.exists()) {
                deleFeature = featureFile.delete();
            } else {
                deleFeature = true;
            }
        }
        return deleFeature && deleFeature;
    }

    /**
     * 用于注册照片人脸
     *
     * @param bgr24    bgr24数据
     * @param width    bgr24宽度
     * @param height   bgr24高度
     * @param fileName 保存的名字，作为唯一ID使用
     * @return 是否注册成功
     */
    public boolean registerBgr24(@NonNull String fileName, byte[] bgr24, int width, int height) {
        synchronized (this) {
            //人脸检测
            List<FaceInfo> faceInfoList = new ArrayList<>();
            int code = compareEngin.detectFaces(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList);
            if (code == ErrorInfo.MOK && faceInfoList.size() > 0) {
                FaceInfo faceInfo = faceInfoList.get(0);
                FaceFeature faceFeature = new FaceFeature();
                //特征提取
                code = compareEngin.extractFaceFeature(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfo, faceFeature);
                if (code == ErrorInfo.MOK) {
                    try {
                        File toFile = new File(FEATURES_PATH + File.separator + fileName);
                        FileUtils.writeByteArrayToFile(toFile, faceFeature.getFeatureData());
                        faceRegisterInfos.add(new FaceRegisterInfo(faceFeature.getFeatureData(), fileName));
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                } else {
                    Log.e(TAG, "特征提取失败, code is " + code);
                    return false;
                }
            } else {
                Log.e(TAG, "registerBgr24: no face detected, code is " + code);
                return false;
            }
        }
    }

    /**
     * 截取合适的头像并旋转，保存为注册头像
     *
     * @param originImageData 原始的BGR24数据
     * @param width           BGR24图像宽度
     * @param height          BGR24图像高度
     * @param orient          人脸角度
     * @param cropRect        裁剪的位置
     * @param imageFormat     图像格式
     * @return 头像的图像数据
     */
    public Bitmap getHeadImage(byte[] originImageData, int width, int height, int orient, Rect cropRect, ArcSoftImageFormat imageFormat) {
        byte[] headImageData = ArcSoftImageUtil.createImageData(cropRect.width(), cropRect.height(), imageFormat);
        int cropCode = ArcSoftImageUtil.cropImage(originImageData, headImageData, width, height, cropRect, imageFormat);
        if (cropCode != ArcSoftImageUtilError.CODE_SUCCESS) {
            throw new RuntimeException("crop image failed, code is " + cropCode);
        }

        //判断人脸旋转角度，若不为0度则旋转注册图
        byte[] rotateHeadImageData = null;
        int rotateCode;
        int cropImageWidth;
        int cropImageHeight;
        // 90度或270度的情况，需要宽高互换
        if (orient == FaceEngine.ASF_OC_90 || orient == FaceEngine.ASF_OC_270) {
            cropImageWidth = cropRect.height();
            cropImageHeight = cropRect.width();
        } else {
            cropImageWidth = cropRect.width();
            cropImageHeight = cropRect.height();
        }
        ArcSoftRotateDegree rotateDegree = null;
        switch (orient) {
            case FaceEngine.ASF_OC_90:
                rotateDegree = ArcSoftRotateDegree.DEGREE_270;
                break;
            case FaceEngine.ASF_OC_180:
                rotateDegree = ArcSoftRotateDegree.DEGREE_180;
                break;
            case FaceEngine.ASF_OC_270:
                rotateDegree = ArcSoftRotateDegree.DEGREE_90;
                break;
            case FaceEngine.ASF_OC_0:
            default:
                rotateHeadImageData = headImageData;
                break;
        }
        // 非0度的情况，旋转图像
        if (rotateDegree != null) {
            rotateHeadImageData = new byte[headImageData.length];
            rotateCode = ArcSoftImageUtil.rotateImage(headImageData, rotateHeadImageData, cropRect.width(), cropRect.height(), rotateDegree, imageFormat);
            if (rotateCode != ArcSoftImageUtilError.CODE_SUCCESS) {
                throw new RuntimeException("rotate image failed, code is " + rotateCode);
            }
        }
        // 将创建一个Bitmap，并将图像数据存放到Bitmap中
        Bitmap headBmp = Bitmap.createBitmap(cropImageWidth, cropImageHeight, Bitmap.Config.RGB_565);
        if (ArcSoftImageUtil.imageDataToBitmap(rotateHeadImageData, headBmp, imageFormat) != ArcSoftImageUtilError.CODE_SUCCESS) {
            throw new RuntimeException("failed to transform image data to bitmap");
        }
        return headBmp;
    }

    /***
     * 对比方法
     * @param faceFeature
     * @return
     */
    public CompareResult compare(final FaceFeature faceFeature) {
        if (compareEngin == null) {
            return null;
        }

        FaceFeature tempFaceFeature = new FaceFeature();
        FaceSimilar faceSimilar = new FaceSimilar();
        float maxSimilar = 0;
        int maxSimilarIndex = -1;
        int faceSize = faceRegisterInfos.size();
        for (int i = 0; i < faceSize; i++) {
            tempFaceFeature.setFeatureData(faceRegisterInfos.get(i).getFeatureData());
            compareEngin.compareFaceFeature(faceFeature, tempFaceFeature, faceSimilar);
            if (faceSimilar.getScore() > maxSimilar) {//找到了人脸
                maxSimilar = faceSimilar.getScore();
                maxSimilarIndex = i;
            }
            /*if(maxSimilar >= 70f){
                break;
            }*/
        }

        CompareResult compareResult = null;
        if (maxSimilarIndex != -1) {
            String name = faceRegisterInfos.get(maxSimilarIndex).getName();
            compareResult = new CompareResult(name.split("~")[0], maxSimilar);
        }
        return compareResult;
    }

    class FaceRegisterInfo {
        private byte[] featureData;
        private String name;

        public FaceRegisterInfo(byte[] featureData, String name) {
            this.featureData = featureData;
            this.name = name;
        }

        public byte[] getFeatureData() {
            return featureData;
        }

        public void setFeatureData(byte[] featureData) {
            this.featureData = featureData;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * 将图像中需要截取的Rect向外扩张一倍，若扩张一倍会溢出，则扩张到边界，若Rect已溢出，则收缩到边界
     *
     * @param width   图像宽度
     * @param height  图像高度
     * @param srcRect 原Rect
     * @return 调整后的Rect
     */
    public static Rect getBestRect(int width, int height, Rect srcRect) {
        if (srcRect == null) {
            return null;
        }
        Rect rect = new Rect(srcRect);

        // 原rect边界已溢出宽高的情况
        int maxOverFlow = Math.max(-rect.left, Math.max(-rect.top, Math.max(rect.right - width, rect.bottom - height)));
        if (maxOverFlow >= 0) {
            rect.inset(maxOverFlow, maxOverFlow);
            return rect;
        }

        // 原rect边界未溢出宽高的情况
        int padding = rect.height() / 2;

        // 若以此padding扩张rect会溢出，取最大padding为四个边距的最小值
        if (!(rect.left - padding > 0 && rect.right + padding < width && rect.top - padding > 0 && rect.bottom + padding < height)) {
            padding = Math.min(Math.min(Math.min(rect.left, width - rect.right), height - rect.bottom), rect.top);
        }
        rect.inset(-padding, -padding);
        return rect;
    }
}
