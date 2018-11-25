package com.skt.ehs.mbs.fpbuilder.blefpbuilder;

/**
 * Created by user on 2017-01-18.
 */
public class BLEFpFloor {
    private int floor_id;
    private int building_id;
    private int campus_id;

    private double rssi_avg;
    private double rssi_std_dev;
    private int rssi_collect_cnt;

    private String ap_id;

    public BLEFpFloor(int floor_id, int building_id, int campus_id, double rssi_avg, double rssi_std_dev, int rssi_collect_cnt, String ap_id) {
        this.floor_id = floor_id;
        this.building_id = building_id;
        this.campus_id = campus_id;
        this.rssi_avg = rssi_avg;
        this.rssi_std_dev = rssi_std_dev;
        this.rssi_collect_cnt = rssi_collect_cnt;
        this.ap_id = ap_id;
    }
}
