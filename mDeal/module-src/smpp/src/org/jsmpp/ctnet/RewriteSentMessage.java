/*
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.jsmpp.ctnet;

import charging.Charging;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.BasicConfigurator;
import org.jsmpp.bean.AlertNotification;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.DataSm;
import org.jsmpp.bean.DeliverSm;
import org.jsmpp.bean.DeliveryReceipt;
import org.jsmpp.bean.MessageType;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.Session;
import org.jsmpp.util.InvalidDeliveryReceiptException;
import vn.ctnet.dao.SmsMtDAO;
import vn.ctnet.entity.SmsMt;

/**
 * @author uudashr
 *
 */
public class RewriteSentMessage extends Thread {

    /*
    Lấy giá trị config cấu hình kết nối smpp server
    */
    private final String server = getValue("smpp_server");
    private final int port = Integer.parseInt(getValue("smpp_port"));
    private final String user = getValue("smpp_user");
    private final String pass = getValue("smpp_pass");
    private final String addressRange = getValue("smpp_addressRange");
    public static SMPPSession session = null;
    private final SmsMtDAO smsMtCtrl = new SmsMtDAO();
    private final Charging charging = new Charging();
    Thread th;
    String threadName;
    
    /*
    Hàm dựng
    */
    public RewriteSentMessage(String name) {
        threadName = name;
    }
    /**
     * Hàm chạy thread gởi và nhận tin nhắn từ SMPP Gateway
     */
    @Override
    public void run() {
        /*
        Nếu khởi tạo đối tượng với tên là SENT thì chạy thread gởi tin nhắn
        */
        if ("SENT".equals(threadName)) 
        {
            while (true) {
                /*
                Lấy danh sách tin nhắn chưa gửi từ CSDL
                Mỗi lần sẽ lấy ra 10 bản tin từ hàng đợi
                */
                List<SmsMt> list = null;
                try {
                    list = smsMtCtrl.getSmsByStatus("PRE");
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
                        try 
                        {
                            
                            session.connectAndBind(
                                    server,
                                    port,
                                    new BindParameter(
                                            BindType.BIND_TRX,
                                            user,
                                            pass,
                                            "sp",
                                            TypeOfNumber.UNKNOWN,
                                            NumberingPlanIndicator.UNKNOWN,
                                            addressRange
                                    ), 3000);

                        }
                        /*
                        Nếu bind không thành công, sẽ bind lại
                        */
                        catch (IOException e) 
                        {
                            System.err.println("Failed connect and bind to host");
                            System.out.println("Timeout try again");
                            try {
                                session = new SMPPSession();
                                session.connectAndBind(
                                        server,
                                        port,
                                        new BindParameter(
                                                BindType.BIND_TRX,
                                                user,
                                                pass,
                                                "sp",
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
                    for (SmsMt list1 : list) {
                        /*
                        Gởi tin nhắn và cập nhật trạng thái
                        */
                        try {
                            //gởi tin nhắn
                            ThreadSubmitSMS sendSMS = new ThreadSubmitSMS(session, list1.getMessage(), initPhoneNumber(list1.getReciever(), 1), list1.getSender());
                            sendSMS.start();
                            
                            //cập nhật trạng thái tin nhắn
                            list1.setSentStatus("SENT");
                            smsMtCtrl.update(list1);
                           
                        } catch (Exception ex) {
                            System.out.println("Error: " + ex.getMessage() + ex.getStackTrace());
                        }
                    }
                    
                }
                
                //dừng 1 giây sau mỗi 10 tin nhắn gởi đi
                try 
                {
                    Thread.sleep(1000);
                }
                catch(Exception ex) 
                {
                
                }
            }

        } 
        /*
        Nếu khởi tạo đối tượng với tên khác SENT thì chạy thread nhận tin nhắn
        */
        else 
        {
            while (true) {
                //Khởi tạo thông tin sự kiện lắng nghe tin nhắn đến
                final AtomicInteger counter = new AtomicInteger();
                
                //nếu session smpp chưa có thì khởi tạo
                if (session == null) 
                {
                    session = new SMPPSession();
                }
                
                //nếu session đã có nhưng chưa bind thì bind thông tin kết nối
                if (session != null && !session.getSessionState().isBound()) 
                {
                    BasicConfigurator.configure();
                    /*
                    Bind thông tin
                    */
                    try 
                    {
                        session.connectAndBind(
                                server,
                                port,
                                new BindParameter(
                                        BindType.BIND_TRX,
                                        user,
                                        pass,
                                        "sp",
                                        TypeOfNumber.UNKNOWN,
                                        NumberingPlanIndicator.UNKNOWN,
                                        addressRange
                                ), 3000);

                    }
                    /*
                    Bind không thành công, bind lại
                    */
                    catch (IOException e) 
                    {
                        System.err.println("Failed connect and bind to host");
                        System.out.println("Timeout try again:");
                        try {
                            session = new SMPPSession();
                            session.connectAndBind(
                                    server,
                                    port,
                                    new BindParameter(
                                            BindType.BIND_TRX,
                                            user,
                                            pass,
                                            "sp",
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
                //nếu bind thành công thì khởi tạo sự kiện lắng nghe tin nhắn đến
                if (session != null) 
                {
                    /*
                    Sự kiện lắng nghe tin nhắn đến
                    */
                    session.setMessageReceiverListener(new MessageReceiverListener() 
                    {
                        @Override
                        public void onAcceptDeliverSm(DeliverSm deliverSm) 
                        {
                            /*
                            Bản tin đến là bàn tin báo cáo gởi từ nhà mạng thì không thực hiện
                            */
                            if (MessageType.SMSC_DEL_RECEIPT.containedIn(deliverSm.getEsmClass())) 
                            {
                                counter.incrementAndGet();
                                // delivery receipt
                                try {
                                    DeliveryReceipt delReceipt = deliverSm.getShortMessageAsDeliveryReceipt();
                                    long id = Long.parseLong(delReceipt.getId()) & 0xffffffff;
                                    String messageId = Long.toString(id, 16).toUpperCase();
                                    System.out.println("Receiving delivery receipt for message '" + messageId + "' : " + delReceipt);
                                } catch (InvalidDeliveryReceiptException e) {
                                    System.err.println("Failed getting delivery receipt");
                                }
                            }
                            /*
                            Bản tin đến là short message từ người dùng thì xử lý
                            */
                            else 
                            {
                                try {
                                    /*
                                    Nếu tin nhắn đến là tin rỗng
                                    */
                                    if (deliverSm.getShortMessage() == null) 
                                    {
                                        //nhận tin nhắn và xử lý
                                        ThreatReceiverSMS RecSMS = new ThreatReceiverSMS("", deliverSm.getSourceAddr(), deliverSm.getDestAddress(),charging);
                                        RecSMS.start();
                                    } 
                                    //nếu tin nhắn đến có nội dung
                                    else 
                                    {
                                        //nhận tin nhắn và xử lý
                                        ThreatReceiverSMS RecSMS = new ThreatReceiverSMS(new String(deliverSm.getShortMessage()), deliverSm.getSourceAddr(), deliverSm.getDestAddress(),charging);
                                        RecSMS.start();
                                    }
                                } catch (Exception ex) {
                                    System.out.println("Error: " + ex.getMessage() + ex.getStackTrace());
                                }
                            }
                        }

                        @Override
                        public DataSmResult onAcceptDataSm(DataSm dataSm, Session source)
                                throws ProcessRequestException {
                            // TODO Auto-generated method stub
                            return null;
                        }

                        @Override
                        public void onAcceptAlertNotification(
                                AlertNotification alertNotification) {
                        }
                    });
                }

            }
        }

    }
    
    /**
     * Hàm khởi tạo số đt tương thích với SMPP gateway
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
            return new String(prop.getProperty(name).getBytes("UTF-8"));

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
     * @param len
     * @return 
     */
    public int countSegment(int len) {

        return (int) Math.ceil((double) len / (double) 150);
    }
}
