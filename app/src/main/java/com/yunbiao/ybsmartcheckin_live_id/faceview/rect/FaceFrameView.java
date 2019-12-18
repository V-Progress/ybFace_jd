package com.yunbiao.ybsmartcheckin_live_id.faceview.rect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.jdjr.risk.face.local.detect.BaseProperty;
import com.yunbiao.ybsmartcheckin_live_id.R;

import java.util.HashMap;
import java.util.Map;

public class FaceFrameView extends SurfaceView {
    private SurfaceHolder surfaceHolder;
    private static final String TAG = "FaceFrameView";

    //人脸数据
    private Map<Long, BaseProperty> basePropertyMap;//所检测到的人脸数据
    private Map<Long, FaceFrameBean> faceFrameBeanMap = new HashMap<>();//所检测到的人脸的集
    private Map<Long, Integer> faceVerifyResultMap = new HashMap<>();//人脸识别结果集

    private Bitmap scanFrame;//扫描框
    private Bitmap scanLine;//扫描线

    private Paint rectPaint;//框画笔
    private Paint linePaint;//线画笔
    private Paint textPaint;//文字画笔
    private Paint pointPaint;//关键点画笔

    public FaceFrameView(Context context) {
        super(context);
        init();
    }

    public FaceFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FaceFrameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(callback);
        this.getHolder().setFormat(PixelFormat.TRANSLUCENT);//设置背景透明

        //初始化扫描框
        scanFrame = BitmapFactory.decodeResource(getResources(), R.mipmap.scan_frame);
        //扫描线
        scanLine = BitmapFactory.decodeResource(getResources(), R.mipmap.scan_line);

        //初始化画笔
        initPaint();
    }

    private void initPaint() {
        rectPaint = new Paint();
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(5);
        rectPaint.setColor(Color.WHITE);

        //扫描线画笔
        linePaint = new Paint();
        linePaint.setColor(Color.WHITE);
        linePaint.setStyle(Paint.Style.FILL);

        //文字画笔
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(26);

        //关键点画笔
        pointPaint = new Paint();
        pointPaint.setColor(Color.WHITE);
        pointPaint.setStrokeWidth(2);
    }


    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            synchronized (this) {
                mDrawFlag = true;
            }
            surfaceHolder = holder;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            synchronized (this) {
                mDrawFlag = false;
            }
            stopDrawThread();
        }
    };

    public void addFace(Map<Long, BaseProperty> map) {
        this.basePropertyMap = map;
        //有人脸并且线程未在执行
        if (basePropertyMap != null && !isDrawThreadRunning()) {
            startDrawThread();
        }
    }

    private void updateStatus(long faceId, int status) {
        if (faceFrameBeanMap != null && faceFrameBeanMap.containsKey(faceId)) {
            faceFrameBeanMap.get(faceId).updateStatus(status);
        }
    }

    public void updateResult(long faceId, int result) {
//        if(faceFrameBeanMap != null && faceFrameBeanMap.containsKey(faceId)){
//            faceFrameBeanMap.get(faceId).updateStatus(result < 0 ? -1 : 1);
//        }
        if (result < 0) {
            if (faceVerifyResultMap.containsKey(faceId)) {
                Integer integer = faceVerifyResultMap.get(faceId);
                integer += 1;
                if (integer >= 7) {
                    updateStatus(faceId, -1);
                    faceVerifyResultMap.remove(faceId);
                } else {
                    faceVerifyResultMap.put(faceId, integer);
                }
            } else {
                faceVerifyResultMap.put(faceId, 1);
            }
        } else if (result == 0) {
            updateStatus(faceId, 1);
            faceVerifyResultMap.remove(faceId);
        }
    }

    private boolean mDrawFlag = true;

    /***
     * 具体绘制方法
     */
    private void drawFace() {
        synchronized (surfaceHolder) {
            if (mDrawFlag) {
                Canvas canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas();
                    drawImpl(canvas);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }

    private void drawImpl(Canvas canvas) {
        if (canvas != null) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            if (basePropertyMap != null) {


                for (Map.Entry<Long, FaceFrameBean> faceEntry : faceFrameBeanMap.entrySet()) {
                    Long key = faceEntry.getKey();
                    if (!basePropertyMap.containsKey(key)) {
                        faceFrameBeanMap.remove(key);
                    }
                }

                for (Map.Entry<Long, BaseProperty> entry : basePropertyMap.entrySet()) {
                    Long key = entry.getKey();
                    if (key == 0) {
                        continue;
                    }
                    BaseProperty value = entry.getValue();
                    if (faceFrameBeanMap.containsKey(key)) {
                        faceFrameBeanMap.get(key).update(value);
                    } else {
                        faceFrameBeanMap.put(key, new FaceFrameBean(value));
                    }
                }

                for (Map.Entry<Long, FaceFrameBean> entry : faceFrameBeanMap.entrySet()) {
                    FaceFrameBean bean = entry.getValue();
                    bean.draw(entry.getKey(), scanFrame, scanLine, canvas, rectPaint, linePaint, textPaint, pointPaint);
                }
            } else {
                faceFrameBeanMap.clear();
                stopDrawThread();
            }
        }
    }

    /***===================================================================================================
     * 绘制线程是否正在进行
     * @return
     */
    private boolean isDrawThreadRunning() {
        return drawThread != null && !drawThread.isStop();
    }

    /***
     * 开始绘制线程
     */
    private void startDrawThread() {
        if (drawThread == null) {
            drawThread = new DrawThread();
            setFps(60);
        }
        drawThread.start();
    }

    /***
     * 停止绘制线程
     */
    private void stopDrawThread() {
        if (drawThread != null) {
            drawThread.stopThread();
            drawThread = null;
        }
    }

    /***
     * 设置FPS、帧数
     * @param fps
     */
    private void setFps(long fps) {
        if (drawThread != null) {
            drawThread.setFps(fps);
        }
    }

    private DrawThread drawThread;

    private class DrawThread extends Thread {
        private volatile boolean isStop = false;
        private long fps = 20;

        public boolean isStop() {
            return isStop;
        }

        public void setFps(long fps) {
            this.fps = fps;
        }

        public void stopThread() {
            isStop = true;
        }

        @Override
        public void run() {
            while (!isStop) {
                drawFace();
                // TODO: 2019/12/11 帧数限制
                try {
                    Thread.sleep(1000 / fps);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
