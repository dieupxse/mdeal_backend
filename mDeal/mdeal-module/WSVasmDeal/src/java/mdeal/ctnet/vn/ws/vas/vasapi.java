/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdeal.ctnet.vn.ws.vas;

import charging.Charging;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import mdeal.ctnet.vn.config.Cog;
import mdeal.ctnet.vn.config.Helper;
import mdeal.ctnet.vn.entity.vasapi.Return;
import mdeal.ctnet.vn.entity.vasapi.ReturnObj;
import vn.ctnet.dao.UsersDao;
import vn.ctnet.dao.VasLogDAO;
import vn.ctnet.entity.Service;
import vn.ctnet.entity.Users;
import vn.ctnet.entity.VasLog;
import vn.ctnet.mdeal.bo.ServiceProcess;
import vn.ctnet.mdeal.config.Utils;
import vn.ctnet.mdeal.entity.ReturnRegister;

/**
 *
 * @author dieup
 */
@WebService(serviceName = "vasapi")
public class vasapi {
    private final String url = "http://10.54.96.35:8080/WSVasmDeal/vasapi?wsdl";
    private VasLogDAO logDao = null;
    @Resource
    WebServiceContext wsContext; 
    /**
     * Web service operation
     *
     * @param msisdn     
     * @param packagecode     
     * @param username
     * @param password
     * @param channel
     * @param info
     * @return
     */
    @WebMethod(operationName = "register")
    public ReturnObj register( @WebParam(name = "msisdn") String msisdn, @WebParam(name = "packagecode") String packagecode, @WebParam(name = "username") String username, @WebParam(name = "password") String password, @WebParam(name="channel")String channel, @WebParam(name="info")String info) {
        //TODO write your implementation code here:
        if(Cog.charging==null) Cog.charging = new Charging();
        VasLog log = new VasLog();
        if(logDao==null) logDao = new VasLogDAO();
        log.setIp(getIP());
        log.setMethod("register");
        log.setUrl(url);
        log.setParams("msisdn="+msisdn+"&packagecode="+packagecode+"&username="+username+"&password="+password+"&channel="+channel+"&info="+info);
        log.setUserName(username);
        log.setChannel(channel);
        try {
            System.out.println("Kiem tra user co ton tai khong? user: "+username+" - Pass: "+password);
            UsersDao userDAO = new UsersDao();
            Users user = userDAO.login(username, Utils.getMD5(password));
            if (user == null || user.getUserName()==null) {
                System.out.println("User khong ton tai ");
                try {
                    log.setError("User khong ton tai");
                    log.setResult("0|USER DANG NHAP KHONG DUNG");
                    logDao.insert(log);
                } catch (Exception e) {
                }
                return Return.Register("0","USER DANG NHAP KHONG DUNG", "", "", 0, "");
            } else {
                System.out.println("User co ton tai - User: "+user.getUserName()+" - Pass: "+user.getPassword());
            }
 
            ServiceProcess sm = new ServiceProcess();
            ReturnRegister rs = sm.register_direct(Helper.initPhoneNumber(msisdn, 3), packagecode, 0, ("".equals(channel) || channel==null ? "MSOCIAL": channel),Cog.charging,username);
            System.out.println(rs.getChargingResult()+"|"+rs.getPackageId()+"|"+rs.getReturnCode()+"|"+rs.getReturnDesc());
            try {
                log.setResult(rs.getReturnCode()+"|"+rs.getReturnDesc()+"|"+rs.getPackageId()+"|"+rs.getPrice());
                logDao.insert(log);
            } catch (Exception e) {
            }
            switch(rs.getReturnCode()) {
                case "1":
                    return Return.Register("1",rs.getReturnDesc(),rs.getPackageId(),"1",rs.getPrice(),"");
                default:
                    return Return.Register(rs.getReturnCode(),rs.getReturnDesc(), "", "", 0, "");
            }
            
           
        } catch (Exception e) {
            try {
                log.setResult("0|THAO TAC KHONG THANH CONG");
                log.setError(e.getMessage());
                logDao.insert(log);
            } catch (Exception ex) {
            }
            return Return.Register("0",e.getMessage(), "", "", 0, "");
        } 

    }
    
    @WebMethod(operationName = "registerfree")
    public ReturnObj registerfree(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "packagecode") String packagecode,@WebParam(name = "price") int price,@WebParam(name = "freeday") int freeday, @WebParam(name = "username") String username, @WebParam(name = "password") String password, @WebParam(name="channel")String channel, @WebParam(name="info")String info) {
        //TODO write your implementation code here:
        if(Cog.charging==null) Cog.charging = new Charging();
        VasLog log = new VasLog();
        if(logDao==null) logDao = new VasLogDAO();
        log.setIp(getIP());
        log.setMethod("registerfree");
        log.setUrl(url);
        log.setParams("msisdn="+msisdn+"&packagecode="+packagecode+"&price="+price+"&freeday="+freeday+"&username="+username+"&password="+password+"&info="+info+"&channel="+channel);
        log.setUserName(username);
        log.setChannel(channel);
        try {
            System.out.println("Kiem tra user co ton tai khong? user: "+username+" - Pass: "+password);
            UsersDao userDAO = new UsersDao();
            Users user = userDAO.login(username, Utils.getMD5(password));
            if (user == null || user.getUserName()==null) {
                System.out.println("User khong ton tai ");
                try {
                    log.setError("User khong ton tai");
                    log.setResult("0|USER DANG NHAP KHONG DUNG");
                    logDao.insert(log);
                } catch (Exception e) {
                }
                return Return.Register("0","USER DANG NHAP KHONG DUNG", "", "", 0, "");
            } else {
                System.out.println("User co ton tai - User: "+user.getUserName()+" - Pass: "+user.getPassword());
            }
 
            ServiceProcess sm = new ServiceProcess();
            ReturnRegister rs = sm.register_free(Helper.initPhoneNumber(msisdn, 3), packagecode, price, freeday,0 ,("".equals(channel) || channel==null ? "MSOCIAL": channel),Cog.charging,username);
            try {
                log.setResult(rs.getReturnCode()+"|"+rs.getReturnDesc()+"|"+rs.getPackageId()+"|"+rs.getPrice());
                logDao.insert(log);
            } catch (Exception e) {
            }
            switch(rs.getReturnCode()) {
                case "1":
                    return Return.Register("1",rs.getReturnDesc(),rs.getPackageId(),"1",rs.getPrice(),"");
                default:
                    return Return.Register(rs.getReturnCode(),rs.getReturnDesc(), "", "", 0, "");
            }
           
        } catch (Exception e) {
            try {
                log.setResult("0|THAO TAC KHONG THANH CONG");
                log.setError(e.getMessage());
                logDao.insert(log);
            } catch (Exception ex) {
            }
            return Return.Register("0",e.getMessage(), "", "", 0, "");
        } 

    }
    
    @WebMethod(operationName = "registerfreecycle")
    public ReturnObj registerfreecycle(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "packagecode") String packagecode,@WebParam(name = "price") int price,@WebParam(name = "cycle") int cycle, @WebParam(name = "username") String username, @WebParam(name = "password") String password, @WebParam(name="channel")String channel, @WebParam(name="info")String info) {
        //TODO write your implementation code here:
        if(Cog.charging==null) Cog.charging = new Charging();
        VasLog log = new VasLog();
        if(logDao==null) logDao = new VasLogDAO();
        log.setIp(getIP());
        log.setMethod("registerfreecycle");
        log.setUrl(url);
        log.setParams("msisdn="+msisdn+"&packagecode="+packagecode+"&price="+price+"&cycle="+cycle+"&username="+username+"&password="+password+"&info="+info+"&channel="+channel);
        log.setUserName(username);
        log.setChannel(channel);
        try {
            System.out.println("Kiem tra user co ton tai khong? user: "+username+" - Pass: "+password);
            UsersDao userDAO = new UsersDao();
            Users user = userDAO.login(username, Utils.getMD5(password));
            if (user == null || user.getUserName()==null) {
                System.out.println("User khong ton tai ");
                try {
                    log.setError("User khong ton tai");
                    log.setResult("0|USER DANG NHAP KHONG DUNG");
                    logDao.insert(log);
                } catch (Exception e) {
                }
                return Return.Register("0","USER DANG NHAP KHONG DUNG", "", "", 0, "");
            } else {
                System.out.println("User co ton tai - User: "+user.getUserName()+" - Pass: "+user.getPassword());
            }
 
            ServiceProcess sm = new ServiceProcess();
            ReturnRegister rs = sm.register_free_cycle(Helper.initPhoneNumber(msisdn, 3), packagecode, price, cycle,0 ,("".equals(channel) || channel==null ? "MSOCIAL": channel),Cog.charging,username);
            try {
                log.setResult(rs.getReturnCode()+"|"+rs.getReturnDesc()+"|"+rs.getPackageId()+"|"+rs.getPrice());
                logDao.insert(log);
            } catch (Exception e) {
            }
            switch(rs.getReturnCode()) {
                case "1":
                    return Return.Register("1",rs.getReturnDesc(),rs.getPackageId(),rs.getCycle()+"",rs.getPrice(),"");
                default:
                    return Return.Register(rs.getReturnCode(),rs.getReturnDesc(), "", "", 0, "");
            }
           
        } catch (Exception e) {
            try {
                log.setResult("0|THAO TAC KHONG THANH CONG");
                log.setError(e.getMessage());
                logDao.insert(log);
            } catch (Exception ex) {
            }
            return Return.Register("0",e.getMessage(), "", "", 0, "");
        } 

    }
    
    @WebMethod(operationName = "unregister")
    public ReturnObj unregister(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "packagecode") String packagecode,@WebParam(name = "username") String username, @WebParam(name = "password") String password, @WebParam(name="channel")String channel, @WebParam(name="info")String info) {
        //TODO write your implementation code here:
        VasLog log = new VasLog();
        if(logDao==null) logDao = new VasLogDAO();
        log.setIp(getIP());
        log.setMethod("unregister");
        log.setUrl(url);
        log.setParams("msisdn="+msisdn+"&packagecode="+packagecode+"&username="+username+"&password="+password+"&info="+info+"&channel="+channel);
        log.setUserName(username);
        log.setChannel(channel);
        try {
            System.out.println("Kiem tra user co ton tai khong? user: "+username+" - Pass: "+password);
            UsersDao userDAO = new UsersDao();
            Users user = userDAO.login(username, Utils.getMD5(password));
            if (user == null || user.getUserName()==null) {
                System.out.println("User khong ton tai ");
                try {
                    log.setError("User khong ton tai");
                    log.setResult("0|USER DANG NHAP KHONG DUNG");
                    logDao.insert(log);
                } catch (Exception e) {
                }
                return Return.CheckUnRegister("0","USER DANG NHAP KHONG DUNG");
            } else {
                System.out.println("User co ton tai - User: "+user.getUserName()+" - Pass: "+user.getPassword());
            }
 
            ServiceProcess sm = new ServiceProcess();
            ReturnRegister rs = sm.unregister_no_mt(Helper.initPhoneNumber(msisdn, 3), packagecode,("".equals(channel) || channel==null ? "MSOCIAL": channel),username);
            try {
                log.setResult(rs.getReturnCode()+"|"+rs.getReturnDesc()+"|"+rs.getPackageId()+"|"+rs.getPrice());
                logDao.insert(log);
            } catch (Exception e) {
            }
            return Return.CheckUnRegister(rs.getReturnCode(),rs.getReturnDesc());
           
        } catch (Exception e) {
            try {
                log.setResult("0|THAO TAC KHONG THANH CONG");
                log.setError(e.getMessage());
                logDao.insert(log);
            } catch (Exception ex) {
            }
            return Return.CheckUnRegister("0","LOI HE THONG");
        } 

    }

    /**
     * Web service operation
     *
     * @param msisdn
     * @param packagecode
     * @param username
     * @param password
     * @return
     */
    @WebMethod(operationName = "checkunregister")
    public ReturnObj checkunregister(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "packagecode") String packagecode, @WebParam(name = "username") String username, @WebParam(name = "password") String password) {
        //TODO write your implementation code here:
        VasLog log = new VasLog();
        if(logDao==null) logDao = new VasLogDAO();
        log.setIp(getIP());
        log.setMethod("checkunregister");
        log.setUrl(url);
        log.setParams("msisdn="+msisdn+"&packagecode="+packagecode+"&username="+username+"&password="+password);
        log.setUserName(username);
        log.setChannel("VAS_API");
        String status = "0";
        try {
            
            UsersDao userCtrl = new UsersDao();
            Users user = userCtrl.login(username, Utils.getMD5(password));
        
            if (user == null || user.getUserName()==null) {
                 try {
                    log.setError("User khong ton tai");
                    log.setResult("0|USER DANG NHAP KHONG DUNG");
                    logDao.insert(log);
                } catch (Exception e) {
                }
                return Return.CheckUnRegister(status,"0|0|0|0|0");
            }
            //   System.out.println("3");
            ServiceProcess sp = new ServiceProcess();
            Service sv = sp.getService(Helper.initPhoneNumber(msisdn,3));
            
            if(sv==null || sv.getPackageID()==null) {
                 try {
                    log.setError("GOI CUOC KHONG TON TAI");
                    log.setResult("0|GOI CUOC KHONG TON TAI");
                    logDao.insert(log);
                } catch (Exception e) {
                
                }
                return Return.CheckUnRegister(status,"0|0|0|0|0");
            } else {
                if(sv.getPackageID().toUpperCase().equals(packagecode.toUpperCase())) {
                    String rs = sv.getPackageID()+"|"+Helper.FormatDate(sv.getRegDate())+"|";
                    
                    if(sv.getStatus().equals("0")) {
                        rs+="0|";
                        rs+=Helper.FormatDate(sv.getModifiedDate())+"|";
                        status = "1";

                    } else {
                        rs+=Helper.FormatDate(sv.getStartDate())+"|";
                        rs+="0|";
                        status = "0";
                    }
                    rs+=sv.getChannel();
                    try {
                        log.setResult(status+"|"+rs);
                        logDao.insert(log);
                    } catch (Exception e) {

                    }
                    return Return.CheckUnRegister(status,rs);
                } else {
                    try {
                        log.setResult(status+"|KHONG CO GOI CUOC");
                        logDao.insert(log);
                    } catch (Exception e) {

                    }
                    return Return.CheckUnRegister(status,"0|0|0|0|0");
                }
                
            }
            
        } catch (Exception e) {
            try {
                log.setResult("0|THAO TAC KHONG THANH CONG");
                log.setError(e.getMessage());
                logDao.insert(log);
            } catch (Exception ex) {
            }
            return Return.CheckUnRegister(status,"0|0|0|0|0");
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
    public ReturnObj checkstatus(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "username") String username, @WebParam(name = "password") String password)  {
        //TODO write your implementation code here:
        VasLog log = new VasLog();
        if(logDao==null) logDao = new VasLogDAO();
        log.setIp(getIP());
        log.setMethod("checkstatus");
        log.setUrl(url);
        log.setParams("msisdn="+msisdn+"&username="+username+"&password="+password);
        log.setUserName(username);
        log.setChannel("VAS_API");
        try {
            UsersDao userCtrl = new UsersDao();
            Users user = userCtrl.login(username, Utils.getMD5(password));
            System.out.println("Kiem tra user co ton tai hay khong");
            if (user == null || user.getUserName()==null) {
                System.out.println("User khong ton tai");
                try {
                    log.setError("User khong ton tai");
                    log.setResult("0|USER DANG NHAP KHONG DUNG");
                    logDao.insert(log);
                } catch (Exception e) {
                }
                return Return.CheckStatus("0","USER DANG NHAP KHONG DUNG","", "", 0, "");
            }
            System.out.println("User co ton tai");
            ServiceProcess sp = new ServiceProcess();
            Service sv = sp.getService(Helper.initPhoneNumber(msisdn, 3));
            System.out.println("Kiem tra service co ton tai hay khong");
            if(sv==null || sv.getPackageID()==null) {
                System.out.println("Service khong ton tai");
                try {
                    log.setResult("0|TB CHUA SU DUNG DICH VU");
                    logDao.insert(log);
                } catch (Exception e) {
                }
                return Return.CheckStatus("0","TB CHUA SU DUNG DICH VU","", "", 0, "");
            } else {
                System.out.println("Service ton tai tra ve ket qua: "+sv.getPackageID()+" - "+sv.getStatus());
                switch(sv.getStatus()) {
                    case "1":
                    case "2":
                    case "3":
                    case "4":
                        vn.ctnet.entity.Package pkg = sp.getPackage(sv.getPackageID());
                        System.out.println("Kiem tra package co ton tai hay khong");
                        if(pkg!=null){
                            System.out.println("Package co ton tai");
                            try {
                                log.setError("");
                                log.setResult("1|TB DANG SU DUNG DICH VU");
                                logDao.insert(log);
                            } catch (Exception e) {
                            }
                            return Return.CheckStatus("1","TB DANG SU DUNG DICH VU",pkg.getPackageID(),"1",(int)pkg.getPrice(),"");
                        } else {
                            try {
                                log.setError("KHONG TON TAI GOI CUOC");
                                log.setResult("1|TB DANG SU DUNG DICH VU");
                                logDao.insert(log);
                            } catch (Exception e) {
                            }
                            System.out.println("Package khong ton tai");
                            return Return.CheckStatus("1","TB DANG SU DUNG DICH VU",sv.getPackageID(),"1",0,"");
                        }
                    default:
                        try {
                            log.setResult("0|TB CHUA SU DUNG DICH VU");
                            logDao.insert(log);
                        } catch (Exception e) {
                        }
                        return Return.CheckStatus("0","TB CHUA SU DUNG DICH VU","", "", 0, "");
                    
                }
            }
            
        } catch (Exception e) {
            System.out.println("Da xay ra loi");
            try {
                log.setResult("0|THAO TAC KHONG THANH CONG");
                log.setError(e.getMessage());
                logDao.insert(log);
            } catch (Exception ex) {
            }
            return Return.CheckStatus("0","LOI HE THONG","", "", 0, "");
        } 
    }
    
    @WebMethod(operationName = "checkstatusbonus")
    public ReturnObj checkstatusbonus(@WebParam(name = "msisdn") String msisdn, @WebParam(name = "packagecode") String packagecode, @WebParam(name = "username") String username, @WebParam(name = "password") String password)  {
        //TODO write your implementation code here:
        VasLog log = new VasLog();
        if(logDao==null) logDao = new VasLogDAO();
        log.setIp(getIP());
        log.setMethod("checkstatusbonus");
        log.setUrl(url);
        log.setParams("msisdn="+msisdn+"&packagecode="+packagecode+"&username="+username+"&password="+password);
        log.setUserName(username);
        log.setChannel("VAS_API");
        try {
            UsersDao userCtrl = new UsersDao();
            Users user = userCtrl.login(username, Utils.getMD5(password));
            System.out.println("Kiem tra user co ton tai hay khong");
            if (user == null || user.getUserName()==null) {
                System.out.println("User khong ton tai");
                try {
                    log.setError("User khong ton tai");
                    log.setResult("0|USER DANG NHAP KHONG DUNG");
                    logDao.insert(log);
                } catch (Exception e) {
                }
                return Return.CheckUnRegister("2","USER DANG NHAP KHONG DUNG");
            }
            System.out.println("User co ton tai");
            ServiceProcess sp = new ServiceProcess();
            ReturnRegister rt = sp.checkstatusbonus(Helper.initPhoneNumber(msisdn, 3),packagecode);
            try {
                log.setResult(rt.getReturnCode()+"|"+rt.getReturnDesc());
                logDao.insert(log);
            } catch (Exception e) {
            }
            return Return.CheckUnRegister(rt.getReturnCode(), rt.getReturnDesc());
            
        } catch (Exception e) {
            System.out.println("Da xay ra loi");
            try {
                log.setResult("2|DA XAY RA LOI");
                log.setError(e.getMessage());
                logDao.insert(log);
            } catch (Exception ex) {
            }
            return Return.CheckUnRegister("2","USER DANG NHAP KHONG DUNG");
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
