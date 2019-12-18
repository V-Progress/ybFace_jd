package com.yunbiao.ybsmartcheckin_live_id.bean;

public class DeviceInfoBean {
    private String sid;
    private int type;
    private Content content;

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public class Content{
        private int bindStatus;
        private String deviceName;
        private String deviceQrCode;
        private String expireDate;
        private String pwd;
        private String status;
        private String serNum;
        private String runKey;
        private int deviceVer;
        private int dtype;
        private int isMirror;
        private int runStatus;
        private String url;
        private int themeId;
        private String type;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getThemeId() {
            return themeId;
        }

        public void setThemeId(int themeId) {
            this.themeId = themeId;
        }

        private double voice;

        private int flag;

        private int restart;

        public int getRestart() {
            return restart;
        }

        public void setRestart(int restart) {
            this.restart = restart;
        }

        public int getFlag() {
            return flag;
        }

        public void setFlag(int flag) {
            this.flag = flag;
        }

        public double getVoice() {
            return voice;
        }

        public void setVoice(double voice) {
            this.voice = voice;
        }

        public int getBindStatus() {
            return bindStatus;
        }

        public void setBindStatus(int bindStatus) {
            this.bindStatus = bindStatus;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public String getDeviceQrCode() {
            return deviceQrCode;
        }

        public void setDeviceQrCode(String deviceQrCode) {
            this.deviceQrCode = deviceQrCode;
        }

        public String getExpireDate() {
            return expireDate;
        }

        public void setExpireDate(String expireDate) {
            this.expireDate = expireDate;
        }

        public String getPwd() {
            return pwd;
        }

        public void setPwd(String pwd) {
            this.pwd = pwd;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getSerNum() {
            return serNum;
        }

        public void setSerNum(String serNum) {
            this.serNum = serNum;
        }

        public String getRunKey() {
            return runKey;
        }

        public void setRunKey(String runKey) {
            this.runKey = runKey;
        }

        public int getDeviceVer() {
            return deviceVer;
        }

        public void setDeviceVer(int deviceVer) {
            this.deviceVer = deviceVer;
        }

        public int getDtype() {
            return dtype;
        }

        public void setDtype(int dtype) {
            this.dtype = dtype;
        }

        public int getIsMirror() {
            return isMirror;
        }

        public void setIsMirror(int isMirror) {
            this.isMirror = isMirror;
        }

        public int getRunStatus() {
            return runStatus;
        }

        public void setRunStatus(int runStatus) {
            this.runStatus = runStatus;
        }

        @Override
        public String toString() {
            return "Content{" +
                    "bindStatus=" + bindStatus +
                    ", deviceName='" + deviceName + '\'' +
                    ", deviceQrCode='" + deviceQrCode + '\'' +
                    ", expireDate='" + expireDate + '\'' +
                    ", pwd='" + pwd + '\'' +
                    ", status='" + status + '\'' +
                    ", serNum='" + serNum + '\'' +
                    ", runKey='" + runKey + '\'' +
                    ", deviceVer=" + deviceVer +
                    ", dtype=" + dtype +
                    ", isMirror=" + isMirror +
                    ", runStatus=" + runStatus +
                    '}';
        }
    }
    public class EntryArray{


    }

    public  class  Entrys{
        private String name;



    }
    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "DeviceInfoBean{" +
                "sid='" + sid + '\'' +
                ", type=" + type +
                ", content=" + content.toString() +
                '}';
    }


}
