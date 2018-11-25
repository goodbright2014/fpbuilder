package com.skt.ehs.mbs.fpbuilder.database;

import com.skt.ehs.mbs.fpbuilder.blefpbuilder.BLEFpCell;
import com.skt.ehs.mbs.fpbuilder.blefpbuilder._ble_indicator;
import com.skt.ehs.mbs.fpbuilder.blefpbuilder._magnetic_direction;
import com.skt.ehs.mbs.fpbuilder.magfpbuilder.CellStatistics;
import com.skt.ehs.mbs.fpbuilder.magfpbuilder.LinkFeatures;
import com.skt.ehs.mbs.fpbuilder.magfpbuilder.RawdataMagnetic;
import com.skt.ehs.mbs.fpbuilder.magfpbuilder._mag_quantity;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 2016-12-07.
 */
public interface FpBuilderDAO {

    // for BLEFpBuilder
    void updateScale(@Param("scale_pixel") double scale_pixel, @Param("map_id") int map_id);

    double selectScalePixel(HashMap<String, Integer> params);

    List<_ble_indicator> selectAPinCoordiwDir(@Param("campus_id") int campus_id, @Param("building_id") int building_id, @Param("floor_id") int floor_id,
                                              @Param("lower_rssi_dir") int lower_rssi_dir, @Param("upper_rssi_dir") int upper_rssi_dir,
                                              @Param("ble_cell_coordi_x1") int ble_cell_coordi_x1, @Param("ble_cell_coordi_y1") int ble_cell_coordi_y1,
                                              @Param("ble_cell_coordi_x2") int ble_cell_coordi_x2, @Param("ble_cell_coordi_y2") int ble_cell_coordi_y2);

    List<_ble_indicator> selectAPinCoordi(@Param("campus_id") int campus_id, @Param("building_id") int building_id, @Param("floor_id") int floor_id,
                                              @Param("ble_cell_coordi_x1") int ble_cell_coordi_x1, @Param("ble_cell_coordi_y1") int ble_cell_coordi_y1,
                                              @Param("ble_cell_coordi_x2") int ble_cell_coordi_x2, @Param("ble_cell_coordi_y2") int ble_cell_coordi_y2);

    List<_magnetic_direction> selectMagDatainCoordi(@Param("campus_id") int campus_id, @Param("building_id") int building_id, @Param("floor_id") int floor_id,
                                                 @Param("ble_cell_coordi_x1") int ble_cell_coordi_x1, @Param("ble_cell_coordi_y1") int ble_cell_coordi_y1,
                                                 @Param("ble_cell_coordi_x2") int ble_cell_coordi_x2, @Param("ble_cell_coordi_y2") int ble_cell_coordi_y2);


    List<_magnetic_direction> selectNDirinCoordi(@Param("campus_id") int campus_id, @Param("building_id") int building_id, @Param("floor_id") int floor_id,
                                                 @Param("ble_cell_coordi_x1") int ble_cell_coordi_x1, @Param("ble_cell_coordi_y1") int ble_cell_coordi_y1,
                                                 @Param("ble_cell_coordi_x2") int ble_cell_coordi_x2, @Param("ble_cell_coordi_y2") int ble_cell_coordi_y2);


    List<Integer> selectNDirinCoordiwDir(@Param("campus_id") int campus_id, @Param("building_id") int building_id, @Param("floor_id") int floor_id,
                                         @Param("lower_rssi_dir") int lower_rssi_dir, @Param("upper_rssi_dir") int upper_rssi_dir,
                                         @Param("ble_cell_coordi_x1") int ble_cell_coordi_x1, @Param("ble_cell_coordi_y1") int ble_cell_coordi_y1,
                                         @Param("ble_cell_coordi_x2") int ble_cell_coordi_x2, @Param("ble_cell_coordi_y2") int ble_cell_coordi_y2);

    List<Integer> selMagCellID(@Param("campus_id") int campus_id, @Param("building_id") int building_id, @Param("floor_id") int floor_id);

    void listInsertFPBLEData(Map<String, Object> map);

    void insertBLEFpFloor(Map<String, Object> map);


    void listInsertBLEFpCell(List<BLEFpCell> bleFpCellList);


    void deleteFpBleData(int cell_id);

    // for MagFpBuilder
    double getMapScale(@Param("campus_id") int campus_id, @Param("building_id") int building_id, @Param("floor_id") int floor_id);

    void updateMDirCount(@Param("campus_id") int campus_id, @Param("building_id") int building_id, @Param("floor_id") int floor_id, @Param("cellid") int cellid,@Param("mDirFactor") int mDirFactor);

    List<_mag_quantity> selectMagVH(@Param("campus_id") int campus_id, @Param("building_id") int building_id, @Param("floor_id") int floor_id, @Param("coord_x1") int coord_x1, @Param("coord_y1") int coord_y1, @Param("coord_x2") int coord_x2, @Param("coord_y2") int coord_y2);

    List<RawdataMagnetic> selectRawData(@Param("campus_id") int campus_id, @Param("building_id") int building_id, @Param("floor_id") int floor_id, @Param("coord_x1") int coord_x1, @Param("coord_y1") int coord_y1, @Param("coord_x2") int coord_x2, @Param("coord_y2") int coord_y2);

    void insert_fp_mag_data(CellStatistics fp_cell_item);

    void delete_fp_mag_data(int cell_id);

    List<String> selPackageKey(@Param("campus_id") int campus_id, @Param("building_id") int building_id, @Param("floor_id") int floor_id);

    List<RawdataMagnetic> selRawData(@Param("campus_id") int campus_id, @Param("building_id") int building_id,
                                     @Param("floor_id") int floor_id, @Param("package_collect_key") String package_collect_key);

    public int selectMapX(@Param("campus_id") int campus_id, @Param("building_id") int building_id,
                          @Param("floor_id") int floor_id);

    public int selectMapY(@Param("campus_id") int campus_id, @Param("building_id") int building_id,
                          @Param("floor_id") int floor_id);

    public double selectMapScale(@Param("campus_id") int campus_id, @Param("building_id") int building_id,
                                 @Param("floor_id") int floor_id);



    void delRawDataBLE(@Param("campus_id") int campus_id, @Param("building_id") int building_id,
                      @Param("floor_id") int floor_id);

    void delRawDataTag(@Param("campus_id") int campus_id, @Param("building_id") int building_id,
                      @Param("floor_id") int floor_id);




    void delFpBleData(@Param("campus_id") int campus_id, @Param("building_id") int building_id,
                  @Param("floor_id") int floor_id);

    void delFpBleCell(@Param("campus_id") int campus_id, @Param("building_id") int building_id,
                      @Param("floor_id") int floor_id);

    void delFpBleFloor(@Param("campus_id") int campus_id, @Param("building_id") int building_id,
                      @Param("floor_id") int floor_id);

    void delFpMagData(@Param("campus_id") int campus_id, @Param("building_id") int building_id,
                  @Param("floor_id") int floor_id);

    void delFpMagCluster(@Param("campus_id") int campus_id, @Param("building_id") int building_id,
                      @Param("floor_id") int floor_id);

    void insertPattern(List<LinkFeatures> pattern);

    void insertMagStat(List<CellStatistics> lst);

    List<String> selAP_ID(@Param("campus_id") int campus_id, @Param("building_id") int building_id, @Param("floor_id") int floor_id);

    List<Integer> selRSSI(@Param("campus_id") int campus_id, @Param("building_id") int building_id,
                                     @Param("floor_id") int floor_id, @Param("ap_id") String ap_id);

    List<Double> bh_07F5();
    List<Double> sql_dn07f5v();
    List<Double> bh_202f();
    List<Double> sql_dn202fv();
    List<String> query1702150115();
    List<CellStatistics> query1702150135();
    List<RawdataMagnetic> query1702151552();
    void query1702151921(@Param("campus_id") int campus_id, @Param("building_id") int building_id, @Param("floor_id") int floor_id, @Param("PACKAGE_COLLECT_KEY") String PACKAGE_COLLECT_KEY,@Param("C_CELL_ID") String C_CELL_ID);
    void query1702151938(@Param("campus_id") int campus_id, @Param("building_id") int building_id, @Param("floor_id") int floor_id, @Param("TAG_ID") String TAG_ID,@Param("SEQUENCE_NUM") int SEQUENCE_NUM);
    List<Double> dummySQL();

}

