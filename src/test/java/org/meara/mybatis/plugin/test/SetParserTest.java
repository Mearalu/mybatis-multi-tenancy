package org.meara.mybatis.plugin.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.meara.mybatis.plugin.TenantInfo;
import org.meara.mybatis.plugin.TenantInfoImpl;
import org.meara.mybatis.plugin.filter.MultiTenancyFilter;
import org.meara.mybatis.plugin.filter.RegExMultiTenancyFilter;
import org.meara.mybatis.plugin.parser.DefaultSqlParser;

import java.util.Properties;

/**
 * Created by Meara on 2016/8/15.
 */
public class SetParserTest {
    DefaultSqlParser defaultSqlParser;

    @Before
    public void init() {
        defaultSqlParser = new DefaultSqlParser()
                .setTenantInfo(
                        new TenantInfoImpl()
                        .setMultiTenancyFilter(
                                new RegExMultiTenancyFilter().setFilterDefault(true)
                        )
                );
    }

    @Test
    public void  test(){
        String sql="SELECT bid, book_name FROM book";
        String newSql = defaultSqlParser.setTenantParameter(sql);
        Assert.assertEquals("SELECT bid, book_name FROM book WHERE book.tenant_id = '2'",
                newSql);
    }
}
