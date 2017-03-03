package org.meara.mybatis.plugin.parser;

import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.update.Update;
import org.meara.mybatis.plugin.TenantInfo;

/**
 * 提取sql处理部分 方便测试以及扩展
 * Created by Meara on 2016/6/8.
 */
public interface SqlParser {


    boolean doStatementFilter(String statementId);

    SqlParser setTenantInfo(TenantInfo tenantInfo);

    /**
     * sql语句处理入口
     *
     * @param sql
     * @return
     */
    String setTenantParameter(String sql);


    /**
     * select语句处理
     *
     * @param selectBody
     */
    void processSelectBody(SelectBody selectBody);

    /**
     * insert语句处理
     *
     * @param insert
     */
    void processInsert(Insert insert);

    /**
     * update语句处理
     *
     * @param update
     */
    void processUpdate(Update update);
}
