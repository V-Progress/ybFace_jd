package com.yunbiao.ybsmartcheckin_live_id.bean;

public class AddStaffResponse extends BaseResponse{
    private String faceId;
    private long entryId;

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public long getEntryId() {
        return entryId;
    }

    public void setEntryId(long entryId) {
        this.entryId = entryId;
    }
}
