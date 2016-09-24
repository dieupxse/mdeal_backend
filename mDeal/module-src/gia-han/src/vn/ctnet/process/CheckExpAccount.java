/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.process;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import vn.ctnet.dao.PackageDAO;
import vn.ctnet.dao.ServiceDAO;
import vn.ctnet.dao.SmsMtDAO;
import vn.ctnet.entity.Service;

/**
 *
 * @author jacob
 */
public class CheckExpAccount extends Thread{
    @Override
    public void run() {
        ServiceDAO svDAO = new ServiceDAO();
        PackageDAO pkDAO = new PackageDAO();
        while (true) {
            int timeout = Integer.parseInt(vn.ctnet.confi.Ultility.getValue("thread_check_exp_account"));
            ArrayList<Service> lsv = null;
            try {
                lsv = svDAO.getExpAccount();
            } catch (Exception ex) {
                System.out.println(ex.getMessage()+ex.getStackTrace());
                lsv = new ArrayList<>();
            }
            if (lsv.size() > 0) {
                for (Service sv: lsv) {
                    sv.setIsPaid(0);
                    sv.setStatus("2");
                    sv.setChannel("SYS");
                    sv.setModifiedDate(new Timestamp(new Date().getTime()));
                    try {
                        svDAO.update(sv);
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage()+ex.getStackTrace());
                    }
                    System.out.println("Chuyen trang thai gia han cho thue bao " + sv.getPhone());
                }
            }
            try {
                Thread.sleep(timeout*1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(CheckExpAccount.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
