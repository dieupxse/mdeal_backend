/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo;

import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import vn.ctnet.dao.CdrDAO;
import vn.ctnet.dao.PackageDAO;
import vn.ctnet.dao.ProfileDAO;
import vn.ctnet.dao.ServiceDAO;
import vn.ctnet.dao.SmsMtDAO;
import vn.ctnet.entity.Cdr;
import vn.ctnet.entity.Profile;
import vn.ctnet.entity.Service;
import vn.ctnet.entity.SmsMt;
import vn.ctnet.mdeal.bo.SMSProcess;

/**
 *
 * @author vanvtse90186
 */
public class Process {

    private final ServiceDAO serviceDao = new ServiceDAO();
    ;
    private final PackageDAO packCtl = new PackageDAO();

    public String register(String msisdn, String packageID, long smsID, String chanel) {
        if (packageID == null || "".equals(packageID)) {
            packageID = "D30";
        }
        String respone = "";
        vn.ctnet.entity.Package pack = null;
        Date d = new Date();
        try {
            pack = packCtl.getPackageByID(packageID);
            if (pack == null || pack.getPackageID() == null) {
                //tin nhan sai cu phap
                sendSMS(msisdn, "9193", vn.ctnet.mdeal.config.Utils.getValue("DKkothanhcongdosaicuphap"), smsID);
                return "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU.";
            }
            try {
                vn.ctnet.entity.Service service = serviceDao.getServiceByPhone(msisdn);
                //serviec co roi
                if (d.before(service.getExpDate()) && (!service.getStatus().equals("0"))) {
                    //con han su dung
                    System.out.println("con han su dung");
                    sendSMS(msisdn, "mDeal", String.format(vn.ctnet.mdeal.config.Utils.getValue("DKroiDKlai"), service.getPackageID()), smsID);
                    respone = "2|MA_GOI_DA_TON_TAI";
                } else {

                    //het han su dung, tru tien(nhung day la khuyen mai nen ko tru tien nhe)
                    service.setExpDate(new Timestamp(vn.ctnet.mdeal.config.Utils.adddays(pack.getNumOfDate()).getTime()));
                    service.setStatus("3");
                    service.setPackageID(pack.getPackageID());
                    service.setIsPaid(1);
                    service.setChannel(chanel);
                    service.setModifiedDate(new Date());
                    service.setSmsmoID(BigInteger.valueOf(smsID));
                    serviceDao.update(service);

                }

            } catch (ClassNotFoundException | SQLException e) {
                //service chua co,DK moi 
                try {
                    addNewProfile(msisdn);
                    Service service = new Service();
                    //  service.setPhone(msisdn);
                    service.setSmsmoID(BigInteger.valueOf(smsID));
                    service.setPhone(msisdn);
                    service.setPackageID(pack.getPackageID());
                    service.setRegDate(new Timestamp(new Date().getTime()));
                    service.setStartDate(new Timestamp(new Date().getTime()));

                    service.setExpDate(new Timestamp(vn.ctnet.mdeal.config.Utils.adddays(7).getTime()));
                    service.setChannel(chanel);
                    service.setIsSynched(false);
                    service.setAutoAdjourn(true);
                    service.setModifiedDate(new Date());
                    service.setStatus("1");
                    service.setIsPaid(1);
                    service.setRegisterChannel(chanel);

                    serviceDao.insert(service);
                    //   serviceDao.insert(service);
                    SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
                    respone = "1|" + packageID + "|0|" + format.format(service.getExpDate());
                    sendSMS(msisdn, "mDeal", vn.ctnet.mdeal.config.Utils.getValue("DK7ngaydauthanhcong"), smsID);

                } catch (Exception de) {
                    //  printStackTrace();
                }

            }
        } catch (ClassNotFoundException | SQLException e) {
            //ko co
            // e.printStackTrace();
            sendSMS(msisdn, "", vn.ctnet.mdeal.config.Utils.getValue("DKkothanhcongdosaicuphap"), smsID);
        }

        return respone;

    }

    void addNewProfile(String msisdn) {

        Profile profile = new Profile(msisdn);
        profile.setPhone(msisdn);
        profile.setPassword(vn.ctnet.mdeal.config.Utils.getMD5(msisdn));
        profile.setFullName(msisdn);
        profile.setDob(new Date());
        ProfileDAO proCtl = new ProfileDAO();
        try {
            proCtl.insert(profile);
        } catch (ClassNotFoundException | SQLException ex) {
            //  Logger.getLogger(SMSProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    void sendSMS(String msisdn, String brandName, String message, long smsID) {

        SmsMtDAO smsmtCtl = new SmsMtDAO();
        SmsMt smsmt = new SmsMt();
        smsmt.setMessage(message);
        smsmt.setMessageNum(vn.ctnet.mdeal.config.Utils.countmessage(message.length()));
        smsmt.setReciever(msisdn);
        smsmt.setSmsID(BigInteger.valueOf(smsID));
        smsmt.setSentTime(new Date());
        smsmt.setSender(brandName);
        smsmt.setSentStatus("PRE");
        try {
            smsmtCtl.insert(smsmt);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(SMSProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
