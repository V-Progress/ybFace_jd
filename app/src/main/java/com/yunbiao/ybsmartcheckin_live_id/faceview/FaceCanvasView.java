package com.yunbiao.ybsmartcheckin_live_id.faceview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.jdjr.risk.face.local.detect.BaseProperty;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.db.VIPDetail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressLint("AppCompatCustomView")
public class FaceCanvasView extends ImageView {

    private static final String TAG = "FaceCanvasView";
    private float mXRatio;
    private float mYRatio;

    private Paint mNamePaint;
    private RectF mDrawFaceRect = new RectF();
    public Rect mOverRect;
    private int mCameraWidth;//摄像头宽高
    private int mCameraHeight;
    private int flgPortrait = 0;//人脸框方向
    private Lock lockFace = new ReentrantLock();

    public double wOff = 1.0;//人脸框宽度缩放比例
    public double hOff = 1.0;//人脸框高度缩放比例

    private List<BaseProperty> mFaces;//人脸list
    private int scanWidth;//人脸框宽
    private int scanHeight;//人脸框高
    private Bitmap scanBitmap;//人脸框
    private Map<Integer,String> cacheMap = new HashMap<>();//名称缓存

    public FaceCanvasView(Context context) {
        super(context);
        reset();
    }

    public FaceCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        reset();
    }

    public FaceCanvasView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        reset();
    }

    public void setCavasPortrait() {
        flgPortrait = 1;
    }

    public void setCavasLandscape() {
        flgPortrait = 0;
    }

    public void setCavasReversePortrait() {
        flgPortrait = 3;
    }

    public void setCavasReverseLandscape() {
        flgPortrait = 2;
    }

    public void reset() {
        lockFace.lock();
        if (mFaces == null) {
            mFaces = new ArrayList<>();
        }
        mFaces.clear();

        mCameraWidth = 1;
        mCameraHeight = 1;
        // 识别名
        mNamePaint = new Paint();
        mNamePaint.setColor(Color.WHITE);
        mNamePaint.setTextSize(26);
        mNamePaint.setStyle(Paint.Style.FILL);
        lockFace.unlock();

        scanBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.scan);
    }

    public void setOverlayRect(int left, int right, int top, int bottom, int camWidth, int camHeight) {
        mOverRect = new Rect(left, top, right, bottom);

        double previewWidth = Double.valueOf(right - left);
        double previewHeight = Double.valueOf(bottom - top);

        wOff = previewWidth / camWidth;//预览窗口宽/摄像头宽计算出缩放比例
        hOff = previewHeight / camHeight;//预览窗口高/摄像头高计算出缩放比例

        //定义的摄像头宽高大于预览框过多，识别框变大
        mCameraWidth = camWidth;
        mCameraHeight = camHeight;

        mXRatio = (float) mOverRect.width() / (float) mCameraWidth;
        mYRatio = (float) mOverRect.height() / (float) mCameraHeight;
    }

    private boolean isShowProperty = true;
    public void showProperty(boolean isShow){
        isShowProperty = isShow;
    }

    public void updateFaceBoxes(List<BaseProperty> faces) {
        mFaces = faces;
        invalidate();
    }

//    private FaceView.CacheMap mFaceMap;
//    public void updateFaceBoxes(FaceView.CacheMap map){
//        mFaceMap = map;
//        invalidate();
//    }

    public void clearFaceFrame(){
        mFaces = null;
//        mFaceMap = null;
        invalidate();
    }

    private boolean verifyTag = false;
    public void setTag(boolean isVerifyed){
        verifyTag = isVerifyed;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawFaceResult(canvas);
//        drawFace(canvas);
    }

    private StringBuilder contentText = new StringBuilder();

//    private void drawFace(Canvas canvas){
//        // 清空画布
//        canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
//        lockFace.lock();
//
//        if(mFaceMap == null){
//            return;
//        }
//
//        for (Map.Entry<Long, FaceView.FaceBean> entry : mFaceMap.entrySet()) {
//            Long key = entry.getKey();
//            Log.e(TAG, "drawFace: -----" + key);
//
//            FaceView.FaceBean faceBean = entry.getValue();
//            String userId = faceBean.userId;
//            String sex = faceBean.sex;
//            String age = faceBean.age;
//
//            Rect faceRect = faceBean.faceRect;
//            if(faceRect == null){
//                continue;
//            }
//            int width = faceRect.right - faceRect.left;
//            int height = faceRect.bottom - faceRect.top;
//            Bitmap newBmp = Bitmap.createScaledBitmap(scanBitmap, width, height, true);
//            canvas.drawBitmap(newBmp, faceRect.left, faceRect.top, null);
//
//            contentText.setLength(0);
//            contentText.append(key).append("---");
//
//            if(!TextUtils.isEmpty(userId)){
//                Integer integer = Integer.valueOf(userId);
//                if (!cacheMap.containsKey(integer)) {//如果缓存里不存在就去查
//                    List<VIPDetail> vipDetails = SyncManager.instance().getUserDao().queryByFaceId(integer);
//                    if(vipDetails != null && vipDetails.size() > 0){
//                        String name = vipDetails.get(0).getName();
//                        cacheMap.put(integer,name);
//                        contentText.append(name);
//                    }
//                } else {//存在就取缓存
//                    contentText.append(cacheMap.get(integer));
//                }
//            } else {
//                contentText.append(sex).append(", ")
//                        .append("年龄:").append(age);
//            }
//            canvas.drawText(contentText.toString(), faceRect.left, faceRect.top - 20, mNamePaint);
//
//        }
//        lockFace.unlock();
//    }

    /**
     * 画人脸框：与人脸检测、注册、识别相关
     */
    private void drawFaceResult(Canvas canvas) {
        // 清空画布
        canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
        lockFace.lock();

        if(mFaces == null){
            return;
        }

        for (BaseProperty mFace : mFaces) {
            final Rect previewBox = mFace.getFaceRect();
//            canvas.drawRect(previewBox,mNamePaint);
            int width = previewBox.right - previewBox.left;
            int height = previewBox.bottom - previewBox.top;
            Bitmap newBmp = Bitmap.createScaledBitmap(scanBitmap, width, height, true);
            canvas.drawBitmap(newBmp, previewBox.left, previewBox.top, null);

//            setFaceFacingBack(mFace);
//            Bitmap newBmp = Bitmap.createScaledBitmap(scanBitmap, scanWidth, scanHeight, true);
//            int offset = (int) (scanWidth * 0.15);//计算出边角的偏移量，然后减去这部分距离再绘制
//            canvas.drawBitmap(newBmp, mDrawFaceRect.left - offset, mDrawFaceRect.top - offset, null);

            contentText.setLength(0);
            if(!isShowProperty){
                String userId = mFace.getUserId();
                if(!TextUtils.isEmpty(userId)){
                    Integer integer = Integer.valueOf(userId);
                    if (!cacheMap.containsKey(integer)) {//如果缓存里不存在就去查
                        List<VIPDetail> vipDetails = SyncManager.instance().getUserDao().queryByFaceId(integer);
                        if(vipDetails != null && vipDetails.size() > 0){
                            String name = vipDetails.get(0).getName();
                            cacheMap.put(integer,name);
                            contentText.append(name);
                        }
                    } else {//存在就取缓存
                        contentText.append(cacheMap.get(integer));
                    }
                }
            } else {
                contentText.append(mFace.getGender() == 0 ? "女士" : mFace.getGender() == 1 ? "男士" : "未知").append(", ")
                        .append("年龄:").append(mFace.getAge());
            }

            canvas.drawText(contentText.toString(), previewBox.left, previewBox.top - 20, mNamePaint);
//            canvas.drawText(contentText.toString(), mDrawFaceRect.left - offset, mDrawFaceRect.top - offset - 20, mNamePaint);
        }
        lockFace.unlock();
    }

//    private void checkCache(){
//        if(){}
//    }

    private boolean isMirror = true;

    private void setFaceFacingBack(BaseProperty face) {
        Rect faceRect = face.getFaceRect();
        //右-左 = 宽 乘以缩放比例
        scanWidth = (int) ((faceRect.right - faceRect.left) * (wOff + 0.2));
        scanHeight = (int) ((faceRect.bottom - faceRect.top) * (hOff + 0.2));

        if (flgPortrait == 0) {
            if(isMirror){
                mDrawFaceRect.left = mOverRect.left + (float) faceRect.left * mXRatio;
                mDrawFaceRect.right = mOverRect.left + (float) faceRect.right * mXRatio;
            } else {
                mDrawFaceRect.left = mOverRect.left + (float) (mCameraWidth - faceRect.right) * mXRatio;
                mDrawFaceRect.right = mOverRect.left + (float) (mCameraWidth - faceRect.left) * mXRatio;
            }
            mDrawFaceRect.top = mOverRect.top + (float) faceRect.top * mYRatio;
            mDrawFaceRect.bottom = mOverRect.top + (float) faceRect.bottom * mYRatio;
        } else if (flgPortrait == 1) {
            mDrawFaceRect.left = mOverRect.left + (float) (mCameraWidth - faceRect.bottom) * mXRatio;
            mDrawFaceRect.right = mOverRect.left + (float) (mCameraWidth - faceRect.top) * mXRatio;
            mDrawFaceRect.top = mOverRect.top + (float) faceRect.left * mYRatio;
            mDrawFaceRect.bottom = mOverRect.top + (float) faceRect.right * mYRatio;
        } else if (flgPortrait == 2) {
            mDrawFaceRect.left = mOverRect.left + (float) (mCameraWidth - faceRect.right) * mXRatio;
            mDrawFaceRect.right = mOverRect.left + (float) (mCameraWidth - faceRect.left) * mXRatio;
            mDrawFaceRect.top = mOverRect.top + (float) (mCameraHeight - faceRect.bottom) * mYRatio;
            mDrawFaceRect.bottom = mOverRect.top + (float) (mCameraHeight - faceRect.top) * mYRatio;
        } else if (flgPortrait == 3) {
            mDrawFaceRect.left = mOverRect.left + (float) (faceRect.top) * mXRatio;
            mDrawFaceRect.right = mOverRect.left + (float) (faceRect.bottom) * mXRatio;
            mDrawFaceRect.top = mOverRect.top + (float) (mCameraHeight - faceRect.right) * mYRatio;
            mDrawFaceRect.bottom = mOverRect.top + (float) (mCameraHeight - faceRect.left) * mYRatio;
        }
    }
}