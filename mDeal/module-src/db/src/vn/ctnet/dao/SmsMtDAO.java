/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.dao;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import vn.ctnet.entity.SmsMt;

/**
 *
 * @author jacob
 */
public class SmsMtDAO {
    public boolean insert(SmsMt sms ) throws ClassNotFoundException, SQLException {
        Database DB = new Database();
        Connection conn = DB.connection();
        String sql = "INSERT INTO SMS_MT([Sender],[Reciever],[Message],[MessageNum],[SentTime],[SentStatus]) VALUES (?,?,?,?,GETDATE(),?)";
        PreparedStatement pstm = conn.prepareStatement(sql);
            pstm.setString(1,sms.getSender());
            pstm.setString(2, sms.getReciever());
            pstm.setString(3, sms.getMessage());
            pstm.setInt(4, sms.getMessageNum());
            pstm.setString(5, sms.getSentStatus());
        
        try {
            return (pstm.executeUpdate() > 0);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            
            return false;
        }finally {
            if(pstm!=null) pstm.close();
            if(conn!=null) conn.close();
        }
    }
    
    public boolean update(SmsMt sms ) throws ClassNotFoundException, SQLException {
        Database DB = new Database();
        Connection conn = DB.connection();
        String sql = "UPDATE SMS_MT SET [Sender]=?,[Reciever]=?,[Message]=?,[MessageNum]=?,[SentTime]=GETDATE(),[SentStatus]=? WHERE ID = ?";
        PreparedStatement pstm = conn.prepareStatement(sql);
            pstm.setString(1,sms.getSender());
            pstm.setString(2, sms.getReciever());
            pstm.setString(3, sms.getMessage());
            pstm.setInt(4, sms.getMessageNum());
            pstm.setString(5, sms.getSentStatus());
            pstm.setLong(6, sms.getId());
        
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
    public List<SmsMt> getSmsByStatus(String status) throws ClassNotFoundException, SQLException {
        List<SmsMt> list = null;
        Database DB = new Database();
        Connection conn = DB.connection();
        /*
        [ID]
      ,[SmsID]
      ,[Sender]
      ,[Reciever]
      ,[Message]
      ,[MessageNum]
      ,[SentTime]
      ,[SentStatus]
        */
        String sql = "SELECT TOP 10 * FROM SMS_MT WHERE SentStatus = ?";
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, status);
        ResultSet rs = pstm.executeQuery();
        if(rs!=null) {
            list = new ArrayList<>();
            while(rs.next()) {
                SmsMt sms = new SmsMt();
                sms.setId(rs.getLong("ID"));
                sms.setMessage(rs.getString("Message"));
                sms.setMessageNum(rs.getInt("MessageNum"));
                sms.setReciever(rs.getString("Reciever"));
                sms.setSender(rs.getString("Sender"));
                sms.setSentStatus(rs.getString("SentStatus"));
                sms.setSentTime(rs.getDate("SentTime"));
                sms.setSmsID(BigInteger.valueOf(rs.getLong("SmsID")));
                list.add(sms);
            }
        }
        

        if(rs!=null) rs.close();
        if(pstm!=null) pstm.close();
        if(conn!=null) conn.close();
        return list;
    }
}
