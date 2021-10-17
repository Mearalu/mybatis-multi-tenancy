package org.meara.mybatis.plugin.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.meara.mybatis.plugin.TenantInfoImpl;
import org.meara.mybatis.plugin.filter.RegExMultiTenancyFilter;
import org.meara.mybatis.plugin.parser.DefaultSqlParser;

/**
 * Created by Meara on 2016/8/15.
 */
public class SetParserTest {
    DefaultSqlParser defaultSqlParser;

    @BeforeEach
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
    public void test() {
        String sql = "SELECT bid, book_name FROM book";
        String newSql = defaultSqlParser.setTenantParameter(sql);
        Assertions.assertEquals("SELECT bid, book_name FROM book WHERE book.tenant_id = '2'",
                newSql);
    }
}
