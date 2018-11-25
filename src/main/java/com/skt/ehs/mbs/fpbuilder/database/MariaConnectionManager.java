package com.skt.ehs.mbs.fpbuilder.database;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by user on 2016-12-07.
 */
public class MariaConnectionManager {
    private static MariaConnectionManager mInstance;

    private SqlSessionFactory mSqlSessionFactory;

    public static MariaConnectionManager getInstance() {
        if (mInstance == null)
            mInstance = new MariaConnectionManager();
        return mInstance;
    }

    public synchronized boolean makeConnectionPool()
    {
        String resource = "mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        mSqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        return true;
    }

    public SqlSessionFactory getSqlSessionFactory() {
        return mSqlSessionFactory;
    }



}
