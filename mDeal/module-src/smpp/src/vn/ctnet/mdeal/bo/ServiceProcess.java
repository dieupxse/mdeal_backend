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
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.nio.charset.Charset;
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
import vn.ctnet.dao.SmsContentDAO;
import vn.ctnet.entity.Cdr;
import vn.ctnet.entity.Profile;
import vn.ctnet.entity.Service;
import vn.ctnet.entity.SmsContent;
import vn.ctnet.mdeal.entity.ReturnRegister;

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
    public String register(String msisdn, String packageID, long smsID, String chanel, Charging chargin, String user, boolean isAutoRenew) {
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
                service.setIsAutoRenew(isAutoRenew);
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
                        cdr.setInformation(chanel+".DK."+pack.getPackageID());
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
     * @param username
     * @return
     */
    public ReturnRegister register_direct(String msisdn, String packageID, long smsID, String chanel, Charging chargin, String username, boolean isAutoRenew, String extra_info) {

        ReturnRegister rs = new ReturnRegister();
        try {//try level 1
            rs = DoCharging(msisdn, packageID, smsID, chanel, chargin, username, isAutoRenew, extra_info);
            System.out.println("Result: " + rs.getChargingResult() + "|Package: " + rs.getPackageId() + "|ReturnCode: " + rs.getReturnCode() + "|Desc: " + rs.getReturnDesc() + "|Cycle: " + rs.getCycle() + "|Price: " + rs.getPrice());
            switch (rs.getReturnCode()) {
                case "-1":
                    addNewProfile(msisdn);
                    if (AddService(msisdn, chanel, username, packageID, isAutoRenew)) {
                        return register_direct(msisdn, packageID, smsID, chanel, chargin, username, isAutoRenew, extra_info);
                    } else {
                        rs.setReturnCode("0");
                        rs.setReturnDesc("KHONG THE KHOI TAO THONG TIN KHACH HANG");
                        return rs;
                    }
                default:
                    return rs;
            }
            //catch level 1
        } catch (Exception e) {
            rs.setReturnCode("0");
            rs.setReturnDesc(e.getMessage());
            return rs;
        }
    }

    public ReturnRegister register_direct(String msisdn, String packageID, long smsID, String chanel, Charging chargin, String username, boolean isAutoRenew) {
        return register_direct(msisdn, packageID, smsID, chanel, chargin, username, isAutoRenew, "");
    }

    public ReturnRegister register_free(String msisdn, String packageID, int price, int day, long smsID, String chanel, Charging chargin, String username, boolean isAutoRenew) {

        ReturnRegister rs = new ReturnRegister();
        try {//try level 1
            rs = DoChargingCustom(msisdn, packageID, price, day, 1, smsID, chanel, chargin, username, isAutoRenew);
            switch (rs.getReturnCode()) {
                case "-1":
                    addNewProfile(msisdn);
                    if (AddService(msisdn, chanel, username, packageID, isAutoRenew)) {
                        return register_free(msisdn, packageID, price, day, smsID, chanel, chargin, username, isAutoRenew);
                    } else {
                        rs.setReturnCode("0");
                        rs.setReturnDesc("KHONG THE KHOI TAO THONG TIN KHACH HANG");
                        return rs;
                    }
                default:
                    return rs;
            }
            //catch level 1
        } catch (Exception e) {
            rs.setReturnCode("0");
            rs.setReturnDesc(e.getMessage());
            return rs;
        }
    }

    public ReturnRegister register_free_cycle(String msisdn, String packageID, int price, int cycle, long smsID, String chanel, Charging chargin, String username, boolean isAutoRenew) {

        ReturnRegister rs = new ReturnRegister();
        try {//try level 1
            vn.ctnet.entity.Package pack = packCtl.getPackageByID(packageID);
            if (pack == null || pack.getPackageID() == null) {
                pack = packCtl.getPackageByID("D1");
            }
            rs = DoChargingCustom(msisdn, packageID, price, pack.getNumOfDate(), cycle, smsID, chanel, chargin, username, isAutoRenew);
            switch (rs.getReturnCode()) {
                case "-1":
                    addNewProfile(msisdn);
                    if (AddService(msisdn, chanel, username, packageID, isAutoRenew)) {
                        return register_free_cycle(msisdn, packageID, price, cycle, smsID, chanel, chargin, username, isAutoRenew);
                    } else {
                        rs.setReturnCode("0");
                        rs.setReturnDesc("KHONG THE KHOI TAO THONG TIN KHACH HANG");
                        rs.setCycle(cycle);
                        return rs;
                    }
                default:
                    return rs;
            }
            //catch level 1
        } catch (Exception e) {
            rs.setReturnCode("0");
            rs.setReturnDesc(e.getMessage());
            return rs;
        }
    }

    public ReturnRegister DoCharging(String msisdn, String packageID, long smsID, String chanel, Charging chargin, String username, boolean isAutoRenew, String extra_info) {
        System.out.println("Call Do Charging");
        ReturnRegister result = new ReturnRegister();
        try {
            packageID = packageID.toUpperCase();
            vn.ctnet.entity.Package pack = null;
            if (packageID == null || "".equals(packageID)) {
                packageID = "D1";
            }
            pack = packCtl.getPackageByID(packageID);
            return DoChargingCustom(msisdn, pack.getPackageID(), (int) pack.getPrice(), (int) pack.getNumOfDate(), 1, smsID, chanel, chargin, username, isAutoRenew, extra_info);
        } catch (Exception e) {
            result.setReturnCode("0");
            result.setReturnDesc("DANG KY KHONG THANH CONG");
        }
        return result;
    }

    public ReturnRegister DoCharging(String msisdn, String packageID, long smsID, String chanel, Charging chargin, String username, boolean isAutoRenew) {
        return DoCharging(msisdn, packageID, smsID, chanel, chargin, username, isAutoRenew, "");
    }

    public ReturnRegister DoChargingCustom(String msisdn, String packageID, int price, int day, int cycle, long smsID, String chanel, Charging chargin, String username, boolean isAutoRenew, String extra_info) {
        System.out.println("Call Do Charging");
        ReturnRegister result = new ReturnRegister();
        result.setCycle(cycle);
        try {
            packageID = packageID.toUpperCase();
            vn.ctnet.entity.Package pack = null;
            if (packageID == null || "".equals(packageID)) {
                packageID = "D1";
            }
            pack = packCtl.getPackageByID(packageID);
            Date d = new Date();
            vn.ctnet.entity.Service service = serviceDao.getServiceByPhone(msisdn);
            if (service != null && !"".equals(service.getPackageID()) && service.getPackageID() != null) {
                System.out.println("Co service: " + service.getPackageID() + "|" + service.getPhone());
                service.setIsAutoRenew(isAutoRenew);
                if (d.before(service.getExpDate()) && (!service.getStatus().equals("0")) && (!service.getStatus().equals("4"))) {
                    System.out.println("Service con han su dung");
                    result.setReturnCode("2");
                    result.setReturnDesc("GOI CUOC DA TON TAI");
                    if (username.equals("apimdeal")) {
                        String msg = getSms("msg_sv_exist");
                        msg = msg.replace("{GOI}", service.getPackageID());
                        msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                        msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                        SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                        msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                        sendSMS(msisdn, "mDeal", msg, smsID);
                    }
                    return result;
                }
                //try level 3
                try {
                    System.out.println("Tru tien KH");

                    //login charing tru tien
                    String rs = chargin.debit(msisdn, (long) price, "049193", msisdn, vn.ctnet.mdeal.config.Utils.getCategoryId(pack.getPackageID(), 10), vn.ctnet.mdeal.config.Utils.getCategoryId(pack.getPackageID(), 6), 272, extra_info);
                    Cdr cdr = new Cdr();
                    cdr.setMsisdn(msisdn);
                    cdr.setShortCode("049193");
                    cdr.setEventID(vn.ctnet.mdeal.config.Utils.getCategoryId(pack.getPackageID(), 6));
                    cdr.setCpid("001001");
                    cdr.setContentID(vn.ctnet.mdeal.config.Utils.getCategoryId(pack.getPackageID(), 10));
                    cdr.setCost((double) price);
                    cdr.setChannelType(chanel);
                    String kpp = "";
                    if(pack.getPackageID().startsWith("MD")) {
                        kpp = ":KPP-"+pack.getPackageID();
                    }
                    cdr.setInformation(chanel+".DK."+pack.getPackageID()+kpp);
                    cdr.setDebitTime(new Date());
                    cdr.setIsPushed(false);
                    if ("CPS-0000".equals(rs)) {
                        System.out.println("Tru tien thanh cong");
                        //tru tien thanh cong
                        service.setExpDate(new Timestamp(vn.ctnet.mdeal.config.Utils.adddays(day * cycle).getTime()));
                        service.setChannel(chanel);
                        service.setPackageID(packageID.toUpperCase());
                        service.setStatus("3");
                        service.setIsPaid(1);
                        service.setModifiedDate(new Timestamp(new Date().getTime()));
                        service.setUserModified(username);
                        SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
                        if (username.equals("apimdeal")) {
                            /*
                                 Gởi tin nhắn báo đăng ký thành công về cho thuê bao
                             */
                            String msg = getSms("msg_dk_ok");
                            msg = msg.replace("{GOI}", pack.getPackageID());
                            msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                            msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                            SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                            msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                            String status = "PRE";
                            sendSMS(msisdn, "mDeal", msg, smsID, status);
                        }

                        result.setReturnCode("1");
                        result.setReturnDesc("DANG KY THANH CONG");
                        result.setChargingResult(rs);
                        result.setPackageId(pack.getPackageID());
                        result.setPrice(price);

                        cdr.setStatus(1);
                    } else if ("CPS-1001".equals(rs)) {
                        System.out.println("Tru tien khong thanh cong, KH khong du tien");
                        cdr.setStatus(0);

                        service.setExpDate(new Timestamp(new Date().getTime()));
                        service.setStatus("4");
                        service.setIsPaid(2);
                        service.setAdjournDate(new Timestamp(new Date().getTime()));
                        service.setRemainAdjournDate(pack.getNumOfDate());
                        service.setRetry(120);
                        service.setRemainMoney(pack.getPrice());
                        service.setUserModified(username);

                        result.setReturnCode("3");
                        result.setReturnDesc("THUE BAO KHONG DU TIEN");
                        result.setChargingResult(rs);
                        if (username.equals("apimdeal")) {
                            /*
                                 Gởi tin nhắn báo đăng ký không thành công về cho thuê bao
                             */
                            String msg = getSms("msg_dk_fail");
                            msg = msg.replace("{GOI}", pack.getPackageID());
                            msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                            msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                            SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                            msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                            sendSMS(msisdn, "9193", msg, smsID);
                        }

                    } else if ("CPS-1002".equals(rs) || "CPS-1003".equals(rs) || "CPS-1004".equals(rs)) {
                        System.out.println("KH bi khoa thue bao");
                        cdr.setStatus(0);

                        result.setReturnCode("0");
                        result.setReturnDesc("DANG KY KHONG THANH CONG");
                        result.setChargingResult(rs);
                        if (username.equals("apimdeal")) {
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
                        }

                    } else {
                        System.out.println("Loi khac khong biet: " + rs);
                        cdr.setStatus(0);
                        result.setReturnCode("0");
                        result.setReturnDesc("LOI KHONG THE TRU TIEN");
                        result.setChargingResult(rs);
                        if (username.equals("apimdeal")) {
                            /*
                            Gởi tin nhắn báo lỗi hệ thống về cho thuê bao
                             */
                            String msg = getSms("msg_err_sys");
                            msg = msg.replace("{GOI}", pack.getPackageID());
                            msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                            msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                            SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                            msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                            sendSMS(msisdn, "9193", msg, smsID);
                        }
                    }

                    try {
                        //ghi log tru cuoc va cap nhat thue bao
                        cdrCtrl.insert(cdr);
                        serviceDao.update(service);
                    } catch (Exception e) {
                        System.out.println("update service " + e.getMessage());
                        result.setReturnCode("0");
                        result.setReturnDesc(e.getMessage());
                        result.setChargingResult(rs);
                    }
                    return result;
                    //catch level 3
                } catch (Exception e) {
                    System.out.println("Tru tien: " + e.getMessage());
                    result.setReturnCode("0");
                    result.setReturnDesc(e.getMessage());
                    return result;
                }
            } else {
                System.out.println("Service khong ton tai return -1");
                result.setReturnCode("-1");
                result.setReturnDesc("THUE BAO CHUA TON TAI TREN HE THONG");
                return result;
            }
        } catch (Exception e) {
            System.out.println("get service: " + e.getMessage());
            result.setReturnCode("0");
            result.setReturnDesc(e.getMessage());
            return result;
        }
    }

    public ReturnRegister DoChargingCustom(String msisdn, String packageID, int price, int day, int cycle, long smsID, String chanel, Charging chargin, String username, boolean isAutoRenew) {
        return DoChargingCustom(msisdn, packageID, price, day, cycle, smsID, chanel, chargin, username, isAutoRenew, "");
    }

    public boolean AddService(String msisdn, String channel, String username, String packageId, boolean isAutoRenew) {
        try {
            Service service = new Service();
            service.setSmsmoID(BigInteger.valueOf(0));
            service.setPhone(msisdn);
            service.setPackageID(packageId);
            service.setRegDate(new Timestamp(new Date().getTime()));
            service.setStartDate(new Timestamp(new Date().getTime()));
            service.setExpDate(new Timestamp(new Date().getTime()));
            service.setChannel(channel);
            service.setIsSynched(false);
            service.setAutoAdjourn(true);
            service.setModifiedDate(new Date());
            service.setStatus("0");
            service.setIsPaid(0);
            service.setRegisterChannel(channel);
            service.setNote("DK_VIA_" + channel);
            service.setUserModified(username);
            service.setIsAutoRenew(isAutoRenew);
            //Cập nhật thông tin thuê bao
            serviceDao.insert(service);
            return true;
        } catch (Exception e) {
            return false;
        }

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
    public String unregister(String msisdn, String packageID, long smsID, String chanel, String user) {

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

    public ReturnRegister unregister_no_mt(String msisdn, String packageID, String chanel, String user) {

        ReturnRegister rs = new ReturnRegister();
        vn.ctnet.entity.Package pack;
        try {
            pack = packCtl.getPackageByID(packageID);
            if (pack == null) {
                //String msg = getSms("msg_wrong");
                //sendSMS(msisdn, "9193", msg, smsID);
                rs.setReturnCode("0");
                rs.setReturnDesc("GOI CUOC KHONG TON TAI TREN HE THONG");
                return rs;
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
                    /*String msg = getSms("msg_huy_ok");
                    msg = msg.replace("{GOI}", pack.getPackageID());
                    msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                    msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                    SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                    msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                    sendSMS(msisdn, "9193", msg, smsID);*/
                    rs.setReturnCode("1");
                    rs.setReturnDesc("HUY DICH VU THANH CONG");
                    rs.setPackageId(packageID);
                    rs.setPrice((int) pack.getPrice());
                    return rs;
                } else {
                    /*String msg = getSms("msg_huy_not_exist");
                    msg = msg.replace("{GOI}", pack.getPackageID());
                    msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                    msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                    SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                    msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                    sendSMS(msisdn, "9193", msg, smsID);
                    respone = "2|HUY_KHONG_THANH CONG_DO_THUE_BAO_CHUA_DANG_KY";*/
                    rs.setReturnCode("2");
                    rs.setReturnDesc("HUY KHONG THANH CONG DO THUE BAO CHUA DANG KY GOI CUOC");
                    return rs;
                }

            } catch (Exception e) {
                //service chua co,DK moi 
                /*String msg = getSms("msg_huy_not_exist");
                msg = msg.replace("{GOI}", pack.getPackageID());
                msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                sendSMS(msisdn, "9193", msg, smsID);
                respone = "0|HUY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";*/
                rs.setReturnCode("0");
                rs.setReturnDesc("HUY KHONG THANH CONG DO LOI HE THONG");
                return rs;
            }
        } catch (Exception e) {
            rs.setReturnCode("0");
            rs.setReturnDesc("HUY KHONG THANH CONG DO LOI HE THONG");
            return rs;
        }
    }

    public ReturnRegister checkstatusbonus(String msisdn, String packageID) {

        ReturnRegister rs = new ReturnRegister();
        vn.ctnet.entity.Package pack;
        try {
            pack = packCtl.getPackageByID(packageID);
            if (pack == null) {
                //String msg = getSms("msg_wrong");
                //sendSMS(msisdn, "9193", msg, smsID);
                rs.setReturnCode("2");
                rs.setReturnDesc("GOI CUOC KHONG TON TAI TREN HE THONG");
                return rs;
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

                    /*String msg = getSms("msg_huy_ok");
                    msg = msg.replace("{GOI}", pack.getPackageID());
                    msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                    msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                    SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                    msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                    sendSMS(msisdn, "9193", msg, smsID);*/
                    rs.setReturnCode("0");
                    rs.setReturnDesc("GOI CUOC DA TON TAI VA DANG KHA DUNG TREN HE THONG");
                    rs.setPackageId(packageID);
                    rs.setPrice((int) pack.getPrice());
                    return rs;
                } else {
                    /*String msg = getSms("msg_huy_not_exist");
                    msg = msg.replace("{GOI}", pack.getPackageID());
                    msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                    msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                    SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                    msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                    sendSMS(msisdn, "9193", msg, smsID);
                    respone = "2|HUY_KHONG_THANH CONG_DO_THUE_BAO_CHUA_DANG_KY";*/
                    rs.setReturnCode("1");
                    rs.setReturnDesc("CO THE DANG KY GOI CUOC");
                    return rs;
                }

            } catch (Exception e) {
                //service chua co,DK moi 
                /*String msg = getSms("msg_huy_not_exist");
                msg = msg.replace("{GOI}", pack.getPackageID());
                msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                sendSMS(msisdn, "9193", msg, smsID);
                respone = "0|HUY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";*/
                rs.setReturnCode("2");
                rs.setReturnDesc("LOI HE THONG");
                return rs;
            }
        } catch (Exception e) {
            rs.setReturnCode("2");
            rs.setReturnDesc("LOI HE THONG");
            return rs;
        }
    }

    public ReturnRegister QueueConfirmRegister(String msisdn, String pkg, String channel, boolean isSentMt) {
        ReturnRegister rs = new ReturnRegister();
        vn.ctnet.entity.Package pack;
        try {
            pack = packCtl.getPackageByID(pkg);
            if (pack == null) {
                //String msg = getSms("msg_wrong");
                //sendSMS(msisdn, "9193", msg, smsID);
                rs.setReturnCode("2");
                rs.setReturnDesc("GOI CUOC KHONG TON TAI TREN HE THONG");
                System.out.println(rs.getReturnCode() + "|" + rs.getReturnDesc());
                return rs;
            }
            try {

                Date d = new Date();
                vn.ctnet.entity.Service service = serviceDao.getServiceByPhone(msisdn);
                if (service != null && service.getPhone() != null && !service.getPhone().equals("")) {
                    if ((d.before(service.getExpDate()) //con han su dung
                            && (!service.getStatus().equals("0")) //trang thai dang kich hoat
                            && service.getPackageID().equals(pack.getPackageID())//goi cuoc co ton tai
                            )
                            || (service.getStatus().equals("4") //trang thai dang kich hoat
                            && service.getPackageID().equals(pack.getPackageID())//goi cuoc co ton tai
                            )) {

                        rs.setReturnCode("0");
                        rs.setReturnDesc("GOI CUOC DA TON TAI VA DANG KHA DUNG TREN HE THONG");
                        rs.setPackageId(pkg);
                        rs.setPrice((int) pack.getPrice());
                        System.out.println(rs.getReturnCode() + "|" + rs.getReturnDesc());

                        return rs;
                    } else {

                        SMSProcess exe = new SMSProcess();
                        exe.QueueConfirmRegister(msisdn, pkg, 0, isSentMt);
                        rs.setReturnCode("1");
                        rs.setReturnDesc("DANG KY LAI");
                        System.out.println(rs.getReturnCode() + "|" + rs.getReturnDesc());
                        return rs;
                    }
                } else {
                    SMSProcess exe = new SMSProcess();
                    exe.QueueConfirmRegister(msisdn, pkg, 0, isSentMt);
                    rs.setReturnCode("1");
                    rs.setReturnDesc("DANG KY MOI");
                    System.out.println(rs.getReturnCode() + "|" + rs.getReturnDesc());
                    return rs;
                }
                //serviec co roi

            } catch (Exception e) {

                rs.setReturnCode("2");
                rs.setReturnDesc("LOI HE THONG");
                System.out.println(rs.getReturnCode() + "|" + rs.getReturnDesc() + "|" + e.getMessage());
                return rs;
            }
        } catch (Exception e) {
            rs.setReturnCode("2");
            rs.setReturnDesc("LOI HE THONG");
            return rs;
        }
    }

    /**
     * Hàm kiểm tra thông tin thuê bao
     *
     * @param msisdn
     * @param smsID
     * @param chanel
     * @return
     */
    public Service checkProfile(String msisdn, long smsID, String chanel, boolean isSendMt) {

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
                if (isSendMt) {
                    sendSMS(msisdn, "mDeal", msg, smsID);
                }

                //    respone = "1|SUCCESS";
                return service;
            } else {
                System.out.println("het han su dung");
                String msg = getSms("msg_not_reg");
                if (isSendMt) {
                    sendSMS(msisdn, "9193", msg, smsID);
                }
                //    respone = "2|SUBSCRIBER_NOT_REGISTER";
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            String msg = getSms("msg_not_reg");
            if (isSendMt) {
                sendSMS(msisdn, "9193", msg, smsID);
            }
            //  respone = "2|SUBSCRIBER_NOT_REGISTER";
            return null;
        }
    }

    public Service checkProfile(String msisdn, long smsID, String chanel) {
        return checkProfile(msisdn, smsID, chanel, true);
    }

    /**
     * Get thong tin thue bao
     *
     * @param msisdn
     * @return
     */
    public Service getService(String msisdn) {
        try {
            Service service = serviceDao.getServiceByPhone(msisdn);
            return service;
        } catch (Exception e) {
            return null;
        }
    }

    public vn.ctnet.entity.Package getPackage(String PackageId) {
        try {
            return packCtl.getPackageByID(PackageId);
        } catch (Exception e) {
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
        Reader reader = null;
        SmsContentDAO smsDao = new SmsContentDAO();
        try {
            SmsContent sc = smsDao.getSms(name);
            if (sc != null && !"".equals(sc.getContent())) {
                return sc.getContent();
            }
        } catch (Exception e) {

        }
        try {
            String path = "C:\\mdeal_config\\sms.properties";
            input = new FileInputStream(new File(path));
            reader = new InputStreamReader(input, Charset.forName("UTF-8"));
            // load a properties file
            prop.load(reader);
            return prop.getProperty(name);

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return "";
        } finally {
            if (input != null) {
                try {
                    input.close();
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
