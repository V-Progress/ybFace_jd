package com.yunbiao.ybsmartcheckin_live_id.temper_12inch;

import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.databinding.BindingAdapter;

public class ImageViewAdapter {

    @BindingAdapter("android:src")
    public static void setSrc(ImageView imageView, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    @BindingAdapter("android:src")
    public static void setSrc(ImageView imageView, @DrawableRes int resId) {
        imageView.setImageResource(resId);
    }

}
