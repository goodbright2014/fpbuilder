package com.skt.ehs.mbs.fpbuilder.blefpbuilder;

/**
 * Created by user on 2016-12-16.
 */
public class _ble_indicator {

    String ap_id;
    int rssi;
    int rssi_dir;


    public String get_ap_id() {
        return ap_id;
    }
    public int get_rssi() {
        return rssi;
    }

    public int getRssi_dir() {
        return rssi_dir;
    }


    @Override
    public String toString() {
        return "_ble_indicator{" +
                "ap_id='" + ap_id + '\'' +
                ", rssi=" + rssi +
                ", rssi_dir=" + rssi_dir +
                '}';
    }
}
