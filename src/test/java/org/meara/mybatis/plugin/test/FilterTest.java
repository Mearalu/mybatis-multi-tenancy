package org.meara.mybatis.plugin.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.meara.mybatis.plugin.TenantInfoImpl;
import org.meara.mybatis.plugin.filter.RegExMultiTenancyFilter;
import org.meara.mybatis.plugin.parser.DefaultSqlParser;

/**
 * 过滤器测试
 * Created by Meara on 2016/8/15.
 */
public class FilterTest {
    DefaultSqlParser defaultSqlParser;

    @Before
    public void init() {
        defaultSqlParser = new DefaultSqlParser()
                .setMultiTenancyFilter(new RegExMultiTenancyFilter().setFilterDefault(true))
                .setTenantIdColumn("tid")
                .setTenantInfo(new TenantInfoImpl());
    }
    @Test
    public void tableAllowTest() {
        defaultSqlParser.setMultiTenancyFilter(new RegExMultiTenancyFilter()
                .setFilterDefault(true)
                .setFilterStatementPatterns("")
                .setFilterTablePatterns("^book")
        );
        String sql = "SELECT bid, book_name FROM book";
        String newSql = defaultSqlParser.setTenantParameter(sql);
        Assert.assertNotEquals("SELECT bid, book_name FROM book WHERE book.tid = '2'",
                newSql);
    }
    @Test
    public void tableDenyTest() {
        defaultSqlParser.setMultiTenancyFilter(new RegExMultiTenancyFilter()
                .setFilterDefault(false)
                .setFilterStatementPatterns(".*")
                .setFilterTablePatterns("^book")
        );
        String sql = "SELECT bid, book_name FROM book";
        String newSql = defaultSqlParser.setTenantParameter(sql);
        Assert.assertEquals("SELECT bid, book_name FROM book WHERE book.tid = '2'",
                newSql);
    }
}
