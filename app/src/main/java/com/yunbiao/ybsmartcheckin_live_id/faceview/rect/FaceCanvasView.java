package com.yunbiao.ybsmartcheckin_live_id.faceview.rect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.jdjr.risk.face.local.detect.BaseProperty;
import com.jdjr.risk.face.local.extract.FaceProperty;
import com.jdjr.risk.face.local.verify.VerifyResult;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.faceview.face_new.FaceResult;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressLint("AppCompatCustomView")
public class FaceCanvasView extends ImageView {
    private static final String TAG = "FaceCanvasView";

    private Paint mNamePaint;
    private Paint mRectPaint;
    private Lock lockFace = new ReentrantLock();
    private StringBuilder contentText = new StringBuilder();

    private Map<Integer,String> cacheMap = new HashMap<>();//名称缓存
    private Bitmap scanBitmap;

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



        scanBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.face_rect);
    }

    private boolean isShowProperty = true;
    public void showProperty(boolean isShow){
        isShowProperty = isShow;
    }

    private HashMap<Long, FaceResult> mFaceMap;
    public void updateFaceBoxes(HashMap<Long, FaceResult> faceResults) {
        mFaceMap = faceResults;
        postInvalidate();
    }

    public void clearFaceFrame(){
        mFaceMap = null;
        postInvalidate();
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
            if(face == null){
                return;
            }
//            Rect faceRect = face.getFaceRect();
            RectF faceRect = FaceBoxUtil.getRect(face.getFaceRect());
            int width = (int) (faceRect.right - faceRect.left);
            int height = (int) (faceRect.bottom - faceRect.top);
            Bitmap newBmp = Bitmap.createScaledBitmap(scanBitmap, width, height, true);
            canvas.drawBitmap(newBmp, faceRect.left, faceRect.top, null);


//            FaceProperty faceProperty = value.getFaceProperty();
//            VerifyResult verifyResult = value.getVerifyResult();
//            canvas.drawRect(faceRect,mRectPaint);
//            getText(faceProperty,verifyResult);
//            canvas.drawText(contentText.toString(), faceRect.left, faceRect.top - 20, mNamePaint);
        }
    }

    private void getText(FaceProperty faceProperty, VerifyResult verifyResult){
        contentText.setLength(0);
        if (!isShowProperty) {
            if(verifyResult == null || verifyResult.getUser() == null){
                return;
            }
            String userId = verifyResult.getUser().getUserId();
            if(!TextUtils.isEmpty(userId)){
                Integer integer = Integer.valueOf(userId);
//                if (!cacheMap.containsKey(integer)) {//如果缓存里不存在就去查
//                    List<UserBean> userBeans = APP.getUserDao().queryByFaceId(integer);
//                    if(userBeans != null && userBeans.size() > 0){
//                        String name = userBeans.get(0).getName();
//                        cacheMap.put(integer,name);
//                        contentText.append(name);
//                    }
//                } else {//存在就取缓存
//                    contentText.append(cacheMap.get(integer));
//                }
            }
        } else {
            if(faceProperty != null){
                contentText.append(faceProperty.getGender() == 0 ? "女士" : faceProperty.getGender() == 1 ? "男士" : "未知");
//                        .append(", ").append(faceProperty.getAge());
            }
        }
    }
}