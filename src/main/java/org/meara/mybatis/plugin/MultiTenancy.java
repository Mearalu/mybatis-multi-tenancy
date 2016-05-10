package org.meara.mybatis.plugin;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
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
    private String tenantIdColumn;
    //属性参数信息
    private Properties properties;

    public MultiTenancy() {
    }

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
        if (!multiTenancyFilter.statementFilter(ms.getId())) return;
        BoundSql boundSql = ms.getBoundSql(invocation.getArgs());
        /**
         * 根据已有BoundSql构造新的BoundSql
         *
         */
        Statement stmt = CCJSqlParserUtil.parse(boundSql.getSql());
        addWhere(stmt);
        BoundSql newBoundSql = new BoundSql(
                ms.getConfiguration(),
                stmt.toString(),//更改后的sql
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

    /**
     * 添加租户id条件
     *
     * @param stmt
     * @return
     * @throws JSQLParserException
     */
    private String addWhere(Statement stmt) throws JSQLParserException {
        //插入现在也会自动插入租户id
        if (stmt instanceof Insert) {
            Insert insert = (Insert) stmt;
            if (multiTenancyFilter.tableFilter(
                    insert.getTable().getName()
            )) {
                insert.getColumns().add(new Column(this.tenantIdColumn));
                if (insert.getSelect() != null && insert.getSelect().getSelectBody() != null
                        && insert.getSelect().getSelectBody() instanceof PlainSelect) {
                    PlainSelect plainSelect = (PlainSelect) insert.getSelect().getSelectBody();
                    plainSelect.getSelectItems()
                            .add(
                                    new SelectExpressionItem(new Column("'" + this.tenantInfo.getTenantId() + "'"))
                            );
                }
                if (insert.getItemsList() != null && insert.getItemsList() instanceof ExpressionList) {
                    ExpressionList expressionList = (ExpressionList) insert.getItemsList();
                    expressionList.getExpressions().add(new StringValue("'" + tenantInfo.getTenantId() + "'"));
                }
            }
        }
        if (stmt instanceof Select) {
            Select select = (Select) stmt;

            PlainSelect ps = (PlainSelect) select.getSelectBody();
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            List<String> tableList = tablesNamesFinder.getTableList(select);
            for (String table : tableList) {
                if (!multiTenancyFilter.tableFilter(table)) continue;
                EqualsTo equalsTo = new EqualsTo();
                equalsTo.setLeftExpression(new Column(table + '.' + this.tenantIdColumn));
                equalsTo.setRightExpression(new StringValue("'" + this.tenantInfo.getTenantId() + "'"));
                //加入判断防止条件为空时生成 "and null" 导致查询结果为空
                if (ps.getWhere() == null) {
                    ps.setWhere(equalsTo);
                } else {
                    AndExpression andExpression = new AndExpression(equalsTo, ps.getWhere());
                    ps.setWhere(andExpression);
                }
            }
        }
        return stmt.toString();
    }

    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
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

    /**
     * 获取配置信息
     * {dialect=mysql, tenantInfo=org.xue.test.TenantInfoImpl, tenantIdColumn=tenant_id}
     *
     * @param properties
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
        try {
            String tenantInfoStr = getString(properties, "tenantInfo");
            if (tenantInfoStr != null) {
                Class onwClass = Class.forName(tenantInfoStr);
                setTenantInfo((TenantInfo) onwClass.newInstance());
            }
            String tenantIdColumnStr = getString(properties, "tenantIdColumn");
            if (tenantIdColumnStr != null) setTenantIdColumn(tenantIdColumnStr);

            RegExMultiTenancyFilter multiTenancyFilter = new RegExMultiTenancyFilter();
            multiTenancyFilter.setFilterTablePatterns(getString(properties, "filterTablePatterns"));
            multiTenancyFilter.setFilterStatementPatterns(getString(properties, "filterStatementPatterns"));
            String filterDefault = getString(properties, "filterDefault");
            multiTenancyFilter.setFilterDefault(Boolean.valueOf(filterDefault));
            setMultiTenancyFilter(multiTenancyFilter);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("init arg error");
        }
    }

    private String getString(Properties properties, String key) {
        String value = this.properties.getProperty(key);
        return value == null || value.isEmpty() ? null : value;
    }

    /**
     *
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
}
