package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in_smt;

public abstract class SMTTempBase2Activity extends SMTBaseActivity {
    private int smtModel;
    private long mSpeechDelay;
    private Float mTempMinThreshold;
    private Float mTempWarningThreshold;
    private float mCurrentTemp = 0.0f;
    private boolean isRunning = false;
    private long mCacheTime = 0;
    private boolean mLowTemp;
    private Float mAmbCorrValue;
    private Float mTempCorrValue;
    private boolean mDistanceTipEnable;
    private boolean mFEnabled;


}
