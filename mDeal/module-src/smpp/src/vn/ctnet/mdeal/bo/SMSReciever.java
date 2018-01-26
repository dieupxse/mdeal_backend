/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.mdeal.bo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import vn.ctnet.dao.SmsMoDAO;
import vn.ctnet.entity.SmsMo;
import vn.ctnet.dao.PackageDAO;
import charging.Charging;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import vn.ctnet.dao.SmsContentDAO;
import vn.ctnet.entity.SmsContent;

/**
 *
 * @author vanvtse90186
 */
public class SMSReciever {

    /*
     Khai báo biến
     */
    private final PackageDAO packageDAO = new PackageDAO();
    private final SmsMoDAO smsDAO = new SmsMoDAO();

    /**
     * Hàm gởi tạo bản tin MO
     *
     * @param message
     * @param msisdn
     * @param destAdd
     * @param charging
     * @return
     */
    public String recieverSMS(String message, String msisdn, String destAdd, Charging charging) {

        Date d = new Date();
        SmsMo smsmo = new SmsMo();
        smsmo.setChanel("SMS");
        smsmo.setMessage(message);
        smsmo.setRecTime(d);
        smsmo.setReciever(msisdn);
        smsmo.setSender(destAdd);
        smsmo.setOperator("VMS");
        smsmo.setPaidStatus("");
        smsmo.setStatus("PRE");
        smsmo.setSmsID(Long.valueOf("0"));

        try {
            smsDAO.insert(smsmo);
            Dotransaction(message, msisdn, smsmo.getSmsID(), charging);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(SMSReciever.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "0";
    }

    /**
     * Hàm xử lý bản tin nhận
     *
     * @param message
     * @param msisdn
     * @param smsID
     * @param charging
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public String Dotransaction(String message, String msisdn, long smsID, Charging charging) throws ClassNotFoundException, SQLException {
        String rsp = "";
        //khởi tạo giá trị mặc định cho tin nhắn nếu không tồn tại
        if ("".equals(message) || message == null) {
            System.out.println("Message = null");
            message = message + "";
        }

        //thay nhiều khoảng trắng = 1 khoảng trắng
        message = message.replaceAll("\\s+", " ");
        String[] action = message.trim().toUpperCase().split("\\s+");
        SMSProcess exe = new SMSProcess();
        //lấy config bật chức năng hủy gói
        String enable_unregister = getValue("enable_unregister");
        String enable_confirm_register = getValue("enable_confirm_register");
        //Xử lý logic
        try {
            /*
             action[0]: tên cú pháp
             */
            switch (action[0]) {
                /*
                 Cú pháp đăng ký
                 */
                case "DK":
                    //do
                    if (action.length >= 2) {
                        if (action[1] == null || "".equals(action[1])) {
                            action[1] = "";
                        }
                        if ("1".equals(enable_confirm_register)) {
                            exe.QueueConfirmRegister(msisdn, action[1], 0);
                        } else {
                            exe.register(msisdn, action[1], smsID, "SMS", null);
                        }

                    } else {
                        if ("1".equals(enable_confirm_register)) {
                            exe.QueueConfirmRegister(msisdn, "D1", 0);
                        } else {
                            exe.register(msisdn, "D1", smsID, "SMS", null);
                        }

                    }
                    break;

                /*
                 Cú pháp hủy    
                 */
                case "HUY":
                    if (enable_unregister != null && enable_unregister.equals("1")) {
                        if (action.length >= 2) {
                            if (action[1] == null || "".equals(action[1])) {
                                String msg = getSms("msg_wrong");
                                exe.sendSMS(msisdn, "9193", msg, (long) smsID);
                            } else {
                                exe.QueueCancel(msisdn, action[1], (long) smsID);
                            }

                        } else {
                            String msg = getSms("msg_wrong");
                            exe.sendSMS(msisdn, "9193", msg, (long) smsID);
                        }
                    }
                    break;

                //xac nhận hủy gói cước
                case "YES":
                    exe.UnregisterAccepted(msisdn, "", smsID);
                    break;
                //xac nhan dang ky goi cuoc
                case "Y":

                    String pkag = "D1";
                    if ("".equals(action[1]) || action[1] == null) {
                        pkag = "";
                    }
                    pkag = action[1];
                    exe.RegisterAccepted(msisdn, pkag, smsID, "SMS", null);
                    break;
                /*
                 Cú pháp hướng dẫn   
                 */
                case "HD":
                    exe.guideUser(msisdn, (long) 0);
                    break;

                //cú pháp kiểm tra thông tin thuê bao
                case "KT":
                    exe.checkProfile(msisdn, (long) 0, "SMS");
                    break;

                //Cú pháp tải ứng dụng
                case "TAI":
                    exe.tai(msisdn, (long) smsID);
                    break;
                /*
                    Cú pháp bổ sung ngày 01/02/2016
                    - MA: nhận mã qua tin nhắn, nếu chưa đăng ký thì đăng ký D1
                    - UD: Đăng ký gói D1
                    - KM: Đăng ký gới D1
                    - UD7,KM7: Đăng ký gói D7
                    - UD30,KM30: Đăng ký gới D30
                    - UD90,KM90: Đăng ký gói D90
                    - Y: Xác nhận hủy gói
                 */
                //nhan ma giam gia
                case "MA":
                    boolean dkOk = exe.checkStatusAndRegister(msisdn, smsID, "SYS", charging);
                    if (dkOk == true) {
                        exe.getMcard(msisdn, smsID);
                    }

                    break;

                //dang ky D1
                case "UD":
                case "KM":
                    if ("1".equals(enable_confirm_register)) {
                        exe.QueueConfirmRegister(msisdn,"D1", 0);
                    } else {
                        exe.register(msisdn, "D1", smsID, "SMS", null);
                    }
                    break;

                //Dang ky D7
                case "UD7":
                case "KM7":
                    if ("1".equals(enable_confirm_register)) {
                        exe.QueueConfirmRegister(msisdn,"D7", 0);
                    } else {
                        exe.register(msisdn, "D7", smsID, "SMS", null);
                    }
                    break;

                //Dang ky D30
                case "UD30":
                case "KM30":
                    if ("1".equals(enable_confirm_register)) {
                        exe.QueueConfirmRegister(msisdn,"D30", 0);
                    } else {
                        exe.register(msisdn, "D30", smsID, "SMS", null);
                    }
                    break;

                //Dang ky D90
                case "UD90":
                case "KM90":
                    if ("1".equals(enable_confirm_register)) {
                        exe.QueueConfirmRegister(msisdn,"D90", 0);
                    } else {
                        exe.register(msisdn, "D90", smsID, "SMS", null);
                    }
                    break;

                //trường hợp nhắn tin sai cú pháp
                default:
                    //khong co noi dung
                    if (action[0] == null || "".equals(action[0])) {
                        System.out.println("Cu phap khong dung");
                        String msg = getSms("msg_wrong");
                        exe.sendSMS(msisdn, "9193", msg, (long) smsID);
                    } //có nội dung
                    else {
                        //nếu tin nhắn là tên gói cước, lấy thông tin gói cước
                        vn.ctnet.entity.Package pkg = packageDAO.getPackageByID(action[0].trim().toUpperCase());

                        //nếu gói cước tồn tại, đăng ký gói cước tương ứng
                        if (pkg != null && pkg.getPackageID() != null) {
                            if ("1".equals(enable_confirm_register)) {
                                exe.QueueConfirmRegister(msisdn, pkg.getPackageID(), smsID);
                            } else {
                                exe.register(msisdn, pkg.getPackageID(), smsID, "SMS", null);
                            }
                        } //nếu gói cước không tồn tại, xử lý chuỗi thông minh
                        else {
                            String text = action[0].toUpperCase();
                            text = text.toUpperCase();
                            //Nếu tin nhắn gửi lên bắt đầu bằng DK
                            if (text.startsWith("DK")) {
                                text = text.substring(2);
                                String pk = text.substring(0, 3);
                                //3 ký tự tiếp theo là tên gói cước D30, hoặc D90 thì đăng ký gói cước
                                if (pk.equals("D30") || pk.equals("D90")) {
                                    System.out.println("Dang ky goi " + pk);
                                    if ("1".equals(enable_confirm_register)) {
                                        exe.QueueConfirmRegister(msisdn, pk, smsID);
                                    } else {
                                        exe.register(msisdn, pk, smsID, "SMS", null);
                                    }

                                } //nếu không phải
                                else {
                                    pk = text.substring(0, 2);
                                    //nếu 2 ký tự tiếp theo của chuỗi là tên gói cước D1 hoặc D7 thì đăng ký gói cước
                                    if (pk.equals("D1") || pk.equals("D7")) {
                                        System.out.println("Dang ky goi " + pk);
                                        if ("1".equals(enable_confirm_register)) {
                                            exe.QueueConfirmRegister(msisdn, pk, smsID);
                                        } else {
                                            exe.register(msisdn, pk, smsID, "SMS", null);
                                        }
                                    } else {
                                        if ("1".equals(enable_confirm_register)) {
                                            exe.QueueConfirmRegister(msisdn, "D1", smsID);
                                        } else {
                                            exe.register(msisdn, "D1", smsID, "SMS", null);
                                        }
                                    }
                                }
                            } //Nếu tin nhắn bắt đầu bằng D tên gói cước
                            else if (text.startsWith("D")) {
                                text = text.substring(1);
                                String pk = text.substring(0, 3);
                                //nếu 3 ký tự đầu tiên của chuỗi là D30 hoặc D90 thì đăng ký gói cước
                                if (pk.equals("D30") || pk.equals("D90")) {
                                    System.out.println("Dang ky goi " + pk);
                                    if ("1".equals(enable_confirm_register)) {
                                        exe.QueueConfirmRegister(msisdn, pk, smsID);
                                    } else {
                                        exe.register(msisdn, pk, smsID, "SMS", null);
                                    }
                                } //nếu không phải
                                else {
                                    pk = text.substring(0, 2);
                                    //nếu 2 ký tự đầu tiên của tin nhắn gởi lên là D1, hoặc D7 thì đăng ký gói cước
                                    if (pk.equals("D1") || pk.equals("D7")) {
                                        System.out.println("Dang ky goi " + pk);
                                        if ("1".equals(enable_confirm_register)) {
                                            exe.QueueConfirmRegister(msisdn, pk, smsID);
                                        } else {
                                            exe.register(msisdn, pk, smsID, "SMS", null);
                                        }
                                    } //nếu không thì đăng ký D1
                                    else {
                                        if ("1".equals(enable_confirm_register)) {
                                            exe.QueueConfirmRegister(msisdn, "D1", smsID);
                                        } else {
                                            exe.register(msisdn, "D1", smsID, "SMS", null);
                                        }
                                    }
                                }
                            } //nếu tin nhắn không bắt đầu bằng DK hoặc D thì thông báo sai cú pháp
                            else {
                                System.out.println("Cu phap khong dung");
                                String msg = getSms("msg_wrong");
                                exe.sendSMS(msisdn, "9193", msg, (long) smsID);
                            }
                        }
                    }
                    break;
            }
        } //Nếu xảy ra lỗi thì thông báo sai cú pháp
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            String msg = getSms("msg_wrong");
            exe.sendSMS(msisdn, "9193", msg, (long) smsID);
        }
        return "";
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
}
