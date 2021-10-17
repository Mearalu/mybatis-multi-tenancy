package org.meara.mybatis.plugin.test;


import org.junit.jupiter.api.Test;

import java.sql.*;

/**
 * H2内存模式测试
 * Created by meara on 2017/03/02.
 */
public class H2Test {


    @Test
    public void memTest() throws SQLException {
        Connection conn = DriverManager.
                getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "");
        // add application code here
        Statement stmt = conn.createStatement();

        stmt.executeUpdate("CREATE TABLE TEST_MEM(ID INT PRIMARY KEY,NAME VARCHAR(255));");
        stmt.executeUpdate("INSERT INTO TEST_MEM VALUES(1, 'Hello_Mem');");
        ResultSet rs = stmt.executeQuery("SELECT * FROM TEST_MEM");
        while(rs.next()) {
            System.out.println(rs.getInt("ID")+","+rs.getString("NAME"));
        }
        conn.close();
    }
}
