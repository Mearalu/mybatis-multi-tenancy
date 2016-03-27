package org.xue.test;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.xue.mapper.UserMapper;

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
//        List<Map> users = userDao.selectAll();
        List<Map> users = userDao.selectWhere(0L);
        System.out.println(users);
        Assert.assertTrue(users.size()==1);
    }
}
