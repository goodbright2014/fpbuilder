package com.skt.ehs.mbs.fpbuilder.magfpbuilder;

/**
 * Created by user on 2016-12-12.
 */
public class CellStatistics {
    private int floor_id;
    private int building_id;
    private int campus_id;
    private int mag_cell_id;
    private int ble_cell_id;
    private int position_x;
    private int position_y;
    private double bh_avg;
    private double bh_std_dev;
    private double bh_min;
    private double bh_max;
    private double bh_median;
    private double bv_avg;
    private double bv_std_dev;
    private double bv_min;
    private double bv_max;
    private double bv_median;
    private int collect_cnt;
    private int cell_size;
    private int ndir_norm_factor;

    public CellStatistics() {
    }

    public CellStatistics(int floor_id, int building_id, int campus_id, int mag_cell_id, int ble_cell_id, int position_x, int position_y, double bh_avg, double bh_std_dev, double bh_min, double bh_max, double bh_median, double bv_avg, double bv_std_dev, double bv_min, double bv_max, double bv_median, int collect_cnt, int cell_size, int ndir_norm_factor) {
        this.floor_id = floor_id;
        this.building_id = building_id;
        this.campus_id = campus_id;
        this.mag_cell_id = mag_cell_id;
        this.ble_cell_id = ble_cell_id;
        this.position_x = position_x;
        this.position_y = position_y;
        this.bh_avg = bh_avg;
        this.bh_std_dev = bh_std_dev;
        this.bh_min = bh_min;
        this.bh_max = bh_max;
        this.bh_median = bh_median;
        this.bv_avg = bv_avg;
        this.bv_std_dev = bv_std_dev;
        this.bv_min = bv_min;
        this.bv_max = bv_max;
        this.bv_median = bv_median;
        this.collect_cnt = collect_cnt;
        this.cell_size = cell_size;
        this.ndir_norm_factor = ndir_norm_factor;
    }

    public int getFloor_id() {
        return floor_id;
    }

    public void setFloor_id(int floor_id) {
        this.floor_id = floor_id;
    }

    public int getBuilding_id() {
        return building_id;
    }

    public void setBuilding_id(int building_id) {
        this.building_id = building_id;
    }

    public int getCampus_id() {
        return campus_id;
    }

    public void setCampus_id(int campus_id) {
        this.campus_id = campus_id;
    }

    public int getMag_cell_id() {
        return mag_cell_id;
    }

    public void setMag_cell_id(int mag_cell_id) {
        this.mag_cell_id = mag_cell_id;
    }

    public int getBle_cell_id() {
        return ble_cell_id;
    }

    public void setBle_cell_id(int ble_cell_id) {
        this.ble_cell_id = ble_cell_id;
    }

    public int getPosition_x() {
        return position_x;
    }

    public void setPosition_x(int position_x) {
        this.position_x = position_x;
    }

    public int getPosition_y() {
        return position_y;
    }

    public void setPosition_y(int position_y) {
        this.position_y = position_y;
    }

    public double getBh_avg() {
        return bh_avg;
    }

    public void setBh_avg(double bh_avg) {
        this.bh_avg = bh_avg;
    }

    public double getBh_std_dev() {
        return bh_std_dev;
    }

    public void setBh_std_dev(double bh_std_dev) {
        this.bh_std_dev = bh_std_dev;
    }

    public double getBh_min() {
        return bh_min;
    }

    public void setBh_min(double bh_min) {
        this.bh_min = bh_min;
    }

    public double getBh_max() {
        return bh_max;
    }

    public void setBh_max(double bh_max) {
        this.bh_max = bh_max;
    }

    public double getBh_median() {
        return bh_median;
    }

    public void setBh_median(double bh_median) {
        this.bh_median = bh_median;
    }

    public double getBv_avg() {
        return bv_avg;
    }

    public void setBv_avg(double bv_avg) {
        this.bv_avg = bv_avg;
    }

    public double getBv_std_dev() {
        return bv_std_dev;
    }

    public void setBv_std_dev(double bv_std_dev) {
        this.bv_std_dev = bv_std_dev;
    }

    public double getBv_min() {
        return bv_min;
    }

    public void setBv_min(double bv_min) {
        this.bv_min = bv_min;
    }

    public double getBv_max() {
        return bv_max;
    }

    public void setBv_max(double bv_max) {
        this.bv_max = bv_max;
    }

    public double getBv_median() {
        return bv_median;
    }

    public void setBv_median(double bv_median) {
        this.bv_median = bv_median;
    }

    public int getCollect_cnt() {
        return collect_cnt;
    }

    public void setCollect_cnt(int collect_cnt) {
        this.collect_cnt = collect_cnt;
    }

    public int getCell_size() {
        return cell_size;
    }

    public void setCell_size(int cell_size) {
        this.cell_size = cell_size;
    }

    public int getNdir_norm_factor() {
        return ndir_norm_factor;
    }

    public void setNdir_norm_factor(int ndir_norm_factor) {
        this.ndir_norm_factor = ndir_norm_factor;
    }
}
