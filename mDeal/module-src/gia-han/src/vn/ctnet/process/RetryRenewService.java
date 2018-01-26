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
import vn.ctnet.dao.CdrDAO;
import vn.ctnet.dao.PackageDAO;
import vn.ctnet.dao.ServiceDAO;
import vn.ctnet.entity.Cdr;
import vn.ctnet.entity.Service;

/**
 *
 * @author jacob
 */
public class RetryRenewService extends Thread {

    Charging dia = new Charging();
    ServiceDAO svDAO = new ServiceDAO();
    PackageDAO pkDAO = new PackageDAO();
    CdrDAO cdrDAO = new CdrDAO();

    @Override
    public void run() {
        while (true) {
            int timeout = Integer.parseInt(vn.ctnet.confi.Ultility.getValue("thread_retry_renew"));
            String enable_mt_renew_fail = vn.ctnet.confi.Ultility.getValue("enable_mt_renew_fail");
            String enable_mt_renew = vn.ctnet.confi.Ultility.getValue("enable_mt_renew");
            int numofretry = Integer.parseInt(vn.ctnet.confi.Ultility.getValue("numofretry"));
            try {
                ArrayList<Service> lsv = svDAO.getReTryAccount();
                if (lsv.size() > 0) {
                    for (Service sv : lsv) {
                        //het so lan retry thi huy dich vu
                        if (sv.getRetry() == 0 && sv.getIsPaid() == 2 && sv.getRemainMoney() > 0) {
                            System.out.println("Huy dich vu thue bao " + sv.getPhone());
                            vn.ctnet.entity.Package pkg = pkDAO.getPackageByID(sv.getPackageID());
                            sv.setStatus("0");
                            sv.setChannel("SYS");
                            sv.setIsPaid(0);
                            sv.setModifiedDate(new Timestamp(new Date().getTime()));
                            sv.setExpDate(new Timestamp(new Date().getTime()));
                            sv.setRemainMoney(Double.valueOf("0"));
                            //Khong huy dich vu ma set retry lai sau 3 ngay
                            //sv.setRetry(numofretry);
                            //sv.setAdjournDate(new Timestamp(vn.ctnet.confi.Ultility.addHour(72).getTime()));
                            //sv.setRemainAdjournDate(numofretry/2);
                            //sv.setModifiedDate(new Timestamp(new Date().getTime()));
                           
                            
                            
                            //Gui tin bao huy thanh cong 
                            
                            String msg = vn.ctnet.confi.Ultility.getSms("msg_gh_fail");
                            msg = msg.replace("{GOI}", pkg.getPackageID());
                            msg = msg.replace("{NGAY}", pkg.getNumOfDate() + "");
                            msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pkg.getPrice())).replace(',', '.'));
                            SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                            msg = msg.replace("{DATE}", fm.format(sv.getExpDate()));
                            SendSMS sendSMS = new SendSMS(sv.getPhone(), "9193", msg);
                            sendSMS.start();
                            
                        } else {
                            //con so lan retry tiep tuc thuc hien tru tien
                            System.out.println("Gia han goi " + sv.getPackageID() + " thue bao " + sv.getPhone());
                            vn.ctnet.entity.Package pkg = pkDAO.getPackageByID(sv.getPackageID());
                            if (pkg != null) {
                                String rsdb = dia.debit(vn.ctnet.confi.Ultility.initPhoneNumber(sv.getPhone(), 2), Math.round(sv.getRemainMoney()), "049193", vn.ctnet.confi.Ultility.initPhoneNumber(sv.getPhone(), 2), vn.ctnet.confi.Ultility.getCategoryId(sv.getPackageID(), 10), vn.ctnet.confi.Ultility.getCategoryId(sv.getPackageID(), 6), 272);//CPS-0000
                                Cdr cdr = new Cdr();
                                cdr.setChannelType(sv.getChannel());
                                cdr.setContentID(vn.ctnet.confi.Ultility.getCategoryId(sv.getPackageID(), 10));
                                cdr.setCost(sv.getRemainMoney());
                                cdr.setCpid("001");
                                cdr.setDebitTime(new Date());
                                cdr.setEventID(vn.ctnet.confi.Ultility.getCategoryId(sv.getPackageID(), 6));
                                cdr.setInformation("SYS.RETRY."+sv.getPackageID());
                                cdr.setIsPushed(false);
                                cdr.setMsisdn(sv.getPhone());
                                cdr.setShortCode("049193");

                                //tru tien con lai thanh cong thi se chuyen trang thai
                                if (rsdb.equals("CPS-0000")) {
                                    cdr.setStatus(1);
                                    System.out.println("Thuc hien tru tien thue bao " + sv.getPhone() + " thanh cong");

                                    sv.setIsPaid(1);
                                    sv.setStatus("2");
                                    sv.setExpDate(new Timestamp(vn.ctnet.confi.Ultility.adddays(sv.getRemainAdjournDate()).getTime()));
                                    sv.setRemainMoney(Double.valueOf("0"));
                                    sv.setRetry(0);
                                    sv.setAdjournDate(new Timestamp(new Date().getTime()));
                                    sv.setRemainAdjournDate(0);
                                    sv.setModifiedDate(new Timestamp(new Date().getTime()));
                                    sv.setStartDate(new Timestamp(new Date().getTime()));
                                    System.out.println("Ngay con lai: " + sv.getRemainAdjournDate());
                                    String msg = vn.ctnet.confi.Ultility.getSms("msg_gh_ok");
                                    msg = msg.replace("{GOI}", pkg.getPackageID());
                                    msg = msg.replace("{NGAY}", pkg.getNumOfDate() + "");
                                    msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pkg.getPrice())).replace(',', '.'));
                                    SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                                    msg = msg.replace("{DATE}", fm.format(sv.getExpDate()));
                                    SendSMS sendSMS = new SendSMS(sv.getPhone(), "mDeal", msg);
                                    sendSMS.start();

                                } //Thue bao khong kha dung
                                else if (rsdb.equals("CPS-1004") || rsdb.equals("CPS-1007") || rsdb.startsWith("CPE")) {
                                    cdr.setStatus(0);
                                    sv.setIsPaid(0);
                                    sv.setRetry(0);
                                    sv.setAdjournDate(new Timestamp(new Date().getTime()));
                                    sv.setStatus("0");
                                    sv.setExpDate(new Timestamp(new Date().getTime()));
                                    sv.setChannel("SYS");
                                    sv.setRemainMoney(Double.valueOf("0"));
                                    sv.setRemainAdjournDate(0);
                                } //tru cuoc goi D1
                                else {
                                    cdr.setStatus(0);
                                    /*
                                    
                                    Kiem tra xem goi cuoc có phai là goi D1 Khong
                                    Neu la khong phai thi tru goi D1
                                    */
                                    if (!sv.getPackageID().equals("D1")) {
                                        Thread.sleep(4000);
                                        //khong thanh cong se tru goi cuoc D1
                                        vn.ctnet.entity.Package pkd1 = pkDAO.getPackageByID("D1");
                                        String rtrp = dia.debit(vn.ctnet.confi.Ultility.initPhoneNumber(sv.getPhone(), 2), Math.round(pkd1.getPrice()), "049193", vn.ctnet.confi.Ultility.initPhoneNumber(sv.getPhone(), 2), vn.ctnet.confi.Ultility.getCategoryId(pkd1.getPackageID(), 10), vn.ctnet.confi.Ultility.getCategoryId(pkd1.getPackageID(), 6), 272);
                                        Cdr cdrtry = new Cdr();
                                        cdrtry.setChannelType(sv.getChannel());
                                        cdrtry.setContentID(vn.ctnet.confi.Ultility.getCategoryId(pkd1.getPackageID(), 10));
                                        cdrtry.setCost(pkd1.getPrice());
                                        cdrtry.setCpid("001");
                                        cdrtry.setDebitTime(new Date());
                                        cdrtry.setEventID(vn.ctnet.confi.Ultility.getCategoryId(pkd1.getPackageID(), 6));
                                        cdrtry.setInformation("SYS.RETRY."+sv.getPackageID());
                                        cdrtry.setIsPushed(false);
                                        cdrtry.setMsisdn(sv.getPhone());
                                        cdrtry.setShortCode("049193");
                                        //tru thanh cong goi cuoc D1 se set lai so tien con phai tru va cac lan thu tiep theo
                                        if (rtrp.equals("CPS-0000")) {
                                            cdrtry.setStatus(1);
                                            sv.setIsPaid(2);
                                            sv.setStatus("4");
                                            sv.setRemainMoney(sv.getRemainMoney() == 0 ? 0 : sv.getRemainMoney() - pkd1.getPrice());
                                            sv.setRetry(sv.getRetry() == 0 ? 0 : sv.getRetry() - 1);
                                            sv.setStartDate(new Timestamp(new Date().getTime()));
                                            sv.setExpDate(new Timestamp(vn.ctnet.confi.Ultility.adddays(pkd1.getNumOfDate()).getTime()));
                                            sv.setAdjournDate(new Timestamp(vn.ctnet.confi.Ultility.adddays(pkd1.getNumOfDate()).getTime()));
                                            sv.setRemainAdjournDate(sv.getRemainAdjournDate() == 0 ? 0 : sv.getRemainAdjournDate() - pkd1.getNumOfDate());
                                            sv.setModifiedDate(new Timestamp(new Date().getTime()));
                                        } else {
                                            cdrtry.setStatus(0);
                                            //giam so lan retry va set lai ngay gio retry
                                            sv.setIsPaid(2);
                                            sv.setRetry(sv.getRetry() == 0 ? 0 : sv.getRetry() - 1);
                                            sv.setStatus("4");
                                            sv.setAdjournDate(new Timestamp(vn.ctnet.confi.Ultility.addHour(12).getTime()));
                                            sv.setModifiedDate(new Timestamp(new Date().getTime()));
                                        }
                                        cdrDAO.insert(cdrtry);
                                    } 
                                    /*
                                    Neu là goi D1 thì thuc hien cap nhat vao csdl
                                    
                                    */
                                    else {
                                        //giam so lan retry va set lai ngay gio retry
                                        sv.setIsPaid(2);
                                        sv.setRetry(sv.getRetry() == 0 ? 0 : sv.getRetry() - 1);
                                        sv.setStatus("4");
                                        sv.setAdjournDate(new Timestamp(vn.ctnet.confi.Ultility.addHour(12).getTime()));
                                        sv.setModifiedDate(new Timestamp(new Date().getTime()));
                                    }
                                }
                                cdrDAO.insert(cdr);
                            }
                        }
                        svDAO.update(sv);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
            try {
                Thread.sleep(timeout * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(RetryRenewService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
