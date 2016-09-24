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
    private final String systemType = getValue("smpp_system_type");
    public static SMPPSession session = null;
    Thread th;
    String threadName;

    /*
     Hàm dựng
     */
    public RewriteSentMessage() {
    }

    /**
     * Hàm chạy thread gởi và nhận tin nhắn từ SMPP Gateway
     */
    @Override
    public void run() {
         BasicConfigurator.configure();
        //Nếu như session chưa tồn tại thì tạo mới session smpp
        if (session == null) {
            session = new SMPPSession();
        }
        //nếu session đã tồn tại nhưng chưa bind thông tin kết nối thì bind thông tin kết nối
        System.out.println(session.getSessionState().toString());
        if (session != null && !session.getSessionState().isBound()) {
            /*
             Bind thông tin kết nối
             */
            try {

                 session.connectAndBind(this.server, this.port, new BindParameter(BindType.BIND_TX, this.user, this.pass, this.systemType, TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, this.addressRange), 3000L);

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
                            ), 10000);
                    System.out.println("Connect SMPP Server success ...!");
                } catch (IOException ex) {
                    System.out.println("Error: " + ex.getMessage() + ex.getStackTrace());
                }
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
