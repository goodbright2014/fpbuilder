package com.skt.ehs.mbs.fpbuilder.magfpbuilder;

/**
 * Created by user on 2016-12-07.
 */
public class Floor_Magnetic {
    private _mag_cell[][] _mcm; // magnetic cell matrix
    private int mcm_M;   // M of M-by-N matrix
    private int mcm_N;   // N of M-by-N matrix

    private int size_cell_mag;
    private int size_cell_ble;

    private double scale_pixel_map;
    private int campus_id;
    private int building_id;
    private int floor_id;

    Floor_Magnetic(int M, int N, int size, int size_ble, int campus, int building, int floor , double scale) {
        this.mcm_M = M;
        this.mcm_N = N;
        this._mcm = new _mag_cell[M][N];
        this.size_cell_mag = size;
        this.size_cell_ble = size_ble;
        this.campus_id = campus;
        this.building_id = building;
        this.floor_id = floor;
        this.scale_pixel_map = scale;
    }

    public void setCell(int M, int N, _mag_cell magCell) {
        _mcm[M][N] = magCell;
    }

    public int getM() {
        return mcm_M;
    }

    public void setM(int m) {
        mcm_M = m;
    }

    public int getN() {
        return mcm_N;
    }

    public void setN(int n) {
        mcm_N = n;
    }

    public void setCellLocation(int M, int N, int x, int y) {
        _mcm[M][N].set_location_x(x);
        _mcm[M][N].set_location_y(y);
    }

    public double getScale_pixel_map() {
        return scale_pixel_map;
    }

    public void setScale_pixel_map(double scale_pixel_map) {
        this.scale_pixel_map = scale_pixel_map;
    }

    public int getSize_cell_mag() {
        return size_cell_mag;
    }

    public int getSize_cell_ble() { return size_cell_ble; }

    public _mag_cell getCell(int M, int N) {
        return _mcm[M][N];
    }

    public int getCampus_id() {
        return campus_id;
    }

    public int getBuilding_id() {
        return building_id;
    }

    public int getFloor_id() {
        return floor_id;
    }

    public void showCellID() {
        for(int j=0;j< mcm_N;j++) {
            for(int i=0;i< mcm_M; i++) {
                if(_mcm[i][j] != null)
                    System.out.printf("%9d ", _mcm[i][j].get_id());
            }
            System.out.println();
        }
    }

    public void showCellLocation() {
        for(int j=0;j< mcm_N;j++) {
            for(int i=0;i< mcm_M; i++) {
                System.out.printf("("+_mcm[i][j].get_location_x()+","+_mcm[i][j].get_location_y()+")");
            }
            System.out.println();
        }
    }

}
