package com.kleen.plugin;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
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
import net.sf.jsqlparser.statement.update.Update;
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
 *共享数据库的多租户系统实现
 * TODO 白名单模式
 * TODO 黑名单模式
 * Created by kleen@qq.com on 2016/08/13.
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class MultiTenancy implements Interceptor {

    //获取tenantId的接口
    private TenantInfo tenantInfo;
    //属性参数信息
    private Properties properties;

    public MultiTenancy(){
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

        Object parameterObject = null;
        if (invocation.getArgs().length > 1) {
            parameterObject = invocation.getArgs()[1];
        }
        BoundSql boundSql = ms.getBoundSql(parameterObject);
        /**
         * 根据已有BoundSql构造新的BoundSql
         *
         */
        BoundSql newBoundSql = new BoundSql(
                ms.getConfiguration(),
                addWhere(boundSql.getSql()),//更改后的sql
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
     * @param sql
     * @return
     * @throws JSQLParserException
     */
    private String addWhere(String sql) throws JSQLParserException {
        Statement stmt = CCJSqlParserUtil.parse(sql);
        //TODO 不够全面,遇到问题在优化
        if (stmt instanceof Insert) {
            //获得Update对象
            Insert insert = (Insert) stmt;
            insert.getColumns().add(new Column(properties.getProperty("tenantIdColumn")));
            ((ExpressionList) insert.getItemsList()).getExpressions().add(new StringValue("," + tenantInfo.getTenantId() + ","));
            return insert.toString();
        }

        if (stmt instanceof Update) {
            //获得Update对象
            Update updateStatement = (Update) stmt;
            //获得where条件表达式
            Expression where = updateStatement.getWhere();
            if (where instanceof BinaryExpression) {
                EqualsTo equalsTo = new EqualsTo();
                equalsTo.setLeftExpression(new Column(properties.getProperty("tenantIdColumn")));
                equalsTo.setRightExpression(new StringValue("," + tenantInfo.getTenantId() + ","));
                AndExpression andExpression = new AndExpression(equalsTo, where);
                updateStatement.setWhere(andExpression);
            }
            return updateStatement.toString();
        }

        if (stmt instanceof Select) {
            Select select = (Select) stmt;
            PlainSelect ps = (PlainSelect) select.getSelectBody();
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            List<String> tableList = tablesNamesFinder.getTableList(select);
            if (tableList.size() > 1) {
                return select.toString();
            }
            for (String table : tableList) {
                EqualsTo equalsTo = new EqualsTo();
                equalsTo.setLeftExpression(new Column(table + '.' + properties.getProperty("tenantIdColumn")));
                equalsTo.setRightExpression(new StringValue("," + tenantInfo.getTenantId() + ","));
                AndExpression andExpression = new AndExpression(equalsTo, ps.getWhere());
                ps.setWhere(andExpression);
            }
            return select.toString();
        }
            throw new RuntimeException("非法sql语句,请检查"+sql);
    }

    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * 获取配置信息
     * {dialect=mysql, tenantInfo=org.xue.test.TenantInfoImpl, tenantIdColumn=tenant_id}
     * @param properties
     */
    public void setProperties(Properties properties) {
        this.properties=properties;
        try {
            Class onwClass=Class.forName(this.properties.getProperty("tenantInfo"));
            this.tenantInfo=(TenantInfo)onwClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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
