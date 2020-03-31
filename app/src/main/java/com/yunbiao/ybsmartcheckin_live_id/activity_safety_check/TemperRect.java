package com.yunbiao.ybsmartcheckin_live_id.activity_safety_check;

import android.graphics.Rect;

public class TemperRect {

    public static Rect getSmall(){
        return new Rect(20,20,60,40);//40 * 20
    }

    public static Rect getMiddle(){
        return new Rect(15,15,65,45);//50 * 30
    }

    public static Rect getLarge(){
        return new Rect(10,10,70,50);//60 * 40
    }

}
