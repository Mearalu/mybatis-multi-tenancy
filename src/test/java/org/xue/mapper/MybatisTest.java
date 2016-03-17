package org.xue.mapper;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;
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
        List<Map> users = userDao.selectAll();
        System.out.println(users);
    }
}
