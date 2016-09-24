/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.process;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import vn.ctnet.dao.PackageDAO;
import vn.ctnet.dao.ServiceDAO;
import vn.ctnet.dao.SmsMtDAO;
import vn.ctnet.entity.Service;
import vn.ctnet.entity.SmsMt;

/**
 *
 * @author jacob
 */
public class WarningExpFreeAccount extends Thread {
     public int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }
    @Override
    public void run() {
        System.out.println("Chay thread goi canh bao het han goi FREE");
        ServiceDAO svDAO = new ServiceDAO();
        PackageDAO pkDAO = new PackageDAO();
        while (true) {
            int timeout = Integer.parseInt(vn.ctnet.confi.Ultility.getValue("thread_warning_free"));
            String enable_mt_exp_free = vn.ctnet.confi.Ultility.getValue("enable_mt_exp_free");
            ArrayList<Service> lsv = null;
            try {
                lsv = svDAO.getExpWarning();
            } catch (ClassNotFoundException | SQLException ex) {
                Logger.getLogger(CheckExpAccount.class.getName()).log(Level.SEVERE, null, ex);
                lsv = new ArrayList<>();
            }
            if (lsv.size() > 0) {
                for (Service sv : lsv) {
                    System.out.println("Goi thong thong bao het han den thue bao " + sv.getPhone());
                    sv.setIsPushMsg(1);
                    try {
                        svDAO.update(sv);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    vn.ctnet.entity.Package pkg = null;
                    try {
                        pkg = pkDAO.getPackageByID(sv.getPackageID());
                        if(enable_mt_exp_free!=null && enable_mt_exp_free.equals("1")) {
                            String msg = vn.ctnet.confi.Ultility.getSms("msg_warning_free");
                            msg = msg.replace("{GOI}", sv.getPackageID());
                            msg = msg.replace("{NGAY}", pkg.getNumOfDate() + "");
                            msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pkg.getPrice())).replace(',', '.'));
                            SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                            msg = msg.replace("{DATE}", fm.format(sv.getExpDate()));
                            String status = "PRE";
                            String isModifiedMt = vn.ctnet.confi.Ultility.getValue("modified_mt_free_warning");
                            if(isModifiedMt!=null && isModifiedMt.equals("1")) {
                                int rand = randInt(1,10);
                                if(rand%2==0) {
                                    status = "PRE";
                                } else {
                                    status = "SENT";
                                }
                            }
                            SendSMS sendSMS = new SendSMS(sv.getPhone(),"mDeal",msg,status);
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
                Logger.getLogger(WarningExpFreeAccount.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
