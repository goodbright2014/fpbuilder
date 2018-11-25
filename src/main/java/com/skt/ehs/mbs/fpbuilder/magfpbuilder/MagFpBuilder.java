package com.skt.ehs.mbs.fpbuilder.magfpbuilder;

import com.dtw.DTW;
import com.dtw.FastDTW;
import com.dtw.TimeWarpInfo;
import com.skt.ehs.mbs.fpbuilder.database.FpBuilderDAO;
import com.skt.ehs.mbs.fpbuilder.database.MariaConnectionManager;
import com.util.DistanceFunction;
import com.util.DistanceFunctionFactory;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import com.timeseries.TimeSeries;

import static com.dtw.DTW.getWarpDistBetween;

/**
 * Created by user on 2016-12-07.
 */
public class MagFpBuilder {

    final Logger logger = LoggerFactory.getLogger(MagFpBuilder.class);

    Floor_Magnetic mFloor;

    public MagFpBuilder(int M, int N, int cellSize,int cellSize_ble, int campus, int building, int floor) {
        double scale = readMapScale(campus,building,floor);
        mFloor = new Floor_Magnetic(M, N, cellSize,cellSize_ble, campus, building, floor, scale);
    }

    public void LoadCellID() {
        for (int x = 1 ; x <= mFloor.getM(); x++) {

            int id = 0;
            int id_x = 10000 * x; // magnetic cell id goes like 10000 ~ 99990000
            for (int y = 1; y <= mFloor.getN(); y++) {
                id = id_x + y;
                _mag_cell bleCell= new _mag_cell(id);
                mFloor.setCell(x-1,y-1,bleCell);
            }
        }
        mFloor.showCellID();
    }

    public void LoadCellLocation() {
        int location_x = 0;
        int location_y = 0;
        int object_cell_size = mFloor.getSize_cell_mag();
        double object_scale_pixel = mFloor.getScale_pixel_map();
        logger.debug("축척 센치미터 per pixel : "+object_scale_pixel);
        int pixels_per_cell = (int)Math.round(object_cell_size / object_scale_pixel);
        logger.debug("Cell 크기 pixel 단위로 : "+pixels_per_cell);

        for (int x = 1 ; x <= mFloor.getM(); x++) {

            location_x = (int)Math.round(( pixels_per_cell * x - (int)(pixels_per_cell / 2) ) * mFloor.getScale_pixel_map());

            //logger.debug("location_x : "+location_x);
            // location_x = object_cell_size * x - object_cell_size / 2;
            for (int y = 1; y <= mFloor.getN(); y++) {

                location_y = (int)Math.round(( pixels_per_cell * y - pixels_per_cell / 2 ) * mFloor.getScale_pixel_map());
                //location_y = object_cell_size * y - object_cell_size / 2;
                //logger.debug("location_y : "+location_y);
                mFloor.setCellLocation(x-1,y-1,location_x,location_y);
            }
            //logger.debug(" ");
        }
        mFloor.showCellLocation();
    }

    private static double readMapScale(int c, int b, int f) {
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();
        double scale= 0;

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);

            scale = mapper.getMapScale(c,b,f);

        }
        return scale;
    }

    List<_mag_quantity> getMagVHInCoordinate(int c, int b, int f, int x1, int y1, int x2, int y2) {
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();
        List<_mag_quantity> ret;

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            ret = mapper.selectMagVH(c,b,f,x1,y1,x2, y2);
        }
        return ret;
    }

    List<RawdataMagnetic> loadDataInCoordinate(int c, int b, int f, int x1, int y1, int x2, int y2) {
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();
        List<RawdataMagnetic>  ret;

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            ret = mapper.selectRawData(c,b,f,x1,y1,x2, y2);
        }
        return ret;
    }

    public void addFP(CellStatistics elem) {
        // DB store
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();
        SqlSession session = mngr.getSqlSessionFactory().openSession();

        try {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            mapper.insert_fp_mag_data(elem);
            session.commit();
        } catch (Exception e) {
            throw e;
        } finally {
            session.close();
        }
    }


    public void listAddFP(List<CellStatistics> lst) throws Throwable {
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();
        SqlSession session = mngr.getSqlSessionFactory().openSession();

        try {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            mapper.insertMagStat(lst);
            session.commit();
        } catch (PersistenceException e) {
            throw e.getCause();
        }
    }



    public void delFP(int cell_id) {
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();
        SqlSession session = mngr.getSqlSessionFactory().openSession();

        try {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            mapper.delete_fp_mag_data(cell_id);
            session.commit();
        } catch (Exception e) {
            throw e;
        } finally {
            session.close();
        }
    }

    //int CalculateBLECellID(int cell_location_x, int cell_location_y) {
    int CalculateBLECellID(int magCellID) {


        int object_cell_size_ble = mFloor.getSize_cell_ble();
        int object_cell_size_mag = mFloor.getSize_cell_mag();

        // 이 구현은 BLE Cell Size 가 Mag Cell Size 의 정수배라는 가정에서 구현되었다
        int m = Math.round(object_cell_size_ble / object_cell_size_mag);

        int x = ((int)(magCellID / 10000) %  m == 0)? (int)(magCellID / 10000) /  m :  (int)(magCellID / 10000) /  m + 1;
        int y = ((magCellID % 10000) % m ==0)? (magCellID % 10000) / m : (magCellID % 10000) / m + 1;

        //System.out.printf("x is  %d ,, y is %d...",x,y);

       // int x = (cell_location_x % object_cell_size_ble == 0)? ( cell_location_x / object_cell_size_ble ) : (cell_location_x / object_cell_size_ble )+1;
       // int y = (cell_location_y % object_cell_size_ble == 0)? ( cell_location_y / object_cell_size_ble ) : (cell_location_y / object_cell_size_ble )+1;

        return 1000 * x + y;
    }

    List<Integer> CalculateMagCellID(int ble_cell_id) {
        List<Integer> ret = new ArrayList<>();
        int object_cell_size_ble = mFloor.getSize_cell_ble();
        int object_cell_size_mag = mFloor.getSize_cell_mag();

        int m = Math.round(object_cell_size_ble / object_cell_size_mag);  // 정수배라고 가정

        for(int i = 1 ; i <= m ; i++) {
            for(int j = 1; j <= m ; j++ ) {
                int x = (ble_cell_id / 1000 - 1) * m + i;
                int y = (ble_cell_id % 1000 - 1) * m + j;
                if( x <= mFloor.getM() && y <= mFloor.getN()) {
                    int mag_cell_id = x * 10000 + y;
                    ret.add(mag_cell_id);
                }
            }
        }
        return ret;
    }

    // 지자시 셀 기준으로 DB 를 access 하게 되면 지자기셀 갯수만큼 Database 에 접근하게 되어 시간이 많이 걸린다.
    // 따라서 BLE 셀 기준으로 Data 를 Loading 하여, Loading 된 데이터로부터 지자기셀단위 통계를 집계한다. 2017-01-19 jaehyu
    public boolean buildStatistics()  {
        int object_campus_id = mFloor.getCampus_id();
        int object_building_id = mFloor.getBuilding_id();
        int object_floor_id = mFloor.getFloor_id();
        int object_cell_size_ble = mFloor.getSize_cell_ble();
        int object_cell_size_mag = mFloor.getSize_cell_mag();

        // 이 구현은 BLE Cell Size 가 Mag Cell Size 의 정수배라는 가정에서 구현되었다
        int m = Math.round(object_cell_size_ble / object_cell_size_mag);

        // BLE Cell 의 갯수, BLE_M x BLE_N
        int BLE_M = (mFloor.getM() % m == 0)? mFloor.getM() / m : mFloor.getM() / m + 1;
        int BLE_N = (mFloor.getN() % m == 0)? mFloor.getN() / m : mFloor.getN() / m + 1;
        // 먼저 최소단위인 픽셀단위 지자기셀의 크기, pixels_per_cell
        int pixels_per_cell = (int)Math.round(mFloor.getSize_cell_mag() / mFloor.getScale_pixel_map());  // 휴빌론의 경우 21 픽셀
        System.out.println("pixels_per_cell"+pixels_per_cell);

        logger.debug("mag_fp_builder process started .................................................................................. ");
        Timestamp timestamp_buildStatistics_start = new Timestamp(System.currentTimeMillis());
        logger.debug("starting of buildStatistics TIMESTAMP : "+timestamp_buildStatistics_start);

        List<CellStatistics> fp_mag_data = new ArrayList<>();

        for(int i = 0; i < BLE_M; i++) {
            for(int j =0; j < BLE_N;  j++) {

                int ble_cell_id = (i+1) * 1000 + (j+1);
                // 두번째로 현재 BLE 셀이 커버하는 지자기셀을 구한다.
                List<Integer> mag_cell_id_list = CalculateMagCellID(ble_cell_id);
                //System.out.println();
                //mag_cell_id_list.forEach(System.out::println);
                int s = mag_cell_id_list.get(0);
                int e = mag_cell_id_list.get(mag_cell_id_list.size() - 1);

                //System.out.printf("start mag cell : %d , end mag cell : %d", s,e);
                //System.out.println();
                //System.out.println();

                int coordinate_pixel_x1 = (s / 10000 - 1) * pixels_per_cell;
                int coordinate_pixel_x2 = e / 10000 * pixels_per_cell;
                int coordinate_pixel_y1 = ( s % 10000 -1 ) * pixels_per_cell;
                int coordinate_pixel_y2 = e % 10000 * pixels_per_cell;



                //System.out.println();
                //System.out.println("BLE Cell ID : "+ble_cell_id);
                //for(int mag_cell_id : mag_cell_id_list) System.out.print(mag_cell_id+", ");
                //System.out.printf("Pixel Coordinate : ("+coordinate_pixel_x1+", "+coordinate_pixel_y1+") ("+coordinate_pixel_x2+", "+coordinate_pixel_y2+")");
                //System.out.println();


                // BLE Cell 하나가 커버하는 영역의 RawData를 Loading 한다
                List<RawdataMagnetic> dataPool =  loadDataInCoordinate(object_campus_id, object_building_id, object_floor_id,
                        coordinate_pixel_x1, coordinate_pixel_y1, coordinate_pixel_x2, coordinate_pixel_y2);


                //System.out.println("Count : "+dataPool.size());
                // 이제 dataPool 은 몇개의 지자기셀단위로 집계될수 있는 RawData 를 담고있다

                Iterator<Integer> iter = mag_cell_id_list.iterator();
                while(iter.hasNext()) {
                    int mag_cell_id = iter.next();



                    int cell_location_x = mFloor.getCell(mag_cell_id/10000 - 1, mag_cell_id%10000 - 1).get_location_x();
                    int cell_location_y = mFloor.getCell(mag_cell_id/10000 - 1, mag_cell_id%10000 - 1).get_location_y();

                    int mag_cell_pixel_x1 = (mag_cell_id / 10000 - 1) * pixels_per_cell;
                    int mag_cell_pixel_x2 = mag_cell_id / 10000 * pixels_per_cell-1;
                    int mag_cell_pixel_y1 = ( mag_cell_id % 10000 -1 ) * pixels_per_cell;
                    int mag_cell_pixel_y2 = mag_cell_id % 10000 * pixels_per_cell-1;


                    List<Double> bh_list = new ArrayList<>();
                    bh_list = dataPool
                              .stream()
                              .filter(x -> x.getPosition_x() >= mag_cell_pixel_x1 && x.getPosition_x() <= mag_cell_pixel_x2 && x.getPosition_y() >= mag_cell_pixel_y1 && x.getPosition_y() <= mag_cell_pixel_y2)
                              .mapToDouble(x -> x.getMag_bh())
                              .boxed()
                              .collect(Collectors.toList());

                    //bh_list.forEach(System.out::println);
                    //System.out.println();

                    List<Double> bv_list = new ArrayList<>();
                    bv_list = dataPool
                            .stream()
                            .filter(x -> x.getPosition_x() >= mag_cell_pixel_x1 && x.getPosition_x() <= mag_cell_pixel_x2 && x.getPosition_y() >= mag_cell_pixel_y1 && x.getPosition_y() <= mag_cell_pixel_y2)
                            .mapToDouble(x -> x.getMag_bv())
                            .boxed()
                            .collect(Collectors.toList());

                    // ndir_norm_factor 는 측위시 Tag의 nDir값을 지도기준의 dir 값으로 변환해주기위한 값이다.  지도상 dir = ndir - ndir_norm_factor
                    List<Integer> ndir_list = magCellGetOrientation(dataPool,mag_cell_pixel_x1,mag_cell_pixel_x2,mag_cell_pixel_y1,mag_cell_pixel_y2);
                    int ndir_norm_factor = normalizeNDir(ndir_list);

                    CellStatistics fp_per_cell;

                    final int bh_count = bh_list.size();
                    final int bv_count = bv_list.size();

                    if ( bh_count > 0 && bv_count > 0) {

                        final double bh_min = bh_list.stream().mapToDouble(x -> x).summaryStatistics().getMin();
                        final double bh_max = bh_list.stream().mapToDouble(x -> x).summaryStatistics().getMax();
                        final double bh_avg = bh_list.stream().mapToDouble(x -> x).summaryStatistics().getAverage();
                        final double bh_sum_of_dev = bh_list.stream().mapToDouble(x -> Math.pow(x - bh_avg, 2.0)).sum();
                        final double bh_std_dev = Math.sqrt(bh_sum_of_dev / (bh_count - 1));
                        // transform list => array for sorting ..  data need to be sorting when calculate median.
                        double bh_array[] = bh_list.stream().mapToDouble(x -> x).toArray();
                        // sorting
                        Arrays.sort(bh_array);
                        //Arrays.stream(h_array).forEach(System.out::println);
                        // get median
                        final double bh_median;
                        if (bh_array.length % 2 == 0)
                            bh_median = (bh_array[bh_array.length / 2] + bh_array[bh_array.length / 2 - 1]) / 2;
                        else
                            bh_median = bh_array[bh_array.length / 2];
                        //System.out.println("bh_median per cell : "+bh_median);


                        final double bv_min = bv_list.stream().mapToDouble(x -> x).summaryStatistics().getMin();
                        final double bv_max = bv_list.stream().mapToDouble(x -> x).summaryStatistics().getMax();
                        final double bv_avg = bv_list.stream().mapToDouble(x -> x).summaryStatistics().getAverage();

                        // calculate standard deviation
                        final double bv_sum_of_dev = bv_list.stream().mapToDouble(x -> Math.pow(x - bv_avg, 2.0)).sum();
                        final double bv_std_dev = Math.sqrt(bv_sum_of_dev / (bv_count - 1)); // Bessel correction for sample stdev

                        // calculate median value
                        double bv_array[] = bv_list.stream().mapToDouble(x -> x).toArray();
                        Arrays.sort(bv_array);
                        //Arrays.stream(h_array).forEach(System.out::println);
                        final double bv_median;
                        if (bv_array.length % 2 == 0)
                            bv_median = (bv_array[bv_array.length / 2] + bv_array[bv_array.length / 2 - 1]) / 2;
                        else
                            bv_median = bv_array[bv_array.length / 2];
                        //System.out.println("bv_median per cell : "+bv_median);


                        // 수집셀단위 ndir 리스트 관찰

                        fp_per_cell = new CellStatistics(object_floor_id, object_building_id, object_campus_id,
                                mag_cell_id, ble_cell_id, cell_location_x, cell_location_y,
                                bh_avg, bh_std_dev, bh_min, bh_max, bh_median,
                                bv_avg, bv_std_dev, bv_min, bv_max, bv_median, bv_count, object_cell_size_mag, ndir_norm_factor);

                        fp_mag_data.add(fp_per_cell);
                    }
                }

            }
        }

        try {
            listAddFP(fp_mag_data);
        } catch (Throwable throwable) {
            //throwable.printStackTrace();
        }

        logger.debug("mag_fp_builder process ending ....................................................................................... ");
        Timestamp timestamp_buildStatistics_end = new Timestamp(System.currentTimeMillis());
        logger.debug("ending of buildStatistics TIMESTAMP : "+timestamp_buildStatistics_end);

        return false;
    }

//Motion Direction - 이동가능한 방향 (으로 연속된 6개의 magcell 이 존재할때 가중치를 부여)
    public void magCellGetMDirCount(int campus, int building, int floor) {
        List<Integer> magFpCellList = null;
        try {
            magFpCellList = getMagFpCellList(campus, building, floor);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        //magFpCellList.forEach(System.out::println);
        System.out.println("magFpCellList Size : "+magFpCellList.size());

        // motion direction matrix
        int[][] mdm= new int[mFloor.getM()][mFloor.getN()];

        for(int magCellID : magFpCellList) {
            int x = (int)(magCellID / 10000) ;
            int y = magCellID % 10000;
            mdm[x][y]=1;
        }

        for (int j=0 ; j < mFloor.getN(); j++) {
            for (int i = 0; i < mFloor.getM(); i++) {
                System.out.print(" " + mdm[i][j] + " ");
            }
            System.out.println("\n");
        }

        int north_count;
        int east_count;
        int south_count;
        int west_count;
        int mdirFactor;

        for(int magCellID : magFpCellList) {
            north_count = 0;
            east_count=0;
            south_count=0;
            west_count=0;
            mdirFactor=0;

            System.out.println("\n"+magCellID);
            int x = (int)(magCellID / 10000) ;
            int y = magCellID % 10000;

            // 북쪽으로 조사
            for ( int j = y ; j > 0; j--) {
                if( mdm[x][j] == 1) {
                    //System.out.println(" x "+x+" j "+j+" "+mdm[x][j]);
                    north_count++;
                }
                else break;
            }
            System.out.println( "\tNorth count : "+ north_count);

            // 동쪽으로 조사
            for ( int i = x ; i < mFloor.getM(); i++) {
                if( mdm[i][y] == 1) {
                    //System.out.println(" x "+x+" j "+j+" "+mdm[x][j]);
                    east_count++;
                }
                else break;
            }
            System.out.println( "\tEast count : "+ east_count);

            //남쪽으로 조사
            for ( int j = y ; j < mFloor.getN(); j++) {
                if( mdm[x][j] == 1) {
                    //System.out.println(" x "+x+" j "+j+" "+mdm[x][j]);
                    south_count++;
                }
                else break;
            }
            System.out.println( "\tSouth count : "+ south_count);

            // 서쪽으로 조사
            for ( int i = x ; i > 0; i--) {
                if( mdm[i][y] == 1) {
                    //System.out.println(" x "+x+" j "+j+" "+mdm[x][j]);
                    west_count++;
                }
                else break;
            }
            System.out.println( "\tWest count : "+ west_count);

            // mdirFactor 산출
            if(north_count > 6) mdirFactor += 1;
            if(east_count > 6) mdirFactor += 2;
            if(south_count > 6) mdirFactor += 4;
            if(west_count > 6) mdirFactor += 8;
            System.out.println( "\tmdirFactor : "+ mdirFactor);

            updateMDirCount(campus, building, floor,magCellID,mdirFactor);
        }
    }

    static void updateMDirCount(int campus, int building, int floor, int cellid, int mDirFactor) {
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();
        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            mapper.updateMDirCount(campus, building, floor,cellid,mDirFactor);
            session.commit();
        }
    }

    public static List<Integer> getMagFpCellList ( int campus, int building, int floor) throws Throwable {
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();
        List<Integer> ret;
        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            ret = mapper.selMagCellID(campus, building, floor);
            return ret;
        }
    }
    int normalizeNDir(List<Integer> ndir_list) {
        int ndir_norm_factor=0;
        List<Double> ndir_double_list = new ArrayList<Double>();

        if(ndir_list.size()>0) {
            // integer List 를 double List 로 변환
            ndir_list.forEach(x -> ndir_double_list.add(Double.valueOf(x)));
            ndir_norm_factor = (int)Math.round(getMeanAngle(ndir_double_list));     // getMeanAngle() 은 atan2()의 결과값이므로 -180~0, 0~180 의 범위를 갖는다
        }

        return ndir_norm_factor;
    }


    List<Integer> magCellGetOrientation(List<RawdataMagnetic> dataPool, int x1, int x2, int y1, int y2) {

        // 수집방향별로 세트를 나누어 각 수집방향을 갖는 NDir값을 수집방향 북쪽기준으로 이동한다

        List<Integer> ndir_list = new ArrayList<>();
        List<Integer> ndir_list2 = new ArrayList<>();
        List<Integer> ndir_list3 = new ArrayList<>();
        List<Integer> ndir_list4 = new ArrayList<>();
        List<Integer> ndir_list5 = new ArrayList<>();
        List<Integer> ndir_list6 = new ArrayList<>();
        List<Integer> ndir_list7 = new ArrayList<>();
        List<Integer> ndir_list8 = new ArrayList<>();

        ndir_list = dataPool
                .stream()
                .filter(x -> x.getPosition_x() >= x1 && x.getPosition_x() <= x2 && x.getPosition_y() >= y1 && x.getPosition_y() <= y2)
                .filter(x -> x.getRssi_dir() > 67 && x.getRssi_dir() < 113)   // 지도상 북을 가상자북방향으로 하기 위함. 지도상 수집방향이 북쪽인 수집데이터는 그대로 사용
                .map(x -> x.getNorth_dir())
                .collect(Collectors.toList());
        // 남쪽방향
        ndir_list2 = dataPool.stream()
                .filter(x -> x.getPosition_x() >= x1 && x.getPosition_x() <= x2 && x.getPosition_y() >= y1 && x.getPosition_y() <= y2)
                .filter(x -> x.getRssi_dir() > -113 && x.getRssi_dir() < -67)
                .map(x -> (x.getNorth_dir() - 180))
                .collect(Collectors.toList());

        // 동쪽방향
        ndir_list3 = dataPool.stream()
                .filter(x -> x.getPosition_x() >= x1 && x.getPosition_x() <= x2 && x.getPosition_y() >= y1 && x.getPosition_y() <= y2)
                .filter(x -> (x.getRssi_dir() > -23 && x.getRssi_dir() <= 0) || (x.getRssi_dir() >= 0 && x.getRssi_dir() <23)) // EAST
                .map(x -> (x.getNorth_dir() - 90))
                .collect(Collectors.toList());
        // 서쪽방향
        ndir_list4 = dataPool.stream()
                .filter(x -> x.getPosition_x() >= x1 && x.getPosition_x() <= x2 && x.getPosition_y() >= y1 && x.getPosition_y() <= y2)
                .filter(x -> (x.getRssi_dir() > 157 && x.getRssi_dir() <= 180) || (x.getRssi_dir() >= -180 && x.getRssi_dir() < -157)) // WEST
                .map(x -> (x.getNorth_dir() + 90))
                .collect(Collectors.toList());
        // 남동쪽방향
        ndir_list5 = dataPool.stream()
                .filter(x -> x.getPosition_x() >= x1 && x.getPosition_x() <= x2 && x.getPosition_y() >= y1 && x.getPosition_y() <= y2)
                .filter(x -> x.getRssi_dir() > -68 && x.getRssi_dir() < -22)  // 남동쪽방향
                .map(x -> (x.getNorth_dir() - 135))
                .collect(Collectors.toList());
        // 북서쪽방향
        ndir_list6 = dataPool.stream()
                .filter(x -> x.getPosition_x() >= x1 && x.getPosition_x() <= x2 && x.getPosition_y() >= y1 && x.getPosition_y() <= y2)
                .filter(x -> x.getRssi_dir() > 112 && x.getRssi_dir() < 158)
                .map(x -> (x.getNorth_dir() + 45))
                .collect(Collectors.toList());
        // 남서쪽방향
        ndir_list7 = dataPool.stream()
                .filter(x -> x.getPosition_x() >= x1 && x.getPosition_x() <= x2 && x.getPosition_y() >= y1 && x.getPosition_y() <= y2)
                .filter(x -> x.getRssi_dir() > -158 && x.getRssi_dir() < -112)
                .map(x -> (x.getNorth_dir() + 135))
                .collect(Collectors.toList());
        // 북동쪽방향
        ndir_list8 = dataPool.stream()
                .filter(x -> x.getPosition_x() >= x1 && x.getPosition_x() <= x2 && x.getPosition_y() >= y1 && x.getPosition_y() <= y2)
                .filter(x -> x.getRssi_dir() > 22 && x.getRssi_dir() < 68)
                .map(x -> (x.getNorth_dir() - 45))
                .collect(Collectors.toList());

/*
        if(ndir_list.size() >0 ) {
            System.out.println("ndir_list1");
            for (int i : ndir_list) {
                System.out.printf("%6d", i);
            }
            System.out.println();
        }
        if(ndir_list2.size() >0 ) {
            System.out.println("ndir_list2");
            for (int i : ndir_list2) {
                System.out.printf("%6d", i);
            }
            System.out.println();
        }
        if(ndir_list3.size() >0 ) {
            System.out.println("ndir_list3");
            for (int i : ndir_list3) {
                System.out.printf("%6d", i);
            }
            System.out.println();
        }
        if(ndir_list4.size() >0 ) {
            System.out.println("ndir_list4");
            for (int i : ndir_list4) {
                System.out.printf("%6d", i);
            }
            System.out.println();
        }



*/
        ndir_list.addAll(ndir_list2);
        ndir_list.addAll(ndir_list3);
        ndir_list.addAll(ndir_list4);
        ndir_list.addAll(ndir_list5);
        ndir_list.addAll(ndir_list6);
        ndir_list.addAll(ndir_list7);
        ndir_list.addAll(ndir_list8);

        return ndir_list;
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



        // 2.클러스터링 기법을 적용하여 도출된 패턴들의 FingerPrinting 을 구축

        // 두번째 패키지에 대하여... (패키지란 수집툴에 의해 한번 묶음전송된 단위수집일정의 데이터 묶음)

            // 위 첫번째 패키지와 동일과정 적용.
            // 두번째 패키지에 관하여 각 수집선별 대표수집선 선택 (DTW, K-means)
            // 첫번째 과정에서 선출된 수집선별 대표리스트와 ((동일수집선여부)) 검사
            // 동일 수집선 존재하면 어느것을 대표자료로 할지 결정 ( which algorithm ? )

        // 반복 : 모든 패키지에 대해 수행.


    public static List<String> getPackageList ( int campus, int building, int floor) throws Throwable {
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();
        List<String> ret;
        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            ret = mapper.selPackageKey(campus, building, floor);
            return ret;
        }
    }

    public static List<RawdataMagnetic> getRawData(int campus, int building, int floor, String packageKey) throws Throwable {
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();
        List<RawdataMagnetic> ret;

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            ret = mapper.selRawData(campus, building, floor, packageKey);
            return ret;
        }
    }

    public boolean buildClustering()  {

        int object_campus_id = mFloor.getCampus_id();
        int object_building_id = mFloor.getBuilding_id();
        int object_floor_id = mFloor.getFloor_id();
        int object_cell_size = mFloor.getSize_cell_mag();
        int pixels_per_cell = (int)Math.round( object_cell_size / mFloor.getScale_pixel_map());


        List<String> packageList = Collections.emptyList();
        try {
            packageList = getPackageList(object_campus_id,object_building_id,object_floor_id);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        Iterator<String> iter = packageList.iterator();

        while(iter.hasNext()) {             // 1.패키지별.....
            List<RawdataMagnetic> packageData  = Collections.emptyList();
            String packageKey = iter.next();
            try {
                packageData = getRawData(object_campus_id, object_building_id, object_floor_id, packageKey);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            // gLine - 수집선별 수집정보 (Map) , 하나의 저장단위 package 에 몇개의 수집라인이 존재하는가 (GroupBy collected_time)
            Map<Date, List<RawdataMagnetic>> gLine = packageData.stream().collect(Collectors.groupingBy(RawdataMagnetic::getCollected_time,
                    Collectors.toList()));
            for (Map.Entry<Date, List<RawdataMagnetic>> mapOneLine : gLine.entrySet()) {    // 2.수집선별........
                List<RawdataMagnetic> dataPerLink = mapOneLine.getValue();
                //Date date = mapOneLine.getKey();
                //System.out.println(date);

                List<List<RawdataMagnetic>> patternList = new ArrayList<>();

                ///////////////////////////////////////////////////////////////
                //
                // Tag 별 수집선 데이터간 유사도를 측정하고, 대표패턴 선출한다.

                patternList = get_feature_by_id(dataPerLink);

                ///////////////////////////////////////////////////////////////
                //   대표 패턴 리스트 FP_CLUSTER 에 저장
                ///////////////////////////////////////////////////////////////
                if(patternList != null) {
                    List<LinkFeatures> pattern = new ArrayList<>();

                    int cluster_id;
                    int _seqnum;
                    int mag_cell_idx_x;
                    int mag_cell_idx_y;
                    int mag_cell_id;
                    double bh;
                    double bv;
                    String packageID;
                    String tagID;
                    int posX;
                    int posY;
                    Date cTime;
                    int numOfPattern = patternList.size() ;

                    Iterator<List<RawdataMagnetic>> patternListIterator = patternList.iterator();
                    while (patternListIterator.hasNext()) {   // 수집링크 기준 대표패턴 묶음

                        List<RawdataMagnetic> featureList = patternListIterator.next();

                        _seqnum = 0;  // 패턴내 시퀀스 넘버 .. clear here
                        int millis = (int)(System.currentTimeMillis()%1000000000);   // clusterid 0~999999999


                        for(RawdataMagnetic item : featureList) {    // 대표패턴 하나
                            LinkFeatures fpItem;

                            packageID = item.getPackage_collect_key();
                            tagID = item.getTag_id();
                            posX = item.getPosition_x();
                            posY = item.getPosition_y();
                            cTime = item.getCollected_time();

                            cluster_id = millis;
                            bv = item.getMag_bv();
                            bh = item.getMag_bh();

                            //String tag_id = item.getTag_id();
                            //System.out.println("Pattern TAG is : "+tag_id);


                            mag_cell_idx_x = (int)(item.getPosition_x() / pixels_per_cell);
                            mag_cell_idx_y = (int)(item.getPosition_y() / pixels_per_cell);
                            mag_cell_id = mag_cell_idx_x * 10000 +  mag_cell_idx_y;

                            fpItem = new LinkFeatures(object_floor_id, object_building_id, object_campus_id,
                                    cluster_id, _seqnum, mag_cell_id, bh, bv);

                            logger.debug("LinkFeatures{" +
                                    ", package_id=" + packageID +
                                    ", pattern_list_size=" + numOfPattern +
                                    ", tag_id=" + tagID +
                                    ", cTime=" + cTime +
                                    ", cluster_id=" + cluster_id +
                                    ", seqnum=" + _seqnum +
                                    ", posX=" + posX +
                                    ", posY=" + posY +
                                    ", mag_cell_id=" + mag_cell_id +
                                    ", bh=" + bh +
                                    ", bv=" + bv +
                                    '}');
                            //System.out.println(fpItem);
                            pattern.add(fpItem);
                            _seqnum++;
                        }
                    }

                    try {
                        addCluster(pattern);
                    } catch (Throwable throwable) {
                        //throwable.printStackTrace();
                    }
                }
            }
        }


        return false;
    }


    public void addCluster(List<LinkFeatures> pattern) throws Throwable {

        MariaConnectionManager mngr = MariaConnectionManager.getInstance();

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            mapper.insertPattern(pattern);
            session.commit();
        } catch (PersistenceException e) {
            throw e.getCause();
        }
    }

    public void addCluster2(List<LinkFeatures> pattern) throws Throwable {
        List<LinkFeatures> cache_list = new ArrayList<>();
        int iter=0;
        int startidx = 0;
        int endidx = startidx + 1000;
        int sizeLeft = pattern.size();
        while(true) {
            if (sizeLeft / 1000 >= 1) {
                cache_list = pattern.subList(startidx, endidx);
                try {
                    logger.debug((iter+1)+" addCluster : "+cache_list.size());
                    addCluster(cache_list);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                sizeLeft = sizeLeft - 1000;
                iter++;
                startidx = iter * 1000;
                endidx = startidx + 1000;
            } else {
                endidx = startidx + sizeLeft;
                cache_list = pattern.subList(startidx, endidx);
                try {
                    logger.debug((iter+1)+" addCluster : "+cache_list.size());
                    if(cache_list.size() > 0) addCluster(cache_list);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                break;
            }
        }
    }


    /*
        대표패턴 선출루틴

        수집라인 하나에 해당하는, 여러개의 Tag 를 사용했을 수도 있는 수집라인 데이터셋 data 를 입력받어,
        각 Tag 별 수집라인데이터 간의 유사도를 측정 , 비교한다 (Clustering)

        클러스터링 이후에는 클러스터를 대표할 대표패턴을 결정하여 , 리턴값인 패턴리스트에 추가한다.
     */

    public List<List<RawdataMagnetic>> get_feature_by_id(List<RawdataMagnetic> data) {
        List<List<RawdataMagnetic>> patternList=new ArrayList<>();

        Map<String,List<RawdataMagnetic>> gTag = data.stream().collect(Collectors.groupingBy(RawdataMagnetic::getTag_id, Collectors.toList()));
        List<List<Double>> data_t_bh = new ArrayList<>();
        List<List<Double>> data_t_bv = new ArrayList<>();




        gTag.forEach((k,v) -> {    // k : tag 별 , v : 수집데이터             // 3. 태그별...
            List<RawdataMagnetic> training_ = v;

            // for debugging use only  그러므로 테스트 후, 지우자 !!!!
            List<String> pList = training_.stream().map(RawdataMagnetic::getPackage_collect_key).collect(Collectors.toList());
            List<String> tagList = training_.stream().map(RawdataMagnetic::getTag_id).collect(Collectors.toList());
            List<Integer> rssiDirList = training_.stream().map(RawdataMagnetic::getRssi_dir).collect(Collectors.toList());
            List<Integer> posXList = training_.stream().map(RawdataMagnetic::getPosition_x).collect(Collectors.toList());
            List<Integer> posYList = training_.stream().map(RawdataMagnetic::getPosition_y).collect(Collectors.toList());
            List<Integer> northDirList = training_.stream().map(RawdataMagnetic::getNorth_dir).collect(Collectors.toList());

            /*
            for (int t=0; t < training_.size(); t++) {
                System.out.println("PackageKey - tagid - rssiDir - PosX - posY - northDir : "+pList.get(t)+" "+tagList.get(t)+" "+rssiDirList.get(t)+" "
                        + posXList.get(t) +" "+posYList.get(t) +" "+northDirList.get(t));
            }
            */
            //////////////////////////////////////


            List<Double> training_bh = training_.stream().mapToDouble(RawdataMagnetic::getMag_bh).boxed().collect(Collectors.toList());
            List<Double> training_bv = training_.stream().mapToDouble(RawdataMagnetic::getMag_bv).boxed().collect(Collectors.toList());

            List<Double> zero_mean_bh = zero_mean(training_bh);
            List<Double> zero_mean_bv = zero_mean(training_bv);


            data_t_bh.add(zero_mean_bh);
            data_t_bv.add(zero_mean_bv);

        });

        int numOfTags = gTag.size();
        double[][] dtw_bh = new double[numOfTags][numOfTags];
        double[][] dtw_bv = new double[numOfTags][numOfTags];

        // Compare two TimeSeries
        TimeSeries tsI;
        TimeSeries tsJ;
        TimeSeries tsX;
        TimeSeries tsY;
        DistanceFunction distFn;
        TimeWarpInfo info;

        distFn = DistanceFunctionFactory.getDistFnByName("ManhattanDistance");

        for (int i = 0; i < numOfTags; i++) {
            for (int j = 0; j < numOfTags; j++) {
                tsI = new TimeSeries((ArrayList)data_t_bh.get(i));
                tsJ = new TimeSeries((ArrayList)data_t_bh.get(j));

                info = DTW.getWarpInfoBetween(tsI, tsJ, distFn);
                dtw_bh[i][j] = info.getDistance()/info.getPath().size();
                //logger.debug("i & j : "+i+" "+j);
                //logger.debug("dtw_bh : "+dtw_bh[i][j]);
                System.out.printf("dtw_bh[%d][%d] : %f  ",i,j,dtw_bh[i][j]);
                System.out.println();

                logger.debug("BH_DTW["+i+"]["+j+"] : "+dtw_bh[i][j]);

                tsX = new TimeSeries((ArrayList)data_t_bv.get(i));
                tsY = new TimeSeries((ArrayList)data_t_bv.get(j));
                info = DTW.getWarpInfoBetween(tsX, tsY, distFn);
                dtw_bv[i][j] = info.getDistance()/info.getPath().size();


                //logger.debug("i & j : "+i+" "+j);
                //logger.debug("dtw_bv : "+dtw_bv[i][j]);
                System.out.printf("dtw_bv[%d][%d] : %f  ",i,j,dtw_bv[i][j]);
                System.out.println();
                logger.debug("BV_DTW["+i+"]["+j+"] : "+dtw_bv[i][j]);
                dtw_bh[j][i] = dtw_bh[i][j];
                dtw_bv[j][i] = dtw_bv[i][j];

                //System.out.println();
            }
            //System.out.println();
        }

        double[] weight = new double[numOfTags];
        for (int i =0 ; i < numOfTags; i++) {
            for(int j = 0 ; j < numOfTags; j++) {
                weight[i] = + dtw_bh[i][j];
            }
        }
        //System.out.println("weight : "+weight.length);

        //
        // 아래 대표패턴 선출 알고리즘은 2017-01-06 일자 서울대 자료 pattern.py 에 구현되어 있는 것을 따른다. (수집을 위해 3개의 Tag 를 사용한 경우에 해당)
        //
        // 2017-01-23 현재 Tag 하나 이용하여 BLE data 수집하는 경우가 있으니 , 이는 패턴선출 과정에서 제외시킨다
        //


        if (numOfTags == 1) {
            // outlier 버리고 , 모두 대표패턴 인정 했으면 좋겠으나 ,
            // 일단 모두 대표로 인정

            for (Map.Entry<String, List<RawdataMagnetic>> entry : gTag.entrySet()) {
                List<RawdataMagnetic> candidate= entry.getValue();
                System.out.println("numOfTags : "+numOfTags);
                patternList.add(candidate);
            }

        } if (numOfTags == 2) {
// 2017-02-02 현재 지자기 패턴선출을 위한 지자기 수집은 Tag 3개를 사용하는 것으로 한정한다.. 그러므로 tag갯수 2개인 경우는 pass

            // tag 가 2개이면 relation 은 [0,1] 1개
            boolean pattern_mat = false;
            if ((dtw_bh[0][1] < 1.3) && (dtw_bv[0][1] < 1.3)) pattern_mat = true;
            // 두개가 같으면 ( relation 이 true 이면 ) 아무거나 임의로 하나 대표 인정
            boolean[] repr_mat = {false, false};
            if(pattern_mat == true) repr_mat[0] = true;

            // 두개가 다르면 각각 대표인정
             else {
                repr_mat[0] = true;
                repr_mat[1] = true;
            }
            int idx = 0;
            for (Map.Entry<String, List<RawdataMagnetic>> entry : gTag.entrySet()) {
                List<RawdataMagnetic> candidate= entry.getValue();
                if (repr_mat[idx] == true) {
                    patternList.add(candidate);
                }
                idx++;
            }

        } else if (numOfTags == 3) {
            boolean[] pattern_mat = {false, false, false};

            if ((dtw_bh[0][1] < 1.3) && (dtw_bv[0][1] < 1.3)) pattern_mat[0] = true;
            if ((dtw_bh[1][2] < 1.3) && (dtw_bv[1][2] < 1.3)) pattern_mat[1] = true;
            if ((dtw_bh[2][0] < 1.3) && (dtw_bv[2][0] < 1.3)) pattern_mat[2] = true;

            int same_pattern_cnt = 0;
            for(int i = 0; i < numOfTags; i++) {
                if(pattern_mat[i]==true) same_pattern_cnt++;
            }

            logger.debug("Same Pattern Count : "+same_pattern_cnt);

            boolean[] repr_mat = {false, false, false};
            if (same_pattern_cnt == 3) {     // 3개의 패턴이 같으면 1개의 대표
                repr_mat[findIndexMin(weight)] = true;
            } else if (same_pattern_cnt == 2) {    // 2개의 패턴이 같으면 1개의 대표  dtw 각 2개의 합이 제일 작은 것이 대표
                if (pattern_mat[0] == false) {
                    repr_mat[2] = true;
                } else if (pattern_mat[1] == false) {
                    repr_mat[0] = true;
                } else repr_mat[1] = true;
            } else if (same_pattern_cnt == 1) {   // 1개의 패턴이 같으면 2 개의 대표
                if (pattern_mat[0] == true) {
                    repr_mat[2] = true;
                    // repr_mat[random.choice([0, 1])] =true;
                    repr_mat[0] =true;
                } else if (pattern_mat[1] == true) {
                    repr_mat[0] = true;
                    //repr_mat[random.choice([1, 2])] =true;
                    repr_mat[1] =true;
                } else {
                    repr_mat[1] = true;
                    //repr_mat[random.choice([0, 2])] =true;
                    repr_mat[0] = true;
                }
            } else {                     // 같은 패턴이 없으면 3개 모두 대표
                repr_mat[0] = true;
                repr_mat[1] = true;
                repr_mat[2] = true;
            }

            //System.out.println("repr_mat");
            for (int i = 0 ; i <numOfTags ; i++) {
                System.out.print(repr_mat[i]);
                System.out.print(" ");
            }
            //System.out.println();

            int idx = 0;
            for (Map.Entry<String, List<RawdataMagnetic>> entry : gTag.entrySet()) {
                List<RawdataMagnetic> candidate= entry.getValue();
                if (repr_mat[idx] == true) {
                    patternList.add(candidate);
                }
                idx++;
            }

            // 현재 수집목적으로 tag 3개까지 사용하는 것을 권장
            // 4개 부터는 3개일때와 비슷한 논리로 대표패턴 선출을 구현 필요.
            // 5개 이상일때는 현재 논리로 구현이 어려움.
            // k-means 사용 ( k 값은 Dirichlet process 로 산출가능 )

        } else if (numOfTags == 4) {
            System.out.println("현재로서는 수집태그 4개이상의 경우에 대해 대표패턴 선출을 진행할 수 없습니다");
            logger.debug("현재로서는 수집태그 4개이상의 경우에 대해 대표패턴 선출이 구현되어 있지 않습니다.");
        } else if (numOfTags > 4) {
            System.out.println("현재로서는 수집태그 4개이상의 경우에 대해 대표패턴 선출을 진행할 수 없습니다");
            logger.debug("현재로서는 수집태그 4개이상의 경우에 대해 대표패턴 선출이 구현되어 있지 않습니다.");
        }

        return patternList;
    }


    List<Double> zero_mean(List<Double> training) {
        List<Double> ret;

        Double average = training.stream().mapToDouble(val -> val).average().getAsDouble();

        ret = training.stream().mapToDouble(x -> x - average).boxed().collect(Collectors.toList());

        return ret;
    }




    public int findIndexMin(double[] weight) {
        int indexMin = 0;
        double min = weight[0];
        for(int i=0;i<weight.length;i++){
            if(weight[i]<min){
                min=weight[i];
                indexMin = i;
            }
        }
        return indexMin;
    }

    public double[] to_double_array(List<Double> srcList) {
        double[] target = new double[srcList.size()];
        for (int i = 0; i < target.length; i++) {
            target[i] = srcList.get(i);                // java 1.5+ style (outboxing)
        }
        return target;
    }

    //
    // 교차셀 데이터 Cleansing 을 위해 설계한 인터페이스
    //
    public void moveCells() {

        List<RawdataMagnetic> comp1=new ArrayList<>();
        List<RawdataMagnetic> comp2=new ArrayList<>();
        int object_campus_id = mFloor.getCampus_id();
        int object_building_id = mFloor.getBuilding_id();
        int object_floor_id = mFloor.getFloor_id();


        String pack1= "a9ccf56a220b4d098da2cec8165c2cf9"; // 짧은 링크. 이동될 셀을 포함하고 있다
        String pack2= "062deec8cbc4414b8eb425abe99cfece"; // 긴 링크, 기준 링크

        try {
            comp1 = getRawData(object_campus_id, object_building_id, object_floor_id, pack1);
            comp2 = getRawData(object_campus_id, object_building_id, object_floor_id, pack2);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        List<String> cell_list1 = comp1.stream().map(x -> x.getC_cell_id()).distinct().collect(Collectors.toList());
        List<String> cell_list2 = comp2.stream().map(x -> x.getC_cell_id()).distinct().collect(Collectors.toList());

        System.out.println();

        for (String obj:cell_list1)
            if (cell_list2.contains(obj))
            {
                System.out.println(obj);
                //comp2.forEach(System.out::println);
                List<RawdataMagnetic> sublist1 = comp1.stream().filter(x -> obj.equals(x.getC_cell_id())).collect(Collectors.toList());
                List<RawdataMagnetic> sublist2 = comp2.stream().filter(x -> obj.equals(x.getC_cell_id())).collect(Collectors.toList());

                for(RawdataMagnetic raw1 : sublist1 ) {
                   // System.out.println("\ntag_id : "+raw1.getTag_id()+"\tcell_id : "+raw1.getC_cell_id()+"\trssi_dir : "+raw1.getRssi_dir()+"\tPosX : "+ raw1.getPosition_x()+"\tPosY : "+raw1.getPosition_y()
                    //        +" \tBF : "+Math.sqrt(Math.pow(raw1.getMag_bv(),2)+Math.pow(raw1.getMag_bh(),2)));
                    System.out.println("\tPackage_collect_key : "+raw1.getPackage_collect_key()+"\tC_cell_id : "+raw1.getC_cell_id()+"\tTag_id : "+raw1.getTag_id()+"\tSequence_num : "+raw1.getSequence_num());

                    try {
                        clearTagData(object_campus_id, object_building_id, object_floor_id, raw1.getPackage_collect_key(), raw1.getC_cell_id());
                        clearAPData(object_campus_id, object_building_id, object_floor_id, raw1.getTag_id(), raw1.getSequence_num());
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            }
    }


    //
    // NDir_Normalization_Factor 적용된 이동각도값산출 테스트용 인터페이스
    //
    public void CompareOnline() {
        List<String> cell_list=null;
        List<RawdataMagnetic> online_data=null;
        List<CellStatistics> fp_list=null;
        try {
            cell_list = query1702150115();
            online_data = query1702151552();
            fp_list = query1702150135();

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        //fp_list.forEach(System.out::println);


        int iter;
        int value=0;

        for(RawdataMagnetic item : online_data) {

            iter = 0;
            for (String retval: item.getC_cell_id().split(",")) {
                if (iter == 0) value = (Integer.valueOf(retval) +1  )* 10000;
                else {
                    value = value + Integer.valueOf(retval)+1 ;
                    //System.out.print("\t"+value+"\t");
                    //System.out.print("\t");
                    for(CellStatistics fp : fp_list) {
                        if(fp.getMag_cell_id()==value) {
                            //System.out.print(fp.getNdir_norm_factor());
                            //System.out.print("\t");
                            //System.out.print(item.getNorth_dir());
                            System.out.print("\t");
                            System.out.println(item.getNorth_dir() - fp.getNdir_norm_factor());
                        }
                    }

                }
                iter++;
            }
        }
    }
    public void InspectFP() {
        List<Double> aa;
        aa = dummySQL();
        aa.forEach(System.out::println);
    }

    public void InspectMagData() {
        int ble_cell_id;
        ble_cell_id = CalculateBLECellID(460039);
        System.out.println(ble_cell_id);
        int pixels_per_cell = (int)Math.round(mFloor.getSize_cell_mag() / mFloor.getScale_pixel_map());  // 휴빌론의 경우 21 픽셀
        List<Integer> mag_cell_id_list = CalculateMagCellID(ble_cell_id);
        //System.out.println();
        //mag_cell_id_list.forEach(System.out::println);
        int s = mag_cell_id_list.get(0);
        int e = mag_cell_id_list.get(mag_cell_id_list.size() - 1);

        //System.out.printf("start mag cell : %d , end mag cell : %d", s,e);
        //System.out.println();
        //System.out.println();

        int coordinate_pixel_x1 = (s / 10000 - 1) * pixels_per_cell;
        int coordinate_pixel_x2 = e / 10000 * pixels_per_cell-1;
        int coordinate_pixel_y1 = ( s % 10000 -1 ) * pixels_per_cell;
        int coordinate_pixel_y2 = e % 10000 * pixels_per_cell-1;

        System.out.println(coordinate_pixel_x1+"\t"+coordinate_pixel_y1+"\t"+coordinate_pixel_x2+"\t"+coordinate_pixel_y2);
    }

    public void testDTW() {

        List<Double> bhList=null;
        List<Double> zero_bh_list=null;
        List<Double> bvList=null;
        List<Double> bhList2=null;
        List<Double> zero_bh_list2=null;
        List<Double> bvList2=null;

        try {
            bhList = sql_bh_07F5();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        System.out.println("size of bhList : "+bhList.size());
        try {
            bvList = sql_dn07f5v();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        /*
        double[] bh_dn8F71 = new double[bhList.size()];
        for(int i = 0; i < bhList.size(); i++){
            bh_dn8F71[i] = bhList.get(i);
        }
*/
        try {
            bhList2 = sql_bh_202f();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        System.out.println("size of bhList2 : "+bhList2.size());

        try {
            bvList2 = sql_dn202fv();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
/*
        double[] bh_dnA686 = new double[bhList2.size()];
        for(int i = 0; i < bhList2.size(); i++){
            bh_dnA686[i] = bhList2.get(i);
        }
*/

       // computeDtwDistance((ArrayList)bhList,(ArrayList)bvList, (ArrayList)bhList2,(ArrayList)bvList2);

        zero_bh_list = zero_mean(bhList);
        zero_bh_list2 = zero_mean(bhList2);

        // Compare two TimeSeries
        TimeSeries tsI = new TimeSeries((ArrayList)zero_bh_list);
        TimeSeries tsJ = new TimeSeries((ArrayList)zero_bh_list2);
        DistanceFunction distFn;

        distFn = DistanceFunctionFactory.getDistFnByName("ManhattanDistance");


        TimeWarpInfo info = DTW.getWarpInfoBetween(tsI, tsJ, distFn);
        System.out.println("Warp Distance: " + info.getDistance());
        System.out.println("Warp Path:     " + info.getPath());
        System.out.println("Warp Path Size:     " + info.getPath().size());

        System.out.println("DTW Distance:     " + info.getDistance()/info.getPath().size());
    }


    private static double computeAverage(ArrayList<Double> list) {
        double avg = 0;
        for (double v : list) {
            avg += v;
        }
        return avg / list.size();
    }

    private static double computeDtwDistance(ArrayList<Double> bhHistoryList, ArrayList<Double> bvHistoryList,
                                      ArrayList<Double> bhObservedList, ArrayList<Double> bvObservedList) {
        double bhHistoryAvg = computeAverage(bhHistoryList);
        double bvHistoryAvg = computeAverage(bvHistoryList);
        double bhObservedAvg = computeAverage(bhObservedList);
        double bvObservedAvg = computeAverage(bvObservedList);
        ArrayList<Double> bhHistoryZeroList = new ArrayList<Double>();
        ArrayList<Double> bvHistoryZeroList = new ArrayList<Double>();
        ArrayList<Double> bhObservedZeroList = new ArrayList<Double>();
        ArrayList<Double> bvObservedZeroList = new ArrayList<Double>();

        for (int i = 0; i < bhObservedList.size(); i++) {
            bhObservedZeroList.add(bhObservedList.get(i) - bhObservedAvg);
            bvObservedZeroList.add(bvObservedList.get(i) - bvObservedAvg);
            bhHistoryZeroList.add(bhHistoryList.get(i) - bhHistoryAvg);
            bvHistoryZeroList.add(bvHistoryList.get(i) - bvHistoryAvg);
        }
        double distance0 = computeDtwScore(bhHistoryZeroList, bhObservedZeroList);
        System.out.println("distance0 : "+distance0/bhObservedList.size());

        double distance = computeDtwScore(bhHistoryZeroList, bhObservedZeroList)
                + computeDtwScore(bvHistoryZeroList, bvObservedZeroList);
        System.out.println("d: " + String.format("%.2f", distance/bhObservedList.size()));

        return (distance/bhObservedList.size());
    }

    private static double computeDtwScore(ArrayList<Double> list1, ArrayList<Double> list2) {
        TimeSeries ts1 = new TimeSeries(list1);
        TimeSeries ts2 = new TimeSeries(list2);
        @SuppressWarnings("static-access")
        TimeWarpInfo info = FastDTW.getWarpInfoBetween(ts1, ts2, 6, new DistanceFunctionFactory().getDistFnByName("EuclideanDistance"));
        return info.getDistance();
    }

    //
    // DTW 거리산출을 위한 테스트용 인터페이스
    //
    public static List<Double> sql_dn07f5v() throws Throwable {
        List<Double> ret;

        MariaConnectionManager mngr = MariaConnectionManager.getInstance();

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            ret = mapper.sql_dn07f5v();
            session.commit();
        } catch (PersistenceException e) {
            throw e.getCause();
        }

        return ret;
    }

    //
    // DTW 거리산출을 위한 테스트용 인터페이스
    //
    public static List<Double> sql_bh_07F5() throws Throwable {
        List<Double> ret;

        MariaConnectionManager mngr = MariaConnectionManager.getInstance();

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            ret = mapper.bh_07F5();
            session.commit();
        } catch (PersistenceException e) {
            throw e.getCause();
        }

        return ret;
    }

    //
    // DTW 거리산출을 위한 테스트용 인터페이스
    //
    public static List<Double> sql_bh_202f() throws Throwable {
        List<Double> ret;
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            ret = mapper.bh_202f();
            session.commit();
        } catch (PersistenceException e) {
            throw e.getCause();
        }
        return ret;
    }

    //
    // DTW 거리산출을 위한 테스트용 인터페이스
    //
    public static List<Double> sql_dn202fv() throws Throwable {
        List<Double> ret;
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            ret = mapper.sql_dn202fv();
            session.commit();
        } catch (PersistenceException e) {
            throw e.getCause();
        }
        return ret;
    }

    //
    // NDir_Normalization_Factor 적용된 이동각도값산출 테스트용 인터페이스
    //
    public static List<String> query1702150115() throws Throwable {
        List<String> ret;
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            ret = mapper.query1702150115();
            session.commit();
        } catch (PersistenceException e) {
            throw e.getCause();
        }
        return ret;
    }


    //
    // NDir_Normalization_Factor 적용된 이동각도값산출 테스트용 인터페이스
    //
    public static List<CellStatistics> query1702150135() throws Throwable {
        List<CellStatistics> ret;
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            ret = mapper.query1702150135();
            session.commit();
        } catch (PersistenceException e) {
            throw e.getCause();
        }
        return ret;
    }

    //
    // NDir_Normalization_Factor 적용된 이동각도값산출 테스트용 인터페이스
    //
    public static List<RawdataMagnetic> query1702151552() throws Throwable {
        List<RawdataMagnetic> ret;
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            ret = mapper.query1702151552();
            session.commit();
        } catch (PersistenceException e) {
            throw e.getCause();
        }
        return ret;
    }

    //
    // 교차셀 데이터 Cleansing 을 위해 설계한 인터페이스
    //
    public static void clearTagData(int campus, int building, int floor, String PACKAGE_COLLECT_KEY, String C_CELL_ID) throws Throwable {
        List<RawdataMagnetic> ret;
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            mapper.query1702151921(campus,building,floor,PACKAGE_COLLECT_KEY,C_CELL_ID);
            session.commit();
        } catch (PersistenceException e) {
            throw e.getCause();
        }

    }

    //
    // 교차셀 데이터 Cleansing 을 위해 설계한 인터페이스
    //
    public static void clearAPData(int campus, int building, int floor, String TAG_ID, int SEQUENCE_NUM) throws Throwable {
        List<RawdataMagnetic> ret;
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            mapper.query1702151938(campus,building,floor,TAG_ID,SEQUENCE_NUM);
            session.commit();
        } catch (PersistenceException e) {
            throw e.getCause();
        }

    }


    List<Double>  dummySQL() {
        List<Double> result=new ArrayList<>();
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            result = mapper.dummySQL();
            session.commit();
        } catch (PersistenceException e) {
            //
        }

        return result;
    }

}