/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jsmpp.ctnet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;
import static org.jsmpp.ctnet.RewriteSentMessage.session;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;

/**
 *
 * @author dieup
 */
public class TestBindConnect extends Thread{
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
    @Override
    public void run() {
         //Nếu như session chưa tồn tại thì tạo mới session smpp
                System.out.println(server);
                System.out.println(port);
                System.out.println(user);
                System.out.println(pass);
                System.out.println(systemType);
                System.out.println(addressRange);
                
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
                                        "",
                                        TypeOfNumber.UNKNOWN,
                                        NumberingPlanIndicator.UNKNOWN,
                                        null
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
                                            "",
                                            TypeOfNumber.UNKNOWN,
                                            NumberingPlanIndicator.UNKNOWN,
                                            null
                                    ), 3000);
                            System.out.println("Connect SMPP Server success ...!");
                        } catch (IOException ex) {
                            System.out.println("Error: " + ex.getMessage() + ex.getStackTrace());
                        }
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
}
