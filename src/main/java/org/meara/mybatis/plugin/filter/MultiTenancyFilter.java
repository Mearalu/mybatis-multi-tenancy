package org.meara.mybatis.plugin.filter;

import java.util.Properties;

/**
 * 过滤器接口
 * Created by Meara on 2016/5/4.
 */
public interface MultiTenancyFilter {
    void setConfig(Properties properties);

    //按照表名进行过滤
    boolean doTableFilter(String table);

    //按照statementId进行过滤
    boolean doStatementFilter(String statementId);
}
