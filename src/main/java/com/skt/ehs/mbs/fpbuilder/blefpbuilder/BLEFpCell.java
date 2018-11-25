package com.skt.ehs.mbs.fpbuilder.blefpbuilder;

/**
 * Created by user on 2017-01-17.
 */
public class BLEFpCell {
    private int campus_id;
    private int building_id;
    private int floor_id;
    private int ble_cell_id;
    private int rssi_dir;
    private int position_x;
    private int position_y;
    private int cell_size;
    private int collect_cnt;
    private double n_dir_avg;
    private double n_dir_stddev;

    public BLEFpCell(int campus_id, int building_id,int floor_id,int ble_cell_id, int rssi_dir, int position_x, int position_y, int cell_size, int collect_cnt, double n_dir_avg, double n_dir_stddev) {
        this.campus_id = campus_id;
        this.building_id = building_id;
        this.floor_id = floor_id;
        this.ble_cell_id = ble_cell_id;
        this.rssi_dir = rssi_dir;
        this.position_x = position_x;
        this.position_y = position_y;
        this.cell_size = cell_size;
        this.collect_cnt = collect_cnt;
        this.n_dir_avg = n_dir_avg;
        this.n_dir_stddev = n_dir_stddev;
    }

    @Override
    public String toString() {
        return "BLEFpCell{" +
                "ble_cell_id=" + ble_cell_id +
                ", rssi_dir=" + rssi_dir +
                ", position_x=" + position_x +
                ", position_y=" + position_y +
                ", cell_size=" + cell_size +
                ", collect_cnt=" + collect_cnt +
                ", n_dir_avg=" + n_dir_avg +
                ", n_dir_stddev=" + n_dir_stddev +
                '}';
    }
}
