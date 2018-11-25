package com.skt.ehs.mbs.fpbuilder.blefpbuilder;

/**
 * Created by user on 2016-12-16.
 */
public class _ble_cell {

    private int _id;
    private int _location_x;
    private int _location_y; // 실거리 ??

    _ble_cell(int id) {
        _id = id;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int ble_cell_id) {
        this._id = ble_cell_id;
    }

    public int get_location_x() {
        return _location_x;
    }

    public void set_location_x(int location_x) {
        this._location_x = location_x;
    }

    public int get_location_y() {
        return _location_y;
    }

    public void set_location_y(int location_y) {
        this._location_y = location_y;
    }

    @Override
    public String toString() {
        return "_ble_cell{" +
                "_id=" + _id +
                ", _location_x=" + _location_x +
                ", _location_y=" + _location_y +
                '}';
    }

}
