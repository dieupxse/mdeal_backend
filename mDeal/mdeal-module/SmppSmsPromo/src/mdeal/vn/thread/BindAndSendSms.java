/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdeal.vn.thread;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.BasicConfigurator;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import sms.mdeal.accesser.SmsSendRequestAccesser;
import sms.mdeal.entity.SmsSendRequestEntity;

/**
 *
 * @author dieup
 */
public class BindAndSendSms extends Thread{
    /*
     Lấy giá trị config cấu hình kết nối smpp server
     */
    private final String server = getValue("smpp_server");
    private final int port = Integer.parseInt(getValue("smpp_port"));
    private final String user = getValue("smpp_user");
    private final String pass = getValue("smpp_pass");
    private final String systemType = getValue("smpp_system_type");
    private final String addressRange = getValue("smpp_addressRange");
    public static SMPPSession session = null;
    private final SmsSendRequestAccesser smsMtCtrl = new SmsSendRequestAccesser();
    Thread th;
    String threadName;

    /*
     Hàm dựng
     */
    public BindAndSendSms() {
    }
    

    /**
     * Hàm chạy thread gởi và nhận tin nhắn từ SMPP Gateway
     */
    @Override
    public void run() {
        while (true) {
            /*
             Lấy danh sách tin nhắn chưa gửi từ CSDL
             Mỗi lần sẽ lấy ra 10 bản tin từ hàng đợi
             */
            List<SmsSendRequestEntity> list = null;
            try {
                list = smsMtCtrl.getSmsToSent();
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage() + ex.getStackTrace());
            }

            /*
             Duyệt danh sách tin nhắn chưa gửi và tiền hành gửi
             */
            if (list != null && list.size() > 0) {
                System.out.println("sending messages");
                /*
                 Cấu hình binding SMPP gateway
                 */
                BasicConfigurator.configure();

                //Nếu như session chưa tồn tại thì tạo mới session smpp
                if (session == null) {
                    session = new SMPPSession();
                }
                //nếu session đã tồn tại nhưng chưa bind thông tin kết nối thì bind thông tin kết nối
                if (session != null && !session.getSessionState().isBound()) {
                    /*
                     Bind thông tin kết nối
                     */
                    try {
                        
                        session.connectAndBind(
                                server,
                                port,
                                new BindParameter(
                                        BindType.BIND_TX,
                                        user,
                                        pass,
                                        systemType,
                                        TypeOfNumber.UNKNOWN,
                                        NumberingPlanIndicator.UNKNOWN,
                                        addressRange
                                ), 3000);
                        System.out.println("Bind connect success !");
                    } /*
                     Nếu bind không thành công, sẽ bind lại
                     */ catch (IOException e) {
                        System.err.println("Failed connect and bind to host");
                        System.out.println("Timeout try again");
                        try {
                            session = new SMPPSession();
                            session.connectAndBind(
                                    server,
                                    port,
                                    new BindParameter(
                                            BindType.BIND_TX,
                                            user,
                                            pass,
                                            systemType,
                                            TypeOfNumber.UNKNOWN,
                                            NumberingPlanIndicator.UNKNOWN,
                                            addressRange
                                    ), 3000);
                            System.out.println("Connect SMPP Server success ...!");
                        } catch (IOException ex) {
                            System.out.println("Error: " + ex.getMessage() + ex.getStackTrace());
                        }
                    }
                }

                /*
                 Duyệt list gởi tin nhắn đi
                 */
                for (SmsSendRequestEntity list1 : list) {
                    /*
                     Gởi tin nhắn và cập nhật trạng thái
                     */
                    try {
                        //gởi tin nhắn
                        SubmitMessage sendSMS = new SubmitMessage(session, list1.getMessage(), initPhoneNumber(list1.getReciever(), 1), list1.getSender());
                        sendSMS.start();

                        //cập nhật trạng thái tin nhắn
                        list1.setIsProcessed(true);
                        smsMtCtrl.update(list1);
                        
                    } catch (Exception ex) {
                        System.out.println("Error: " + ex.getMessage() + ex.getStackTrace());
                    }
                }
                
            }

            //dừng 1 giây sau mỗi 10 tin nhắn gởi đi
            try {
                Thread.sleep(1000);
            } catch (Exception ex) {
                
            }
        }
        
    }

    /**
     * Hàm khởi tạo số đt tương thích với SMPP gateway
     *
     * @param phone
     * @param type
     * @return
     */
    public String initPhoneNumber(String phone, int type) {
        //using for smpp
        if (type == 1) {
            if (phone.startsWith("0")) {
                return phone.replaceFirst("0", "84");
            } else if (phone.startsWith("84")) {
                return phone;
            } else if (phone.startsWith("+84")) {
                return phone.replace("+", "");
            } else {
                return phone;
            }
        } //using for chargin proxy
        else {
            if (phone.startsWith("0")) {
                return phone.replaceFirst("0", "");
            } else if (phone.startsWith("84")) {
                return phone.replaceFirst("84", "");
            } else if (phone.startsWith("+84")) {
                return phone.replace("+84", "");
            } else {
                return phone;
            }
        }
    }

    /**
     * Hàm lấy giá trị từ file config
     *
     * @param name
     * @return
     */
    public String getValue(String name) {
        Properties prop = new Properties();
        InputStream input = null;
        
        try {
            String path = "C:\\mdeal_config\\smssendrequest.properties";
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
     * Hàm lấy nội dung tin nhắn theo segment với bản tin dài
     *
     * @param msg
     * @param seg
     * @return
     */
    public String getMessageWithSegment(String msg, int seg) {
        
        String rs = "";
        int partNum = (int) Math.ceil((double) msg.length() / (double) 150);
        for (int i = 1; i <= partNum; i++) {
            if (i == seg) {
                rs = msg.substring((seg - 1) * 150,
                        (seg * 150 > msg.length() ? msg.length() : seg * 150));
            }
        }
        return rs;
    }

    /**
     * Hàm đếm segment với chiều dài một bản tin
     *
     * @param len
     * @return
     */
    public int countSegment(int len) {
        
        return (int) Math.ceil((double) len / (double) 150);
    }
}
