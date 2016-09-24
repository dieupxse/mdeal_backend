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
public class SmsMoDAO {
    public boolean insert(SmsMo sms ) throws ClassNotFoundException, SQLException {
        Database DB = new Database();
        Connection conn = DB.connection();
        String sql = "INSERT INTO SMS_MO([Sender],[Reciever],[Message],[Chanel],[RecTime],[Status],[Operator],[PaidStatus]) VALUES (?,?,?,?,GETDATE(),?,?,?)";
        PreparedStatement pstm = conn.prepareStatement(sql);
            pstm.setString(1,sms.getSender());
            pstm.setString(2, sms.getReciever());
            pstm.setString(3, sms.getMessage());
            pstm.setString(4, sms.getChanel());
            pstm.setString(5, sms.getStatus());
            pstm.setString(6, sms.getOperator());
            pstm.setString(7, sms.getPaidStatus());
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
     public boolean update(SmsMo sms ) throws ClassNotFoundException, SQLException {
        Database DB = new Database();
        Connection conn = DB.connection();
        String sql = "UPDATE SMS_MO SET [Sender]=?,[Reciever]=?,[Message] =?,[Chanel] =? ,[RecTime]=?,[Status] =? ,[Operator] =? ,[PaidStatus] =? WHERE SmsID = ?";
        PreparedStatement pstm = conn.prepareStatement(sql);
            pstm.setString(1,sms.getSender());
            pstm.setString(2, sms.getReciever());
            pstm.setString(3, sms.getMessage());
            pstm.setString(4, sms.getChanel());
            pstm.setDate(5, new java.sql.Date(sms.getRecTime().getTime()));
            pstm.setString(6, sms.getStatus());
            pstm.setString(7, sms.getOperator());
            pstm.setString(8, sms.getPaidStatus());
            pstm.setLong(9, sms.getSmsID());
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
