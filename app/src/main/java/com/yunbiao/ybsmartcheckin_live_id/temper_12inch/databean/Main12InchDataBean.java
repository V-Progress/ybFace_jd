package com.yunbiao.ybsmartcheckin_live_id.temper_12inch.databean;

import androidx.databinding.ObservableField;

public class Main12InchDataBean {

    public final ObservableField<Boolean> isShowResult;

    public final ObservableField<Integer> resultBgRes;

    public final ObservableField<Integer> resultContentBgRes;

    public final ObservableField<Integer> resultIconRes;

    public final ObservableField<String> resultTipStr;

    public final ObservableField<String> resultTempStr;

    public Main12InchDataBean() {
        this.isShowResult = new ObservableField<>(false);
        this.resultBgRes = new ObservableField<>(0);
        this.resultContentBgRes = new ObservableField<>(0);
        this.resultIconRes = new ObservableField<>(0);
        this.resultTipStr = new ObservableField<>("");
        this.resultTempStr = new ObservableField<>("");
    }

    public ObservableField<Boolean> getIsShowResult() {
        return isShowResult;
    }

    public ObservableField<Integer> getResultBgRes() {
        return resultBgRes;
    }

    public ObservableField<Integer> getResultContentBgRes() {
        return resultContentBgRes;
    }

    public ObservableField<Integer> getResultIconRes() {
        return resultIconRes;
    }

    public ObservableField<String> getResultTipStr() {
        return resultTipStr;
    }

    public ObservableField<String> getResultTempStr() {
        return resultTempStr;
    }
}
