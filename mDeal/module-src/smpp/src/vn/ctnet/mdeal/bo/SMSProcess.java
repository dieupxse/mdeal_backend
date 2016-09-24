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
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;

import vn.ctnet.dao.*;
import vn.ctnet.entity.*;

/**
 *
 * @author vanvtse90186
 * Class này thực hiện cho các giao dịch đăng ký qua SMS và API VAS
 */
public class SMSProcess {
    /*
     Khai báo biến
     */

    private final ServiceDAO serviceDao = new ServiceDAO();
    private final PackageDAO packCtl = new PackageDAO();
    private final CdrDAO cdrCtrl = new CdrDAO();
    private final QueueRequestDAO  QueueDAO  = new QueueRequestDAO();
    /*
     * Hàm lấy giá trị config 
     * @param name
     * @return String
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
     * Hàm tạo số random trong khoảng giá trị Min,Max
     *
     * @param min
     * @param max
     * @return int
     */
    public int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    /**
     * Hàm đăng ký gói cước
     *
     * @param msisdn : số điện thoại
     * @param packageID : Tên gói cước
     * @param smsID : MoId nếu có
     * @param chanel : Kênh đăng ký
     * @param chargin : Charging Proxy session
     * @return
     */
    public String register(String msisdn, String packageID, long smsID, String chanel, Charging chargin) {
        /*
         Khai báo các biến nội bộ
         */
        //giá trị này quyết định các số nào được phép đăng ký, chỉ dùng trong trường hợp test
        String allownumber = getValue("allow_number");
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
        int free_day = 0;

        //Nếu đăng ký qua kênh Vas
        if ("VAS".equals(chanel)) {
            free_day = Integer.parseInt(getValue("free_day_vas"));
        } //Đăng ký qua các kênh khác
        else {
            free_day = Integer.parseInt(getValue("free_day"));
        }

        //Khởi tạo gói cước mặc định nếu packageId không tồn tại
        if (packageID == null || "".equals(packageID)) {
            packageID = "D1";
        }
        packageID = packageID.toUpperCase();
        String respone = "";
        vn.ctnet.entity.Package pack = null;
        Date d = new Date();

        /*
         Khối xử lý logic đăng ký
         */
        try {
            //Lấy thông tin gói cước từ packageId
            pack = packCtl.getPackageByID(packageID);
            //khởi tạo gói cước mặc định D1 nếu gói cước không tồn tại
            if (pack == null || pack.getPackageID() == null) {
                pack = packCtl.getPackageByID("D1");
            }

            //Thông tin thuê bao đã tồn tại trong hệ thống, người dùng đã từng đăng ký dịch vụ rồi
            try {
                //Lấy thông tin thuê bao qua số điện thoại
                vn.ctnet.entity.Service service = serviceDao.getServiceByPhone(msisdn);

                //Nếu thông tin thuê bao đã tồn tại và còn hạn sử dụng, báo gói cước đã tồn tại về cho thuê bao
                if (d.before(service.getExpDate()) && (!service.getStatus().equals("0")) && (!service.getStatus().equals("4"))) {
                    String msg = getSms("msg_sv_exist");
                    msg = msg.replace("{GOI}", service.getPackageID());
                    msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                    msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                    SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                    msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                    sendSMS(msisdn, "mDeal", msg, smsID);
                    respone = "2|MA_GOI_DA_TON_TAI";
                } //Thông tin thuê bao tồn tại và đã hết ngày sử dụng, tiến hành đăng ký lại cho thuê bao
                else {
                    
                    //Nếu đăng ký sử dụng API qua kênh vas
                    if (chanel.equals("VAS") || chanel.equals("SYS")) {
                        /*
                         Khối lệnh xử lý đăng ký qua kênh vas
                         */
                        try {
                            //Thực hiện trừ tiền thuê bao
                            String rs = chargin.debit(msisdn, (long) pack.getPrice(), "049193", msisdn, vn.ctnet.mdeal.config.Utils.getCategoryId(pack.getPackageID(), 10), vn.ctnet.mdeal.config.Utils.getCategoryId(pack.getPackageID(), 6), 272);
                            /*
                             Khởi tạo giá trị CDR, log thông tin trừ cước
                             */
                            Cdr cdr = new Cdr();
                            cdr.setMsisdn(msisdn);
                            cdr.setShortCode("049193");
                            cdr.setEventID(vn.ctnet.mdeal.config.Utils.getCategoryId(pack.getPackageID(), 6));
                            cdr.setCpid("001001");
                            cdr.setContentID(vn.ctnet.mdeal.config.Utils.getCategoryId(pack.getPackageID(), 10));
                            cdr.setCost(pack.getPrice());
                            cdr.setChannelType("VAS");
                            cdr.setInformation("MdealVasCharging");
                            cdr.setDebitTime(new Date());
                            cdr.setIsPushed(false);

                            // Trừ cước thành công, cập nhật thông tin người dùng và trả về kết quả cho API VAS
                            if ("CPS-0000".equals(rs)) //khối lệnh logic
                            {
                                /*
                                 Cập nhật thông tin thuê bao
                                 */
                                service.setExpDate(new Timestamp(vn.ctnet.mdeal.config.Utils.adddays(pack.getNumOfDate()).getTime()));
                                service.setChannel(chanel);
                                service.setPackageID(packageID.toUpperCase());
                                service.setStatus("3"); //trạng thái đăng ký lại
                                service.setIsPaid(1); //đã thanh toán thành công
                                service.setModifiedDate(new Timestamp(new Date().getTime()));

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

                                //Giảm tỉ lệ bản tin trả về cho khách hàng
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

                                /*
                                 Trả về kết quả cho API VAS
                                 */
                                SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
                                respone = "1|" + pack.getPackageID() + "|" + (int) pack.getPrice() + "|" + format.format(service.getExpDate());

                                //Cập nhật trạng thái CDR => thành công.
                                cdr.setStatus(1);
                            } //Thuê bao không đủ tiền
                            else if ("CPS-1001".equals(rs)) {
                                //Cập nhật trạng thái CDR => Không thành công
                                cdr.setStatus(0);
                                
                                //chuyển thuê bao sang retry
                                service.setExpDate(new Timestamp(new Date().getTime()));
                                service.setStatus("4");
                                service.setIsPaid(2);
                                service.setAdjournDate(new Timestamp(new Date().getTime()));
                                service.setRemainAdjournDate(pack.getNumOfDate());
                                service.setRetry(120);
                                service.setRemainMoney(pack.getPrice());
                                service.setNote("DK_VIA_VAS");
                                /*
                                 Cập nhật trạng thái thuê bao
                                 */
                                service.setExpDate(new Timestamp(new Date().getTime()));
                                service.setStatus("4"); //trạng thái thành retry trừ cước
                                service.setIsPaid(2); //Chưa thanh toán - đang chờ retry
                                service.setAdjournDate(new Timestamp(new Date().getTime())); //thời gian retry trừ cước
                                service.setRemainAdjournDate(pack.getNumOfDate()); //số ngày còn lại của gói cước
                                service.setRetry(120); //số lần trừ cước còn lại
                                service.setRemainMoney(pack.getPrice()); //số tiền còn phải trừ

                                //kết quả trả về cho API VAS
                                respone = "3|THUE_BAO_KHONG_DU_TIEN";

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
                            } //Thuê bao không khả dụng
                            else if ("CPS-1002".equals(rs) || "CPS-1003".equals(rs) || "CPS-1004".equals(rs)) 
                            {
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

                            } //Các trường hợp lỗi hệ thống khác
                            else 
                            {
                                //cập nhật trạng thái CDR => không thành công
                                cdr.setStatus(0);
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

                                //trả về kết qua cho API VAS
                                respone = "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";
                            } //end khối lệnh logic

                            /*
                             Cập nhật thông tin thuê bao, CDR
                             */
                            try {
                                cdrCtrl.insert(cdr);
                                serviceDao.update(service);
                            } catch (Exception e) {
                                System.out.println("Ghi long tru cuoc CDR ERROR !!!!!!!!");
                                System.out.println(e.getMessage());
                            }

                            /*
                             Kết thúc khối lệnh đăng ký qua kênh Vas    
                             */
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Đã xảy ra lỗi");
                            System.out.println(e.getMessage());
                        }

                    } //Nếu không phải đăng ký qua kênh vas
                    else {
                        /*
                         Chuyển trạng thái thuê bao cho module trừ cước thực hiện tiếp
                         */
                        service.setExpDate(new Timestamp(vn.ctnet.mdeal.config.Utils.adddays(pack.getNumOfDate()).getTime()));
                        service.setStatus("3"); //đăng ký lại
                        service.setPackageID(pack.getPackageID());
                        service.setIsPaid(0); //chưa thanh toán
                        service.setChannel(chanel);
                        service.setModifiedDate(new Date());
                        service.setSmsmoID(BigInteger.valueOf(smsID));
                        service.setModifiedDate(new Timestamp(new Date().getTime()));
                        service.setNote("DK_SMS");
                        //Cập nhật thông tin gói cước
                        serviceDao.update(service);
                    }
                }

            } //Nếu thông tin thuê bao chưa tồn tại
            catch (Exception e) {
                /*
                 Khởi tạo thông tin, thêm mới người dùng
                 */
                try {
                    System.out.println("Tao moi profile");
                    addNewProfile(msisdn);
                } catch (Exception ex) {
                    System.out.println("Profile da ton tai");
                    System.out.println(ex.getMessage());
                }

                /*
                 Thông tin đăng ký lần đầu tiên, miễn phí gói cước
                 */
                //Đăng ký miễn phí
                try {
                    SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                    /*
                     Khởi tạo thông tin đăng ký 
                     */
                    Service service = new Service();
                    service.setSmsmoID(BigInteger.valueOf(smsID));
                    service.setPhone(msisdn);
                    service.setPackageID(pack.getPackageID());
                    service.setRegDate(new Timestamp(new Date().getTime()));
                    service.setStartDate(new Timestamp(new Date().getTime()));
                    service.setExpDate(new Timestamp(vn.ctnet.mdeal.config.Utils.adddays(free_day).getTime()));
                    service.setChannel(chanel);
                    service.setIsSynched(false);
                    service.setAutoAdjourn(true);
                    service.setModifiedDate(new Date());
                    service.setStatus("1");
                    service.setIsPaid(1);
                    service.setRegisterChannel(chanel);
                    service.setModifiedDate(new Timestamp(new Date().getTime()));
                    service.setNote("DK_SMS");
                    //Cập nhật thông tin thuê bao
                    serviceDao.insert(service);

                    /*
                     Gởi tin nhắn thông báo đăng ký thành công cho người dùng
                     */
                    SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
                    respone = "1|" + packageID + "|0|" + format.format(service.getExpDate());
                    String msg = getSms("msg_free_7_day");
                    msg = msg.replace("{GOI}", pack.getPackageID());
                    msg = msg.replace("{NGAY}", (pack.getNumOfDate()==1 ? "" :  pack.getNumOfDate()+""));
                    msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                    msg = msg.replace("{FREE}", free_day + "");
                    msg = msg.replace("{DATE}", fm.format(service.getExpDate()));

                    /**
                     * Giam ti le MT tra ve thong bao mien phi 7 ngay
                     *
                     */
                    String status = "PRE";
                    String isModifiedMt = getValue("modified_mt_7_day_free");
                    if (isModifiedMt != null && isModifiedMt.equals("1")) {
                        int rand = randInt(1, 10);
                        if (rand % 2 == 0) {
                            status = "PRE";
                        } else {
                            status = "SENT";
                        }
                    }
                    
                    //gởi tin nhắn
                    sendSMS(msisdn, "mDeal", msg, smsID, status);

                } catch (Exception de) {
                    System.out.println(de.getMessage());
                    de.printStackTrace();
                }

            }
        } catch (Exception e) {
            String msg = getSms("msg_wrong");
            sendSMS(msisdn, "9193", msg, smsID);
        }
        //trả về kết qua cho API Vas
        return respone;

    }

    /**
     * Hàm Hủy gói cước
     * @param msisdn
     * @param packageID
     * @param smsID
     * @param chanel
     * @return 
     */
    public String unregister(String msisdn, String packageID, long smsID, String chanel) {

        String respone = "";
        vn.ctnet.entity.Package pack;
        try 
        {
            //lấy thông tin gói cước
            pack = packCtl.getPackageByID(packageID);
            
            /*
            Nếu thông tin gói cước không tồn tại, báo hủy không thành công cho người dùng
            */
            if (pack == null || pack.getPackageID() == null) {
                String msg = getSms("msg_wrong");
                sendSMS(msisdn, "9193", msg, smsID);
                return "0|HUY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";
            }
            
            /**
             * Nếu gói cước tồn tại, xử lý hủy
             */
            try 
            {
                Date d = new Date();
                //Lấy thông tin thuê bao
                vn.ctnet.entity.Service service = serviceDao.getServiceByPhone(msisdn);
                
                /*
                Nếu thuê bao đang hoạt động tương ứng với gói cước được hủy,
                tiến hành hủy gói cước cho thuê bao
                */
                if ( //huy dich vu khi dich vu dang hoat dong
                        (d.before(service.getExpDate()) //con han su dung
                        && (!service.getStatus().equals("0")) //trang thai dang kich hoat
                        && service.getPackageID().equals(pack.getPackageID()) //goi cuoc co ton tai
                        )
                        || ((service.getStatus().equals("4")) //trang thai dang retry
                        && service.getPackageID().equals(pack.getPackageID())) //goi cuoc co ton tai
                        ) {
                    /*
                    Cập nhật lại thông tin gói cước
                    */
                    service.setStatus("0"); //trạng thái hủy
                    service.setChannel(chanel);
                    service.setAutoAdjourn(false);
                    service.setExpDate(new Timestamp(new Date().getTime()));
                    service.setStartDate(new Timestamp(new Date().getTime()));
                    service.setModifiedDate(new Timestamp(new Date().getTime()));
                    serviceDao.update(service);
                    
                    /*
                    Gởi thông báo hủy thành công
                    */
                    String msg = getSms("msg_huy_ok");
                    msg = msg.replace("{GOI}", pack.getPackageID());
                    msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                    msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                    SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                    msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                    sendSMS(msisdn, "9193", msg, smsID);
                    
                    //trả về kết quả cho API Vas
                    respone = "1|HUY_THANH_CONG";
                }
                /*
                Nếu gói cước tồn tại nhưng đã hết hạn sử dụng, hoặc không tương ứng với gói cước được hủy
                */
                else 
                {
                    /*
                    Gởi MT thông báo hủy gói cước không tồn tại
                    */
                    String msg = getSms("msg_huy_not_exist");
                    msg = msg.replace("{GOI}", pack.getPackageID());
                    msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                    msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                    SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                    msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                    sendSMS(msisdn, "9193", msg, smsID);
                    
                    //trả về kết qua cho API VAS
                    respone = "2|HUY_KHONG_THANH CONG_DO_THUE_BAO_CHUA_DANG_KY";
                }

            }
            //Nếu thuê bao không tồn tại
            catch (Exception e) 
            {
                /*
                 Khởi tạo thông tin, thêm mới người dùng
                 */
                try {
                    System.out.println("Tao moi profile");
                    addNewProfile(msisdn);
                } catch (Exception ex) {
                    System.out.println("Profile da ton tai");
                }
                
                /*
                Gởi bản tin MT báo chưa đăng ký gói cước
                */
                String msg = getSms("msg_huy_not_exist");
                msg = msg.replace("{GOI}", pack.getPackageID());
                msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                sendSMS(msisdn, "9193", msg, smsID);
                
                //tra kết quả cho API VAS
                respone = "0|HUY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";
            }
        } catch (Exception e) {
            /*
            Gởi MT thông báo hủy không thành công
            */
            e.printStackTrace();
            String msg = getSms("msg_huy_fail");
            msg = msg.replace("{GOI}", packageID);
            sendSMS(msisdn, "9193", msg, smsID);
            respone = "0|HUY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU";
        }

        return respone;

    }

    /**
     * Hàm kiểm tra trạng thái thuê bao
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
     * Kiểm tra trạng thái và đăng ký nếu chưa có gói cước.
     * @param msisdn
     * @param smsID
     * @param chanel 
     * @param charging 
     * @return  
     */
    public boolean checkStatusAndRegister(String msisdn, long smsID, String chanel, Charging charging) throws ClassNotFoundException, SQLException {
        String defaultPackageByGetCode = getValue("default_package_get_code");
        Date d = new Date();
        Service service = serviceDao.getServiceByPhone(msisdn);
        if (service!=null && service.getExpDate() != null && (d.before(service.getExpDate())) && (!service.getStatus().equals("0"))) {
            return true;
        } else {
            String rsp = register(msisdn,defaultPackageByGetCode, smsID, chanel, charging);
            String[] data = rsp.replace("|", ":").split(":");
            return data.length >=2 && ("1".equals(data[0]) || "2".equals(data[0]));
        }
    }
    
    /**
     * get mCard
     * @param msisdn
     * @param smsID 
     */
    
    
    
    public void getMcard(String msisdn, long smsID) {
        try {
            //implement webservice
            URL url = new URL(getValue("ws_getcode"));      
            long id = Long.parseLong(getValue("id_get_code"));
            int expHour = Integer.parseInt(getValue("code_exp_after_hour"));
            String brandName = (getValue("brandname_mcard") == null ? "9193" :getValue("brandname_mcard")); 
            GetCode ws = new GetCode(url);
            String rs = ws.getGetCodeSoap().getMaCode(msisdn, id);
            
            String msg = getSms("get_code");
            msg = msg.replace("{CODE}", rs);
            SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            msg = msg.replace("{DATE}", fm.format(vn.ctnet.mdeal.config.Utils.addHour(expHour)));
            sendSMS(msisdn, brandName, msg, smsID);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    /**
     * Hàm gởi tin hướng dẫn sử dụng cho thuê bao
     * @param msisdn
     * @param l 
     */
    void guideUser(String msisdn, long l) {
        String msg = getSms("msg_hd");
        sendSMS(msisdn, "9193", msg, l);
    }
    
    /**
     * Hàm gởi tin nhắn
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
     * Hàm gởi tin nhắn
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
     * Hàm thêm mới tài khoản người dùng
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
     * Hàm gởi link tải ứng dụng
     * @param msisdn
     * @param l 
     */
    void tai(String msisdn, long l) {
        String msg = getSms("msg_tai");
        sendSMS(msisdn, "9193", msg, l);
    }
    
    /**
     * Hàm lấy nội dung bản tin MT
     * @param name
     * @return 
     */
    public String getSms(String name) 
    {

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
    
    /**
     * Insert unregister request to queue
     * @param msisdn
     * @param pkg
     * @param smsID 
     */
    public void QueueCancel(String msisdn, String pkg, long smsID)
    {
        int confirmExpAfterMinute = Integer.parseInt(getValue("confirm_exp_after_minute"));
        vn.ctnet.entity.Package pack;
        try 
        {
            //lấy thông tin gói cước
            pack = packCtl.getPackageByID(pkg);
            
            /*
            Nếu thông tin gói cước không tồn tại, báo hủy không thành công cho người dùng
            */
            if (pack == null || pack.getPackageID() == null) {
                String msg = getSms("msg_wrong");
                sendSMS(msisdn, "9193", msg, smsID);
                return;
            }
            
            /**
             * Nếu gói cước tồn tại, xử lý gởi xác nhận hủy
             */
            try 
            {
                Date d = new Date();
                //Lấy thông tin thuê bao
                vn.ctnet.entity.Service service = serviceDao.getServiceByPhone(msisdn);
                
                /*
                Nếu thuê bao đang hoạt động tương ứng với gói cước được hủy,
                tiến hành đưa vào danh sách queue
                */
                if ( //huy dich vu khi dich vu dang hoat dong
                        (d.before(service.getExpDate()) //con han su dung
                        && (!service.getStatus().equals("0")) //trang thai dang kich hoat
                        && service.getPackageID().equals(pack.getPackageID()) //goi cuoc co ton tai
                        )
                        || ((service.getStatus().equals("4")) //trang thai dang retry
                        && service.getPackageID().equals(pack.getPackageID())) //goi cuoc co ton tai
                        ) {
                    /*
                    Đưa vào danh sách queue
                    */
                 
                    try {
                        QueueRequest queueRequest = new QueueRequest();
                        queueRequest.setAction("UNREGISTER");
                        queueRequest.setCreateDate(new Timestamp(new Date().getTime()));
                        queueRequest.setExpDate(new Timestamp(vn.ctnet.mdeal.config.Utils.addMinute(confirmExpAfterMinute).getTime()));
                        queueRequest.setPhone(msisdn);
                        queueRequest.setStatus(false);
                        
                        QueueDAO.insert(queueRequest);
                        /*
                        Gởi thông báo hủy thành công
                        */
                        String msg = getSms("msg_confirm_unregister");
                        msg = msg.replace("{GOI}", pack.getPackageID());
                        msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                        msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                        SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                        msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                        msg = msg.replace("{PHUT}", confirmExpAfterMinute+"");
                        sendSMS(msisdn, "9193", msg, smsID);
                        return;
                    } 
                    catch(Exception e) 
                    {
                         String msg = getSms("msg_err_sys");
                         sendSMS(msisdn, "9193", msg, smsID);
                    
                    }
                    
                }
                /*
                Nếu gói cước tồn tại nhưng đã hết hạn sử dụng, hoặc không tương ứng với gói cước được hủy
                */
                else 
                {
                    /*
                    Gởi MT thông báo hủy gói cước không tồn tại
                    */
                    String msg = getSms("msg_huy_not_exist");
                    msg = msg.replace("{GOI}", pack.getPackageID());
                    msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                    msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                    SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                    msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                    sendSMS(msisdn, "9193", msg, smsID);
                    return;
                }

            }
            //Nếu thuê bao không tồn tại
            catch (Exception e) 
            {
                /*
                 Khởi tạo thông tin, thêm mới người dùng
                 */
                try {
                    System.out.println("Tao moi profile");
                    addNewProfile(msisdn);
                } catch (Exception ex) {
                    System.out.println("Profile da ton tai");
                }
                
               /*
                Gởi MT thông báo hủy gói cước không tồn tại
                */
                String msg = getSms("msg_huy_not_exist");
                msg = msg.replace("{GOI}", pack.getPackageID());
                msg = msg.replace("{NGAY}", pack.getNumOfDate() + "");
                msg = msg.replace("{GIA}", String.format(Locale.US, "%,d", ((int) pack.getPrice())).replace(',', '.'));
                sendSMS(msisdn, "9193", msg, smsID);
                
                
            }
        } catch (Exception e) {
            /*
            Gởi MT thông báo hủy không thành công
            */
            e.printStackTrace();
            String msg = getSms("msg_huy_fail");
            msg = msg.replace("{GOI}", pkg);
            sendSMS(msisdn, "9193", msg, smsID);
            
        }

    }
    
    public void UnregisterAccepted(String msisdn, String pkg, long smsID) throws ClassNotFoundException, SQLException 
    {
            /**
             * Kiem tra thue bao co goi ban tin huy hay chua, va ban tin co hieu lục hay khong
             */
             
             ArrayList<QueueRequest> qr = QueueDAO.GetQueueByParam(msisdn,0);
             if(qr!=null && qr.size()>0) {
                 for(QueueRequest q : qr) {
                     q.setStatus(true);
                     QueueDAO.update(q);
                 }
             } else {
                 
                    String msg = getSms("msg_wrong");
                    sendSMS(msisdn, "9193", msg, smsID);
                 return;
             }
        
            /**
             * Nếu gói cước tồn tại, xử lý hủy
             */
            try 
            {
                Date d = new Date();
                //Lấy thông tin thuê bao
                vn.ctnet.entity.Service service = serviceDao.getServiceByPhone(msisdn);
                
                /*
                Nếu thuê bao đang hoạt động tương ứng với gói cước được hủy,
                tiến hành hủy gói cước cho thuê bao
                */
                if (service!=null && service.getPhone()!=null) {
                    /*
                    Cập nhật lại thông tin gói cước
                    */
                    service.setStatus("0"); //trạng thái hủy
                    service.setChannel("SMS");
                    service.setAutoAdjourn(false);
                    service.setExpDate(new Timestamp(new Date().getTime()));
                    service.setStartDate(new Timestamp(new Date().getTime()));
                    service.setModifiedDate(new Timestamp(new Date().getTime()));
                    serviceDao.update(service);
                    
                    /*
                    Gởi thông báo hủy thành công
                    */
                    String msg = getSms("msg_huy_ok");
                    msg = msg.replace("{GOI}", service.getPackageID());
                    SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy");
                    msg = msg.replace("{DATE}", fm.format(service.getExpDate()));
                    sendSMS(msisdn, "9193", msg, smsID);
                }
            }
            //Nếu thuê bao không tồn tại
            catch (Exception e) 
            {
                /*
                 Khởi tạo thông tin, thêm mới người dùng
                 */
                try {
                    System.out.println("Tao moi profile");
                    addNewProfile(msisdn);
                } catch (Exception ex) {
                    System.out.println("Profile da ton tai");
                }
                String msg = getSms("msg_huy_fail");
                msg = msg.replace("{GOI}", pkg);
                sendSMS(msisdn, "9193", msg, smsID);
            }

    }
}
