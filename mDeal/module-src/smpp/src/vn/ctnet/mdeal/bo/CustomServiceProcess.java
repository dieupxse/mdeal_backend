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
 * @author dieup
 * Class này thực hiện cho việc đăng ký qua webservice với các tham số, thuộc tính tùy chỉnh
 */
public class CustomServiceProcess {
    /*
    Variable
    */
    private final ServiceDAO serviceDao = new ServiceDAO();
    private final PackageDAO packCtl = new PackageDAO();
    private final CdrDAO cdrCtrl = new CdrDAO();
    
    /*
       Lấy giá trị config
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
    
    /*
    Tạo giá trị số random trong khoảng minmax
    */
    public int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }
    
    /*
    Hàm đăng ký custom
    Params:
    - msisdn: số điện thoại
    - packageId: tên gói cước
    - smsId: moId nếu có
    - chanel: Kênh đăng ký
    - chargin: Charging sesssion
    - statusMt: trạng thái bản tin Mt: PRE/SENT
    - note: ghi chú
    */
    public String register_custom(String msisdn, String packageID, long smsID, String chanel, Charging chargin, String statusMT,String note) {
        int free_day = Integer.parseInt(getValue("free_day"));
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
                    msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.') );
                    SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                    msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                    if(note==null || !note.equals("CUSTOM_LINK")) {
                        sendSMS(msisdn, "mDeal", msg, smsID,statusMT);
                    }
                    respone = "2|MA_GOI_DA_TON_TAI";

                } //neu het han su dung hoac chua dang ky thi dang ky cho nguoi dung
                else {
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
                            service.setNote(note);
                            // serviceCtl.edit(service);
                            String msg = getSms("msg_dk_ok");
                            msg = msg.replace("{GOI}", pack.getPackageID());
                            msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                            msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.') );
                            SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                            msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                            sendSMS(msisdn, "mDeal", msg, smsID,statusMT);
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
                            service.setNote(note);
                            
                            respone = "3|THUE_BAO_KHONG_DU_TIEN";
                            String msg = getSms("msg_dk_fail");
                            msg = msg.replace("{GOI}", pack.getPackageID());
                            msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                            msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.') );
                            SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                            msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                            if(note==null || !note.equals("CUSTOM_LINK")) {
                                sendSMS(msisdn, "9193", msg, smsID,statusMT);
                            }
                        } else if ("CPS-1002".equals(rs)) {
                            cdr.setStatus(0);
                            respone = "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";
                            String msg = getSms("msg_dk_fail");
                            msg = msg.replace("{GOI}", pack.getPackageID());
                            msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                            msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.') );
                            SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                            msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                            sendSMS(msisdn, "9193", msg, smsID,statusMT);
                        } else if ("CPS-1003".equals(rs)) {
                            cdr.setStatus(0);
                            respone = "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";
                            String msg = getSms("msg_dk_fail");
                            msg = msg.replace("{GOI}", pack.getPackageID());
                            msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                            msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.') );
                            SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                            msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                            sendSMS(msisdn, "9193", msg, smsID,statusMT);
                        } else if ("CPS-1004".equals(rs)) {
                            cdr.setStatus(0);
                            String msg = getSms("msg_dk_fail");
                            msg = msg.replace("{GOI}", pack.getPackageID());
                            msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                            msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.') );
                            SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                            msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                            sendSMS(msisdn, "9193", msg, smsID,statusMT);
                            respone = "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";
                        } else {
                            cdr.setStatus(0);
                            String msg = getSms("msg_err_sys");
                            msg = msg.replace("{GOI}", pack.getPackageID());
                            msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                            msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.') );
                            SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                            msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                            sendSMS(msisdn, "9193", msg, smsID,statusMT);
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
                    service.setNote(note);
                    serviceDao.insert(service);
                    //   serviceDao.insert(service);
                    SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
                    respone = "1|" + packageID + "|0|" + format.format(service.getExpDate());
                    String msg = getSms("msg_free_7_day");
                    msg = msg.replace("{GOI}", pack.getPackageID());
                    msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                    msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.') );
                    SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                    msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                    msg = msg.replace("{FREE}", free_day + "");
                    sendSMS(msisdn, "mDeal", msg, smsID,statusMT);

                } catch (Exception de) {
                    System.out.println(de.getMessage());
                    de.printStackTrace();
                }

            }
        } catch (Exception e) {
            //ko co
            // e.printStackTrace();
            String msg = getSms("msg_wrong");
            sendSMS(msisdn, "9193", msg, smsID,statusMT);
        }

        return respone;

    }
    
    /*
    Hàm gửi tin nhắn
    Params:
    - msisdn: số điện thoại cần gởi
    - brandName: gởi từ brandname nào 9193/mDeal
    - message: Nội dung tin nhắn
    - smsId: moId nếu có
    */
    void sendSMS(String msisdn, String brandName, String message, long smsID) {
        InsertSMS insertSMS = new InsertSMS(msisdn, brandName, message, smsID);
        insertSMS.start();
    }
    
    /*
    Hàm gởi tin nhắn
    Params:
    - msisdn: số điện thoại cần gởi
    - brandName: gởi từ brandname nào 9193/mDeal
    - message: Nội dung tin nhắn
    - smsId: moId nếu có
    - status: trạng thái bản tin PRE/SENT 
    */
    void sendSMS(String msisdn, String brandName, String message, long smsID,String status) {
        InsertSMS insertSMS = new InsertSMS(msisdn, brandName, message, smsID,status);
        insertSMS.start();
    }
    
    /*
    Hàm add new profile, thêm người dùng vào danh sách thuê bao sử dụng
    Params:
    - msisdn: số điện thoại
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
    
    /*
    Hàm get nội dung bản tin Mt
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
