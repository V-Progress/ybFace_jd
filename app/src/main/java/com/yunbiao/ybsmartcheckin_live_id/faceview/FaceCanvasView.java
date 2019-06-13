package com.yunbiao.ybsmartcheckin_live_id.faceview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.jdjr.risk.face.local.detect.BaseProperty;
import com.jdjr.risk.face.local.extract.FaceProperty;
import com.jdjr.risk.face.local.verify.VerifyResult;
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

    private Paint mNamePaint;
    private Paint mRectPaint;
    public Rect mOverRect;
    private int flgPortrait = 0;//人脸框方向
    private Lock lockFace = new ReentrantLock();
    private StringBuilder contentText = new StringBuilder();

//    private Bitmap scanBitmap;//人脸框
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
        // 识别名
        mNamePaint = new Paint();
        mNamePaint.setColor(Color.WHITE);
        mNamePaint.setTextSize(26);
        mNamePaint.setStyle(Paint.Style.FILL);

        mRectPaint = new Paint();
        mRectPaint.setAntiAlias(false);
        mRectPaint.setColor(Color.WHITE);
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setStrokeWidth(2);
        mRectPaint.setTextSize(25);
        lockFace.unlock();

//        scanBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.scan);
    }

    public void setOverlayRect(int left, int right, int top, int bottom, int camWidth, int camHeight) {
        mOverRect = new Rect(left, top, right, bottom);
    }

    private boolean isShowProperty = true;
    public void showProperty(boolean isShow){
        isShowProperty = isShow;
    }

    private HashMap<Long, FaceResult> mFaceMap;
    public void updateFaceBoxes(HashMap<Long, FaceResult> faceResults) {
        mFaceMap = faceResults;
        invalidate();
    }

    public void clearFaceFrame(){
        mFaceMap = null;
        invalidate();
    }

    private boolean verifyTag = false;
    public void setTag(boolean isVerifyed){
        verifyTag = isVerifyed;
    }

    @Override
    public void onDraw(Canvas canvas) {
        drawFaceResult(canvas);
    }

    private void drawFaceResult(Canvas canvas){
        if (mFaceMap == null || mFaceMap.size() == 0 || mFaceMap.values() == null || mFaceMap.values().size() == 0) {
            return;
        }

        for (FaceResult value : mFaceMap.values()) {
            BaseProperty face = value.getBaseProperty();
            FaceProperty faceProperty = value.getFaceProperty();
            VerifyResult verifyResult = value.getVerifyResult();
            Rect faceRect = face.getFaceRect();
            canvas.drawRect(faceRect,mRectPaint);

//            int width = faceRect.right - faceRect.left;
//            int height = faceRect.bottom - faceRect.top;
//            Bitmap newBmp = Bitmap.createScaledBitmap(scanBitmap, width, height, true);
//            canvas.drawBitmap(newBmp, faceRect.left, faceRect.top, null);

            getText(faceProperty,verifyResult);
            canvas.drawText(contentText.toString(), faceRect.left, faceRect.top - 20, mNamePaint);
        }
    }

    private void getText(FaceProperty faceProperty,VerifyResult verifyResult){
        contentText.setLength(0);
        if(verifyResult == null || verifyResult.getUser() == null){
            return;
        }
        String userId = verifyResult.getUser().getUserId();
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
        } else {
            if(faceProperty == null){
                return;
            }
            contentText.append(faceProperty.getGender() == 0 ? "女士" : faceProperty.getGender() == 1 ? "男士" : "未知").append(", ")
                    .append("年龄:").append(faceProperty.getAge());
        }
    }
}