package com.yunbiao.ybsmartcheckin_live_id.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.Properties;

/**
 * Created by Administrator on 2019/4/1.
 */

public class PropsUtil {
    private static final String TAG = "PropsUtil";
    private static PropsUtil instance;

    private Properties props;
    private final String KEY_XMPP_HOST = "xmppHost";
    private final String KEY_XMPP_PORT = "xmppPort";
    private final String KEY_XMPP_APIKEY = "apiKey";
    private final String KEY_RES_HOST = "resourceHost";
    private final String KEY_RES_PORT = "resourcePort";
    private final String KEY_BOARD_TYPE = "boardType";
    private final String KEY_CALCU_TYPE = "calculationType";

    public static PropsUtil instance(){
        if(instance == null){
            synchronized(PropsUtil.class){
                if(instance == null){
                    instance = new PropsUtil();
                }
            }
        }
        return instance;
    }

    private PropsUtil(){}

    public void init(Context context){
        props = new Properties();
        try {
            int id = context.getResources().getIdentifier("androidpn", "raw",
                    context.getPackageName());
            props.load(context.getResources().openRawResource(id));
        } catch (Exception e) {
            Log.e(TAG, "Could not find the properties file.", e);
            // e.printStackTrace();
        }
    }

    public Integer getBoardType(){
        if(props == null)
            return null;
        return Integer.valueOf(props.getProperty(KEY_BOARD_TYPE,"0"));//默认为云标地址
    }

    public boolean setBoardType(String value){
        if(TextUtils.isEmpty(value))
            return false;
        if(props == null)
            return false;
        props.setProperty(KEY_BOARD_TYPE,value);

        String property = props.getProperty(KEY_BOARD_TYPE);
        return TextUtils.equals(value,property);
    }


    public String getHost(){
        if(props == null)
            return null;
        return props.getProperty(KEY_XMPP_HOST,"47.105.80.245");//默认为云标地址
    }

    public boolean setHost(String value){
        if(TextUtils.isEmpty(value))
            return false;
        if(props == null)
            return false;
        props.setProperty(KEY_XMPP_HOST,value);

        String property = props.getProperty(KEY_XMPP_HOST);
        return TextUtils.equals(value,property);
    }

    public String getPort(){
        if(props == null)
            return null;
        return props.getProperty(KEY_XMPP_PORT, "5222");//默认为云标端口
    }

    public boolean setPort(String value){
        if(TextUtils.isEmpty(value))
            return false;
        if(props == null)
            return false;
        props.setProperty(KEY_XMPP_PORT,value);

        String property = props.getProperty(KEY_XMPP_PORT);
        return TextUtils.equals(value,property);
    }


    public String getApikey(){
        if(props == null)
            return null;
        return props.getProperty(KEY_XMPP_APIKEY, "");
    }

    public boolean setApikey(String value){
        if(TextUtils.isEmpty(value))
            return false;
        if(props == null)
            return false;
        props.setProperty(KEY_XMPP_APIKEY, value);

        String property = props.getProperty(KEY_XMPP_APIKEY);
        return TextUtils.equals(value,property);
    }

    //设置资源地址
    public String getResHost(){
        if(props == null)
            return null;
        return props.getProperty(KEY_RES_HOST, "47.105.80.245");
    }

    public boolean setResHost(String value){
        if(TextUtils.isEmpty(value))
            return false;
        if(props == null)
            return false;
        props.setProperty(KEY_RES_HOST, value);

        String property = props.getProperty(KEY_RES_HOST);
        return TextUtils.equals(value,property);
    }

    public String getResPort(){
        if(props == null)
            return null;
        return props.getProperty(KEY_RES_PORT, "80");
    }

    public boolean setResPort(String value){
        if(TextUtils.isEmpty(value))
            return false;
        if(props == null)
            return false;
        props.setProperty(KEY_RES_PORT, value);

        String property = props.getProperty(KEY_RES_PORT);
        return TextUtils.equals(value,property);
    }
}
