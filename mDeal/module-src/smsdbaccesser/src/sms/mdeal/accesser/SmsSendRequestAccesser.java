/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sms.mdeal.accesser;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import sms.mdeal.entity.SmsSendRequestEntity;
import sms.mdeal.database.DB;
/**
 *
 * @author dieup
 */
public class SmsSendRequestAccesser {
    
    private SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String numberSmsToSent = sms.mdeal.ultils.Ultility.getValue("numberSmsToSent");
    /**
     * update sms sent request
     * @param sms
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException 
     */
    public boolean update(SmsSendRequestEntity sms) throws ClassNotFoundException, SQLException {
        DB db = new DB();
        Connection conn = db.connection();
        String sql =    "UPDATE [MDEAL].[dbo].[SMS_SEND_REQUEST] " +
                        "SET [Message] = ? " +
                        ",[Reciever] = ?" +
                        ",[Sender] = ?" +
                        ",[DateAdded] = ?" +
                        ",[DateSent] = ?" +
                        ",[SentTime] = ?" +
                        ",[IsProcessed] = ?" +
                        ",[UserSentId] = ? " +
                        "WHERE RequestId = ?";
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, sms.getMessage());
        pstm.setString(2,sms.getReciever());
        pstm.setString(3,sms.getSender());
        pstm.setString(4, sms.getDateAdded()== null ? fm.format(new Date()) : fm.format(sms.getDateAdded()));
        pstm.setString(5, sms.getDateSent()== null ? fm.format(new Date()) : fm.format(sms.getDateSent()));
        pstm.setString(6, sms.getSentTime()== null ? fm.format(new Date()) : fm.format(sms.getSentTime()));
        pstm.setBoolean(7, sms.isProcessed());
        pstm.setLong(8, sms.getUserSentId());
        pstm.setLong(9, sms.getRequestId());
        try {
            return (pstm.executeUpdate() > 0);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
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
    
    /**
     * get sms to sent
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException 
     */
    public ArrayList<SmsSendRequestEntity> getSmsToSent() throws ClassNotFoundException, SQLException {
        ArrayList<SmsSendRequestEntity> list = null;
        DB db = new DB();
        Connection conn = db.connection();
        String sql = "SELECT TOP "+numberSmsToSent+" * FROM [MDEAL].[dbo].[SMS_SEND_REQUEST] WHERE [IsProcessed] = 0 AND GETDATE() >= [SentTime]";
        PreparedStatement pstm = conn.prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();
        if (rs != null) {
            list = new ArrayList<>();
            while (rs.next()) {
                SmsSendRequestEntity sv = new SmsSendRequestEntity();
                sv.setDateAdded(rs.getTimestamp("DateAdded"));
                sv.setDateSent(rs.getTimestamp("DateSent"));
                sv.setSentTime(rs.getTimestamp("SentTime"));
                sv.setIsProcessed(rs.getBoolean("IsProcessed"));
                sv.setMessage(rs.getString("Message"));
                sv.setReciever(rs.getString("Reciever"));
                sv.setRequestId(rs.getLong("RequestId"));
                sv.setSender(rs.getString("Sender"));
                sv.setUserSentId(rs.getLong("UserSentId"));
                list.add(sv);
            }
        }

        if (rs != null) {
            rs.close();
        }
        if (pstm != null) {
            pstm.close();
        }
        if (conn != null) {
            conn.close();
        }
        return list;
    }
}
