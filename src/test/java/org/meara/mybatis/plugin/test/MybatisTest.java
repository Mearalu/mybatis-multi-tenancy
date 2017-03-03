package org.meara.mybatis.plugin.test;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.meara.mybatis.plugin.mapper.BookMapper;
import org.meara.mybatis.plugin.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Meara on 2016/3/17.
 */
public class MybatisTest {
    private SqlSession sqlSession;
    private static final Logger logger = LoggerFactory.getLogger(MybatisTest.class);

    //h2数据库字段为大写
    @Before
    public void init() throws IOException, SQLException {
        InputStream is = MybatisTest.class.getClassLoader().getResourceAsStream("mybatisConf.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);


        sqlSession = sqlSessionFactory.openSession(false);

        //执行初始化脚本
        Connection connection = sqlSession.getConnection();
        ScriptRunner runner = new ScriptRunner(connection);
        Resources.setCharset(Charset.forName("UTF-8"));
        runner.setLogWriter(null);//设为null则不输出日志
        runner.runScript(
                new InputStreamReader(
                        MybatisTest.class.getClassLoader().getResourceAsStream("test-init.sql")
                ));
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
        newBook.put("bid", 3);
        newBook.put("bookName", "水浒传");
        bookMapper.insertBook(newBook);
        Map book = bookMapper.selectById(3);
        Assert.assertTrue("2".equals(Objects.toString(book.get("TENANT_ID"))));
    }

    @Test
    public void insertSelectTest() {
        BookMapper bookMapper = sqlSession.getMapper(BookMapper.class);
        Map<String, Object> newBook = new HashMap<>();
        newBook.put("bid", 4);
        bookMapper.insertSelect(newBook);
        Map book = bookMapper.selectById(4);
        Assert.assertTrue("2".equals(Objects.toString(book.get("TENANT_ID"))));
    }

    @Test
    public void updateTest() {
        BookMapper bookMapper = sqlSession.getMapper(BookMapper.class);
        Map map = new HashMap();
        map.put("bid", "2");
        map.put("bookName", "封神榜");
        bookMapper.updateBook(map);
        sqlSession.commit(true);
    }

    @Test
    public void updateNoWhereBook() {
        BookMapper bookMapper = sqlSession.getMapper(BookMapper.class);
        Map map = new HashMap();
        map.put("bookName", "xiaozhang");
        bookMapper.updateNoWhereBook(map);
        sqlSession.commit(true);
    }


    @After
    public void rollback() {
//        sqlSession.rollback(true);
        sqlSession.close();
    }
}
