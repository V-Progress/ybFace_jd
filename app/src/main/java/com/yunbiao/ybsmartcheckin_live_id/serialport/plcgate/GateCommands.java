package com.yunbiao.ybsmartcheckin_live_id.serialport.plcgate;

/**
 * Created by chen on 2019/4/3.
 */

public class GateCommands {

    public static final int STATUS_READ_TIMEOUT = 601;

    //开门指令
    public static final String GATE_OPEN_DOOR = "9901000120000D0A";

    //ok
    public static final String GATE_CONNECT_OK = "6F6B";

    //接收：开
    public static final String GATE_DOOR_ON = "6F6E0A";

    //接收：关
    public static final String GATE_DOOR_OFF = "6F66660A";

}
