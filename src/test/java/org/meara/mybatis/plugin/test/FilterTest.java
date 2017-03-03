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
        defaultSqlParser=new DefaultSqlParser();
    }

    @Test
    public void tableAllowTest() {
        defaultSqlParser .setTenantInfo(new TenantInfoImpl()
                .setTenantIdColumn("tenant_id")
                .setMultiTenancyFilter(
                        new RegExMultiTenancyFilter()
                                .setFilterDefault(true)
                        .setFilterStatementRegexStr("")
                        .setFilterTableRegexStr("^book")
                )
        );
        String sql = "SELECT bid, book_name FROM book";
        String newSql = defaultSqlParser.setTenantParameter(sql);
        Assert.assertNotEquals("SELECT bid, book_name FROM book WHERE book.tenant_id = '2'",
                newSql);
    }

    @Test
    public void tableDenyTest() {
        defaultSqlParser .setTenantInfo(new TenantInfoImpl()
                .setTenantIdColumn("tenant_id")
                .setMultiTenancyFilter(
                        new RegExMultiTenancyFilter()
                                .setFilterDefault(false)
                                .setFilterStatementRegexStr(".*")
                                .setFilterTableRegexStr("^book")
                )
        );
        String sql = "SELECT bid, book_name FROM book";
        String newSql = defaultSqlParser.setTenantParameter(sql);
        Assert.assertEquals("SELECT bid, book_name FROM book WHERE book.tenant_id = '2'",
                newSql);
    }
}
