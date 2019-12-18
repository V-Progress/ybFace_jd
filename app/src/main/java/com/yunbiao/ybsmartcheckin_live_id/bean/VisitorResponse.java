package com.yunbiao.ybsmartcheckin_live_id.bean;

import com.yunbiao.ybsmartcheckin_live_id.db2.Visitor;

import java.util.List;

public class VisitorResponse {
    private String message;
    private int status;
    private List<Visitor> visitor;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<Visitor> getVisitor() {
        return visitor;
    }

    public void setVisitor(List<Visitor> visitor) {
        this.visitor = visitor;
    }

    @Override
    public String toString() {
        return "VisitorResponse{" +
                "message='" + message + '\'' +
                ", status=" + status +
                ", visitor=" + visitor +
                '}';
    }
}
