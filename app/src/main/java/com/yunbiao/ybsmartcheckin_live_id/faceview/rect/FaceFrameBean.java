package com.yunbiao.ybsmartcheckin_live_id.faceview.rect;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;

import com.jdjr.risk.face.local.detect.BaseProperty;

import java.util.List;

public class FaceFrameBean {
    private Rect faceRect;
    private List<Point> landmarks;
    private int status = 0;//0正在识别，1识别成功，-1识别失败
    private int DIFF_VALUE_OFFSET = 3;//人脸框稳定值

    public void updateStatus(int s){
        status = s;
    }

    public FaceFrameBean(BaseProperty baseProperty) {
        faceRect = baseProperty.faceRect;
        landmarks = baseProperty.getLandmarks();
    }

    public void update(BaseProperty baseProperty) {
        Rect rect = baseProperty.faceRect;
        landmarks = baseProperty.getLandmarks();
        if (Math.abs(rect.left - faceRect.left) <= DIFF_VALUE_OFFSET ||
        Math.abs(rect.top - faceRect.top) <= DIFF_VALUE_OFFSET ||
        Math.abs(rect.right - faceRect.right) <= DIFF_VALUE_OFFSET ||
        Math.abs(rect.bottom - faceRect.bottom) <= DIFF_VALUE_OFFSET){
            return;
        }
        this.faceRect = rect;
    }

    private static final String TAG = "FaceFrameBean";
    public void draw(Long key, Bitmap scanFrame,Bitmap scanLine, Canvas canvas, Paint rectPaint, Paint linePaint, Paint textPaint, Paint pointPaint) {
        if (faceRect == null) {
            return;
        }
        RectF rect = FaceBoxUtil.getRect(faceRect);
        drawRect(scanFrame,rect, canvas,rectPaint);
        drawLine(scanLine,rect, canvas, linePaint);
        drawText(String.valueOf(key), rect, canvas, textPaint);
//        drawKeyPoint(canvas, pointPaint);
    }

    private void drawRect(Bitmap bitmap,RectF rect, Canvas canvas,Paint paint) {
        int color;
        if(status > 0){
            color = Color.GREEN;
        } else if(status < 0){
            color = Color.RED;
        } else {
            color = Color.WHITE;
        }
        paint.setColor(color);
        canvas.drawRect(rect, paint);

//        int width = (int) (rect.right - rect.left);
//        int height = (int) (rect.bottom - rect.top);
//        Bitmap newBmp = Bitmap.createScaledBitmap(bitmap, width, height, true);
//        canvas.drawBitmap(newBmp,rect.left,rect.top,null);
    }

    private void drawText(String text, RectF rect, Canvas canvas, Paint paint) {
        int color;
        if(status > 0){
            text = "识别成功";
            color = Color.GREEN;
        } else if(status < 0){
            text = "识别失败";
            color = Color.RED;
        } else {
            text = "正在识别";
            color = Color.WHITE;
        }
        paint.setColor(color);
        canvas.drawText(text, rect.left, rect.top - 20, paint);
    }

    private float temp = 0;
    private void drawLine( Bitmap scanLine,RectF rect, Canvas canvas, Paint paint) {
        if(status == 0){
            float height = rect.bottom - rect.top;
            temp = temp < height-10
                    ? (temp < height / 5) || (temp >= height * 0.8) ? temp + (height / 30) : temp + (height / 15)
                    : 0;
            Bitmap newBmp = Bitmap.createScaledBitmap(scanLine, (int) (rect.right - rect.left), 15, true);
            canvas.drawBitmap(newBmp,rect.left,rect.top + temp,null);
        }

//        canvas.drawLine(rect.left, rect.top + temp, rect.right, rect.top + temp, paint);
//        //取扫描线底部
//        float lineBottom = rect.top + 50 + temp;
//        //判断扫描线底部是否与人脸框底部重合，如果重合则位置不再移动
//        lineBottom = lineBottom < rect.bottom ?lineBottom : rect.bottom;
//        mRectF.set(rect.left,rect.top + temp ,rect.right,lineBottom);
//        canvas.drawRect(mRectF,paint);
    }

    private void drawKeyPoint(Canvas canvas, Paint paint) {
        if (landmarks == null) {
            return;
        }
        for (Point landmark : landmarks) {
            float x = landmark.x * FaceBoxUtil.getXRatio();
            float y = landmark.y * FaceBoxUtil.getYRatio();
            canvas.drawCircle(x, y, paint.getStrokeWidth(), paint);
        }
    }
}
