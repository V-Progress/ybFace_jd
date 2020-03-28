package com.yunbiao.ybsmartcheckin_live_id.activity_temper_multiple;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yunbiao.ybsmartcheckin_live_id.R;

public class BlackAreaCorrectView extends FrameLayout {

    private ImageView imageView;
    private SeekBar xSeekBar;
    private SeekBar ySeekBar;

    public BlackAreaCorrectView(Context context) {
        super(context);
        init();
    }

    public BlackAreaCorrectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BlackAreaCorrectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public BlackAreaCorrectView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private Bitmap mBitmap;
    private Rect mAreaRect = new Rect(0, 0, 5, 5);
    public void setInitRect(Rect rect){
        mAreaRect.set(rect);
        if(imageView != null){
            xSeekBar.setProgress(mAreaRect.left);
            ySeekBar.setProgress(mAreaRect.top);
            Bitmap bitmap = drawRectangles(mBitmap, mAreaRect);
            imageView.setImageBitmap(bitmap);
        }
    }

    private static final String TAG = "BlackAreaCorrectView";
    private void init() {
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        //横屏
        if(measuredWidth > measuredHeight){

        } else if(measuredHeight > measuredWidth){

        } else {

        }



        mBitmap = big(BitmapFactory.decodeResource(getResources(),R.mipmap.h_1));
        Log.e(TAG, "init: " + mBitmap.getWidth());
        Log.e(TAG, "init: " + mBitmap.getHeight());

        imageView = new ImageView(getContext());
        imageView.setLayoutParams(new LayoutParams(640, 480));
        addView(imageView);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        Bitmap bitmap = drawRectangles(mBitmap, mAreaRect);
        imageView.setImageBitmap(bitmap);

        xSeekBar = new SeekBar(getContext());
        ySeekBar = new SeekBar(getContext());

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        addView(xSeekBar,layoutParams);

        layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = 50;
        addView(ySeekBar,layoutParams);

        xSeekBar.setProgress(0);
        ySeekBar.setProgress(0);
        xSeekBar.setMax(mBitmap.getWidth());
        ySeekBar.setMax(mBitmap.getHeight());

        xSeekBar.setOnSeekBarChangeListener(xChangeListener);
        ySeekBar.setOnSeekBarChangeListener(yChangeListener);
    }

    private SeekBar.OnSeekBarChangeListener xChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mAreaRect.left = (0 + progress);
            mAreaRect.right = (5 + progress);

            Bitmap bitmap = drawRectangles(mBitmap, mAreaRect);
            imageView.setImageBitmap(bitmap);

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    private SeekBar.OnSeekBarChangeListener yChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mAreaRect.top = (0 + progress);
            mAreaRect.bottom = (5 + progress);

            Bitmap bitmap = drawRectangles(mBitmap, mAreaRect);
            imageView.setImageBitmap(bitmap);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private Bitmap drawRectangles(Bitmap imageBitmap, Rect rect) {
        Bitmap mutableBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);//不填充
        paint.setStrokeWidth(10); //线的宽度
        canvas.drawRect(rect,paint);
        return mutableBitmap;
    }

    /**Bitmap放大的方法*/
    private static Bitmap big(Bitmap bitmap) {
        float width = 640 / bitmap.getWidth();
        float height = 480 / bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.postScale(width,height); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        return resizeBmp;
    }
}
