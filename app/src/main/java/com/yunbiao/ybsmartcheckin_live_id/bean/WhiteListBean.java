package com.yunbiao.ybsmartcheckin_live_id.bean;

import java.util.List;

public class WhiteListBean {

    private int status;

    private String message;

    private List<WhiteBean> data;

    public static class WhiteBean{
        private String province;//省
        private String city;//市 （为0就是没设置）
        private String county;//区（为0就是没设置）

        @Override
        public String toString() {
            return "WhiteBean{" +
                    "province='" + province + '\'' +
                    ", city='" + city + '\'' +
                    ", county='" + county + '\'' +
                    '}';
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCounty() {
            return county;
        }

        public void setCounty(String county) {
            this.county = county;
        }
    }

    @Override
    public String toString() {
        return "WhiteListBean{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<WhiteBean> getData() {
        return data;
    }

    public void setData(List<WhiteBean> data) {
        this.data = data;
    }
}
