package com.yunbiao.ybsmartcheckin_live_id.serialport.utils;

/**
 * Created by chen on 2019/4/3.
 */

public class HexUtil {

    public static byte[] hexStr2Bytes(String src) {
        int l = src.length() / 2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            ret[i] = (byte) Integer
                    .valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
        }
        return ret;
    }

    //取反
    public static String parseHex2Opposite(String src) {
        String hexStr;
        byte[] er = hexStr2Bytes(src);
        byte erBefore[] = new byte[er.length];
        for (int i = 0; i < er.length; i++) {
            erBefore[i] = (byte) ~er[i];
        }
        hexStr = bytes2HexStr(erBefore, erBefore.length);
        if (hexStr.length() < src.length()) {
            for (int j = 0; j < src.length() - hexStr.length(); j++) {
                hexStr = "0" + hexStr;
            }
        }
        return hexStr;
    }

    public static String bytes2HexStr(byte[] b, int size) {
        StringBuffer result = new StringBuffer();
        String hex;
        for (int i = 0; i < size; i++) {
            hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            result.append(hex.toUpperCase());
        }
        return result.toString();
    }

    public static String intStr2HexStr(String intStr, boolean isKeepOne) {
        String hexStr;
        int tempInt = Integer.parseInt(intStr);
        hexStr = Integer.toHexString(tempInt);
        if (hexStr.length() == 1 && !isKeepOne) {
            hexStr = "0" + hexStr;
        }
        return hexStr.toUpperCase();
    }

    public static String int2HexStr(int num, boolean isKeepOne) {
        String hexStr;
        hexStr = Integer.toHexString(num);
        if (hexStr.length() == 1 && !isKeepOne) {
            hexStr = "0" + hexStr;
        }
        return hexStr.toUpperCase();
    }

    private static String[] binaryArray =
            {"0000", "0001", "0010", "0011",
                    "0100", "0101", "0110", "0111",
                    "1000", "1001", "1010", "1011",
                    "1100", "1101", "1110", "1111"};

    //转换为二进制字符串
    public static String bytes2BinaryStr(byte[] bArray) {
        String outStr = "";
        int pos;
        for (byte b : bArray) {
            //高四位
            pos = (b & 0xF0) >> 4;
            outStr += binaryArray[pos];
            //低四位
            pos = b & 0x0F;
            outStr += binaryArray[pos];
        }
        return outStr;
    }

    //16进制字符串转为二进制字符串
    public static String hexStr2BinaryStr(String src) {
        return bytes2BinaryStr(hexStr2Bytes(src));
    }

}
