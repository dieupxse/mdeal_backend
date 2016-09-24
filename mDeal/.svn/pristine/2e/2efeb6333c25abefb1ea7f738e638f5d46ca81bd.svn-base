/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import vn.ctnet.entity.*;

/**
 *
 * @author jacob
 */
public class ProfileDAO {
    public boolean insert(Profile pro ) throws ClassNotFoundException, SQLException {
        Database DB = new Database();
        Connection conn = DB.connection();
        String sql = "INSERT INTO PROFILE([Phone],[FullName],[DOB],[Email],[Gender],[Description],[Score],[Lable],[Password],[Avartar]) VALUES (?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement pstm = conn.prepareStatement(sql);
            pstm.setString(1,pro.getPhone());
            pstm.setString(2, pro.getFullName());
            pstm.setDate(3, new java.sql.Date(pro.getDob().getTime()));
            pstm.setString(4, pro.getEmail());
            pstm.setString(5, pro.getGender());
            pstm.setString(6, pro.getDescription());
            pstm.setInt(7, pro.getScore());
            pstm.setString(8, pro.getLable());
            pstm.setString(9, pro.getPassword());
            pstm.setString(10, pro.getPassword());
        
        try {
            return (pstm.executeUpdate() > 0);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            
            return false;
        }finally {
            if(conn!=null) conn.close();
            if(pstm!=null) pstm.close();
        }
        
        
    }
    public boolean update(Profile pro ) throws ClassNotFoundException, SQLException {
        Database DB = new Database();
        Connection conn = DB.connection();
        String sql = "UPDATE PROFILE SET [FullName] = ?,[DOB] = ?,[Email] = ?,[Gender] = ?,[Description] = ?,[Score] = ?,[Lable]=?,[Password]=?,[Avartar]=?) WHERE Phone = ?";
        PreparedStatement pstm = conn.prepareStatement(sql);
            pstm.setString(1, pro.getFullName());
            pstm.setDate(2, new java.sql.Date(pro.getDob().getTime()));
            pstm.setString(3, pro.getEmail());
            pstm.setString(4, pro.getGender());
            pstm.setString(5, pro.getDescription());
            pstm.setInt(6, pro.getScore());
            pstm.setString(7, pro.getLable());
            pstm.setString(8, pro.getPassword());
            pstm.setString(9, pro.getPassword());
            pstm.setString(10,pro.getPhone());
        
        try {
            return (pstm.executeUpdate() > 0);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            
            return false;
        }finally {
            if(conn!=null) conn.close();
            if(pstm!=null) pstm.close();
        }
    }
}
