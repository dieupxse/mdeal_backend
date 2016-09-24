/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import vn.ctnet.entity.Users;

/**
 *
 * @author vanvtse90186
 */
public class UsersDao {
    
    public Users login(String user, String pass) throws ClassNotFoundException, SQLException {
        Database DB = new Database();
        Connection conn = DB.connection();
        String sql = "SELECT * FROM USERS WHERE UserName = ? and Password = ?";
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, user);
        pstm.setString(2, pass);
        ResultSet rs = pstm.executeQuery();

        if (rs != null) {
            Users sv = new Users();
            while (rs.next()) {
                sv.setUserID(rs.getLong("UserID"));
                sv.setUserName(rs.getString("UserName"));
                sv.setFullName(rs.getString("FullName"));
                sv.setEmail(rs.getString("Email"));                
                sv.setPhone(rs.getString("Phone"));
                sv.setPassword(rs.getString("Password"));
                sv.setDescription(rs.getString("Description"));
                sv.setAvartar(rs.getString("RoleID"));
                sv.setPermission(rs.getString("Permission"));

            }
            conn.close();
            pstm.close();
            rs.close();
            return sv;
        }
        return null;
    }
    
}
