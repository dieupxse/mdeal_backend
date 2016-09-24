/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.mdeal.bo;

import charging.Charging;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import vn.ctnet.dao.CdrDAO;
import vn.ctnet.dao.PackageDAO;
import vn.ctnet.dao.ProfileDAO;
import vn.ctnet.dao.ServiceDAO;
import vn.ctnet.entity.Cdr;
import vn.ctnet.entity.Profile;
import vn.ctnet.entity.Service;

/**
 *
 * @author jacob Class này thực hiện cho việc đăng ký qua webservice bình
 * thường, không có các thuộc tính, tham số tùy chỉnh
 */
public class ServiceProcess {
    /*
     Khai báo biến
     */

    private final ServiceDAO serviceDao = new ServiceDAO();
    private final PackageDAO packCtl = new PackageDAO();
    private final CdrDAO cdrCtrl = new CdrDAO();

    /**
     * Hàm lấy giá trị config
     *
     * @param name
     * @return
     */
    public String getValue(String name) {
        Properties prop = new Properties();
        InputStream input = null;

        try {
            String path = "C:\\mdeal_config\\config.properties";
            input = new FileInputStream(new File(path));

            // load a properties file
            prop.load(input);
            return prop.getProperty(name);

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return "";
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Hàm lấy số random giữa min, max
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

    /**
     * Hàm đăng ký gói cước
     *
     * @param msisdn
     * @param packageID
     * @param smsID
     * @param chanel
     * @param chargin
     * @return
     */
    public String register(String msisdn, String packageID, long smsID, String chanel, Charging chargin, String user) {
        String allownumber = getValue("allow_number");
        int free_day = Integer.parseInt(getValue("free_day"));
        if (allownumber != null && !"".equals(allownumber)) {
            String[] freeze = allownumber.split(",");
            boolean a = false;
            for (String freeze1 : freeze) {

                if (msisdn.equals(freeze1)) {
                    //xuong
                    a = true;
                    break;
                }
            }
            if (!a) {
                return "";
            }
        }
        //Ngày được miễn phí
        
        if (packageID == null || "".equals(packageID)) {
            packageID = "D1";
        }
        packageID = packageID.toUpperCase();
        String respone = "";
        vn.ctnet.entity.Package pack = null;
        Date d = new Date();
        try {
            pack = packCtl.getPackageByID(packageID);
            //khong tim thay goi cuoc
            if (pack == null || pack.getPackageID() == null) {
                //tin nhan sai cu phap
                /*String msg = getSms("msg_wrong");
                 sendSMS(msisdn, "9193", msg, smsID);
                 return "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU."; */
                pack = packCtl.getPackageByID("D1");
            }
            //kiem tra han su dung neu con thong bao ve cho nguoi dung
            try {
                vn.ctnet.entity.Service service = serviceDao.getServiceByPhone(msisdn);
                //serviec co roi
                if (d.before(service.getExpDate()) && (!service.getStatus().equals("0")) && (!service.getStatus().equals("4"))) {
                    //con han su dung
                    System.out.println("con han su dung");
                    String msg = getSms("msg_sv_exist");
                    msg = msg.replace("{GOI}", service.getPackageID());
                    msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                    msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                    SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                    msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                    sendSMS(msisdn, "mDeal", msg, smsID);
                    respone = "2|MA_GOI_DA_TON_TAI";

                } //neu het han su dung hoac chua dang ky thi dang ky cho nguoi dung
                else {
                    service.setUserModified(user);
                    try {
                        //login charing tru tien
                        String rs = chargin.debit(msisdn, (long) pack.getPrice(), "049193", msisdn, vn.ctnet.mdeal.config.Utils.getCategoryId(pack.getPackageID(), 10), vn.ctnet.mdeal.config.Utils.getCategoryId(pack.getPackageID(), 6), 272);
                        Cdr cdr = new Cdr();
                        cdr.setMsisdn(msisdn);
                        cdr.setShortCode("049193");
                        cdr.setEventID(vn.ctnet.mdeal.config.Utils.getCategoryId(pack.getPackageID(), 6));
                        cdr.setCpid("001001");
                        cdr.setContentID(vn.ctnet.mdeal.config.Utils.getCategoryId(pack.getPackageID(), 10));
                        cdr.setCost(pack.getPrice());
                        cdr.setChannelType(chanel);
                        cdr.setInformation("Charging" + chanel);
                        cdr.setDebitTime(new Date());
                        cdr.setIsPushed(false);

                        if ("CPS-0000".equals(rs)) {
                            //tru tien thanh cong
                            service.setExpDate(new Timestamp(vn.ctnet.mdeal.config.Utils.adddays(pack.getNumOfDate()).getTime()));
                            service.setChannel(chanel);
                            service.setPackageID(packageID.toUpperCase());
                            service.setStatus("3");
                            service.setIsPaid(1);
                            service.setModifiedDate(new Timestamp(new Date().getTime()));
                            // serviceCtl.edit(service);
                            String msg = getSms("msg_dk_ok");
                            msg = msg.replace("{GOI}", pack.getPackageID());
                            msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                            msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                            SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                            msg = msg.replace("{DATE}", fm.format(service.getExpDate()));

                            String status = "PRE";
                            String isModifiedMt = getValue("modified_mt");
                            if (isModifiedMt != null && isModifiedMt.equals("1")) {
                                int rand = randInt(1, 10);
                                if (rand % 2 == 0) {
                                    status = "PRE";
                                } else {
                                    status = "SENT";
                                }
                            }
                            sendSMS(msisdn, "mDeal", msg, smsID, status);

                            SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
                            respone = "1|" + pack.getPackageID() + "|" + (int) pack.getPrice() + "|" + format.format(service.getExpDate());
                            cdr.setStatus(1);
                        } else if ("CPS-1001".equals(rs)) {
                            cdr.setStatus(0);

                            service.setExpDate(new Timestamp(new Date().getTime()));
                            service.setStatus("4");
                            service.setIsPaid(2);
                            service.setAdjournDate(new Timestamp(new Date().getTime()));
                            service.setRemainAdjournDate(pack.getNumOfDate());
                            service.setRetry(120);
                            service.setRemainMoney(pack.getPrice());

                            respone = "3|THUE_BAO_KHONG_DU_TIEN";
                            String msg = getSms("msg_dk_fail");
                            msg = msg.replace("{GOI}", pack.getPackageID());
                            msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                            msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                            SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                            msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                            sendSMS(msisdn, "9193", msg, smsID);
                        } else if ("CPS-1002".equals(rs) || "CPS-1003".equals(rs) || "CPS-1004".equals(rs)) {
                            //Cập nhật trạng thái CDR => không thành công
                            cdr.setStatus(0);
                            //Kết quả trả về cho API VAS
                            respone = "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";

                            /*
                             Gởi tin nhắn thông báo đăng ký không thành công về cho khách hàng
                             */
                            String msg = getSms("msg_dk_fail");
                            msg = msg.replace("{GOI}", pack.getPackageID());
                            msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                            msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                            SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                            msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                            sendSMS(msisdn, "9193", msg, smsID);

                        } else {
                            cdr.setStatus(0);
                            String msg = getSms("msg_err_sys");
                            msg = msg.replace("{GOI}", pack.getPackageID());
                            msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                            msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                            SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                            msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                            sendSMS(msisdn, "9193", msg, smsID);

                            respone = "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";
                        }

                        try {
                            cdrCtrl.insert(cdr);
                            serviceDao.update(service);
                            System.out.println("Ghi long tru cuoc CDR");
                        } catch (Exception e) {
                            System.out.println("Ghi long tru cuoc CDR ERROR !!!!!!!!");
                            System.out.println(e.getMessage());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(e.getMessage());
                    }

                }

            } catch (Exception e) {
                try {
                    System.out.println("Tao moi profile");
                    addNewProfile(msisdn);
                } catch (Exception ex) {
                    System.out.println("Profile da ton tai");
                    System.out.println(ex.getMessage());
                }
                //service chua co,DK moi 
                try {
                    System.out.println("Dang ky lan dau");
                    Service service = new Service();
                    //  service.setPhone(msisdn);
                    service.setSmsmoID(BigInteger.valueOf(smsID));
                    service.setPhone(msisdn);
                    service.setPackageID(pack.getPackageID());
                    service.setRegDate(new Timestamp(new Date().getTime()));
                    service.setStartDate(new Timestamp(new Date().getTime()));

                    service.setExpDate(new Timestamp(vn.ctnet.mdeal.config.Utils.adddays(free_day).getTime()));
                    service.setChannel(chanel);
                    service.setIsSynched(false);
                    service.setAutoAdjourn(true);
                    service.setModifiedDate(new Timestamp(new Date().getTime()));
                    service.setStatus("1");
                    service.setIsPaid(1);
                    service.setRegisterChannel(chanel);
                    service.setModifiedDate(new Timestamp(new Date().getTime()));
                    service.setUserModified(user);

                    serviceDao.insert(service);
                    //   serviceDao.insert(service);
                    SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
                    respone = "1|" + packageID + "|0|" + format.format(service.getExpDate());
                    String msg = getSms("msg_free_7_day");
                    msg = msg.replace("{GOI}", pack.getPackageID());
                    msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                    msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                    SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                    msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                    msg = msg.replace("{FREE}", free_day + "");
                    String status = "PRE";
                    String isModifiedMt = getValue("modified_mt");
                    if (isModifiedMt != null && isModifiedMt.equals("1")) {
                        int rand = randInt(1, 10);
                        if (rand % 2 == 0) {
                            status = "PRE";
                        } else {
                            status = "SENT";
                        }
                    }
                    sendSMS(msisdn, "mDeal", msg, smsID, status);

                } catch (Exception de) {
                    System.out.println(de.getMessage());
                    de.printStackTrace();
                }

            }
        } catch (Exception e) {
            //ko co
            // e.printStackTrace();
            String msg = getSms("msg_wrong");
            sendSMS(msisdn, "9193", msg, smsID);
        }

        return respone;

    }

    /**
     * Hàm đăng ký trực tiếp, trừ tiền trực tiếp đối với tất cả thuê bao
     *
     * @param msisdn
     * @param packageID
     * @param smsID
     * @param chanel
     * @param chargin
     * @return
     */
    public String register_direct(String msisdn, String packageID, long smsID, String chanel, Charging chargin) {

        /**
         * Make sure package ID exist
         */
        if (packageID == null || "".equals(packageID)) {
            packageID = "D1";
        }

        packageID = packageID.toUpperCase();
        String respone = "";
        vn.ctnet.entity.Package pack = null;
        Date d = new Date();
        try {//try level 1
            pack = packCtl.getPackageByID(packageID);
            //khong tim thay goi cuoc
            if (pack == null || pack.getPackageID() == null) {
                //tin nhan sai cu phap
                /*String msg = getSms("msg_wrong");
                 sendSMS(msisdn, "9193", msg, smsID);
                 return "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU."; */
                pack = packCtl.getPackageByID("D1");
            }
            //kiem tra han su dung neu con thong bao ve cho nguoi dung
            //try level 2
            try {

                vn.ctnet.entity.Service service = serviceDao.getServiceByPhone(msisdn);
                //serviec co roi
                //try level 3
                try {
                    //login charing tru tien
                    String rs = chargin.debit(msisdn, (long) pack.getPrice(), "049193", msisdn, vn.ctnet.mdeal.config.Utils.getCategoryId(pack.getPackageID(), 10), vn.ctnet.mdeal.config.Utils.getCategoryId(pack.getPackageID(), 6), 272);
                    Cdr cdr = new Cdr();
                    cdr.setMsisdn(msisdn);
                    cdr.setShortCode("049193");
                    cdr.setEventID(vn.ctnet.mdeal.config.Utils.getCategoryId(pack.getPackageID(), 6));
                    cdr.setCpid("001001");
                    cdr.setContentID(vn.ctnet.mdeal.config.Utils.getCategoryId(pack.getPackageID(), 10));
                    cdr.setCost(pack.getPrice());
                    cdr.setChannelType(chanel);
                    cdr.setInformation("Charging" + chanel);
                    cdr.setDebitTime(new Date());
                    cdr.setIsPushed(false);

                    if ("CPS-0000".equals(rs)) {
                        //tru tien thanh cong
                        service.setExpDate(new Timestamp(vn.ctnet.mdeal.config.Utils.adddays(pack.getNumOfDate()).getTime()));
                        service.setChannel(chanel);
                        service.setPackageID(packageID.toUpperCase());
                        service.setStatus("3");
                        service.setIsPaid(1);
                        service.setModifiedDate(new Timestamp(new Date().getTime()));
                        // serviceCtl.edit(service);
                        String msg = getSms("msg_dk_ok");
                        msg = msg.replace("{GOI}", pack.getPackageID());
                        msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                        msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                        SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                        msg = msg.replace("{DATE}", fm.format(service.getExpDate()));

                        String status = "PRE";
                        String isModifiedMt = getValue("modified_mt");
                        if (isModifiedMt != null && isModifiedMt.equals("1")) {
                            int rand = randInt(1, 10);
                            if (rand % 2 == 0) {
                                status = "PRE";
                            } else {
                                status = "SENT";
                            }
                        }
                        sendSMS(msisdn, "mDeal", msg, smsID, status);

                        SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
                        respone = "1|" + pack.getPackageID() + "|" + (int) pack.getPrice() + "|" + format.format(service.getExpDate());
                        cdr.setStatus(1);
                    } else if ("CPS-1001".equals(rs)) {
                        cdr.setStatus(0);

                        service.setExpDate(new Timestamp(new Date().getTime()));
                        service.setStatus("4");
                        service.setIsPaid(2);
                        service.setAdjournDate(new Timestamp(new Date().getTime()));
                        service.setRemainAdjournDate(pack.getNumOfDate());
                        service.setRetry(120);
                        service.setRemainMoney(pack.getPrice());

                        respone = "3|THUE_BAO_KHONG_DU_TIEN";
                        String msg = getSms("msg_dk_fail");
                        msg = msg.replace("{GOI}", pack.getPackageID());
                        msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                        msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                        SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                        msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                        sendSMS(msisdn, "9193", msg, smsID);
                    } else if ("CPS-1002".equals(rs) || "CPS-1003".equals(rs) || "CPS-1004".equals(rs)) {
                        cdr.setStatus(0);
                        respone = "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";
                        String msg = getSms("msg_dk_fail");
                        msg = msg.replace("{GOI}", pack.getPackageID());
                        msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                        msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                        SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                        msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                        sendSMS(msisdn, "9193", msg, smsID);
                    } else {
                        cdr.setStatus(0);
                        String msg = getSms("msg_err_sys");
                        msg = msg.replace("{GOI}", pack.getPackageID());
                        msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                        msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                        SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                        msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                        sendSMS(msisdn, "9193", msg, smsID);
                        respone = "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";

                    }

                    try {
                        cdrCtrl.insert(cdr);
                        serviceDao.update(service);
                        System.out.println("Ghi long tru cuoc CDR");
                    } catch (Exception e) {
                        System.out.println("Ghi long tru cuoc CDR ERROR !!!!!!!!");
                        System.out.println(e.getMessage());
                        respone = "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";
                    }
                    //catch level 3
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                    respone = "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";
                }

                //catch level 2
            } catch (Exception e) {
                /**
                 * Chua tung dang ky dich vu Dang ky moi goi cuoc Thuc hien tru
                 * cuoc binh thuong
                 */
                try {
                    System.out.println("Tao moi profile");
                    addNewProfile(msisdn);
                } catch (Exception ex) {
                    System.out.println("Profile da ton tai");
                    System.out.println(ex.getMessage());
                }

                vn.ctnet.entity.Service service = serviceDao.getServiceByPhone(msisdn);
                /**
                 * thao tac tru tien truc tiep cho khach hang
                 */
                try {
                    //login charing tru tien
                    String rs = chargin.debit(msisdn, (long) pack.getPrice(), "049193", msisdn, vn.ctnet.mdeal.config.Utils.getCategoryId(pack.getPackageID(), 10), vn.ctnet.mdeal.config.Utils.getCategoryId(pack.getPackageID(), 6), 272);
                    Cdr cdr = new Cdr();
                    cdr.setMsisdn(msisdn);
                    cdr.setShortCode("049193");
                    cdr.setEventID(vn.ctnet.mdeal.config.Utils.getCategoryId(pack.getPackageID(), 6));
                    cdr.setCpid("001001");
                    cdr.setContentID(vn.ctnet.mdeal.config.Utils.getCategoryId(pack.getPackageID(), 10));
                    cdr.setCost(pack.getPrice());
                    cdr.setChannelType(chanel);
                    cdr.setInformation("Charging" + chanel);
                    cdr.setDebitTime(new Date());
                    cdr.setIsPushed(false);

                    if ("CPS-0000".equals(rs)) {
                        //tru tien thanh cong
                        service.setExpDate(new Timestamp(vn.ctnet.mdeal.config.Utils.adddays(pack.getNumOfDate()).getTime()));
                        service.setChannel(chanel);
                        service.setPackageID(packageID.toUpperCase());
                        service.setStatus("3");
                        service.setIsPaid(1);
                        service.setModifiedDate(new Timestamp(new Date().getTime()));
                        // serviceCtl.edit(service);
                        String msg = getSms("msg_dk_ok");
                        msg = msg.replace("{GOI}", pack.getPackageID());
                        msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                        msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                        SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                        msg = msg.replace("{DATE}", fm.format(service.getExpDate()));

                        String status = "PRE";
                        String isModifiedMt = getValue("modified_mt");
                        if (isModifiedMt != null && isModifiedMt.equals("1")) {
                            int rand = randInt(1, 10);
                            if (rand % 2 == 0) {
                                status = "PRE";
                            } else {
                                status = "SENT";
                            }
                        }
                        sendSMS(msisdn, "mDeal", msg, smsID, status);

                        SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
                        respone = "1|" + pack.getPackageID() + "|" + (int) pack.getPrice() + "|" + format.format(service.getExpDate());
                        cdr.setStatus(1);
                    } else if ("CPS-1001".equals(rs)) {
                        cdr.setStatus(0);
                        respone = "3|THUE_BAO_KHONG_DU_TIEN";
                        String msg = getSms("msg_dk_fail");
                        msg = msg.replace("{GOI}", pack.getPackageID());
                        msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                        msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                        SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                        msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                        sendSMS(msisdn, "9193", msg, smsID);
                    } else if ("CPS-1002".equals(rs)) {
                        cdr.setStatus(0);
                        respone = "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";
                        String msg = getSms("msg_dk_fail");
                        msg = msg.replace("{GOI}", pack.getPackageID());
                        msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                        msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                        SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                        msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                        sendSMS(msisdn, "9193", msg, smsID);
                    } else if ("CPS-1003".equals(rs)) {
                        cdr.setStatus(0);
                        respone = "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";
                        String msg = getSms("msg_dk_fail");
                        msg = msg.replace("{GOI}", pack.getPackageID());
                        msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                        msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                        SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                        msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                        sendSMS(msisdn, "9193", msg, smsID);
                    } else if ("CPS-1004".equals(rs)) {
                        cdr.setStatus(0);
                        String msg = getSms("msg_dk_fail");
                        msg = msg.replace("{GOI}", pack.getPackageID());
                        msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                        msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                        SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                        msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                        sendSMS(msisdn, "9193", msg, smsID);
                        respone = "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";
                    } else {
                        cdr.setStatus(0);
                        String msg = getSms("msg_err_sys");
                        msg = msg.replace("{GOI}", pack.getPackageID());
                        msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                        msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                        SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                        msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                        sendSMS(msisdn, "9193", msg, smsID);
                        respone = "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";

                    }

                    try {
                        cdrCtrl.insert(cdr);
                        serviceDao.update(service);
                        System.out.println("Ghi long tru cuoc CDR");
                    } catch (Exception ex) {
                        System.out.println("Ghi long tru cuoc CDR ERROR !!!!!!!!");
                        System.out.println(ex.getMessage());
                        respone = "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";
                    }
                } catch (Exception exx) {
                    exx.printStackTrace();
                    System.out.println(exx.getMessage());
                    respone = "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";
                }

            }

            //catch level 1
        } catch (Exception e) {
            //ko co
            // e.printStackTrace();
            String msg = getSms("msg_wrong");
            sendSMS(msisdn, "9193", msg, smsID);
            respone = "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";
        }

        return respone;

    }

    /**
     * Hàm hủy thuê bao
     *
     * @param msisdn
     * @param packageID
     * @param smsID
     * @param chanel
     * @return
     */
    public String unregister(String msisdn, String packageID, long smsID, String chanel,String user) {

        String respone = "";
        vn.ctnet.entity.Package pack;
        try {
            pack = packCtl.getPackageByID(packageID);
            if (pack == null) {
                String msg = getSms("msg_wrong");
                sendSMS(msisdn, "9193", msg, smsID);
                return "0|HUY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";
            }
            try {

                Date d = new Date();
                vn.ctnet.entity.Service service = serviceDao.getServiceByPhone(msisdn);
                //serviec co roi
                if ( //huy dich vu khi dich vu dang hoat dong
                        (d.before(service.getExpDate()) //con han su dung
                        && (!service.getStatus().equals("0")) //trang thai dang kich hoat
                        && service.getPackageID().equals(pack.getPackageID())//goi cuoc co ton tai
                        )
                        || (service.getStatus().equals("4") //trang thai dang kich hoat
                        && service.getPackageID().equals(pack.getPackageID())//goi cuoc co ton tai
                        )) {
                    service.setStatus("0");
                    service.setChannel(chanel);
                    service.setAutoAdjourn(false);
                    service.setExpDate(new Timestamp(new Date().getTime()));
                    service.setStartDate(new Timestamp(new Date().getTime()));
                    service.setModifiedDate(new Timestamp(new Date().getTime()));
                    service.setUserModified(user);
                    serviceDao.update(service);
                    String msg = getSms("msg_huy_ok");
                    msg = msg.replace("{GOI}", pack.getPackageID());
                    msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                    msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                    SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                    msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                    sendSMS(msisdn, "9193", msg, smsID);
                    respone = "1|HUY_THANH_CONG";
                } else {
                    String msg = getSms("msg_huy_not_exist");
                    msg = msg.replace("{GOI}", pack.getPackageID());
                    msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                    msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                    SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                    msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                    sendSMS(msisdn, "9193", msg, smsID);
                    respone = "2|HUY_KHONG_THANH CONG_DO_THUE_BAO_CHUA_DANG_KY";
                }

            } catch (Exception e) {
                //service chua co,DK moi 
                String msg = getSms("msg_huy_not_exist");
                msg = msg.replace("{GOI}", pack.getPackageID());
                msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                sendSMS(msisdn, "9193", msg, smsID);
                respone = "0|HUY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";
            }
        } catch (Exception e) {
            //ko co
            e.printStackTrace();
            String msg = getSms("msg_huy_fail");
            msg = msg.replace("{GOI}", packageID);
            sendSMS(msisdn, "9193", msg, smsID);
            respone = "0|HUY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";
        }

        return respone;

    }

    /**
     * Hàm kiểm tra thông tin thuê bao
     *
     * @param msisdn
     * @param smsID
     * @param chanel
     * @return
     */
    public Service checkProfile(String msisdn, long smsID, String chanel) {

        try {

            Date d = new Date();
            Service service = serviceDao.getServiceByPhone(msisdn);

            if (service.getExpDate() != null && (d.before(service.getExpDate())) && (!service.getStatus().equals("0"))) {
                System.out.println("con han su dung");
                vn.ctnet.entity.Package pack = packCtl.getPackageByID(service.getPackageID());
                //con han su dung
                String msg = getSms("msg_kt");
                msg = msg.replace("{GOI}", pack.getPackageID());
                msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                msg = msg.replace("{DATE}", fm.format(service.getExpDate()));

                sendSMS(msisdn, "mDeal", msg, smsID);
                //    respone = "1|SUCCESS";
                return service;
            } else {
                System.out.println("het han su dung");
                String msg = getSms("msg_not_reg");
                sendSMS(msisdn, "9193", msg, smsID);
                //    respone = "2|SUBSCRIBER_NOT_REGISTER";
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            String msg = getSms("msg_not_reg");
            sendSMS(msisdn, "9193", msg, smsID);
            //  respone = "2|SUBSCRIBER_NOT_REGISTER";
            return null;
        }
    }

    /**
     * Hàm gửi thông tin hướng dẫn cho người dùng
     *
     * @param msisdn
     * @param l
     */
    void guideUser(String msisdn, long l) {
        String msg = getSms("msg_hd");
        sendSMS(msisdn, "9193", msg, l);
    }

    /**
     * Hàm gửi tin nhắn
     *
     * @param msisdn
     * @param brandName
     * @param message
     * @param smsID
     */
    void sendSMS(String msisdn, String brandName, String message, long smsID) {
        InsertSMS insertSMS = new InsertSMS(msisdn, brandName, message, smsID);
        insertSMS.start();
    }

    /**
     * Hàm gửi tin nhắn
     *
     * @param msisdn
     * @param brandName
     * @param message
     * @param smsID
     * @param status
     */
    void sendSMS(String msisdn, String brandName, String message, long smsID, String status) {
        InsertSMS insertSMS = new InsertSMS(msisdn, brandName, message, smsID, status);
        insertSMS.start();
    }

    /**
     * Hàm thêm mới thông tin người dùng
     *
     * @param msisdn
     */
    void addNewProfile(String msisdn) {

        Profile profile = new Profile(msisdn);
        profile.setPhone(msisdn);
        profile.setPassword(vn.ctnet.mdeal.config.Utils.getMD5(msisdn));
        profile.setFullName(msisdn);
        profile.setDob(new Date());
        ProfileDAO proCtl = new ProfileDAO();
        try {
            proCtl.insert(profile);
        } catch (Exception ex) {
            //  Logger.getLogger(SMSProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Hàm gửi thong tin tải ứng dụng
     *
     * @param msisdn
     * @param l
     */
    void tai(String msisdn, long l) {
        String msg = getSms("msg_tai");
        sendSMS(msisdn, "9193", msg, l);
    }

    /**
     * Hàm lấy nội dung bản tin MT
     *
     * @param name
     * @return
     */
    public String getSms(String name) {

        Properties prop = new Properties();
        InputStream input = null;

        try {
            String path = "C:\\mdeal_config\\sms.properties";
            input = new FileInputStream(new File(path));

            // load a properties file
            prop.load(input);
            return prop.getProperty(name);

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return "";
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
