/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdeal.ctnet.vn.ws;

import charging.Charging;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import mdeal.ctnet.vn.config.Cog;
import mdeal.ctnet.vn.config.Helper;
import mdeal.ctnet.vn.entity.Reply;
import vn.ctnet.mdeal.bo.*;
import mdeal.ctnet.vn.config.OutputString;
import mdeal.ctnet.vn.entity.Info;
import vn.ctnet.dao.*;
import vn.ctnet.entity.Service;
import vn.ctnet.entity.Users;
import vn.ctnet.entity.Package;
import vn.ctnet.entity.VasLog;
import vn.ctnet.mdeal.config.Utils;

/**
 *
 * @author vanvtse90186
 */
@WebService(serviceName = "CtnetVas")
public class CtnetVas {
    private final String url = "http://10.54.96.35:8080/WSVasmDeal/CtnetVas?wsdl";
    private VasLogDAO logDao = null;
    @Resource
    WebServiceContext wsContext; 
    /**
     * Web service operation
     *
     * @param msisdn
     * @param packageCode
     * @param username
     * @param password
     * @return
     * @throws java.lang.ClassNotFoundException
     */
    @WebMethod(operationName = "Register")
    public Reply Register(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "packageCode") String packageCode, @WebParam(name = "username") String username, @WebParam(name = "password") String password) {
        //TODO write your implementation code here:
        OutputString output = new OutputString();
        if(logDao==null) logDao = new VasLogDAO();
        VasLog log = new VasLog();
        log.setIp(getIP());
        log.setMethod("Register");
        log.setUrl(url);
        log.setParams("msisdn="+msisdn+"&packageCode="+packageCode+"&username="+username+"&password="+password);
        log.setUserName(username);
        log.setChannel("VAS");
        if(Cog.charging==null) Cog.charging = new Charging();
        try {

            UsersDao userDAO = new UsersDao();
            Users user = userDAO.login(username, Utils.getMD5(password));
            if (user == null) {
                try{
                log.setResult("0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU");
                log.setError("User dang nhap khong dung");
                logDao.insert(log);
                } catch(Exception e) {
                //do nothing
                }
                return output.register("0", "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU");
            }
 
            SMSProcess sm = new SMSProcess();
            String rs = sm.register(Helper.initPhoneNumber(msisdn, 3), packageCode, 0, "VAS",Cog.charging);
            
            rs = rs.replace("|", ":");
            String message = rs.replace(":", "|");
            log.setResult(message);
            logDao.insert(log);
            return output.register(rs.split(":")[0], message);
        } catch (Exception e) {
            try {
                log.setResult("0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU");
                log.setError(e.getMessage());
                logDao.insert(log);
            }catch(Exception ex) {
                //do nothing
            }
            
            return output.register("0", "0|DANG_KY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU");
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
    @WebMethod(operationName = "Unregister")
    public Reply Unregister(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "packageCode") String packageCode, @WebParam(name = "username") String username, @WebParam(name = "password") String password) {
        //TODO write your implementation code here:
        OutputString output = new OutputString();
        if(logDao==null) logDao = new VasLogDAO();
        VasLog log = new VasLog();
        log.setIp(getIP());
        log.setMethod("UnRegister");
        log.setUrl(url);
        log.setParams("msisdn="+msisdn+"&packageCode="+packageCode+"&username="+username+"&password="+password);
        log.setUserName(username);
        log.setChannel("VAS");
        try {

            UsersDao userCtrl = new UsersDao();
            Users user = userCtrl.login(username, Utils.getMD5(password));
        
            if (user == null) {
               try{
                   log.setResult("0|HUY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU");
                    log.setError("User dang nhap khong dung");
                    logDao.insert(log);
               } catch(Exception e) {
               
               }
                
                return output.unregister("0", "0|HUY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU");
            }
            //   System.out.println("3");
            SMSProcess sm = new SMSProcess();
            String rs = sm.unregister(Helper.initPhoneNumber(msisdn, 3), packageCode, 0, "VAS");
//sm.unregister(msisdn, packageCode, 0, "VAS");
            //     System.out.println(rs);
            //     System.out.println("4");
            rs = rs.replace("|", ":");
            String message = rs.replace(":", "|");
            log.setResult(message);
            logDao.insert(log);
            return output.unregister(rs.split(":")[0], message);
        } catch (Exception e) {
            try{
                log.setResult("0|HUY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU");
                log.setError(e.getMessage());
                logDao.insert(log);
            }catch(Exception ex) {
            
            }
            
            return output.register("0", "0|HUY_KHONG_THANH_CONG_XIN_VUI_LONG_THU_LAI_SAU");
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
    @WebMethod(operationName = "CheckStatus")
    public Reply CheckStatus(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "username") String username, @WebParam(name = "password") String password) {
        //TODO write your implementation code here:
        VasLog log = new VasLog();
        if(logDao==null) logDao = new VasLogDAO();
        log.setIp(getIP());
        log.setMethod("CheckStatus");
        log.setUrl(url);
        log.setParams("msisdn="+msisdn+"&username="+username+"&password="+password);
        log.setUserName(username);
        log.setChannel("VAS");
        try {
            UsersDao userCtrl = new UsersDao();
            Users user = userCtrl.login(username, Utils.getMD5(password));
            if (user == null) {
                Reply rp = new Reply();
                rp.setErrorId("0");
                rp.setErrorDesc("0|THUE_BAO_CHUA_SU DUNG DICH_VU");
                try{
                    log.setResult("0|THUE_BAO_CHUA_SU DUNG DICH_VU");
                    log.setError("User dang nhap khong dung");
                    logDao.insert(log);
                } catch(Exception e) {
                    //do nothing
                }
                
                return rp;
            }

            SMSProcess sm = new SMSProcess();

            Service rs = sm.checkProfile(Helper.initPhoneNumber(msisdn, 3), 0, "VAS");
            System.out.println("Web service check "+msisdn);
            if (rs != null) {
                System.out.println("get service ok");
                PackageDAO packCtrl = new PackageDAO();
                Package pack = packCtrl.getPackageByID(rs.getPackageID());
                mdeal.ctnet.vn.entity.Subscripton service = new mdeal.ctnet.vn.entity.Subscripton();
                service.setPackageCode(rs.getPackageID());
                service.setCycle(pack.getNumOfDate());
                service.setEndtime(rs.getExpDate());
                service.setPrice((int)pack.getPrice());

                Reply rp = new Reply();
                rp.setErrorId("1");
                rp.setErrorDesc("1|THUE_BAO_DANG_SU DUNG DICH_VU");
                ArrayList<mdeal.ctnet.vn.entity.Subscripton> list = new ArrayList<>();
                list.add(service);
                rp.setSubscriptonList(list);
                try{
                    log.setResult("1|THUE_BAO_DANG_SU DUNG DICH_VU");
                    logDao.insert(log);
                } catch(Exception e) {
                    
                }
                
                return rp;
            } else {
                System.out.println("get service not ok");
                Reply rp = new Reply();
                rp.setErrorId("0");
                rp.setErrorDesc("THUE_BAO_CHUA_SU DUNG DICH_VU");
                try{
                    log.setResult("0|THUE_BAO_CHUA_SU DUNG DICH_VU");
                    logDao.insert(log);
                }catch(Exception e) {
                
                }
                
                return rp;
            }

        } catch (Exception e) {
            Reply rp = new Reply();
            rp.setErrorId("0");
            rp.setErrorDesc("0|THUE_BAO_CHUA_SU DUNG DICH_VU");
            log.setResult("0|THUE_BAO_CHUA_SU DUNG DICH_VU");
            try{
                log.setError(e.getMessage());
                logDao.insert(log);
            } catch(Exception ex) {
                //do nothing
            }
            
            return rp;
        } 
    }

    @WebMethod(operationName = "GetListPackage")
    public Reply GetListPackage(@WebParam(name = "username") String username, @WebParam(name = "password") String password)  {
        //TODO write your implementation code here:
        OutputString output = new OutputString();
        if(logDao==null) logDao = new VasLogDAO();
        VasLog log = new VasLog();
        log.setIp(getIP());
        log.setMethod("GetListPackage");
        log.setUrl(url);
        log.setParams("username="+username+"&password="+password);
        log.setUserName(username);
        log.setChannel("VAS");
        try {
            System.out.println("1");
            UsersDao userCtrl = new UsersDao();
            Users user = userCtrl.login(username, Utils.getMD5(password));
            System.out.println("2");
            if (user == null) {
                Reply rp = new Reply();
//                rp.setErrorId("0");
//                rp.setErrorDesc("INVALID_USER");
                try{
                    log.setResult("0|THUE_BAO_CHUA_SU DUNG DICH_VU");
                    log.setError("User dang nhap khong dung");
                    logDao.insert(log);
                }catch(Exception e)
                {
                    //do nothing
                }
                return rp;
            }

            PackageDAO packCtrl = new PackageDAO();
            List<Package> list = packCtrl.getListPackge();
            ArrayList<Info> listSub = new ArrayList<>();
            for (vn.ctnet.entity.Package list1 : list) {
                Info pack = new Info();
                pack.setPackagecode(list1.getPackageID());
                pack.setCycle(list1.getNumOfDate());
                pack.setPrice((int)list1.getPrice());
                pack.setDesc(list1.getDescription());

                listSub.add(pack);
            }

            Reply rp = new Reply();

            rp.setServiceList(listSub);
            try{
                log.setResult("GET LIST OKIE");
                logDao.insert(log);
            }catch(Exception e) {
                //do nothign
            }
            
            return rp;
        } catch (Exception e) {
            Reply rp = new Reply();
            try{
                log.setResult("NOT OKIE");
                log.setError(e.getMessage());
                logDao.insert(log);
            }catch(Exception ex) {
                //do nothign
            }
            
            return rp;
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
        else if(type==2) {
            if (phone.startsWith("0")) {
                return phone.replaceFirst("0", "");
            } else if (phone.startsWith("84")) {
                return phone.replaceFirst("84", "");
            } else if (phone.startsWith("+84")) {
                return phone.replace("+84", "");
            } else {
                return phone;
            }
        }else{
            return "84"+phone;
        }
    }
    
    private String getIP() {
        try {
            MessageContext mc = wsContext.getMessageContext();
            HttpServletRequest req = (HttpServletRequest)mc.get(MessageContext.SERVLET_REQUEST); 
            return req.getRemoteAddr();
        } catch(Exception e) {
            return "unknow";
        }
        
    }
}
