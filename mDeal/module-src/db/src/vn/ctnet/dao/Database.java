/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author jacob
 */
public class Database {
    public Connection connection() throws ClassNotFoundException, SQLException{
        String server = vn.ctnet.ultil.Ultility.getValue("db_server");
        String port =  vn.ctnet.ultil.Ultility.getValue("db_port");
        String database =  vn.ctnet.ultil.Ultility.getValue("db_database");
        String user =  vn.ctnet.ultil.Ultility.getValue("db_user");
        String password =  vn.ctnet.ultil.Ultility.getValue("db_password");
        
        Class.forName( "com.microsoft.sqlserver.jdbc.SQLServerDriver" );
        Connection con = DriverManager.getConnection( "jdbc:sqlserver://"+server+":"+port+";DatabaseName="+database+";user="+user+";Password="+password+";Max Pool Size=500");
        return con;
    }
}
