package com.skt.ehs.mbs.fpbuilder.database;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by user on 2016-12-19.
 */
public interface ReadInputDAO {

    List<Integer> selCampus();
    String selCampName(int camp_id);

    String selBuildingName(int b_id);
    List<Integer> selBuilding(int camp_id);

    List<Integer> selFloor(int bid);
    String selFloorName(int f_id);
}
