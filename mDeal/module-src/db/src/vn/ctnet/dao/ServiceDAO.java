/*
 * Decompiled with CFR 0_114.
 */
package vn.ctnet.dao;

import java.io.PrintStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import vn.ctnet.dao.Database;
import vn.ctnet.entity.Service;
import vn.ctnet.ultil.Ultility;

public class ServiceDAO {
    private final SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final String numOfRenewD1 = Ultility.getValue("number_renew_D1");
    private final String numOfRenewD7 = Ultility.getValue("number_renew_D7");
    private final String numOfRenewD30 = Ultility.getValue("number_renew_D30");
    private final String startDatePushWarningMsg = Ultility.getValue("start_date_push_warning_msg");

    public boolean insert(Service sv) throws ClassNotFoundException, SQLException {
        Database DB = new Database();
        Connection conn = DB.connection();
        String sql = "INSERT INTO SERVICE(PackageID,RegDate,StartDate,ExpDate,Channel,IsSynched,RegisterChannel,Status,AutoAdjourn,Retry,AdjournDate,RemainMoney,RemainAdjournDate,Description,ModifiedDate,isPaid,Phone,isPushMsg,userModified,Note,IsAutoRenew) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, sv.getPackageID() == null ? "D30" : sv.getPackageID());
        pstm.setString(2, sv.getRegDate() == null ? this.fm.format(new Date()) : this.fm.format(sv.getRegDate()));
        pstm.setString(3, sv.getStartDate() == null ? this.fm.format(new Date()) : this.fm.format(sv.getStartDate()));
        pstm.setString(4, sv.getExpDate() == null ? this.fm.format(new Date()) : this.fm.format(sv.getExpDate()));
        pstm.setString(5, sv.getChannel() == null ? "SMS" : sv.getChannel());
        pstm.setBoolean(6, sv.getIsSynched() == null ? false : sv.getIsSynched());
        pstm.setString(7, sv.getRegisterChannel() == null ? "SMS" : sv.getRegisterChannel());
        pstm.setString(8, sv.getStatus() == null ? "0" : sv.getStatus());
        pstm.setBoolean(9, sv.getAutoAdjourn() == null ? false : sv.getAutoAdjourn());
        pstm.setInt(10, sv.getRetry() == null ? 0 : sv.getRetry());
        pstm.setString(11, sv.getAdjournDate() == null ? this.fm.format(new Date()) : this.fm.format(sv.getAdjournDate()));
        pstm.setDouble(12, sv.getRemainMoney() == null ? 0.0 : sv.getRemainMoney());
        pstm.setInt(13, sv.getRemainAdjournDate() == null ? 0 : sv.getRemainAdjournDate());
        pstm.setString(14, sv.getDescription() == null ? "" : sv.getDescription());
        pstm.setString(15, sv.getModifiedDate() == null ? this.fm.format(new Date()) : this.fm.format(sv.getModifiedDate()));
        pstm.setInt(16, sv.getIsPaid() == null ? 0 : sv.getIsPaid());
        pstm.setString(17, sv.getPhone() == null ? "" : sv.getPhone());
        pstm.setInt(18, sv.getIsPushMsg() == null ? 0 : sv.getIsPushMsg());
        pstm.setString(19, sv.getUserModified() == null ? "" : sv.getUserModified());
        pstm.setString(20, sv.getNote() == null ? "" : sv.getNote());
        pstm.setBoolean(21, sv.isIsAutoRenew());
        try {
            boolean bl = pstm.executeUpdate() > 0;
            return bl;
        }
        catch (SQLException e) {
            e.printStackTrace();
            boolean bl = false;
            return bl;
        }
        finally {
            if (conn != null) {
                conn.close();
            }
            if (pstm != null) {
                pstm.close();
            }
        }
    }

    public boolean update(Service sv) throws ClassNotFoundException, SQLException {
        Database DB = new Database();
        Connection conn = DB.connection();
        String sql = "UPDATE SERVICE SET PackageID=?,RegDate=?,StartDate=?,ExpDate=?,Channel=?,IsSynched=?,RegisterChannel=?,Status=?,AutoAdjourn=?,Retry=?,AdjournDate=?,RemainMoney=?,RemainAdjournDate=?,Description=?,ModifiedDate=?,isPaid=?,Phone=? ,isPushMsg=? ,userModified=? ,Note = ?, IsAutoRenew=? WHERE ServiceID = ?";
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, sv.getPackageID());
        pstm.setString(2, sv.getRegDate() == null ? this.fm.format(new Date()) : this.fm.format(sv.getRegDate()));
        pstm.setString(3, sv.getStartDate() == null ? this.fm.format(new Date()) : this.fm.format(sv.getStartDate()));
        pstm.setString(4, sv.getExpDate() == null ? this.fm.format(new Date()) : this.fm.format(sv.getExpDate()));
        pstm.setString(5, sv.getChannel());
        pstm.setBoolean(6, sv.getIsSynched());
        pstm.setString(7, sv.getRegisterChannel());
        pstm.setString(8, sv.getStatus());
        pstm.setBoolean(9, sv.getAutoAdjourn());
        pstm.setInt(10, sv.getRetry());
        pstm.setString(11, sv.getAdjournDate() == null ? this.fm.format(new Date()) : this.fm.format(sv.getAdjournDate()));
        pstm.setDouble(12, sv.getRemainMoney());
        pstm.setInt(13, sv.getRemainAdjournDate());
        pstm.setString(14, sv.getDescription());
        pstm.setString(15, sv.getModifiedDate() == null ? this.fm.format(new Date()) : this.fm.format(sv.getModifiedDate()));
        pstm.setInt(16, sv.getIsPaid());
        pstm.setString(17, sv.getPhone());
        pstm.setInt(18, sv.getIsPushMsg() == null ? 0 : sv.getIsPushMsg());
        pstm.setString(19, sv.getUserModified() == null ? "" : sv.getUserModified());
        pstm.setString(20, sv.getNote() == null ? "" : sv.getNote());
        pstm.setBoolean(21, sv.isIsAutoRenew());
        pstm.setLong(22, sv.getServiceID());
        try {
            boolean bl = pstm.executeUpdate() > 0;
            return bl;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            boolean bl = false;
            return bl;
        }
        finally {
            if (conn != null) {
                conn.close();
            }
            if (pstm != null) {
                pstm.close();
            }
        }
    }

    public ArrayList<Service> getAllService() throws ClassNotFoundException, SQLException {
        String sql;
        ArrayList<Service> list = null;
        Database DB = new Database();
        Connection conn = DB.connection();
        PreparedStatement pstm = conn.prepareStatement(sql = "SELECT * FROM SERVICE");
        ResultSet rs = pstm.executeQuery();
        if (rs != null) {
            list = new ArrayList<Service>();
            while (rs.next()) {
                Service sv = new Service();
                sv.setServiceID(rs.getLong("ServiceID"));
                sv.setPackageID(rs.getString("PackageID"));
                sv.setRegDate(rs.getTimestamp("RegDate"));
                sv.setStartDate(rs.getTimestamp("StartDate"));
                sv.setExpDate(rs.getTimestamp("ExpDate"));
                sv.setAdjournDate(rs.getTimestamp("AdjournDate"));
                sv.setAutoAdjourn(rs.getBoolean("AutoAdjourn"));
                sv.setChannel(rs.getString("Channel"));
                sv.setStatus(rs.getString("Status"));
                sv.setDescription(rs.getString("Description"));
                sv.setIsPaid(rs.getInt("isPaid"));
                sv.setIsSynched(rs.getBoolean("IsSynched"));
                sv.setModifiedDate(rs.getDate("ModifiedDate"));
                sv.setPhone(rs.getString("Phone"));
                sv.setRegisterChannel(rs.getString("RegisterChannel"));
                sv.setRemainAdjournDate(rs.getInt("RemainAdjournDate"));
                sv.setRemainMoney(rs.getDouble("RemainMoney"));
                sv.setRetry(rs.getInt("Retry"));
                sv.setSmsmoID(BigInteger.valueOf(rs.getLong("Sms_mo_ID")));
                sv.setIsPushMsg(rs.getInt("isPushMsg"));
                sv.setUserModified(rs.getString("userModified"));
                sv.setNote(rs.getString("Note"));
                sv.setIsAutoRenew(rs.getBoolean("IsAutoRenew"));
                
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

    public ArrayList<Service> getDebitService() throws ClassNotFoundException, SQLException {
        String sql;
        ArrayList<Service> list = null;
        Database DB = new Database();
        Connection conn = DB.connection();
        PreparedStatement pstm = conn.prepareStatement(sql = "SELECT * FROM SERVICE WHERE (Status = '3' OR Status='1') AND isPaid = 0");
        ResultSet rs = pstm.executeQuery();
        if (rs != null) {
            list = new ArrayList<Service>();
            while (rs.next()) {
                Service sv = new Service();
                sv.setServiceID(rs.getLong("ServiceID"));
                sv.setPackageID(rs.getString("PackageID"));
                sv.setRegDate(rs.getTimestamp("RegDate"));
                sv.setStartDate(rs.getTimestamp("StartDate"));
                sv.setExpDate(rs.getTimestamp("ExpDate"));
                sv.setAdjournDate(rs.getTimestamp("AdjournDate"));
                sv.setAutoAdjourn(rs.getBoolean("AutoAdjourn"));
                sv.setChannel(rs.getString("Channel"));
                sv.setStatus(rs.getString("Status"));
                sv.setDescription(rs.getString("Description"));
                sv.setIsPaid(rs.getInt("isPaid"));
                sv.setIsSynched(rs.getBoolean("IsSynched"));
                sv.setModifiedDate(rs.getDate("ModifiedDate"));
                sv.setPhone(rs.getString("Phone"));
                sv.setRegisterChannel(rs.getString("RegisterChannel"));
                sv.setRemainAdjournDate(rs.getInt("RemainAdjournDate"));
                sv.setRemainMoney(rs.getDouble("RemainMoney"));
                sv.setRetry(rs.getInt("Retry"));
                sv.setSmsmoID(BigInteger.valueOf(rs.getLong("Sms_mo_ID")));
                sv.setIsPushMsg(rs.getInt("isPushMsg"));
                sv.setUserModified(rs.getString("userModified"));
                sv.setNote(rs.getString("Note"));
                sv.setIsAutoRenew(rs.getBoolean("IsAutoRenew"));
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

    public ArrayList<Service> getExpAccount() throws ClassNotFoundException, SQLException {
        String sql;
        ArrayList<Service> list = null;
        Database DB = new Database();
        Connection conn = DB.connection();
        PreparedStatement pstm = conn.prepareStatement(sql = "SELECT * FROM SERVICE WHERE ExpDate <= GETDATE() and isPaid=1 and (Status ='1' or Status ='2' or Status ='3') and IsAutoRenew=1");
        ResultSet rs = pstm.executeQuery();
        if (rs != null) {
            list = new ArrayList<Service>();
            while (rs.next()) {
                Service sv = new Service();
                sv.setServiceID(rs.getLong("ServiceID"));
                sv.setPackageID(rs.getString("PackageID"));
                sv.setRegDate(rs.getTimestamp("RegDate"));
                sv.setStartDate(rs.getTimestamp("StartDate"));
                sv.setExpDate(rs.getTimestamp("ExpDate"));
                sv.setAdjournDate(rs.getTimestamp("AdjournDate"));
                sv.setAutoAdjourn(rs.getBoolean("AutoAdjourn"));
                sv.setChannel(rs.getString("Channel"));
                sv.setStatus(rs.getString("Status"));
                sv.setDescription(rs.getString("Description"));
                sv.setIsPaid(rs.getInt("isPaid"));
                sv.setIsSynched(rs.getBoolean("IsSynched"));
                sv.setModifiedDate(rs.getDate("ModifiedDate"));
                sv.setPhone(rs.getString("Phone"));
                sv.setRegisterChannel(rs.getString("RegisterChannel"));
                sv.setRemainAdjournDate(rs.getInt("RemainAdjournDate"));
                sv.setRemainMoney(rs.getDouble("RemainMoney"));
                sv.setRetry(rs.getInt("Retry"));
                sv.setSmsmoID(BigInteger.valueOf(rs.getLong("Sms_mo_ID")));
                sv.setIsPushMsg(rs.getInt("isPushMsg"));
                sv.setUserModified(rs.getString("userModified"));
                sv.setNote(rs.getString("Note"));
                sv.setIsAutoRenew(rs.getBoolean("IsAutoRenew"));
                
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
    
    public ArrayList<Service> getCancelAccount() throws ClassNotFoundException, SQLException {
        String sql;
        ArrayList<Service> list = null;
        Database DB = new Database();
        Connection conn = DB.connection();
        PreparedStatement pstm = conn.prepareStatement(sql = "SELECT * FROM SERVICE WHERE ExpDate <= GETDATE() and isPaid=1 and (Status ='1' or Status ='2' or Status ='3') and IsAutoRenew=0");
        ResultSet rs = pstm.executeQuery();
        if (rs != null) {
            list = new ArrayList<Service>();
            while (rs.next()) {
                Service sv = new Service();
                sv.setServiceID(rs.getLong("ServiceID"));
                sv.setPackageID(rs.getString("PackageID"));
                sv.setRegDate(rs.getTimestamp("RegDate"));
                sv.setStartDate(rs.getTimestamp("StartDate"));
                sv.setExpDate(rs.getTimestamp("ExpDate"));
                sv.setAdjournDate(rs.getTimestamp("AdjournDate"));
                sv.setAutoAdjourn(rs.getBoolean("AutoAdjourn"));
                sv.setChannel(rs.getString("Channel"));
                sv.setStatus(rs.getString("Status"));
                sv.setDescription(rs.getString("Description"));
                sv.setIsPaid(rs.getInt("isPaid"));
                sv.setIsSynched(rs.getBoolean("IsSynched"));
                sv.setModifiedDate(rs.getDate("ModifiedDate"));
                sv.setPhone(rs.getString("Phone"));
                sv.setRegisterChannel(rs.getString("RegisterChannel"));
                sv.setRemainAdjournDate(rs.getInt("RemainAdjournDate"));
                sv.setRemainMoney(rs.getDouble("RemainMoney"));
                sv.setRetry(rs.getInt("Retry"));
                sv.setSmsmoID(BigInteger.valueOf(rs.getLong("Sms_mo_ID")));
                sv.setIsPushMsg(rs.getInt("isPushMsg"));
                sv.setUserModified(rs.getString("userModified"));
                sv.setNote(rs.getString("Note"));
                sv.setIsAutoRenew(rs.getBoolean("IsAutoRenew"));
                
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

    public ArrayList<Service> getExpWarning() throws ClassNotFoundException, SQLException {
        String sql;
        ArrayList<Service> list = null;
        Database DB = new Database();
        Connection conn = DB.connection();
        PreparedStatement pstm = conn.prepareStatement(sql = "SELECT * FROM SERVICE WHERE Status = '1' and isPaid=1 and (isPushMsg is null or isPushMsg = 0) and DATEDIFF(day,ExpDate,DATEADD(DAY,2,GETDATE())) =2");
        ResultSet rs = pstm.executeQuery();
        if (rs != null) {
            list = new ArrayList<Service>();
            while (rs.next()) {
                Service sv = new Service();
                sv.setServiceID(rs.getLong("ServiceID"));
                sv.setPackageID(rs.getString("PackageID"));
                sv.setRegDate(rs.getTimestamp("RegDate"));
                sv.setStartDate(rs.getTimestamp("StartDate"));
                sv.setExpDate(rs.getTimestamp("ExpDate"));
                sv.setAdjournDate(rs.getTimestamp("AdjournDate"));
                sv.setAutoAdjourn(rs.getBoolean("AutoAdjourn"));
                sv.setChannel(rs.getString("Channel"));
                sv.setStatus(rs.getString("Status"));
                sv.setDescription(rs.getString("Description"));
                sv.setIsPaid(rs.getInt("isPaid"));
                sv.setIsSynched(rs.getBoolean("IsSynched"));
                sv.setModifiedDate(rs.getDate("ModifiedDate"));
                sv.setPhone(rs.getString("Phone"));
                sv.setRegisterChannel(rs.getString("RegisterChannel"));
                sv.setRemainAdjournDate(rs.getInt("RemainAdjournDate"));
                sv.setRemainMoney(rs.getDouble("RemainMoney"));
                sv.setRetry(rs.getInt("Retry"));
                sv.setSmsmoID(BigInteger.valueOf(rs.getLong("Sms_mo_ID")));
                sv.setIsPushMsg(rs.getInt("isPushMsg"));
                sv.setUserModified(rs.getString("userModified"));
                sv.setNote(rs.getString("Note"));
                sv.setIsAutoRenew(rs.getBoolean("IsAutoRenew"));
                
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

    public ArrayList<Service> getExpWarningD1() throws ClassNotFoundException, SQLException {
        String sql;
        ArrayList<Service> list = null;
        Database DB = new Database();
        Connection conn = DB.connection();
        PreparedStatement pstm = conn.prepareStatement(sql = "SELECT * FROM SERVICE WHERE Status ='2' and (PackageID = 'D1' or PackageID = 'MD1') and isPaid=1 and RegDate >='"+startDatePushWarningMsg+"' and isPushMsg >= " + this.numOfRenewD1 + "");
        ResultSet rs = pstm.executeQuery();
        if (rs != null) {
            list = new ArrayList<Service>();
            while (rs.next()) {
                Service sv = new Service();
                sv.setServiceID(rs.getLong("ServiceID"));
                sv.setPackageID(rs.getString("PackageID"));
                sv.setRegDate(rs.getTimestamp("RegDate"));
                sv.setStartDate(rs.getTimestamp("StartDate"));
                sv.setExpDate(rs.getTimestamp("ExpDate"));
                sv.setAdjournDate(rs.getTimestamp("AdjournDate"));
                sv.setAutoAdjourn(rs.getBoolean("AutoAdjourn"));
                sv.setChannel(rs.getString("Channel"));
                sv.setStatus(rs.getString("Status"));
                sv.setDescription(rs.getString("Description"));
                sv.setIsPaid(rs.getInt("isPaid"));
                sv.setIsSynched(rs.getBoolean("IsSynched"));
                sv.setModifiedDate(rs.getDate("ModifiedDate"));
                sv.setPhone(rs.getString("Phone"));
                sv.setRegisterChannel(rs.getString("RegisterChannel"));
                sv.setRemainAdjournDate(rs.getInt("RemainAdjournDate"));
                sv.setRemainMoney(rs.getDouble("RemainMoney"));
                sv.setRetry(rs.getInt("Retry"));
                sv.setSmsmoID(BigInteger.valueOf(rs.getLong("Sms_mo_ID")));
                sv.setIsPushMsg(rs.getInt("isPushMsg"));
                sv.setUserModified(rs.getString("userModified"));
                sv.setNote(rs.getString("Note"));
                sv.setIsAutoRenew(rs.getBoolean("IsAutoRenew"));
                
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

    public ArrayList<Service> getExpWarningD7() throws ClassNotFoundException, SQLException {
        String sql;
        ArrayList<Service> list = null;
        Database DB = new Database();
        Connection conn = DB.connection();
        PreparedStatement pstm = conn.prepareStatement(sql = "SELECT * FROM SERVICE WHERE Status ='2' and (PackageID = 'D7' or PackageID = 'MD7') and RegDate >='"+startDatePushWarningMsg+"' and isPaid=1 and isPushMsg >= " + this.numOfRenewD7 + "");
        ResultSet rs = pstm.executeQuery();
        if (rs != null) {
            list = new ArrayList<Service>();
            while (rs.next()) {
                Service sv = new Service();
                sv.setServiceID(rs.getLong("ServiceID"));
                sv.setPackageID(rs.getString("PackageID"));
                sv.setRegDate(rs.getTimestamp("RegDate"));
                sv.setStartDate(rs.getTimestamp("StartDate"));
                sv.setExpDate(rs.getTimestamp("ExpDate"));
                sv.setAdjournDate(rs.getTimestamp("AdjournDate"));
                sv.setAutoAdjourn(rs.getBoolean("AutoAdjourn"));
                sv.setChannel(rs.getString("Channel"));
                sv.setStatus(rs.getString("Status"));
                sv.setDescription(rs.getString("Description"));
                sv.setIsPaid(rs.getInt("isPaid"));
                sv.setIsSynched(rs.getBoolean("IsSynched"));
                sv.setModifiedDate(rs.getDate("ModifiedDate"));
                sv.setPhone(rs.getString("Phone"));
                sv.setRegisterChannel(rs.getString("RegisterChannel"));
                sv.setRemainAdjournDate(rs.getInt("RemainAdjournDate"));
                sv.setRemainMoney(rs.getDouble("RemainMoney"));
                sv.setRetry(rs.getInt("Retry"));
                sv.setSmsmoID(BigInteger.valueOf(rs.getLong("Sms_mo_ID")));
                sv.setIsPushMsg(rs.getInt("isPushMsg"));
                sv.setUserModified(rs.getString("userModified"));
                sv.setNote(rs.getString("Note"));
                sv.setIsAutoRenew(rs.getBoolean("IsAutoRenew"));
                
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

    public ArrayList<Service> getExpWarningD30() throws ClassNotFoundException, SQLException {
        String sql;
        ArrayList<Service> list = null;
        Database DB = new Database();
        Connection conn = DB.connection();
        PreparedStatement pstm = conn.prepareStatement(sql = "SELECT * FROM SERVICE WHERE Status ='2' and (PackageID = 'D30' or PackageID = 'MD30' ) and RegDate >='"+startDatePushWarningMsg+"' and isPaid=1 and isPushMsg >= " + this.numOfRenewD30 + "");
        ResultSet rs = pstm.executeQuery();
        if (rs != null) {
            list = new ArrayList<Service>();
            while (rs.next()) {
                Service sv = new Service();
                sv.setServiceID(rs.getLong("ServiceID"));
                sv.setPackageID(rs.getString("PackageID"));
                sv.setRegDate(rs.getTimestamp("RegDate"));
                sv.setStartDate(rs.getTimestamp("StartDate"));
                sv.setExpDate(rs.getTimestamp("ExpDate"));
                sv.setAdjournDate(rs.getTimestamp("AdjournDate"));
                sv.setAutoAdjourn(rs.getBoolean("AutoAdjourn"));
                sv.setChannel(rs.getString("Channel"));
                sv.setStatus(rs.getString("Status"));
                sv.setDescription(rs.getString("Description"));
                sv.setIsPaid(rs.getInt("isPaid"));
                sv.setIsSynched(rs.getBoolean("IsSynched"));
                sv.setModifiedDate(rs.getDate("ModifiedDate"));
                sv.setPhone(rs.getString("Phone"));
                sv.setRegisterChannel(rs.getString("RegisterChannel"));
                sv.setRemainAdjournDate(rs.getInt("RemainAdjournDate"));
                sv.setRemainMoney(rs.getDouble("RemainMoney"));
                sv.setRetry(rs.getInt("Retry"));
                sv.setSmsmoID(BigInteger.valueOf(rs.getLong("Sms_mo_ID")));
                sv.setIsPushMsg(rs.getInt("isPushMsg"));
                sv.setUserModified(rs.getString("userModified"));
                sv.setNote(rs.getString("Note"));
                sv.setIsAutoRenew(rs.getBoolean("IsAutoRenew"));
                
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

    public ArrayList<Service> getRenewAccount() throws ClassNotFoundException, SQLException {
        String sql;
        ArrayList<Service> list = null;
        Database DB = new Database();
        Connection conn = DB.connection();
        PreparedStatement pstm = conn.prepareStatement(sql = "SELECT * FROM SERVICE WHERE  isPaid=0 and Status ='2' and IsAutoRenew=1");
        ResultSet rs = pstm.executeQuery();
        if (rs != null) {
            list = new ArrayList<Service>();
            while (rs.next()) {
                Service sv = new Service();
                sv.setServiceID(rs.getLong("ServiceID"));
                sv.setPackageID(rs.getString("PackageID"));
                sv.setRegDate(rs.getTimestamp("RegDate"));
                sv.setStartDate(rs.getTimestamp("StartDate"));
                sv.setExpDate(rs.getTimestamp("ExpDate"));
                sv.setAdjournDate(rs.getTimestamp("AdjournDate"));
                sv.setAutoAdjourn(rs.getBoolean("AutoAdjourn"));
                sv.setChannel(rs.getString("Channel"));
                sv.setStatus(rs.getString("Status"));
                sv.setDescription(rs.getString("Description"));
                sv.setIsPaid(rs.getInt("isPaid"));
                sv.setIsSynched(rs.getBoolean("IsSynched"));
                sv.setModifiedDate(rs.getDate("ModifiedDate"));
                sv.setPhone(rs.getString("Phone"));
                sv.setRegisterChannel(rs.getString("RegisterChannel"));
                sv.setRemainAdjournDate(rs.getInt("RemainAdjournDate"));
                sv.setRemainMoney(rs.getDouble("RemainMoney"));
                sv.setRetry(rs.getInt("Retry"));
                sv.setSmsmoID(BigInteger.valueOf(rs.getLong("Sms_mo_ID")));
                sv.setIsPushMsg(rs.getInt("isPushMsg"));
                sv.setUserModified(rs.getString("userModified"));
                sv.setNote(rs.getString("Note"));
                sv.setIsAutoRenew(rs.getBoolean("IsAutoRenew"));
                
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

    public ArrayList<Service> getReTryAccount() throws ClassNotFoundException, SQLException {
        String sql;
        ArrayList<Service> list = null;
        Database DB = new Database();
        Connection conn = DB.connection();
        PreparedStatement pstm = conn.prepareStatement(sql = "SELECT * FROM SERVICE WHERE (Status = '4' or Status ='5') and isPaid = 2 and AdjournDate <= GETDATE() and RemainMoney > 0 and IsAutoRenew=1");
        ResultSet rs = pstm.executeQuery();
        if (rs != null) {
            list = new ArrayList<Service>();
            while (rs.next()) {
                Service sv = new Service();
                sv.setServiceID(rs.getLong("ServiceID"));
                sv.setPackageID(rs.getString("PackageID"));
                sv.setRegDate(rs.getTimestamp("RegDate"));
                sv.setStartDate(rs.getTimestamp("StartDate"));
                sv.setExpDate(rs.getTimestamp("ExpDate"));
                sv.setAdjournDate(rs.getTimestamp("AdjournDate"));
                sv.setAutoAdjourn(rs.getBoolean("AutoAdjourn"));
                sv.setChannel(rs.getString("Channel"));
                sv.setStatus(rs.getString("Status"));
                sv.setDescription(rs.getString("Description"));
                sv.setIsPaid(rs.getInt("isPaid"));
                sv.setIsSynched(rs.getBoolean("IsSynched"));
                sv.setModifiedDate(rs.getDate("ModifiedDate"));
                sv.setPhone(rs.getString("Phone"));
                sv.setRegisterChannel(rs.getString("RegisterChannel"));
                sv.setRemainAdjournDate(rs.getInt("RemainAdjournDate"));
                sv.setRemainMoney(rs.getDouble("RemainMoney"));
                sv.setRetry(rs.getInt("Retry"));
                sv.setSmsmoID(BigInteger.valueOf(rs.getLong("Sms_mo_ID")));
                sv.setIsPushMsg(rs.getInt("isPushMsg"));
                sv.setUserModified(rs.getString("userModified"));
                sv.setNote(rs.getString("Note"));
                sv.setIsAutoRenew(rs.getBoolean("IsAutoRenew"));
                
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

    public Service getServiceByPhone(String phone)  {
        
        try {
            Database DB = new Database();
            Connection conn = DB.connection();
            String sql = "SELECT * FROM SERVICE WHERE Phone = ?";
            PreparedStatement pstm = conn.prepareStatement(sql);
            pstm.setString(1, phone);
            ResultSet rs = pstm.executeQuery();
            if (rs != null) {
                Service sv = new Service();
                while (rs.next()) {
                    sv.setServiceID(rs.getLong("ServiceID") == 0 ? 0 : rs.getLong("ServiceID"));
                    sv.setPackageID(rs.getString("PackageID"));
                    sv.setRegDate(rs.getTimestamp("RegDate"));
                    sv.setStartDate(rs.getTimestamp("StartDate"));
                    sv.setExpDate(rs.getTimestamp("ExpDate"));
                    sv.setAdjournDate(rs.getTimestamp("AdjournDate"));
                    sv.setAutoAdjourn(rs.getBoolean("AutoAdjourn"));
                    sv.setChannel(rs.getString("Channel"));
                    sv.setStatus(rs.getString("Status"));
                    sv.setDescription(rs.getString("Description"));
                    sv.setIsPaid(rs.getInt("isPaid"));
                    sv.setIsSynched(rs.getBoolean("IsSynched"));
                    sv.setModifiedDate(rs.getDate("ModifiedDate"));
                    sv.setPhone(rs.getString("Phone"));
                    sv.setRegisterChannel(rs.getString("RegisterChannel"));
                    sv.setRemainAdjournDate(rs.getInt("RemainAdjournDate"));
                    sv.setRemainMoney(rs.getDouble("RemainMoney"));
                    sv.setRetry(rs.getInt("Retry"));
                    sv.setSmsmoID(BigInteger.valueOf(rs.getLong("Sms_mo_ID")));
                    sv.setIsPushMsg(rs.getInt("isPushMsg"));
                    sv.setUserModified(rs.getString("userModified"));
                    sv.setNote(rs.getString("Note"));
                    sv.setIsAutoRenew(rs.getBoolean("IsAutoRenew"));
                
                }
                conn.close();
                pstm.close();
                rs.close();
                return sv;
            }
        }
        catch (Exception e) {
            return null;
        }
        return null;
    }
}