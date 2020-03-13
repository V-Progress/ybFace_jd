package com.yunbiao.ybsmartcheckin_live_id.utils;

import java.util.Arrays;

public class IdCardMsg {
    public int type;
    public String name;
    public String sex;
    public String nation_str;

    public String birth_year;
    public String birth_month;
    public String birth_day;
    public String address;
    public String id_num;
    public String sign_office;

    public String useful_s_date_year;
    public String useful_s_date_month;
    public String useful_s_date_day;

    public String useful_e_date_year;
    public String useful_e_date_month;
    public String useful_e_date_day;

    //外国人居留证
    //姓名 性别120b,2b
    public String fname;
    public String fsex;
    //永久居留证号码 30b
    public String fID;
    //国籍代码 6b
    public String fnation;
    //中文姓名30b
    public String fcnName;
    //证件签发日期16b  证件终止日期16b
    public String fuseful_s_date_year;
    public String fuseful_s_date_month;
    public String fuseful_s_date_day;

    public String fuseful_e_date_year;
    public String fuseful_e_date_month;
    public String fuseful_e_date_day;
    //出生日期	16b
    public String fbirth_year;
    public String fbirth_month;
    public String fbirth_day;
    //证件版本号 4b
    public String fver;
    //当次申请受理机关代码8b
    public String forgan;
    //证件类型标志2b
    public String fflag;
    //预留项 6b
    public String fReserved;


    //港澳台居住证
    //姓名
    public String szHMTName;
    //性别
    public String szHMTSex;
    //预留
    public String szHMTRes1;
    //生日
    public String szHMTBirthday;
    //住址
    public String szHMTAddr;
    //身份证号码
    public String szHMTID;
    //签发机关
    public String szHMTOrgan;
    //有效期起始日期起
    public String szHMTBegindate;
    //有效期截止日期
    public String szHMTEndDate;
    //通行证号码
    public String szHMTPassCode;
    //签发次数
    public String szHMTSignFrequency;
    //预留区
    public String szHMTRes2;
    //证件类型标志
    public String szHMTFlag;
    //预留区
    public String szHMTRes3;

    public byte[] ptoto;

    @Override
    public String toString() {
        return "IdCardMsg{" +
                "name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", nation_str='" + nation_str + '\'' +
                ", birth_year='" + birth_year + '\'' +
                ", birth_month='" + birth_month + '\'' +
                ", birth_day='" + birth_day + '\'' +
                ", address='" + address + '\'' +
                ", id_num='" + id_num + '\'' +
                ", sign_office='" + sign_office + '\'' +
                ", useful_s_date_year='" + useful_s_date_year + '\'' +
                ", useful_s_date_month='" + useful_s_date_month + '\'' +
                ", useful_s_date_day='" + useful_s_date_day + '\'' +
                ", useful_e_date_year='" + useful_e_date_year + '\'' +
                ", useful_e_date_month='" + useful_e_date_month + '\'' +
                ", useful_e_date_day='" + useful_e_date_day + '\'' +
                ", fname='" + fname + '\'' +
                ", fsex='" + fsex + '\'' +
                ", fID='" + fID + '\'' +
                ", fnation='" + fnation + '\'' +
                ", fcnName='" + fcnName + '\'' +
                ", fuseful_s_date_year='" + fuseful_s_date_year + '\'' +
                ", fuseful_s_date_month='" + fuseful_s_date_month + '\'' +
                ", fuseful_s_date_day='" + fuseful_s_date_day + '\'' +
                ", fuseful_e_date_year='" + fuseful_e_date_year + '\'' +
                ", fuseful_e_date_month='" + fuseful_e_date_month + '\'' +
                ", fuseful_e_date_day='" + fuseful_e_date_day + '\'' +
                ", fbirth_year='" + fbirth_year + '\'' +
                ", fbirth_month='" + fbirth_month + '\'' +
                ", fbirth_day='" + fbirth_day + '\'' +
                ", fver='" + fver + '\'' +
                ", forgan='" + forgan + '\'' +
                ", fflag='" + fflag + '\'' +
                ", fReserved='" + fReserved + '\'' +
                ", szHMTName='" + szHMTName + '\'' +
                ", szHMTSex='" + szHMTSex + '\'' +
                ", szHMTRes1='" + szHMTRes1 + '\'' +
                ", szHMTBirthday='" + szHMTBirthday + '\'' +
                ", szHMTAddr='" + szHMTAddr + '\'' +
                ", szHMTID='" + szHMTID + '\'' +
                ", szHMTOrgan='" + szHMTOrgan + '\'' +
                ", szHMTBegindate='" + szHMTBegindate + '\'' +
                ", szHMTEndDate='" + szHMTEndDate + '\'' +
                ", szHMTPassCode='" + szHMTPassCode + '\'' +
                ", szHMTSignFrequency='" + szHMTSignFrequency + '\'' +
                ", szHMTRes2='" + szHMTRes2 + '\'' +
                ", szHMTFlag='" + szHMTFlag + '\'' +
                ", szHMTRes3='" + szHMTRes3 + '\'' +
                ", ptoto=" + (ptoto == null ? -1 : ptoto.length) +
                '}';
    }
}
