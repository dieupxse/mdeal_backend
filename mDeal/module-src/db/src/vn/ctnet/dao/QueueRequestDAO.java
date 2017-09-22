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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import vn.ctnet.entity.QueueRequest;

/**
 *
 * @author Administrator
 */
public class QueueRequestDAO {
    SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Insert Queue
     * @param q
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException 
     */
    public boolean insert(QueueRequest q) throws ClassNotFoundException, SQLException
    {
        Database DB = new Database();
        Connection conn = DB.connection();
        String sql = "INSERT INTO QUEUE_REQUEST(Phone,Action,CreateDate,ExpDate,Status)"
                + "VALUES(?,?,GETDATE(),?,?)";
        PreparedStatement ptmm = conn.prepareStatement(sql);
        ptmm.setString(1, q.getPhone());
        ptmm.setString(2, q.getAction());
        ptmm.setTimestamp(3, q.getExpDate());
        ptmm.setBoolean(4, q.getStatus());
        try{
            return (ptmm.executeUpdate() > 0);
        }
        catch(Exception e){
            System.out.println(e.getMessage() + e.getStackTrace());
            return false;
        }
        finally {
            if (conn != null) {
                conn.close();
            }
            if (ptmm != null) {
                ptmm.close();
            }
        }
    }
    
    /**
     * Update queue
     * @param q
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException 
     */
    public boolean update(QueueRequest q) throws ClassNotFoundException, SQLException
    {
        Database DB = new Database();
        Connection conn = DB.connection();
        String sql = "UPDATE QUEUE_REQUEST SET Phone = ?, Action = ?, CreateDate = ?, ExpDate = ?,Status = ? where ID = ?";
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, q.getPhone());
        pstm.setString(2, q.getAction());
        pstm.setTimestamp(3, q.getCreateDate());
        pstm.setTimestamp(4, q.getExpDate());
        pstm.setBoolean(5, q.getStatus());
        pstm.setLong(6, q.getID());
        try {
            return (pstm.executeUpdate() > 0);
        } catch (Exception e) {
            System.out.println(e.getMessage() + e.getStackTrace());
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
     * Get All Queue by Status
     * @param status
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException 
     */
    public ArrayList<QueueRequest> GetAll(int status) throws ClassNotFoundException, SQLException
    {
        ArrayList<QueueRequest> list = null;
        Database DB = new Database();
        Connection conn = DB.connection();
        String sql = "SELECT * FROM QUEUE_REQUEST WHERE Status = ?"; 
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setInt(1, status);
        ResultSet rs = pstm.executeQuery();
        
        if(rs != null){
            list = new ArrayList<>();
            while(rs.next()){
                QueueRequest q = new QueueRequest();
                q.setID(rs.getLong("ID"));
                q.setPhone(rs.getString("Phone"));
                q.setAction(rs.getString("Action"));
                q.setCreateDate(rs.getTimestamp("CreateDate"));
                q.setExpDate(rs.getTimestamp("ExpDate"));
                q.setStatus(rs.getBoolean("Status"));
                list.add(q);
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
    
    /**
     * Get By Params
     * @param start
     * @param end
     * @param status
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException 
     */
    public ArrayList<QueueRequest> GetByParams(Timestamp start, Timestamp end, int status) throws ClassNotFoundException, SQLException
    {
        ArrayList<QueueRequest> list = null;
        Database DB = new Database();
        Connection conn = DB.connection();
        String sql = "SELECT * FROM QUEUE_REQUEST WHERE Status = ? and ExpDate BETWEEN ? and ?"; 
        
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setInt(1, status);
        pstm.setTimestamp(2, start);
        pstm.setTimestamp(3, end);
        ResultSet rs = pstm.executeQuery();
        
        if(rs != null){
            list = new ArrayList<>();
            while(rs.next()){
                QueueRequest q = new QueueRequest();
                q.setID(rs.getLong("ID"));
                q.setPhone(rs.getString("Phone"));
                q.setAction(rs.getString("Action"));
                q.setCreateDate(rs.getTimestamp("CreateDate"));
                q.setExpDate(rs.getTimestamp("ExpDate"));
                q.setStatus(rs.getBoolean("Status"));
                list.add(q);
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
    
    /**
     * Delete queue
     * @param id
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException 
     */
    public boolean Delete(long id) throws ClassNotFoundException, SQLException
    {
        Database DB = new Database();
        Connection conn = DB.connection();
        String sql = "DELETE FROM QUEUE_REQUEST where ID = ?";
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setLong(1, id);
        try {
            return (pstm.executeUpdate() > 0);
        } catch (Exception e) {
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
    
    /**
     * Get QueueByPhone
     * @param phone
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException 
     */
    public ArrayList<QueueRequest> GetQueueByPhone(String phone) throws ClassNotFoundException, SQLException
    {
        ArrayList<QueueRequest> list = null;
        Database DB = new Database();
        Connection conn = DB.connection();
        String sql = "SELECT * FROM QUEUE_REQUEST WHERE Phone = ?"; 
        
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, phone);
        ResultSet rs = pstm.executeQuery();
        if(rs != null){
            list = new ArrayList<>();
            while(rs.next()){
                QueueRequest q = new QueueRequest();
                q.setID(rs.getLong("ID"));
                q.setPhone(rs.getString("Phone"));
                q.setAction(rs.getString("Action"));
                q.setCreateDate(rs.getTimestamp("CreateDate"));
                q.setExpDate(rs.getTimestamp("ExpDate"));
                q.setStatus(rs.getBoolean("Status"));
                list.add(q);
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
    
    /**
     * Get Queue By Params
     * @param phone
     * @param status
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException 
     */
    public ArrayList<QueueRequest> GetQueueByParam(String phone, int status) throws ClassNotFoundException, SQLException
    {
        ArrayList<QueueRequest> list = null;
        Database DB = new Database();
        Connection conn = DB.connection();
        String sql = "SELECT * FROM QUEUE_REQUEST WHERE Phone = ? and status = ?"; 
        
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, phone);
        pstm.setInt(2, status);
        ResultSet rs = pstm.executeQuery();
        if(rs != null){
            list = new ArrayList<>();
            while(rs.next()){
                QueueRequest q = new QueueRequest();
                q.setID(rs.getLong("ID"));
                q.setPhone(rs.getString("Phone"));
                q.setAction(rs.getString("Action"));
                q.setCreateDate(rs.getTimestamp("CreateDate"));
                q.setExpDate(rs.getTimestamp("ExpDate"));
                q.setStatus(rs.getBoolean("Status"));
                list.add(q);
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
    
    public ArrayList<QueueRequest> GetQueueByParam(String phone, String action, int status) throws ClassNotFoundException, SQLException
    {
        ArrayList<QueueRequest> list = null;
        Database DB = new Database();
        Connection conn = DB.connection();
        String sql = "SELECT * FROM QUEUE_REQUEST WHERE Phone = ? and status = ? and Action = ?"; 
        
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, phone);
        pstm.setInt(2, status);
        pstm.setString(3, action);
        ResultSet rs = pstm.executeQuery();
        if(rs != null){
            list = new ArrayList<>();
            while(rs.next()){
                QueueRequest q = new QueueRequest();
                q.setID(rs.getLong("ID"));
                q.setPhone(rs.getString("Phone"));
                q.setAction(rs.getString("Action"));
                q.setCreateDate(rs.getTimestamp("CreateDate"));
                q.setExpDate(rs.getTimestamp("ExpDate"));
                q.setStatus(rs.getBoolean("Status"));
                list.add(q);
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
