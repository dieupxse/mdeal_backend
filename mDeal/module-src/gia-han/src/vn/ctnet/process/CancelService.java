/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.process;

import charging.Charging;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import vn.ctnet.Ultils.Ultils;
import vn.ctnet.dao.CdrDAO;
import vn.ctnet.dao.PackageDAO;
import vn.ctnet.dao.ServiceDAO;
import vn.ctnet.dao.SmsMtDAO;
import vn.ctnet.entity.Cdr;
import vn.ctnet.entity.Service;

/**
 *
 * @author dieup
 */
public class CancelService extends Thread {
    @Override
    public void run() {
        ServiceDAO svDAO = new ServiceDAO();
        PackageDAO pkDAO = new PackageDAO();
        SmsMtDAO smsDAO = new SmsMtDAO();
        CdrDAO cdrDAO = new CdrDAO();
        Charging dia = new Charging();
        Ultils ult = new Ultils();
        while (true) {
            
            try {
                ArrayList<Service> lsv = svDAO.getCancelAccount();

                if (lsv.size() > 0) {
                    for (Service sv : lsv) {
                        System.out.println("Huy goi " + sv.getPackageID() + " thue bao " + sv.getPhone());
                        vn.ctnet.entity.Package pkg = pkDAO.getPackageByID(sv.getPackageID());
                        if (pkg != null) {
                            sv.setIsPaid(0);
                            sv.setRetry(0);
                            sv.setAdjournDate(new Timestamp(new Date().getTime()));
                            sv.setStatus("0");
                            sv.setModifiedDate(new Timestamp(new Date().getTime()));   
                            svDAO.update(sv);
                        }
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(RenewService.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
            try {
                Thread.sleep(10*1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(RenewService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
   

}
