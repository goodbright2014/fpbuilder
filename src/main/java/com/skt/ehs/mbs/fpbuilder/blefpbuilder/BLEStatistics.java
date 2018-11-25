package com.skt.ehs.mbs.fpbuilder.blefpbuilder;

/**
 * Created by user on 2016-12-16.
 */
public class BLEStatistics {

    private int floor_id;
    private int building_id;
    private int campus_id;
    private int ble_cell_id;
    private int rssi_dir;
    private String ap_id;
    private int rssi_collect_cnt;
    private double rssi_min;
    private double rssi_max;
    private double rssi_avg;
    private double rssi_std_dev;

    public BLEStatistics(int floor, int building, int campus, int cell_id, int rssi_dir, String ap_id, int count, double min, double max, double average, double stddev) {

        this.floor_id = floor;
        this.building_id = building;
        this.campus_id = campus;
        this.ble_cell_id = cell_id;
        this.rssi_dir = rssi_dir;
        this.ap_id = ap_id;
        this.rssi_collect_cnt = count;
        this.rssi_min = min;
        this.rssi_max = max;
        this.rssi_avg = average;
        this.rssi_std_dev = stddev;
    }

    @Override
    public String toString() {
        return "FPElement{" +
                "floor_id=" + floor_id +
                ", building_id=" + building_id +
                ", campus_id=" + campus_id +
                ", ble_cell_id=" + ble_cell_id +
                ", rssi_dir=" + rssi_dir +
                ", ap_id='" + ap_id + '\'' +
                ", rssi_collect_cnt=" + rssi_collect_cnt +
                ", rssi_min=" + rssi_min +
                ", rssi_max=" + rssi_max +
                ", rssi_avg=" + rssi_avg +
                ", rssi_std_dev=" + rssi_std_dev +
                '}';
    }

}
