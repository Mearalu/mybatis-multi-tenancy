package org.meara.mybatis.plugin;

/**
 * 默认过滤还是忽略 true表示按租户过滤
 * Created by Meara on 2016/5/4.
 */
public interface MultiTenancyFilter {
    boolean tableFilter(String table);
    boolean statementFilter(String statementId);
}
