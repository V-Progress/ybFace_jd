package com.yunbiao.ybsmartcheckin_live_id.printer;

public interface UsbPrinterStatus {

    /***
     * 打印机可用
     */
    int AVAILABLE = 0;

    /***
     * 打印机未连接
     */
    int PRINTER_NOT_CONNECT = 1;

    /***
     * 打印机和调用库不匹配
     */
    int SDK_DONT_MATCH = 2;

    /***
     * 打印头已打开
     */
    int PRINT_HEAD_OPEN = 3;

    /***
     * 切到未复位
     */
    int CUTTER_NOT_RESET = 4;

    /***
     * 打印头过热
     */
    int PRINT_HEAD_OVERHEAT = 5;

    /***
     * 黑标错误
     */
    int BLACK_MARK_ERROR = 6;

    /***
     * 无纸
     */
    int PAPER_NO = 7;

    /***
     * 纸将用尽
     */
    int PAPER_LESS = 8;

    public static String getStringStatus(int status){
        String strStatus = "";
        switch (status) {
            case AVAILABLE:
//                strStatus = "打印机正常";
                break;
            case PRINTER_NOT_CONNECT:
                strStatus = "打印机未连接";
                break;
            case SDK_DONT_MATCH:
//                strStatus = "打印机和调用库不匹配";
                break;
            case PRINT_HEAD_OPEN:
                strStatus = "打印头已打开";
                break;
            case CUTTER_NOT_RESET:
                strStatus = "切刀未复位";
                break;
            case PRINT_HEAD_OVERHEAT:
                strStatus = "打印头过热";
                break;
            case BLACK_MARK_ERROR:
                strStatus = "黑标错误";
                break;
            case PAPER_NO:
                strStatus = "无纸";
                break;
            case PAPER_LESS:
                strStatus = "纸将用尽";
                break;
            default:
                strStatus = "未知错误";
                break;
        }
        return strStatus;
    }

}
