/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import vn.ctnet.entity.Cdr;

/**
 *
 * @author jacob
 */
public class CdrDAO {

    

    public boolean insert(Cdr cdr) throws ClassNotFoundException, SQLException {
        Database DB = new Database();
        Connection conn = DB.connection();
        String sql = "INSERT INTO CDR([msisdn],[shortCode],[eventID],[CPID],[contentID],[status],[cost],[channelType],[Information],[debitTime],[isPushed]) VALUES (?,?,?,?,?,?,?,?,?,GETDATE(),?)";
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, cdr.getMsisdn());
        pstm.setString(2, cdr.getShortCode());
        pstm.setString(3, cdr.getEventID());
        pstm.setString(4, cdr.getCpid());
        pstm.setString(5, cdr.getContentID());
        pstm.setInt(6, cdr.getStatus());
        pstm.setDouble(7, cdr.getCost());
        pstm.setString(8, cdr.getChannelType());
        pstm.setString(9, cdr.getInformation());
        pstm.setBoolean(10, cdr.getIsPushed());

        try {
            return (pstm.executeUpdate() > 0);
        } catch (SQLException e) {
            System.out.println(e.getMessage());

            return false;
        } finally {
            if (conn != null) {
                conn.close();
            }
            if (pstm != null) {
                pstm.close();
            }
        }
    }
}
