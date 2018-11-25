package com.skt.ehs.mbs.fpbuilder.blefpbuilder;

/**
 * Created by user on 2017-01-23.
 */
public class _magnetic_direction {
    int north_dir;
    int rssi_dir;

    public int getN_dir() {
        return north_dir;
    }

    public void setN_dir(int n_dir) {
        this.north_dir = n_dir;
    }

    public int getRssi_dir() {
        return rssi_dir;
    }

    public void setRssi_dir(int rssi_dir) {
        this.rssi_dir = rssi_dir;
    }

    @Override
    public String toString() {
        return "_magnetic_direction{" +
                "n_dir=" + north_dir +
                ", rssi_dir=" + rssi_dir +
                '}';
    }
}
