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
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author jacob
 */
public class PackageDAO {
    public vn.ctnet.entity.Package getPackageByID(String packageId) throws ClassNotFoundException, SQLException {
        Database DB = new Database();
        Connection conn = DB.connection();
        String sql = "SELECT * FROM PACKAGE WHERE PackageID = ?";
        ResultSet rs = null;
        PreparedStatement pstm = null;
        try {
        pstm = conn.prepareStatement(sql);
        pstm.setString(1,packageId);
        rs = pstm.executeQuery();
        vn.ctnet.entity.Package pkg = null;
        if(rs!=null) {
            pkg = new vn.ctnet.entity.Package();
            while(rs.next()) {
                pkg.setDescription(rs.getString("Description"));
                pkg.setNumOfDate(rs.getInt("NumOfDate"));
                pkg.setPackageID(rs.getString("PackageID"));
                pkg.setPackageName(rs.getString("PackageName"));
                pkg.setPrice(rs.getDouble("Price"));
            }
            
            return pkg;
        }
        return null;
        }finally {
            try {
            pstm.close();
            rs.close();
            conn.close();
            } catch(Exception e) {}
        } 
        
    }
    
    public List<vn.ctnet.entity.Package> getListPackge() throws ClassNotFoundException, SQLException {
        List<vn.ctnet.entity.Package> list = null;
        Database DB = new Database();
        Connection conn = DB.connection();

        String sql = "SELECT  PackageID,PackageName,Price,NumOfDate,Description FROM PACKAGE";
        PreparedStatement pstm = conn.prepareStatement(sql);

        ResultSet rs = pstm.executeQuery();
        if (rs != null) {
            list = new ArrayList<>();
            while (rs.next()) {
                vn.ctnet.entity.Package pkg = new vn.ctnet.entity.Package();
                pkg.setDescription(rs.getString("Description"));
                pkg.setNumOfDate(rs.getInt("NumOfDate"));
                pkg.setPackageID(rs.getString("PackageID"));
                pkg.setPackageName(rs.getString("PackageName"));
                pkg.setPrice(rs.getDouble("Price"));
                list.add(pkg);
            }
        }

        if (rs != null) {
            rs.close();
        }
        if (pstm != null) {
            pstm.close();
        }
        if(conn!=null) {
            conn.close();
        }
        return list;
    }
    
}
