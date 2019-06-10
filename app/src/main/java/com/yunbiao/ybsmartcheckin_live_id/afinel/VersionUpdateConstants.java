package com.yunbiao.ybsmartcheckin_live_id.afinel;

import android.content.Context;

/**
 * Created by Administrator on 2015/12/7.
 */

public class VersionUpdateConstants {

    private static final int MESSAGE_DISTRIBUTE = 1; //信息发布
    private static final int QUEUE_BUSINESS = 2; //商家板排队叫号
    private static final int QUEUE = 3;//排队叫号
    private static final int WEI_PRINT = 4;//微信打印
    private static final int WEI_METTING = 5;//微信会议
    private static final int YUNBIAO_PAD = 6;//云标画板
    
    public static final int CURRENT_VERSION = MESSAGE_DISTRIBUTE;

    //得到具体的服务类型
    public static String getServerType(Context context) {
        String serverType = "";
        switch (CURRENT_VERSION) {
            case MESSAGE_DISTRIBUTE:
                serverType = "信息发布";
                break;
            case QUEUE_BUSINESS:
                serverType = "商家板排队叫号";
                break;
            case QUEUE:
                serverType = "排队叫号";
                break;
            case WEI_PRINT:
                serverType = "微信打印";
                break;
            case WEI_METTING:
                serverType = "微信会议";
                break;
            case YUNBIAO_PAD:
                serverType = "云标画板";
                break;
        }
        return serverType;
    }

}
