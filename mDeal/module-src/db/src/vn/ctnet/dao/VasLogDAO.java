/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import vn.ctnet.entity.VasLog;

/**
 *
 * @author dieup
 */
public class VasLogDAO {
    private final SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public boolean insert(VasLog log) throws Exception  {
        Database DB = new Database();
        Connection conn = DB.connection();
        String sql = "INSERT INTO VAS_LOG(LogDate,IP,UserName,Params,Result,Error,Method,Url,Channel) VALUES(?,?,?,?,?,?,?,?,?)";
        PreparedStatement pstm = null;
        
        try {
            pstm  = conn.prepareStatement(sql);
            pstm.setString(1, log.getLogDate() == null ? this.fm.format(new Date()) : this.fm.format(log.getLogDate()));
            pstm.setString(2, log.getIp()==null || log.getIp()=="" ? "unknow" :log.getIp()  );
            pstm.setString(3, log.getUserName());
            pstm.setString(4, log.getParams());
            pstm.setString(5, log.getResult());
            pstm.setString(6, log.getError());
            pstm.setString(7, log.getMethod());
            pstm.setString(8, log.getUrl());
            pstm.setString(9, log.getChannel());
            boolean bl = pstm.executeUpdate() > 0;
            return bl;
        }
        catch (SQLException e) {
            e.printStackTrace();
            boolean bl = false;
            return bl;
        }
        finally {
            try {
            if (conn != null) {
                conn.close();
            }
            if (pstm != null) {
                pstm.close();
            }
            } catch(Exception e) {
            //do nothing
            }
        }
    }
}
