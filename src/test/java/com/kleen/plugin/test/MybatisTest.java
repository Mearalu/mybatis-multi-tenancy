package com.kleen.plugin.test;

import com.kleen.plugin.mapper.UserMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Meara on 2016/3/17.
 */
public class MybatisTest {
    @Test
    public void query(){
        InputStream is = MybatisTest.class.getClassLoader().getResourceAsStream("mybatisConf.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
        SqlSession session= sqlSessionFactory.openSession();
        UserMapper userDao = session.getMapper(UserMapper.class);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("uid",8);
        map.put("username","kleen");
        map.put("password","123");

        userDao.insert(map);
        session.commit();
    }
}
