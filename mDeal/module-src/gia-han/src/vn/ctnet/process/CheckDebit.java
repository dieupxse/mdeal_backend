/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.process;

import charging.Charging;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import vn.ctnet.Ultils.Ultils;
import vn.ctnet.dao.CdrDAO;
import vn.ctnet.dao.PackageDAO;
import vn.ctnet.dao.ServiceDAO;
import vn.ctnet.entity.Cdr;
import vn.ctnet.entity.Service;

/**
 *
 * @author jacob
 */
public class CheckDebit extends Thread {
    /**
     * Hàm tạo số random giữa hai số min,max
     * 
     * @param min
     * @param max
     * @return 
     */
    public int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    @Override
    public void run() {

        ServiceDAO svDAO = new ServiceDAO();
        PackageDAO pkDAO = new PackageDAO();
        CdrDAO cdrDAO = new CdrDAO();
        Charging dia = new Charging();
        Ultils ult = new Ultils();
        while (true) {
            ArrayList<Service> lsv = null;
            int timeout = Integer.parseInt(vn.ctnet.confi.Ultility.getValue("thread_dangky"));
            try {
                lsv = svDAO.getDebitService();
            } catch (ClassNotFoundException | SQLException ex) {
                Logger.getLogger(CheckDebit.class.getName()).log(Level.SEVERE, null, ex);
                lsv = new ArrayList<>();
            }
            if (lsv.size() > 0) {
                System.out.println("Co " + lsv.size() + " bang ghi");
                for (Service sv : lsv) {
                    System.out.println("Dang ky goi " + sv.getPackageID() + " thue bao " + sv.getPhone());
                    vn.ctnet.entity.Package pkg = null;
                    try {
                        pkg = pkDAO.getPackageByID(sv.getPackageID());
                    } catch (ClassNotFoundException | SQLException ex) {
                        Logger.getLogger(CheckDebit.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (pkg != null) {
                        try {
                            String rsdb = dia.debit(ult.initPhoneNumber(sv.getPhone(), 2), (long) pkg.getPrice(), "049193", ult.initPhoneNumber(sv.getPhone(), 2), Ultils.getCategoryId(sv.getPackageID(), 10), Ultils.getCategoryId(sv.getPackageID(), 6), 272);
                            Cdr cdr = new Cdr();
                            cdr.setChannelType(sv.getChannel());
                            cdr.setContentID(Ultils.getCategoryId(sv.getPackageID(), 10));
                            cdr.setCost(pkg.getPrice());
                            cdr.setCpid("001");
                            cdr.setDebitTime(new Date());
                            cdr.setEventID(Ultils.getCategoryId(sv.getPackageID(), 6));
                            cdr.setInformation("mDealCharging");
                            cdr.setIsPushed(false);
                            cdr.setMsisdn(sv.getPhone());
                            cdr.setShortCode("049193");

                            if (rsdb.equals("CPS-0000")) {
                                cdr.setStatus(1);
                                System.out.println("Thuc hien tru tien thue bao " + sv.getPhone() + " thanh cong");
                                sv.setStartDate(new Timestamp(new Date().getTime()));
                                sv.setExpDate(ult.adddays(pkg.getNumOfDate()));
                                sv.setIsPaid(1);
                                sv.setModifiedDate(new Timestamp(new Date().getTime()));
                                sv.setRemainMoney(Double.valueOf("0"));
                                sv.setRetry(0);
                                //"Quy khach da dang ky thanh cong goi mDeal " + pkg.getPackageID() + " gia " + pkg.getPrice() + "/" + pkg.getNumOfDate() + " ngay.Truy cap  http://mDeal.vn de cap nhat ngay cac uu dai giam gia danh rieng cho ban (MIEN CUOC 3G/GPRS). Chi tiet goi 9090 (200d/phut).";
                                String msg = vn.ctnet.confi.Ultility.getSms("msg_dk_ok");
                                msg = msg.replace("{GOI}", pkg.getPackageID());
                                msg = msg.replace("{NGAY}", pkg.getNumOfDate() + "");
                                msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pkg.getPrice())).replace(',', '.'));
                                SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                                msg = msg.replace("{DATE}", fm.format(sv.getExpDate()));
                                String status = "PRE";
                                String isModifiedMt = vn.ctnet.confi.Ultility.getValue("modified_mt");
                                if (isModifiedMt != null && isModifiedMt.equals("1")) {
                                    int rand = randInt(1, 10);
                                    if (rand % 2 == 0) {
                                        status = "PRE";
                                    } else {
                                        status = "SENT";
                                    }
                                }
                                SendSMS sendSMS = new SendSMS(sv.getPhone(), "mDeal", msg, status);
                                sendSMS.start();

                            } else {
                                cdr.setStatus(0);
                                sv.setStatus("0");
                                sv.setModifiedDate(new Timestamp(new Date().getTime()));
                                String msg = vn.ctnet.confi.Ultility.getSms("msg_dk_fail");
                                msg = msg.replace("{GOI}", pkg.getPackageID());
                                msg = msg.replace("{NGAY}", pkg.getNumOfDate() + "");
                                msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pkg.getPrice())).replace(',', '.'));
                                SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                                msg = msg.replace("{DATE}", fm.format(sv.getExpDate()));

                                SendSMS sendSMS = new SendSMS(sv.getPhone(), "9193", msg);
                                sendSMS.start();
                            }
                            cdrDAO.insert(cdr);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        svDAO.update(sv);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

            try {
                Thread.sleep(timeout * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(CheckDebit.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
