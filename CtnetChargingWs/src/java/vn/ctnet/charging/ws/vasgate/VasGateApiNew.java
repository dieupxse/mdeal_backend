/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ctnet.charging.ws.vasgate;

import charging.Charging;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import vn.ctnet.config.OutputString;
import vn.ctnet.dao.PackageDAO;
import vn.ctnet.dao.UsersDao;
import vn.ctnet.dao.VasLogDAO;
import vn.ctnet.entity.Service;
import vn.ctnet.entity.Users;
import vn.ctnet.entity.VasLog;
import vn.ctnet.mdeal.bo.ServiceProcess;
import vn.ctnet.mdeal.config.Utils;
import vn.ctnet.mdeal.entity.ReturnRegister;
import vn.ctnet.ws.response.Pkg;
import vn.ctnet.ws.response.WsResponse;

/**
 *
 * @author dieup
 */
@WebService(serviceName = "VasApiNew", targetNamespace = "http://vasgate.mobifone.vn")
public class VasGateApiNew {

    private static Charging charging = null;
    private VasLogDAO logDao = null;
    @Resource
    WebServiceContext wsContext;

    /**
     * Web service operation
     *
     * @param msisdn
     * @param packageCode
     * @param userName
     * @param password
     * @param info
     * @param channel
     * @return
     */
    @WebMethod(operationName = "register")
    public WsResponse register(
            @WebParam(name = "msisdn") String msisdn,
            @WebParam(name = "packagecode") String packageCode,
            @WebParam(name = "channel") String channel,
            @WebParam(name = "username") String userName,
            @WebParam(name = "password") String password,
            @WebParam(name = "info") String info
    ) {
        //TODO write your implementation code here:
        WsResponse response = new WsResponse();
        VasLog log = new VasLog();
        if (logDao == null) {
            logDao = new VasLogDAO();
        }
        log.setIp(getIP());
        log.setMethod("register");
        log.setUrl("VasGateAPINew");
        log.setParams("msisdn=" + msisdn + "&packagecode=" + packageCode + "&username=" + userName + "&password=" + password + "&channel=" + channel + "&info=" + info);
        log.setUserName(userName);
        log.setChannel("VAS_API_NEW");
        try {
            UsersDao userCtrl = new UsersDao();
            Users user = userCtrl.login(userName, Utils.getMD5(password));
            if (user == null || user.getUserName() == null) {
                try {
                    log.setError("User khong ton tai");
                    log.setResult("0|USER DANG NHAP KHONG DUNG");
                    logDao.insert(log);
                } catch (Exception e) {
                }
                response.setReturncode("0");
                response.setReturndes("USER_DANG_NHAP_KHONG_DUNG");
                return response;
            }
        } catch (Exception e) {
        }
        try {
            PackageDAO packCtrl = new PackageDAO();
            vn.ctnet.entity.Package pack = packCtrl.getPackageByID(packageCode);
            if (pack == null || pack.getPackageID() == null) {
                response.setReturncode("0");
                response.setReturndes("LOI HE THONG");
                try {
                    log.setError(response.getReturncode());
                    log.setResult(response.getReturndes());
                    logDao.insert(log);
                } catch (Exception ex) {
                }
                return response;
            }
            ServiceProcess sm = new ServiceProcess();
            String extra = "Kenhphanphoi-" + packageCode + "-mDeal-Ctnet";
            if (charging == null) {
                charging = new Charging();
            }
            ReturnRegister rs = sm.register_direct(initPhoneNumber(msisdn, 3), packageCode, 0, channel, charging, userName, true, extra);
            response.setReturncode(rs.getReturnCode());
            response.setReturndes(rs.getReturnDesc());
            Pkg pkg = new Pkg(rs.getPackageId(), pack.getNumOfDate(), rs.getPrice(), packageCode);
            response.setPkg(pkg);
            try {
                log.setError(rs.getReturnCode());
                log.setResult(rs.getReturnDesc());
                logDao.insert(log);
            } catch (Exception e) {
            }
            return response;
        } catch (Exception e) {
            response.setReturncode("0");
            response.setReturndes("LOI HE THONG");
            try {
                log.setError("0");
                log.setResult(e.getMessage());
                logDao.insert(log);
            } catch (Exception ex) {
            }
            return response;
        }

    }

    /**
     * Web service operation
     *
     * @param msisdn
     * @param packageCode
     * @param username
     * @param password
     * @return
     */
    @WebMethod(operationName = "checkunregister")
    public WsResponse checkUnregister(
            @WebParam(name = "msisdn") String msisdn,
            @WebParam(name = "packagecode") String packageCode,
            @WebParam(name = "username") String userName,
            @WebParam(name = "password") String password) {
        //TODO write your implementation code here:

        //TODO write your implementation code here:
        VasLog log = new VasLog();
        if (logDao == null) {
            logDao = new VasLogDAO();
        }
        log.setIp(getIP());
        log.setMethod("checkunregister");
        log.setUrl("VasGateAPINew");
        log.setParams("msisdn=" + msisdn + "&packagecode=" + packageCode + "&username=" + userName + "&password=" + password);
        log.setUserName(userName);
        log.setChannel("VAS_API_NEW");
        String status = "0";
        WsResponse response = new WsResponse();
        try {
            UsersDao userCtrl = new UsersDao();
            Users user = userCtrl.login(userName, Utils.getMD5(password));

            if (user == null || user.getUserName() == null) {
                try {
                    log.setError("User khong ton tai");
                    log.setResult("0|USER DANG NHAP KHONG DUNG");
                    logDao.insert(log);
                } catch (Exception e) {
                }
                response.setReturncode("0");
                response.setReturndes("0|0|0|0|0");
                return response;
            }
            //   System.out.println("3");
            ServiceProcess sp = new ServiceProcess();
            Service sv = sp.getService(initPhoneNumber(msisdn, 3));

            if (sv == null || sv.getPackageID() == null) {
                try {
                    log.setError("GOI CUOC KHONG TON TAI");
                    log.setResult("0|GOI CUOC KHONG TON TAI");
                    logDao.insert(log);
                } catch (Exception e) {

                }
                response.setReturncode("0");
                response.setReturndes("0|0|0|0|0");
                return response;
            } else {
                if (sv.getPackageID().toUpperCase().equals(packageCode.toUpperCase())) {
                    String rs = sv.getPackageID() + "|" + formatDate(sv.getRegDate()) + "|";
                    if (sv.getStatus().equals("0")) {
                        rs += "0|";
                        rs += formatDate(sv.getModifiedDate()) + "|";
                        status = "1";

                    } else {
                        rs += formatDate(sv.getStartDate()) + "|";
                        rs += "0|";
                        status = "0";
                    }
                    rs += sv.getChannel();
                    try {
                        log.setResult(status + "|" + rs);
                        logDao.insert(log);
                    } catch (Exception e) {

                    }
                    response.setReturncode(status);
                    response.setReturndes(rs);
                    return response;
                } else {
                    try {
                        log.setResult(status + "|KHONG CO GOI CUOC");
                        logDao.insert(log);
                    } catch (Exception e) {

                    }
                    response.setReturncode(status);
                    response.setReturndes("0|0|0|0|0");
                    return response;
                }

            }

        } catch (Exception e) {
            try {
                log.setResult("0|THAO TAC KHONG THANH CONG");
                log.setError(e.getMessage());
                logDao.insert(log);
            } catch (Exception ex) {
            }
            response.setReturncode(status);
            response.setReturndes("0|0|0|0|0");
            return response;
        }
    }

    /**
     * Web service operation
     *
     * @param msisdn
     * @param username
     * @param password
     * @return
     */
    @WebMethod(operationName = "checkstatus")
    public WsResponse checkStatus(
            @WebParam(name = "msisdn") String msisdn,
            @WebParam(name = "username") String userName,
            @WebParam(name = "password") String password
    ) {
        //TODO write your implementation code here:
        WsResponse response = new WsResponse();
        VasLog log = new VasLog();
        if (logDao == null) {
            logDao = new VasLogDAO();
        }
        log.setIp(getIP());
        log.setMethod("checkstatus");
        log.setUrl("VasGateAPINew");
        log.setParams("msisdn=" + msisdn + "&username=" + userName + "&password=" + password);
        log.setUserName(userName);
        log.setChannel("VAS_API_NEW");
        try {
            ServiceProcess sm = new ServiceProcess();
            Service rs = sm.checkProfile(initPhoneNumber(msisdn, 3), 0, "VAS",false);
            System.out.println("Web service check " + msisdn);
            if (rs != null) {
                System.out.println("get service ok");
                PackageDAO packCtrl = new PackageDAO();
                vn.ctnet.entity.Package pack = packCtrl.getPackageByID(rs.getPackageID());
                Pkg pkg = new Pkg(pack.getPackageID(), pack.getNumOfDate(), (int) pack.getPrice(), pack.getPackageName());
                response.setReturncode("1");
                response.setReturndes("THUE_BAO_DANG_SU_DUNG_DICH_VU");
                try {
                    log.setError(response.getReturncode());
                    log.setResult(response.getReturndes());
                    logDao.insert(log);
                } catch (Exception ex) {
                }
                return response;
            } else {
                response.setReturncode("0");
                response.setReturndes("THUE_BAO_CHUA_SU_DUNG_DICH_VU");
                try {
                    log.setError(response.getReturncode());
                    log.setResult(response.getReturndes());
                    logDao.insert(log);
                } catch (Exception ex) {
                }
                return response;
            }

        } catch (Exception e) {
            response.setReturncode("0");
            response.setReturndes("THUE_BAO_CHUA_SU_DUNG_DICH_VU");
            try {
                log.setError(response.getReturncode());
                log.setResult(response.getReturndes());
                logDao.insert(log);
            } catch (Exception ex) {
            }
            return response;
        }
    }

    private String initPhoneNumber(String phone, int type) {
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
        else if (type == 2) {
            if (phone.startsWith("0")) {
                return phone.replaceFirst("0", "");
            } else if (phone.startsWith("84")) {
                return phone.replaceFirst("84", "");
            } else if (phone.startsWith("+84")) {
                return phone.replace("+84", "");
            } else {
                return phone;
            }
        } else {
            return "84" + phone;
        }
    }

    private String getIP() {
        try {
            MessageContext mc = wsContext.getMessageContext();
            HttpServletRequest req = (HttpServletRequest) mc.get(MessageContext.SERVLET_REQUEST);
            return req.getRemoteAddr();
        } catch (Exception e) {
            return "unknow";
        }

    }

    private static String formatDate(Date date) {
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");//dd/MM/yyyy
        String strDate = sdfDate.format(date);
        return strDate;
    }
}
