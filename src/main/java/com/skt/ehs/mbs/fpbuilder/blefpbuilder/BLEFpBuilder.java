package com.skt.ehs.mbs.fpbuilder.blefpbuilder;

import com.skt.ehs.mbs.fpbuilder.database.FpBuilderDAO;
import com.skt.ehs.mbs.fpbuilder.database.MariaConnectionManager;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by user on 2016-12-16.
 */
public class BLEFpBuilder {

    final Logger logger = LoggerFactory.getLogger(BLEFpBuilder.class);

    Floor_BLE bFloor;

    public BLEFpBuilder(int M, int N, int sizeOfCell, int magCellSize ,int campus, int building, int floor) {
        double scale = readMapScale(campus, building, floor);
        bFloor = new Floor_BLE(M,N,sizeOfCell, magCellSize, campus, building, floor, scale);
    }

    public void LoadCellID() {
        for (int x = 1 ; x <= bFloor.getM(); x++) {

            int id = 0;
            int id_x = 1000 * x;
            for (int y = 1; y <= bFloor.getN(); y++) {
                id = id_x + y;
                _ble_cell bleCell= new _ble_cell(id);
                bFloor.setCell(x-1,y-1,bleCell);
            }
        }
        bFloor.showCellID();
    }

/*
    public void LoadCellLocation() {
        int location_x = 0;
        int location_y = 0;
        int cell_size = bFloor.getBle_cell_size();

        for (int x = 1 ; x <= bFloor.getM(); x++) {
            location_x =  (cell_size * x) - (cell_size / 2);
            for (int y = 1; y <= bFloor.getN(); y++) {
                location_y = (cell_size * y) - (cell_size / 2);
                bFloor.setCellLocation(x-1,y-1,location_x,location_y);
            }
        }
        bFloor.showCellLocation();
    }
*/


    public void LoadCellLocation() {

        int ble_cell_id;
        int location_x;
        int location_y;

        // 먼저 최소단위인 픽셀단위 지자기셀의 크기, pixels_per_cell
        //System.out.println("getMap_scale_pixel : "+ bFloor.getMap_scale_pixel());
        //System.out.println("getMag_cell_size : "+ bFloor.getMag_cell_size() );

        int pixels_per_cell = (int) Math.round(bFloor.getMag_cell_size() / bFloor.getMap_scale_pixel());  // 휴빌론의 경우 21 픽셀, SKT 의 경우 17 픽셀

        //System.out.println("pixels_per_cell : "+ pixels_per_cell);

        int object_cell_size_ble = bFloor.getBle_cell_size();
        int object_cell_size_mag = bFloor.getMag_cell_size();

        int quotient = Math.round(object_cell_size_ble / object_cell_size_mag);  // 정수배라고 가정
        //System.out.println("quotient : "+ quotient);

        for (int m = 0; m < bFloor.getM(); m++) {
            for (int n = 0; n < bFloor.getN(); n++) {  // 2.격자별....

                ble_cell_id = bFloor.getCell(m, n).get_id();
                //System.out.println("ble_cell_id : "+ ble_cell_id);


                // 두번째로 현재 BLE 셀이 커버하는 지자기셀을 구한다 . rectangle 의 시작 지자기셀 위치와 마지막 지자기셀의 좌표로 부터 BLE 셀의 좌표를 구한다
                List<Integer> mag_cell_id_list = CalculateMagCellID(ble_cell_id);

                int s = mag_cell_id_list.get(0);
                int e = mag_cell_id_list.get(mag_cell_id_list.size() - 1);

                location_x = (int)Math.round(((s / 10000 - 1) * pixels_per_cell + 1 +  (( quotient / 2 ) * pixels_per_cell)) * bFloor.getMap_scale_pixel());
                //location_y = (int)Math.round((( s % 10000 -1) * pixels_per_cell) +  (( quotient / 2 ) * pixels_per_cell)  *bFloor.getMap_scale_pixel());
                //System.out.println("location_x : "+ location_x);
                location_y =  (int)Math.round(((( s % 10000 -1 ) * pixels_per_cell) +   (( quotient / 2 ) * pixels_per_cell) + 1) * bFloor.getMap_scale_pixel());
                //System.out.println("location_y : "+ location_y);

                bFloor.setCellLocation(m,n,location_x,location_y);
            }
        }
        bFloor.showCellLocation();
    }


    private static double readMapScale(int campus, int building, int floor) {
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();
        double scale= 0;

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);

            scale = mapper.getMapScale(campus, building, floor);

        }
        return scale;
    }

    private static void insertFPCellPerAP(List<BLEStatistics> fp_cell_per_ap) {
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();

        // transform list to map to insert list of object
        Map<String, Object> map = new HashMap<>();
        map.put("fp_cell_per_ap", fp_cell_per_ap);

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);

            mapper.listInsertFPBLEData(map);
            session.commit();

        }
    }


    private static void deleteFPCell(int cell_id) {
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);

            mapper.deleteFpBleData(cell_id);
            session.commit();

        }
    }

    private List<_ble_indicator> get_aplist_in_coordinate_per_rssidir (int c, int b, int f, int d1, int d2, int x1, int y1, int x2, int y2) {
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();
        List<_ble_indicator> ret;

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            ret = mapper.selectAPinCoordiwDir(c,b,f,d1,d2,x1,y1,x2, y2);
        }
        return ret;
    }


    private List<_ble_indicator> get_aplist_in_coordinate (int c, int b, int f, int x1, int y1, int x2, int y2) {
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();
        List<_ble_indicator> ret;

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            ret = mapper.selectAPinCoordi(c,b,f,x1,y1,x2, y2);
        }
        return ret;
    }
    private class rssiDirBound {
        private int lower_rssi_dir;
        private int upper_rssi_dir;
        private int _rssi_dir;

        rssiDirBound(int rssi_dir, int lower, int upper) {
            _rssi_dir = rssi_dir;
            lower_rssi_dir = lower;
            upper_rssi_dir = upper;
        }

        public int get_rssi_dir() {
            return _rssi_dir;
        }

        public int get_lower_rssi_dir() {
            return lower_rssi_dir;
        }

        public int get_upper_rssi_dir() {
            return upper_rssi_dir;
        }
    }


    private rssiDirBound normalizeRssiDir(int rssi_dir) {
        rssiDirBound ret = null;
        switch (rssi_dir) {
            case 2000000: // EAST
                ret = new rssiDirBound(rssi_dir, -23, 23);
                break;
            case 8000000: // North East
                ret = new rssiDirBound(rssi_dir,22, 68);
                break;
            case 4000000: //North
                ret = new rssiDirBound(rssi_dir,67, 113);
                break;
            case 6000000: //North West
                ret = new rssiDirBound(rssi_dir,112, 158);
                break;
            case 1000000: //WEST
                ret = new rssiDirBound(rssi_dir,157, -157);
                break;
            case 7000000: //South West
                ret = new rssiDirBound(rssi_dir,-158, -112);
                break;
            case 3000000: // South
                ret = new rssiDirBound(rssi_dir,-113, -67);
                break;
            case 5000000: // South East
                ret = new rssiDirBound(rssi_dir,-68, -22);
                break;
            default:
                break;
        }
        return ret;
    }


    // fp_ble_cell 을 구축한다. BLE CELL 별 정보
    // 수집방향별 세트 구성후 , BLE Cell 별 통계를 집계하는 경우 , 수집방향 갯수 x BLE 셀 갯수 만큼의 DB access 가 발생하므로 ,
    // BLE 셀 단위로 Raw Data 를 메모리로 Loding 후 , 수집방향별 집계하는 순서로 재구현한다.
    public boolean build_fp_ble_cell2() throws Throwable {
        int object_campus_id = bFloor.getCampus_id();
        int object_building_id = bFloor.getBuilding_id();
        int object_floor_id = bFloor.getFloor_id();
        int object_cell_size = bFloor.getBle_cell_size();

        int ble_cell_id;

        double scale_pixel = readMapScale(object_campus_id, object_building_id, object_floor_id);  // 수집시 사용한 지도의 축척정보
        bFloor.setMap_scale_pixel(scale_pixel);

        double object_scale_pixel;
        if((object_scale_pixel = bFloor.getMap_scale_pixel())==0) {
            System.out.println("지도정보가 알맞게 데이터베이스에 수록되어있는지 관리자에게 문의하세요");
            throw  new NullPointerException();
        }

        List<BLEFpCell> bleFpCellList= new ArrayList<>();
        BLEFpCell e;
        int[] rssi_dir_index = bFloor.loadRSSIDir();
        rssiDirBound rssi_dir;

        for (int m = 0; m < bFloor.getM(); m++) {
            for (int n = 0; n < bFloor.getN(); n++) {  // 격자별....

                ble_cell_id = bFloor.getCell(m, n).get_id();

                // 먼저 최소단위인 픽셀단위 지자기셀의 크기, pixels_per_cell
                int pixels_per_cell = (int) Math.round(bFloor.getMag_cell_size() / bFloor.getMap_scale_pixel());  // 휴빌론의 경우 21 픽셀
                                                                                                                      // SKT 의 경우 17 픽셀

                // 두번째로 현재 BLE 셀이 커버하는 지자기셀을 구한다 - BLE 셀의 coordinate 를 구하는 데 있어서, 축척을 고려한 pixel 단위의 최소거리인 지자기셀의 크기를
                // 기준으로 BLE 셀의 (픽셀단위)크기 및 픽셀단위 좌표값을 산출하기 위해, 먼저 현재 BLE 셀의 범위가 포함하고 있는 지자기셀들의 ID를 구한다.
                //
                List<Integer> mag_cell_id_list = CalculateMagCellID(ble_cell_id);

                int start = mag_cell_id_list.get(0);
                int end = mag_cell_id_list.get(mag_cell_id_list.size() - 1);
                int coordinate_pixel_x1 = (start / 10000 - 1) * pixels_per_cell;
                int coordinate_pixel_y1 = (start % 10000 - 1) * pixels_per_cell;
                int coordinate_pixel_x2 = end / 10000 * pixels_per_cell;
                int coordinate_pixel_y2 = end % 10000 * pixels_per_cell;

                //
                // BLE Cell 의 Location 정보... Pixel 단위로 전환한다면 Mag Cell 의 Pixel 단위 크기를 최소단위로 하여 Mag Cell 의 갯수만큼 합산하여 산출하도록 한다.
                //

                int cell_location_x = bFloor.getCell(m, n).get_location_x();
                int cell_location_y = bFloor.getCell(m, n).get_location_y();


                //double N_Dir_avg
                List<_magnetic_direction> magnetic_direction_list = get_N_Dir_in_coordinate(object_campus_id, object_building_id, object_floor_id,
                        coordinate_pixel_x1, coordinate_pixel_y1, coordinate_pixel_x2, coordinate_pixel_y2);
                // FP_BLE_CELL 에 수록할 magnetic data collection count 를 산출하기 위한 리스트
                List<_magnetic_direction> magnetic_direction_list2 = get_MagData_in_coordinate(object_campus_id, object_building_id, object_floor_id,
                        coordinate_pixel_x1, coordinate_pixel_y1, coordinate_pixel_x2, coordinate_pixel_y2);

                //System.out.println(magnetic_direction_list);

                for (int i =0; i < rssi_dir_index.length ; i++ ) {  // 수집방향별로 세트를 구성
                    rssi_dir = normalizeRssiDir(rssi_dir_index[i]);
                    final int lowBound = rssi_dir.get_lower_rssi_dir();
                    final int upperBound = rssi_dir.get_upper_rssi_dir();
                    List<Integer> N_Dir_List=null;
                    int mag_collect_count=0;
                    if (rssi_dir.get_rssi_dir() == 1000000) {

                        //System.out.println("ssi_dir_index== 1000000 => ble_cell_id : "+ble_cell_id);
                        //magnetic_direction_list.forEach(System.out::println);
                        N_Dir_List = magnetic_direction_list.stream().filter(x -> ((x.getRssi_dir() > lowBound) &&(x.getRssi_dir()  <= 180)) || ((x.getRssi_dir()  >= -180) && (x.getRssi_dir() < upperBound)))
                                .map(x -> x.getN_dir()).collect(Collectors.toList());
                        //System.out.println("result of rssi_dir=1000000");
                        //N_Dir_List.forEach(System.out::println);
                        //final int icount = N_Dir_List.size();
                        //if(icount>0) {
                        //    System.out.println("get_rssi_dir : "+rssi_dir.get_rssi_dir());
                        //    System.out.println("COUNT : "+icount);
                        //}
                        //System.out.println();
                        //System.out.println();
                        mag_collect_count = (int) (magnetic_direction_list2.stream()
                                .filter(x -> ((x.getRssi_dir() > lowBound) &&(x.getRssi_dir()  <= 180)) || ((x.getRssi_dir()  >= -180) && (x.getRssi_dir() < upperBound)))
                                .count());
                    }
                    if (rssi_dir.get_rssi_dir() == 2000000) {
                        N_Dir_List = magnetic_direction_list.stream().filter(x -> ((x.getRssi_dir() > lowBound) &&(x.getRssi_dir()  <= 0)) || ((x.getRssi_dir()  >= 0) && (x.getRssi_dir() < upperBound)))
                                .map(x -> x.getN_dir()).collect(Collectors.toList());
                        mag_collect_count = (int) (magnetic_direction_list2.stream()
                                .filter(x -> ((x.getRssi_dir() > lowBound) &&(x.getRssi_dir()  <= 0)) || ((x.getRssi_dir()  >= 0) && (x.getRssi_dir() < upperBound)))
                                .count());

                    } else if (rssi_dir.get_rssi_dir() == 3000000 || rssi_dir.get_rssi_dir() == 4000000 || rssi_dir.get_rssi_dir() == 5000000 || rssi_dir.get_rssi_dir() == 6000000
                            || rssi_dir.get_rssi_dir() == 7000000 || rssi_dir.get_rssi_dir() == 8000000) {

                        N_Dir_List = magnetic_direction_list.stream().filter(x -> (x.getRssi_dir() > lowBound) && (x.getRssi_dir() < upperBound))
                                .map(x -> x.getN_dir()).collect(Collectors.toList());

                        mag_collect_count = (int) (magnetic_direction_list2.stream()
                                .filter(x -> (x.getRssi_dir() > lowBound) && (x.getRssi_dir() < upperBound))
                                .count());

                    }
                    //N_Dir_List.forEach(System.out::println);


                    final int count = N_Dir_List.size();
                    //if(count>0) {
                    //    System.out.println("get_rssi_dir : "+rssi_dir.get_rssi_dir());
                    //    System.out.println("COUNT : "+count);
                    //}


                    // 지자기방향의 평균을 구하는데 있어서 , 규격정의서_Smart_EHS_Ready_v0.9.6 를 따른다
                    //final double N_Dir_avg_rad =  N_Dir_List.stream().map(x->x-180).map(x -> Math.toRadians(x)).mapToDouble(x -> x).summaryStatistics().getAverage();
                    //final int N_Dir_avg = (int) Math.round(Math.toDegrees(N_Dir_avg_rad));
                    //System.out.println("N_Dir_avg : " + N_Dir_avg);

                   // final int N_Dir_avg = (int) Math.round(Math.toDegrees(N_Dir_avg_rad) + 180);
                    List<Double> list = new ArrayList<Double>();
/*
                    if (count > 0) {
                        System.out.println();
                        System.out.println();
                        System.out.println("ble_cell_id : " + ble_cell_id + " rssi_dir : " + rssi_dir.get_rssi_dir());
                        N_Dir_List.forEach(System.out::println);

                    }
*/

                    N_Dir_List.forEach(x -> list.add(Double.valueOf(x)));
                    final double N_Dir_avg = getMeanAngle(list);     // getMeanAngle() 은 atan2()의 결과값이므로 -180~0, 0~180 의 범위를 갖는다

                    // final double sum_of_dev = N_Dir_List.stream().mapToDouble(x -> Math.pow(x.doubleValue() - N_Dir_avg, 2.0)).sum();

                    /*
                    final double sum_of_dev = N_Dir_List.stream()
                            .mapToDouble(angle2 -> {
                                //각도가  0 ~ 360 사이의 값이 나오게 360 더하는 과정
                                if(temp_avg[0] < 0.0f) temp_avg[0] += 360;
                                if(angle2 < 0.0f) angle2 += 360;

                                double diff = angle2 - temp_avg[0];  //두 각도 사이의 차이 구함
                                if(diff > 180)
                                    diff -= 360;
                                else if(diff < -180)
                                    diff += 360;

                                return diff;

                            })
                            .sum();
                    */

                    //final double sum_of_dev = N_Dir_List.stream().mapToDouble(x -> Math.pow(x.doubleValue() - N_Dir_avg, 2.0)).sum();

                    final double sum_of_dev = N_Dir_List.stream()
                                               .mapToDouble(x -> {
                                                   return Math.pow(getDiffAngle( to180Degree(x),(int)Math.round(N_Dir_avg) ),2.0);
                                               })
                                               .sum();

                    final double N_Dir_std_dev = Math.sqrt(sum_of_dev / (count - 1));  // Bessel correction for sample stdev
                    //System.out.println("get_rssi_dir : "+rssi_dir.get_rssi_dir());


                    if ( count > 0 ) {
                        e = new BLEFpCell(object_campus_id, object_building_id, object_floor_id, ble_cell_id, rssi_dir.get_rssi_dir(),
                                cell_location_x, cell_location_y, object_cell_size, mag_collect_count, to360Degree((int)Math.round(N_Dir_avg)), N_Dir_std_dev);

                        bleFpCellList.add(e);
                    }
                    //System.out.println();
                    //System.out.println();
                    //System.out.println();
                }
            }
        }
        // ToDo store to DB
        if(bleFpCellList.size() > 0)
            bleFpCell(bleFpCellList);

        return false;
    }

    public double getMeanAngle(List<Double> sample) {

        double x_component = 0.0;
        double y_component = 0.0;
        double avg_d, avg_r;

        for (double angle_d : sample) {
            double angle_r;
            angle_r = Math.toRadians(angle_d);
            x_component += Math.cos(angle_r);
            y_component += Math.sin(angle_r);
        }
        x_component /= sample.size();
        y_component /= sample.size();
        avg_r = Math.atan2(y_component, x_component);
        avg_d = Math.toDegrees(avg_r);

        return avg_d;
    }

    public double getDiffAngle(double  angle1 , double angle2)
    {
        //각도가  0 ~ 360 사이의 값이 나오게 360 더하는 과정
        if(angle1 < 0.0f) angle1 += 360;
        if(angle2 < 0.0f) angle2 += 360;

        double diff = angle2 - angle1;  //두 각도 사이의 차이 구함
        if(diff > 180)
            diff -= 360;
        else if(diff < -180)
            diff += 360;

        return diff;
    }
    // fp_ble_cell 을 구축한다. BLE CELL 별 정보
    public boolean build_fp_ble_cell() throws Throwable {

        int object_campus_id = bFloor.getCampus_id();
        int object_building_id = bFloor.getBuilding_id();
        int object_floor_id = bFloor.getFloor_id();
        int object_cell_size = bFloor.getBle_cell_size();

        int ble_cell_id;

        double scale_pixel = readMapScale(object_campus_id, object_building_id, object_floor_id);  // 수집시 사용한 지도의 축척정보
        bFloor.setMap_scale_pixel(scale_pixel);

        double object_scale_pixel;
        if((object_scale_pixel = bFloor.getMap_scale_pixel())==0) {
            System.out.println("지도정보가 알맞게 데이터베이스에 수록되어있는지 관리자에게 문의하세요");
            throw  new NullPointerException();
        }

        List<BLEFpCell> bleFpCellList= new ArrayList<>();
        BLEFpCell e;
        int[] rssi_dir_index = bFloor.loadRSSIDir();
        rssiDirBound rssi_dir;

        for (int i =0; i < rssi_dir_index.length ; i++ ) {  // 1.수집방향별로 세트를 구성
            rssi_dir = normalizeRssiDir(rssi_dir_index[i]);

            for (int m = 0; m < bFloor.getM(); m++) {
                for (int n = 0; n < bFloor.getN(); n++) {  // 2.격자별....


                    ble_cell_id = bFloor.getCell(m, n).get_id();


                    System.out.printf("campus_id : %d, building_id : %d, floor_id : %d,  ble_cell_id : %d ",
                            object_campus_id, object_building_id, object_floor_id, ble_cell_id);

                    System.out.printf(" ble_cell_location : (" + bFloor.getCell(m, n).get_location_x() + ", " + bFloor.getCell(m, n).get_location_y() + ")");
                    System.out.println();

                    int cell_location_x = bFloor.getCell(m, n).get_location_x();
                    int cell_location_y = bFloor.getCell(m, n).get_location_y();

                    int coordinate_pixel_x1 = (cell_location_x - object_cell_size / 2) / (int) object_scale_pixel;
                    int coordinate_pixel_y1 = (cell_location_y - object_cell_size / 2) / (int) object_scale_pixel;
                    int coordinate_pixel_x2 = (cell_location_x + object_cell_size / 2) / (int) object_scale_pixel;
                    int coordinate_pixel_y2 = (cell_location_y + object_cell_size / 2) / (int) object_scale_pixel;

                    System.out.printf("Pixel Coordinate : (" + coordinate_pixel_x1 + ", " + coordinate_pixel_y1 + ") (" + coordinate_pixel_x2 + ", " + coordinate_pixel_y2 + ")");
                    System.out.println();

                    //double N_Dir_avg
                    List<Integer> N_Dir_List = get_N_Dir_in_coordinate_per_rssidir(object_campus_id, object_building_id, object_floor_id,
                            rssi_dir.get_lower_rssi_dir(), rssi_dir.get_upper_rssi_dir(),coordinate_pixel_x1, coordinate_pixel_y1, coordinate_pixel_x2, coordinate_pixel_y2);

                    System.out.println(N_Dir_List);

                    final int count = N_Dir_List.size();

                    // 지자기방향의 평균을 구하는데 있어서 , 규격정의서_Smart_EHS_Ready_v0.9.6 를 따른다
                    final double N_Dir_avg_rad = N_Dir_List.stream().map(x -> x - 180).map(x -> Math.toRadians(x)).mapToDouble(x -> x).summaryStatistics().getAverage();
                    final int N_Dir_avg = (int) Math.round(Math.toDegrees(N_Dir_avg_rad) + 180);
                    System.out.println("N_Dir_avg : " + N_Dir_avg);

                    final double sum_of_dev = N_Dir_List.stream().mapToDouble(x -> Math.pow(x.doubleValue() - N_Dir_avg, 2.0)).sum();
                    final double N_Dir_std_dev = Math.sqrt(sum_of_dev / (count - 1));  // Bessel correction for sample stdev
                    System.out.println("N_Dir_std_dev : " + N_Dir_std_dev);

                    if ( count > 0 ) {
                        e = new BLEFpCell(object_campus_id, object_building_id, object_floor_id, ble_cell_id, rssi_dir.get_rssi_dir(), cell_location_x, cell_location_y, object_cell_size, count, N_Dir_avg, N_Dir_std_dev);

                        bleFpCellList.add(e);
                    }

                }
            }
        }

        bleFpCellList.forEach(System.out::println);

        // ToDo store to DB
        if(bleFpCellList.size() > 0)
            bleFpCell(bleFpCellList);

        return false;
    }


    private static void bleFpCell(List<BLEFpCell> bleFpCellList)  throws Throwable {

        MariaConnectionManager mngr = MariaConnectionManager.getInstance();

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            mapper.listInsertBLEFpCell(bleFpCellList);
            session.commit();
        } catch (PersistenceException e) {
            throw e.getCause();
        }
    }

    private List<_magnetic_direction> get_MagData_in_coordinate (int c, int b, int f, int x1, int y1, int x2, int y2) {
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();
        List<_magnetic_direction> ret;

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            ret = mapper.selectMagDatainCoordi(c,b,f,x1,y1,x2, y2);
        }
        return ret;
    }

    private List<_magnetic_direction>  get_N_Dir_in_coordinate (int c, int b, int f, int x1, int y1, int x2, int y2) {
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();
        List<_magnetic_direction> ret;

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            ret = mapper.selectNDirinCoordi(c,b,f,x1,y1,x2, y2);
        }
        return ret;
    }

    private List<Integer>  get_N_Dir_in_coordinate_per_rssidir (int c, int b, int f, int d1, int d2, int x1, int y1, int x2, int y2) {
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();
        List<Integer> ret;

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            ret = mapper.selectNDirinCoordiwDir(c,b,f,d1,d2,x1,y1,x2, y2);
        }
        return ret;
    }


    // fp_ble_floor_math 을 구축한다. 층별 통계
    public boolean build_fp_ble_floor_math() throws Throwable {
        int object_campus_id = bFloor.getCampus_id();
        int object_building_id = bFloor.getBuilding_id();
        int object_floor_id = bFloor.getFloor_id();

        List<String> ap_list = getAPList(object_campus_id, object_building_id, object_floor_id);

        List<BLEFpFloor> bleFpFloorList = new ArrayList<>();
        BLEFpFloor e;
        Iterator<String> iter = ap_list.iterator();
        while(iter.hasNext()) {
            String ap_id = iter.next();
            List<Integer> rssiListPerAP = getRawData(object_campus_id, object_building_id, object_floor_id,ap_id);
            final int count = rssiListPerAP.size();

            final double rssi_avg = rssiListPerAP.stream().mapToDouble(Integer::doubleValue).summaryStatistics().getAverage();
            final double sum_of_dev = rssiListPerAP.stream().mapToDouble(x -> Math.pow(x.doubleValue() - rssi_avg, 2.0)).sum();
            final double rssi_std_dev = Math.sqrt(sum_of_dev / (count - 1));  // Bessel correction for sample stdev

            e = new BLEFpFloor(object_floor_id, object_building_id, object_campus_id,  rssi_avg, rssi_std_dev, count, ap_id );

            bleFpFloorList.add(e);
        }
        if( bleFpFloorList.size() > 0 ) addBLEFpFloor(bleFpFloorList);

        return false;
    }

    public void addBLEFpFloor(List<BLEFpFloor> bleFpFloorList) throws Throwable {

        MariaConnectionManager mngr = MariaConnectionManager.getInstance();

        // transform list to map
        Map<String, Object> map = new HashMap<>();
        map.put("bleFpFloor", bleFpFloorList);

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            mapper.insertBLEFpFloor(map);
            session.commit();
        } catch (PersistenceException e) {
            throw e.getCause();
        }
    }

    List<Integer> getRawData(int campus, int building, int floor, String ap_id) {
        List<Integer> ret=null;
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            ret = mapper.selRSSI(campus, building, floor, ap_id);
        }

        return ret;
    }

    public static List<String> getAPList ( int campus, int building, int floor) throws Throwable {
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();
        List<String> ret;
        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            ret = mapper.selAP_ID(campus, building, floor);
            return ret;
        }
    }

    public boolean build2() throws Throwable {

        int object_campus_id = bFloor.getCampus_id();
        int object_building_id = bFloor.getBuilding_id();
        int object_floor_id = bFloor.getFloor_id();
        int object_cell_size_ble = bFloor.getBle_cell_size();

        int ble_cell_id;

        double scale_pixel = readMapScale(object_campus_id, object_building_id, object_floor_id);  // 수집시 사용한 지도의 축척정보
        bFloor.setMap_scale_pixel(scale_pixel);

        double object_scale_pixel;
        if((object_scale_pixel = bFloor.getMap_scale_pixel())==0) {
            System.out.println("지도정보가 알맞게 데이터베이스에 수록되어있는지 관리자에게 문의하세요");
            throw  new NullPointerException();
        }

        System.out.println("object_scale_pixel : "+object_scale_pixel);
        System.out.println("object_cell_size : "+object_cell_size_ble);

        logger.debug("build2 process started .................... ");
        Timestamp timestamp_buildStatistics_start = new Timestamp(System.currentTimeMillis());
        logger.debug("starting of buildBLEStatistics TIMESTAMP : "+timestamp_buildStatistics_start);

        int[] rssi_dir_index = bFloor.loadRSSIDir();
        rssiDirBound rssi_dir;


        // prepare for insert value for storing
        List<BLEStatistics> fp_cell_per_ap = null;
        fp_cell_per_ap = new ArrayList<>();



        for (int m = 0; m < bFloor.getM(); m++) {
            for (int n = 0; n < bFloor.getN(); n++) {  // 2.격자별....



                ble_cell_id = bFloor.getCell(m, n).get_id();


                // 먼저 최소단위인 픽셀단위 지자기셀의 크기, pixels_per_cell
                int pixels_per_cell = (int) Math.round(bFloor.getMag_cell_size() / bFloor.getMap_scale_pixel());  // 휴빌론의 경우 21 픽셀

                // SKT 의 경우 17 픽셀

                // 두번째로 현재 BLE 셀이 커버하는 지자기셀을 구한다 - BLE 셀의 coordinate 를 구하는 데 있어서, 축척을 고려한 pixel 단위의 최소거리인 지자기셀의 크기를
                // 기준으로 BLE 셀의 (픽셀단위)크기 및 픽셀단위 좌표값을 산출하기 위해, 먼저 현재 BLE 셀의 범위가 포함하고 있는 지자기셀들의 ID를 구한다.
                //
                List<Integer> mag_cell_id_list = CalculateMagCellID(ble_cell_id);
                //

                int s = mag_cell_id_list.get(0);
                int e = mag_cell_id_list.get(mag_cell_id_list.size() - 1);


                int coordinate_pixel_x1 = (s / 10000 - 1) * pixels_per_cell;
                int coordinate_pixel_y1 = (s % 10000 - 1) * pixels_per_cell;

                int coordinate_pixel_x2 = e / 10000 * pixels_per_cell - 1;
                int coordinate_pixel_y2 = e % 10000 * pixels_per_cell - 1;

                //System.out.printf("Pixel Coordinate : (" + coordinate_pixel_x1 + ", " + coordinate_pixel_y1 + ") (" + coordinate_pixel_x2 + ", " + coordinate_pixel_y2 + ")");
                //System.out.println();

                List<_ble_indicator> ble_list = get_aplist_in_coordinate(object_campus_id, object_building_id, object_floor_id,
                       coordinate_pixel_x1, coordinate_pixel_y1, coordinate_pixel_x2, coordinate_pixel_y2);

                //System.out.println(ble_list);

                for (int i =0; i < rssi_dir_index.length ; i++ ) {  // 수집방향별로 세트를 구성
                    rssi_dir = normalizeRssiDir(rssi_dir_index[i]);
                    final int lowBound = rssi_dir.get_lower_rssi_dir();
                    final int upperBound = rssi_dir.get_upper_rssi_dir();

                    Map<String, List<Integer>> transform = null;




                    if (rssi_dir.get_rssi_dir() == 1000000) {
                        transform = ble_list.stream().filter(x -> ((x.getRssi_dir() > lowBound) &&(x.getRssi_dir()  <= 180)) || ((x.getRssi_dir()  >= -180) && (x.getRssi_dir() < upperBound)))
                                .collect(Collectors.groupingBy(_ble_indicator::get_ap_id,
                                        Collectors.mapping(_ble_indicator::get_rssi, Collectors.toList())));

                    }
                    if (rssi_dir.get_rssi_dir() == 2000000) {
                        transform = ble_list.stream().filter(x -> ((x.getRssi_dir() > lowBound) &&(x.getRssi_dir()  <= 0)) || ((x.getRssi_dir()  >= 0) && (x.getRssi_dir() < upperBound)))
                                .collect(Collectors.groupingBy(_ble_indicator::get_ap_id,
                                        Collectors.mapping(_ble_indicator::get_rssi, Collectors.toList())));

                    } else if (rssi_dir.get_rssi_dir() == 3000000 || rssi_dir.get_rssi_dir() == 4000000 || rssi_dir.get_rssi_dir() == 5000000 || rssi_dir.get_rssi_dir() == 6000000
                            || rssi_dir.get_rssi_dir() == 7000000 || rssi_dir.get_rssi_dir() == 8000000) {

                        transform = ble_list.stream().filter(x -> (x.getRssi_dir() > lowBound) && (x.getRssi_dir() < upperBound))
                                .collect(Collectors.groupingBy(_ble_indicator::get_ap_id,
                                        Collectors.mapping(_ble_indicator::get_rssi, Collectors.toList())));

                    }
                    // map traverse using set view
                    Set<Map.Entry<String, List<Integer>>> entries = transform.entrySet();
                    for (Map.Entry<String, List<Integer>> entry : entries) {    // AP 별 ....
                        List<Integer> rssi_list = entry.getValue();

                        // calculate statistics
                        String ap_id = entry.getKey();

                        final int count = rssi_list.size();
                        final double rssi_min = rssi_list.stream().mapToDouble(Integer::doubleValue).summaryStatistics().getMin();
                        final double rssi_max = rssi_list.stream().mapToDouble(Integer::doubleValue).summaryStatistics().getMax();
                        final double rssi_avg = rssi_list.stream().mapToDouble(Integer::doubleValue).summaryStatistics().getAverage();
                        final double sum_of_dev = rssi_list.stream().mapToDouble(x -> Math.pow(x.doubleValue() - rssi_avg, 2.0)).sum();
                        final double rssi_std_dev = Math.sqrt(sum_of_dev / (count - 1));  // Bessel correction for sample stdev

                        BLEStatistics cell_elem = new BLEStatistics(object_floor_id, object_building_id, object_campus_id, ble_cell_id, rssi_dir.get_rssi_dir(), entry.getKey(), count, rssi_min, rssi_max, rssi_avg, rssi_std_dev);

                        fp_cell_per_ap.add(cell_elem);
                    }

                    //fp_cell_per_ap.forEach(System.out::println);

                }
            }
        }
        if(fp_cell_per_ap.size()>0) insertFPCellPerAP(fp_cell_per_ap);


        logger.debug("build2 process ending ..................... ");
        Timestamp timestamp_buildStatistics_end = new Timestamp(System.currentTimeMillis());
        logger.debug("ending of buildBLEStatistics TIMESTAMP : "+timestamp_buildStatistics_end);

        return false;

    }

    int to180Degree (int x) {
            int ret=0;

            if(x >180 ) {
                x = 360 - x;
                ret = -1 * x;
            }
            else ret = x;

            return ret;
    }


    int to360Degree (int x) {
        int ret=0;

        if(x < 0 ) {
            ret = x + 360;
        }
        else ret = x;

        return ret;
    }
    // fp_ble_data 를 구축한다 . 수집방향별 BLE Cell Statistics
    public boolean build() throws Throwable {

        int object_campus_id = bFloor.getCampus_id();
        int object_building_id = bFloor.getBuilding_id();
        int object_floor_id = bFloor.getFloor_id();
        int object_cell_size = bFloor.getBle_cell_size();

        int ble_cell_id;

        double scale_pixel = readMapScale(object_campus_id, object_building_id, object_floor_id);  // 수집시 사용한 지도의 축척정보
        bFloor.setMap_scale_pixel(scale_pixel);

        double object_scale_pixel;
        if((object_scale_pixel = bFloor.getMap_scale_pixel())==0) {
            System.out.println("지도정보가 알맞게 데이터베이스에 수록되어있는지 관리자에게 문의하세요");
            throw  new NullPointerException();
        }

        System.out.println("object_scale_pixel : "+object_scale_pixel);
        System.out.println("object_cell_size : "+object_cell_size);

        logger.debug("ble_fp_builder process started .................... ");
        Timestamp timestamp_buildStatistics_start = new Timestamp(System.currentTimeMillis());
        logger.debug("starting of buildBLEStatistics TIMESTAMP : "+timestamp_buildStatistics_start);

        int[] rssi_dir_index = bFloor.loadRSSIDir();
        rssiDirBound rssi_dir;



        for (int i =0; i < rssi_dir_index.length ; i++ ) {  // 1.수집방향별로 세트를 구성
            rssi_dir = normalizeRssiDir(rssi_dir_index[i]);

            for (int m = 0; m < bFloor.getM(); m++) {
                for (int n = 0; n < bFloor.getN(); n++) {  // 2.격자별....


                    ble_cell_id = bFloor.getCell(m, n).get_id();

                    //System.out.printf("campus_id : %d, building_id : %d, floor_id : %d,  ble_cell_id : %d, lower_rssi_dir : %d, upper_rssi_dir : %d ",
                    //       object_campus_id, object_building_id, object_floor_id,  ble_cell_id, rssi_dir.get_lower_rssi_dir(), rssi_dir.get_upper_rssi_dir()  );
                    System.out.printf(" ble_cell_id : %d, lower_rssi_dir : %d, upper_rssi_dir : %d ",
                                    ble_cell_id, rssi_dir.get_lower_rssi_dir(), rssi_dir.get_upper_rssi_dir()  );


                    System.out.printf(" ble_cell_location : ("+bFloor.getCell(m,n).get_location_x()+", "+bFloor.getCell(m,n).get_location_y()+")");
                    System.out.println();
/*
                    int cell_location_x = bFloor.getCell(m, n).get_location_x();
                    int cell_location_y = bFloor.getCell(m, n).get_location_y();

                    int coordinate_pixel_x1 = (cell_location_x - object_cell_size / 2) / (int)object_scale_pixel;
                    int coordinate_pixel_y1 = (cell_location_y - object_cell_size / 2) / (int)object_scale_pixel;
                    int coordinate_pixel_x2 = (cell_location_x + object_cell_size / 2) / (int)object_scale_pixel;
                    int coordinate_pixel_y2 = (cell_location_y + object_cell_size / 2) / (int)object_scale_pixel;
 */
                    // BLE cell 의 크기가 지자기 cell 크기의 정수배 라는 가정하에
                    // 지자기 cell(magCell) 의 pixel 단위 크기를 기준으로 BLE Cell 이 커버하는 pixel coordinate 를 구한다

                    // 지도(pixel) 위에서 locaion 을 구하는 최소단위로서 지자기셀 크기의 픽셀값을 사용한다. (휴빌론 21픽셀)
                    // 그러므로 예를 들어 두번째 BLE셀이 커버하는 픽셀단위 영역은 지자기셀 6개 이후로부터 다시 지자기셀 6개 (127 ~ 126 +126 까지)가 된다
                    //

                    // 먼저 최소단위인 픽셀단위 지자기셀의 크기, pixels_per_cell
                    int pixels_per_cell = (int)Math.round(bFloor.getMag_cell_size() / bFloor.getMap_scale_pixel());  // 휴빌론의 경우 21 픽셀

                    // SKT 의 경우 17 픽셀

                    // 두번째로 현재 BLE 셀이 커버하는 지자기셀을 구한다 - BLE 셀의 coordinate 를 구하는 데 있어서, 축척을 고려한 pixel 단위의 최소거리인 지자기셀의 크기를
                    // 기준으로 BLE 셀의 (픽셀단위)크기 및 픽셀단위 좌표값을 산출하기 위해, 먼저 현재 BLE 셀의 범위가 포함하고 있는 지자기셀들의 ID를 구한다.
                    //
                    List<Integer> mag_cell_id_list = CalculateMagCellID(ble_cell_id);
                    //

                    //System.out.println();
                    //mag_cell_id_list.forEach(System.out::println);
                    int s = mag_cell_id_list.get(0);
                    int e = mag_cell_id_list.get(mag_cell_id_list.size() - 1);

                    //System.out.printf("start mag cell : %d , end mag cell : %d", s,e);
                    //System.out.println();
                    //System.out.println();

                    int coordinate_pixel_x1 = (s / 10000 - 1) * pixels_per_cell + 1;
                    int coordinate_pixel_x2 = e / 10000 * pixels_per_cell;
                    int coordinate_pixel_y1 = ( s % 10000 -1 ) * pixels_per_cell + 1;
                    int coordinate_pixel_y2 = e % 10000 * pixels_per_cell;

                    System.out.printf("Pixel Coordinate : ("+coordinate_pixel_x1+", "+coordinate_pixel_y1+") ("+coordinate_pixel_x2+", "+coordinate_pixel_y2+")");
                    System.out.println();

                    List<_ble_indicator> ble_list = get_aplist_in_coordinate_per_rssidir(object_campus_id, object_building_id, object_floor_id,
                            rssi_dir.get_lower_rssi_dir(), rssi_dir.get_upper_rssi_dir(), coordinate_pixel_x1, coordinate_pixel_y1, coordinate_pixel_x2, coordinate_pixel_y2);

                    System.out.println(ble_list);




                    // Getting rssi_sub_list per ap_id by transform result-list to map<ap_id,rssi_sub_list>
                    Map<String, List<Integer>> transform = ble_list.stream().collect(Collectors.groupingBy(_ble_indicator::get_ap_id,
                            Collectors.mapping(_ble_indicator::get_rssi, Collectors.toList())));

                    // prepare for insert value for storing
                    List<BLEStatistics> fp_cell_per_ap = null;

                    if ( transform.size() >0 ) {

                        fp_cell_per_ap = new ArrayList<>();


                        // map traverse using set view
                        Set<Map.Entry<String, List<Integer>>> entries = transform.entrySet();
                        for (Map.Entry<String, List<Integer>> entry : entries) {    // 3.AP 별 ....
                            List<Integer> rssi_list = entry.getValue();

                            // calculate statistics
                            final int count = rssi_list.size();
                            final double rssi_min = rssi_list.stream().mapToDouble(Integer::doubleValue).summaryStatistics().getMin();
                            final double rssi_max = rssi_list.stream().mapToDouble(Integer::doubleValue).summaryStatistics().getMax();
                            final double rssi_avg = rssi_list.stream().mapToDouble(Integer::doubleValue).summaryStatistics().getAverage();
                            final double sum_of_dev = rssi_list.stream().mapToDouble(x -> Math.pow(x.doubleValue() - rssi_avg, 2.0)).sum();
                            final double rssi_std_dev = Math.sqrt(sum_of_dev / (count - 1));  // Bessel correction for sample stdev

                            // populate insert value
                            BLEStatistics cell_elem = new BLEStatistics(object_floor_id, object_building_id, object_campus_id, ble_cell_id, rssi_dir.get_rssi_dir(), entry.getKey(), count, rssi_min, rssi_max, rssi_avg, rssi_std_dev);
                            fp_cell_per_ap.add(cell_elem);
                        }

                        for (BLEStatistics print_elem : fp_cell_per_ap) {
                            System.out.println(print_elem);
                        }
                        // calculate mag_angle -  해당 cell의 각 수집방향에 대하여 수집된 지자기방향의 평균값으로 대표

                        // insert rows to db.  (delete for to delete sample rows if there are sample raws in database)
                        insertFPCellPerAP(fp_cell_per_ap);
                        //deleteFPCell(ble_cell_id);
                    }
                    else {
                        System.out.printf("there are no data...");
                        System.out.println();
                    }

                }
            }
        }
// Loop here ..*********************************************************************/

        logger.debug("ble_fp_builder process ending ..................... ");
        Timestamp timestamp_buildStatistics_end = new Timestamp(System.currentTimeMillis());
        logger.debug("ending of buildBLEStatistics TIMESTAMP : "+timestamp_buildStatistics_end);

        return true;
    }







    List<Integer> CalculateMagCellID(int ble_cell_id) {
        List<Integer> ret = new ArrayList<>();
        int object_cell_size_ble = bFloor.getBle_cell_size();
        int object_cell_size_mag = bFloor.getMag_cell_size();

        int m = Math.round(object_cell_size_ble / object_cell_size_mag);  // 정수배라고 가정

        for(int i = 1 ; i <= m ; i++) {
            for(int j = 1; j <= m ; j++ ) {
                int x = (ble_cell_id / 1000 - 1) * m + i;
                int y = (ble_cell_id % 1000 - 1) * m + j;
                int mag_cell_id = x * 10000 + y;
                ret.add(mag_cell_id);
            }
        }

        return ret;
    }

/*
    public static boolean ble_fp() {

        if (_prop_cell_size_ble == 0 || _console_campus_id == 0 || _console_building_id == 0 || _console_floor_id == 0) return false;

        int M = (FLOOR_LENGTH_X % _prop_cell_size_ble == 0)? ( FLOOR_LENGTH_X / _prop_cell_size_ble ) : (FLOOR_LENGTH_X / _prop_cell_size_ble )+1;
        int N = (FLOOR_LENGTH_Y % _prop_cell_size_ble == 0)? ( FLOOR_LENGTH_Y / _prop_cell_size_ble ) : (FLOOR_LENGTH_Y / _prop_cell_size_ble )+1;

        BLEFpBuilder fp = new BLEFpBuilder(M,N,_prop_cell_size_ble, _console_campus_id, _console_building_id, _console_floor_id);  // mFloor

        fp.LoadCellID();
        fp.LoadCellLocation();

        try {
            fp.build();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return true;
    }
 */
}
