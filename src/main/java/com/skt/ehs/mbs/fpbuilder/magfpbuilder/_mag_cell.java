package com.skt.ehs.mbs.fpbuilder.magfpbuilder;

/**
 * Created by user on 2016-12-07.
 */
public class _mag_cell {
    private int _id;
    private int _location_x;
    private int _location_y;


    _mag_cell(int id) {
        _id = id;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int get_location_x() {
        return _location_x;
    }

    public void set_location_x(int _location_x) {
        this._location_x = _location_x;
    }

    public int get_location_y() {
        return _location_y;
    }

    public void set_location_y(int _location_y) {
        this._location_y = _location_y;
    }

}
