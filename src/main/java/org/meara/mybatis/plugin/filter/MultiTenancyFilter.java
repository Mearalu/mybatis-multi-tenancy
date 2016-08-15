package org.meara.mybatis.plugin.filter;

/**
 * 过滤器接口
 * Created by Meara on 2016/5/4.
 */
public interface MultiTenancyFilter {
    //按照表名进行过滤
    boolean doTableFilter(String table);
    //按照statementId进行过滤
    boolean doStatementFilter(String statementId);
}
