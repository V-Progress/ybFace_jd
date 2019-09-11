package com.yunbiao.ybsmartcheckin_live_id.faceview.face_new;

import com.jdjr.risk.face.local.detect.BaseProperty;
import com.jdjr.risk.face.local.extract.FaceProperty;
import com.jdjr.risk.face.local.verify.VerifyResult;

public class FaceResult {
    private BaseProperty baseProperty;
    private FaceProperty faceProperty;
    private VerifyResult verifyResult;
    public FaceResult(BaseProperty baseProperty) {
        this.baseProperty = baseProperty;
    }
    public BaseProperty getBaseProperty() {
        return baseProperty;
    }
    public void setBaseProperty(BaseProperty baseProperty) {
        this.baseProperty = baseProperty;
    }
    public FaceProperty getFaceProperty() {
        return faceProperty;
    }
    public void setFaceProperty(FaceProperty faceProperty) {
        this.faceProperty = faceProperty;
    }
    public VerifyResult getVerifyResult() {
        return verifyResult;
    }
    public void setVerifyResult(VerifyResult verifyResult) {
        this.verifyResult = verifyResult;
    }
}
