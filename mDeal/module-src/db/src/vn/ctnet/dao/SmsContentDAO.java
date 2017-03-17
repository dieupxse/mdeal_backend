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
import vn.ctnet.entity.SmsContent;


/**
 *
 * @author Jacob
 */
public class SmsContentDAO {
    private static Database DB;
    private static Connection conn;
    private static PreparedStatement pstm;
    public SmsContent getSms(String key)  {
        try {
            if(DB==null) DB = new Database();
            if(conn==null) conn = DB.connection();
            String sql = "SELECT * FROM SMS_CONTENT WHERE ID = ?";
             pstm = conn.prepareStatement(sql);
            pstm.setString(1, key);
            ResultSet rs = pstm.executeQuery();

            if (rs != null) {
                SmsContent sms = new SmsContent();
                while (rs.next()) {
                    sms.setContent(rs.getString("Content"));
                    sms.setId(rs.getString("ID"));
                }
                rs.close();
                return sms;
            } else {
                return null;
            }
        }catch(Exception e) {
            return null;    
        }
    }
}
