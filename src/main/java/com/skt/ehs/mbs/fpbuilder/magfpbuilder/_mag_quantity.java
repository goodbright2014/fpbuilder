package com.skt.ehs.mbs.fpbuilder.magfpbuilder;

/**
 * Created by user on 2016-12-09.
 */
public class _mag_quantity {
    private double mag_bh;
    private double mag_bv;

    public double get_bh() {
        return mag_bh;
    }

    public void set_bh(double _bh) {
        this.mag_bh = _bh;
    }

    public double get_bv() {
        return mag_bv;
    }

    public void set_bv(double _bv) {
        this.mag_bv = _bv;
    }

    @Override
    public String toString() {
        return "_mag_quantity{" +
                "_bh=" + mag_bh +
                ", _bv=" + mag_bv +
                '}';
    }
}
