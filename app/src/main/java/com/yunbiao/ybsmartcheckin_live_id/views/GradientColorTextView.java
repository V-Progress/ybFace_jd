package com.yunbiao.ybsmartcheckin_live_id.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.AppCompatTextView;

import com.yunbiao.ybsmartcheckin_live_id.R;

import skin.support.widget.SkinCompatBackgroundHelper;
import skin.support.widget.SkinCompatSupportable;
import skin.support.widget.SkinCompatTextHelper;

public class GradientColorTextView extends AppCompatTextView implements SkinCompatSupportable {
    private SkinCompatTextHelper mTextHelper;
    private SkinCompatBackgroundHelper mBackgroundTintHelper;

    public GradientColorTextView(Context context) {
        this(context,null);
    }

    public GradientColorTextView (Context context, AttributeSet attrs) {
        super(context, attrs, 0);

    }

    public GradientColorTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mBackgroundTintHelper = new SkinCompatBackgroundHelper(this);
        mBackgroundTintHelper.loadFromAttributes(attrs, defStyleAttr);
        mTextHelper = SkinCompatTextHelper.create(this);
        mTextHelper.loadFromAttributes(attrs, defStyleAttr);

        getResources().getColor(R.color.skin_color_main_text_color_start);
        getResources().getColor(R.color.skin_color_main_text_color_end);
    }

    @Override
    public void setBackgroundResource(@DrawableRes int resId) {
        super.setBackgroundResource(resId);
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.onSetBackgroundResource(resId);
        }
    }

    @Override
    public void setTextAppearance(int resId) {
        setTextAppearance(getContext(), resId);
    }

    @Override
    public void setTextAppearance(Context context, int resId) {
        super.setTextAppearance(context, resId);
        if (mTextHelper != null) {
            mTextHelper.onSetTextAppearance(context, resId);
        }
    }

    @Override
    public void applySkin() {
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.applySkin();
        }
        if (mTextHelper != null) {
            mTextHelper.applySkin();
        }

        int start = getResources().getColor(R.color.skin_color_main_text_color_start);
        int end = getResources().getColor(R.color.skin_color_main_text_color_end);
        Shader shader =new LinearGradient(0, 0, 0, 20,start, end, Shader.TileMode.CLAMP);
        getPaint().setShader(shader);
        invalidate();
    }
}