/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.process;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import vn.ctnet.Ultils.Ultils;
import vn.ctnet.dao.PackageDAO;
import vn.ctnet.dao.ServiceDAO;
import vn.ctnet.dao.SmsMtDAO;
import vn.ctnet.entity.Service;

/**
 *
 * @author dieup
 */
public class WarningExpD7 extends Thread{
    @Override
    public void run() {
        System.out.println("Chay thread gui thong bao gia han thanh cong goi D7");
        ServiceDAO svDAO = new ServiceDAO();
        PackageDAO pkDAO = new PackageDAO();
        SmsMtDAO smsDAO = new SmsMtDAO();
        Ultils ult = new Ultils();
        
        while (true) {
           int timeout = Integer.parseInt(vn.ctnet.confi.Ultility.getValue("thread_warning_renew_d1"));
           String enable_mt_renew = vn.ctnet.confi.Ultility.getValue("enable_mt_renew");
            ArrayList<Service> lsv = null;
            //thong bao gia han goi D1
            try {
                lsv = svDAO.getExpWarningD7();
            } catch (ClassNotFoundException | SQLException ex) {
                Logger.getLogger(WarningExpD1.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (lsv != null && lsv.size() > 0) {
                for (Service sv : lsv) {
                    System.out.println("Goi thong thong bao gia han thanh cong den thue bao " + sv.getPhone() + " goi D7");
                    //set lai so lan push message = 0 de tang dan len = 15 goi lai
                    sv.setIsPushMsg(0);
                    try {
                        svDAO.update(sv);
                    } catch (ClassNotFoundException | SQLException ex) {
                        Logger.getLogger(WarningExpD1.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    vn.ctnet.entity.Package pkg = null;
                    try {
                        pkg = pkDAO.getPackageByID(sv.getPackageID());
                        if(enable_mt_renew!=null && enable_mt_renew.equals("1")) {
                            String msg = vn.ctnet.confi.Ultility.getSms("msg_gh_ok");
                            msg = msg.replace("{GOI}", sv.getPackageID());
                            msg = msg.replace("{NGAY}", pkg.getNumOfDate() + "");
                            msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pkg.getPrice())).replace(',', '.'));
                            SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                            msg = msg.replace("{DATE}", fm.format(sv.getExpDate()));
                            SendSMS sendSMS = new SendSMS(sv.getPhone(),"mDeal",msg);
                            sendSMS.start();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
            try {
                Thread.sleep(1000*timeout);
            } catch (InterruptedException ex) {
                Logger.getLogger(WarningExpD1.class.getName()).log(Level.SEVERE, null, ex);
            }

        }//end while
    }
}
