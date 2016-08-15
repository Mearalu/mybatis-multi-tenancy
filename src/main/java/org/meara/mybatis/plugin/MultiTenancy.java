package org.meara.mybatis.plugin;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.meara.mybatis.plugin.filter.MultiTenancyFilter;
import org.meara.mybatis.plugin.filter.RegExMultiTenancyFilter;
import org.meara.mybatis.plugin.parser.DefaultSqlParser;
import org.meara.mybatis.plugin.parser.SqlParser;

import java.util.Properties;

/**
 * 共享数据库的多租户系统实现
 * Created by Meara on 2016/3/17.
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class MultiTenancy implements Interceptor {

    //获取tenantId的接口
    private TenantInfo tenantInfo;
    //MultiTenancyFilter筛选器
    private MultiTenancyFilter multiTenancyFilter;
    //数据库中租户ID的列名
    private String tenantIdColumn = "tenant_id";
    //属性参数信息
    private Properties properties;
    //sql处理
    private SqlParser sqlParser;

    public MultiTenancy() {
    }

    public Object intercept(Invocation invocation) throws Throwable {
        mod(invocation);
        return invocation.proceed();
    }

    /**
     * 更改MappedStatement为新的
     * @param invocation
     * @throws Throwable
     */
    public void mod(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation
                .getArgs()[0];
        if (!sqlParser.doStatementFilter(ms.getId())) return;
        BoundSql boundSql = ms.getBoundSql(invocation.getArgs());
        /**
         * 根据已有BoundSql构造新的BoundSql
         *
         */
        BoundSql newBoundSql = new BoundSql(
                ms.getConfiguration(),
                sqlParser.setTenantParameter(boundSql.getSql()),//更改后的sql
                boundSql.getParameterMappings(),
                boundSql.getParameterObject());

        /**
         * 根据已有MappedStatement构造新的MappedStatement
         *
         */
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(),
                ms.getId(),
                new BoundSqlSqlSource(newBoundSql),
                ms.getSqlCommandType());

        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.cache(ms.getCache());
        MappedStatement newMs = builder.build();
        /**
         * 替换MappedStatement
         */
        invocation.getArgs()[0] = newMs;
    }

    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }


    /**
     * 获取配置信息
     * @param properties
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
        try {
            String tenantInfoStr = getCleanProperty("tenantInfo");
            if (tenantInfoStr != null) {
                Class onwClass = Class.forName(tenantInfoStr);
                setTenantInfo((TenantInfo) onwClass.newInstance());
                //sql处理
                DefaultSqlParser defaultSqlParser = new DefaultSqlParser();
                defaultSqlParser.setTenantInfo(tenantInfo);
                //租户字段
                String tenantIdColumnStr = getCleanProperty("tenantIdColumn");
                if (tenantIdColumnStr != null) {
                    setTenantIdColumn(tenantIdColumnStr);
                    defaultSqlParser.setTenantIdColumn(this.tenantIdColumn);
                }
                //过滤器
                RegExMultiTenancyFilter multiTenancyFilter = new RegExMultiTenancyFilter();
                multiTenancyFilter.setFilterTablePatterns(getCleanProperty("filterTablePatterns"));
                multiTenancyFilter.setFilterStatementPatterns(getCleanProperty("filterStatementPatterns"));
                String filterDefault = getCleanProperty("filterDefault");
                if (filterDefault != null) multiTenancyFilter.setFilterDefault(Boolean.valueOf(filterDefault));
                setMultiTenancyFilter(multiTenancyFilter);
                defaultSqlParser.setMultiTenancyFilter(this.multiTenancyFilter);
                this.sqlParser = defaultSqlParser;
            } else {
                throw new IllegalAccessException("config tenantInfoStr cont't be null");
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("init arg error");
        }
    }

    private String getCleanProperty(String key) {
        String value = this.properties.getProperty(key);
        return value == null || value.isEmpty() ? null : value;
    }

    /**
     *用于构造新MappedStatement
     */
    public static class BoundSqlSqlSource implements SqlSource {
        BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }

    public void setTenantInfo(TenantInfo tenantInfo) {
        this.tenantInfo = tenantInfo;
    }

    public void setTenantIdColumn(String tenantIdColumn) {
        this.tenantIdColumn = tenantIdColumn;
    }

    public void setMultiTenancyFilter(MultiTenancyFilter multiTenancyFilter) {
        this.multiTenancyFilter = multiTenancyFilter;
    }

    public MultiTenancy setSqlParser(SqlParser sqlParser) {
        this.sqlParser = sqlParser;
        return this;
    }
}
