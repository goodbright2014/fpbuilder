package com.skt.ehs.mbs.fpbuilder.magfpbuilder;

/**
 * Created by user on 2017-01-16.
 */
public class LinkFeatures {
    private int floor_id;
    private int building_id;
    private int campus_id;
    private int cluster_id;
    private int seqnum;
    private int mag_cell_id;
    private double bh;
    private double bv;

    public LinkFeatures(int floor_id, int building_id, int campus_id, int cluster_id, int seqnum, int mag_cell_id, double bh, double bv) {
        this.floor_id = floor_id;
        this.building_id = building_id;
        this.campus_id = campus_id;
        this.cluster_id = cluster_id;
        this.seqnum = seqnum;
        this.mag_cell_id = mag_cell_id;
        this.bh = bh;
        this.bv = bv;
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

    public int getCluster_id() {
        return cluster_id;
    }

    public void setCluster_id(int cluster_id) {
        this.cluster_id = cluster_id;
    }

    public int getSeqnum() {
        return seqnum;
    }

    public void setSeqnum(int seqnum) {
        this.seqnum = seqnum;
    }

    public int getMag_cell_id() {
        return mag_cell_id;
    }

    public void setMag_cell_id(int mag_cell_id) {
        this.mag_cell_id = mag_cell_id;
    }

    public double getBh() {
        return bh;
    }

    public void setBh(double bh) {
        this.bh = bh;
    }

    public double getBv() {
        return bv;
    }

    public void setBv(double bv) {
        this.bv = bv;
    }

    @Override
    public String toString() {
        return "LinkFeatures{" +
                "floor_id=" + floor_id +
                ", building_id=" + building_id +
                ", campus_id=" + campus_id +
                ", cluster_id=" + cluster_id +
                ", seqnum=" + seqnum +
                ", mag_cell_id=" + mag_cell_id +
                ", bh=" + bh +
                ", bv=" + bv +
                '}';
    }
}
