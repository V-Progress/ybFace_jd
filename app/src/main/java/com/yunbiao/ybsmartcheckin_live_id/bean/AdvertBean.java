package com.yunbiao.ybsmartcheckin_live_id.bean;

import java.util.List;

/**
 * Created by Administrator on 2018/11/26.
 */

public class AdvertBean {

    /**
     * advertObject : {"advertName":"缁欐垜閫佷釜蹇\u20ac掑埌璧靛浗","advertTime":20,"descinfo":"111","imgArray":[{"advertimg":"192.168.1.54/imgserver/busself/advert/20181119/a1c32a3a-4ab7-487a-a369-0d35438733f1.jpg"}],"advertId":1}
     * status : 1
     */
    private AdvertObjectEntity advertObject;
    private int status;

    public void setAdvertObject(AdvertObjectEntity advertObject) {
        this.advertObject = advertObject;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public AdvertObjectEntity getAdvertObject() {
        return advertObject;
    }

    public int getStatus() {
        return status;
    }

    public class AdvertObjectEntity {
        /**
         * advertName : 缁欐垜閫佷釜蹇€掑埌璧靛浗
         * advertTime : 20
         * descinfo : 111
         * imgArray : [{"advertimg":"192.168.1.54/imgserver/busself/advert/20181119/a1c32a3a-4ab7-487a-a369-0d35438733f1.jpg"}]
         * advertId : 1
         */
        private String advertName;
        private int advertTime;
        private String descinfo;
        private List<ImgArrayEntity> imgArray;
        private int advertId;

        public void setAdvertName(String advertName) {
            this.advertName = advertName;
        }

        public void setAdvertTime(int advertTime) {
            this.advertTime = advertTime;
        }

        public void setDescinfo(String descinfo) {
            this.descinfo = descinfo;
        }

        public void setImgArray(List<ImgArrayEntity> imgArray) {
            this.imgArray = imgArray;
        }

        public void setAdvertId(int advertId) {
            this.advertId = advertId;
        }

        public String getAdvertName() {
            return advertName;
        }

        public int getAdvertTime() {
            return advertTime;
        }

        public String getDescinfo() {
            return descinfo;
        }

        public List<ImgArrayEntity> getImgArray() {
            return imgArray;
        }

        public int getAdvertId() {
            return advertId;
        }

        public class ImgArrayEntity {
            /**
             * advertimg : 192.168.1.54/imgserver/busself/advert/20181119/a1c32a3a-4ab7-487a-a369-0d35438733f1.jpg
             */
            private String advertimg;

            public void setAdvertimg(String advertimg) {
                this.advertimg = advertimg;
            }

            public String getAdvertimg() {
                return advertimg;
            }
        }
    }
}
