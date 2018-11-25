package com.skt.ehs.mbs.fpbuilder.blefpbuilder;

/**
 * Created by user on 2016-12-16.
 */
public class Floor_BLE {

    private int bcm_M;  // M value of bcm
    private int bcm_N;  // N value of bcm
    private _ble_cell[][] bcm;  // ble cell matrix

    // Todo 수집방향
    private final int[] rssi_dir = new int[]{1000000, 2000000, 3000000, 4000000, 5000000, 6000000, 7000000, 8000000};

    private int ble_cell_size;
    private int mag_cell_size;

    // later , may need these data be treated in one map object.......
    private double map_scale_pixel;
    private int campus_id;
    private int building_id;
    private int floor_id;


    public Floor_BLE(int M, int N, int sizeOfCell, int magCellSize, int campus, int building, int floor, double scale) {
        this.bcm_M = M;
        this.bcm_N = N;
        this.bcm = new _ble_cell[M][N];
        this.ble_cell_size = sizeOfCell;
        this.mag_cell_size = magCellSize;
        this.campus_id = campus;
        this.building_id = building;
        this.floor_id = floor;
        this.map_scale_pixel = scale;
    }

    public int getCellID(int M, int N) {
        return bcm[M][N].get_id();
    }

    public void showCellID() {
        for(int j=0;j< bcm_N;j++) {
            for(int i=0;i< bcm_M; i++) {
                if(bcm[i][j] != null)
                    System.out.printf("%9d ", bcm[i][j].get_id());
            }
            System.out.println();
        }
    }

    public void showCellLocation() {
        for(int j=0;j< bcm_N;j++) {
            for(int i=0;i< bcm_M; i++) {
                if(bcm[i][j] != null)
                    System.out.printf("("+bcm[i][j].get_location_x()+","+bcm[i][j].get_location_y()+")");
            }
            System.out.println();
        }
    }


    public int getM() {
        return bcm_M;
    }

    public void setM(int m) {
        bcm_M = m;
    }

    public int getN() {
        return bcm_N;
    }

    public void setN(int n) {
        bcm_N = n;
    }

    public _ble_cell getCell(int M, int N) {
        return bcm[M][N];
    }

    public void setCell(int M, int N, _ble_cell bleCell) {
        bcm[M][N] = bleCell;
    }

    public void setCellLocation(int M, int N, int x, int y) {
        bcm[M][N].set_location_x(x);
        bcm[M][N].set_location_y(y);
    }

    public int[] loadRSSIDir () {
        return rssi_dir;
    }


    public double getMap_scale_pixel() {
        return map_scale_pixel;
    }

    public void setMap_scale_pixel(double map_scale_pixel) {
        this.map_scale_pixel = map_scale_pixel;
    }

    public int getBle_cell_size() {
        return ble_cell_size;
    }

    public int getMag_cell_size() {
        return mag_cell_size;
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



}
