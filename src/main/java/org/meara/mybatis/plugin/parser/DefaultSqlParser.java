package org.meara.mybatis.plugin.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import org.meara.mybatis.plugin.TenantInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * sql解析及处理
 * TODO 实现的还是有些乱需整理 线上能正常使用
 * Created by Meara on 2016/6/8.
 */
public class DefaultSqlParser implements SqlParser {
    private static final Logger logger = LoggerFactory.getLogger(DefaultSqlParser.class);
    //获取tenantId的接口
    private TenantInfo tenantInfo;
    //更新时是否更新租户id

    @Override
    public boolean doStatementFilter(String statementId) {
        return tenantInfo.getMultiTenancyFilter().doStatementFilter(statementId);
    }

    private boolean doTableFilter(String table) {
        return tenantInfo.getMultiTenancyFilter().doTableFilter(table);
    }

    @Override
    public String setTenantParameter(String sql) {
        logger.debug("old sql:{}", sql);
        Statement stmt = null;
        try {
            stmt = CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            logger.debug("解析", e);
            logger.error("解析sql[{}]失败\n原因:{}", sql, e.getMessage());
            //如果解析失败不进行任何处理防止业务中断
            return sql;
        }
        if (stmt instanceof Insert) {
            processInsert((Insert) stmt);
        }
        if (stmt instanceof Select) {
            Select select = (Select) stmt;
            processSelectBody(select.getSelectBody());
        }
        if (stmt instanceof Update) {
            processUpdate((Update) stmt);
        }
        if (stmt instanceof Delete) {
            processDelete((Delete) stmt);
        }
        logger.debug("new sql:{}", stmt);
        return stmt.toString();
    }

    @Override
    public void processInsert(Insert insert) {
        if (doTableFilter(
                insert.getTable().getName()
        )) {
            insert.getColumns().add(new Column(this.tenantInfo.getTenantIdColumn()));
            if (insert.getSelect() != null) {
                processPlainSelect((PlainSelect) insert.getSelect().getSelectBody(), true);
            } else if (insert.getItemsList() != null) {
                ((ExpressionList) insert.getItemsList()).getExpressions().add(getValue(this.tenantInfo.getCurrentTenantId()));
            } else {
                //
                throw new RuntimeException("无法处理的 sql");
            }
        }
    }

    /**
     * update语句处理
     * TODO 因为线上系统不允许更改数据租户,所以并未实现它
     *
     * @param update
     */
    @Override
    public void processUpdate(Update update) {
        //获得where条件表达式
        Expression where = update.getWhere();
        update.setWhere(builderExpression(where, update.getTable()));
    }

    @Override
    public void processDelete(Delete delete) {
        //获得where条件表达式
        Expression where = delete.getWhere();
        delete.setWhere(builderExpression(where, delete.getTable()));
    }

    private StringValue getValue(Object val) {
        if (val instanceof Number) {
            return new StringValue(val.toString());
        } else {
            return new StringValue("'" + val + "'");
        }
    }

    private Expression getTenantExpression(Table table) {
        Expression tenantExpression = null;
        List<String> tenantIds = this.tenantInfo.getTenantIds();
/*        //当传入table时,字段前加上别名或者table名
        //别名优先使用
        StringBuilder tenantIdColumnName = new StringBuilder();
        if (table != null) {
            tenantIdColumnName.append(table.getAlias() != null ? table.getAlias().getName() : table.getName());
            tenantIdColumnName.append(".");
        }
        tenantIdColumnName.append(this.tenantInfo.getTenantIdColumn());*/
        //生成字段名
        Column tenantColumn = new Column(table, this.tenantInfo.getTenantIdColumn());
        if (tenantIds.size() == 1) {
            EqualsTo equalsTo = new EqualsTo();
            tenantExpression = equalsTo;
            equalsTo.setLeftExpression(tenantColumn);
            equalsTo.setRightExpression(getValue(tenantIds.get(0)));
        } else {
            //多租户身份
            InExpression inExpression = new InExpression();
            tenantExpression = inExpression;
            inExpression.setLeftExpression(tenantColumn);
            List<Expression> valueList = new ArrayList<>();
            for (String tid : tenantIds) {
                valueList.add(getValue(tid));
            }
            inExpression.setRightItemsList(new ExpressionList(valueList));
        }
        return tenantExpression;
    }

    /**
     * 处理联接语句
     *
     * @param join
     */
    public void processJoin(Join join) {
        if (join.getRightItem() instanceof Table) {
            Table fromTable = (Table) join.getRightItem();
            if (doTableFilter(fromTable.getName())) {
                List<Expression> list = new LinkedList<>();
                for (Expression expression : join.getOnExpressions()) {
                    list.add(builderExpression(expression, fromTable));
                }
                join.setOnExpressions(list);
            }

        }
    }

    /**
     * 处理条件
     * TODO 未解决sql注入问题(考虑替换StringValue为LongValue),因为线上数据库租户字段为int暂时不存在注入问题
     *
     * @param expression
     * @param table
     * @return
     */
    public Expression builderExpression(Expression expression, Table table) {
        Expression tenantExpression = getTenantExpression(table);
        //加入判断防止条件为空时生成 "and null" 导致查询结果为空
        if (expression == null) {
            return tenantExpression;
        } else {
            if (expression instanceof BinaryExpression) {
                BinaryExpression binaryExpression = (BinaryExpression) expression;
                if (binaryExpression.getLeftExpression() instanceof FromItem) {
                    processFromItem((FromItem) binaryExpression.getLeftExpression());
                }
                if (binaryExpression.getRightExpression() instanceof FromItem) {
                    processFromItem((FromItem) binaryExpression.getRightExpression());
                }
            }
            return new AndExpression(tenantExpression, expression);
        }
    }

    /**
     * 处理SelectBody
     */
    @Override
    public void processSelectBody(SelectBody selectBody) {
        if (selectBody instanceof PlainSelect) {
            processPlainSelect((PlainSelect) selectBody);
        } else if (selectBody instanceof WithItem) {
            WithItem withItem = (WithItem) selectBody;
            if (withItem.getSubSelect() != null) {
                processSelectBody(withItem.getSubSelect().getSelectBody());
            }
        } else {
            SetOperationList operationList = (SetOperationList) selectBody;
            if (operationList.getSelects() != null && operationList.getSelects().size() > 0) {
                List<SelectBody> plainSelects = operationList.getSelects();
                for (SelectBody plainSelect : plainSelects) {
                    processSelectBody(plainSelect);
                }
            }
        }
    }

    /**
     * 处理PlainSelect
     */

    public void processPlainSelect(PlainSelect plainSelect) {
        processPlainSelect(plainSelect, false);
    }

    /**
     * 处理PlainSelect
     *
     * @param plainSelect
     * @param addColumn   是否添加租户列,insert into select语句中需要
     */

    public void processPlainSelect(PlainSelect plainSelect, boolean addColumn) {
        FromItem fromItem = plainSelect.getFromItem();
        if (fromItem instanceof Table) {
            Table fromTable = (Table) fromItem;
            if (doTableFilter(fromTable.getName())) {
                plainSelect.setWhere(builderExpression(plainSelect.getWhere(), fromTable));
                if (addColumn) {
                    plainSelect.getSelectItems().add(new SelectExpressionItem(new Column("'" + this.tenantInfo.getCurrentTenantId() + "'")));
                }
            }
        } else {
            processFromItem(fromItem);
        }
        List<Join> joins = plainSelect.getJoins();
        if (joins != null && joins.size() > 0) {
            for (Join join : joins) {
                processJoin(join);
                processFromItem(join.getRightItem());
            }
        }
    }

    /**
     * 处理子查询等
     *
     * @param fromItem
     */
    public void processFromItem(FromItem fromItem) {
        if (fromItem instanceof SubJoin) {
            SubJoin subJoin = (SubJoin) fromItem;
            if (subJoin.getJoinList() != null) {
                for (Join join : subJoin.getJoinList()) {
                    processJoin(join);
                }
            }
            if (subJoin.getLeft() != null) {
                processFromItem(subJoin.getLeft());
            }
        } else if (fromItem instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) fromItem;
            if (subSelect.getSelectBody() != null) {
                processSelectBody(subSelect.getSelectBody());
            }
        } else if (fromItem instanceof ValuesList) {

        } else if (fromItem instanceof LateralSubSelect) {
            LateralSubSelect lateralSubSelect = (LateralSubSelect) fromItem;
            if (lateralSubSelect.getSubSelect() != null) {
                SubSelect subSelect = lateralSubSelect.getSubSelect();
                if (subSelect.getSelectBody() != null) {
                    processSelectBody(subSelect.getSelectBody());
                }
            }
        }
    }

    @Override
    public DefaultSqlParser setTenantInfo(TenantInfo tenantInfo) {
        this.tenantInfo = tenantInfo;
        return this;
    }
}
