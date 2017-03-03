package org.meara.mybatis.plugin.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.meara.mybatis.plugin.MultiTenancy;
import org.meara.mybatis.plugin.filter.RegExMultiTenancyFilter;
import org.meara.mybatis.plugin.TenantInfoImpl;
import org.meara.mybatis.plugin.parser.DefaultSqlParser;

/**
 * sql处理测试
 * Created by Meara on 2016/8/14.
 */
public class ParserTest {
    DefaultSqlParser defaultSqlParser;

    @Before
    public void init() {
        TenantInfoImpl tenantInfo = new TenantInfoImpl()
                .setMultiTenancyFilter(
                        new RegExMultiTenancyFilter().setFilterDefault(true));
        defaultSqlParser = new DefaultSqlParser()
                .setTenantInfo(tenantInfo);
    }

    /**
     * 查询语句
     */
    @Test
    public void selectTest() {
        String sql = "SELECT bid, book_name FROM book";
        String newSql = defaultSqlParser.setTenantParameter(sql);
        Assert.assertEquals("SELECT bid, book_name FROM book WHERE book.tid = '2'",
                newSql);
    }

    /**
     * INSERT INTO SELECT语句测试
     */
    @Test
    public void insertSelectTest() {
        String sql = "INSERT INTO book(bid, book_name) SELECT ?,username FROM `user`LIMIT 1";
        String newSql = defaultSqlParser.setTenantParameter(sql);
        Assert.assertEquals("INSERT INTO book (bid, book_name, tid) SELECT ?, username, '2' FROM `user` WHERE `user`.tid = '2' LIMIT 1",
                newSql);
    }

    /**
     * 左联接以及别名测试
     */
    @Test
    public void leftJoinTest() {
        String sql = "SELECT a1.id ,a2.name as a2Name,a3.name as a3Name  FROM t1 a1   LEFT JOIN t2 a2 ON a1.id = a2.id LEFT JOIN t2 a3 ON a1.gid = a3.id";
        String newSql = defaultSqlParser.setTenantParameter(sql);
        Assert.assertEquals("SELECT a1.id, a2.name AS a2Name, a3.name AS a3Name FROM t1 a1 LEFT JOIN t2 a2 ON a2.tid = '2' AND a1.id = a2.id LEFT JOIN t2 a3 ON a3.tid = '2' AND a1.gid = a3.id WHERE a1.tid = '2'",
                newSql);
    }

    /**
     * 子查询测试
     */
    @Test
    public void selectSelectTest() {
        String sql = "select a from table1 where  column1= (select column1 from table2 where a=1 ) ";
        String newSql = defaultSqlParser.setTenantParameter(sql);
        Assert.assertEquals("SELECT a FROM table1 WHERE table1.tid = '2' AND column1 = (SELECT column1 FROM table2 WHERE table2.tid = '2' AND a = 1)",
                newSql);
    }
}
