package com.skt.ehs.mbs.fpbuilder.magfpbuilder;

import java.util.Date;

/**
 * Created by user on 2016-12-07.
 */
public class RawdataMagnetic {

    private String tag_id;
    private Date collected_time;
    private int sequence_num;
    private int sub_seq_num;
    private String package_collect_key;
    private boolean revision_collect;
    private int position_x;
    private int position_y;

    private double mag_bh;
    private double mag_bv;

    private int rssi_dir;
    private int north_dir;
    private int floor_id;
    private int building_id;
    private int campus_id;
    private String c_cell_id;

    public int getRssi_dir() {
        return rssi_dir;
    }

    public void setRssi_dir(int rssi_dir) {
        this.rssi_dir = rssi_dir;
    }

    public String getTag_id() {
        return tag_id;
    }

    public void setTag_id(String tag_id) {
        this.tag_id = tag_id;
    }

    public int getSequence_num() {
        return sequence_num;
    }

    public void setSequence_num(int sequence_num) {
        this.sequence_num = sequence_num;
    }

    public Date getCollected_time() {
        return collected_time;
    }

    public void setCollected_time(Date collected_time) {
        this.collected_time = collected_time;
    }

    public int getSub_seq_num() {
        return sub_seq_num;
    }

    public void setSub_seq_num(int sub_seq_num) {
        this.sub_seq_num = sub_seq_num;
    }

    public String getPackage_collect_key() {
        return package_collect_key;
    }

    public void setPackage_collect_key(String package_collect_key) {
        this.package_collect_key = package_collect_key;
    }

    public boolean isRevision_collect() {
        return revision_collect;
    }

    public void setRevision_collect(boolean revision_collect) {
        this.revision_collect = revision_collect;
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

    public double getMag_bh() {
        return mag_bh;
    }

    public void setMag_bh(double mag_bh) {
        this.mag_bh = mag_bh;
    }

    public double getMag_bv() {
        return mag_bv;
    }

    public void setMag_bv(double mag_bv) {
        this.mag_bv = mag_bv;
    }


    public int getNorth_dir() {
        return north_dir;
    }

    public void setNorth_dir(int north_dir) {
        this.north_dir = north_dir;
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

    public String getC_cell_id() {
        return c_cell_id;
    }

    public void setC_cell_id(String c_cell_id) {
        this.c_cell_id = c_cell_id;
    }

    @Override
    public String toString() {
        return "RawDataTag{" +
                "tag_id='" + tag_id + '\'' +
                ", sequence_num=" + sequence_num +
                ", sub_seq_num=" + sub_seq_num +
                ", package_collect_key='" + package_collect_key + '\'' +
                ", revision_collect=" + revision_collect +
                ", position_x=" + position_x +
                ", position_y=" + position_y +
                ", mag_bh=" + mag_bh +
                ", mag_bv=" + mag_bv +

                ", north_dir=" + north_dir +
                ", floor_id=" + floor_id +
                ", building_id=" + building_id +
                ", campus_id=" + campus_id +
                ", c_cell_id='" + c_cell_id + '\'' +
                '}';
    }

}
