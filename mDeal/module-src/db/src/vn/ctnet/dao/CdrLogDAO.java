/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import vn.ctnet.entity.CdrLog;
/**
 *
 * @author dieup
 */
public class CdrLogDAO {
    public boolean insert(CdrLog cdr) throws ClassNotFoundException, SQLException {
        Database DB = new Database();
        Connection conn = DB.connection();
        String  sql = "INSERT INTO CDR_LOG([chargeDate],[isdn],[bsisdn],[result],[isRecharge],[sessionLogin],[sessionRequest],[amount],[contentId],[categoryId],[serviceCode],[requestAction]) ";
                sql+=" VALUES (GETDATE(),?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, cdr.getIsdn());
        pstm.setString(2, cdr.getBsisdn());
        pstm.setString(3, cdr.getResult());
        pstm.setBoolean(4,cdr.isIsRecharge());
        pstm.setString(5, cdr.getSessionLogin());
        pstm.setString(6, cdr.getSessionRequest());
        pstm.setString(7, cdr.getAmount());
        pstm.setString(8, cdr.getContentId());
        pstm.setString(9, cdr.getCategoryId());
        pstm.setString(10, cdr.getServiceCode());
        pstm.setInt(11, cdr.getRequestAction());
        System.out.println("Ghi log CDR");
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
