/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package charging;

/**
 *
 * @author jacob
 */
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdiameter.api.*;
import org.jdiameter.client.impl.helpers.EmptyConfiguration;
import org.jdiameter.server.impl.helpers.Parameters;
import static org.jdiameter.client.impl.helpers.EmptyConfiguration.getInstance;
import vn.ctnet.dao.CdrLogDAO;
import vn.ctnet.entity.CdrLog;

/**
 * <p>
 * Title: IN Gateway TEST</p>
 *
 * <p>
 * Description: IN Gateway TEST</p>
 *
 * <p>
 * Copyright: Copyright (c) 2014</p>
 *
 * <p>
 * Company: lemon</p>
 *
 * @author tienduc
 * @version 1.0
 */
public class Charging extends Thread {

    private ClientConfiguration clientConfig;
    private Stack stack;
    private SessionFactory factory;
    private final String ownProductName = Utilities.getValue("ownProductName");
    private final String ownRealm = Utilities.getValue("ownRealm");
    private final int clientPort = Integer.parseInt(Utilities.getValue("clientPort"));
    private final String ownUri = "aaa://" + Utilities.getValue("IP_LOCAL") + ":" + clientPort;
    private final long ownVendorId = Long.parseLong(Utilities.getValue("ownVendorId"));
    private final long vendorId = Long.parseLong(Utilities.getValue("vendorId"));
    private final long authApplId = Long.parseLong(Utilities.getValue("authApplId"));
    private final long acctApplId = Long.parseLong(Utilities.getValue("acctApplId"));;
    private final String serverHost = Utilities.getValue("serverHost");
    private final int serverPort = Integer.parseInt(Utilities.getValue("serverPort"));
    private final String serverRealm = Utilities.getValue("serverRealm");
    private static boolean bLogin = false;
    private static boolean isReady, isConnected;
    private int iThread;
    public String sessionId;
    private CdrLogDAO LogDAO = new CdrLogDAO();
    public Charging() {
        try {
            openConnection(30000);
        } catch (Exception ex) {
            Logger.getLogger(Charging.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Charging(int idx) {
        this.iThread = idx;
    }

    public boolean isReady() {
        return isReady;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public class ClientConfiguration extends EmptyConfiguration {

        public ClientConfiguration() throws Exception {
            super();
            add(Parameters.Assembler, Parameters.Assembler.defValue());
            add(Parameters.OwnDiameterURI, ownUri);
            add(Parameters.OwnProductName, ownProductName);
            add(Parameters.OwnIPAddress, Utilities.IP_LOCAL);
            add(Parameters.OwnVendorID, ownVendorId);
            add(Parameters.ApplicationId,
                    getInstance().add(Parameters.VendorId, vendorId).
                    add(Parameters.AcctApplId, acctApplId));

            add(Parameters.PeerTable,
                    getInstance().add(Parameters.PeerRating,
                            1).add(Parameters.PeerName,
                            "aaa://" + serverHost + ":" + serverPort));
            add(Parameters.RealmTable,
                    getInstance().add(Parameters.RealmEntry,
                            serverRealm + ":" + serverPort));
        }
    }

    @Override
    public void run() {
        int time = Utilities.NUM_CHARGE_SESSION * 60000;//x mins
        int timeout = 30000; //30s theo document
        int sleep = iThread * 60000;
        String info;
        while (true) {
            try {
                if (!isConnected) {
                    System.out.println("<====Connect to Diameter Charging Proxy " + serverHost + " by " + Utilities.CHARGE_USER + "/" + Utilities.CHARGE_PASSWORD);
                    sessionId = openConnection(timeout); //use document
                    if (bLogin & !sessionId.equals("")) {
                        info = sessionId + ":" + DateProc.getYYYYMMDDHHMISS(new Timestamp(System.currentTimeMillis())) + "\n";
                        System.out.println("Successfully login to Charging Proxy, session_id=" + sessionId);
                        logSession(info);
                        Thread.sleep(sleep);
                    }
                }
                if (!isConnected && !bLogin) {
                    System.out.println("Connect to proxy failed!!!. Try later 10 secconds");
                    Thread.sleep(10000);
                } else {
                    Thread.sleep(time);
                }
            } catch (Exception e) {
                e.printStackTrace();
                //MonitorServer.logMonitor("CHARGE", "ChargeConnection.run::Connect to " + serverHost + " failed:" + e.getMessage(), MonitorServer.CDR_ERROR);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Charging.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    /**
     * Open stack
     *
     * @param timeout
     * @return 
     * @throws Exception
     */
    public String openConnection(long timeout) throws Exception {
        try {
            clientConfig = new ClientConfiguration();
            //start stack
            stack = new org.jdiameter.client.impl.StackImpl();
            this.factory = stack.init(clientConfig);
            MetaData metaData = stack.getMetaData();

            if (metaData.getStackType() != StackType.TYPE_CLIENT
                    || metaData.getMinorVersion() <= 0) {
                throw new Exception("Incorrect driver");
            }
            System.out.println(">>>>> Stack starting......");
            stack.start(Mode.ALL_PEERS, timeout, TimeUnit.MILLISECONDS); //30s theo document
            isReady = true;
            sessionId = login(3);
            if (sessionId == null || sessionId.equals("")) {
                System.out.println(">>>>>>>>>> Login fail (Check connection or parameter config)");
                isConnected = false;
                bLogin = false;
                return "";
            } else {
                isConnected = true;
                bLogin = true;
                System.out.println(">>>>> Stack is running... " + isConnected);
                return sessionId;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Close connection
     */
    public void closeConnection() {
        try {
            if (stack != null) {
                try {
                    if (bLogin) {
                        logout();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                stack.stop(30000, TimeUnit.MILLISECONDS); //30s theo document
                stack.destroy();
                System.out.println(">>>>> Stack stop success.");
                System.out.println("Logout success!!!");
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Log session
     *
     * @param info
     * @throws Exception
     */
    private synchronized void logSession(String info) throws Exception {
        try {
            FileOutputStream fos = new FileOutputStream("./sessions.txt", true);
            fos.write(info.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Login connection
     *
     * @return boolean
     * @throws Exception
     */
    public String login(int i) throws Exception {

        Session session = factory.getNewSession();
        try {
            Request requestLogin = session.createRequest(265,
                    org.jdiameter.api.ApplicationId.createByAuthAppId(vendorId,
                            authApplId),
                    serverRealm, serverHost);
            requestLogin.getAvps().addAvp(258, 4L, true, false, true);
            requestLogin.getAvps().addAvp(264, Utilities.IP_LOCAL, true, false, true);
            requestLogin.getAvps().addAvp(296, this.ownRealm, true, false, true);

            requestLogin.getAvps().addAvp(293, serverHost, true, false, true);
            requestLogin.getAvps().addAvp(283, serverRealm, true, false, true);
            requestLogin.getAvps().addAvp(274, i, true);

            requestLogin.getAvps().addAvp(1, Utilities.CHARGE_USER, true, false, false);
            requestLogin.getAvps().addAvp(2, Utilities.CHARGE_PASSWORD, true, false, true);

            Future<Message> future = session.send(requestLogin);
            Answer response = (Answer) future.get();
            String resultCode = response.getAvps().getAvp(1021).getUTF8String();
            //String description = response.getAvps().getAvp(1011).getUTF8String();
            System.out.println("Result-Code-login:" + resultCode);
            //System.out.println("Description-Result-Code-login:" + description);
            String sess = "";
            if (resultCode.equals("CPS-0000")) {
                System.out.println(Utilities.CHARGE_USER + ":" + Utilities.CHARGE_PASSWORD + " >>>>>>Login success");
                Avp avpSesionId = response.getAvps().getAvp(263);
                sess = avpSesionId.getUTF8String();
                String s[] = sess.split(";");
                System.out.println(sess);
                String ss = "";
                if (s.length > 1) {
                    ss = s[2];
                }
                return ss;
            } else {
                System.out.println(">>>>>Login result fail: " + resultCode);
                isConnected = false;
                bLogin = false;
                return sess;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            session.release();
        }
    }

    /**
     * Logout connection
     *
     * @return
     * @throws Exception
     */
    public boolean logout() throws Exception {
        Session session = factory.getNewSession();
        try {
            Request requestLogout = session.createRequest(274,
                    org.jdiameter.api.ApplicationId.createByAccAppId(vendorId,
                            authApplId),
                    serverRealm, serverHost);

            requestLogout.getAvps().addAvp(264, Utilities.IP_LOCAL, true, false, true);
            requestLogout.getAvps().addAvp(296, ownRealm, true, false, true);
            requestLogout.getAvps().addAvp(293, serverHost, true, false, true);
            requestLogout.getAvps().addAvp(283, serverRealm, true, false, true);
            Future<Message> future = session.send(requestLogout);
            Answer response = (Answer) future.get();
            Avp avpResultCode = response.getAvps().getAvp(1021);
            String resultCode = avpResultCode.getUTF8String();
            //String description = response.getAvps().getAvp(1011).getUTF8String();
            System.out.println(resultCode + ":" + resultCode);
            if (resultCode.equals("CPS-0000")) {
                return true;
            } else {
                return false;
            }
        } finally {
            session.release();
        }
    }

    /**
     * keepalive DWR Device-Watchdog-Request. Sau 15p ko goi charge --> keepalive
     */
    public boolean DiviceWatchdog() throws Exception {
        Session session = factory.getNewSession();
        try {
            Request requestDiviceWatchdog = session.createRequest(280,
                    org.jdiameter.api.ApplicationId.createByAuthAppId(vendorId,
                            authApplId),
                    serverRealm, serverHost);

            requestDiviceWatchdog.getAvps().addAvp(264, Utilities.IP_LOCAL, true, false, true);
            requestDiviceWatchdog.getAvps().addAvp(296, this.ownRealm, true, false, true);
            requestDiviceWatchdog.getAvps().addAvp(283, serverRealm, true, false, true);
            requestDiviceWatchdog.getAvps().addAvp(293, serverHost, true, false, true);
            // oringin-state_id
            requestDiviceWatchdog.getAvps().addAvp(278, (int) System.currentTimeMillis(), true, false, true);
            // requestDiviceWatchdog.getAvps().addAvp(20000, Utilities.IP_LOCAL, true, false, false);
            // requestDiviceWatchdog.getAvps().addAvp(20001, sessionId, true, false, false);
            Future<Message> future = session.send(requestDiviceWatchdog);
            Answer response = (Answer) future.get();
            String resultCode = response.getAvps().getAvp(1021).getUTF8String();
            String description = response.getAvps().getAvp(1011).getUTF8String();
            System.out.println(resultCode + " " + description);
            System.out.println("Result-Code-Debit:" + resultCode);
            System.out.println("Description-Result-Code-Debit:" + description);
            if (resultCode.equals("CPS-0000")) {
                return true;
            } else {
                System.out.println(" result DiviceWatchdog fail: " + resultCode);
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            session.release();
        }

    }

    /**
     * Debit Tru tien truc tiep vao tai khoan
     *
     * @param isdn
     * @param amount
     * @throws Exception
     */
    public String debit(String isdn, long amount, String serviceCode, String bsisdn, String contentId, String categoryId, int requestAction, String extra_info) throws Exception {
        System.out.println(">>>Charging UserId:" + isdn + ":Amount:" + amount + ":ServiceCode:" + serviceCode + ":ShortCodeCharging:" + Utilities.CHARGING_PROXY_SHORT_CODE);
        isdn = Utilities.formatUserId(isdn);
        CdrLog log = new CdrLog();
        log.setSessionLogin(sessionId);
        log.setIsdn(isdn);
        log.setBsisdn(bsisdn);
        log.setCategoryId(categoryId);
        log.setContentId(contentId);
        log.setAmount(amount+"");
        log.setServiceCode(serviceCode);
        log.setRequestAction(requestAction);
        log.setIsRecharge(false);
//        String sessionrq = Utilities.CHARGE_USER + "." + serviceCode + "." + sessionId;
        String sessionrq = Utilities.CHARGE_USER + "." + Utilities.CHARGE_USER + "." + sessionId;
        log.setSessionRequest(sessionrq);
        System.out.println("Session Request String: "+sessionrq);
        Session session = factory.getNewSession(sessionrq);
        String resultCode = "";
        try {

            Request request = session.createRequest(272,
                    org.jdiameter.api.ApplicationId.createByAuthAppId(vendorId,
                            authApplId),
                    serverRealm, serverHost);

            AvpSet avpSet = request.getAvps();

            // System.out.println("session:" + sessionrq);
            avpSet.addAvp(263, sessionrq, true, false, false);
            avpSet.addAvp(264, Utilities.IP_LOCAL, true, false, true);
            avpSet.addAvp(296, this.ownRealm, true, false, true);

            avpSet.addAvp(293, serverHost, true, false, true);
            avpSet.addAvp(283, serverRealm, true, false, true);

            //add auth_application_id: default=4
            avpSet.addAvp(Avp.AUTH_APPLICATION_ID, 4L, true, false, true);

            //service context id: default=6.32260@3gpp.org
            //AvpCode.SERVICE_CONTEXT_ID: 461
            avpSet.addAvp(461, "6.32260@3gpp.org", true, false, false);
            //CC_REQUEST_TYPE: 416
            avpSet.addAvp(416, 4, true, false); // REQUEST_EVENT
            //CC_REQUEST_NUMBER: 415
            avpSet.addAvp(415, 0, true, false, true);
            //REQUESTED_ACTION: 436
            avpSet.addAvp(436, 0, true, false);

            //add isdn
            //SUBSCRIPTION_ID: 443
            AvpSet subId = request.getAvps().addGroupedAvp(443);
            //SUBSCRIPTION_ID_TYPE: 450
            subId.addAvp(450, 0, true, false);
            //SUBSCRIPTION_ID_DATA: 444
            System.out.println(">>>>>>>>>>>>do ma: " + isdn);
            subId.addAvp(444, isdn, true, false, false);

            //add amount
            //MULTIPLE_SERVICES_CREDIT_CONTROL
            AvpSet mscControl = avpSet.addGroupedAvp(456);

            //SERVICE_IDENTIFIER: 439
            mscControl.addAvp(439, 4, true, false);

            //REQUESTED_SERVICE_UNIT: 437
            AvpSet request_service_unit = mscControl.addGroupedAvp(437);
            AvpSet ccMoney = request_service_unit.addGroupedAvp(413); // CC-Money
            AvpSet ccMoney_Unit_Value = ccMoney.addGroupedAvp(445);
            ccMoney_Unit_Value.addAvp(447, amount, true, false);
            // Co the ve sau su dung
            //Bundle	10000	Grouped	VOM	Descript a bundle. For multiple bundles, repeate Bundle AVP. Used when debit or credit bundle.
            //Unit-Name	444	UTF8String	VOM	Bundle name (GPRS, VOICE,...)
            //Unit-Value	445 	Grouped	VOM	
            //Value-Digits	447	Unsigned	VOM	Bundle value

            //add avp of CPS: cp_id, sp_id,...
            /**
             * cpid	10111 spid	10112 categoryid	10113 contentid	10114 shortcode
             * 10115 b_isdn	10116
             */
            avpSet.addAvp(10111, Utilities.CPID, true, false, true);
            avpSet.addAvp(10112, Utilities.SP_ID, true, false, true);
            avpSet.addAvp(10113, categoryId, true, false, true);
            avpSet.addAvp(10114, contentId, true, false, true);
            avpSet.addAvp(10115, Utilities.CHARGING_PROXY_SHORT_CODE, true, false, true);
            avpSet.addAvp(10116, (null==extra_info || "".equals(extra_info)) ? bsisdn : extra_info, true, false, true);
            //avpSet.addAvp(10117,((null==extra_info || "".equals(extra_info)) ? "" : extra_info ),true, false, true);
//            avpSet.addAvp(20000, Utilities.IP_LOCAL, true, false, false);
//            avpSet.addAvp(20001, sessionId, true, false, false);

            Future<Message> future = session.send(request);
            Answer response = (Answer) future.get();

            resultCode = response.getAvps().getAvp(1021).getUTF8String();
            //String description = response.getAvps().getAvp(1011).getUTF8String();
            //System.out.println(resultCode + " " + description);
            System.out.println("[" + resultCode + "]");
            // System.out.println("Description-Result-Code-Debit:" + description);
            if (resultCode.equals("CPS-0000")) {
                log.setResult(resultCode);
                
                System.out.println(">>>OK!!!");
            } else {
                if (resultCode.equals("CPS-2011")) {
                    System.out.println("Session bi het han >>> login va Charge lai:");
                    login(3);
                    if (bLogin) {
                        resultCode = debitRecharge(isdn, amount, serviceCode, bsisdn, contentId, categoryId, requestAction);
                        if (resultCode.equals("CPS-0000")) {
                            System.out.println("Recharge OK!!!");
                        } else {
                            System.out.println("Recharge Fail!!!");
                        }
                    }
                } else {
                    System.out.println(">>>Charging Fail!!!");
                }
                log.setResult(resultCode);
            }
            LogDAO.insert(log);
            return resultCode;
        } catch (Exception e) {
            e.printStackTrace();
            closeConnection();
            try {
                logout();
            } catch (Exception exclose) {
            }
            openConnection(30000);
            if (bLogin) {
                resultCode = debitRecharge(isdn, amount, serviceCode, bsisdn, contentId, categoryId, requestAction);
                if (resultCode.equals("CPS-0000")) {
                    System.out.println("ReCharging OK!!!");
                } else {
                    System.out.println("ReCharging Fail!!!");
                }
                return resultCode;
            }

        } finally {
            session.release();
        }
        return resultCode;
    }
    
    public String debit(String isdn, long amount, String serviceCode, String bsisdn, String contentId, String categoryId, int requestAction) throws Exception {
        return debit(isdn, amount, serviceCode, bsisdn, contentId, categoryId, requestAction,"");
    }

    public String debitRecharge(String isdn, long amount, String serviceCode, String bsisdn, String contentId, String categoryId, int requestAction, String extra_info) throws Exception {
        CdrLog log = new CdrLog();
        log.setSessionLogin(sessionId);
        log.setIsdn(isdn);
        log.setBsisdn(bsisdn);
        log.setCategoryId(categoryId);
        log.setContentId(contentId);
        log.setAmount(amount+"");
        log.setServiceCode(serviceCode);
        log.setRequestAction(requestAction);
        log.setIsRecharge(true);
        System.out.println(">>>Recharging UserId:" + isdn + ":Amount:" + amount + ":ServiceCode:" + serviceCode);
        String sessionrq = Utilities.CHARGE_USER + "." + Utilities.CHARGE_USER + "." + sessionId;
        log.setSessionRequest(sessionrq);
        Session session = factory.getNewSession(sessionrq);
        try {

            Request request = session.createRequest(272,
                    org.jdiameter.api.ApplicationId.createByAuthAppId(vendorId,
                            authApplId),
                    serverRealm, serverHost);

            AvpSet avpSet = request.getAvps();
            
            avpSet.addAvp(263, sessionrq, true, false, false);
            avpSet.addAvp(264, Utilities.IP_LOCAL, true, false, true);
            avpSet.addAvp(296, this.ownRealm, true, false, true);

            avpSet.addAvp(293, serverHost, true, false, true);
            avpSet.addAvp(283, serverRealm, true, false, true);

            //add auth_application_id: default=4
            avpSet.addAvp(Avp.AUTH_APPLICATION_ID, 4L, true, false, true);

            //service context id: default=6.32260@3gpp.org
            //AvpCode.SERVICE_CONTEXT_ID: 461
            avpSet.addAvp(461, "6.32260@3gpp.org", true, false, false);
            //CC_REQUEST_TYPE: 416
            avpSet.addAvp(416, 4, true, false); // REQUEST_EVENT
            //CC_REQUEST_NUMBER: 415
            avpSet.addAvp(415, 0, true, false, true);
            //REQUESTED_ACTION: 436
            avpSet.addAvp(436, 0, true, false);

            //add isdn
            //SUBSCRIPTION_ID: 443
            AvpSet subId = request.getAvps().addGroupedAvp(443);
            //SUBSCRIPTION_ID_TYPE: 450
            subId.addAvp(450, 0, true, false);
            //SUBSCRIPTION_ID_DATA: 444
            subId.addAvp(444, isdn, true, false, false);

            //add amount
            //MULTIPLE_SERVICES_CREDIT_CONTROL
            AvpSet mscControl = avpSet.addGroupedAvp(456);

            //SERVICE_IDENTIFIER: 439
            mscControl.addAvp(439, 4, true, false);

            //REQUESTED_SERVICE_UNIT: 437
            AvpSet request_service_unit = mscControl.addGroupedAvp(437);
            AvpSet ccMoney = request_service_unit.addGroupedAvp(413); // CC-Money
            AvpSet ccMoney_Unit_Value = ccMoney.addGroupedAvp(445);
            ccMoney_Unit_Value.addAvp(447, amount, true, false);
            // Co the ve sau su dung
            //Bundle	10000	Grouped	VOM	Descript a bundle. For multiple bundles, repeate Bundle AVP. Used when debit or credit bundle.
            //Unit-Name	444	UTF8String	VOM	Bundle name (GPRS, VOICE,...)
            //Unit-Value	445 	Grouped	VOM	
            //Value-Digits	447	Unsigned	VOM	Bundle value

            //add avp of CPS: cp_id, sp_id,...
            /**
             * cpid	10111 spid	10112 categoryid	10113 contentid	10114 shortcode
             * 10115 b_isdn	10116
             */
            avpSet.addAvp(10111, Utilities.CPID, true, false, true);
            avpSet.addAvp(10112, Utilities.SP_ID, true, false, true);
            avpSet.addAvp(10113, categoryId, true, false, true);
            avpSet.addAvp(10114, contentId, true, false, true);
            avpSet.addAvp(10115, Utilities.CHARGING_PROXY_SHORT_CODE, true, false, true);
            avpSet.addAvp(10116, (null==extra_info || "".equals(extra_info)) ? bsisdn : extra_info, true, false, true);
            //avpSet.addAvp(10117,((null==extra_info || "".equals(extra_info)) ? "" : extra_info ),true, false, true);
            Future<Message> future = session.send(request);
            Answer response = (Answer) future.get();

            String resultCode = response.getAvps().getAvp(1021).getUTF8String();
            //  String description = response.getAvps().getAvp(1011).getUTF8String();
            //System.out.println(resultCode + " " + description);
            System.out.println("[" + resultCode + "]");
            //  System.out.println("Description-Result-Code-Debit:" + description);
            if (resultCode.equals("CPS-0000")) {
                
                System.out.println("Charging OK!!!");
            } else {
                System.out.println("Charging Fail....");
            }
            log.setResult(resultCode);
            LogDAO.insert(log);
            return resultCode;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            session.release();
        }

    }
    
    public String debitRecharge(String isdn, long amount, String serviceCode, String bsisdn, String contentId, String categoryId, int requestAction) throws Exception {
        return debitRecharge(isdn, amount, serviceCode, bsisdn, contentId, categoryId, requestAction, "");
    }

    /**
     * Refun so tien bi tru của khách hang vao tai khoan
     *
     * @param isdn
     * @param amount
     * @throws Exception
     */
    public String refun(String sessionId, String isdn, long amount, String serviceCode, String bsisdn, String contentId, String categoryId, int requestAction) throws Exception {
        Session session = factory.getNewSession();
        try {

            Request request = session.createRequest(272,
                    org.jdiameter.api.ApplicationId.createByAuthAppId(vendorId,
                            authApplId),
                    serverRealm, serverHost);

            AvpSet avpSet = request.getAvps();
            avpSet.addAvp(263, sessionId, true, false, false);
            avpSet.addAvp(258, authApplId, true);
            avpSet.addAvp(264, Utilities.IP_LOCAL, true, false, true);
            avpSet.addAvp(296, this.ownRealm, true, false, true);

            avpSet.addAvp(293, serverHost, true, false, true);
            avpSet.addAvp(283, serverRealm, true, false, true);

            //add auth_application_id: default=4
            avpSet.addAvp(Avp.AUTH_APPLICATION_ID, 4L, true, false, true);

            //service context id: default=6.32260@3gpp.org
            //AvpCode.SERVICE_CONTEXT_ID: 461
            avpSet.addAvp(461, "6.32260@3gpp.org", true, false, false);
            //CC_REQUEST_TYPE: 416
            avpSet.addAvp(416, 4, true, false); // REQUEST_EVENT
            //CC_REQUEST_NUMBER: 415
            avpSet.addAvp(415, 0, true, false, true);
            //REQUESTED_ACTION: 436
            avpSet.addAvp(436, requestAction, true, false); //1 la refun

            //add isdn
            //SUBSCRIPTION_ID: 443
            AvpSet subId = request.getAvps().addGroupedAvp(443);
            //SUBSCRIPTION_ID_TYPE: 450
            subId.addAvp(450, 0, true, false);
            //SUBSCRIPTION_ID_DATA: 444
            subId.addAvp(444, isdn, true, false, false);
            //add amount
            //MULTIPLE_SERVICES_CREDIT_CONTROL
            AvpSet mscControl = avpSet.addGroupedAvp(456);
            //SERVICE_IDENTIFIER: 439
            mscControl.addAvp(439, 4, true, false);

            //REQUESTED_SERVICE_UNIT: 437
            AvpSet request_service_unit = mscControl.addGroupedAvp(437);
            AvpSet ccMoney = request_service_unit.addGroupedAvp(413); // CC-Money
            AvpSet ccMoney_Unit_Value = ccMoney.addGroupedAvp(445);
            ccMoney_Unit_Value.addAvp(447, amount, true, false);

            if (requestAction == 11 || requestAction == 12) {
                request_service_unit.addAvp(10001, "Recharge_Profile", true, false, false);
            }
            // Co the ve sau su dung
            //Bundle	10000	Grouped	VOM	Descript a bundle. For multiple bundles, repeate Bundle AVP. Used when debit or credit bundle.
            //Unit-Name	444	UTF8String	VOM	Bundle name (GPRS, VOICE,...)
            //Unit-Value	445 	Grouped	VOM	
            //Value-Digits	447	Unsigned	VOM	Bundle value

            //add avp of CPS: cp_id, sp_id,...
            /**
             * cpid	10111 spid	10112 categoryid	10113 contentid	10114 shortcode
             * 10115 b_isdn	10116
             */
            avpSet.addAvp(10111, Utilities.CPID, true, false, true);
            avpSet.addAvp(10112, Utilities.SP_ID, true, false, true);
            avpSet.addAvp(10113, categoryId, true, false, true);
            avpSet.addAvp(10114, contentId, true, false, true);
            avpSet.addAvp(10115, Utilities.SP_SHORT_CODE, true, false, true);
            avpSet.addAvp(10116, bsisdn, true, false, true);

            Future<Message> future = session.send(request);
            Answer response = (Answer) future.get();

            String resultCode = response.getAvps().getAvp(1021).getUTF8String();
            String description = response.getAvps().getAvp(1011).getUTF8String();
            System.out.println(resultCode + " " + description);
            System.out.println(response.getAvps().getAvp(268).getUnsigned32());
            if (resultCode.equals("CPS-0000")) {
                System.out.println("Charging OK!!!");
            } else {
                System.out.println("Charging Fail....");
            }
            return resultCode;
        } finally {
            session.release();
        }

    }

   
}

