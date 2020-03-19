package com.yunbiao.faceview;

public class RequestFeatureStatus {
    public static final int SEARCHING = 0;//处理中
    public static final int SUCCEED = 1;//处理完成
    public static final int FAILED = 2;//处理失败
    public static final int TO_RETRY = 3;//重试中
    public static final int SUCCEED_STRANGER = -1;//陌生人
}
