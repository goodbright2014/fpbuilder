package com.skt.ehs.mbs.fpbuilder;

import com.skt.ehs.mbs.fpbuilder.blefpbuilder.BLEFpBuilder;
import com.skt.ehs.mbs.fpbuilder.database.FpBuilderDAO;
import com.skt.ehs.mbs.fpbuilder.database.MariaConnectionManager;
import com.skt.ehs.mbs.fpbuilder.database.ReadInputDAO;
import com.skt.ehs.mbs.fpbuilder.magfpbuilder.MagFpBuilder;
import org.apache.ibatis.session.SqlSession;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;



/**
 * Created by user on 2016-12-16.
 */
public class FPManager {

    // property file name
    public static final String PROP_FILE_NAME = "config.properties";
    public static final String PROP_CELLSIZE_MAG = "CELL_SIZE_MAG";
    public static final String PROP_CELLSIZE_BLE = "CELL_SIZE_BLE";

    private List<_menu> cMenu;
    private List<_menu> bMenu;
    private List<_menu> fMenu;

    public static int _table_img_width;
    public static int _table_img_height;
    public static double _table_scale_pixel;

    // menu
    public static int _console_campus_id;
    public static int _console_building_id;
    public static int _console_floor_id;
    // conf.properties
    private static int _prop_cell_size_mag;
    private static int _prop_cell_size_ble;

    public static boolean LoadDBConnectionPool() {

        if (!MariaConnectionManager.getInstance().makeConnectionPool()) {
            return false;
        }
        return true;
    }

    public static void readMapImage(int campus, int building, int floor) {

            MariaConnectionManager mngr = MariaConnectionManager.getInstance();

            try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
                FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
                _table_img_width = mapper.selectMapX(campus,building,floor);
            }
            try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
                FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
                _table_img_height = mapper.selectMapY(campus,building,floor);
            }
            try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
                FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
                _table_scale_pixel = mapper.selectMapScale(campus,building,floor);
            }
    }


    public static boolean ble_fp() throws Throwable{

        if (_prop_cell_size_ble == 0 || _console_campus_id == 0 || _console_building_id == 0 || _console_floor_id == 0) return false;

        // FP구축할 대상 층의 지도정보 table에 접근하여 class variable ( _table_img_width, _table_img_height, _table_scale_pixel) 을 세팅한다
        readMapImage(_console_campus_id, _console_building_id, _console_floor_id );

        System.out.println( "x, y, scale : "+_table_img_width+" "+_table_img_height+" "+_table_scale_pixel);

        int M = ((int)(_table_img_width * _table_scale_pixel) % _prop_cell_size_ble == 0)? ( (int)(_table_img_width * _table_scale_pixel) / _prop_cell_size_ble ) : ((int)(_table_img_width * _table_scale_pixel) / _prop_cell_size_ble )+1;
        int N = ((int)(_table_img_height * _table_scale_pixel) % _prop_cell_size_ble == 0)? ( (int)(_table_img_height * _table_scale_pixel)  / _prop_cell_size_ble ) : ((int)(_table_img_height * _table_scale_pixel)  / _prop_cell_size_ble )+1;

        System.out.println( "m, n : "+M+" "+N);

        BLEFpBuilder fp = new BLEFpBuilder(M,N,_prop_cell_size_ble, _prop_cell_size_mag ,_console_campus_id, _console_building_id, _console_floor_id);  // mFloor

        fp.LoadCellID();
        fp.LoadCellLocation();

        try {
            fp.build2();
            //fp.build();
            fp.build_fp_ble_cell2();
            //fp.build_fp_ble_cell();

            fp.build_fp_ble_floor_math();

        } catch (Throwable throwable) {
            throw throwable;
        }

        return true;

    }

    public static boolean mag_fp () throws Throwable{

        if (_prop_cell_size_mag == 0  || _console_campus_id == 0 || _console_building_id == 0 || _console_floor_id == 0 ) return false;

        // FP구축할 대상 층의 지도정보 table에 접근하여 class variable ( _table_img_width, _table_img_height, _table_scale_pixel) 을 세팅한다
        readMapImage(_console_campus_id, _console_building_id, _console_floor_id );

        /*

            매트릭스 분배를 이미지 사이즈 기준으로 시도 -> 먼저 셀크기를 pixel 단위로 변환

         */

        int pixels_per_cell = (int)Math.round(_prop_cell_size_mag / _table_scale_pixel);

        int M = (_table_img_width % pixels_per_cell == 0)? _table_img_width/pixels_per_cell:_table_img_width/pixels_per_cell+1;
        int N = (_table_img_height % pixels_per_cell == 0)? _table_img_height/pixels_per_cell:_table_img_height/pixels_per_cell+1;


        MagFpBuilder fp_mag = new MagFpBuilder(M,N, _prop_cell_size_mag, _prop_cell_size_ble, _console_campus_id, _console_building_id, _console_floor_id);


        // 1.flooring part  A.give cell id, B.give cell location
        fp_mag.LoadCellID();
        fp_mag.LoadCellLocation();

        // 2. FP작성

        // TODO 임시로 닫음. 주석기호 제거


        //fp_mag.buildStatistics();
       // fp_mag.buildClustering();


        //fp_mag.testDTW();
        //fp_mag.testNDir();
        //fp_mag.CompareOnline();
        //fp_mag.moveCells();
        //fp_mag.InspectFP();
        //fp_mag.InspectMagData();
        fp_mag.magCellGetMDirCount(_console_campus_id, _console_building_id, _console_floor_id);

        return true;
    }

    private static class _menu {
        int _menu;
        int _id;
        String _name;

        public _menu(int menu, int id, String name) {
            this._menu = menu;
            this._id = id;
            this._name = name;
        }

        public void setMenu(int menu) {
            this._menu = menu;
        }

        public void setId(int id) {
            this._id = id;
        }

        public void setName(String name) {
            this._name = name;
        }

        @Override
        public String toString() {
            return "_menu{" +
                    "_menu=" + _menu +
                    ", _id=" + _id +
                    ", _name='" + _name + '\'' +
                    '}';
        }
    }

    public static void selectMenuFloor(List<_menu> menu, int bid) {
        System.out.println("FP를 구축하고자하는 해당 층을 선택하세요. ( q : quit )");
        List<Integer> floor_idList;

        MariaConnectionManager mngr = MariaConnectionManager.getInstance();
        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            ReadInputDAO mapper = session.getMapper(ReadInputDAO.class);

            floor_idList = mapper.selFloor(bid);
            int iter = 0;
            for(int f_id : floor_idList) {
                String f_name = mapper.selFloorName(f_id);
                iter++;
                _menu menu_item = new _menu(iter, f_id, f_name);
                System.out.println(iter+". "+f_name);

                menu.add(menu_item);
            }
        }
    }


    public static void selectMenuBuilding(List<_menu> menu, int campus) {
        System.out.println("빌딩을 선택하세요. ( q : quit )");

        List<Integer> building_idList;
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();
        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            ReadInputDAO mapper = session.getMapper(ReadInputDAO.class);

            building_idList = mapper.selBuilding(campus);
            int iter = 0;
            for(int b_id : building_idList) {
                String b_name = mapper.selBuildingName(b_id);
                iter++;
                _menu menu_item = new _menu(iter, b_id, b_name);
                System.out.println(iter+". "+b_name);

                menu.add(menu_item);
            }
        }
    }

    public static void selectMenuCampus(List<_menu> menu) {
        System.out.println("\n\n아래에서 캠퍼스를 선택하세요. ( q : quit )");

        List<Integer> campus_idList;
        MariaConnectionManager mngr = MariaConnectionManager.getInstance();
        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            ReadInputDAO mapper = session.getMapper(ReadInputDAO.class);

            campus_idList = mapper.selCampus();
            int iter = 0;
            for(int camp_id : campus_idList) {

                String camp_name = mapper.selCampName(camp_id);
                iter++;
                _menu menu_item = new _menu(iter, camp_id, camp_name);
                System.out.println(iter+". "+camp_name);

                menu.add(menu_item);
            }
        }


    }

    public static void showMenu(FPManager mgr) {

        // instantiate of package private List<_menu> pMenu

        mgr.cMenu = new ArrayList<_menu>();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String input = "";

        while(!input.equalsIgnoreCase("q")) {

            selectMenuCampus(mgr.cMenu);
            try {
                input = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (input.equals("q")) break;

            for (_menu item : mgr.cMenu) {
                try {
                    if(item._menu == Integer.valueOf(input)) _console_campus_id = item._id;
                } catch (NumberFormatException e) {
                    // It's OK to ignore "e" here because returning a default value is the documented behaviour on invalid input.
                }
            }

            if(_console_campus_id==0) {
                System.out.println("번호선택이 잘못되었습니다. 처음부터 다시 시작합니다.");
                continue;
            }

            System.out.println("Selected Campus ID  : "+_console_campus_id);

            mgr.bMenu = new ArrayList<_menu>();

            selectMenuBuilding(mgr.bMenu, _console_campus_id);
            try {
                input = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (input.equals("q")) break;

            for (_menu item : mgr.bMenu) {
                try {
                    if(item._menu == Integer.valueOf(input)) _console_building_id = item._id;
                } catch (NumberFormatException e) {
                    // It's OK to ignore "e" here because returning a default value is the documented behaviour on invalid input.
                }
            }

            if(_console_building_id==0) {
                System.out.println("번호선택이 잘못되었습니다. 처음부터 다시 시작합니다.");
                continue;
            }

            System.out.println("Selected Building ID  : "+_console_building_id);

            mgr.fMenu = new ArrayList<_menu>();

            selectMenuFloor(mgr.fMenu,_console_building_id);
            try {
                input = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (input.equals("q")) break;

            for (_menu item : mgr.fMenu) {
                try {
                    if(item._menu == Integer.valueOf(input)) _console_floor_id = item._id;
                } catch (NumberFormatException e) {
                    // It's OK to ignore "e" here because returning a default value is the documented behaviour on invalid input.
                }
            }

            if(_console_floor_id==0) {
                System.out.println("번호선택이 잘못되었습니다. 처음부터 다시 시작합니다.");
                continue;
            }
            System.out.println("Selected Floor ID  : "+_console_floor_id);


            break;

        }
    }

    public static void LoadProperties() {
        Properties prop = new Properties();

        InputStream input = null;
        try {
            input = new FileInputStream(PROP_FILE_NAME);
            prop.load(input);
            String _prop_str_cell_size_mag = prop.getProperty(PROP_CELLSIZE_MAG);
            _prop_cell_size_mag = Integer.parseInt((_prop_str_cell_size_mag != null && !_prop_str_cell_size_mag.trim().isEmpty()) ? _prop_str_cell_size_mag : "0");
            String _prop_str_cell_size_ble = prop.getProperty(PROP_CELLSIZE_BLE);
            _prop_cell_size_ble = Integer.parseInt((_prop_str_cell_size_ble != null && !_prop_str_cell_size_ble.trim().isEmpty()) ? _prop_str_cell_size_ble : "0");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean delRawData() {
        // 아래는 혹시라도 main() 에서 이 함수를 호출할  수 있기에 comment 처리 해놓는다.
        // Raw Data 를 지울때는 충분한 그럴만한 이유가 있어야한다. 심사숙고 !!!
        /*
        if ( _console_campus_id == 0 || _console_building_id == 0 || _console_floor_id == 0) return false;

        MariaConnectionManager mngr = MariaConnectionManager.getInstance();

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            mapper.delRawDataBLE(_console_campus_id,_console_building_id,_console_floor_id);
            session.commit();
        }
        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            mapper.delRawDataTag(_console_campus_id,_console_building_id,_console_floor_id);
            session.commit();
        }
*/
        return false;

    }

    public static boolean deleteFP() {

        if ( _console_campus_id == 0 || _console_building_id == 0 || _console_floor_id == 0) return false;

        MariaConnectionManager mngr = MariaConnectionManager.getInstance();

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            mapper.delFpBleData(_console_campus_id,_console_building_id,_console_floor_id);
            session.commit();
        }

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            mapper.delFpBleCell(_console_campus_id,_console_building_id,_console_floor_id);
            session.commit();
        }

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            mapper.delFpBleFloor(_console_campus_id,_console_building_id,_console_floor_id);
            session.commit();
        }

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            mapper.delFpMagData(_console_campus_id,_console_building_id,_console_floor_id);
            session.commit();
        }

        try (SqlSession session = mngr.getSqlSessionFactory().openSession()) {
            FpBuilderDAO mapper = session.getMapper(FpBuilderDAO.class);
            mapper.delFpMagCluster(_console_campus_id,_console_building_id,_console_floor_id);
            session.commit();
        }

        return true;
    }

    public static void main(String[] args) {

        FPManager mgr = new FPManager();

        if(!LoadDBConnectionPool()) {
            //exit
        }

        showMenu(mgr);

        LoadProperties();

        System.out.println("campus, building, floor IDs are : "+_console_campus_id+
                " "+_console_building_id+
                " "+_console_floor_id);

        //
        //delRawData();
        //


        // TODO fp_ble_data, fp_ble_cell, fp_ble_floor , fp_mag_data, fp_mag_cluster 모두 지운다
        // delete of current FP

/*
        if(!deleteFP()) {
            System.out.println("선택된 빌딩과 층정보에 문제가 있습니다. 관리자에게 문의하세요.");
            System.exit(0);
        }


        try {
            if(!ble_fp()) System.out.println("ble_fp() did not work properly. check out config.properties file");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.exit(0);
        }
*/

        try {
            if(!mag_fp()) System.out.println("mag_fp() did not work properly. check out config.properties file");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.exit(0);
        }

    }
}
