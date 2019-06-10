package com.yunbiao.ybsmartcheckin_live_id.bean;

import java.util.List;

/**
 * Created by Administrator on 2018/10/18.
 */

public class StaffBean {

    /**
     * lateNum : 2
     * dep : [{"gotime":"00:00","entry":[{"head":"http://192.168.1.54/imgserver/resource/head/2018/2018-10-09/4a81efa5-eeb6-48c6-8c26-341e99ccdb8a.jpg","number":"001","sex":0,"name":"刘诗诗","autograph":"大家好,我是气质女神刘诗诗","faceId":0,"id":10,"position":"女神","age":22},{"head":"http://192.168.1.54/imgserver/resource/head/2018/2018-10-10/b1daa6e8-63d1-43c3-9ba6-ae1fbbedf4af.jpg","number":"002","sex":1,"name":"蒿俊闵","autograph":"足球运动员","faceId":1,"id":11,"position":"队长","age":31}],"parentName":"技术部-","downtips":"成功","gotips":"成功","depId":4,"depName":"技术部-财务部","down":"00:00","parentId":1}]
     * status : 1
     */
    private int lateNum;
    private List<DepEntity> dep;
    private int status;

    public void setLateNum(int lateNum) {
        this.lateNum = lateNum;
    }

    public void setDep(List<DepEntity> dep) {
        this.dep = dep;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getLateNum() {
        return lateNum;
    }

    public List<DepEntity> getDep() {
        return dep;
    }

    public int getStatus() {
        return status;
    }

    public class DepEntity {
        /**
         * gotime : 00:00
         * entry : [{"head":"http://192.168.1.54/imgserver/resource/head/2018/2018-10-09/4a81efa5-eeb6-48c6-8c26-341e99ccdb8a.jpg","number":"001","sex":0,"name":"刘诗诗","autograph":"大家好,我是气质女神刘诗诗","faceId":0,"id":10,"position":"女神","age":22},{"head":"http://192.168.1.54/imgserver/resource/head/2018/2018-10-10/b1daa6e8-63d1-43c3-9ba6-ae1fbbedf4af.jpg","number":"002","sex":1,"name":"蒿俊闵","autograph":"足球运动员","faceId":1,"id":11,"position":"队长","age":31}]
         * parentName : 技术部-
         * downtips : 成功
         * gotips : 成功
         * depId : 4
         * depName : 技术部-财务部
         * down : 00:00
         * parentId : 1
         */
        private String gotime;
        private List<EntryEntity> entry;
        private String parentName;
        private String downtips;
        private String gotips;
        private int depId;
        private String depName;
        private String down;
        private int parentId;

        public void setGotime(String gotime) {
            this.gotime = gotime;
        }

        public void setEntry(List<EntryEntity> entry) {
            this.entry = entry;
        }

        public void setParentName(String parentName) {
            this.parentName = parentName;
        }

        public void setDowntips(String downtips) {
            this.downtips = downtips;
        }

        public void setGotips(String gotips) {
            this.gotips = gotips;
        }

        public void setDepId(int depId) {
            this.depId = depId;
        }

        public void setDepName(String depName) {
            this.depName = depName;
        }

        public void setDown(String down) {
            this.down = down;
        }

        public void setParentId(int parentId) {
            this.parentId = parentId;
        }

        public String getGotime() {
            return gotime;
        }

        public List<EntryEntity> getEntry() {
            return entry;
        }

        public String getParentName() {
            return parentName;
        }

        public String getDowntips() {
            return downtips;
        }

        public String getGotips() {
            return gotips;
        }

        public int getDepId() {
            return depId;
        }

        public String getDepName() {
            return depName;
        }

        public String getDown() {
            return down;
        }

        public int getParentId() {
            return parentId;
        }

        public class EntryEntity {
            /**
             * head : http://192.168.1.54/imgserver/resource/head/2018/2018-10-09/4a81efa5-eeb6-48c6-8c26-341e99ccdb8a.jpg
             * number : 001
             * sex : 0
             * name : 刘诗诗
             * autograph : 大家好,我是气质女神刘诗诗
             * faceId : 0
             * id : 10
             * position : 女神
             * age : 22
             */
            private String head;
            private String number;
            private int sex;
            private String name;
            private String autograph;
            private int faceId;
            private int id;
            private String position;
            private String birthday;
            private int age;

            public String getBirthday() {
                return birthday;
            }

            public void setBirthday(String birthday) {
                this.birthday = birthday;
            }

            public void setHead(String head) {
                this.head = head;
            }

            public void setNumber(String number) {
                this.number = number;
            }

            public void setSex(int sex) {
                this.sex = sex;
            }

            public void setName(String name) {
                this.name = name;
            }

            public void setAutograph(String autograph) {
                this.autograph = autograph;
            }

            public void setFaceId(int faceId) {
                this.faceId = faceId;
            }

            public void setId(int id) {
                this.id = id;
            }

            public void setPosition(String position) {
                this.position = position;
            }

            public void setAge(int age) {
                this.age = age;
            }

            public String getHead() {
                return head;
            }

            public String getNumber() {
                return number;
            }

            public int getSex() {
                return sex;
            }

            public String getName() {
                return name;
            }

            public String getAutograph() {
                return autograph;
            }

            public int getFaceId() {
                return faceId;
            }

            public int getId() {
                return id;
            }

            public String getPosition() {
                return position;
            }

            public int getAge() {
                return age;
            }
        }
    }
}
