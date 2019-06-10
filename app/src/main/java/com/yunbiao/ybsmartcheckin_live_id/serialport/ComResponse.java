package com.yunbiao.ybsmartcheckin_live_id.serialport;

import java.io.Serializable;

/**
 * Created by chen on 2019/4/3.
 */

public class ComResponse implements Serializable {

    private int id;
    private String request;
    private String response;
    private int status;
    private int type;
    private boolean isReadSuccess;

    public int getId() {
        return id;
    }

    public String getRequest() {
        return request;
    }

    public String getResponse() {
        return response;
    }

    public int getStatus() {
        return status;
    }

    public int getType() {
        return type;
    }

    public boolean isReadSuccess() {
        return isReadSuccess;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setReadSuccess(boolean readSuccess) {
        isReadSuccess = readSuccess;
    }

    @Override
    public String toString() {
        return "ComResponse{" +
                "id = " + id +
                ", request = '" + request + "\'" +
                ", response = '" + response + "\'" +
                ", status = " + status +
                ", type = " + type +
                ", isReadSuccess = " + isReadSuccess +
                '}';
    }

}
