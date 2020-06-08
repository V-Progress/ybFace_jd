package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.lcw.library.imagepicker.utils.ImageLoader;
import com.yunbiao.ybsmartcheckin_live_id.APP;

public class ImgLoader implements ImageLoader {
    @Override
    public void loadImage(ImageView imageView, String imagePath) {
        Glide.with(APP.getContext()).load(imagePath).asBitmap().override(50, 50).into(imageView);
    }

    @Override
    public void loadPreImage(ImageView imageView, String imagePath) {
        Glide.with(APP.getContext()).load(imagePath).asBitmap().override(50, 50).into(imageView);
    }

    @Override
    public void clearMemoryCache() {
        //清理缓存
        Glide.get(APP.getContext()).clearMemory();
    }
}
