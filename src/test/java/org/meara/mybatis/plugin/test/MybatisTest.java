package org.meara.mybatis.plugin.test;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.meara.mybatis.plugin.mapper.BookMapper;
import org.meara.mybatis.plugin.mapper.UserMapper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Meara on 2016/3/17.
 */
public class MybatisTest {
    private SqlSession sqlSession;

    @Before
    public void init() {
        InputStream is = MybatisTest.class.getClassLoader().getResourceAsStream("mybatisConf.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
        sqlSession = sqlSessionFactory.openSession(false);
    }

    /**
     * 匹配测试
     */
    @Test
    public void queryTest() {
        BookMapper userDao = sqlSession.getMapper(BookMapper.class);
        List<Map> users = userDao.selectAll();
        Assert.assertTrue(users.size() == 1);
    }

    /**
     * 忽略
     */
    @Test
    public void ignoreTest() {
        UserMapper userDao = sqlSession.getMapper(UserMapper.class);
        List<Map> users = userDao.selectWhere(0);
        Assert.assertTrue(users.size() == 2);
    }

    @Test
    public void insertTest() {
        BookMapper bookMapper = sqlSession.getMapper(BookMapper.class);
        Map<String, Object> newBook = new HashMap<>();
        newBook.put("bid",3);
        newBook.put("bookName", "水浒传");
        bookMapper.insertBook(newBook);
        Map book = bookMapper.selectById(3);
        Assert.assertTrue("2".equals(Objects.toString(book.get("tenant_id"))));
    }

    @Test
    public void insertSelectTest() {
        BookMapper bookMapper = sqlSession.getMapper(BookMapper.class);
        Map<String, Object> newBook = new HashMap<>();
        newBook.put("bid",4);
        bookMapper.insertSelect(newBook);
        Map book = bookMapper.selectById(4);
        Assert.assertTrue("2".equals(Objects.toString(book.get("tenant_id"))));
    }

    @After
    public void rollback() {
        sqlSession.rollback(true);
        sqlSession.close();
    }
}
