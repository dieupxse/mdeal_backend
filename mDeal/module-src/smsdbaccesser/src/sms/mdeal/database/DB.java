/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sms.mdeal.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author dieup
 */
public class DB {
    public Connection connection() throws ClassNotFoundException, SQLException{
        String server = sms.mdeal.ultils.Ultility.getValue("db_server");
        String port =  sms.mdeal.ultils.Ultility.getValue("db_port");
        String database =  sms.mdeal.ultils.Ultility.getValue("db_database");
        String user =  sms.mdeal.ultils.Ultility.getValue("db_user");
        String password =  sms.mdeal.ultils.Ultility.getValue("db_password");
        
        Class.forName( "com.microsoft.sqlserver.jdbc.SQLServerDriver" );
        Connection con = DriverManager.getConnection( "jdbc:sqlserver://"+server+":"+port+";DatabaseName="+database+";user="+user+";Password="+password);
        return con;
    }
}
