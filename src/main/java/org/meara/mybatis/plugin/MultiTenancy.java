package org.meara.mybatis.plugin;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.meara.mybatis.plugin.parser.DefaultSqlParser;
import org.meara.mybatis.plugin.parser.SqlParser;
import org.meara.mybatis.plugin.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger= LoggerFactory.getLogger(MultiTenancy.class);

    //获取tenantId的接口
    private TenantInfo tenantInfo;
    //数据库中租户ID的列名
    private static final String DEFAULT_TENANTIDCOLUMN = "tenant_id";
    //属性参数信息
    private Properties properties = new Properties();
    //sql处理
    private SqlParser sqlParser;

    public MultiTenancy(TenantInfo tenantInfo, SqlParser sqlParser) {
        this.tenantInfo=tenantInfo;
        this.sqlParser=sqlParser;
        properties.put("tenantIdColumn", "tenant_id");
        properties.put("filterDefault", "false");
        properties.put("dialect", "mysql");
    }
    public MultiTenancy(TenantInfo tenantInfo){
        this(tenantInfo,null);
    }
    public MultiTenancy(SqlParser sqlParser){
        this(null,sqlParser);
    }
    public MultiTenancy(){
        this(null,null);
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        mod(invocation);
        return invocation.proceed();
    }

    /**
     * 更改MappedStatement为新的
     *
     * @param invocation
     * @throws Throwable
     */
    public void mod(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation
                .getArgs()[0];
        //logger.info("sql:{}",ms.getBoundSql(invocation.getArgs()[1]).getSql());
        if (!sqlParser.doStatementFilter(ms.getId())) {
            return;
        }
        String methodName = invocation.getMethod().getName();
        BoundSql boundSql = ms.getBoundSql(invocation.getArgs()[1]);

        /**
         * 根据已有BoundSql构造新的BoundSql
         *
         */
        BoundSql newBoundSql = new BoundSql(
                ms.getConfiguration(),
                sqlParser.setTenantParameter(boundSql.getSql()),//更改后的sql
                boundSql.getParameterMappings(),
                boundSql.getParameterObject());

        MappedStatement newMs=buildMappedStatement(ms,new BoundSqlSqlSource(newBoundSql));
        /**
         * 替换MappedStatement
         */
        invocation.getArgs()[0] = newMs;
    }
    /**
     * 根据已有MappedStatement构造新的MappedStatement
     */
    private MappedStatement buildMappedStatement(MappedStatement ms, SqlSource sqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), sqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuilder keyProperties = new StringBuilder();
            for (String keyProperty : ms.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        return builder.build();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }


    /**
     * 获取配置信息
     *
     * @param properties
     */
    @Override
    public void setProperties(Properties properties) {
        this.properties.putAll(properties);
        if (tenantInfo == null) {
            try {
                String tenantInfoStr = ConfigUtil.getCleanProperty(properties, "tenantInfo");
                if (tenantInfoStr != null) {
                    Class onwClass = Class.forName(tenantInfoStr);
                    setTenantInfo((TenantInfo) onwClass.newInstance());
                    //租户字段
                    tenantInfo.setTenantIdColumn(getCleanProperty("tenantIdColumn"));
                    tenantInfo.setFilterConfig(properties);
                } else {
                    throw new IllegalAccessException("config tenantInfoStr cont't be null");
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("TenantInfo init arg error");
            }
        }
        if (sqlParser == null) {
            //sql处理
            DefaultSqlParser defaultSqlParser = new DefaultSqlParser();
            defaultSqlParser.setTenantInfo(tenantInfo);
            setSqlParser(defaultSqlParser);
        }

    }

    private String getCleanProperty(String key) {
        return ConfigUtil.getCleanProperty(this.properties, key);
    }

    /**
     * 用于构造新MappedStatement
     */
    public static class BoundSqlSqlSource implements SqlSource {
        BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }

    public MultiTenancy setSqlParser(SqlParser sqlParser) {
        this.sqlParser = sqlParser;
        return this;
    }

    public MultiTenancy setTenantInfo(TenantInfo tenantInfo) {
        this.tenantInfo = tenantInfo;
        return this;
    }
}
